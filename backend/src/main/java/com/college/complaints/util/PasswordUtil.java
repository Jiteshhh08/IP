package com.college.complaints.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {

    private static final String PREFIX = "pbkdf2:";
    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;

    private PasswordUtil() {
    }

    public static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = hash(password, salt, ITERATIONS, KEY_LENGTH);
        Base64.Encoder encoder = Base64.getEncoder();
        return PREFIX + ITERATIONS + ":" + encoder.encodeToString(salt) + ":" + encoder.encodeToString(hash);
    }

    public static boolean matches(String rawPassword, String stored) {
        if (rawPassword == null || stored == null || !stored.startsWith(PREFIX)) {
            return false;
        }
        try {
            String[] parts = stored.split(":");
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = hash(rawPassword, salt, iterations, expected.length * 8);
            return MessageDigest.isEqual(actual, expected);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static byte[] hash(String password, byte[] salt, int iterations, int bitLength) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, bitLength);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("Password hashing failed", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
