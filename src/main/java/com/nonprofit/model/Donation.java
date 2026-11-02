package com.nonprofit.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Base class for all donation types (money, food, clothes).
 */
@Entity
@Table(name = "donation")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donation_id")
    protected Long donationId;

    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    protected Donor donor;

    @Column(name = "donated_at", nullable = false)
    protected LocalDateTime donatedAt = LocalDateTime.now();

    protected Donation() {
    }

    public abstract Receipt processDonation();

    public Long getDonationId() {
        return donationId;
    }

    public Donor getDonor() {
        return donor;
    }

    public void setDonor(Donor donor) {
        this.donor = donor;
    }

    public LocalDateTime getDonatedAt() {
        return donatedAt;
    }
}
