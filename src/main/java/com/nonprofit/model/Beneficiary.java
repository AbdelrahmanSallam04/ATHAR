package com.nonprofit.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Beneficiary entity extending User.
 * Represents recipients of aids, services, and resources.
 */
@Entity
@Table(name = "beneficiaries")
public class Beneficiary extends User {

    public Beneficiary() {
        super();
        setRole("BENEFICIARY");
    }

    public Beneficiary(String name, String contactInfo, String passwordHash) {
        super(name, contactInfo, passwordHash, "BENEFICIARY");
    }
}
