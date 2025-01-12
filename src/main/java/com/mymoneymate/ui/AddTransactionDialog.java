package com.mymoneymate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.mymoneymate.models.Category;
import com.mymoneymate.services.CategoryService;
import com.mymoneymate.services.TransactionService;
import com.mymoneymate.services.exceptions.ServiceException;

public class AddTransactionDialog extends JDialog {

    private final Integer userId;
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final JTextField amountField;
    private final JTextField dateField;
    private final JTextArea descriptionArea;
    private final JComboBox<Category> categoryComboBox;
    private final JComboBox<Category.TransactionType> typeComboBox;
    private boolean transactionAdded = false;

    public AddTransactionDialog(JFrame parent, Integer userId) {
        super(parent, "Add Transaction", true);
        this.userId = userId;
        this.transactionService = new TransactionService();
        this.categoryService = new CategoryService();

        setLayout(new BorderLayout());

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Transaction Type
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Type:"), gbc);

        typeComboBox = new JComboBox<>(Category.TransactionType.values());
        typeComboBox.addActionListener(e -> updateCategories());
        gbc.gridx = 1;
        mainPanel.add(typeComboBox, gbc);

        // Category
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Category:"), gbc);

        categoryComboBox = new JComboBox<>();
        gbc.gridx = 1;
        mainPanel.add(categoryComboBox, gbc);

        // Amount
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Amount:"), gbc);

        amountField = new JTextField(10);
        gbc.gridx = 1;
        mainPanel.add(amountField, gbc);

        // Date
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);

        dateField = new JTextField(10);
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        gbc.gridx = 1;
        mainPanel.add(dateField, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Description:"), gbc);

        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        gbc.gridx = 1;
        mainPanel.add(scrollPane, gbc);

        // Buttons
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

        // Initialize categories
        updateCategories();

        // Set dialog properties
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void updateCategories() {
        categoryComboBox.removeAllItems();
        try {
            Category.TransactionType selectedType = (Category.TransactionType) typeComboBox.getSelectedItem();
            List<Category> categories = categoryService.getCategoriesByType(selectedType);
            for (Category category : categories) {
                categoryComboBox.addItem(category);
            }
        } catch (ServiceException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading categories: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSave() {
        try {
            // Validate amount
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountField.getText().trim());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new NumberFormatException("Amount must be positive");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid positive amount",
                        "Invalid Amount",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate date
            LocalDate date;
            try {
                date = LocalDate.parse(dateField.getText().trim());
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid date in YYYY-MM-DD format",
                        "Invalid Date",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get selected category
            Category category = (Category) categoryComboBox.getSelectedItem();
            if (category == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select a category",
                        "Invalid Category",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            transactionService.addTransaction(userId, amount, category.getId(), descriptionArea.getText().trim(), date,
                    (Category.TransactionType) typeComboBox.getSelectedItem());
            transactionAdded = true;
            dispose();

        } catch (ServiceException e) {
            System.err.println("Error saving transaction: " + e.getCause().getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error saving transaction: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isTransactionAdded() {
        return transactionAdded;
    }
}
