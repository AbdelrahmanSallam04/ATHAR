package com.nonprofit.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Thread-safe Singleton SessionFactory utility for Hibernate in plain Java.
 * Reads db/.env directly using standard Java (no external libraries required)
 * and populates Java system properties so they resolve in hibernate.cfg.xml.
 */
public class HibernateUtil {

    // Singleton instance created only ONCE when the class is loaded
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // 1. Load db/.env into Java System Properties using pure standard Java
            loadEnvFile();

            // 2. Build the SessionFactory using hibernate.cfg.xml
            return new Configuration().configure().buildSessionFactory();
        } catch (Exception ex) {
            System.err.println("Initial SessionFactory creation failed: " + ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Reads key=value pairs from db/.env and sets them as Java System Properties.
     */
    private static void loadEnvFile() {
        File envFile = findEnvFile();
        if (envFile == null) {
            System.err.println("Warning: db/.env not found (working directory: "
                    + System.getProperty("user.dir") + ")");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comment lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String value = line.substring(eqIdx + 1).trim();

                    // Strip optional quotes
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }

                    System.setProperty(key, value);
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not read .env file: " + e.getMessage());
        }
    }

    /**
     * Finds db/.env (or .env) by walking up from the compiled classes folder,
     * then from the working directory, so the app runs from any folder.
     */
    private static File findEnvFile() {
        File[] starts = new File[2];
        try {
            starts[0] = new File(HibernateUtil.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
        } catch (Exception ignored) {
        }
        starts[1] = new File(System.getProperty("user.dir"));

        for (File start : starts) {
            for (File dir = start; dir != null; dir = dir.getParentFile()) {
                File candidate = new File(dir, "db/.env");
                if (candidate.isFile()) {
                    return candidate;
                }
                candidate = new File(dir, ".env");
                if (candidate.isFile()) {
                    return candidate;
                }
            }
        }
        return null;
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
