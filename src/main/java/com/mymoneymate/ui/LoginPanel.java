package com.mymoneymate.ui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.Optional;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.mymoneymate.dao.UserDAO;
import com.mymoneymate.models.User;

public class LoginPanel extends JPanel {

    private final MainWindow mainWindow;
    private final UserDAO userDAO;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.userDAO = new UserDAO();

        setLayout(new GridBagLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        ImageIcon icon = new ImageIcon(getClass().getResource("/Icons/logo-main.png"));
        JLabel iconLabel = new JLabel(icon);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(iconLabel, gbc);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(usernameField, gbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(passwordField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());
        buttonPanel.add(loginButton);

        registerButton = new JButton("Register");
        registerButton.addActionListener(e -> showRegistrationDialog());
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Optional<User> user = userDAO.findByUsername(username);
            if (user.isPresent() && user.get().getPassword().equals(password)) {
                mainWindow.showDashboard(user.get().getId());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRegistrationDialog() {
        JDialog dialog = new JDialog(mainWindow, "Register New User", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Components
        JTextField newUsernameField = new JTextField(20);
        JPasswordField newPasswordField = new JPasswordField(20);
        JTextField emailField = new JTextField(20);

        // Add components
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        dialog.add(newUsernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        dialog.add(newPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        dialog.add(emailField, gbc);

        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(e -> {
            try {
                User newUser = new User(
                        newUsernameField.getText(),
                        new String(newPasswordField.getPassword()),
                        emailField.getText());
                userDAO.create(newUser);
                JOptionPane.showMessageDialog(dialog,
                        "Registration successful! Please login.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Registration failed: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialog.add(registerBtn, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(mainWindow);
        dialog.setVisible(true);
    }
}
