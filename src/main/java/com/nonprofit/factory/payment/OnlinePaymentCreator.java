package com.nonprofit.factory.payment;

/**
 * Concrete creator that produces OnlinePayment objects.
 * Holds the card details and passes them to the payment it creates.
 */
public class OnlinePaymentCreator extends PaymentCreator {

    private final String cardHolder;
    private final String cardNumber;
    private final String expiryDate;
    private final String cvv;

    public OnlinePaymentCreator(String cardHolder, String cardNumber, String expiryDate, String cvv) {
        this.cardHolder = cardHolder;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    @Override
    public Payment createPayment() {
        return new OnlinePayment(cardHolder, cardNumber, expiryDate, cvv);
    }
}
