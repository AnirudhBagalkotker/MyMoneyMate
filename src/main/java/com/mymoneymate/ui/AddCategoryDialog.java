package com.mymoneymate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

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
import com.mymoneymate.services.exceptions.ServiceException;

public class AddCategoryDialog extends JDialog {
	private final CategoryService categoryService;
	private final Integer currentUserId;
	private final JTextField nameField;
	private final JTextArea descriptionArea;
	private final JComboBox<Category.TransactionType> typeComboBox;
	private boolean categoryAdded = false;

	public AddCategoryDialog(JFrame parent, Integer userId) {
		this.currentUserId = userId;
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
		gbc.gridx = 1;
		mainPanel.add(typeComboBox, gbc);

		// Name
		gbc.gridx = 0;
		gbc.gridy = 1;
		mainPanel.add(new JLabel("Name:"), gbc);

		nameField = new JTextField(20);
		gbc.gridx = 1;
		mainPanel.add(nameField, gbc);

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

		// Set dialog properties
		pack();
		setLocationRelativeTo(parent);
		setResizable(false);
	}

	private void handleSave() {
		try {
			categoryService.createCategory(currentUserId, nameField.getText(),
					(Category.TransactionType) typeComboBox.getSelectedItem(), descriptionArea.getText());
			categoryAdded = true;
			dispose();
		} catch (ServiceException e) {
			System.err.println("Error Saving Category: " + e.getCause().getMessage());
			JOptionPane.showMessageDialog(this,
					"Error Saving Category: " + e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public boolean isCategoryAdded() {
		return categoryAdded;
	}

}
