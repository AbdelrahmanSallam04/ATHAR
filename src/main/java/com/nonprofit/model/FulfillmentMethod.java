package com.nonprofit.model;

public enum FulfillmentMethod {
    OFFICE_DROPOFF("Office Drop-off"),
    COURIER_PICKUP("Courier Pickup");

    private final String displayName;

    FulfillmentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

