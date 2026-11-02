package com.nonprofit.controller;

import com.nonprofit.factory.payment.CashPaymentCreator;
import com.nonprofit.factory.payment.CheckPaymentCreator;
import com.nonprofit.factory.payment.OnlinePaymentCreator;
import com.nonprofit.factory.payment.PaymentCreator;
import com.nonprofit.factory.payment.PaymentException;
import com.nonprofit.model.Receipt;
import com.nonprofit.service.DonationService;
import com.nonprofit.view.Toast;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Controller for money-donation.fxml.
 * Acts as the "Application" of the Factory Method pattern: it is the only
 * place that picks a concrete PaymentCreator, based on the user's choice.
 */
public class MoneyDonationController {

    @FXML private TextField donorNameField;
    @FXML private TextField contactField;
    @FXML private TextField amountField;
    @FXML private Label donorNameError;
    @FXML private Label amountError;

    @FXML private ToggleGroup paymentMethodGroup;
    @FXML private ToggleButton cashOption;
    @FXML private ToggleButton checkOption;
    @FXML private ToggleButton onlineOption;
    @FXML private Label methodError;

    @FXML private GridPane checkDetailsBox;
    @FXML private TextField checkNumberField;
    @FXML private TextField bankNameField;

    @FXML private VBox onlineDetailsBox;
    @FXML private TextField cardHolderField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private PasswordField cvvField;

    @FXML private Label paymentErrorBanner;

    @FXML private Label summaryDonor;
    @FXML private Label summaryMethod;
    @FXML private Label summaryTotal;
    @FXML private Button donateButton;

    private final DonationService donationService = new DonationService();

    @FXML
    private void initialize() {
        // Show only the fields of the selected payment method
        showWhen(checkDetailsBox, checkOption.selectedProperty().get());
        showWhen(onlineDetailsBox, onlineOption.selectedProperty().get());
        checkOption.selectedProperty().addListener((obs, was, is) -> showWhen(checkDetailsBox, is));
        onlineOption.selectedProperty().addListener((obs, was, is) -> showWhen(onlineDetailsBox, is));

        // Messages take no space while empty
        for (Label label : new Label[]{donorNameError, amountError, methodError, paymentErrorBanner}) {
            label.managedProperty().bind(label.textProperty().isNotEmpty());
            label.visibleProperty().bind(label.managedProperty());
        }

        // Live summary
        donorNameField.textProperty().addListener((obs, was, is) ->
                summaryDonor.setText(is.isBlank() ? "—" : is.trim()));
        amountField.textProperty().addListener((obs, was, is) -> updateTotal());
        paymentMethodGroup.selectedToggleProperty().addListener((obs, was, is) -> {
            summaryMethod.setText(is == null ? "Not selected" : ((ToggleButton) is).getText());
            if (is != null) {
                methodError.setText("");
            }
        });

        // Validate when leaving a field
        donorNameField.focusedProperty().addListener((obs, was, is) -> {
            if (!is) validateDonorName();
        });
        amountField.focusedProperty().addListener((obs, was, is) -> {
            if (!is && !amountField.getText().isBlank()) validateAmount();
        });
    }

    @FXML
    private void onQuickAmount(ActionEvent event) {
        amountField.setText((String) ((Node) event.getSource()).getUserData());
        validateAmount();
    }

    @FXML
    private void onDonate() {
        paymentErrorBanner.setText("");

        boolean valid = validateDonorName() & validateAmount() & validateMethod();
        if (!valid) {
            return;
        }

        String donorName = donorNameField.getText().trim();
        String contact = contactField.getText().trim();
        double amount = Double.parseDouble(amountField.getText().trim());
        PaymentCreator creator = createPaymentCreator();

        Task<Receipt> task = new Task<>() {
            @Override
            protected Receipt call() {
                return donationService.makeMoneyDonation(donorName, contact, amount, creator);
            }
        };
        task.setOnSucceeded(e -> {
            setBusy(false);
            Receipt receipt = task.getValue();
            receipt.printReceipt();
            Toast.success(donateButton, "Donation saved. Thank you, " + donorName + "!");
            showReceipt(receipt);
            clearForm();
        });
        task.setOnFailed(e -> {
            setBusy(false);
            Throwable error = task.getException();
            if (error instanceof PaymentException) {
                paymentErrorBanner.setText("Payment failed: " + error.getMessage());
            } else {
                error.printStackTrace();
                paymentErrorBanner.setText("The donation could not be saved. " + error.getMessage());
                Toast.error(donateButton, "Something went wrong. Please try again.");
            }
        });

        setBusy(true);
        Thread worker = new Thread(task, "donation-worker");
        worker.setDaemon(true);
        worker.start();
    }

    /** Picks the concrete creator for the selected payment method. */
    private PaymentCreator createPaymentCreator() {
        Toggle selected = paymentMethodGroup.getSelectedToggle();
        if (selected == cashOption) {
            return new CashPaymentCreator();
        } else if (selected == checkOption) {
            return new CheckPaymentCreator(checkNumberField.getText(), bankNameField.getText());
        } else if (selected == onlineOption) {
            return new OnlinePaymentCreator(cardHolderField.getText(), cardNumberField.getText(),
                    expiryField.getText(), cvvField.getText());
        }
        return null;
    }

    // ---------- Validation ----------

    private boolean validateDonorName() {
        boolean ok = !donorNameField.getText().isBlank();
        markField(donorNameField, donorNameError, ok ? "" : "Full name is required.");
        return ok;
    }

    private boolean validateAmount() {
        String error = "";
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                error = "Amount must be greater than 0.";
            }
        } catch (NumberFormatException e) {
            error = "Enter the amount as a number, e.g. 500.";
        }
        markField(amountField, amountError, error);
        return error.isEmpty();
    }

    private boolean validateMethod() {
        boolean ok = paymentMethodGroup.getSelectedToggle() != null;
        methodError.setText(ok ? "" : "Choose a payment method.");
        return ok;
    }

    private void markField(Control field, Label errorLabel, String message) {
        errorLabel.setText(message);
        field.getStyleClass().remove("invalid");
        if (!message.isEmpty()) {
            field.getStyleClass().add("invalid");
        }
    }

    // ---------- UI helpers ----------

    private void updateTotal() {
        double amount = 0;
        try {
            amount = Math.max(0, Double.parseDouble(amountField.getText().trim()));
        } catch (NumberFormatException ignored) {
        }
        summaryTotal.setText(String.format("EGP %,.2f", amount));
    }

    private void setBusy(boolean busy) {
        donateButton.setDisable(busy);
        donateButton.setText(busy ? "Processing…" : "Donate now");
    }

    private static void showWhen(Node node, boolean show) {
        node.setVisible(show);
        node.setManaged(show);
    }

    private void showReceipt(Receipt receipt) {
        FontIcon icon = new FontIcon("mdi2c-check-circle");
        icon.setIconSize(48);
        icon.getStyleClass().add("success-icon");

        Label title = new Label("Thank you, " + receipt.getUserName() + "!");
        title.getStyleClass().add("h3");
        Label caption = new Label("Your donation has been received.");
        caption.getStyleClass().add("caption");

        GridPane rows = new GridPane();
        rows.setHgap(24);
        rows.setVgap(8);
        addReceiptRow(rows, 0, "Receipt No.", receipt.getReceiptId());
        addReceiptRow(rows, 1, "Amount", String.format("EGP %,.2f", receipt.getAmount()));
        addReceiptRow(rows, 2, "Payment method", receipt.getPaymentMethod());
        addReceiptRow(rows, 3, "Date", receipt.getFormattedDate());

        VBox content = new VBox(12, icon, title, caption, rows);
        content.setStyle("-fx-padding: 16 24 8 24;");

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(donateButton.getScene().getWindow());
        dialog.setTitle("Donation Receipt");
        dialog.getDialogPane().getStylesheets().addAll(donateButton.getScene().getStylesheets());
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(new ButtonType("Done", ButtonBar.ButtonData.OK_DONE));
        dialog.showAndWait();
    }

    private static void addReceiptRow(GridPane grid, int row, String key, String value) {
        Label keyLabel = new Label(key);
        keyLabel.getStyleClass().add("summary-key");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("summary-value");
        grid.addRow(row, keyLabel, valueLabel);
    }

    private void clearForm() {
        for (TextField field : new TextField[]{donorNameField, contactField, amountField,
                checkNumberField, bankNameField, cardHolderField, cardNumberField, expiryField, cvvField}) {
            field.clear();
        }
        donorNameField.getStyleClass().remove("invalid");
        amountField.getStyleClass().remove("invalid");
        donorNameError.setText("");
        amountError.setText("");
        paymentMethodGroup.selectToggle(null);
    }
}
