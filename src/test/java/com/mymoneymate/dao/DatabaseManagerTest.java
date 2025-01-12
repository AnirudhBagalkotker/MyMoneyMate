package com.mymoneymate.dao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerTest {
    
    @Test
    public void testDatabaseConnection() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        assertTrue(dbManager.testConnection(), "Database connection should be successful");
    }
}