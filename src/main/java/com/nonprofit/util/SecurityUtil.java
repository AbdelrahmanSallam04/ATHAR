package com.nonprofit.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Utility for secure password hashing and verification using standard Java cryptography.
 * Encapsulates SHA-256 hashing with cryptographically secure random salting.
 */
public class SecurityUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Hashes a plaintext password with a freshly generated salt.
     * Returns a string formatted as "salt:hash" in hexadecimal.
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltHex = HexFormat.of().formatHex(salt);
        String hashHex = computeHash(password, salt);
        return saltHex + ":" + hashHex;
    }

    /**
     * Verifies whether the provided plaintext password matches the stored "salt:hash".
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        String[] parts = storedHash.split(":", 2);
        String saltHex = parts[0];
        String originalHashHex = parts[1];

        try {
            byte[] salt = HexFormat.of().parseHex(saltHex);
            String computedHash = computeHash(password, salt);
            return MessageDigest.isEqual(
                    originalHashHex.getBytes(StandardCharsets.UTF_8),
                    computedHash.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }

    private static String computeHash(String password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JVM", e);
        }
    }
}
