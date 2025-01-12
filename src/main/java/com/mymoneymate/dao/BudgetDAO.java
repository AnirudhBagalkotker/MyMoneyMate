package com.mymoneymate.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mymoneymate.models.Budget;

public class BudgetDAO implements BaseDAO<Budget> {
	private final DatabaseManager dbManager;

	public BudgetDAO() {
		this.dbManager = DatabaseManager.getInstance();
	}

	@Override
	public Budget create(Budget budget) throws SQLException {
		String sql = "INSERT INTO budgets (user_id, category_id, amount, period, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setInt(1, budget.getUserId());
			statement.setInt(2, budget.getCategoryId());
			statement.setBigDecimal(3, budget.getAmount());
			statement.setString(4, budget.getPeriod().toString());
			statement.setDate(5, java.sql.Date.valueOf(budget.getStartDate()));
			statement.setDate(6, java.sql.Date.valueOf(budget.getEndDate()));

			int affectedRows = statement.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating transaction failed, no rows affected.");
			}

			try (Statement stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
				if (rs.next()) {
					budget.setId(rs.getInt(1));
					return budget;
				} else
					throw new SQLException("Creating budget failed, no ID obtained.");
			}
		}
	}

	@Override
	public Optional<Budget> findById(Integer id) throws SQLException {
		String sql = "SELECT * FROM budgets WHERE id = ?";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setInt(1, id);
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					Budget budget = new Budget(
							rs.getInt("user_id"),
							rs.getInt("category_id"),
							rs.getBigDecimal("amount"),
							Budget.BudgetPeriod.valueOf(rs.getString("period")),
							rs.getDate("start_date").toLocalDate(),
							rs.getDate("end_date").toLocalDate());
					budget.setId(rs.getInt("id"));
					return Optional.of(budget);
				}
			}
		}

		return Optional.empty();
	}

	@Override
	public List<Budget> findAll() throws SQLException {
		String sql = "SELECT * FROM budgets";
		List<Budget> budgets = new ArrayList<>();
		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql);
				ResultSet rs = statement.executeQuery()) {
			while (rs.next()) {
				Budget budget = new Budget(
						rs.getInt("user_id"),
						rs.getInt("category_id"),
						rs.getBigDecimal("amount"),
						Budget.BudgetPeriod.valueOf(rs.getString("period")),
						rs.getDate("start_date").toLocalDate(),
						rs.getDate("end_date").toLocalDate());
				budget.setId(rs.getInt("id"));
				budgets.add(budget);
			}
		}
		return budgets;
	}

	@Override
	public boolean update(Budget budget) throws SQLException {
		String sql = "UPDATE budgets SET user_id = ?, category_id = ?, amount = ?, period = ?, start_date = ?, end_date = ? WHERE id = ?";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setInt(1, budget.getUserId());
			statement.setInt(2, budget.getCategoryId());
			statement.setBigDecimal(3, budget.getAmount());
			statement.setString(4, budget.getPeriod().toString());
			statement.setDate(5, java.sql.Date.valueOf(budget.getStartDate()));
			statement.setDate(6, java.sql.Date.valueOf(budget.getEndDate()));
			statement.setInt(7, budget.getId());
			int affectedRows = statement.executeUpdate();
			return affectedRows > 0;
		}
	}

	@Override
	public boolean delete(Integer id) throws SQLException {
		String sql = "DELETE FROM budgets WHERE id = ?";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setInt(1, id);
			int affectedRows = statement.executeUpdate();
			return affectedRows > 0;
		}
	}

	public List<Budget> findByUserId(Integer userId) throws SQLException {
		String sql = "SELECT * FROM budgets WHERE user_id = ?";
		List<Budget> budgets = new ArrayList<>();
		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setInt(1, userId);
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					Budget budget = new Budget(
							rs.getInt("user_id"),
							rs.getInt("category_id"),
							rs.getBigDecimal("amount"),
							Budget.BudgetPeriod.valueOf(rs.getString("period")),
							rs.getDate("start_date").toLocalDate(),
							rs.getDate("end_date").toLocalDate());
					budget.setId(rs.getInt("id"));
					budgets.add(budget);
				}
			}
		}
		return budgets;
	}

	public List<Budget> findByCategoryId(Integer categoryId) throws SQLException {
		String sql = "SELECT * FROM budgets WHERE category_id = ?";
		List<Budget> budgets = new ArrayList<>();
		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setInt(1, categoryId);
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					Budget budget = new Budget(
							rs.getInt("user_id"),
							rs.getInt("category_id"),
							rs.getBigDecimal("amount"),
							Budget.BudgetPeriod.valueOf(rs.getString("period")),
							rs.getDate("start_date").toLocalDate(),
							rs.getDate("end_date").toLocalDate());
					budget.setId(rs.getInt("id"));
					budgets.add(budget);
				}
			}
		}
		return budgets;
	}

	public List<Budget> findByPeriod(Budget.BudgetPeriod period) throws SQLException {
		String sql = "SELECT * FROM budgets WHERE period = ?";
		List<Budget> budgets = new ArrayList<>();
		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setString(1, period.toString());
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					Budget budget = new Budget(
							rs.getInt("user_id"),
							rs.getInt("category_id"),
							rs.getBigDecimal("amount"),
							Budget.BudgetPeriod.valueOf(rs.getString("period")),
							rs.getDate("start_date").toLocalDate(),
							rs.getDate("end_date").toLocalDate());
					budget.setId(rs.getInt("id"));
					budgets.add(budget);
				}
			}
		}
		return budgets;
	}

	public List<Budget> findByUserIdAndPeriod(Integer userId, Budget.BudgetPeriod period) throws SQLException {
		String sql = "SELECT * FROM budgets WHERE user_id = ? AND period = ?";
		List<Budget> budgets = new ArrayList<>();
		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setInt(1, userId);
			statement.setString(2, period.toString());
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					Budget budget = new Budget(
							rs.getInt("user_id"),
							rs.getInt("category_id"),
							rs.getBigDecimal("amount"),
							Budget.BudgetPeriod.valueOf(rs.getString("period")),
							rs.getDate("start_date").toLocalDate(),
							rs.getDate("end_date").toLocalDate());
					budget.setId(rs.getInt("id"));
					budgets.add(budget);
				}
			}
		}
		return budgets;
	}

	public List<Budget> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
		String sql = "SELECT * FROM budgets WHERE start_date BETWEEN ? AND ?";
		List<Budget> budgets = new ArrayList<>();
		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setDate(2, java.sql.Date.valueOf(startDate));
			statement.setDate(3, java.sql.Date.valueOf(endDate));
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					Budget budget = new Budget(
							rs.getInt("user_id"),
							rs.getInt("category_id"),
							rs.getBigDecimal("amount"),
							Budget.BudgetPeriod.valueOf(rs.getString("period")),
							rs.getDate("start_date").toLocalDate(),
							rs.getDate("end_date").toLocalDate());
					budget.setId(rs.getInt("id"));
					budgets.add(budget);
				}
			}
		}
		return budgets;
	}

	public List<Budget> findByUserIdAndDateRange(Integer userId, LocalDate startDate, LocalDate endDate)
			throws SQLException {
		String sql = "SELECT * FROM budgets WHERE user_id = ? AND start_date BETWEEN ? AND ?";
		List<Budget> budgets = new ArrayList<>();
		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setInt(1, userId);
			statement.setDate(2, java.sql.Date.valueOf(startDate));
			statement.setDate(3, java.sql.Date.valueOf(endDate));
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					Budget budget = new Budget(
							rs.getInt("user_id"),
							rs.getInt("category_id"),
							rs.getBigDecimal("amount"),
							Budget.BudgetPeriod.valueOf(rs.getString("period")),
							rs.getDate("start_date").toLocalDate(),
							rs.getDate("end_date").toLocalDate());
					budget.setId(rs.getInt("id"));
					budgets.add(budget);
				}
			}
		}
		return budgets;
	}

	public List<Budget> findByUserIdAndPeriodAndDateRange(Integer userId, Budget.BudgetPeriod period,
			LocalDate startDate, LocalDate endDate) throws SQLException {
		String sql = "SELECT * FROM budgets WHERE user_id = ? AND period = ? AND start_date BETWEEN ? AND ?";
		List<Budget> budgets = new ArrayList<>();
		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setInt(1, userId);
			statement.setString(2, period.toString());
			statement.setDate(3, java.sql.Date.valueOf(startDate));
			statement.setDate(4, java.sql.Date.valueOf(endDate));
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					Budget budget = new Budget(
							rs.getInt("user_id"),
							rs.getInt("category_id"),
							rs.getBigDecimal("amount"),
							Budget.BudgetPeriod.valueOf(rs.getString("period")),
							rs.getDate("start_date").toLocalDate(),
							rs.getDate("end_date").toLocalDate());
					budget.setId(rs.getInt("id"));
					budgets.add(budget);
				}
			}
		}
		return budgets;
	}

}
