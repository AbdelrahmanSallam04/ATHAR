package com.nonprofit.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Donor entity extending User.
 * Represents users who make monetary or in-kind donations.
 */
@Entity
@Table(name = "donor")
public class Donor extends User {

    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Donation> donationHistory = new ArrayList<>();

    public Donor() {
        super();
        setRole("DONOR");
    }

    public Donor(String name, String contactInfo) {
        super(name, contactInfo, null, "DONOR");
    }

    public Donor(String name, String contactInfo, String passwordHash) {
        super(name, contactInfo, passwordHash, "DONOR");
    }

    /**
     * Associates a donation with this donor and adds it to their donation history.
     */
    public void makeDonation(Donation donation) {
        donation.setDonor(this);
        donationHistory.add(donation);
    }

    public List<Donation> getDonationHistory() {
        return donationHistory;
    }

    public void setDonationHistory(List<Donation> donationHistory) {
        this.donationHistory = donationHistory;
    }
}
