package com.nonprofit.service;

import com.nonprofit.model.*;
import com.nonprofit.repository.DonorRepository;
import com.nonprofit.repository.InKindDonationRepository;
import com.nonprofit.util.UserSession;

import java.util.List;

/**
 * Service orchestrating In-Kind (Food & Clothes) donation processing,
 * address management, and coordination with repositories.
 */
public class InKindDonationService {

    private final InKindDonationRepository inKindDonationRepository;
    private final DonorRepository donorRepository;

    public InKindDonationService() {
        this(new InKindDonationRepository(), new DonorRepository());
    }

    public InKindDonationService(InKindDonationRepository inKindDonationRepository, DonorRepository donorRepository) {
        this.inKindDonationRepository = inKindDonationRepository;
        this.donorRepository = donorRepository;
    }

    /**
     * Retrieves the current donor from the session (with addresses loaded from DB),
     * or falls back to the latest donor in the database.
     */
    public Donor getActiveDonor() {
        Donor current = UserSession.getCurrentDonor();

        // If logged in, re-fetch from DB so Hibernate-managed addresses are available
        if (current != null && current.getId() != null) {
            Donor fresh = donorRepository.findById(current.getId()).orElse(null);
            if (fresh != null) {
                UserSession.setCurrentDonor(fresh);
                return fresh;
            }
        }

        // Fallback: latest donor in the database (legacy / no-auth path)
        Donor active = donorRepository.findLatestDonorWithAddresses();
        if (active == null) {
            active = new Donor("Registered Donor", "01000000000");
            donorRepository.save(active);
        }
        UserSession.setCurrentDonor(active);
        return active;
    }

    /**
     * Persists a new address linked to the active donor.
     */
    public Address saveNewAddress(Donor donor, Address address) {
        return donorRepository.saveAddress(donor, address);
    }

    /**
     * Submits in-kind donations. Handles clothes-only, food-only, or combined batches atomically.
     */
    public Receipt submitInKindDonations(Donor donor, List<ClothingItem> clothingItems,
                                         List<FoodItem> foodItems, FulfillmentMethod method,
                                         Address address) {
        return inKindDonationRepository.saveInKindDonations(
                donor, clothingItems, foodItems, method, address
        );
    }
}
