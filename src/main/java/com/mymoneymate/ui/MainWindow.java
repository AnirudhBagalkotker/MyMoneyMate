package com.mymoneymate.ui;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("FieldMayBeFinal")
public class MainWindow extends JFrame {

    private JPanel contentPane;
    private CardLayout cardLayout;
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;

    public MainWindow() {
        this.setTitle("MyMoneyMate");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(1200, 900));
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        ImageIcon icon = new ImageIcon(getClass().getResource("/Icons/favicon.png"));
        this.setIconImage(icon.getImage());

        // Initialize card layout for switching between panels
        cardLayout = new CardLayout();
        contentPane = new JPanel(cardLayout);
        this.setContentPane(contentPane);

        // Initialize panels
        loginPanel = new LoginPanel(this);

        // Add panels to card layout
        contentPane.add(loginPanel, "LOGIN");

        // Start with login panel
        cardLayout.show(contentPane, "LOGIN");

        // Center on screen
        this.pack();
        this.setLocationRelativeTo(null);
    }

    public void showLogin() {
        cardLayout.show(contentPane, "LOGIN");
    }

    public void showDashboard(int userId) {
        dashboardPanel = new DashboardPanel(this, userId);
        contentPane.add(dashboardPanel, "DASHBOARD");
        cardLayout.show(contentPane, "DASHBOARD");
    }
}
