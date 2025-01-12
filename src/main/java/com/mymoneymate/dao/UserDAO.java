package com.mymoneymate.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mymoneymate.models.User;

public class UserDAO implements BaseDAO<User> {
    private final DatabaseManager dbManager;

    public UserDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    @Override
    public User create(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) 
			{
            
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                    return user;
                } else throw new SQLException("Creating user failed, no ID obtained.");
            }
        }
    }

    @Override
    public Optional<User> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql)) 
			{
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                    );
                    user.setId(rs.getInt("id"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        
        try (Connection conn = dbManager.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) 
		{
            while (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email")
                );
                user.setId(rs.getInt("id"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, password = ?, email = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql)) {
            
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setInt(4, user.getId());
            
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql)) 
			{
            
            statement.setInt(1, id);
            
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = dbManager.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql)) 
			{
            
            statement.setString(1, username);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                    );
                    user.setId(rs.getInt("id"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }
}