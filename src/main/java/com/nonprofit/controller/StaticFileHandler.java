package com.nonprofit.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Handles serving static assets (HTML, CSS, JavaScript) from src/main/webapp.
 */
public class StaticFileHandler implements HttpHandler {

    private final File webAppRoot;

    public StaticFileHandler() {
        this.webAppRoot = new File("src/main/webapp");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // Default route redirects or serves login.html
        if ("/".equals(path) || path.isEmpty()) {
            path = "/login.html";
        }

        File requestedFile = new File(webAppRoot, path.startsWith("/") ? path.substring(1) : path);

        // Security check: ensure requested file is within webAppRoot
        try {
            if (!requestedFile.getCanonicalPath().startsWith(webAppRoot.getCanonicalPath())) {
                sendNotFound(exchange, "Access denied: Path traversal attempt.");
                return;
            }
        } catch (IOException e) {
            sendNotFound(exchange, "Invalid file path.");
            return;
        }

        if (!requestedFile.exists()) {
            File htmlCandidate = new File(webAppRoot, (path.startsWith("/") ? path.substring(1) : path) + ".html");
            if (htmlCandidate.exists()) {
                requestedFile = htmlCandidate;
            }
        }

        if (!requestedFile.exists() || requestedFile.isDirectory()) {
            sendNotFound(exchange, "Page or resource not found: " + path);
            return;
        }

        String contentType = determineContentType(requestedFile.getName());
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, requestedFile.length());

        try (FileInputStream fis = new FileInputStream(requestedFile);
             OutputStream os = exchange.getResponseBody()) {
            fis.transferTo(os);
        }
    }

    private void sendNotFound(HttpExchange exchange, String message) throws IOException {
        String html = "<!DOCTYPE html><html><head><title>ATHAR System</title></head><body style='font-family:sans-serif;text-align:center;padding:50px;'>"
                + "<h2>ATHAR System Backend Active</h2>"
                + "<p>" + message + "</p>"
                + "<p>Backend APIs available at: <code>/api/signup</code>, <code>/api/login</code>, <code>/api/me</code>, <code>/api/logout</code>.</p>"
                + "</body></html>";
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(404, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String determineContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "text/plain; charset=UTF-8";
    }
}
