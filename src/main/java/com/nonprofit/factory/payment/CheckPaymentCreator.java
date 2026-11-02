package com.nonprofit.factory.payment;

/**
 * Concrete creator that produces CheckPayment objects.
 * Holds the check details and passes them to the payment it creates.
 */
public class CheckPaymentCreator extends PaymentCreator {

    private final String checkNumber;
    private final String bankName;

    public CheckPaymentCreator(String checkNumber, String bankName) {
        this.checkNumber = checkNumber;
        this.bankName = bankName;
    }

    @Override
    public Payment createPayment() {
        return new CheckPayment(checkNumber, bankName);
    }
}
