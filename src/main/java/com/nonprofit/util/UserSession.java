package com.nonprofit.util;

import com.nonprofit.model.Donor;

/**
 * Holds the currently authenticated user for the session.
 * Used across the platform to avoid re-asking for donor info on every screen.
 */
public final class UserSession {

    private static Donor currentDonor;

    private UserSession() {
    }

    public static Donor getCurrentDonor() {
        return currentDonor;
    }

    public static void setCurrentDonor(Donor donor) {
        currentDonor = donor;
    }

    public static boolean isLoggedIn() {
        return currentDonor != null;
    }
}

