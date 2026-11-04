package com.nonprofit.service;

import com.nonprofit.model.*;
import com.nonprofit.repository.UserRepository;
import com.nonprofit.util.SecurityUtil;

import java.util.List;

/**
 * Service layer handling Authentication and Signup CRUD business logic.
 * Direct subclass instantiation via switch-case (no UserFactory).
 */
public class AuthService {

    private final UserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user based on their chosen role and role-specific fields.
     */
    public User registerUser(String name, String contactInfo, String plainPassword, String role,
                             List<String> skills, List<String> preferences) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (contactInfo == null || contactInfo.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact info is required.");
        }
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required.");
        }

        String normalizedContact = contactInfo.trim().toLowerCase();
        if (userRepository.existsByContactInfo(normalizedContact)) {
            throw new IllegalStateException("An account with this contact information already exists.");
        }

        String passwordHash = SecurityUtil.hashPassword(plainPassword);
        String normalizedRole = role.trim().toUpperCase();

        User user = switch (normalizedRole) {
            case "DONOR" -> new Donor(name.trim(), normalizedContact, passwordHash);
            case "VOLUNTEER" -> {
                Volunteer volunteer = new Volunteer(name.trim(), normalizedContact, passwordHash);
                if (skills != null) {
                    skills.forEach(volunteer::addSkill);
                }
                if (preferences != null) {
                    preferences.forEach(volunteer::addPreference);
                }
                yield volunteer;
            }
            case "BENEFICIARY" -> new Beneficiary(name.trim(), normalizedContact, passwordHash);
            case "EVENT_MANAGER" -> new EventManager(name.trim(), normalizedContact, passwordHash);
            case "BENEFICIARY_MANAGER" -> new BeneficiaryManager(name.trim(), normalizedContact, passwordHash);
            default -> throw new IllegalArgumentException("Unsupported user role: " + role);
        };

        userRepository.save(user);
        return user;
    }

    /**
     * Authenticates an existing user by verifying credentials.
     *
     * @param contactInfo   the email or phone number
     * @param plainPassword the plain text password
     * @return the authenticated User entity
     */
    public User authenticate(String contactInfo, String plainPassword) {
        if (contactInfo == null || plainPassword == null) {
            throw new IllegalArgumentException("Contact info and password are required.");
        }

        String normalizedContact = contactInfo.trim().toLowerCase();
        User user = userRepository.findByContactInfo(normalizedContact)
                .orElseThrow(() -> new IllegalArgumentException("Invalid contact info or password."));

        if (!SecurityUtil.verifyPassword(plainPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid contact info or password.");
        }

        return user;
    }
}
