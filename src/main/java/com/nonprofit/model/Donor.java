package com.nonprofit.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "donor")
public class Donor extends User {

    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Donation> donationHistory = new ArrayList<>();

    public Donor() {
    }

    public Donor(String name, String contactInfo) {
        super(name, contactInfo);
    }

    public void makeDonation(Donation donation) {
        donation.setDonor(this);
        donationHistory.add(donation);
    }

    public List<Donation> getDonationHistory() {
        return donationHistory;
    }
}
