package com.nonprofit;

import javafx.application.Application;

/**
 * Entry point. Launches JavaFX through a class that does not extend
 * Application, which avoids "JavaFX runtime components are missing"
 * when running from the classpath (VS Code / IntelliJ Run button).
 */
public class Main {

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
