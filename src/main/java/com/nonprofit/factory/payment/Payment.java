package com.nonprofit.factory.payment;

/**
 * Product interface of the Factory Method pattern.
 * Every concrete payment type implements these operations.
 */
public interface Payment {

    /**
     * Performs the payment.
     *
     * @throws PaymentException if the payment is rejected
     */
    void pay(double amount);

    /** Name shown on the receipt, e.g. "Cash". */
    String getMethodName();
}
