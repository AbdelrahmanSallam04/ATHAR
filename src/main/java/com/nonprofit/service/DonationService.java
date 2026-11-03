package com.nonprofit.service;

import com.nonprofit.factory.payment.PaymentCreator;
import com.nonprofit.model.Donation;
import com.nonprofit.model.Donor;
import com.nonprofit.model.MoneyDonation;
import com.nonprofit.model.Receipt;
import com.nonprofit.repository.DonorRepository;
import com.nonprofit.repository.MoneyDonationRepository;
import com.nonprofit.util.UserSession;

public class DonationService {

    private final MoneyDonationRepository moneyDonationRepository;
    private final DonorRepository donorRepository;

    public DonationService() {
        this(new MoneyDonationRepository(), new DonorRepository());
    }

    public DonationService(MoneyDonationRepository moneyDonationRepository, DonorRepository donorRepository) {
        this.moneyDonationRepository = moneyDonationRepository;
        this.donorRepository = donorRepository;
    }

    /**
     * Processes the payment through the given creator, then saves the donation
     * linked to the currently logged-in donor (from {@link UserSession}).
     * Never creates a duplicate donor row.
     *
     * @throws com.nonprofit.factory.payment.PaymentException if the payment is rejected
     */
    public Receipt makeMoneyDonation(String donorName, String contact, double amount, PaymentCreator creator) {
        // Resolve the existing donor from the session instead of creating a new one
        Donor donor = resolveSessionDonor(donorName, contact);

        MoneyDonation donation = new MoneyDonation(amount);
        // Use setDonor directly to avoid touching the lazy donationHistory collection
        // on a detached donor (which would trigger a LazyInitializationException).
        donation.setDonor(donor);
        donation.setPaymentCreator(creator);

        Receipt receipt = donation.processDonation();
        moneyDonationRepository.saveWithExistingDonor(donation, donor);
        return receipt;
    }

    /**
     * Returns the Hibernate-managed donor for the current session.
     * If no session donor exists (fallback path), looks up or creates one.
     */
    private Donor resolveSessionDonor(String donorName, String contact) {
        Donor sessionDonor = UserSession.getCurrentDonor();
        if (sessionDonor != null && sessionDonor.getId() != null) {
            // Re-fetch as a managed entity so Hibernate does not try to INSERT it again
            return donorRepository.findById(sessionDonor.getId())
                    .orElse(sessionDonor);
        }
        // Fallback: no session — create a transient donor (legacy path)
        return new Donor(donorName, contact);
    }

    /** Saves the donation (and its donor, if new) in one transaction. */
    public void save(Donation donation) {
        if (donation instanceof MoneyDonation moneyDonation) {
            moneyDonationRepository.save(moneyDonation);
        } else {
            moneyDonationRepository.saveDonation(donation);
        }
    }
}
