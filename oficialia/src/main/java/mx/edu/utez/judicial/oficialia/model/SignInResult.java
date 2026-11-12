package mx.edu.utez.judicial.oficialia.model;

/**
 * Outcome of a sign in attempt. The user is only present when the status is SIGNED_IN,
 * and lockedMinutes is only meaningful when it is LOCKED.
 */
public record SignInResult(Status status, User user, long lockedMinutes) {

    public enum Status {
        SIGNED_IN,
        INVALID_CREDENTIALS,
        INACTIVE_ACCOUNT,
        LOCKED
    }

    public static SignInResult signedIn(User user) {
        return new SignInResult(Status.SIGNED_IN, user, 0);
    }

    public static SignInResult invalidCredentials() {
        return new SignInResult(Status.INVALID_CREDENTIALS, null, 0);
    }

    public static SignInResult inactiveAccount() {
        return new SignInResult(Status.INACTIVE_ACCOUNT, null, 0);
    }

    public static SignInResult locked(long minutes) {
        return new SignInResult(Status.LOCKED, null, minutes);
    }
}
