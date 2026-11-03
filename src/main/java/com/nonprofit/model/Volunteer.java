package com.nonprofit.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Volunteer entity extending User.
 * Represents volunteers with associated skills, preferences, and logged hours.
 */
@Entity
@Table(name = "volunteers")
public class Volunteer extends User {

    @Column(name = "volunteer_hours", nullable = false)
    private int volunteerHours = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "volunteer_skills", joinColumns = @JoinColumn(name = "volunteer_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "volunteer_preferences", joinColumns = @JoinColumn(name = "volunteer_id"))
    @Column(name = "preference")
    private List<String> preferences = new ArrayList<>();

    public Volunteer() {
        super();
        setRole("VOLUNTEER");
    }

    public Volunteer(String name, String contactInfo, String passwordHash) {
        super(name, contactInfo, passwordHash, "VOLUNTEER");
        this.volunteerHours = 0;
    }

    public int getVolunteerHours() {
        return volunteerHours;
    }

    public void setVolunteerHours(int volunteerHours) {
        this.volunteerHours = volunteerHours;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills != null ? skills : new ArrayList<>();
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences != null ? preferences : new ArrayList<>();
    }

    public void addSkill(String skill) {
        if (skill != null && !skill.trim().isEmpty()) {
            this.skills.add(skill.trim());
        }
    }

    public void addPreference(String preference) {
        if (preference != null && !preference.trim().isEmpty()) {
            this.preferences.add(preference.trim());
        }
    }
}
