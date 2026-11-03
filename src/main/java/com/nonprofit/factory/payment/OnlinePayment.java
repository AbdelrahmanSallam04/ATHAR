package com.nonprofit.factory.payment;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Concrete product: simulated online card payment.
 * Card details are only validated, never stored or sent anywhere.
 */
public class OnlinePayment implements Payment {

    private static final DateTimeFormatter EXPIRY_FORMAT = DateTimeFormatter.ofPattern("MM/yy");

    private final String cardHolder;
    private final String cardNumber;
    private final String expiryDate;
    private final String cvv;

    public OnlinePayment(String cardHolder, String cardNumber, String expiryDate, String cvv) {
        this.cardHolder = cardHolder == null ? "" : cardHolder.trim();
        this.cardNumber = cardNumber == null ? "" : cardNumber.replaceAll("[\\s-]", "");
        this.expiryDate = expiryDate == null ? "" : expiryDate.trim();
        this.cvv = cvv == null ? "" : cvv.trim();
    }

    @Override
    public void pay(double amount) {
        if (amount <= 0) {
            throw new PaymentException("Amount must be greater than 0.");
        }
        if (cardHolder.isEmpty()) {
            throw new PaymentException("Card holder name is required.");
        }
        if (!cardNumber.matches("\\d{13,19}") || !passesLuhnCheck(cardNumber)) {
            throw new PaymentException("Card number is invalid.");
        }
        if (isExpired()) {
            throw new PaymentException("Expiry date must be a future date in MM/YY format.");
        }
        if (!cvv.matches("\\d{3,4}")) {
            throw new PaymentException("CVV must be 3 or 4 digits.");
        }
        System.out.println("Online payment of " + amount + " approved for card " + getMaskedCardNumber() + ".");
    }

    @Override
    public String getMethodName() {
        return "Online";
    }

    /** Only the last 4 digits are ever exposed, e.g. "**** 4242". */
    public String getMaskedCardNumber() {
        String last4 = cardNumber.length() >= 4 ? cardNumber.substring(cardNumber.length() - 4) : cardNumber;
        return "**** " + last4;
    }

    private boolean isExpired() {
        try {
            YearMonth expiry = YearMonth.parse(expiryDate, EXPIRY_FORMAT);
            return expiry.isBefore(YearMonth.now());
        } catch (DateTimeParseException e) {
            return true;
        }
    }

    // Standard checksum used by card numbers to catch typos
    private static boolean passesLuhnCheck(String number) {
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
}
