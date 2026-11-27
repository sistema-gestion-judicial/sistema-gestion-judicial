package mx.edu.utez.judicial.oficialia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import mx.edu.utez.judicial.common.api.ApiJson;
import mx.edu.utez.judicial.oficialia.model.User;

/**
 * Writes the legal audit trail (audit_log, V007) inside the caller's transaction, so an act and its record
 * are saved together or not at all (DFR 3.3.9, business rule 5).
 */
final class AuditLog {

    private AuditLog() {
    }

    /**
     * The author is found by e-mail in the same statement. An e-mail with no account leaves the user id
     * null, which the table refuses, so the act is rolled back instead of being saved without an author.
     *
     * @param actor the user who acted, or null when the system acted on its own
     * @param details extra values saved as JSON, or null
     */
    static void record(Connection connection, User actor, String courtCode, String action, String entityType,
                       String entityId, String reason, Map<String, Object> details) throws SQLException {
        String sql = "INSERT INTO audit_log (actor_type, actor_user_id, court_code, action, entity_type, entity_id, "
            + "reason, details) VALUES (?, (SELECT id FROM users WHERE email = ?), ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, actor == null ? "SYSTEM" : "USER");
            statement.setString(2, actor == null ? null : actor.getEmail());
            statement.setString(3, courtCode);
            statement.setString(4, action);
            statement.setString(5, entityType);
            statement.setString(6, entityId);
            statement.setString(7, reason);
            statement.setString(8, details == null ? null : ApiJson.write(details));
            statement.executeUpdate();
        }
    }
}
