package mx.edu.utez.judicial.oficialia.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.Annex;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityWarning;
import mx.edu.utez.judicial.oficialia.model.Party;
import mx.edu.utez.judicial.oficialia.model.PartyRole;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;

/**
 * Demand requests stored in the sgj_oficialia database (tables demand_requests, parties and annexes).
 */
public class JdbcDemandRequestDao implements DemandRequestDao {

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
    public void update(DemandRequest request) {
        String sql = "UPDATE demand_requests SET status = ?, case_file_number = ?, desk_name = ?, "
            + "rejection_reason = ?, ruled_by = ?, ruled_at = ? WHERE folio = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, request.getStatus().name());
            statement.setString(2, request.getCaseFileNumber());
            statement.setString(3, request.getDeskName());
            statement.setString(4, request.getRejectionReason());
            statement.setString(5, request.getRuledBy());
            statement.setTimestamp(6, request.getRuledAt() == null ? null : Timestamp.valueOf(request.getRuledAt()));
            statement.setString(7, request.getFolio());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update demand request " + request.getFolio(), e);
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
