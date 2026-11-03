package com.nonprofit.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * BeneficiaryManager entity extending User.
 * Represents administrative users responsible for evaluating beneficiary needs and resource allocation.
 */
@Entity
@Table(name = "beneficiary_managers")
public class BeneficiaryManager extends User {

    public BeneficiaryManager() {
        super();
        setRole("BENEFICIARY_MANAGER");
    }

    public BeneficiaryManager(String name, String contactInfo, String passwordHash) {
        super(name, contactInfo, passwordHash, "BENEFICIARY_MANAGER");
    }
}
