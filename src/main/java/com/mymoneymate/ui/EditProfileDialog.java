package com.mymoneymate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mymoneymate.dao.UserDAO;
import com.mymoneymate.models.User;

public class EditProfileDialog extends JDialog {

    private final Integer currentUserId;
    private final UserDAO userDAO;
    private final JTextField emailField;
    private final JTextField usernameField;
    private boolean profileUpdated = false;

    public EditProfileDialog(JFrame parent, Integer userId) {
        super(parent, "Edit Profile", true);
        this.currentUserId = userId;
        this.userDAO = new UserDAO();

        setLayout(new BorderLayout());

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(15);
        gbc.gridx = 1;
        mainPanel.add(usernameField, gbc);

        // Email Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Email:"), gbc);
        emailField = new JTextField(15);
        gbc.gridx = 1;
        mainPanel.add(emailField, gbc);

        loadUserData();

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setForeground(Color.RED);

        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add panels to dialog
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Set dialog properties
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void loadUserData() {
        try {
            userDAO.findById(currentUserId).ifPresent(user -> {
                usernameField.setText(user.getUsername());
                emailField.setText(user.getEmail());
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading user data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSave() {
        try {
            String newUsername = usernameField.getText().trim();
            String newEmail = emailField.getText().trim();

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Email cannot be empty", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Optional<User> userOptional = userDAO.findById(currentUserId);
            if (userOptional.isEmpty()) {
                JOptionPane.showMessageDialog(this, "User not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User user = userOptional.get();
            user.setUsername(newUsername);
            user.setEmail(newEmail);

            if (userDAO.update(user)) {
                profileUpdated = true;
                JOptionPane.showMessageDialog(this, "Profile updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating profile: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isProfileUpdated() {
        return profileUpdated;
    }
}
