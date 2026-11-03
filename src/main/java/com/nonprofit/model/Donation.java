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

    // ----- Self-description used by lists such as the profile's donation history -----

    /** Short type name, e.g. "Money", "Food", "Clothes". */
    public abstract String getTypeLabel();

    /** One-line summary of what was donated, e.g. "EGP 500.00" or "12 units: Rice, Oil". */
    public abstract String getSummary();

    /** How the donation is delivered or paid. Money donations override this with the payment method. */
    public String getMethodLabel() {
        return fulfillmentMethod != null ? fulfillmentMethod.getDisplayName() : "—";
    }

    /** Money value of the donation; 0 for in-kind donations. */
    public double getMonetaryValue() {
        return 0;
    }

    /** Number of physical items; 0 for money donations. */
    public int getItemCount() {
        return 0;
    }

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
