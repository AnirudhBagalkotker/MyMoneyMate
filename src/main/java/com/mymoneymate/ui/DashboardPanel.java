package com.mymoneymate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import com.mymoneymate.dao.UserDAO;
import com.mymoneymate.models.Budget;
import com.mymoneymate.models.Category;
import com.mymoneymate.models.Transaction;
import com.mymoneymate.models.User;
import com.mymoneymate.services.BudgetService;
import com.mymoneymate.services.BudgetService.BudgetStatus;
import com.mymoneymate.services.CategoryService;
import com.mymoneymate.services.TransactionService;
import com.mymoneymate.services.exceptions.ServiceException;
import com.toedter.calendar.JDateChooser;

@SuppressWarnings("CallToPrintStackTrace")
public class DashboardPanel extends JPanel {

    private final MainWindow mainWindow;
    private final int currentUserId;
    private final TransactionService transactionService = new TransactionService();
    private final BudgetService budgetService = new BudgetService();
    private final CategoryService categoryService = new CategoryService();
    private final UserDAO userDAO = new UserDAO();
    private JTabbedPane tabbedPane;
    private final Font defaultFont = UIManager.getFont("Label.font");

    public DashboardPanel(MainWindow mainWindow, int userId) {
        this.mainWindow = mainWindow;
        this.currentUserId = userId;
        UIManager.put("TabbedPane.font", defaultFont.deriveFont(Font.BOLD, 16));
        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Add tabs
        tabbedPane.addTab("Overview", createOverviewPanel());
        tabbedPane.addTab("Transactions", createTransactionsPanel());
        tabbedPane.addTab("Budget", createBudgetPanel());
        tabbedPane.addTab("Category", createCategoryPanel());
        tabbedPane.addTab("Reports", createReportsPanel());
        tabbedPane.addTab("Account", createAccountPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Get Summary Data
        try {
            // Top Panel with Greeting and App Title
            JPanel topPanel = new JPanel(new GridLayout(2, 1));
            topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            String userName = userDAO.findById(currentUserId).get().getUsername();
            JLabel appTitleLabel = new JLabel("MyMoneyMate - Personal Finance Management System", SwingConstants.CENTER);
            appTitleLabel.setFont(defaultFont.deriveFont(Font.BOLD, 24));

            JLabel greetingLabel = new JLabel("Hi, " + userName, SwingConstants.CENTER);
            greetingLabel.setFont(defaultFont.deriveFont(Font.BOLD, 18));

            topPanel.add(appTitleLabel);
            topPanel.add(greetingLabel);

            // Buttons Panel
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JButton addTransactionButton = new JButton("Add Transaction");
            addTransactionButton.addActionListener(e -> showAddTransactionDialog());
            JButton addBudgetButton = new JButton("Add Budget");
            addBudgetButton.addActionListener(e -> showAddBudgetDialog());
            JButton addCategoryButton = new JButton("Add Category");
            addCategoryButton.addActionListener(e -> showAddCategoryDialog());
            JButton logoutButton = new JButton("Logout");
            logoutButton.addActionListener(e -> handleLogout());
            logoutButton.setForeground(Color.RED);

            addTransactionButton.setFocusPainted(false);
            addBudgetButton.setFocusPainted(false);
            addCategoryButton.setFocusPainted(false);
            logoutButton.setFocusPainted(false);

            buttonPanel.add(Box.createHorizontalGlue());
            buttonPanel.add(addTransactionButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
            buttonPanel.add(addBudgetButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
            buttonPanel.add(addCategoryButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
            buttonPanel.add(logoutButton);
            buttonPanel.add(Box.createHorizontalGlue());

            JPanel topPanel2 = new JPanel(new GridLayout(2, 1));
            topPanel2.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            topPanel2.add(topPanel, BorderLayout.NORTH);
            topPanel2.add(buttonPanel, BorderLayout.CENTER);

            panel.add(topPanel2, BorderLayout.NORTH);

            // Get Summary Data
            LocalDate monthStartDate = LocalDate.now().withDayOfMonth(1);
            LocalDate endDate = LocalDate.now();

            BigDecimal monthIncome = transactionService.calculateIncome(currentUserId, monthStartDate, endDate);
            BigDecimal monthExpenses = transactionService.calculateExpenses(currentUserId, monthStartDate, endDate);
            BigDecimal monthBalance = transactionService.calculateBalance(currentUserId, monthStartDate, endDate);

            BigDecimal totalBalance = transactionService.calculateBalance(currentUserId, LocalDate.of(1970, 1, 1), endDate);

            // Summary Panel
            JPanel summaryPanel = new JPanel(new GridLayout(4, 1));
            summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            Font boldFont = defaultFont.deriveFont(Font.BOLD, 16);

            JPanel mothIncomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
            JLabel monthIncomeLabel = new JLabel("Month Income:");
            monthIncomeLabel.setFont(boldFont);
            mothIncomePanel.add(monthIncomeLabel);
            mothIncomePanel.add(new JLabel("₹" + monthIncome.toString()));
            summaryPanel.add(mothIncomePanel);

            JPanel monthExpensesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
            JLabel monthExpensesLabel = new JLabel("Month Expenses:");
            monthExpensesLabel.setFont(boldFont);
            monthExpensesPanel.add(monthExpensesLabel);
            monthExpensesPanel.add(new JLabel("₹" + monthExpenses.toString()));
            summaryPanel.add(monthExpensesPanel);

            JPanel monthBalancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
            JLabel monthBalanceLabel = new JLabel("Month Balance:");
            monthBalanceLabel.setFont(boldFont);
            monthBalancePanel.add(monthBalanceLabel);
            monthBalancePanel.add(new JLabel("₹" + monthBalance.toString()));
            summaryPanel.add(monthBalancePanel);

            JPanel totalBalancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
            JLabel totalBalanceLabel = new JLabel("Current Balance:");
            totalBalanceLabel.setFont(boldFont);
            totalBalancePanel.add(totalBalanceLabel);
            totalBalancePanel.add(new JLabel("₹" + totalBalance.toString()));
            summaryPanel.add(totalBalancePanel);

            panel.add(summaryPanel, BorderLayout.CENTER);

            // Recent Transactions
            List<Transaction> recentTransactions = transactionService.getUserTransactions(currentUserId, endDate.minusDays(7), endDate);

            String[] columnNames = {"Date", "Category", "Amount", "Description"};
            Object[][] data = new Object[recentTransactions.size()][4];

            for (int i = 0; i < recentTransactions.size(); i++) {
                Transaction t = recentTransactions.get(i);
                data[i][0] = t.getTransactionDate().toString();
                data[i][1] = categoryService.getCategoryById(t.getCategoryId()).getName();
                data[i][2] = t;
                data[i][3] = t.getDescription();
            }

            JTable transactionTable = new JTable(data, columnNames);
            transactionTable.setFillsViewportHeight(true);
            transactionTable.setRowHeight(36);
            transactionTable.setBorder(null);
            transactionTable.setShowGrid(false);
            transactionTable.getTableHeader().setReorderingAllowed(false);
            transactionTable.getTableHeader().setResizingAllowed(false);
            transactionTable.getTableHeader().setFont(defaultFont.deriveFont(Font.BOLD, 18));
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);

            for (int x = 0; x < transactionTable.getColumnCount(); x++) {
                if (x == 2) {
                    transactionTable.getColumnModel().getColumn(x).setCellRenderer(new AmountCellRenderer());
                } else {
                    transactionTable.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
                }
            }

            JTableHeader transactionTableHeader = transactionTable.getTableHeader();
            DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) transactionTableHeader.getDefaultRenderer();
            headerRenderer.setHorizontalAlignment(JLabel.CENTER);
            transactionTableHeader.setDefaultRenderer(headerRenderer);

            JScrollPane scrollPane = new JScrollPane(transactionTable);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            panel.add(scrollPane, BorderLayout.SOUTH);

            return panel;
        } catch (ServiceException | SQLException e) {
            System.err.println("Error getting summary data");
            e.printStackTrace();

            JPanel summaryPanel = new JPanel(new GridLayout(4, 2, 5, 5));
            summaryPanel.add(new JLabel("Month Income:"));
            summaryPanel.add(new JLabel("₹0.00"));
            summaryPanel.add(new JLabel("Month Expenses:"));
            summaryPanel.add(new JLabel("₹0.00"));
            summaryPanel.add(new JLabel("Month Balance:"));
            summaryPanel.add(new JLabel("₹0.00"));
            summaryPanel.add(new JLabel("Current Balance:"));
            summaryPanel.add(new JLabel("₹0.00"));

            panel.add(summaryPanel, BorderLayout.CENTER);
            return panel;
        }
    }

    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add transaction table
        String[] columns = {"Date", "Category", "Amount", "Description"};
        Object[][] data = getTransactions();
        JTable transactionsTable = new JTable(data, columns);

        transactionsTable.setRowHeight(36);
        transactionsTable.setBorder(null);
        transactionsTable.setShowGrid(false);
        transactionsTable.getTableHeader().setReorderingAllowed(false);
        transactionsTable.getTableHeader().setResizingAllowed(false);
        transactionsTable.getTableHeader().setFont(defaultFont.deriveFont(Font.BOLD, 18));
        transactionsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    showEditTransactionDialog();
                }
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int x = 0; x < transactionsTable.getColumnCount(); x++) {
            if (x == 2) {
                transactionsTable.getColumnModel().getColumn(x).setCellRenderer(new AmountCellRenderer());
            } else {
                transactionsTable.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
            }
        }

        JTableHeader transactionsTableHeader = transactionsTable.getTableHeader();
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) transactionsTableHeader.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        transactionsTableHeader.setDefaultRenderer(headerRenderer);

        JScrollPane scrollPane = new JScrollPane(transactionsTable);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Transaction");
        addButton.addActionListener(e -> showAddTransactionDialog());
        buttonPanel.add(addButton);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBudgetPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add budget table
        String[] columns = {"Category", "Amount", "Spent", "Usage", "Period"};
        Object[][] data = getBudgets();
        JTable budgetTable = new JTable(data, columns);

        budgetTable.setRowHeight(36);
        budgetTable.setBorder(null);
        budgetTable.setShowGrid(false);
        budgetTable.getTableHeader().setReorderingAllowed(false);
        budgetTable.getTableHeader().setResizingAllowed(false);
        budgetTable.getTableHeader().setFont(defaultFont.deriveFont(Font.BOLD, 18));
        budgetTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    showEditBudgetDialog();
                }
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int x = 0; x < budgetTable.getColumnCount(); x++) {
            budgetTable.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
        }

        JTableHeader budgetTableHeader = budgetTable.getTableHeader();
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) budgetTableHeader.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        budgetTableHeader.setDefaultRenderer(headerRenderer);

        JScrollPane scrollPane = new JScrollPane(budgetTable);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Budget");
        addButton.addActionListener(e -> showAddBudgetDialog());
        buttonPanel.add(addButton);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add category table
        String[] columns = {"Category", "Type", "Added/Spent"};
        Object[][] data = getCategories();
        JTable categoryTable = new JTable(data, columns);
        categoryTable.setRowHeight(36);
        categoryTable.setBorder(null);
        categoryTable.setShowGrid(false);
        categoryTable.getTableHeader().setReorderingAllowed(false);
        categoryTable.getTableHeader().setResizingAllowed(false);
        categoryTable.getTableHeader().setFont(defaultFont.deriveFont(Font.BOLD, 18));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int x = 0; x < categoryTable.getColumnCount(); x++) {
            categoryTable.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
        }

        JTableHeader categoryTableHeader = categoryTable.getTableHeader();
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) categoryTableHeader.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        categoryTableHeader.setDefaultRenderer(headerRenderer);

        JScrollPane scrollPane = new JScrollPane(categoryTable);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JButton addButton = new JButton("Add Category");
        addButton.addActionListener(e -> showAddCategoryDialog());
        buttonPanel.add(addButton);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create top control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Date range selection
        JLabel fromLabel = new JLabel("From:");
        JDateChooser fromDate = new JDateChooser();
        fromDate.setDate(Date.from(LocalDate.now().minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        fromDate.setPreferredSize(new Dimension(160, 30));
        fromDate.setForeground(Color.WHITE);
        fromDate.getDateEditor().getUiComponent().setForeground(Color.WHITE);

        JLabel toLabel = new JLabel("To:");
        JDateChooser toDate = new JDateChooser();
        toDate.setDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        toDate.setPreferredSize(new Dimension(160, 30));
        toDate.setForeground(Color.WHITE);
        toDate.getDateEditor().getUiComponent().setForeground(Color.WHITE);

        JButton generateButton = new JButton("Generate Report");
        generateButton.addActionListener(e -> generateReport(fromDate.getDate(), toDate.getDate()));

        controlPanel.add(fromLabel);
        controlPanel.add(fromDate);
        controlPanel.add(toLabel);
        controlPanel.add(toDate);
        controlPanel.add(generateButton);

        // Create tabbed pane for different charts
        JTabbedPane chartsTabbedPane = new JTabbedPane();
        chartsTabbedPane.addTab("Expense Overview", createExpenseOverviewPanel());
        chartsTabbedPane.addTab("Category Distribution", createCategoryDistributionPanel());
        chartsTabbedPane.addTab("Income vs Expenses", createIncomeExpenseComparisonPanel());
        chartsTabbedPane.addTab("Budget Analysis", createBudgetAnalysisPanel());

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(chartsTabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createExpenseOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create dataset for the line chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            LocalDate startDate = LocalDate.now().minusMonths(6);
            LocalDate endDate = LocalDate.now();

            // Get monthly expenses
            List<Transaction> transactions = transactionService.getUserTransactions(currentUserId, startDate, endDate);

            // Group transactions by month
            Map<YearMonth, BigDecimal> monthlyExpenses = transactions.stream()
                    .filter(t -> t.getType() == Category.TransactionType.EXPENSE)
                    .collect(Collectors.groupingBy(
                            t -> YearMonth.from(t.getTransactionDate()),
                            Collectors.mapping(
                                    Transaction::getAmount,
                                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                            )
                    ));

            // Add data to dataset
            monthlyExpenses.forEach((month, amount)
                    -> dataset.addValue(amount.doubleValue(), "Expenses", month.toString())
            );

            // Create the chart
            JFreeChart lineChart = ChartFactory.createLineChart(
                    "Monthly Expenses Overview",
                    "Month",
                    "Amount (₹)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            // Customize the chart
            CategoryPlot plot = lineChart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(Color.GRAY);

            // Add the chart to a panel
            ChartPanel chartPanel = new ChartPanel(lineChart);
            chartPanel.setPreferredSize(new Dimension(800, 400));
            panel.add(chartPanel, BorderLayout.CENTER);

        } catch (ServiceException e) {
            e.printStackTrace();
            panel.add(new JLabel("Error generating expense overview chart"), BorderLayout.CENTER);
        }

        return panel;
    }

    private JPanel createCategoryDistributionPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create dataset for the pie chart
        DefaultPieDataset dataset = new DefaultPieDataset();

        try {
            LocalDate startDate = LocalDate.now().withDayOfMonth(1);
            LocalDate endDate = LocalDate.now();

            // Get category-wise expenses
            List<Transaction> transactions = transactionService.getUserTransactions(currentUserId, startDate, endDate);
            Map<Integer, BigDecimal> categoryExpenses = transactions.stream()
                    .filter(t -> t.getType() == Category.TransactionType.EXPENSE)
                    .collect(Collectors.groupingBy(
                            Transaction::getCategoryId,
                            Collectors.mapping(
                                    Transaction::getAmount,
                                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                            )
                    ));

            // Add data to dataset
            categoryExpenses.forEach((categoryId, amount) -> {
                try {
                    String categoryName = categoryService.getCategoryById(categoryId).getName();
                    dataset.setValue(categoryName, amount.doubleValue());
                } catch (ServiceException e) {
                    e.printStackTrace();
                }
            });

            // Create the chart
            JFreeChart pieChart = ChartFactory.createPieChart(
                    "Expense Distribution by Category",
                    dataset,
                    true,
                    true,
                    false
            );

            // Customize the chart
            PiePlot plot = (PiePlot) pieChart.getPlot();
            plot.setBackgroundPaint(Color.WHITE);

            // Add the chart to a panel
            ChartPanel chartPanel = new ChartPanel(pieChart);
            chartPanel.setPreferredSize(new Dimension(800, 400));
            panel.add(chartPanel, BorderLayout.CENTER);

        } catch (ServiceException e) {
            e.printStackTrace();
            panel.add(new JLabel("Error generating category distribution chart"), BorderLayout.CENTER);
        }

        return panel;
    }

    private JPanel createIncomeExpenseComparisonPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create dataset for the bar chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            LocalDate startDate = LocalDate.now().minusMonths(6);
            LocalDate endDate = LocalDate.now();

            // Get monthly income and expenses
            List<Transaction> transactions = transactionService.getUserTransactions(currentUserId, startDate, endDate);

            // Group transactions by month and type
            Map<YearMonth, Map<Category.TransactionType, BigDecimal>> monthlyTransactions = transactions.stream()
                    .collect(Collectors.groupingBy(
                            t -> YearMonth.from(t.getTransactionDate()),
                            Collectors.groupingBy(
                                    Transaction::getType,
                                    Collectors.mapping(
                                            Transaction::getAmount,
                                            Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                                    )
                            )
                    ));

            // Add data to dataset
            monthlyTransactions.forEach((month, typeMap) -> {
                dataset.addValue(
                        typeMap.getOrDefault(Category.TransactionType.INCOME, BigDecimal.ZERO).doubleValue(),
                        "Income",
                        month.toString()
                );
                dataset.addValue(
                        typeMap.getOrDefault(Category.TransactionType.EXPENSE, BigDecimal.ZERO).doubleValue(),
                        "Expenses",
                        month.toString()
                );
            });

            // Create the chart
            JFreeChart barChart = ChartFactory.createBarChart(
                    "Income vs Expenses Comparison",
                    "Month",
                    "Amount (₹)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            // Customize the chart
            CategoryPlot plot = barChart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(Color.GRAY);

            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, Color.GREEN);  // Income bars
            renderer.setSeriesPaint(1, Color.RED);    // Expense bars

            // Add the chart to a panel
            ChartPanel chartPanel = new ChartPanel(barChart);
            chartPanel.setPreferredSize(new Dimension(800, 400));
            panel.add(chartPanel, BorderLayout.CENTER);

        } catch (ServiceException e) {
            e.printStackTrace();
            panel.add(new JLabel("Error generating income vs expenses chart"), BorderLayout.CENTER);
        }

        return panel;
    }

    private JPanel createBudgetAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create dataset for the bar chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            // Get all budgets and their status
            List<BudgetStatus> budgetStatuses = budgetService.checkAllBudgets(currentUserId);

            // Add data to dataset
            for (BudgetStatus status : budgetStatuses) {
                Budget budget = status.getBudget();
                String categoryName = categoryService.getCategoryById(budget.getCategoryId()).getName();

                dataset.addValue(budget.getAmount().doubleValue(), "Budget", categoryName);
                dataset.addValue(status.getSpent().doubleValue(), "Spent", categoryName);
            }

            // Create the chart
            JFreeChart barChart = ChartFactory.createBarChart(
                    "Budget vs Actual Spending",
                    "Category",
                    "Amount (₹)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            // Customize the chart
            CategoryPlot plot = barChart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(Color.GRAY);

            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, Color.BLUE);   // Budget bars
            renderer.setSeriesPaint(1, Color.ORANGE); // Spent bars

            // Add the chart to a panel
            ChartPanel chartPanel = new ChartPanel(barChart);
            chartPanel.setPreferredSize(new Dimension(800, 400));
            panel.add(chartPanel, BorderLayout.CENTER);

        } catch (ServiceException e) {
            e.printStackTrace();
            panel.add(new JLabel("Error generating budget analysis chart"), BorderLayout.CENTER);
        }

        return panel;
    }

    private void generateReport(Date fromDate, Date toDate) {
        // Convert dates to LocalDate
        LocalDate startDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        try {
            // Get transactions for the selected period
            List<Transaction> transactions = transactionService.getUserTransactions(currentUserId, startDate, endDate);

            // Calculate summaries
            BigDecimal totalIncome = transactions.stream()
                    .filter(t -> t.getType() == Category.TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalExpenses = transactions.stream()
                    .filter(t -> t.getType() == Category.TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal netSavings = totalIncome.subtract(totalExpenses);

            // Create and show report dialog
            StringBuilder report = new StringBuilder();
            report.append("Financial Report\n\n");
            report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");
            report.append("Total Income: ₹").append(totalIncome).append("\n");
            report.append("Total Expenses: ₹").append(totalExpenses).append("\n");
            report.append("Net Savings: ₹").append(netSavings).append("\n\n");

            // Show report in a dialog
            JTextArea textArea = new JTextArea(report.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this,
                    scrollPane,
                    "Financial Report",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (ServiceException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error generating report: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshReportsPanel() {
        JPanel reportPanel = (JPanel) tabbedPane.getComponentAt(4);
        reportPanel.removeAll();
        reportPanel.add(createReportsPanel());
    }

    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // User Information Section
        JPanel userInfoPanel = new JPanel(new GridBagLayout());
        TitledBorder titledBorder = BorderFactory.createTitledBorder("User Information");
        titledBorder.setTitleFont(defaultFont.deriveFont(Font.BOLD, 18));

        Border lineBorder = BorderFactory.createLineBorder(Color.WHITE, 0);
        userInfoPanel.setBorder(BorderFactory.createCompoundBorder(lineBorder, titledBorder));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        try {
            Optional<User> user = userDAO.findById(currentUserId);
            if (user.isPresent()) {
                JLabel nameLabel = new JLabel("Name: ");
                JLabel nameValue = new JLabel(user.get().getUsername());
                JLabel emailLabel = new JLabel("Email: ");
                JLabel emailValue = new JLabel(user.get().getEmail());

                nameLabel.setFont(defaultFont.deriveFont(Font.BOLD, 16));
                emailLabel.setFont(defaultFont.deriveFont(Font.BOLD, 16));
                nameValue.setFont(defaultFont.deriveFont(Font.PLAIN, 16));
                emailValue.setFont(defaultFont.deriveFont(Font.PLAIN, 16));

                gbc.gridx = 0;
                gbc.gridy = 0;
                userInfoPanel.add(nameLabel, gbc);
                gbc.gridx = 1;
                userInfoPanel.add(nameValue, gbc);

                gbc.gridx = 0;
                gbc.gridy = 1;
                userInfoPanel.add(emailLabel, gbc);
                gbc.gridx = 1;
                userInfoPanel.add(emailValue, gbc);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user information");
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading user information: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton editProfileButton = new JButton("Edit Profile");
        editProfileButton.addActionListener(e -> showEditProfileDialog());
        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());
        logoutButton.setForeground(Color.RED);

        editProfileButton.setFocusPainted(false);
        changePasswordButton.setFocusPainted(false);
        logoutButton.setFocusPainted(false);

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(editProfileButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(changePasswordButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(logoutButton);
        buttonPanel.add(Box.createHorizontalGlue());

        // Main Panel Layout
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(userInfoPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showAddTransactionDialog() {
        AddTransactionDialog dialog = new AddTransactionDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                currentUserId);
        dialog.setVisible(true);

        if (dialog.isTransactionAdded()) {
            refreshPanels();
        }
    }

    private Object[][] getTransactions() {
        LocalDate startDate = LocalDate.of(1970, 1, 1);
        LocalDate endDate = LocalDate.now();
        try {
            List<Transaction> transactions = transactionService.getUserTransactions(
                    currentUserId, startDate, endDate);
            Object[][] data = new Object[transactions.size()][4];
            for (int i = 0; i < transactions.size(); i++) {
                Transaction transaction = transactions.get(i);
                data[i][0] = transaction.getTransactionDate().toString();
                data[i][1] = categoryService.getCategoryById(transaction.getCategoryId()).getName();
                data[i][2] = transaction;
                data[i][3] = transaction.getDescription();
            }
            return data;
        } catch (ServiceException e) {
            System.err.println("Error getting transactions");
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading transactions: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return new Object[0][];
        }
    }

    private void showEditTransactionDialog() {
        JPanel panel = (JPanel) tabbedPane.getComponentAt(1);
        JTable table = (JTable) ((JScrollPane) panel.getComponent(1)).getViewport().getView();
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            selectedRow = 1;
        }

        try {
            Transaction transaction = (Transaction) table.getValueAt(selectedRow, 2);
            LocalDate date = LocalDate.parse((String) table.getValueAt(selectedRow, 0));
            Category category = categoryService.getCategoriesByName((String) table.getValueAt(selectedRow, 1)).get();
            Category.TransactionType type = transaction.getType();
            BigDecimal amount = (BigDecimal) transaction.getAmount();
            String description = (String) table.getValueAt(selectedRow, 3);

            List<Transaction> transactions = transactionService.getUserTransactions(currentUserId, date, date);
            Transaction transactionToEdit = transactions.stream()
                    .filter(t -> t.getAmount().equals(amount) && t.getDescription().equals(description)
                    && t.getType().equals(type) && t.getCategoryId().equals(category.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException("Transaction not found"));

            EditTransactionDialog dialog = new EditTransactionDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    currentUserId,
                    transactionToEdit);
            dialog.setVisible(true);

            if (dialog.isTransactionUpdated()) {
                refreshPanels();
            }
        } catch (ServiceException e) {
            System.err.println("Error editing transaction: " + e.getCause().getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error editing transaction: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddBudgetDialog() {
        AddBudgetDialog dialog = new AddBudgetDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                currentUserId);
        dialog.setVisible(true);

        if (dialog.isBudgetAdded()) {
            refreshPanels();
        }
    }

    private Object[][] getBudgets() {
        try {
            List<BudgetStatus> budgets = budgetService.checkAllBudgets(currentUserId);
            Object[][] data = new Object[budgets.size()][5];

            for (int i = 0; i < budgets.size(); i++) {
                BudgetStatus budgetStatus = budgets.get(i);
                Budget budget = budgetStatus.getBudget();
                int categoryId = budget.getCategoryId();
                String categoryName = categoryService.getCategoryById(categoryId).getName();
                data[i][0] = categoryName;
                data[i][1] = "₹" + budget.getAmount().toString();
                data[i][2] = "₹" + budgetStatus.getSpent().toString();
                data[i][3] = budgetStatus.getPercentageUsed();
                data[i][4] = budget.getPeriod().name();
            }
            return data;
        } catch (ServiceException e) {
            System.err.println("Error getting budgets: " + e.getCause().getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading budgets: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return new Object[0][];
        }
    }

    private void showEditBudgetDialog() {
        JPanel panel = (JPanel) tabbedPane.getComponentAt(2);
        JTable table = (JTable) ((JScrollPane) panel.getComponent(1)).getViewport().getView();
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            selectedRow = 1;
        }

        String categoryName = (String) table.getValueAt(selectedRow, 0);
        BigDecimal amount = (BigDecimal) new BigDecimal(((String) table.getValueAt(selectedRow, 1)).substring(1));
        Budget.BudgetPeriod period = Budget.BudgetPeriod.valueOf((String) table.getValueAt(selectedRow, 4));

        try {
            Optional<Category> category = categoryService.getCategoriesByName(categoryName);
            if (category.isPresent()) {
                List<Budget> budgets = budgetService.getUserBudgets(currentUserId);
                Budget budgetToEdit = budgets.stream()
                        .filter(b -> Objects.equals(b.getCategoryId(), category.get().getId())
                        && b.getAmount().equals(amount) && b.getPeriod().equals(period))
                        .findFirst()
                        .orElseThrow(() -> new ServiceException("Budget not found"));

                EditBudgetDialog dialog = new EditBudgetDialog(
                        (JFrame) SwingUtilities.getWindowAncestor(this),
                        currentUserId,
                        budgetToEdit);
                dialog.setVisible(true);

                if (dialog.isBudgetUpdated()) {
                    refreshPanels();
                }
            }
        } catch (ServiceException ex) {
            System.err.println("Error editing budget: " + ex.getCause().getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error editing budget: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddCategoryDialog() {
        AddCategoryDialog dialog = new AddCategoryDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                currentUserId);
        dialog.setVisible(true);

        if (dialog.isCategoryAdded()) {
            refreshPanels();
        }
    }

    private Object[][] getCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            List<BigDecimal> categorySpent = transactionService.getTransactionTotalsByCategory(currentUserId,
                    LocalDate.now().withDayOfMonth(1), LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()));
            Object[][] data = new Object[categories.size()][3];

            for (int i = 0; i < categories.size(); i++) {
                Category category = categories.get(i);
                data[i][0] = category.getName();
                data[i][1] = category.getType().name();
                data[i][2] = "₹" + categorySpent.get(i).toString();
            }
            return data;
        } catch (ServiceException e) {
            System.err.println("Error getting categories: " + e.getCause().getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading categories: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return new Object[0][];
        }
    }

    private void showEditProfileDialog() {
        EditProfileDialog dialog = new EditProfileDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                currentUserId);
        dialog.setVisible(true);

        if (dialog.isProfileUpdated()) {
            refreshProfile();
        }
    }

    private void refreshProfile() {
        // Update profile
        JPanel profilePanel = (JPanel) tabbedPane.getComponentAt(5);
        JPanel userInfoPanel = (JPanel) profilePanel.getComponent(0);
        JLabel nameLabel = (JLabel) userInfoPanel.getComponent(1);
        JLabel emailLabel = (JLabel) userInfoPanel.getComponent(3);
        try {
            Optional<User> user = userDAO.findById(currentUserId);
            nameLabel.setText(user.get().getUsername());
            emailLabel.setText(user.get().getEmail());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showChangePasswordDialog() {
        EditPasswordDialog dialog = new EditPasswordDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                currentUserId);
        dialog.setVisible(true);

        if (dialog.isPasswordUpdated()) {
            handleLogout();
        }
    }

    private void refreshPanels() {
        tabbedPane.removeAll();

        // Add tabs
        tabbedPane.addTab("Overview", createOverviewPanel());
        tabbedPane.addTab("Transactions", createTransactionsPanel());
        tabbedPane.addTab("Budget", createBudgetPanel());
        tabbedPane.addTab("Category", createCategoryPanel());
        tabbedPane.addTab("Reports", createReportsPanel());
        tabbedPane.addTab("Account", createAccountPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void handleLogout() {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            mainWindow.showLogin();
        }
    }

    class AmountCellRenderer extends DefaultTableCellRenderer {

        public AmountCellRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public void setValue(Object value) {
            if (value instanceof Transaction transaction) {
                BigDecimal amount = transaction.getAmount();
                setText((transaction.getType() == Category.TransactionType.INCOME ? "+₹" : "-₹") + amount.abs().toString());
                setForeground(transaction.getType() == Category.TransactionType.INCOME ? Color.GREEN : Color.RED);
            } else {
                super.setValue(value);
            }
        }
    }
}
