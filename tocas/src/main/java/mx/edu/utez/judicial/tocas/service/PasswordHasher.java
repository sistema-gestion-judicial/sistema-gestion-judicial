package mx.edu.utez.judicial.tocas.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** PBKDF2 password hashing (JDK only, no extra libraries). */
public final class PasswordHasher {
    private static final int ITERATIONS = 120_000;

    private PasswordHasher() {}

    public static String newSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), ITERATIONS, 256);
            byte[] out = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash password", e);
        }
    }

    public static boolean matches(String password, String salt, String expectedHash) {
        byte[] a = hash(password, salt).getBytes();
        byte[] b = expectedHash.getBytes();
        return MessageDigest.isEqual(a, b);
    }
}
