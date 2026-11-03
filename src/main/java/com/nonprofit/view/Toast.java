package com.nonprofit.view;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Non-blocking message shown at the top-centre of the window for 3 seconds.
 */
public final class Toast {

    private Toast() {
    }

    public static void success(Node anchor, String message) {
        show(anchor, message, "toast-success", "mdi2c-check-circle");
    }

    public static void error(Node anchor, String message) {
        show(anchor, message, "toast-error", "mdi2c-close-circle");
    }

    private static void show(Node anchor, String message, String styleClass, String iconLiteral) {
        if (anchor.getScene() == null) {
            return;
        }
        Window window = anchor.getScene().getWindow();

        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(20);
        HBox content = new HBox(icon, new Label(message));
        content.getStyleClass().addAll("toast", styleClass);

        Popup popup = new Popup();
        popup.getScene().getStylesheets().setAll(anchor.getScene().getStylesheets());
        popup.getContent().add(content);
        popup.show(window);

        // Centre horizontally near the top once the size is known
        popup.setX(window.getX() + (window.getWidth() - content.getWidth()) / 2);
        popup.setY(window.getY() + 72);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), content);
        fadeOut.setToValue(0);
        SequentialTransition life = new SequentialTransition(new PauseTransition(Duration.seconds(3)), fadeOut);
        life.setOnFinished(e -> popup.hide());
        life.play();
    }
}

