package com.nonprofit.model;

import jakarta.persistence.*;

/**
 * Entity representing a physical address associated with a User.
 * Saved once per user and reused across multiple donations or requests.
 */
@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "location_type")
    private String locationType = "Home"; // "Home", "Office", "Other"

    @Column(name = "city", nullable = false)
    private String city = "Cairo";

    @Column(name = "street_address", nullable = false)
    private String streetAddress;

    @Column(name = "building", nullable = false)
    private String building;

    @Column(name = "floor")
    private String floor;

    @Column(name = "apartment")
    private String apartment;

    @Column(name = "notes")
    private String notes;

    public Address() {
    }

    public Address(String locationType, String city, String streetAddress, String building,
                   String floor, String apartment, String notes) {
        this.locationType = locationType;
        this.city = city;
        this.streetAddress = streetAddress;
        this.building = building;
        this.floor = floor;
        this.apartment = apartment;
        this.notes = notes;
    }

    public boolean isValid() {
        if (streetAddress == null || streetAddress.trim().isEmpty()) {
            return false;
        }
        if (building == null || building.trim().isEmpty()) {
            return false;
        }
        if ("Home".equalsIgnoreCase(locationType)) {
            if (floor == null || floor.trim().isEmpty()) {
                return false;
            }
            if (apartment == null || apartment.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (locationType != null && !locationType.isBlank()) {
            sb.append("[").append(locationType).append("] ");
        }
        if (streetAddress != null) sb.append(streetAddress);
        if (building != null && !building.isBlank()) sb.append(", Bldg ").append(building);
        if (floor != null && !floor.isBlank()) sb.append(", Fl ").append(floor);
        if (apartment != null && !apartment.isBlank()) sb.append(", Apt ").append(apartment);
        if (city != null && !city.isBlank()) sb.append(", ").append(city);
        if (notes != null && !notes.isBlank()) sb.append(" (").append(notes).append(")");
        return sb.toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return getFormattedAddress();
    }
}
