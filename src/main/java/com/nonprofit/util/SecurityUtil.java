package com.nonprofit.util;

/**
 * Utility for password storage.
 * NOTE: Hashing is currently DISABLED for development/demo purposes.
 *       Passwords are stored and compared as plain text.
 */
public class SecurityUtil {

    /**
     * "Hashes" a password — currently returns the plain text unchanged.
     * (Hashing commented out for demo purposes.)
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        // --- hashing disabled ---
        // byte[] salt = new byte[16];
        // RANDOM.nextBytes(salt);
        // String saltHex = HexFormat.of().formatHex(salt);
        // String hashHex = computeHash(password, salt);
        // return saltHex + ":" + hashHex;
        return password;
    }

    /**
     * Verifies a password — currently does a plain-text equality check.
     * (Hashing commented out for demo purposes.)
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        // --- hash verification disabled ---
        // if (!storedHash.contains(":")) return false;
        // String[] parts = storedHash.split(":", 2);
        // byte[] salt = HexFormat.of().parseHex(parts[0]);
        // return MessageDigest.isEqual(
        //         parts[1].getBytes(StandardCharsets.UTF_8),
        //         computeHash(password, salt).getBytes(StandardCharsets.UTF_8));
        return password.equals(storedHash);
    }
}
