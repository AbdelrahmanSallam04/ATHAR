package com.nonprofit;

import com.nonprofit.controller.AuthController;
import com.nonprofit.controller.StaticFileHandler;
import com.nonprofit.repository.UserRepository;
import com.nonprofit.service.AuthService;
import com.nonprofit.util.HibernateUtil;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Main application entry point for the ATHAR system.
 * Initializes Hibernate ORM and starts the embedded HTTP Web Server.
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

        // 2. Wire MVC Components
        UserRepository userRepository = new UserRepository();
        AuthService authService = new AuthService(userRepository);
        AuthController authController = new AuthController(authService);
        StaticFileHandler staticFileHandler = new StaticFileHandler();

        // 3. Configure and Start HTTP Server (Pure standard Java, no Spring Boot)
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

        final HttpServer finalServer = server;
        try {
            finalServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

            // Register Controllers
            finalServer.createContext("/api/", authController);
            finalServer.createContext("/", staticFileHandler);

            finalServer.start();

            System.out.println(">> Server running at: http://localhost:" + port + "/");
            System.out.println(">> API Endpoints:");
            System.out.println("   - POST http://localhost:" + port + "/api/signup");
            System.out.println("   - POST http://localhost:" + port + "/api/login");
            System.out.println("   - POST http://localhost:" + port + "/api/logout");
            System.out.println("   - GET  http://localhost:" + port + "/api/me");
            System.out.println("==================================================");

            // Shutdown Hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down ATHAR Server...");
                finalServer.stop(1);
                HibernateUtil.shutdown();
                System.out.println("Server stopped cleanly.");
            }));

        } catch (Exception e) {
            System.err.println("Failed to start HTTP server on port " + port + ": " + e.getMessage());
            e.printStackTrace();
        }
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
