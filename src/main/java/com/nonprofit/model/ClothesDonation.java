package com.nonprofit.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clothes_donation")
public class ClothesDonation extends Donation {

    @OneToMany(mappedBy = "donation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClothingItem> items = new ArrayList<>();

    public ClothesDonation() {
    }

    public void addItem(ClothingItem item) {
        item.setDonation(this);
        items.add(item);
    }

    public void removeItem(ClothingItem item) {
        items.remove(item);
        item.setDonation(null);
    }

    public int getTotalItemsCount() {
        return items.stream().mapToInt(ClothingItem::getQuantity).sum();
    }

    @Override
    public Receipt processDonation() {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("At least one clothing item must be added.");
        }
        if (donor == null) {
            throw new IllegalStateException("Donation must have an associated donor.");
        }

        if (fulfillmentMethod == FulfillmentMethod.COURIER_PICKUP) {
            if (pickupAddress == null || !pickupAddress.isValid()) {
                throw new IllegalStateException("Valid pickup address is required.");
            }
            this.status = DonationStatus.PENDING;
        } else {
            this.status = DonationStatus.AWAITING_DROPOFF;
        }

        int totalQuantity = getTotalItemsCount();
        StringBuilder desc = new StringBuilder("Clothes Donation (" + totalQuantity + " items): ");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) desc.append(", ");
            desc.append(items.get(i).getDescription());
        }

        String details;
        if (fulfillmentMethod == FulfillmentMethod.COURIER_PICKUP) {
            details = "Courier pickup scheduled from: " + pickupAddress.getFormattedAddress()
                    + " | Our team will contact you within 48 hours.";
        } else {
            details = "Please drop off items at ATHAR Main Center: 12 El-Thawra St, Heliopolis. "
                    + "Working hours: Sun-Thu 9am-5pm. Show your Donation ID at reception.";
        }

        String methodTitle = fulfillmentMethod == FulfillmentMethod.COURIER_PICKUP
                ? "Courier Pickup" : "Office Drop-off";

        return new Receipt(donor.getName(), 0.0, methodTitle, desc.toString(), details);
    }

    public List<ClothingItem> getItems() {
        return items;
    }

    public void setItems(List<ClothingItem> items) {
        this.items = items;
    }
}
