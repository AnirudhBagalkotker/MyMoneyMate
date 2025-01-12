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
import javax.swing.JTextField;

import com.mymoneymate.models.Budget;
import com.mymoneymate.models.Category;
import com.mymoneymate.models.Category.TransactionType;
import com.mymoneymate.services.BudgetService;
import com.mymoneymate.services.CategoryService;
import com.mymoneymate.services.exceptions.ServiceException;

public class AddBudgetDialog extends JDialog {
	private final Integer userId;
	private final BudgetService budgetService;
	private final CategoryService categoryService;
	private final JTextField amountField;
	private final JComboBox<Budget.BudgetPeriod> periodComboBox;
	private final JTextField startDateField;
	private final JTextField endDateField;
	private final JComboBox<Category> categoryComboBox;
	private boolean budgetAdded = false;

	public AddBudgetDialog(JFrame parent, Integer userId) {
		super(parent, "Add Budget", true);
		this.userId = userId;
		this.budgetService = new BudgetService();
		this.categoryService = new CategoryService();

		setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Category
		gbc.gridx = 0;
		gbc.gridy = 0;
		mainPanel.add(new JLabel("Category:"), gbc);

		categoryComboBox = new JComboBox<>();
		gbc.gridx = 1;
		mainPanel.add(categoryComboBox, gbc);

		// Amount
		gbc.gridx = 0;
		gbc.gridy = 1;
		mainPanel.add(new JLabel("Amount:"), gbc);

		amountField = new JTextField(10);
		gbc.gridx = 1;
		mainPanel.add(amountField, gbc);

		// Budget Period
		gbc.gridx = 0;
		gbc.gridy = 2;
		mainPanel.add(new JLabel("Period:"), gbc);

		periodComboBox = new JComboBox<>(Budget.BudgetPeriod.values());
		gbc.gridx = 1;
		mainPanel.add(periodComboBox, gbc);

		// Start Date
		gbc.gridx = 0;
		gbc.gridy = 3;
		mainPanel.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);

		startDateField = new JTextField(10);
		startDateField.setText(LocalDate.of(1970, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
		gbc.gridx = 1;
		mainPanel.add(startDateField, gbc);

		// End Date
		gbc.gridx = 0;
		gbc.gridy = 4;
		mainPanel.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);

		endDateField = new JTextField(10);
		endDateField.setText(LocalDate.of(9999, 12, 31).format(DateTimeFormatter.ISO_LOCAL_DATE));
		gbc.gridx = 1;
		mainPanel.add(endDateField, gbc);

		// Buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton saveButton = new JButton("Save");
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setForeground(Color.RED);

		saveButton.addActionListener(e -> handleSave());
		cancelButton.addActionListener(e -> dispose());

		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);

		add(mainPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		updateCategories();

		pack();
		setLocationRelativeTo(parent);
		setResizable(false);
	}

	private void updateCategories() {
		categoryComboBox.removeAllItems();
		try {
			List<Category> categories = categoryService.getCategoriesByType(TransactionType.EXPENSE);
			for (Category category : categories) {
				categoryComboBox.addItem(category);
			}
		} catch (ServiceException e) {
			JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
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

			Category category = (Category) categoryComboBox.getSelectedItem();
			if (category == null) {
				JOptionPane.showMessageDialog(this, "Please select a category", "Invalid Category",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			Budget.BudgetPeriod period = (Budget.BudgetPeriod) periodComboBox.getSelectedItem();

			budgetService.createBudget(userId, category.getId(), amount, period, startDate, endDate);
			budgetAdded = true;
			dispose();

		} catch (ServiceException e) {
			JOptionPane.showMessageDialog(this, "Error saving budget: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public boolean isBudgetAdded() {
		return budgetAdded;
	}
}
