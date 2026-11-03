package com.nonprofit.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * EventManager entity extending User.
 * Represents administrative users responsible for organizing events and tracking volunteer applications.
 */
@Entity
@Table(name = "event_managers")
public class EventManager extends User {

    public EventManager() {
        super();
        setRole("EVENT_MANAGER");
    }

    public EventManager(String name, String contactInfo, String passwordHash) {
        super(name, contactInfo, passwordHash, "EVENT_MANAGER");
    }
}
