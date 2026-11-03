package com.nonprofit.controller;

import com.nonprofit.model.*;
import com.nonprofit.service.InKindDonationService;
import com.nonprofit.view.Toast;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for in-kind-donation.fxml.
 * Manages combined Clothes & Food donations, donor profile reuse,
 * and normalized address persistence with drop-down selection.
 */
public class InKindDonationController {

    private static final String ADD_NEW_ADDRESS_OPTION = "+ Add New Address...";

    // Header Donor Profile
    @FXML private Label activeDonorName;
    @FXML private Label activeDonorContact;

    // Tab Bar
    @FXML private ToggleButton clothesTab;
    @FXML private ToggleButton foodTab;
    private ToggleGroup categoryGroup;

    // Clothes Controls
    @FXML private VBox clothesInputBox;
    @FXML private ComboBox<String> clothingTypeCombo;
    @FXML private ComboBox<String> clothingGenderCombo;
    @FXML private ComboBox<String> clothingSizeCombo;
    @FXML private Spinner<Integer> clothingQuantitySpinner;
    @FXML private Button addClothingBtn;

    // Food Controls
    @FXML private VBox foodInputBox;
    @FXML private ComboBox<String> foodTypeCombo;
    @FXML private Spinner<Integer> foodQuantitySpinner;
    @FXML private CheckBox foodPerishableCheck;
    @FXML private Button addFoodBtn;

    // Dynamic Items Table
    @FXML private Label itemsCardTitle;
    @FXML private Label itemInputError;
    @FXML private Label tableError;
    @FXML private TableView<ItemEntry> itemsTable;
    @FXML private TableColumn<ItemEntry, String> itemCategoryCol;
    @FXML private TableColumn<ItemEntry, String> itemDescriptionCol;
    @FXML private TableColumn<ItemEntry, Integer> itemQuantityCol;
    @FXML private TableColumn<ItemEntry, Void> itemActionCol;

    // Fulfillment
    @FXML private ToggleButton officeDropoffOption;
    @FXML private ToggleButton courierPickupOption;
    private ToggleGroup fulfillmentGroup;
    @FXML private VBox officeNoticeBox;
    @FXML private VBox courierAddressBox;

    // Saved & New Address Controls
    @FXML private ComboBox<String> savedAddressCombo;
    @FXML private VBox newAddressForm;
    @FXML private RadioButton homeTypeRadio;
    @FXML private RadioButton officeTypeRadio;
    @FXML private RadioButton otherTypeRadio;
    private ToggleGroup locationTypeGroup;
    @FXML private TextField streetField;
    @FXML private ComboBox<String> cityCombo;
    @FXML private TextField buildingField;
    @FXML private VBox floorContainer;
    @FXML private TextField floorField;
    @FXML private VBox apartmentContainer;
    @FXML private TextField apartmentField;
    @FXML private TextField notesField;
    @FXML private Label streetError;
    @FXML private Label buildingError;
    @FXML private Label floorError;
    @FXML private Label apartmentError;

    // Summary Card
    @FXML private Label summaryDonor;
    @FXML private Label summaryFulfillment;
    @FXML private Label summaryAddress;
    @FXML private VBox summaryItemsBox;
    @FXML private Label summaryTotalCount;
    @FXML private Button submitButton;
    @FXML private Label generalErrorBanner;

    // State
    private Donor currentDonor;
    private final ObservableList<ItemEntry> currentItems = FXCollections.observableArrayList();
    private final InKindDonationService donationService = new InKindDonationService();

    @FXML
    private void initialize() {
        loadDonorProfile();
        setupTabs();
        setupInputData();
        setupTable();
        setupFulfillment();
        setupLiveSummaryAndValidation();
    }

    private void loadDonorProfile() {
        currentDonor = donationService.getActiveDonor();
        if (currentDonor != null) {
            activeDonorName.setText("Donor: " + currentDonor.getName());
            activeDonorContact.setText(currentDonor.getContactInfo() != null ? currentDonor.getContactInfo() : "");
            summaryDonor.setText(currentDonor.getName());
        }
        refreshSavedAddresses();
    }

    private void refreshSavedAddresses() {
        savedAddressCombo.getItems().clear();
        List<Address> saved = currentDonor != null ? currentDonor.getSavedAddresses() : List.of();

        for (Address addr : saved) {
            savedAddressCombo.getItems().add(addr.getFormattedAddress());
        }
        savedAddressCombo.getItems().add(ADD_NEW_ADDRESS_OPTION);

        if (!saved.isEmpty()) {
            savedAddressCombo.getSelectionModel().selectFirst();
            showWhen(newAddressForm, false);
            summaryAddress.setText(saved.get(0).getFormattedAddress());
        } else {
            savedAddressCombo.getSelectionModel().select(ADD_NEW_ADDRESS_OPTION);
            showWhen(newAddressForm, true);
            summaryAddress.setText("New pickup address");
        }
    }

    // ---------- Initialization Setups ----------

    private void setupTabs() {
        categoryGroup = new ToggleGroup();
        clothesTab.setToggleGroup(categoryGroup);
        foodTab.setToggleGroup(categoryGroup);

        categoryGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                categoryGroup.selectToggle(oldVal);
                return;
            }
            boolean isClothes = newVal == clothesTab;
            showWhen(clothesInputBox, isClothes);
            showWhen(foodInputBox, !isClothes);
            itemsCardTitle.setText(isClothes ? "Clothing Items" : "Food Items");
            // DO NOT clear currentItems! Items remain preserved across tabs until submitted.
        });

        showWhen(clothesInputBox, true);
        showWhen(foodInputBox, false);
    }

    private void setupInputData() {
        // Clothes Combos
        clothingTypeCombo.getItems().addAll(
                "T-Shirt", "Shirt / Blouse", "Pants / Trousers", "Jacket / Coat",
                "Dress / Abaya", "Sweater / Hoodie", "Shoes / Footwear",
                "Baby & Kids Clothes", "Blanket / Winter Wear"
        );
        clothingTypeCombo.getSelectionModel().selectFirst();

        clothingGenderCombo.getItems().addAll("Men", "Women", "Kids", "Unisex");
        clothingGenderCombo.getSelectionModel().select("Unisex");

        clothingSizeCombo.getItems().addAll("S", "M", "L", "XL", "XXL", "Kids Size", "Free Size");
        clothingSizeCombo.getSelectionModel().select("M");

        clothingQuantitySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1)
        );

        // Food Combos (Package size baked in)
        foodTypeCombo.getItems().addAll(
                "Rice (1 kg bag)",
                "Pasta (400g pack)",
                "Cooking Oil (1L bottle)",
                "Sugar (1 kg bag)",
                "Flour (1 kg bag)",
                "Canned Beans & Fava (Can)",
                "Canned Tuna (Can)",
                "Tomato Paste (Can / Jar)",
                "Lentils & Legumes (500g bag)",
                "Cooked Meals (Ready Meal Box)",
                "Fresh Produce (Box / kg)",
                "Baby Formula & Milk (Can / Pack)",
                "Dates (Pack / Box)"
        );
        foodTypeCombo.getSelectionModel().selectFirst();

        foodQuantitySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 1)
        );

        // Cities
        cityCombo.getItems().addAll(
                "Cairo", "Giza", "Heliopolis", "Nasr City", "New Cairo / 5th Settlement",
                "Maadi", "6th of October", "Sheikh Zayed", "Alexandria"
        );
        cityCombo.getSelectionModel().select("Cairo");
    }

    private void setupTable() {
        itemsTable.setItems(currentItems);

        itemCategoryCol.setCellValueFactory(cell -> cell.getValue().categoryProperty());
        itemDescriptionCol.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        itemQuantityCol.setCellValueFactory(cell -> cell.getValue().quantityProperty().asObject());

        itemActionCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button();

            {
                FontIcon icon = new FontIcon("mdi2d-delete-outline");
                icon.setIconSize(16);
                icon.getStyleClass().add("error-text");
                removeBtn.setGraphic(icon);
                removeBtn.getStyleClass().addAll("icon-button", "btn-ghost");
                removeBtn.setOnAction(e -> {
                    ItemEntry item = getTableView().getItems().get(getIndex());
                    currentItems.remove(item);
                    updateSummaryItems();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void setupFulfillment() {
        fulfillmentGroup = new ToggleGroup();
        officeDropoffOption.setToggleGroup(fulfillmentGroup);
        courierPickupOption.setToggleGroup(fulfillmentGroup);

        fulfillmentGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                fulfillmentGroup.selectToggle(oldVal);
                return;
            }
            boolean isPickup = newVal == courierPickupOption;
            showWhen(courierAddressBox, isPickup);
            showWhen(officeNoticeBox, !isPickup);
            summaryFulfillment.setText(isPickup ? "Courier Pickup" : "Office Drop-off");

            if (isPickup) {
                updateSummaryAddressFromSelection();
            } else {
                summaryAddress.setText("ATHAR HQ, Heliopolis");
            }
        });

        showWhen(courierAddressBox, false);
        showWhen(officeNoticeBox, true);

        // Address Combobox listener
        savedAddressCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isNew = ADD_NEW_ADDRESS_OPTION.equals(newVal);
            showWhen(newAddressForm, isNew);
            updateSummaryAddressFromSelection();
        });

        // Location Types
        locationTypeGroup = new ToggleGroup();
        homeTypeRadio.setToggleGroup(locationTypeGroup);
        officeTypeRadio.setToggleGroup(locationTypeGroup);
        otherTypeRadio.setToggleGroup(locationTypeGroup);

        locationTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean isHome = newVal == homeTypeRadio;
            showWhen(floorContainer, isHome);
            showWhen(apartmentContainer, isHome);
        });
    }

    private void updateSummaryAddressFromSelection() {
        if (!courierPickupOption.isSelected()) {
            summaryAddress.setText("ATHAR HQ, Heliopolis");
            return;
        }

        String selected = savedAddressCombo.getValue();
        if (selected == null || ADD_NEW_ADDRESS_OPTION.equals(selected)) {
            String street = streetField.getText().trim();
            summaryAddress.setText(street.isEmpty() ? "New pickup address" : street + ", " + cityCombo.getValue());
        } else {
            summaryAddress.setText(selected);
        }
    }

    private void setupLiveSummaryAndValidation() {
        for (Label label : new Label[]{itemInputError, tableError, streetError,
                buildingError, floorError, apartmentError, generalErrorBanner}) {
            label.managedProperty().bind(label.textProperty().isNotEmpty());
            label.visibleProperty().bind(label.managedProperty());
        }

        streetField.textProperty().addListener((obs, was, is) -> updateSummaryAddressFromSelection());
    }

    // ---------- Actions ----------

    @FXML
    private void onAddClothingItem(ActionEvent event) {
        itemInputError.setText("");
        tableError.setText("");

        String type = clothingTypeCombo.getValue();
        String gender = clothingGenderCombo.getValue();
        String size = clothingSizeCombo.getValue();
        int quantity = clothingQuantitySpinner.getValue();

        // Check if an identical clothing item already exists
        ItemEntry existingEntry = null;
        for (ItemEntry entry : currentItems) {
            if ("Clothes".equals(entry.getCategory()) && entry.getRawItem() instanceof ClothingItem existingItem) {
                if (type.equalsIgnoreCase(existingItem.getClothingType())
                        && gender.equalsIgnoreCase(existingItem.getGenderCategory())
                        && size.equalsIgnoreCase(existingItem.getSize())) {
                    existingEntry = entry;
                    break;
                }
            }
        }

        if (existingEntry != null) {
            ClothingItem existingItem = (ClothingItem) existingEntry.getRawItem();
            int newTotal = existingItem.getQuantity() + quantity;
            existingItem.setQuantity(newTotal);
            existingEntry.setQuantity(newTotal);
            existingEntry.setDescription(existingItem.getDescription());
            itemsTable.refresh();
            Toast.success(addClothingBtn, "Updated to " + existingItem.getDescription());
        } else {
            ClothingItem item = new ClothingItem(type, gender, size, quantity);
            currentItems.add(new ItemEntry("Clothes", item.getDescription(), quantity, item));
            Toast.success(addClothingBtn, "Added " + item.getDescription());
        }

        updateSummaryItems();
    }

    @FXML
    private void onAddFoodItem(ActionEvent event) {
        itemInputError.setText("");
        tableError.setText("");

        String type = foodTypeCombo.getValue();
        int quantity = foodQuantitySpinner.getValue();
        boolean perishable = foodPerishableCheck.isSelected();

        // Check if an identical food item already exists
        ItemEntry existingEntry = null;
        for (ItemEntry entry : currentItems) {
            if ("Food".equals(entry.getCategory()) && entry.getRawItem() instanceof FoodItem existingItem) {
                if (type.equalsIgnoreCase(existingItem.getFoodType())
                        && (perishable == existingItem.isPerishable())) {
                    existingEntry = entry;
                    break;
                }
            }
        }

        if (existingEntry != null) {
            FoodItem existingItem = (FoodItem) existingEntry.getRawItem();
            int newTotal = existingItem.getQuantity() + quantity;
            existingItem.setQuantity(newTotal);
            existingEntry.setQuantity(newTotal);
            existingEntry.setDescription(existingItem.getDescription());
            itemsTable.refresh();
            Toast.success(addFoodBtn, "Updated to " + existingItem.getDescription());
        } else {
            FoodItem item = new FoodItem(type, quantity, perishable);
            currentItems.add(new ItemEntry("Food", item.getDescription(), quantity, item));
            Toast.success(addFoodBtn, "Added " + item.getDescription());
        }

        updateSummaryItems();
    }

    @FXML
    private void onSubmitDonation() {
        generalErrorBanner.setText("");

        if (currentItems.isEmpty()) {
            tableError.setText("Please add at least one item before proceeding.");
            return;
        }

        boolean isPickup = courierPickupOption.isSelected();
        Address selectedAddress = null;

        if (isPickup) {
            String chosen = savedAddressCombo.getValue();
            if (chosen != null && !ADD_NEW_ADDRESS_OPTION.equals(chosen)) {
                // Find matching saved address
                for (Address a : currentDonor.getSavedAddresses()) {
                    if (a.getFormattedAddress().equals(chosen)) {
                        selectedAddress = a;
                        break;
                    }
                }
            } else {
                // New address validation
                if (!validateNewAddress()) {
                    return;
                }
                RadioButton selectedRadio = (RadioButton) locationTypeGroup.getSelectedToggle();
                String locationType = selectedRadio != null ? selectedRadio.getText() : "Home";

                selectedAddress = new Address(
                        locationType,
                        cityCombo.getValue(),
                        streetField.getText().trim(),
                        buildingField.getText().trim(),
                        floorField.getText().trim(),
                        apartmentField.getText().trim(),
                        notesField.getText().trim()
                );
            }
        }

        FulfillmentMethod method = isPickup
                ? FulfillmentMethod.COURIER_PICKUP
                : FulfillmentMethod.OFFICE_DROPOFF;

        // Separate items by type
        List<ClothingItem> clothingList = new ArrayList<>();
        List<FoodItem> foodList = new ArrayList<>();
        for (ItemEntry entry : currentItems) {
            if ("Clothes".equals(entry.getCategory())) {
                clothingList.add((ClothingItem) entry.getRawItem());
            } else if ("Food".equals(entry.getCategory())) {
                foodList.add((FoodItem) entry.getRawItem());
            }
        }

        final Address finalAddress = selectedAddress;

        // Async execution
        Task<Receipt> task = new Task<>() {
            @Override
            protected Receipt call() {
                return donationService.submitInKindDonations(
                        currentDonor, clothingList, foodList, method, finalAddress
                );
            }
        };

        task.setOnSucceeded(e -> {
            setBusy(false);
            Receipt receipt = task.getValue();
            receipt.printReceipt();
            Toast.success(submitButton, "Donation request recorded successfully!");
            showConfirmationDialog(receipt);

            // Clear data ONLY AFTER PROCEEDING!
            clearForm();
        });

        task.setOnFailed(e -> {
            setBusy(false);
            Throwable error = task.getException();
            error.printStackTrace();
            generalErrorBanner.setText("Failed to save donation: " + error.getMessage());
            Toast.error(submitButton, "Could not save donation. Please try again.");
        });

        setBusy(true);
        Thread worker = new Thread(task, "in-kind-donation-worker");
        worker.setDaemon(true);
        worker.start();
    }

    // ---------- Validation ----------

    private boolean validateNewAddress() {
        boolean ok = true;

        if (streetField.getText().isBlank()) {
            markField(streetField, streetError, "Street address is required.");
            ok = false;
        } else {
            markField(streetField, streetError, "");
        }

        if (buildingField.getText().isBlank()) {
            markField(buildingField, buildingError, "Building is required.");
            ok = false;
        } else {
            markField(buildingField, buildingError, "");
        }

        if (homeTypeRadio.isSelected()) {
            if (floorField.getText().isBlank()) {
                markField(floorField, floorError, "Floor is required for home.");
                ok = false;
            } else {
                markField(floorField, floorError, "");
            }

            if (apartmentField.getText().isBlank()) {
                markField(apartmentField, apartmentError, "Apartment is required.");
                ok = false;
            } else {
                markField(apartmentField, apartmentError, "");
            }
        }

        return ok;
    }

    private void markField(Control field, Label errorLabel, String message) {
        errorLabel.setText(message);
        field.getStyleClass().remove("invalid");
        if (!message.isEmpty()) {
            field.getStyleClass().add("invalid");
        }
    }

    // ---------- UI Helpers ----------

    private void updateSummaryItems() {
        summaryItemsBox.getChildren().clear();

        if (currentItems.isEmpty()) {
            Label placeholder = new Label("No items added");
            placeholder.getStyleClass().add("caption");
            summaryItemsBox.getChildren().add(placeholder);
            summaryTotalCount.setText("0 Items");
            return;
        }

        int totalCount = 0;
        for (ItemEntry entry : currentItems) {
            totalCount += entry.getQuantity();
            Label itemLabel = new Label("• [" + entry.getCategory() + "] " + entry.getDescription());
            itemLabel.getStyleClass().add("body-text");
            itemLabel.setWrapText(true);
            summaryItemsBox.getChildren().add(itemLabel);
        }

        summaryTotalCount.setText(totalCount + " Items");
    }

    private void showConfirmationDialog(Receipt receipt) {
        FontIcon icon = new FontIcon("mdi2c-check-circle");
        icon.setIconSize(48);
        icon.getStyleClass().add("success-icon");

        Label title = new Label("Thank you, " + receipt.getUserName() + "!");
        title.getStyleClass().add("h2");
        Label subtitle = new Label("Your in-kind donation request has been recorded.");
        subtitle.getStyleClass().add("caption");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        addReceiptRow(grid, 0, "Receipt / Tracking ID", receipt.getReceiptId());
        addReceiptRow(grid, 1, "Fulfillment Method", receipt.getPaymentMethod());
        addReceiptRow(grid, 2, "Items Summary", receipt.getDescription());
        if (!receipt.getDetails().isEmpty()) {
            addReceiptRow(grid, 3, "Instructions", receipt.getDetails());
        }
        addReceiptRow(grid, 4, "Date & Time", receipt.getFormattedDate());

        VBox content = new VBox(14, icon, title, subtitle, grid);
        content.setStyle("-fx-padding: 20 28 12 28; -fx-pref-width: 540px;");

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(submitButton.getScene().getWindow());
        dialog.setTitle("Donation Confirmation Voucher");
        dialog.getDialogPane().getStylesheets().addAll(submitButton.getScene().getStylesheets());
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(new ButtonType("Done", ButtonBar.ButtonData.OK_DONE));
        dialog.showAndWait();
    }

    private static void addReceiptRow(GridPane grid, int row, String key, String value) {
        Label keyLabel = new Label(key);
        keyLabel.getStyleClass().add("summary-key");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("summary-value");
        valueLabel.setWrapText(true);
        grid.addRow(row, keyLabel, valueLabel);
    }

    private void setBusy(boolean busy) {
        submitButton.setDisable(busy);
        submitButton.setText(busy ? "Processing Request…" : "Submit Donation");
    }

    private void clearForm() {
        streetField.clear();
        buildingField.clear();
        floorField.clear();
        apartmentField.clear();
        notesField.clear();
        currentItems.clear();
        updateSummaryItems();
        officeDropoffOption.setSelected(true);
        loadDonorProfile(); // Reloads saved addresses including any newly added one
    }

    private static void showWhen(Node node, boolean show) {
        node.setVisible(show);
        node.setManaged(show);
    }

    // ---------- Table Row Model ----------

    public static class ItemEntry {
        private final SimpleStringProperty category;
        private final SimpleStringProperty description;
        private final SimpleIntegerProperty quantity;
        private final Object rawItem;

        public ItemEntry(String category, String description, int quantity, Object rawItem) {
            this.category = new SimpleStringProperty(category);
            this.description = new SimpleStringProperty(description);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.rawItem = rawItem;
        }

        public SimpleStringProperty categoryProperty() {
            return category;
        }

        public String getCategory() {
            return category.get();
        }

        public SimpleStringProperty descriptionProperty() {
            return description;
        }

        public String getDescription() {
            return description.get();
        }

        public void setDescription(String desc) {
            this.description.set(desc);
        }

        public SimpleIntegerProperty quantityProperty() {
            return quantity;
        }

        public int getQuantity() {
            return quantity.get();
        }

        public void setQuantity(int qty) {
            this.quantity.set(qty);
        }

        public Object getRawItem() {
            return rawItem;
        }
    }
}
