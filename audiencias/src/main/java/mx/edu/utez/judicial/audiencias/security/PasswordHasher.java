package mx.edu.utez.judicial.audiencias.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.nio.charset.StandardCharsets;

/**
 * Hashes and checks passwords with BCrypt. Each hash carries its own salt and cost, so the value
 * stored in users.password (60 characters) is all that is needed to verify a password later.
 */
public final class PasswordHasher {

    /** Work factor: each +1 doubles the time. 12 takes roughly a quarter of a second. */
    private static final int COST = 12;

    /** BCrypt only reads the first 72 bytes, so longer passwords are rejected instead of truncated. */
    public static final int MAX_PASSWORD_BYTES = 72;

    private PasswordHasher() {
    }

    public static boolean isUsable(String password) {
        return password != null
            && !password.isEmpty()
            && password.getBytes(StandardCharsets.UTF_8).length <= MAX_PASSWORD_BYTES;
    }

    public static String hash(String password) {
        if (!isUsable(password)) {
            throw new IllegalArgumentException(
                "The password must have between 1 and " + MAX_PASSWORD_BYTES + " bytes.");
        }
        return BCrypt.withDefaults().hashToString(COST, password.toCharArray());
    }

    /** False for a wrong password, a malformed hash, null values or an unusable password. */
    public static boolean matches(String password, String hash) {
        if (hash == null || !isUsable(password)) {
            return false;
        }
        try {
            return BCrypt.verifyer().verify(password.toCharArray(), hash).verified;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
