package com.nonprofit.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Donor entity extending User.
 * Represents users who make donations and purchase tickets.
 */
@Entity
@Table(name = "donors")
public class Donor extends User {

    public Donor() {
        super();
        setRole("DONOR");
    }

    public Donor(String name, String contactInfo, String passwordHash) {
        super(name, contactInfo, passwordHash, "DONOR");
    }

    /**
     * Stub method defined in UML for future Donation module implementation.
     */
    public void makeDonation(Donation donation) {
        // Will be implemented in Donation module
        System.out.println("Donor " + getName() + " made a donation: " + donation);
    }
}
