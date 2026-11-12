package mx.edu.utez.judicial.oficialia.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Generates the temporary password given to a newly registered user. It always has letters and numbers,
 * so it passes the same rules as a password chosen by the user, and it leaves out look-alike characters
 * (0/O, 1/l/I) because somebody has to read it aloud or copy it by hand.
 */
public final class TemporaryPassword {

    public static final int LENGTH = 12;

    private static final String LETTERS = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String DIGITS = "23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private TemporaryPassword() {
    }

    public static String generate() {
        return generate(RANDOM);
    }

    static String generate(Random random) {
        List<Character> characters = new ArrayList<>();
        // One of each kind guaranteed, the rest from both
        characters.add(pick(LETTERS, random));
        characters.add(pick(DIGITS, random));
        String all = LETTERS + DIGITS;
        while (characters.size() < LENGTH) {
            characters.add(pick(all, random));
        }
        Collections.shuffle(characters, random);
        StringBuilder password = new StringBuilder(LENGTH);
        characters.forEach(password::append);
        return password.toString();
    }

    private static char pick(String alphabet, Random random) {
        return alphabet.charAt(random.nextInt(alphabet.length()));
    }
}
