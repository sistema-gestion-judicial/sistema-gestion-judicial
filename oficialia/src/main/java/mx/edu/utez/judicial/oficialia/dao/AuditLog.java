package mx.edu.utez.judicial.oficialia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import mx.edu.utez.judicial.common.api.ApiJson;
import mx.edu.utez.judicial.oficialia.model.User;

/**
 * Writes the legal audit trail (audit_log, V008) inside the caller's transaction, so an act and its record
 * are saved together or not at all (DFR 3.3.9, business rule 5).
 */
final class AuditLog {

    private AuditLog() {
    }

    /**
     * @param actor the user who acted, or null when the system acted on its own
     * @param details extra values saved as JSON, or null
     * @throws IllegalArgumentException when the user has no database id: every act needs an identified author
     */
    static void record(Connection connection, User actor, String courtCode, String action, String entityType,
                       String entityId, String reason, Map<String, Object> details) throws SQLException {
        if (actor != null && actor.getId() == null) {
            throw new IllegalArgumentException("The audit log needs the database id of " + actor.getEmail());
        }
        String sql = "INSERT INTO audit_log (actor_type, actor_user_id, court_code, action, entity_type, entity_id, "
            + "reason, details) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, actor == null ? "SYSTEM" : "USER");
            if (actor == null) {
                statement.setNull(2, Types.BIGINT);
            } else {
                statement.setLong(2, actor.getId());
            }
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
