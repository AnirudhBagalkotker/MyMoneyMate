package com.mymoneymate;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.mymoneymate.dao.DatabaseManager;
import com.mymoneymate.ui.MainWindow;

public class Main {

    public static void main(String[] args) {
        System.out.println("MyMoneyMate - Personal Finance Management System");

        // Test & verify database connection
        DatabaseManager dbManager = DatabaseManager.getInstance();
        if (dbManager.testConnection()) {
            System.out.println("Successfully connected to the database!");
        } else {
            System.err.println("Failed to connect to the database!");
            return;
        }

        // Launch the UI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Launch the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        });
    }
}
