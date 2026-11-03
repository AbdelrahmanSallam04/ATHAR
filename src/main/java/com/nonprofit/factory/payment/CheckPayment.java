package com.nonprofit.factory.payment;

/**
 * Concrete product: payment by bank check.
 */
public class CheckPayment implements Payment {

    private final String checkNumber;
    private final String bankName;

    public CheckPayment(String checkNumber, String bankName) {
        this.checkNumber = checkNumber == null ? "" : checkNumber.trim();
        this.bankName = bankName == null ? "" : bankName.trim();
    }

    @Override
    public void pay(double amount) {
        if (amount <= 0) {
            throw new PaymentException("Amount must be greater than 0.");
        }
        if (!checkNumber.matches("\\d{4,12}")) {
            throw new PaymentException("Check number must be 4 to 12 digits.");
        }
        if (bankName.isEmpty()) {
            throw new PaymentException("Bank name is required.");
        }
        System.out.println("Check #" + checkNumber + " (" + bankName + ") of " + amount + " accepted.");
    }

    @Override
    public String getMethodName() {
        return "Check";
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public String getBankName() {
        return bankName;
    }
}
