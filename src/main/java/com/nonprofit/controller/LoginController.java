package com.nonprofit.controller;

import com.nonprofit.model.Donor;
import com.nonprofit.model.User;
import com.nonprofit.service.AuthService;
import com.nonprofit.util.UserSession;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Controller for login.fxml.
 * Handles both Sign-In and Create Account modes.
 * On successful authentication, replaces this scene with the main shell.
 */
public class LoginController {

    private static final String LOGO_PATH = "/com/nonprofit/images/athar-mark.png";

    // Tab toggles
    @FXML private ToggleGroup modeGroup;
    @FXML private ToggleButton loginTab;
    @FXML private ToggleButton signupTab;
    @FXML private ImageView logoView;
    @FXML private Label brandLabel;

    // Login pane
    @FXML private VBox loginPane;
    @FXML private TextField loginContactField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginContactError;
    @FXML private Label loginPasswordError;
    @FXML private Label loginGeneralError;
    @FXML private Button loginButton;

    // Signup pane
    @FXML private VBox signupPane;
    @FXML private TextField signupNameField;
    @FXML private TextField signupContactField;
    @FXML private PasswordField signupPasswordField;
    @FXML private PasswordField signupConfirmField;
    @FXML private Label signupNameError;
    @FXML private Label signupContactError;
    @FXML private Label signupPasswordError;
    @FXML private Label signupConfirmError;
    @FXML private Label signupGeneralError;
    @FXML private Button signupButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        loadLogo();

        // Bind error label visibility to non-empty text
        for (Label lbl : new Label[]{
                loginContactError, loginPasswordError, loginGeneralError,
                signupNameError, signupContactError, signupPasswordError,
                signupConfirmError, signupGeneralError}) {
            lbl.managedProperty().bind(lbl.textProperty().isNotEmpty());
            lbl.visibleProperty().bind(lbl.managedProperty());
        }

        // Switch between Login / Signup panes
        modeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                modeGroup.selectToggle(oldVal);
                return;
            }
            boolean isLogin = newVal == loginTab;
            showPane(loginPane, isLogin);
            showPane(signupPane, !isLogin);
            clearErrors();
        });
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    @FXML
    private void onLogin() {
        clearErrors();
        String contact = loginContactField.getText().trim();
        String password = loginPasswordField.getText();

        boolean valid = true;
        if (contact.isBlank()) {
            loginContactError.setText("Contact info is required.");
            valid = false;
        }
        if (password.isBlank()) {
            loginPasswordError.setText("Password is required.");
            valid = false;
        }
        if (!valid) return;

        loginButton.setDisable(true);
        loginButton.setText("Signing in…");

        Task<User> task = new Task<>() {
            @Override
            protected User call() {
                return authService.authenticate(contact, password);
            }
        };

        task.setOnSucceeded(e -> {
            User user = task.getValue();
            storeSessionAndProceed(user);
        });

        task.setOnFailed(e -> {
            loginButton.setDisable(false);
            loginButton.setText("Sign In");
            Throwable ex = task.getException();
            loginGeneralError.setText(ex.getMessage() != null ? ex.getMessage() : "Sign-in failed. Please try again.");
        });

        Thread t = new Thread(task, "login-worker");
        t.setDaemon(true);
        t.start();
    }

    // ─── Signup ───────────────────────────────────────────────────────────────

    @FXML
    private void onSignup() {
        clearErrors();
        String name    = signupNameField.getText().trim();
        String contact = signupContactField.getText().trim();
        String pass    = signupPasswordField.getText();
        String confirm = signupConfirmField.getText();

        boolean valid = true;
        if (name.isBlank()) {
            signupNameError.setText("Full name is required.");
            valid = false;
        }
        if (contact.isBlank()) {
            signupContactError.setText("Contact info is required.");
            valid = false;
        }
        if (pass.length() < 6) {
            signupPasswordError.setText("Password must be at least 6 characters.");
            valid = false;
        }
        if (!pass.equals(confirm)) {
            signupConfirmError.setText("Passwords do not match.");
            valid = false;
        }
        if (!valid) return;

        signupButton.setDisable(true);
        signupButton.setText("Creating account…");

        Task<User> task = new Task<>() {
            @Override
            protected User call() {
                // Register the new donor
                authService.registerUser(name, contact, pass, "DONOR", null, null);
                // Then authenticate to get the authenticated user
                return authService.authenticate(contact, pass);
            }
        };

        task.setOnSucceeded(e -> storeSessionAndProceed(task.getValue()));

        task.setOnFailed(e -> {
            signupButton.setDisable(false);
            signupButton.setText("Create Account");
            Throwable ex = task.getException();
            signupGeneralError.setText(ex.getMessage() != null ? ex.getMessage() : "Registration failed. Please try again.");
        });

        Thread t = new Thread(task, "signup-worker");
        t.setDaemon(true);
        t.start();
    }

    // ─── Session → shell ──────────────────────────────────────────────────────

    /**
     * Stores the authenticated user in {@link UserSession} and opens the main shell.
     */
    private void storeSessionAndProceed(User user) {
        if (user instanceof Donor donor) {
            UserSession.setCurrentDonor(donor);
        } else {
            Donor donor = new Donor(user.getName(), user.getContactInfo());
            donor.setId(user.getId());
            donor.setRole(user.getRole());
            UserSession.setCurrentDonor(donor);
        }

        Platform.runLater(this::openShell);
    }

    private void openShell() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/nonprofit/view/shell.fxml"));

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = loginButton.getScene();

            // Reuse the existing scene (keeps stylesheets) and swap the root
            scene.setRoot(root);
            stage.setTitle("ATHAR – Non-Profit Management & Impact Platform");
            stage.setMinWidth(1100);
            stage.setMinHeight(700);
            stage.setWidth(1366);
            stage.setHeight(768);
        } catch (Exception ex) {
            ex.printStackTrace();
            loginGeneralError.setText("Could not open the dashboard: " + ex.getMessage());
            loginButton.setDisable(false);
            loginButton.setText("Sign In");
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void loadLogo() {
        URL logoUrl = getClass().getResource(LOGO_PATH);
        if (logoUrl == null) return;
        try {
            Image logo = new Image(logoUrl.toExternalForm());
            logoView.setImage(logo);
            logoView.setVisible(true);
            logoView.setManaged(true);
            brandLabel.setVisible(false);
            brandLabel.setManaged(false);
        } catch (Exception ignored) {
        }
    }

    private static void showPane(VBox pane, boolean visible) {
        pane.setVisible(visible);
        pane.setManaged(visible);
    }

    private void clearErrors() {
        for (Label lbl : new Label[]{
                loginContactError, loginPasswordError, loginGeneralError,
                signupNameError, signupContactError, signupPasswordError,
                signupConfirmError, signupGeneralError}) {
            lbl.setText("");
        }
    }
}
