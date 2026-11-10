package mx.edu.utez.judicial.oficialia.config;

/**
 * Values of the sign in and session rules. Change them only here.
 */
public final class AuthSettings {

    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final int LOCK_MINUTES = 15;
    public static final int SESSION_TIMEOUT_MINUTES = 20;

    // Password recovery (RF 1.3)
    public static final int RECOVERY_CODE_MINUTES = 15;
    public static final int RECOVERY_CODE_LENGTH = 6;
    /** Wrong guesses allowed per code; a short numeric code could otherwise be found by trying them all. */
    public static final int RECOVERY_MAX_FAILED_ATTEMPTS = 5;
    public static final int PASSWORD_MIN_LENGTH = 8;

    private AuthSettings() {
    }
}
