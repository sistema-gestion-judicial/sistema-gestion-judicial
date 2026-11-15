package mx.edu.utez.judicial.oficialia.dao;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import mx.edu.utez.judicial.oficialia.model.RecoveryCode;

/**
 * Keeps the recovery codes in memory, shared by every servlet of the application. They are lost when Tomcat
 * restarts, which only means asking for a new code.
 */
public class InMemoryRecoveryCodeDao implements RecoveryCodeDao {

    private static final InMemoryRecoveryCodeDao INSTANCE = new InMemoryRecoveryCodeDao();

    private final Map<String, RecoveryCode> codesByEmail = new ConcurrentHashMap<>();

    public static InMemoryRecoveryCodeDao getInstance() {
        return INSTANCE;
    }

    @Override
    public void save(RecoveryCode code) {
        codesByEmail.put(code.getEmail(), code);
    }

    @Override
    public Optional<RecoveryCode> findByEmail(String email) {
        return Optional.ofNullable(codesByEmail.get(email));
    }

    @Override
    public void delete(String email) {
        codesByEmail.remove(email);
    }
}
