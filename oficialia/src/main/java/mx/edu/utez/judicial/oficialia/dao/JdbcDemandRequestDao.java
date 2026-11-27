package mx.edu.utez.judicial.oficialia.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.Annex;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityWarning;
import mx.edu.utez.judicial.oficialia.model.Party;
import mx.edu.utez.judicial.oficialia.model.PartyRole;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import mx.edu.utez.judicial.oficialia.model.User;

/**
 * Demand requests stored in the sgj_oficialia database (tables demand_requests, parties and annexes).
 * Each save also keeps the case file, the duplicity warning decision and the status log up to date
 * (case_files, duplicity_warning_decisions and request_status_history), in the same transaction; a ruling
 * also takes its case file number from case_file_sequences and leaves its record in audit_log.
 */
public class JdbcDemandRequestDao implements DemandRequestDao {

    private static final String REQUEST = "DEMAND_REQUEST";

    private static final String SELECT_REQUESTS = "SELECT r.*, c.name AS court_name FROM demand_requests r "
        + "JOIN courts c ON c.code = r.court_code";

    private final Database database;

    public JdbcDemandRequestDao(Database database) {
        this.database = database;
    }

    @Override
    public List<DemandRequest> findAll() {
        return find("", null);
    }

    @Override
    public List<DemandRequest> findByCourt(String courtName) {
        return find(" WHERE c.name = ?", courtName);
    }

    @Override
    public Optional<DemandRequest> findByFolio(String folio) {
        if (folio == null) {
            return Optional.empty();
        }
        return find(" WHERE r.folio = ?", folio.trim()).stream().findFirst();
    }

    public int count() {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM demand_requests");
             ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Could not count demand requests", e);
        }
    }

    @Override
    public void insert(DemandRequest request) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                insertRequest(connection, request);
                insertParty(connection, request.getFolio(), request.getPlaintiff());
                insertParty(connection, request.getFolio(), request.getDefendant());
                for (Annex annex : request.getAnnexes()) {
                    insertAnnex(connection, request.getFolio(), annex);
                }
                insertWarningDecision(connection, request);
                insertStatusChange(connection, request.getFolio(), null, RequestStatus.PENDING, null,
                    request.getCapturedBy(), registeredAt(request.getReceivedDate()));
                saveRuling(connection, request, RequestStatus.PENDING);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not save demand request " + request.getFolio(), e);
        }
    }

    @Override
    public String admit(DemandRequest request, User clerk, LocalDateTime ruledAt) {
        String folio = request.getFolio();
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                requirePending(connection, folio);
                int year = ruledAt.getYear();
                int sequence = nextSequence(connection, request.getCourtCode(), year);
                String number = String.format("%d/%05d/%s", year, sequence, request.getCourtCode());
                try (PreparedStatement statement = connection.prepareStatement("UPDATE demand_requests SET "
                    + "status = 'ADMISSIBLE', case_file_number = ?, ruled_by = ?, ruled_at = ? WHERE folio = ?")) {
                    statement.setString(1, number);
                    statement.setString(2, clerk.getFullName());
                    statement.setTimestamp(3, timestamp(ruledAt));
                    statement.setString(4, folio);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO case_files "
                    + "(number, folio, court_code, year, sequence, opened_at) VALUES (?, ?, ?, ?, ?, ?)")) {
                    statement.setString(1, number);
                    statement.setString(2, folio);
                    statement.setString(3, request.getCourtCode());
                    statement.setInt(4, year);
                    statement.setInt(5, sequence);
                    statement.setTimestamp(6, timestamp(ruledAt));
                    statement.executeUpdate();
                }
                insertStatusChange(connection, folio, RequestStatus.PENDING, RequestStatus.ADMISSIBLE, null,
                    clerk.getFullName(), ruledAt);
                AuditLog.record(connection, clerk, request.getCourtCode(), "REQUEST_ADMITTED", REQUEST, folio,
                    null, Map.of("caseFileNumber", number));
                connection.commit();
                return number;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not open the case file of request " + folio, e);
        }
    }

    @Override
    public void assignHearingDesk(String caseFileNumber, String deskName, LocalDateTime assignedAt) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String courtCode;
                try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT court_code FROM case_files WHERE number = ? AND desk_name IS NULL FOR UPDATE")) {
                    statement.setString(1, caseFileNumber);
                    try (ResultSet rows = statement.executeQuery()) {
                        if (!rows.next()) {
                            // Unknown, or the desk was already assigned: nothing to change or to log
                            connection.rollback();
                            return;
                        }
                        courtCode = rows.getString(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE case_files SET desk_name = ? WHERE number = ?")) {
                    statement.setString(1, deskName);
                    statement.setString(2, caseFileNumber);
                    statement.executeUpdate();
                }
                // Kept until demand_requests drops its case file columns (see V006)
                try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE demand_requests SET desk_name = ? WHERE case_file_number = ?")) {
                    statement.setString(1, deskName);
                    statement.setString(2, caseFileNumber);
                    statement.executeUpdate();
                }
                // The system assigns the desk, not a person (business rule 6)
                AuditLog.record(connection, null, courtCode, "HEARING_DESK_ASSIGNED", "CASE_FILE", caseFileNumber,
                    null, Map.of("hearingDesk", deskName, "assignedAt", assignedAt.toString()));
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not assign the desk of case file " + caseFileNumber, e);
        }
    }

    @Override
    public void reject(DemandRequest request, String reason, User clerk, LocalDateTime ruledAt) {
        String folio = request.getFolio();
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                requirePending(connection, folio);
                try (PreparedStatement statement = connection.prepareStatement("UPDATE demand_requests SET "
                    + "status = 'INADMISSIBLE', rejection_reason = ?, ruled_by = ?, ruled_at = ? WHERE folio = ?")) {
                    statement.setString(1, reason);
                    statement.setString(2, clerk.getFullName());
                    statement.setTimestamp(3, timestamp(ruledAt));
                    statement.setString(4, folio);
                    statement.executeUpdate();
                }
                insertStatusChange(connection, folio, RequestStatus.PENDING, RequestStatus.INADMISSIBLE, reason,
                    clerk.getFullName(), ruledAt);
                AuditLog.record(connection, clerk, request.getCourtCode(), "REQUEST_REJECTED", REQUEST, folio,
                    reason, null);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not reject request " + folio, e);
        }
    }

    /**
     * Locks the request until the transaction ends and checks it is still pending, so a ruling made by
     * another clerk in the meantime is never overwritten.
     */
    private static void requirePending(Connection connection, String folio) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT status FROM demand_requests WHERE folio = ? FOR UPDATE")) {
            statement.setString(1, folio);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new IllegalStateException("Request " + folio + " does not exist");
                }
                if (RequestStatus.valueOf(rows.getString(1)) != RequestStatus.PENDING) {
                    throw new IllegalStateException("Request " + folio + " is not pending anymore");
                }
            }
        }
    }

    /**
     * Takes the next number of the court for the year in one statement: the first ruling of a year inserts 1,
     * the next ones add 1 to the row, and LAST_INSERT_ID(expr) hands the new value back to this connection.
     * The statement locks the row exclusively until the transaction ends, so a second clerk waits and never
     * gets the same number. (INSERT IGNORE followed by SELECT ... FOR UPDATE deadlocks when two clerks of a
     * court rule at once: both take a shared lock and then both wait for an exclusive one.)
     */
    private static int nextSequence(Connection connection, String courtCode, int year) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO case_file_sequences "
            + "(court_code, year, last_sequence) VALUES (?, ?, LAST_INSERT_ID(1)) "
            + "ON DUPLICATE KEY UPDATE last_sequence = LAST_INSERT_ID(last_sequence + 1)")) {
            statement.setString(1, courtCode);
            statement.setInt(2, year);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT LAST_INSERT_ID()");
             ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getInt(1);
        }
    }

    /** Opens or updates the case file and logs the ruling when the status changed from the stored one. */
    private static void saveRuling(Connection connection, DemandRequest request, RequestStatus stored)
            throws SQLException {
        if (request.getCaseFileNumber() != null) {
            saveCaseFile(connection, request);
        }
        if (request.getStatus() != stored) {
            insertStatusChange(connection, request.getFolio(), stored, request.getStatus(),
                request.getRejectionReason(), request.getRuledBy(), request.getRuledAt());
        }
    }

    /** The case file number has the form AAAA/NNNNN/JJ; a later desk assignment only updates the desk. */
    private static void saveCaseFile(Connection connection, DemandRequest request) throws SQLException {
        String number = request.getCaseFileNumber();
        String[] parts = number.split("/");
        String sql = "INSERT INTO case_files (number, folio, court_code, year, sequence, desk_name, opened_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE desk_name = VALUES(desk_name)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, number);
            statement.setString(2, request.getFolio());
            statement.setString(3, request.getCourtCode());
            statement.setInt(4, Integer.parseInt(parts[0]));
            statement.setInt(5, Integer.parseInt(parts[1]));
            statement.setString(6, request.getDeskName());
            statement.setTimestamp(7, timestamp(request.getRuledAt() == null ? LocalDateTime.now() : request.getRuledAt()));
            statement.executeUpdate();
        }
        // A request saved with its number already given (sample or imported data) moves the sequence past it
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO case_file_sequences "
            + "(court_code, year, last_sequence) VALUES (?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE last_sequence = GREATEST(last_sequence, VALUES(last_sequence))")) {
            statement.setString(1, request.getCourtCode());
            statement.setInt(2, Integer.parseInt(parts[0]));
            statement.setInt(3, Integer.parseInt(parts[1]));
            statement.executeUpdate();
        }
    }

    private static void insertStatusChange(Connection connection, String folio, RequestStatus from, RequestStatus to,
                                           String reason, String changedBy, LocalDateTime changedAt)
            throws SQLException {
        String sql = "INSERT INTO request_status_history (folio, from_status, to_status, reason, changed_by, "
            + "changed_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, folio);
            statement.setString(2, from == null ? null : from.name());
            statement.setString(3, to.name());
            statement.setString(4, reason);
            statement.setString(5, changedBy);
            statement.setTimestamp(6, timestamp(changedAt == null ? LocalDateTime.now() : changedAt));
            statement.executeUpdate();
        }
    }

    /** A request saved after a warning is one whose clerk chose to continue (DFR 3.1). */
    /**
     * The request row is locked, so a ruling and a correction cannot cross. The parties are replaced as a
     * whole; their phone and e-mail come in the corrected request.
     */
    @Override
    public void correct(DemandRequest corrected, DuplicityWarning warning, User clerk, LocalDateTime at) {
        String folio = corrected.getFolio();
        // Read before locking, for the court and the old values of the audit record; the lock rechecks the status
        DemandRequest current = findByFolio(folio)
            .orElseThrow(() -> new IllegalStateException("Request " + folio + " does not exist"));
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                requirePending(connection, folio);
                StringBuilder sql = new StringBuilder("UPDATE demand_requests SET matter = ?, legal_representative = ?");
                if (warning != null) {
                    sql.append(", duplicity_court = ?, duplicity_matter = ?, duplicity_decision = ?");
                }
                sql.append(" WHERE folio = ?");
                try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                    int index = 1;
                    statement.setString(index++, corrected.getMatter());
                    statement.setString(index++, corrected.getLegalRepresentative());
                    if (warning != null) {
                        statement.setString(index++, warning.getCourtName());
                        statement.setString(index++, warning.getMatter());
                        statement.setString(index++, warning.getDecision());
                    }
                    statement.setString(index, folio);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM parties WHERE folio = ?")) {
                    statement.setString(1, folio);
                    statement.executeUpdate();
                }
                insertParty(connection, folio, corrected.getPlaintiff());
                insertParty(connection, folio, corrected.getDefendant());
                if (warning != null) {
                    try (PreparedStatement statement = connection.prepareStatement(
                             "INSERT INTO duplicity_warning_decisions (folio, court_code, matched_court, matched_matter, "
                                 + "decision, decided_by, decided_at) VALUES (?, ?, ?, ?, 'CONTINUE', ?, ?)")) {
                        statement.setString(1, folio);
                        statement.setString(2, current.getCourtCode());
                        statement.setString(3, warning.getCourtName());
                        statement.setString(4, warning.getMatter() == null ? corrected.getMatter() : warning.getMatter());
                        statement.setString(5, clerk.getFullName());
                        statement.setTimestamp(6, timestamp(at));
                        statement.executeUpdate();
                    }
                }
                AuditLog.record(connection, clerk, current.getCourtCode(), "REQUEST_CORRECTED", "DEMAND_REQUEST",
                    folio, null, correctionDetails(current, corrected));
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not correct demand request " + folio, e);
        }
    }

    /** Old and new value of every corrected field, for the audit log. */
    private static Map<String, Object> correctionDetails(DemandRequest before, DemandRequest after) {
        Map<String, Object> details = new LinkedHashMap<>();
        putChange(details, "matter", before.getMatter(), after.getMatter());
        putChange(details, "legalRepresentative", before.getLegalRepresentative(), after.getLegalRepresentative());
        putChange(details, "plaintiff", partyText(before.getPlaintiff()), partyText(after.getPlaintiff()));
        putChange(details, "defendant", partyText(before.getDefendant()), partyText(after.getDefendant()));
        return details;
    }

    private static String partyText(Party party) {
        return party.isIdentified() ? party.getName() + " (" + party.getPersonId() + ")" : null;
    }

    private static void putChange(Map<String, Object> details, String field, String before, String after) {
        if (!Objects.equals(before, after)) {
            Map<String, Object> change = new LinkedHashMap<>();
            change.put("before", before);
            change.put("after", after);
            details.put(field, change);
        }
    }

    private static void insertWarningDecision(Connection connection, DemandRequest request) throws SQLException {
        DuplicityWarning warning = request.getDuplicityWarning();
        if (warning == null) {
            return;
        }
        String sql = "INSERT INTO duplicity_warning_decisions (folio, court_code, matched_court, matched_matter, "
            + "decision, decided_by, decided_at) VALUES (?, ?, ?, ?, 'CONTINUE', ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, request.getFolio());
            statement.setString(2, request.getCourtCode());
            statement.setString(3, warning.getCourtName());
            statement.setString(4, warning.getMatter() == null ? request.getMatter() : warning.getMatter());
            statement.setString(5, request.getCapturedBy());
            statement.setTimestamp(6, timestamp(registeredAt(request.getReceivedDate())));
            statement.executeUpdate();
        }
    }

    /** Registered today means now; an older received date (sample or imported data) keeps its day. */
    private static LocalDateTime registeredAt(LocalDate receivedDate) {
        return receivedDate.equals(LocalDate.now()) ? LocalDateTime.now() : receivedDate.atStartOfDay();
    }

    private static Timestamp timestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private List<DemandRequest> find(String where, String parameter) {
        try (Connection connection = database.getConnection()) {
            Map<String, ResultRow> rowsByFolio = new LinkedHashMap<>();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_REQUESTS + where)) {
                if (parameter != null) {
                    statement.setString(1, parameter);
                }
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        rowsByFolio.put(rows.getString("folio"), ResultRow.read(rows));
                    }
                }
            }
            if (rowsByFolio.isEmpty()) {
                return List.of();
            }
            loadParties(connection, where, parameter, rowsByFolio);
            loadAnnexes(connection, where, parameter, rowsByFolio);
            List<DemandRequest> requests = new ArrayList<>();
            for (ResultRow row : rowsByFolio.values()) {
                requests.add(row.toDemandRequest());
            }
            return requests;
        } catch (SQLException e) {
            throw new DataAccessException("Could not read demand requests", e);
        }
    }

    private static void loadParties(Connection connection, String where, String parameter,
                                    Map<String, ResultRow> rowsByFolio) throws SQLException {
        String sql = "SELECT p.* FROM parties p JOIN demand_requests r ON r.folio = p.folio "
            + "JOIN courts c ON c.code = r.court_code" + where;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) {
                statement.setString(1, parameter);
            }
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    ResultRow owner = rowsByFolio.get(rows.getString("folio"));
                    if (owner == null) {
                        continue;
                    }
                    PartyRole role = PartyRole.valueOf(rows.getString("role"));
                    Party party = new Party(role, rows.getString("name"), rows.getString("person_type"),
                        rows.getString("person_id"), rows.getString("phone"), rows.getString("email"));
                    if (role == PartyRole.PLAINTIFF) {
                        owner.plaintiff = party;
                    } else {
                        owner.defendant = party;
                    }
                }
            }
        }
    }

    private static void loadAnnexes(Connection connection, String where, String parameter,
                                    Map<String, ResultRow> rowsByFolio) throws SQLException {
        String sql = "SELECT a.* FROM annexes a JOIN demand_requests r ON r.folio = a.folio "
            + "JOIN courts c ON c.code = r.court_code" + where + " ORDER BY a.id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) {
                statement.setString(1, parameter);
            }
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    ResultRow owner = rowsByFolio.get(rows.getString("folio"));
                    if (owner != null) {
                        owner.annexes.add(new Annex(rows.getString("file_name"), rows.getInt("size_kb")));
                    }
                }
            }
        }
    }

    static void insertRequest(Connection connection, DemandRequest request) throws SQLException {
        String sql = "INSERT INTO demand_requests (folio, received_date, court_code, matter, legal_representative, "
            + "captured_by, duplicity_court, duplicity_matter, duplicity_decision, status, case_file_number, "
            + "desk_name, rejection_reason, ruled_by, ruled_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        DuplicityWarning warning = request.getDuplicityWarning();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, request.getFolio());
            statement.setDate(2, Date.valueOf(request.getReceivedDate()));
            statement.setString(3, request.getCourtCode());
            statement.setString(4, request.getMatter());
            statement.setString(5, request.getLegalRepresentative());
            statement.setString(6, request.getCapturedBy());
            statement.setString(7, warning == null ? null : warning.getCourtName());
            statement.setString(8, warning == null ? null : warning.getMatter());
            statement.setString(9, warning == null ? null : warning.getDecision());
            statement.setString(10, request.getStatus().name());
            statement.setString(11, request.getCaseFileNumber());
            statement.setString(12, request.getDeskName());
            statement.setString(13, request.getRejectionReason());
            statement.setString(14, request.getRuledBy());
            if (request.getRuledAt() == null) {
                statement.setNull(15, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(15, Timestamp.valueOf(request.getRuledAt()));
            }
            statement.executeUpdate();
        }
    }

    static void insertParty(Connection connection, String folio, Party party) throws SQLException {
        String sql = "INSERT INTO parties (folio, role, name, person_type, person_id, phone, email) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, folio);
            statement.setString(2, party.getRole().name());
            statement.setString(3, party.getName());
            statement.setString(4, party.getPersonType());
            statement.setString(5, party.getPersonId());
            statement.setString(6, party.getPhone());
            statement.setString(7, party.getEmail());
            statement.executeUpdate();
        }
    }

    static void insertAnnex(Connection connection, String folio, Annex annex) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO annexes (folio, file_name, size_kb) VALUES (?, ?, ?)")) {
            statement.setString(1, folio);
            statement.setString(2, annex.getFileName());
            statement.setInt(3, annex.getSizeKb());
            statement.executeUpdate();
        }
    }

    /** One demand_requests row while its parties and annexes are being read. */
    private static final class ResultRow {

        private String folio;
        private LocalDate receivedDate;
        private String courtName;
        private String courtCode;
        private String matter;
        private String legalRepresentative;
        private String capturedBy;
        private DuplicityWarning duplicityWarning;
        private RequestStatus status;
        private String caseFileNumber;
        private String deskName;
        private String rejectionReason;
        private String ruledBy;
        private java.time.LocalDateTime ruledAt;
        private Party plaintiff = Party.unidentified(PartyRole.PLAINTIFF);
        private Party defendant = Party.unidentified(PartyRole.DEFENDANT);
        private final List<Annex> annexes = new ArrayList<>();

        static ResultRow read(ResultSet rows) throws SQLException {
            ResultRow row = new ResultRow();
            row.folio = rows.getString("folio");
            row.receivedDate = rows.getDate("received_date").toLocalDate();
            row.courtName = rows.getString("court_name");
            row.courtCode = rows.getString("court_code");
            row.matter = rows.getString("matter");
            row.legalRepresentative = rows.getString("legal_representative");
            row.capturedBy = rows.getString("captured_by");
            String duplicityCourt = rows.getString("duplicity_court");
            if (duplicityCourt != null) {
                row.duplicityWarning = new DuplicityWarning(duplicityCourt, rows.getString("duplicity_matter"),
                    rows.getString("duplicity_decision"));
            }
            row.status = RequestStatus.valueOf(rows.getString("status"));
            row.caseFileNumber = rows.getString("case_file_number");
            row.deskName = rows.getString("desk_name");
            row.rejectionReason = rows.getString("rejection_reason");
            row.ruledBy = rows.getString("ruled_by");
            Timestamp ruledAt = rows.getTimestamp("ruled_at");
            row.ruledAt = ruledAt == null ? null : ruledAt.toLocalDateTime();
            return row;
        }

        DemandRequest toDemandRequest() {
            DemandRequest request = new DemandRequest(folio, receivedDate, courtName, courtCode, matter, plaintiff,
                defendant, legalRepresentative, capturedBy, annexes, duplicityWarning);
            if (status == RequestStatus.ADMISSIBLE) {
                request.recordAdmissible(caseFileNumber, deskName, ruledBy, ruledAt);
            } else if (status == RequestStatus.INADMISSIBLE) {
                request.recordInadmissible(rejectionReason, ruledBy, ruledAt);
            }
            return request;
        }
    }
}
