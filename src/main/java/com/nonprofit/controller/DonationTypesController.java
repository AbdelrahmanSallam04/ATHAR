
package com.nonprofit.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

/**
 * Controller for donation-types.fxml.
 * Lets the user pick a donation type (Money or In-Kind).
 * Only Money is available for now; it opens the money donation page.
 */
public class DonationTypesController {

    private static final String TYPES_PAGE = "/com/nonprofit/view/donation-types.fxml";
    private static final String MONEY_PAGE = "/com/nonprofit/view/money-donation.fxml";

    @FXML
    private VBox root;

    /** Opens the money donation page in place of this page. */
    @FXML
    private void onMoneyDonation() throws IOException {
        Pane contentArea = (Pane) root.getParent();

        Button back = new Button("Back to donation types", new FontIcon("mdi2a-arrow-left"));
        back.getStyleClass().add("btn-ghost");
        back.setOnAction(e -> showPage(contentArea, TYPES_PAGE));

        Node moneyPage = FXMLLoader.load(getClass().getResource(MONEY_PAGE));
        contentArea.getChildren().setAll(new VBox(12, back, moneyPage));
    }

    private void showPage(Pane contentArea, String fxml) {
        try {
            contentArea.getChildren().setAll((Node) FXMLLoader.load(getClass().getResource(fxml)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
