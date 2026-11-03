package com.nonprofit.controller;

import com.nonprofit.model.Address;
import com.nonprofit.model.Donation;
import com.nonprofit.model.DonationStatus;
import com.nonprofit.model.Donor;
import com.nonprofit.service.ProfileService;
import com.nonprofit.view.Toast;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Controller for profile.fxml: shows and edits the logged-in donor's info,
 * manages saved addresses and lists the donation history.
 */
public class ProfileController implements ShellAware {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String ALL_TYPES = "All types";

    @FXML private VBox signedOutBox;
    @FXML private VBox profileContent;

    @FXML private Label avatarLabel;
    @FXML private Label nameLabel;
    @FXML private Label roleBadge;
    @FXML private Label contactLabel;

    @FXML private Label totalMoneyLabel;
    @FXML private Label donationCountLabel;
    @FXML private Label itemCountLabel;

    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private Label profileError;
    @FXML private Button resetButton;
    @FXML private Button saveButton;

    @FXML private VBox addressList;
    @FXML private Label noAddressesLabel;

    @FXML private ComboBox<String> typeFilter;
    @FXML private TableView<Donation> historyTable;
    @FXML private TableColumn<Donation, String> dateColumn;
    @FXML private TableColumn<Donation, String> typeColumn;
    @FXML private TableColumn<Donation, String> detailsColumn;
    @FXML private TableColumn<Donation, String> methodColumn;
    @FXML private TableColumn<Donation, DonationStatus> statusColumn;

    private final ProfileService profileService = new ProfileService();
    private final ObservableList<Donation> donations = FXCollections.observableArrayList();
    private final FilteredList<Donation> filteredDonations = new FilteredList<>(donations);

    private ShellController shell;
    private Donor donor;

    @FXML
    private void initialize() {
        profileError.managedProperty().bind(profileError.textProperty().isNotEmpty());
        profileError.visibleProperty().bind(profileError.managedProperty());

        nameField.textProperty().addListener((obs, was, is) -> updateSaveState());
        contactField.textProperty().addListener((obs, was, is) -> updateSaveState());

        setUpHistoryTable();
        loadProfile();
    }

    @Override
    public void setShell(ShellController shell) {
        this.shell = shell;
    }

    // ---------- Loading ----------

    /**
     * Shows the session's donor right away, then refreshes it and loads the
     * history in parallel in the background.
     */
    private void loadProfile() {
        Donor sessionDonor = profileService.getSessionDonor();
        if (sessionDonor == null) {
            showSignedOut();
            return;
        }
        donor = sessionDonor;
        showDonor();

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(36, 36);
        historyTable.setPlaceholder(new VBox(10, spinner, new Label("Loading your donations…")) {{
            setAlignment(javafx.geometry.Pos.CENTER);
        }});

        runInBackground(() -> profileService.getDonationHistory(sessionDonor),
                this::showHistory, "Could not load your donation history.");
        runInBackground(profileService::getCurrentDonor, fresh -> {
            // Only refresh if the user hasn't started editing
            if (fresh != null && !hasUnsavedChanges()) {
                donor = fresh;
                showDonor();
            }
        }, "Could not refresh your profile.");
    }

    /** Re-reads the donor only (after an address change). */
    private void reloadDonor() {
        runInBackground(profileService::getCurrentDonor, fresh -> {
            donor = fresh;
            showAddresses();
        }, "Could not refresh your addresses.");
    }

    private void showSignedOut() {
        signedOutBox.setVisible(true);
        signedOutBox.setManaged(true);
        profileContent.setVisible(false);
        profileContent.setManaged(false);
    }

    private void showDonor() {
        String name = donor.getName() == null ? "" : donor.getName();
        avatarLabel.setText(name.isBlank() ? "?" : name.substring(0, 1).toUpperCase());
        nameLabel.setText(name);
        roleBadge.setText(donor.getRole() == null ? "Donor" : capitalize(donor.getRole()));
        contactLabel.setText(donor.getContactInfo());

        nameField.setText(name);
        contactField.setText(donor.getContactInfo());
        profileError.setText("");
        updateSaveState();

        showAddresses();
    }

    private void showHistory(List<Donation> history) {
        donations.setAll(history);
        historyTable.setPlaceholder(new Label("You haven't made any donations yet."));

        ProfileService.ProfileStats stats = profileService.calculateStats(history);
        totalMoneyLabel.setText(String.format("EGP %,.2f", stats.totalMoney()));
        donationCountLabel.setText(String.valueOf(stats.donationCount()));
        itemCountLabel.setText(String.valueOf(stats.itemCount()));
    }

    // ---------- Personal information ----------

    private void updateSaveState() {
        boolean changed = hasUnsavedChanges();
        saveButton.setDisable(!changed);
        resetButton.setDisable(!changed);
    }

    private boolean hasUnsavedChanges() {
        return donor != null
                && (!nameField.getText().trim().equals(nullToEmpty(donor.getName()))
                || !contactField.getText().trim().equals(nullToEmpty(donor.getContactInfo())));
    }

    @FXML
    private void onResetProfile() {
        nameField.setText(donor.getName());
        contactField.setText(donor.getContactInfo());
        profileError.setText("");
    }

    @FXML
    private void onSaveProfile() {
        profileError.setText("");
        String name = nameField.getText();
        String contact = contactField.getText();
        saveButton.setDisable(true);

        Task<Donor> task = new Task<>() {
            @Override
            protected Donor call() {
                return profileService.updateProfile(donor, name, contact);
            }
        };
        task.setOnSucceeded(e -> {
            donor = task.getValue();
            showDonor();
            if (shell != null) {
                shell.refreshUserInfo();
            }
            Toast.success(saveButton, "Your profile was updated.");
        });
        task.setOnFailed(e -> {
            updateSaveState();
            Throwable error = task.getException();
            if (error instanceof IllegalArgumentException) {
                profileError.setText(error.getMessage());
            } else {
                error.printStackTrace();
                profileError.setText("Your changes could not be saved. Please try again.");
            }
        });
        start(task);
    }

    // ---------- Addresses ----------

    private void showAddresses() {
        addressList.getChildren().clear();
        for (Address address : donor.getSavedAddresses()) {
            addressList.getChildren().add(addressRow(address));
        }
        boolean empty = donor.getSavedAddresses().isEmpty();
        noAddressesLabel.setVisible(empty);
        noAddressesLabel.setManaged(empty);
    }

    private Node addressRow(Address address) {
        FontIcon pin = new FontIcon("mdi2m-map-marker-outline");
        pin.setIconSize(20);
        pin.getStyleClass().add("address-icon");

        Label text = new Label(address.getFormattedAddress());
        text.setWrapText(true);
        text.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(text, Priority.ALWAYS);

        Button delete = new Button();
        delete.setGraphic(new FontIcon("mdi2t-trash-can-outline"));
        delete.getStyleClass().add("icon-button");
        delete.setTooltip(new Tooltip("Delete address"));
        delete.setAccessibleText("Delete address");
        delete.setOnAction(e -> confirmDeleteAddress(address));

        HBox row = new HBox(pin, text, delete);
        row.getStyleClass().add("address-row");
        return row;
    }

    private void confirmDeleteAddress(Address address) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(addressList.getScene().getWindow());
        confirm.getDialogPane().getStylesheets().addAll(addressList.getScene().getStylesheets());
        confirm.setTitle("Delete address");
        confirm.setHeaderText("Delete this address?");
        confirm.setContentText(address.getFormattedAddress());
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        confirm.getButtonTypes().setAll(ButtonType.CANCEL, deleteButton);
        confirm.getDialogPane().lookupButton(deleteButton).getStyleClass().add("btn-danger");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != deleteButton) {
            return;
        }
        runInBackground(() -> {
            profileService.deleteAddress(donor, address);
            return null;
        }, ignored -> {
            Toast.success(addressList, "Address deleted.");
            reloadDonor();
        }, null);
    }

    @FXML
    private void onAddAddress() {
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("Home", "Office", "Other"));
        type.getSelectionModel().select("Home");
        type.setMaxWidth(Double.MAX_VALUE);
        TextField city = new TextField("Cairo");
        TextField street = new TextField();
        TextField building = new TextField();
        TextField floor = new TextField();
        TextField apartment = new TextField();
        TextField notes = new TextField();
        notes.setPromptText("e.g. Ring the bell twice");

        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(12);
        addFormField(form, 0, 0, "Location type *", type);
        addFormField(form, 0, 1, "City *", city);
        addFormField(form, 1, 0, "Street address *", street);
        addFormField(form, 1, 1, "Building *", building);
        addFormField(form, 2, 0, "Floor", floor);
        addFormField(form, 2, 1, "Apartment", apartment);
        addFormField(form, 3, 0, "Notes", notes);

        Label error = new Label();
        error.getStyleClass().add("error-text");
        error.setWrapText(true);

        VBox content = new VBox(12, form, error);
        content.setStyle("-fx-padding: 8 8 0 8;");

        Dialog<Address> dialog = new Dialog<>();
        dialog.initOwner(addressList.getScene().getWindow());
        dialog.setTitle("Add address");
        dialog.setHeaderText("New saved address");
        dialog.getDialogPane().getStylesheets().addAll(addressList.getScene().getStylesheets());
        dialog.getDialogPane().setContent(content);
        ButtonType saveType = new ButtonType("Save address", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, saveType);

        // Keep the dialog open until the address is valid
        dialog.getDialogPane().lookupButton(saveType).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            Address candidate = buildAddress(type, city, street, building, floor, apartment, notes);
            if (!candidate.isValid()) {
                error.setText("Street and building are required, and floor and apartment for a home address.");
                event.consume();
            }
        });
        dialog.setResultConverter(button -> button == saveType
                ? buildAddress(type, city, street, building, floor, apartment, notes)
                : null);

        dialog.showAndWait().ifPresent(address -> runInBackground(() -> {
            profileService.addAddress(donor, address);
            return null;
        }, ignored -> {
            Toast.success(addressList, "Address saved.");
            reloadDonor();
        }, null));
    }

    private static Address buildAddress(ComboBox<String> type, TextField city, TextField street,
                                        TextField building, TextField floor, TextField apartment,
                                        TextField notes) {
        return new Address(type.getValue(), city.getText().trim(), street.getText().trim(),
                building.getText().trim(), floor.getText().trim(), apartment.getText().trim(),
                notes.getText().trim());
    }

    private static void addFormField(GridPane form, int row, int column, String label, Control field) {
        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("field-label");
        form.add(new VBox(6, fieldLabel, field), column, row);
    }

    // ---------- Donation history ----------

    private void setUpHistoryTable() {
        historyTable.setItems(filteredDonations);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        dateColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getDonatedAt().format(DATE_FORMAT)));
        typeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getTypeLabel()));
        detailsColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getSummary()));
        methodColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getMethodLabel()));
        statusColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getStatus()));

        typeColumn.setCellFactory(column -> badgeCell(ProfileController::typeBadgeStyle, type -> type));
        statusColumn.setCellFactory(column -> badgeCell(ProfileController::statusBadgeStyle,
                DonationStatus::getDisplayName));
        detailsColumn.setCellFactory(column -> new TableCell<>() {
            private final Label label = new Label();

            {
                label.setWrapText(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                label.setText(item);
                label.setMaxWidth(getTableColumn().getWidth() - 24);
                setGraphic(empty ? null : label);
            }
        });

        typeFilter.setItems(FXCollections.observableArrayList(ALL_TYPES, "Money", "Food", "Clothes"));
        typeFilter.getSelectionModel().select(ALL_TYPES);
        typeFilter.valueProperty().addListener((obs, was, selected) ->
                filteredDonations.setPredicate(donation ->
                        ALL_TYPES.equals(selected) || donation.getTypeLabel().equals(selected)));
    }

    private static <T> TableCell<Donation, T> badgeCell(java.util.function.Function<T, String> styleFor,
                                                        java.util.function.Function<T, String> textFor) {
        return new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(textFor.apply(item));
                badge.getStyleClass().addAll("badge", styleFor.apply(item));
                setGraphic(badge);
            }
        };
    }

    private static String typeBadgeStyle(String type) {
        return switch (type) {
            case "Money" -> "badge-gold";
            case "Food" -> "badge-success";
            default -> "badge-info";
        };
    }

    private static String statusBadgeStyle(DonationStatus status) {
        return switch (status) {
            case RECEIVED -> "badge-success";
            case CANCELLED -> "badge-danger";
            case PENDING, AWAITING_DROPOFF -> "badge-warning";
        };
    }

    // ---------- Helpers ----------

    /**
     * Runs database work off the UI thread. Errors with a user-facing message
     * (invalid input, rules) are shown as a toast; others use the fallback message.
     */
    private <T> void runInBackground(Callable<T> work, Consumer<T> onSuccess, String fallbackError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> {
            Throwable error = task.getException();
            boolean userError = error instanceof IllegalArgumentException || error instanceof IllegalStateException;
            if (!userError) {
                error.printStackTrace();
            }
            String message = userError || fallbackError == null ? error.getMessage() : fallbackError;
            Toast.error(addressList, message);
        });
        start(task);
    }

    private static void start(Task<?> task) {
        Thread worker = new Thread(task, "profile-worker");
        worker.setDaemon(true);
        worker.start();
    }

    private static String capitalize(String text) {
        String lower = text.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }
}
