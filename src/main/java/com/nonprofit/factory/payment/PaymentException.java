package com.nonprofit.factory.payment;

/**
 * Thrown when a payment is rejected (invalid amount, invalid check or card details).
 */
public class PaymentException extends RuntimeException {

    public PaymentException(String message) {
        super(message);
    }
}
