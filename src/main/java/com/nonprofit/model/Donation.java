package com.nonprofit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Base abstract class for all donation types (Money, Food, Clothes).
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    protected DonationStatus status = DonationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_method")
    protected FulfillmentMethod fulfillmentMethod = FulfillmentMethod.OFFICE_DROPOFF;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id")
    protected Address pickupAddress;

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

    public DonationStatus getStatus() {
        return status;
    }

    public void setStatus(DonationStatus status) {
        this.status = status;
    }

    public FulfillmentMethod getFulfillmentMethod() {
        return fulfillmentMethod;
    }

    public void setFulfillmentMethod(FulfillmentMethod fulfillmentMethod) {
        this.fulfillmentMethod = fulfillmentMethod;
    }

    public Address getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(Address pickupAddress) {
        this.pickupAddress = pickupAddress;
    }
}
