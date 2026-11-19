
package mx.edu.utez.judicial.audiencias.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.audiencias.model.CaseDocument;
import mx.edu.utez.judicial.common.db.Database;

public class JdbcCaseDocumentDao implements CaseDocumetDao {

    private static final String SELECT =
        "SELECT id, case_id, url, description, uploaded_at, sequence_number FROM case_documents";

    private final Database database;

    public JdbcCaseDocumentDao(Database database) {
        this.database = database;
    }

    @Override
    public Optional<CaseDocument> findById(int id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT + " WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(toDocument(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read document " + id, e);
        }
    }

    @Override
    public List<CaseDocument> findByCaseId(int caseId) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(SELECT + " WHERE case_id = ? ORDER BY sequence_number")) {
            statement.setInt(1, caseId);
            try (ResultSet rows = statement.executeQuery()) {
                List<CaseDocument> documents = new ArrayList<>();
                while (rows.next()) {
                    documents.add(toDocument(rows));
                }
                return documents;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the documents of case " + caseId, e);
        }
    }

    @Override
    public int insert(CaseDocument document) {
        String sql = "INSERT INTO case_documents (case_id, url, description, sequence_number) VALUES (?, ?, ?, ?)";
        try (Connection connection = database.getConnection()) {
            int sequenceNumber = nextSequenceNumber(connection, document.getCaseId());
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                if (document.getCaseId() == null) {
                    statement.setNull(1, Types.INTEGER);
                } else {
                    statement.setInt(1, document.getCaseId());
                }
                statement.setString(2, document.getUrl());
                statement.setString(3, document.getDescription());
                statement.setInt(4, sequenceNumber);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not save document " + document.getUrl(), e);
        }
    }

    /**
     * Next free position within the case file: 1 for a document with no case yet, otherwise
     * one past the highest {@code sequence_number} already used by that case.
     */
    private static int nextSequenceNumber(Connection connection, Integer caseId) throws SQLException {
        if (caseId == null) {
            return 1;
        }
        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT COALESCE(MAX(sequence_number), 0) + 1 FROM case_documents WHERE case_id = ?")) {
            statement.setInt(1, caseId);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getInt(1);
            }
        }
    }

    @Override
    public void delete(int id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM case_documents WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete document " + id, e);
        }
    }

    private static CaseDocument toDocument(ResultSet rows) throws SQLException {
        return new CaseDocument(rows.getInt("id"), rows.getObject("case_id", Integer.class), rows.getString("url"),
            rows.getString("description"), rows.getTimestamp("uploaded_at").toLocalDateTime(),
            rows.getInt("sequence_number"));
    }
}
