package mx.edu.utez.judicial.oficialia.model;

/**
 * Outcome of a sign in attempt. The user is only present when the status is SIGNED_IN.
 */
public record SignInResult(Status status, User user) {

    public enum Status {
        SIGNED_IN,
        INVALID_CREDENTIALS,
        INACTIVE_ACCOUNT
    }

    public static SignInResult signedIn(User user) {
        return new SignInResult(Status.SIGNED_IN, user);
    }

    public static SignInResult invalidCredentials() {
        return new SignInResult(Status.INVALID_CREDENTIALS, null);
    }

    public static SignInResult inactiveAccount() {
        return new SignInResult(Status.INACTIVE_ACCOUNT, null);
    }
}
