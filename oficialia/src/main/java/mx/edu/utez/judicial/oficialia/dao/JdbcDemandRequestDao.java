package mx.edu.utez.judicial.oficialia.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 */
public class JdbcDemandRequestDao implements DemandRequestDao {

    /** The matter comes from matter_id and the case file from case_files, the tables that replace the old columns. */
    private static final String SELECT_REQUESTS = "SELECT r.*, c.name AS court_name, m.name AS matter_name, "
        + "cf.number AS case_file, cf.hearing_desk_name AS hearing_desk FROM demand_requests r "
        + "JOIN courts c ON c.code = r.court_code JOIN subject_matters m ON m.id = r.matter_id "
        + "LEFT JOIN case_files cf ON cf.folio = r.folio";
    private static final Pattern CASE_FILE = Pattern.compile("(\\d{4})/(\\d{5})/(\\d{2})");
    private static final String REQUEST = "DEMAND_REQUEST";

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
                if (request.getStatus() == RequestStatus.ADMISSIBLE) {
                    insertCaseFile(connection, request);
                }
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
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int year = ruledAt.getYear();
                int sequence = nextSequence(connection, request.getCourtCode(), year);
                String number = String.format("%d/%05d/%s", year, sequence, request.getCourtCode());
                markRuled(connection, request.getFolio(), "UPDATE demand_requests SET status = 'ADMISSIBLE', "
                    + "case_file_number = ?, ruled_by = ?, ruled_by_user_id = ?, ruled_at = ? "
                    + "WHERE folio = ? AND status = 'PENDING'", number, clerk, ruledAt);
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO case_files "
                    + "(number, folio, court_code, year, sequence, hearing_desk_pending, opened_at) "
                    + "VALUES (?, ?, ?, ?, ?, TRUE, ?)")) {
                    statement.setString(1, number);
                    statement.setString(2, request.getFolio());
                    statement.setString(3, request.getCourtCode());
                    statement.setInt(4, year);
                    statement.setInt(5, sequence);
                    statement.setTimestamp(6, Timestamp.valueOf(ruledAt));
                    statement.executeUpdate();
                }
                AuditLog.record(connection, clerk, request.getCourtCode(), "REQUEST_ADMITTED", REQUEST,
                    request.getFolio(), null, Map.of("caseFileNumber", number));
                connection.commit();
                return number;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not open the case file of request " + request.getFolio(), e);
        }
    }

    @Override
    public void assignHearingDesk(String caseFileNumber, String deskName, LocalDateTime assignedAt) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement("UPDATE case_files SET "
                    + "hearing_desk_name = ?, hearing_desk_pending = FALSE, hearing_desk_assigned_at = ? "
                    + "WHERE number = ?")) {
                    statement.setString(1, deskName);
                    statement.setTimestamp(2, Timestamp.valueOf(assignedAt));
                    statement.setString(3, caseFileNumber);
                    statement.executeUpdate();
                }
                // Kept until the old column is dropped, for code that still reads it
                try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE demand_requests SET desk_name = ? WHERE case_file_number = ?")) {
                    statement.setString(1, deskName);
                    statement.setString(2, caseFileNumber);
                    statement.executeUpdate();
                }
                Matcher number = CASE_FILE.matcher(caseFileNumber);
                String courtCode = number.matches() ? number.group(3) : null;
                // The system assigns the desk, not a person (business rule 6)
                AuditLog.record(connection, null, courtCode, "HEARING_DESK_ASSIGNED", "CASE_FILE", caseFileNumber,
                    null, Map.of("hearingDesk", deskName));
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
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // The reason catalog has no screen yet, so every reason is recorded as OTHER with its text
                markRuled(connection, request.getFolio(), "UPDATE demand_requests SET status = 'INADMISSIBLE', "
                    + "rejection_reason = ?, rejection_reason_code = 'OTHER', ruled_by = ?, ruled_by_user_id = ?, "
                    + "ruled_at = ? WHERE folio = ? AND status = 'PENDING'", reason, clerk, ruledAt);
                AuditLog.record(connection, clerk, request.getCourtCode(), "REQUEST_REJECTED", REQUEST,
                    request.getFolio(), reason, null);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not reject request " + request.getFolio(), e);
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

    /** Runs the ruling UPDATE, which only matches a pending request. */
    private static void markRuled(Connection connection, String folio, String sql, String value, User clerk,
                                  LocalDateTime ruledAt) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            statement.setString(2, clerk.getFullName());
            if (clerk.getId() == null) {
                statement.setNull(3, Types.BIGINT);
            } else {
                statement.setLong(3, clerk.getId());
            }
            statement.setTimestamp(4, Timestamp.valueOf(ruledAt));
            statement.setString(5, folio);
            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException("Request " + folio + " is not pending anymore");
            }
        }
    }

    /** The case file of an admissible request loaded with the demo data, and its court's sequence. */
    private static void insertCaseFile(Connection connection, DemandRequest request) throws SQLException {
        Matcher number = CASE_FILE.matcher(request.getCaseFileNumber());
        if (!number.matches()) {
            throw new IllegalArgumentException("Invalid case file number " + request.getCaseFileNumber());
        }
        int year = Integer.parseInt(number.group(1));
        int sequence = Integer.parseInt(number.group(2));
        Timestamp openedAt = Timestamp.valueOf(request.getRuledAt());
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO case_files (number, folio, "
            + "court_code, year, sequence, hearing_desk_name, hearing_desk_pending, hearing_desk_assigned_at, "
            + "opened_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, request.getCaseFileNumber());
            statement.setString(2, request.getFolio());
            statement.setString(3, request.getCourtCode());
            statement.setInt(4, year);
            statement.setInt(5, sequence);
            statement.setString(6, request.getDeskName());
            statement.setBoolean(7, request.getDeskName() == null);
            statement.setTimestamp(8, request.getDeskName() == null ? null : openedAt);
            statement.setTimestamp(9, openedAt);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO case_file_sequences "
            + "(court_code, year, last_sequence) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE "
            + "last_sequence = GREATEST(last_sequence, ?)")) {
            statement.setString(1, request.getCourtCode());
            statement.setInt(2, year);
            statement.setInt(3, sequence);
            statement.setInt(4, sequence);
            statement.executeUpdate();
        }
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

    private static void insertRequest(Connection connection, DemandRequest request) throws SQLException {
        String sql = "INSERT INTO demand_requests (folio, received_date, court_code, matter, legal_representative, "
            + "captured_by, duplicity_court, duplicity_matter, duplicity_decision, status, case_file_number, "
            + "desk_name, rejection_reason, ruled_by, ruled_at, matter_id, has_duplicity_warning, "
            + "rejection_reason_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
            + "(SELECT id FROM subject_matters WHERE name = ?), ?, ?)";
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
            statement.setString(16, request.getMatter());
            statement.setBoolean(17, warning != null);
            statement.setString(18, request.getStatus() == RequestStatus.INADMISSIBLE ? "OTHER" : null);
            statement.executeUpdate();
        }
    }

    private static void insertParty(Connection connection, String folio, Party party) throws SQLException {
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

    private static void insertAnnex(Connection connection, String folio, Annex annex) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO annexes (folio, file_name, size_kb, size_bytes) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, folio);
            statement.setString(2, annex.getFileName());
            statement.setInt(3, annex.getSizeKb());
            statement.setLong(4, annex.getSizeKb() * 1024L);
            statement.executeUpdate();
        }
    }

    /** One demand_requests row while its parties and annexes are being read. */
    private static final class ResultRow {

        private String folio;
        private java.time.LocalDate receivedDate;
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
            row.matter = rows.getString("matter_name");
            row.legalRepresentative = rows.getString("legal_representative");
            row.capturedBy = rows.getString("captured_by");
            String duplicityCourt = rows.getString("duplicity_court");
            if (duplicityCourt != null) {
                row.duplicityWarning = new DuplicityWarning(duplicityCourt, rows.getString("duplicity_matter"),
                    rows.getString("duplicity_decision"));
            }
            row.status = RequestStatus.valueOf(rows.getString("status"));
            // case_files has every case file since V007; the old columns are only a fallback
            String caseFile = rows.getString("case_file");
            row.caseFileNumber = caseFile != null ? caseFile : rows.getString("case_file_number");
            row.deskName = caseFile != null ? rows.getString("hearing_desk") : rows.getString("desk_name");
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
