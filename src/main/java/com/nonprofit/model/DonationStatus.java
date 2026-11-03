package com.nonprofit.model;

public enum DonationStatus {
    PENDING("Pending Pickup Dispatch"),
    AWAITING_DROPOFF("Awaiting Office Drop-off"),
    RECEIVED("Received & Verified"),
    CANCELLED("Cancelled");

    private final String displayName;

    DonationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

