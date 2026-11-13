package mx.edu.utez.judicial.oficialia.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

/**
 * Verification code sent by e-mail to recover a password (RF 1.3). It expires, and wrong guesses are counted
 * so that a short numeric code cannot be found by trying them all.
 */
public class RecoveryCode {

    private final String email;
    private final String code;
    private final LocalDateTime expiresAt;
    private int failedAttempts;

    public RecoveryCode(String email, String code, LocalDateTime expiresAt) {
        this.email = email;
        this.code = code;
        this.expiresAt = expiresAt;
    }

    public String getEmail() {
        return email;
    }

    public boolean isExpired(LocalDateTime now) {
        return !now.isBefore(expiresAt);
    }

    /** Compares every byte, so the response time does not reveal how many digits were right. */
    public boolean matches(String candidate) {
        if (candidate == null) {
            return false;
        }
        return MessageDigest.isEqual(code.getBytes(StandardCharsets.UTF_8),
            candidate.getBytes(StandardCharsets.UTF_8));
    }

    /** Counts a wrong guess and returns how many have been made. */
    public synchronized int registerFailedAttempt() {
        failedAttempts++;
        return failedAttempts;
    }

    /** Leaves the code out, so it cannot reach a log. */
    @Override
    public String toString() {
        return "RecoveryCode[email=" + email + "]";
    }
}
