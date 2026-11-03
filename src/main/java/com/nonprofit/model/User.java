package com.nonprofit.model;

import com.nonprofit.pattern.strategy.ICommunication;
import jakarta.persistence.*;

/**
 * Abstract base User entity representing any registered user in the ATHAR system.
 * Uses JOINED inheritance mapping so each role subclass maintains its own table.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    protected String name;

    @Column(name = "contact_info", nullable = false, unique = true)
    protected String contactInfo;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", nullable = false)
    private String role;

    @Transient
    private ICommunication communicationPreference;

    public User() {
    }

    public User(String name, String contactInfo, String passwordHash, String role) {
        this.name = name;
        this.contactInfo = contactInfo;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public ICommunication getCommunicationPreference() {
        return communicationPreference;
    }

    public void setCommunicationPreference(ICommunication communicationPreference) {
        this.communicationPreference = communicationPreference;
    }

    /**
     * UML defined notification method. Delegates to communicationPreference strategy when configured.
     */
    public void notify(String message) {
        if (communicationPreference != null) {
            communicationPreference.send(message, this);
        } else {
            System.out.println("[Notification to " + contactInfo + "]: " + message);
        }
    }
}
