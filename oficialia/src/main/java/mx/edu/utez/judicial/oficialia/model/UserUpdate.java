package mx.edu.utez.judicial.oficialia.model;

/**
 * The corrected data of an existing user (RF 1.6). There is no password and no court on purpose: the
 * password is changed only by its owner, and a user never moves to another court from this option.
 * The phone is optional (null when there is none).
 */
public record UserUpdate(long id, String firstName, String lastName, String email, String phone, UserRole role) {
}
