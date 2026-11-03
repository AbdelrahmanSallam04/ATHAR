package com.nonprofit.controller;

import com.nonprofit.model.Donor;
import com.nonprofit.util.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * Controller for shell.fxml: manages sidebar navigation and swaps views into the content area.
 * Also displays the logged-in donor's name from {@link UserSession}.
 */
public class ShellController {

    public static final String LOGO_PATH = "/com/nonprofit/images/athar-mark.png";

    // Navigation key -> FXML file
    private static final Map<String, String> SCREENS = Map.of(
            "donations", "/com/nonprofit/view/donation-types.fxml");

    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton donationsNav;
    @FXML private Label pageTitle;
    @FXML private VBox contentArea;
    @FXML private ImageView logoImage;
    @FXML private Label brandMark;
    @FXML private ImageView watermark;

    // Sidebar user info
    @FXML private Label avatarLabel;
    @FXML private Label sidebarUserName;
    @FXML private Label sidebarUserRole;
    @FXML private Label topbarAvatar;

    @FXML
    private void initialize() {
        loadLogo();
        populateUserInfo();

        navGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                // Keep one item always selected
                navGroup.selectToggle(oldToggle);
            } else {
                showScreen(newToggle);
            }
        });
        navGroup.selectToggle(donationsNav);
    }

    /**
     * Fills the sidebar / topbar with the name of the currently logged-in donor.
     */
    private void populateUserInfo() {
        Donor donor = UserSession.getCurrentDonor();
        if (donor != null) {
            String name = donor.getName() != null ? donor.getName() : "User";
            String initial = name.isBlank() ? "?" : String.valueOf(name.charAt(0)).toUpperCase();
            String role = donor.getRole() != null ? capitalize(donor.getRole().toLowerCase()) : "Donor";

            sidebarUserName.setText(name);
            sidebarUserRole.setText(role);
            avatarLabel.setText(initial);
            topbarAvatar.setText(initial);
        } else {
            sidebarUserName.setText("Guest");
            sidebarUserRole.setText("Not signed in");
            avatarLabel.setText("?");
            topbarAvatar.setText("?");
        }
    }

    private void showScreen(Toggle toggle) {
        ToggleButton item = (ToggleButton) toggle;
        String key = (String) item.getUserData();
        pageTitle.setText(item.getText());

        String fxml = SCREENS.get(key);
        if (fxml == null) {
            contentArea.getChildren().setAll(comingSoon(item.getText()));
            return;
        }
        try {
            Node screen = FXMLLoader.load(getClass().getResource(fxml));
            contentArea.getChildren().setAll(screen);
        } catch (IOException e) {
            e.printStackTrace();
            contentArea.getChildren().setAll(new Label("Could not load screen: " + e.getMessage()));
        }
    }

    private Node comingSoon(String moduleName) {
        FontIcon icon = new FontIcon("mdi2t-tools");
        icon.setIconSize(44);
        Label title = new Label(moduleName + " is coming soon");
        title.getStyleClass().add("h3");
        Label caption = new Label("This module is still being developed by the ATHAR team.");
        caption.getStyleClass().add("caption");

        VBox box = new VBox(icon, title, caption);
        box.getStyleClass().addAll("card", "empty-state");
        return box;
    }

    /**
     * Shows the logo if athar-logo.png has been added to resources; otherwise keeps
     * the text mark.
     */
    private void loadLogo() {
        URL logoUrl = getClass().getResource(LOGO_PATH);
        if (logoUrl == null) {
            return;
        }
        try {
            Image logo = new Image(logoUrl.toExternalForm());
            logoImage.setImage(logo);
            Rectangle roundedCorners = new Rectangle(52, 52);
            roundedCorners.setArcWidth(24);
            roundedCorners.setArcHeight(24);
            logoImage.setClip(roundedCorners);
            logoImage.setVisible(true);
            logoImage.setManaged(true);
            brandMark.setVisible(false);
            brandMark.setManaged(false);

            watermark.setImage(logo);
            watermark.setVisible(true);
            watermark.setManaged(true);
        } catch (Exception ignored) {
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
