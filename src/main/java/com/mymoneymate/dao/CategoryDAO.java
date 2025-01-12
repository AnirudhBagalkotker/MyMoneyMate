package com.mymoneymate.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mymoneymate.models.Category;

public class CategoryDAO implements BaseDAO<Category> {
	private final DatabaseManager dbManager;

	public CategoryDAO() {
		this.dbManager = DatabaseManager.getInstance();
	}

	@Override
	public Category create(Category category) throws SQLException {
		String sql = "INSERT INTO categories (name, type, description) VALUES (?, ?, ?)";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, category.getName());
			statement.setString(2, category.getType().toString());
			statement.setString(3, category.getDescription());

			int affectedRows = statement.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating user failed, no rows affected.");
			}

			try (Statement stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
				if (rs.next()) {
					category.setId(rs.getInt(1));
					return category;
				} else
					throw new SQLException("Creating category failed, no ID obtained.");
			}
		}
	}

	@Override
	public Optional<Category> findById(Integer id) throws SQLException {
		String sql = "SELECT * FROM categories WHERE id = ?";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setInt(1, id);
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					Category category = new Category(
							rs.getString("name"),
							Category.TransactionType.valueOf(rs.getString("type")),
							rs.getString("description"));
					category.setId(rs.getInt("id"));
					return Optional.of(category);
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public List<Category> findAll() throws SQLException {
		String sql = "SELECT * FROM categories";
		List<Category> categories = new ArrayList<>();

		try (Connection conn = dbManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				Category category = new Category(
						rs.getString("name"),
						Category.TransactionType.valueOf(rs.getString("type")),
						rs.getString("description"));
				category.setId(rs.getInt("id"));
				categories.add(category);
			}
			return categories;
		}
	}

	@Override
	public boolean delete(Integer id) throws SQLException {
		String sql = "DELETE FROM categories WHERE id = ?";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setInt(1, id);
			int affectedRows = statement.executeUpdate();
			return affectedRows > 0;
		}
	}

	@Override
	public boolean update(Category category) throws SQLException {
		String sql = "UPDATE categories SET name = ?, type = ?, description = ? WHERE id = ?";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setString(1, category.getName());
			statement.setString(2, category.getType().toString());
			statement.setString(3, category.getDescription());
			statement.setInt(4, category.getId());
			int affectedRows = statement.executeUpdate();
			return affectedRows > 0;
		}
	}

	public List<Category> findByType(Category.TransactionType type) throws SQLException {
		String sql = "SELECT * FROM categories WHERE type = ? ORDER BY name";
		List<Category> categories = new ArrayList<>();

		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setString(1, type.toString());
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					Category category = new Category(
							rs.getString("name"),
							Category.TransactionType.valueOf(rs.getString("type")),
							rs.getString("description"));
					category.setId(rs.getInt("id"));
					categories.add(category);
				}
			}
			return categories;
		}
	}

	public Optional<Category> findByName(String name) throws SQLException {
		String sql = "SELECT * FROM categories WHERE name = ?";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setString(1, name);
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					Category category = new Category(
							rs.getString("name"),
							Category.TransactionType.valueOf(rs.getString("type")),
							rs.getString("description"));
					category.setId(rs.getInt("id"));
					return Optional.of(category);
				}
			}
		}
		return Optional.empty();
	}
}
