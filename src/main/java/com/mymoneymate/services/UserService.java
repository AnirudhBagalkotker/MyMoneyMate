package com.mymoneymate.services;

import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Pattern;

import com.mymoneymate.dao.UserDAO;
import com.mymoneymate.models.User;
import com.mymoneymate.services.exceptions.ServiceException;
import com.mymoneymate.services.exceptions.ValidationException;

public class UserService {

    private final UserDAO userDAO;
    private static final Pattern EMAIL_PATTERN
            = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public User registerUser(String username, String password, String email)
            throws ServiceException {
        // Validate input
        validateUsername(username);
        validatePassword(password);
        validateEmail(email);

        try {
            // Check if username already exists
            Optional<User> existingUser = userDAO.findByUsername(username);
            if (existingUser.isPresent()) {
                throw new ValidationException("Username already exists");
            }

            // Create new user
            User newUser = new User(username, hashPassword(password), email);
            return userDAO.create(newUser);

        } catch (SQLException e) {
            throw new ServiceException("Error registering user", e);
        }
    }

    public User login(String username, String password) throws ServiceException {
        try {
            Optional<User> user = userDAO.findByUsername(username);
            if (user.isPresent() && verifyPassword(password, user.get().getPassword())) {
                return user.get();
            }
            throw new ValidationException("Invalid username or password");
        } catch (SQLException e) {
            throw new ServiceException("Error during login", e);
        }
    }

    public boolean updatePassword(int userId, String password, String OldPassword) throws ServiceException {
        try {
            validatePassword(password);
            Optional<User> user = userDAO.findById(userId);
            if (user.isPresent()) {
                if (!verifyPassword(OldPassword, user.get().getPassword())) {
                    throw new ValidationException("Old password is incorrect");
                }
                user.get().setPassword(hashPassword(password));
                userDAO.update(user.get());
                return true;
            } else {
                throw new ValidationException("User not found");
            }
        } catch (SQLException e) {
            throw new ServiceException("Error updating password", e);
        }
    }

    private void validateUsername(String username) throws ValidationException {
        if (username == null || username.trim().length() < 3) {
            throw new ValidationException("Username must be at least 3 characters long");
        }
    }

    private void validatePassword(String password) throws ValidationException {
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }
    }

    private void validateEmail(String email) throws ValidationException {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }

    // In a real application, use a proper password hashing library like BCrypt
    private String hashPassword(String password) {
        // TODO: Implement proper password hashing
        return password;
    }

    private boolean verifyPassword(String inputPassword, String storedPassword) {
        // TODO: Implement proper password verification
        return inputPassword.equals(storedPassword);
    }
}
