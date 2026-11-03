package com.nonprofit.service;

import com.nonprofit.model.*;
import com.nonprofit.repository.UserRepository;
import com.nonprofit.util.SecurityUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer handling Authentication and Signup CRUD business logic.
 * Direct subclass instantiation via switch-case (no UserFactory).
 */
public class AuthService {

    private final UserRepository userRepository;
    private final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();

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
     * Authenticates an existing user and issues an active session token.
     */
    public UserSession authenticate(String contactInfo, String plainPassword) {
        if (contactInfo == null || plainPassword == null) {
            throw new IllegalArgumentException("Contact info and password are required.");
        }

        String normalizedContact = contactInfo.trim().toLowerCase();
        User user = userRepository.findByContactInfo(normalizedContact)
                .orElseThrow(() -> new IllegalArgumentException("Invalid contact info or password."));

        if (!SecurityUtil.verifyPassword(plainPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid contact info or password.");
        }

        String token = UUID.randomUUID().toString();
        UserSession session = new UserSession(token, user.getId(), user.getName(), user.getContactInfo(), user.getRole());
        activeSessions.put(token, session);
        return session;
    }

    public void logout(String sessionToken) {
        if (sessionToken != null) {
            activeSessions.remove(sessionToken);
        }
    }

    public Optional<UserSession> getSession(String sessionToken) {
        if (sessionToken == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(activeSessions.get(sessionToken));
    }

    /**
     * Session record holding authenticated user details in-memory.
     */
    public record UserSession(String token, Long userId, String name, String contactInfo, String role) {
    }
}
