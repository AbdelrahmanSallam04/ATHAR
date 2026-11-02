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
        // Connect to the database before the window opens
        HibernateUtil.getSessionFactory();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/nonprofit/view/shell.fxml"));

        Scene scene = new Scene(root, 1366, 768);
        scene.getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());

        URL logo = getClass().getResource(ShellController.LOGO_PATH);
        if (logo != null) {
            stage.getIcons().add(new Image(logo.toExternalForm()));
        }

        stage.setTitle("ATHAR – Non-Profit Management & Impact Platform");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        HibernateUtil.shutdown();
    }
}
