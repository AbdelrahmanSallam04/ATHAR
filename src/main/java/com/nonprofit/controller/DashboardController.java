package com.nonprofit.controller;

import com.nonprofit.model.Donor;
import com.nonprofit.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for dashboard.fxml: the donor's home screen with shortcut cards.
 * Each card opens the matching sidebar item through the shell.
 */
public class DashboardController implements ShellAware {

    @FXML private Label welcomeTitle;

    private ShellController shell;

    @FXML
    private void initialize() {
        Donor donor = UserSession.getCurrentDonor();
        if (donor != null && donor.getName() != null && !donor.getName().isBlank()) {
            welcomeTitle.setText("Welcome back, " + donor.getName());
        } else {
            welcomeTitle.setText("Welcome to ATHAR");
        }
    }

    @Override
    public void setShell(ShellController shell) {
        this.shell = shell;
    }

    @FXML
    private void onMakeDonation() {
        shell.navigateTo("donations");
    }

    @FXML
    private void onOpenProfile() {
        shell.navigateTo("profile");
    }

    @FXML
    private void onOpenCommunications() {
        shell.navigateTo("communications");
    }
}
