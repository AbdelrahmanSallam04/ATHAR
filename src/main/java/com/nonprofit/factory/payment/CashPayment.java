package com.nonprofit.factory.payment;

/**
 * Concrete product: cash handed in at the organization.
 */
public class CashPayment implements Payment {

    @Override
    public void pay(double amount) {
        if (amount <= 0) {
            throw new PaymentException("Amount must be greater than 0.");
        }
        System.out.println("Cash payment of " + amount + " received.");
    }

    @Override
    public String getMethodName() {
        return "Cash";
    }
}
