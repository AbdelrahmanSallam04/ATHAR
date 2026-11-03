package com.nonprofit.pattern.strategy;

import com.nonprofit.model.User;

/**
 * Strategy interface for communication mechanisms as defined in the UML Diagram.
 * Concrete implementations (e.g., Email, SMS) will be added in the Communication module.
 */
public interface ICommunication {
    void send(String message, User user);
}

