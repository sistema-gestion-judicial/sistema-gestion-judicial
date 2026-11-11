package mx.edu.utez.judicial.oficialia.service;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Creates and checks salted password hashes with PBKDF2-HMAC-SHA256, using only the JDK.
 * A stored value has the form iterations:salt:hash, with salt and hash in Base64, so every
 * password keeps the parameters it was created with.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 600_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final String SEPARATOR = ":";
    private static final int STORED_PARTS = 3;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    /** Hashes the password with a new random salt and returns the value to store. */
    public static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = derive(password, salt, ITERATIONS);
        return ITERATIONS + SEPARATOR + encode(salt) + SEPARATOR + encode(hash);
    }

    /**
     * Tells whether the password produces the stored hash.
     *
     * @throws IllegalArgumentException when the stored value does not have the expected form
     */
    public static boolean matches(String password, String storedHash) {
        String[] parts = storedHash.split(SEPARATOR);
        if (parts.length != STORED_PARTS) {
            throw new IllegalArgumentException("The stored hash must have the form iterations:salt:hash");
        }
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = decode(parts[1]);
        byte[] expected = decode(parts[2]);

        // isEqual always compares every byte, so the response time does not reveal how many matched
        return MessageDigest.isEqual(derive(password, salt, iterations), expected);
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, HASH_BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("The JDK could not compute " + ALGORITHM, e);
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] decode(String text) {
        return Base64.getDecoder().decode(text);
    }
}
