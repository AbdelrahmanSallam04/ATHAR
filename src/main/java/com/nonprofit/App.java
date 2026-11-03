package com.nonprofit;

import com.nonprofit.controller.ShellController;
import com.nonprofit.util.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application {

    private static final String STYLESHEET = "/com/nonprofit/css/athar.css";

    @Override
    public void init() {
        try {
            // Pre-initialize SessionFactory if DB is reachable
            HibernateUtil.getSessionFactory();
        } catch (Throwable t) {
            System.err.println("Notice: Database SessionFactory init deferred: " + t.getMessage());
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Open the Login / Signup screen first.
        // After successful authentication the LoginController swaps the root
        // of this scene to shell.fxml without creating a new window.
        Parent root = FXMLLoader.load(getClass().getResource("/com/nonprofit/view/login.fxml"));

        Scene scene = new Scene(root, 520, 780);
        scene.getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());

        URL logo = getClass().getResource(ShellController.LOGO_PATH);
        if (logo != null) {
            try {
                stage.getIcons().add(new Image(logo.toExternalForm()));
            } catch (Exception ignored) {
            }
        }

        stage.setTitle("ATHAR – Sign In");
        stage.setMinWidth(440);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        HibernateUtil.shutdown();
    }
}
