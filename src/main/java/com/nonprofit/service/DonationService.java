package com.nonprofit.service;

import com.nonprofit.factory.payment.PaymentCreator;
import com.nonprofit.model.Donation;
import com.nonprofit.model.Donor;
import com.nonprofit.model.MoneyDonation;
import com.nonprofit.model.Receipt;
import com.nonprofit.repository.MoneyDonationRepository;

public class DonationService {

    private final MoneyDonationRepository moneyDonationRepository;

    public DonationService() {
        this(new MoneyDonationRepository());
    }

    public DonationService(MoneyDonationRepository moneyDonationRepository) {
        this.moneyDonationRepository = moneyDonationRepository;
    }

    /**
     * Processes the payment through the given creator, then saves the donation.
     *
     * @throws com.nonprofit.factory.payment.PaymentException if the payment is rejected
     */
    public Receipt makeMoneyDonation(String donorName, String contact, double amount, PaymentCreator creator) {
        Donor donor = new Donor(donorName, contact);
        MoneyDonation donation = new MoneyDonation(amount);
        donor.makeDonation(donation);
        donation.setPaymentCreator(creator);

        Receipt receipt = donation.processDonation();
        moneyDonationRepository.save(donation);
        return receipt;
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
