package com.nonprofit.controller;

import com.nonprofit.model.User;
import com.nonprofit.service.AuthService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller handling authentication and user signup HTTP endpoints.
 * Built with standard Java com.sun.net.httpserver (zero external web framework dependencies).
 */
public class AuthController implements HttpHandler {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();

        // Add standard CORS headers for local/cross-origin development
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Session-Token");

        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            switch (path) {
                case "/api/signup" -> {
                    if ("POST".equals(method)) handleSignup(exchange);
                    else sendError(exchange, 405, "Method Not Allowed");
                }
                case "/api/login" -> {
                    if ("POST".equals(method)) handleLogin(exchange);
                    else sendError(exchange, 405, "Method Not Allowed");
                }
                case "/api/logout" -> {
                    if ("POST".equals(method)) handleLogout(exchange);
                    else sendError(exchange, 405, "Method Not Allowed");
                }
                case "/api/me" -> {
                    if ("GET".equals(method)) handleMe(exchange);
                    else sendError(exchange, 405, "Method Not Allowed");
                }
                default -> sendError(exchange, 404, "Endpoint Not Found: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleSignup(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, Object> json = parseJsonSimple(body);

        String name = (String) json.get("name");
        String contactInfo = (String) json.get("contactInfo");
        String password = (String) json.get("password");
        String role = (String) json.get("role");

        @SuppressWarnings("unchecked")
        List<String> skills = (List<String>) json.getOrDefault("skills", Collections.emptyList());
        @SuppressWarnings("unchecked")
        List<String> preferences = (List<String>) json.getOrDefault("preferences", Collections.emptyList());

        try {
            User registeredUser = authService.registerUser(name, contactInfo, password, role, skills, preferences);
            String responseJson = String.format(
                    "{\"success\":true,\"message\":\"User registered successfully\",\"userId\":%d,\"role\":\"%s\"}",
                    registeredUser.getId(), registeredUser.getRole()
            );
            sendJsonResponse(exchange, 201, responseJson);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(exchange, 400, e.getMessage());
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, Object> json = parseJsonSimple(body);

        String contactInfo = (String) json.get("contactInfo");
        String password = (String) json.get("password");

        try {
            AuthService.UserSession session = authService.authenticate(contactInfo, password);
            String roleSlug = session.role().toLowerCase().replace('_', '-');
            String redirectUrl = "/stubs/" + roleSlug + "-dashboard.html";

            // Set session token in response header and JSON
            exchange.getResponseHeaders().add("Set-Cookie", "sessionToken=" + session.token() + "; Path=/; HttpOnly");

            String responseJson = String.format(
                    "{\"success\":true,\"token\":\"%s\",\"userId\":%d,\"name\":\"%s\",\"contactInfo\":\"%s\",\"role\":\"%s\",\"redirectUrl\":\"%s\"}",
                    session.token(), session.userId(), escapeJson(session.name()), escapeJson(session.contactInfo()), session.role(), redirectUrl
            );
            sendJsonResponse(exchange, 200, responseJson);
        } catch (IllegalArgumentException e) {
            sendError(exchange, 401, e.getMessage());
        }
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String token = extractSessionToken(exchange);
        if (token != null) {
            authService.logout(token);
        }
        exchange.getResponseHeaders().add("Set-Cookie", "sessionToken=; Path=/; Max-Age=0; HttpOnly");
        sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Logged out successfully\"}");
    }

    private void handleMe(HttpExchange exchange) throws IOException {
        String token = extractSessionToken(exchange);
        Optional<AuthService.UserSession> sessionOpt = authService.getSession(token);

        if (sessionOpt.isPresent()) {
            AuthService.UserSession s = sessionOpt.get();
            String responseJson = String.format(
                    "{\"authenticated\":true,\"userId\":%d,\"name\":\"%s\",\"contactInfo\":\"%s\",\"role\":\"%s\"}",
                    s.userId(), escapeJson(s.name()), escapeJson(s.contactInfo()), s.role()
            );
            sendJsonResponse(exchange, 200, responseJson);
        } else {
            sendJsonResponse(exchange, 200, "{\"authenticated\":false}");
        }
    }

    private String extractSessionToken(HttpExchange exchange) {
        // 1. Check Header 'Authorization: Bearer <token>'
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }

        // 2. Check Header 'X-Session-Token'
        String customHeader = exchange.getRequestHeaders().getFirst("X-Session-Token");
        if (customHeader != null && !customHeader.isBlank()) {
            return customHeader.trim();
        }

        // 3. Check Cookie header
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String[] pair = cookie.trim().split("=", 2);
                if (pair.length == 2 && "sessionToken".equals(pair[0])) {
                    return pair[1];
                }
            }
        }
        return null;
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String json = String.format("{\"success\":false,\"error\":\"%s\"}", escapeJson(message));
        sendJsonResponse(exchange, statusCode, json);
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * Minimal lightweight JSON parser for primitive fields and string lists.
     */
    private static Map<String, Object> parseJsonSimple(String body) {
        Map<String, Object> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) {
            return map;
        }

        // Extract key-value string pairs
        Pattern stringFieldPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = stringFieldPattern.matcher(body);
        while (matcher.find()) {
            map.put(matcher.group(1), matcher.group(2));
        }

        // Extract string arrays, e.g., "skills": ["skill1", "skill2"]
        Pattern arrayPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\[([^\\]]*)\\]");
        Matcher arrayMatcher = arrayPattern.matcher(body);
        while (arrayMatcher.find()) {
            String arrayKey = arrayMatcher.group(1);
            String rawItems = arrayMatcher.group(2).trim();
            List<String> items = new ArrayList<>();
            if (!rawItems.isEmpty()) {
                Pattern itemPattern = Pattern.compile("\"([^\"]+)\"");
                Matcher itemMatcher = itemPattern.matcher(rawItems);
                while (itemMatcher.find()) {
                    items.add(itemMatcher.group(1));
                }
            }
            map.put(arrayKey, items);
        }

        return map;
    }
}
