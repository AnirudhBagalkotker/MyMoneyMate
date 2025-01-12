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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mymoneymate.models.Budget;
import com.mymoneymate.services.BudgetService;
import com.mymoneymate.services.CategoryService;
import com.mymoneymate.services.exceptions.ServiceException;

public class EditBudgetDialog extends JDialog {
	private final Integer currentUserId;
	private final Budget budget;
	private final BudgetService budgetService;
	private final CategoryService categoryService;
	private final JTextField categoryField;
	private final JTextField amountField;
	private final JComboBox<Budget.BudgetPeriod> periodComboBox;
	private final JTextField startDateField;
	private final JTextField endDateField;
	private boolean budgetUpdated = false;

	public EditBudgetDialog(JFrame parent, Integer userId, Budget oldBudget) {
		super(parent, "Edit Budget", true);
		this.currentUserId = userId;
		this.budget = oldBudget;
		this.budgetService = new BudgetService();
		this.categoryService = new CategoryService();

		setLayout(new BorderLayout());

		// Create main panel with padding
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Category
		gbc.gridx = 0;
		gbc.gridy = 0;
		mainPanel.add(new JLabel("Category:"), gbc);

		categoryField = new JTextField(10);

		try {
			int categoryId = budget.getCategoryId();
			categoryField.setText(categoryService.getCategoryById(categoryId).getName());
		} catch (ServiceException e) {
			e.printStackTrace();
		}

		gbc.gridx = 1;
		mainPanel.add(categoryField, gbc);

		// Amount
		gbc.gridx = 0;
		gbc.gridy = 1;
		mainPanel.add(new JLabel("Amount:"), gbc);

		amountField = new JTextField(10);
		amountField.setText(budget.getAmount().toString());
		gbc.gridx = 1;
		mainPanel.add(amountField, gbc);

		// Budget Period
		gbc.gridx = 0;
		gbc.gridy = 2;
		mainPanel.add(new JLabel("Period:"), gbc);

		periodComboBox = new JComboBox<>(Budget.BudgetPeriod.values());
		for (int i = 0; i < periodComboBox.getItemCount(); i++) {
			if (periodComboBox.getItemAt(i).name().equals(budget.getPeriod().name())) {
				periodComboBox.setSelectedIndex(i);
				break;
			}
		}
		gbc.gridx = 1;
		mainPanel.add(periodComboBox, gbc);

		// Start Date
		gbc.gridx = 0;
		gbc.gridy = 3;
		mainPanel.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);

		startDateField = new JTextField(10);
		startDateField.setText(budget.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
		gbc.gridx = 1;
		mainPanel.add(startDateField, gbc);

		// End Date
		gbc.gridx = 0;
		gbc.gridy = 4;
		mainPanel.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);

		endDateField = new JTextField(10);
		endDateField.setText(budget.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
		gbc.gridx = 1;
		mainPanel.add(endDateField, gbc);

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

	private void handleSave() {
		try {
			BigDecimal amount;
			try {
				amount = new BigDecimal(amountField.getText().trim());
				if (amount.compareTo(BigDecimal.ZERO) <= 0) {
					throw new NumberFormatException("Amount must be positive");
				}
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Please enter a valid positive amount", "Invalid Amount",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			LocalDate startDate;
			LocalDate endDate;
			try {
				startDate = LocalDate.parse(startDateField.getText().trim());
				endDate = endDateField.getText().trim().isEmpty() ? null
						: LocalDate.parse(endDateField.getText().trim());
				if (endDate != null && startDate.isAfter(endDate)) {
					JOptionPane.showMessageDialog(this, "Start date cannot be after end date", "Invalid Date",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			} catch (DateTimeParseException e) {
				JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format", "Invalid Date",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			Budget.BudgetPeriod period = (Budget.BudgetPeriod) periodComboBox.getSelectedItem();

			// Update the budget
			budget.setAmount(amount);
			budget.setPeriod(period);
			budget.setStartDate(startDate);
			budget.setEndDate(endDate);

			budgetService.updateBudget(budget);
			budgetUpdated = true;
			dispose();

		} catch (ServiceException e) {
			System.err.println("Error saving budget: " + e.getCause().getMessage());
			JOptionPane.showMessageDialog(this,
					"Error saving budget: " + e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void handleDelete() {
		int result = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to delete this budget?",
				"Confirm Delete",
				JOptionPane.YES_NO_OPTION);

		if (result != JOptionPane.YES_OPTION) {
			return;
		}

		try {
			budgetService.deleteBudget(budget.getId());
			budgetUpdated = true;
			dispose();
		} catch (ServiceException e) {
			System.err.println("Error deleting budget: " + e.getCause().getMessage());
			JOptionPane.showMessageDialog(this,
					"Error deleting budget: " + e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public boolean isBudgetUpdated() {
		return budgetUpdated;
	}
}
