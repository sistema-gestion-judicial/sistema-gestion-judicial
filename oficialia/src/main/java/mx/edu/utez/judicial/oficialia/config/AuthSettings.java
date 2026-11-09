package mx.edu.utez.judicial.oficialia.config;

/**
 * Values of the sign in and session rules. Change them only here.
 */
public final class AuthSettings {

    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final int LOCK_MINUTES = 15;

    private AuthSettings() {
    }
}
