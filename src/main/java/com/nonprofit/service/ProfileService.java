package com.nonprofit.service;

import com.nonprofit.model.Address;
import com.nonprofit.model.Donation;
import com.nonprofit.model.Donor;
import com.nonprofit.repository.DonationRepository;
import com.nonprofit.repository.DonorRepository;
import com.nonprofit.repository.UserRepository;
import com.nonprofit.util.UserSession;

import java.util.List;

/**
 * Business logic for the donor's profile: viewing and editing personal info,
 * managing saved addresses and reading the donation history.
 */
public class ProfileService {

    /** Totals shown in the profile's stat cards. */
    public record ProfileStats(double totalMoney, int donationCount, int itemCount) {
    }

    private final DonorRepository donorRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;

    public ProfileService() {
        this(new DonorRepository(), new DonationRepository(), new UserRepository());
    }

    public ProfileService(DonorRepository donorRepository, DonationRepository donationRepository,
                          UserRepository userRepository) {
        this.donorRepository = donorRepository;
        this.donationRepository = donationRepository;
        this.userRepository = userRepository;
    }

    /** The logged-in donor as stored at login (no database call), or null. */
    public Donor getSessionDonor() {
        Donor current = UserSession.getCurrentDonor();
        return current == null || current.getId() == null ? null : current;
    }

    /**
     * Returns the logged-in donor freshly loaded from the database, or null if
     * nobody is logged in.
     */
    public Donor getCurrentDonor() {
        Donor current = UserSession.getCurrentDonor();
        if (current == null || current.getId() == null) {
            return null;
        }
        Donor fresh = donorRepository.findById(current.getId()).orElse(current);
        UserSession.setCurrentDonor(fresh);
        return fresh;
    }

    public List<Donation> getDonationHistory(Donor donor) {
        return donationRepository.findByDonor(donor.getId());
    }

    public ProfileStats calculateStats(List<Donation> donations) {
        double totalMoney = donations.stream().mapToDouble(Donation::getMonetaryValue).sum();
        int itemCount = donations.stream().mapToInt(Donation::getItemCount).sum();
        return new ProfileStats(totalMoney, donations.size(), itemCount);
    }

    /**
     * Saves a new name and contact info for the donor.
     *
     * @throws IllegalArgumentException if a field is empty or the contact info belongs to another account
     */
    public Donor updateProfile(Donor donor, String name, String contactInfo) {
        String newName = name == null ? "" : name.trim();
        String newContact = contactInfo == null ? "" : contactInfo.trim();
        if (newName.isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (newContact.isEmpty()) {
            throw new IllegalArgumentException("Email or phone is required.");
        }
        userRepository.findByContactInfo(newContact).ifPresent(owner -> {
            if (!owner.getId().equals(donor.getId())) {
                throw new IllegalArgumentException("This email or phone is already used by another account.");
            }
        });

        Donor updated = donorRepository.updateProfile(donor.getId(), newName, newContact);
        UserSession.setCurrentDonor(updated);
        return updated;
    }

    /**
     * @throws IllegalArgumentException if required address fields are missing
     */
    public void addAddress(Donor donor, Address address) {
        if (!address.isValid()) {
            throw new IllegalArgumentException(
                    "Street and building are required (and floor and apartment for a home address).");
        }
        donorRepository.saveAddress(donor, address);
    }

    /**
     * @throws IllegalStateException if a pickup donation still uses this address
     */
    public void deleteAddress(Donor donor, Address address) {
        if (donorRepository.isAddressUsedByDonation(address.getId())) {
            throw new IllegalStateException(
                    "This address is used by a pickup donation, so it can't be deleted.");
        }
        donorRepository.deleteAddress(donor.getId(), address.getId());
    }
}
