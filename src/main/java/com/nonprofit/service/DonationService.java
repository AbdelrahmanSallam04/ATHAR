package com.nonprofit.service;

import com.nonprofit.factory.payment.PaymentCreator;
import com.nonprofit.model.Donation;
import com.nonprofit.model.Donor;
import com.nonprofit.model.MoneyDonation;
import com.nonprofit.model.Receipt;
import com.nonprofit.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DonationService {

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
        save(donation);
        return receipt;
    }

    /** Saves the donation (and its donor, if new) in one transaction. */
    public void save(Donation donation) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(donation);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }
}
