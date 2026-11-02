package com.nonprofit.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Receipt {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final String receiptId;
    private final String userName;
    private final double amount;
    private final String paymentMethod;
    private final String description;
    private final long timestamp;

    public Receipt(String userName, double amount, String paymentMethod, String description) {
        this.receiptId = "RCPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.userName = userName;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }

    public void printReceipt() {
        System.out.println(getReceiptText());
    }

    public String getReceiptText() {
        return "Receipt ID: " + receiptId + "\n"
                + "Donor: " + userName + "\n"
                + "Amount: " + String.format("%.2f EGP", amount) + "\n"
                + "Payment method: " + paymentMethod + "\n"
                + "Description: " + description + "\n"
                + "Date: " + getFormattedDate();
    }

    public String getFormattedDate() {
        return DATE_FORMAT.format(Instant.ofEpochMilli(timestamp));
    }

    public String getReceiptId() {
        return receiptId;
    }

    public String getUserName() {
        return userName;
    }

    public double getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
