package com.nonprofit.factory.payment;

/**
 * Creator of the Factory Method pattern.
 * Declares the factory method that returns a Payment. Subclasses decide
 * which concrete Payment is created.
 */
public abstract class PaymentCreator {

    /** The factory method. */
    public abstract Payment createPayment();

    /**
     * Core logic that relies on the product returned by the factory method.
     * It works with any Payment without knowing its concrete type.
     *
     * @return the payment that was used, so callers can read its method name
     * @throws PaymentException if the payment is rejected
     */
    public Payment processPayment(double amount) {
        Payment payment = createPayment();
        payment.pay(amount);
        return payment;
    }
}
