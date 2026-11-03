package com.nonprofit.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clothing_item")
public class ClothingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_id", nullable = false)
    private ClothesDonation donation;

    @Column(name = "clothing_type", nullable = false)
    private String clothingType;

    @Column(name = "gender_category")
    private String genderCategory = "Unisex"; // "Men", "Women", "Kids", "Unisex"

    @Column(name = "size")
    private String size = "M"; // "S", "M", "L", "XL", "Kids"

    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    public ClothingItem() {
    }

    public ClothingItem(String clothingType, String genderCategory, String size, int quantity) {
        this.clothingType = clothingType;
        this.genderCategory = genderCategory;
        this.size = size;
        this.quantity = quantity;
    }

    public String getDescription() {
        return quantity + "x " + clothingType + " (" + genderCategory + ", Size " + size + ")";
    }

    public Long getId() {
        return id;
    }

    public ClothesDonation getDonation() {
        return donation;
    }

    public void setDonation(ClothesDonation donation) {
        this.donation = donation;
    }

    public String getClothingType() {
        return clothingType;
    }

    public void setClothingType(String clothingType) {
        this.clothingType = clothingType;
    }

    public String getGenderCategory() {
        return genderCategory;
    }

    public void setGenderCategory(String genderCategory) {
        this.genderCategory = genderCategory;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

