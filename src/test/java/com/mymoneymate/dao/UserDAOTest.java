package com.mymoneymate.dao;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mymoneymate.models.User;

public class UserDAOTest {
    private UserDAO userDAO;
    
    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
    }
    
    // Helper method to generate unique username
    private String generateUniqueUsername() {
        return "testuser_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    @Test
    void testCreateAndFindUser() throws SQLException {
        // Create a test user with unique username
        String uniqueUsername = generateUniqueUsername();
        User newUser = new User(uniqueUsername, "password123", uniqueUsername + "@example.com");
        
        User createdUser = userDAO.create(newUser);
        assertNotNull(createdUser.getId());
        
        // Try to find the user by ID
        Optional<User> foundUser = userDAO.findById(createdUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(uniqueUsername, foundUser.get().getUsername());
        
        // Clean up
        userDAO.delete(createdUser.getId());
    }
    
    @Test
    void testUpdateUser() throws SQLException {
        // Create a test user with unique username
        String uniqueUsername = generateUniqueUsername();
        User user = new User(uniqueUsername, "password123", uniqueUsername + "@example.com");
        
        User createdUser = userDAO.create(user);
        assertNotNull(createdUser.getId());
        
        // Update the user
        String newEmail = "updated_" + uniqueUsername + "@example.com";
        createdUser.setEmail(newEmail);
        boolean updated = userDAO.update(createdUser);
        assertTrue(updated);
        
        // Verify the update
        Optional<User> foundUser = userDAO.findById(createdUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(newEmail, foundUser.get().getEmail());
        
        // Clean up
        userDAO.delete(createdUser.getId());
    }
}