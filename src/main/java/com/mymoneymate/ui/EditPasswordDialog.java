package com.mymoneymate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.mymoneymate.services.UserService;
import com.mymoneymate.services.exceptions.ServiceException;

public class EditPasswordDialog extends JDialog {

    private final Integer currentUserId;
    private final UserService userService;
    private final JTextField oldPasswordField;
    private final JTextField passwordField;
    private final JTextField confirmPasswordField;
    private boolean passwordUpdated = false;

    public EditPasswordDialog(JFrame parent, Integer userId) {
        super(parent, "Edit Password", true);
        this.currentUserId = userId;
        this.userService = new UserService();

        setLayout(new BorderLayout());

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Password Field
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Current Password:"), gbc);
        oldPasswordField = new JPasswordField(15);
        gbc.gridx = 1;
        mainPanel.add(oldPasswordField, gbc);

        // New Password Field
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("New Password:"), gbc);
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);

        // Confirm Password Field
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Confirm Password:"), gbc);
        confirmPasswordField = new JPasswordField(15);
        gbc.gridx = 1;
        mainPanel.add(confirmPasswordField, gbc);

        // Buttons Panel
        JPanel buttonPanel;
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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

    private void handleSave() {
        try {
            String oldPassword = oldPasswordField.getText().trim();
            String newPassword = passwordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newPassword.equals(confirmPassword)) {
                if (userService.updatePassword(currentUserId, newPassword, oldPassword)) {
                    passwordUpdated = true;
                    JOptionPane.showMessageDialog(this, "Password updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Password update failed", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ServiceException e) {
            JOptionPane.showMessageDialog(this, "Error updating password: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isPasswordUpdated() {
        return passwordUpdated;
    }
}
