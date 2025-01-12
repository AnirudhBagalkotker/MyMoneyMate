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
import com.mymoneymate.models.Transaction;
import com.mymoneymate.services.CategoryService;
import com.mymoneymate.services.TransactionService;
import com.mymoneymate.services.exceptions.ServiceException;

public class EditTransactionDialog extends JDialog {

    private final Transaction transaction;
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final JTextField amountField;
    private final JTextField dateField;
    private final JTextArea descriptionArea;
    private final JComboBox<Category> categoryComboBox;
    private final JComboBox<Category.TransactionType> typeComboBox;
    private boolean transactionUpdated = false;

    public EditTransactionDialog(JFrame parent, Integer userId, Transaction transaction) {
        super(parent, "Edit Transaction", true);
        this.transaction = transaction;
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
        typeComboBox.setSelectedItem(transaction.getType());
        typeComboBox.addActionListener(e -> updateCategories());
        gbc.gridx = 1;
        mainPanel.add(typeComboBox, gbc);

        // Category
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Category:"), gbc);

        categoryComboBox = new JComboBox<>();

        // Initialize categories and select current category
        updateCategories();
        for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
            if (categoryComboBox.getItemAt(i).getId().equals(transaction.getCategoryId())) {
                categoryComboBox.setSelectedIndex(i);
                break;
            }
        }
        gbc.gridx = 1;
        mainPanel.add(categoryComboBox, gbc);

        // Amount
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Amount:"), gbc);

        amountField = new JTextField(10);
        amountField.setText(transaction.getAmount().toString());
        gbc.gridx = 1;
        mainPanel.add(amountField, gbc);

        // Date
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);

        dateField = new JTextField(10);
        dateField.setText(transaction.getTransactionDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        gbc.gridx = 1;
        mainPanel.add(dateField, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Description:"), gbc);

        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setText(transaction.getDescription());
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        gbc.gridx = 1;
        mainPanel.add(scrollPane, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton deleteButton = new JButton("Delete");
        JButton cancelButton = new JButton("Cancel");

        // Style the buttons
        deleteButton.setForeground(Color.RED);
        cancelButton.setForeground(Color.GRAY);

        // Add action listeners
        saveButton.addActionListener(e -> handleSave());
        deleteButton.addActionListener(e -> handleDelete());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);

        // Add panels to dialog
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

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

            // Update the transaction
            transaction.setAmount(amount);
            transaction.setCategoryId(category.getId());
            transaction.setDescription(descriptionArea.getText().trim());
            transaction.setTransactionDate(date);
            transaction.setType((Category.TransactionType) typeComboBox.getSelectedItem());

            transactionService.updateTransaction(transaction);
            transactionUpdated = true;
            dispose();

        } catch (ServiceException e) {
            System.err.println("Error updating transaction: " + e.getCause().getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error updating transaction: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this transaction?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            transactionService.deleteTransaction(transaction.getId());
            transactionUpdated = true;
            dispose();
        } catch (ServiceException e) {
            System.err.println("Error deleting transaction: " + e.getCause().getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error deleting transaction: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isTransactionUpdated() {
        return transactionUpdated;
    }
}
