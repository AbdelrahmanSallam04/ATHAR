package com.nonprofit.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "donor")
public class Donor extends User {

    @OneToMany(mappedBy = "donor")
    private List<Donation> donationHistory = new ArrayList<>();

    protected Donor() {
    }

    public Donor(String name, String contactInfo) {
        super(name, contactInfo);
    }

    /** Links the donation to this donor; call donation.processDonation() afterwards. */
    public void makeDonation(Donation donation) {
        donation.setDonor(this);
        donationHistory.add(donation);
    }

    public List<Donation> getDonationHistory() {
        return donationHistory;
    }
}
