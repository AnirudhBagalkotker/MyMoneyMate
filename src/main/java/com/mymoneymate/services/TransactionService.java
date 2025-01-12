package com.mymoneymate.services;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mymoneymate.dao.CategoryDAO;
import com.mymoneymate.dao.TransactionDAO;
import com.mymoneymate.models.Category;
import com.mymoneymate.models.Transaction;
import com.mymoneymate.services.exceptions.ServiceException;
import com.mymoneymate.services.exceptions.ValidationException;

public class TransactionService {
    private final TransactionDAO transactionDAO;
    private final CategoryDAO categoryDAO;

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.categoryDAO = new CategoryDAO();
    }

    public Transaction addTransaction(Integer userId, BigDecimal amount, Integer categoryId, String description,
            LocalDate transactionDate, Category.TransactionType type) throws ServiceException {

        validateTransaction(amount, categoryId, type, transactionDate);

        try {
            Optional<Category> category = categoryDAO.findById(categoryId);
            if (category.isEmpty()) {
                throw new ValidationException("Invalid category");
            }
            if (category.get().getType() != type) {
                throw new ValidationException(
                        "Category type does not match transaction type");
            }

            Transaction transaction = new Transaction(
                    userId, amount, categoryId, description,
                    transactionDate, type);

            return transactionDAO.create(transaction);

        } catch (SQLException e) {
            throw new ServiceException("Error adding transaction", e);
        }
    }

    public Transaction updateTransaction(Transaction transaction) throws ServiceException {
        validateTransaction(transaction.getAmount(), transaction.getCategoryId(), transaction.getType(),
                transaction.getTransactionDate());

        try {
            Optional<Category> category = categoryDAO.findById(transaction.getCategoryId());
            if (category.isEmpty()) {
                throw new ValidationException("Invalid category");
            }
            if (category.get().getType() != transaction.getType()) {
                throw new ValidationException("Category type does not match transaction type");
            }

            boolean isUpdated = transactionDAO.update(transaction);
            if (!isUpdated) {
                throw new ServiceException("Transaction update failed");
            }

            return transaction;
        } catch (SQLException e) {
            throw new ServiceException("Error updating transaction", e);
        }
    }

    public List<Transaction> getUserTransactions(Integer userId, LocalDate startDate, LocalDate endDate)
            throws ServiceException {
        try {
            return transactionDAO.findByUserIdAndDateRange(userId, startDate, endDate);
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving transactions", e);
        }
    }

    public List<BigDecimal> getTransactionTotalsByCategory(Integer userId, LocalDate startDate, LocalDate endDate)
            throws ServiceException {
        try {
            List<Category> categories = categoryDAO.findAll();
            List<BigDecimal> totals = new ArrayList<>();
            for (Category category : categories) {
                totals.add(transactionDAO.getSumByPeriodAndCategory(userId, category.getId(), startDate, endDate));
            }
            return totals;
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving transaction", e);
        }
    }

    public BigDecimal calculateBalance(Integer userId, LocalDate start, LocalDate end) throws ServiceException {
        try {
            BigDecimal income = transactionDAO.getSumByTypeAndPeriod(
                    userId, Category.TransactionType.INCOME, start, end);
            BigDecimal expenses = transactionDAO.getSumByTypeAndPeriod(
                    userId, Category.TransactionType.EXPENSE, start, end);
            return income.subtract(expenses);
        } catch (SQLException e) {
            throw new ServiceException("Error calculating balance", e);
        }
    }

    public BigDecimal calculateIncome(Integer userId, LocalDate start, LocalDate end) throws ServiceException {
        try {
            return transactionDAO.getSumByTypeAndPeriod(userId, Category.TransactionType.INCOME, start, end);
        } catch (SQLException e) {
            throw new ServiceException("Error calculating income", e);
        }
    }

    public BigDecimal calculateExpenses(Integer userId, LocalDate start, LocalDate end) throws ServiceException {
        try {
            return transactionDAO.getSumByTypeAndPeriod(userId, Category.TransactionType.EXPENSE, start, end);
        } catch (SQLException e) {
            throw new ServiceException("Error calculating expenses", e);
        }
    }

    public void deleteTransaction(Integer transactionId) throws ServiceException {
        try {
            transactionDAO.delete(transactionId);
        } catch (SQLException e) {
            throw new ServiceException("Error deleting transaction", e);
        }
    }

    private void validateTransaction(BigDecimal amount, Integer categoryId, Category.TransactionType type,
            LocalDate transactionDate) throws ValidationException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
        if (categoryId == null) {
            throw new ValidationException("Category is required");
        }
        if (type == null) {
            throw new ValidationException("Transaction type is required");
        }
        if (transactionDate == null) {
            throw new ValidationException("Transaction date is required");
        }
        if (transactionDate.isAfter(LocalDate.now())) {
            throw new ValidationException("Transaction date cannot be in the future");
        }
    }
}