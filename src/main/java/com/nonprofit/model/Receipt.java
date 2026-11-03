package com.nonprofit.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents a donation receipt or confirmation voucher.
 */
public class Receipt {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final String receiptId;
    private final String userName;
    private final double amount;
    private final String paymentMethod;
    private final String description;
    private final String details;
    private final long timestamp;

    public Receipt(String userName, double amount, String paymentMethod, String description) {
        this(userName, amount, paymentMethod, description, null);
    }

    public Receipt(String userName, double amount, String paymentMethod, String description, String details) {
        this.receiptId = "ATH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.userName = userName;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.details = details != null ? details : "";
        this.timestamp = System.currentTimeMillis();
    }

    public void printReceipt() {
        System.out.println(getReceiptText());
    }

    public String getReceiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("Receipt ID: ").append(receiptId).append("\n");
        sb.append("Donor: ").append(userName).append("\n");
        if (amount > 0) {
            sb.append("Amount: ").append(String.format("%.2f EGP", amount)).append("\n");
        }
        sb.append("Fulfillment / Method: ").append(paymentMethod).append("\n");
        sb.append("Description: ").append(description).append("\n");
        if (!details.isEmpty()) {
            sb.append("Details: ").append(details).append("\n");
        }
        sb.append("Date: ").append(getFormattedDate()).append("\n");
        sb.append("========================================");
        return sb.toString();
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

    public String getDetails() {
        return details;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

