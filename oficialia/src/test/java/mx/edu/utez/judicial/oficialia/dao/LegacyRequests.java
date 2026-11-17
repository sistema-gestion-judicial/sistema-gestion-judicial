package mx.edu.utez.judicial.oficialia.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.Annex;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityWarning;
import mx.edu.utez.judicial.oficialia.model.Party;

/**
 * Saves requests the way the application did before V006: only demand_requests, parties and annexes, with the
 * columns they had then. The upgrade tests use it to fill a database that does not have the newer tables yet,
 * so it keeps its own SQL instead of the DAO's, which writes the newer columns.
 */
final class LegacyRequests {

    private LegacyRequests() {
    }

    static void insert(Database database, DemandRequest request) {
        try (Connection connection = database.getConnection()) {
            insertRequest(connection, request);
            insertParty(connection, request.getFolio(), request.getPlaintiff());
            insertParty(connection, request.getFolio(), request.getDefendant());
            for (Annex annex : request.getAnnexes()) {
                insertAnnex(connection, request.getFolio(), annex);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not save " + request.getFolio(), e);
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
            statement.setTimestamp(15, request.getRuledAt() == null ? null : Timestamp.valueOf(request.getRuledAt()));
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
}
