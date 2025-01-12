package com.mymoneymate.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mymoneymate.models.Category;
import com.mymoneymate.models.Transaction;

public class TransactionDAO implements BaseDAO<Transaction> {

    private final DatabaseManager dbManager;

    public TransactionDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    @Override
    public Transaction create(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (amount, description, category_id, user_id, type, transaction_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setBigDecimal(1, transaction.getAmount());
            statement.setString(2, transaction.getDescription());
            statement.setInt(3, transaction.getCategoryId());
            statement.setInt(4, transaction.getUserId());
            statement.setString(5, transaction.getType().toString());
            statement.setDate(6, java.sql.Date.valueOf(transaction.getTransactionDate()));

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating transaction failed, no rows affected.");
            }

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    transaction.setId(rs.getInt(1));
                    return transaction;
                } else {
                    throw new SQLException("Creating transaction failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public Optional<Transaction> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ? ORDER BY transaction_date DESC, created_at DESC;";

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Transaction transaction = new Transaction(
                            rs.getInt("user_id"),
                            rs.getBigDecimal("amount"),
                            rs.getInt("category_id"),
                            rs.getString("description"),
                            rs.getDate("transaction_date").toLocalDate(),
                            Category.TransactionType.valueOf(rs.getString("type")));
                    transaction.setId(rs.getInt("id"));
                    return Optional.of(transaction);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Transaction> findAll() throws SQLException {
        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC, created_at DESC;";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Transaction transaction = new Transaction(
                        rs.getInt("user_id"),
                        rs.getBigDecimal("amount"),
                        rs.getInt("category_id"),
                        rs.getString("description"),
                        rs.getDate("transaction_date").toLocalDate(),
                        Category.TransactionType.valueOf(rs.getString("type")));
                transaction.setId(rs.getInt("id"));
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    @Override
    public boolean update(Transaction transaction) throws SQLException {
        String sql = "UPDATE transactions SET amount = ?, description = ?, category_id = ?, user_id = ?, type = ?, transaction_date = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setBigDecimal(1, transaction.getAmount());
            statement.setString(2, transaction.getDescription());
            statement.setInt(3, transaction.getCategoryId());
            statement.setInt(4, transaction.getUserId());
            statement.setString(5, transaction.getType().toString());
            statement.setDate(6, java.sql.Date.valueOf(transaction.getTransactionDate()));
            statement.setInt(7, transaction.getId());
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }
    }

    public List<Transaction> findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC, created_at DESC;";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                            rs.getInt("user_id"),
                            rs.getBigDecimal("amount"),
                            rs.getInt("category_id"),
                            rs.getString("description"),
                            rs.getDate("transaction_date").toLocalDate(),
                            Category.TransactionType.valueOf(rs.getString("type")));
                    transaction.setId(rs.getInt("id"));
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }

    public List<Transaction> findByCategoryId(Integer categoryId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE category_id = ? ORDER BY transaction_date DESC, created_at DESC;";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                            rs.getInt("user_id"),
                            rs.getBigDecimal("amount"),
                            rs.getInt("category_id"),
                            rs.getString("description"),
                            rs.getDate("transaction_date").toLocalDate(),
                            Category.TransactionType.valueOf(rs.getString("type")));
                    transaction.setId(rs.getInt("id"));
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }

    public List<Transaction> findByDateRange(String startDate, String endDate) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE transaction_date BETWEEN ? AND ? ORDER BY transaction_date DESC, created_at DESC;";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, startDate);
            statement.setString(2, endDate);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                            rs.getInt("user_id"),
                            rs.getBigDecimal("amount"),
                            rs.getInt("category_id"),
                            rs.getString("description"),
                            rs.getDate("transaction_date").toLocalDate(),
                            Category.TransactionType.valueOf(rs.getString("type")));
                    transaction.setId(rs.getInt("id"));
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }

    public List<Transaction> findByType(Category.TransactionType type) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE type = ? ORDER BY transaction_date DESC, created_at DESC;";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, type.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                            rs.getInt("user_id"),
                            rs.getBigDecimal("amount"),
                            rs.getInt("category_id"),
                            rs.getString("description"),
                            rs.getDate("transaction_date").toLocalDate(),
                            Category.TransactionType.valueOf(rs.getString("type")));
                    transaction.setId(rs.getInt("id"));
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }

    public BigDecimal sumByUserIdAndType(Integer userId, Category.TransactionType type) throws SQLException {
        String sql = "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = ?";
        BigDecimal sum = BigDecimal.ZERO;

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, type.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next() && rs.getBigDecimal(1) != null) {
                    sum = rs.getBigDecimal(1);
                }
            }
        }
        return sum;
    }

    public List<Transaction> findByUserIdAndDateRange(Integer userId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        String sql = "SELECT * FROM transactions WHERE user_id = ? AND transaction_date BETWEEN ? AND ? ORDER BY transaction_date DESC, created_at DESC;";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setDate(2, java.sql.Date.valueOf(startDate));
            statement.setDate(3, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                            rs.getInt("user_id"),
                            rs.getBigDecimal("amount"),
                            rs.getInt("category_id"),
                            rs.getString("description"),
                            rs.getDate("transaction_date").toLocalDate(),
                            Category.TransactionType.valueOf(rs.getString("type")));
                    transaction.setId(rs.getInt("id"));
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }

    public BigDecimal getSumByPeriodAndCategory(Integer userId, Integer categoryId, LocalDate startDate,
            LocalDate endDate) throws SQLException {
        String sql = "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND category_id = ? AND transaction_date BETWEEN ? AND ?";
        BigDecimal sum = BigDecimal.ZERO;

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, categoryId);
            statement.setDate(3, java.sql.Date.valueOf(startDate));
            statement.setDate(4, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next() && rs.getBigDecimal(1) != null) {
                    sum = rs.getBigDecimal(1);
                }
            }
        }
        return sum;
    }

    public BigDecimal getSumByTypeAndPeriod(Integer userId, Category.TransactionType type, LocalDate startDate,
            LocalDate endDate) throws SQLException {
        String sql = "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = ? AND transaction_date BETWEEN ? AND ?";
        BigDecimal sum = BigDecimal.ZERO;

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, type.toString());
            statement.setDate(3, java.sql.Date.valueOf(startDate));
            statement.setDate(4, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next() && rs.getBigDecimal(1) != null) {
                    sum = rs.getBigDecimal(1);
                }
            }
        }
        return sum;
    }

    public Map<String, BigDecimal> getCategoryTotalsByPeriod(Integer userId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        String sql = "SELECT category_id, SUM(amount) FROM transactions WHERE user_id = ? AND transaction_date BETWEEN ? AND ? GROUP BY category_id";
        Map<String, BigDecimal> categoryTotals = new HashMap<>();

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setDate(2, java.sql.Date.valueOf(startDate));
            statement.setDate(3, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    if (rs.getBigDecimal("SUM(amount)") == null) {
                        categoryTotals.put(rs.getString("category_id"), BigDecimal.ZERO);
                    } else {
                        categoryTotals.put(rs.getString("category_id"), rs.getBigDecimal("SUM(amount)"));
                    }
                }
            }
        }
        return categoryTotals;
    }

    public List<Transaction> findByPeriodAndCategory(Integer userId, Integer categoryId, LocalDate startDate,
            LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE user_id = ? AND category_id = ? AND transaction_date BETWEEN ? AND ? ORDER BY transaction_date DESC, created_at DESC;";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = dbManager.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, categoryId);
            statement.setDate(3, java.sql.Date.valueOf(startDate));
            statement.setDate(4, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                            rs.getInt("user_id"),
                            rs.getBigDecimal("amount"),
                            rs.getInt("category_id"),
                            rs.getString("description"),
                            rs.getDate("transaction_date").toLocalDate(),
                            Category.TransactionType.valueOf(rs.getString("type")));
                    transaction.setId(rs.getInt("id"));
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }
}
