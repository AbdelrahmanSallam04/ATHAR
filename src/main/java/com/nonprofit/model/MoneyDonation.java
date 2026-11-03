package com.nonprofit.model;

import com.nonprofit.factory.payment.Payment;
import com.nonprofit.factory.payment.PaymentCreator;
import jakarta.persistence.*;

/**
 * Client of the payment Factory Method: it only knows the abstract
 * PaymentCreator and Payment, never the concrete payment classes.
 */
@Entity
@Table(name = "money_donation")
public class MoneyDonation extends Donation {

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Transient
    private PaymentCreator paymentCreator;

    protected MoneyDonation() {
    }

    public MoneyDonation(double amount) {
        this.amount = amount;
    }

    public void setPaymentCreator(PaymentCreator creator) {
        this.paymentCreator = creator;
    }

    /**
     * @throws com.nonprofit.factory.payment.PaymentException if the payment is rejected
     */
    @Override
    public Receipt processDonation() {
        if (paymentCreator == null) {
            throw new IllegalStateException("Choose a payment method first.");
        }
        if (donor == null) {
            throw new IllegalStateException("Donation has no donor.");
        }

        Payment payment = paymentCreator.processPayment(amount);
        paymentMethod = payment.getMethodName();

        return new Receipt(donor.getName(), amount, paymentMethod, "Money donation");
    }

    public double getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
