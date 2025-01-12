package com.mymoneymate.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:database/mymoneymate.db";
    private static DatabaseManager instance;
    
    private DatabaseManager() {
        initializeDatabase();
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
    private void initializeDatabase() {
        // First, ensure we can load the schema file
        InputStream inputStream = getClass().getResourceAsStream("/database/schema.sql");
        if (inputStream == null) {
            System.err.println("Error: Could not find schema.sql in resources");
            return;
        }

        // Read the schema file
        String schema;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            schema = reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            System.err.println("Error reading schema.sql: " + e.getMessage());
            return;
        }

        // Execute the schema
        try (Connection conn = getConnection();
            Statement stmt = conn.createStatement()) {
            
            // Split the schema into individual statements
            String[] statements = schema.split(";");
            
            // Execute each statement
            for (String sql : statements) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql);
                }
            }
            System.out.println("Database initialized successfully.");
            
        } catch (SQLException e) {
            System.err.println("Error executing SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}