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
        // Look inside db/.env first
        File envFile = new File("db/.env");
        if (!envFile.exists()) {
            envFile = new File(".env");
        }
        if (!envFile.exists()) {
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

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
