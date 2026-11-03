package com.nonprofit.model;

import jakarta.persistence.*;

@Entity
@Table(name = "food_item")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_id", nullable = false)
    private FoodDonation donation;

    @Column(name = "food_type", nullable = false)
    private String foodType;

    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @Column(name = "is_perishable")
    private boolean perishable = false;

    public FoodItem() {
    }

    public FoodItem(String foodType, int quantity, boolean perishable) {
        this.foodType = foodType;
        this.quantity = quantity;
        this.perishable = perishable;
    }

    public String getDescription() {
        return quantity + "x " + foodType + (perishable ? " (Refrigerated)" : "");
    }

    public Long getId() {
        return id;
    }

    public FoodDonation getDonation() {
        return donation;
    }

    public void setDonation(FoodDonation donation) {
        this.donation = donation;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isPerishable() {
        return perishable;
    }

    public void setPerishable(boolean perishable) {
        this.perishable = perishable;
    }
}
