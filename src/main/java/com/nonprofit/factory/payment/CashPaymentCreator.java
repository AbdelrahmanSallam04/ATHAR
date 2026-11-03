package com.nonprofit.factory.payment;

/**
 * Concrete creator that produces CashPayment objects.
 */
public class CashPaymentCreator extends PaymentCreator {

    @Override
    public Payment createPayment() {
        return new CashPayment();
    }
}
