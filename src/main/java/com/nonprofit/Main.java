package com.nonprofit;

import com.nonprofit.controller.AuthController;
import com.nonprofit.controller.StaticFileHandler;
import com.nonprofit.repository.UserRepository;
import com.nonprofit.service.AuthService;
import com.nonprofit.util.HibernateUtil;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Application;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Main application entry point for the ATHAR system.
 * Starts the embedded authentication HTTP server in the background,
 * and launches the JavaFX desktop application.
 */
public class Main {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" Starting ATHAR Non-Profit Management System...  ");
        System.out.println("==================================================");

        // 1. Initialize Hibernate SessionFactory & verify database connection
        try {
            HibernateUtil.getSessionFactory();
            System.out.println("[Hibernate] SessionFactory initialized successfully.");
        } catch (Throwable e) {
            System.err.println("[Hibernate] Warning: Initial SessionFactory creation error: " + e.getMessage());
            System.err.println("[Hibernate] Ensure your database credentials in db/.env or .env are valid.");
        }

        // 2. Start embedded HTTP server in background thread for web auth / APIs
        startHttpServerAsync();

        // 3. Launch JavaFX Desktop Application
        Application.launch(App.class, args);
    }

    private static void startHttpServerAsync() {
        Thread serverThread = new Thread(() -> {
            try {
                UserRepository userRepository = new UserRepository();
                AuthService authService = new AuthService(userRepository);
                AuthController authController = new AuthController(authService);
                StaticFileHandler staticFileHandler = new StaticFileHandler();

                int port = getPort();
                HttpServer server = null;
                int[] candidatePorts = new int[]{port, 8085, 8090, 9090};
                for (int candidate : candidatePorts) {
                    try {
                        server = HttpServer.create(new InetSocketAddress(candidate), 0);
                        port = candidate;
                        break;
                    } catch (IOException ex) {
                        // Try next port candidate
                    }
                }

                if (server == null) {
                    System.err.println("Failed to bind HTTP server on candidate ports.");
                    return;
                }

                server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
                server.createContext("/api/", authController);
                server.createContext("/", staticFileHandler);
                server.start();

                System.out.println(">> Auth Web Server running at: http://localhost:" + port + "/");
                System.out.println(">> Web Pages: http://localhost:" + port + "/login.html, /signup.html");
                System.out.println("==================================================");

                final HttpServer finalServer = server;
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("\nShutting down ATHAR HTTP Server...");
                    finalServer.stop(1);
                    HibernateUtil.shutdown();
                }));
            } catch (Exception e) {
                System.err.println("Notice: Embedded HTTP server could not start: " + e.getMessage());
            }
        }, "athar-http-server");

        serverThread.setDaemon(true);
        serverThread.start();
    }

    private static int getPort() {
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return DEFAULT_PORT;
    }
}
