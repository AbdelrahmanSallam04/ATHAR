package com.nonprofit.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_donation")
public class FoodDonation extends Donation {

    @OneToMany(mappedBy = "donation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FoodItem> items = new ArrayList<>();

    public FoodDonation() {
    }

    public void addItem(FoodItem item) {
        item.setDonation(this);
        items.add(item);
    }

    public void removeItem(FoodItem item) {
        items.remove(item);
        item.setDonation(null);
    }

    public int getTotalItemsCount() {
        return items.stream().mapToInt(FoodItem::getQuantity).sum();
    }

    public boolean hasPerishableItems() {
        return items.stream().anyMatch(FoodItem::isPerishable);
    }

    @Override
    public Receipt processDonation() {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("At least one food item must be added.");
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
        StringBuilder desc = new StringBuilder("Food Donation (" + totalQuantity + " items/units): ");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) desc.append(", ");
            desc.append(items.get(i).getDescription());
        }

        String details;
        if (fulfillmentMethod == FulfillmentMethod.COURIER_PICKUP) {
            details = "Courier pickup scheduled from: " + pickupAddress.getFormattedAddress()
                    + (hasPerishableItems() ? " [EXPRESS: Perishable items included]" : "")
                    + " | Our team will contact you within 24-48 hours.";
        } else {
            details = "Please drop off items at ATHAR Food Distribution Hub: 12 El-Thawra St, Heliopolis. "
                    + "Working hours: Sun-Thu 9am-5pm. Show your Donation ID at reception.";
        }

        String methodTitle = fulfillmentMethod == FulfillmentMethod.COURIER_PICKUP
                ? "Courier Pickup" : "Office Drop-off";

        return new Receipt(donor.getName(), 0.0, methodTitle, desc.toString(), details);
    }

    @Override
    public String getTypeLabel() {
        return "Food";
    }

    @Override
    public String getSummary() {
        StringBuilder summary = new StringBuilder(getTotalItemsCount() + " units: ");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) summary.append(", ");
            summary.append(items.get(i).getDescription());
        }
        return summary.toString();
    }

    @Override
    public int getItemCount() {
        return getTotalItemsCount();
    }

    public List<FoodItem> getItems() {
        return items;
    }

    public void setItems(List<FoodItem> items) {
        this.items = items;
    }
}
