package com.nonprofit.model;

import jakarta.persistence.*;

/**
 * Base class for every user type (Donor, Volunteer, Beneficiary, ...).
 * Kept minimal until the login module is merged.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    protected Long userId;

    @Column(name = "name", nullable = false)
    protected String name;

    @Column(name = "contact_info")
    protected String contactInfo;

    protected User() {
    }

    protected User(String name, String contactInfo) {
        this.name = name;
        this.contactInfo = contactInfo;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
}
