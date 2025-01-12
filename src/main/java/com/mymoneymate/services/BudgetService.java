package com.mymoneymate.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mymoneymate.dao.BudgetDAO;
import com.mymoneymate.dao.CategoryDAO;
import com.mymoneymate.dao.TransactionDAO;
import com.mymoneymate.models.Budget;
import com.mymoneymate.models.Category;
import com.mymoneymate.services.exceptions.ServiceException;
import com.mymoneymate.services.exceptions.ValidationException;

public class BudgetService {
    private final BudgetDAO budgetDAO;
    private final CategoryDAO categoryDAO;
    private final TransactionDAO transactionDAO;

    public BudgetService() {
        this.budgetDAO = new BudgetDAO();
        this.categoryDAO = new CategoryDAO();
        this.transactionDAO = new TransactionDAO();
    }

    public Budget createBudget(Integer userId, Integer categoryId, BigDecimal amount, Budget.BudgetPeriod period,
            LocalDate startDate, LocalDate endDate) throws ServiceException {
        validateBudget(userId, categoryId, amount, period, startDate, endDate);

        try {
            // Verify category exists
            Optional<Category> category = categoryDAO.findById(categoryId);
            if (category.isEmpty()) {
                throw new ValidationException("Invalid category ID.");
            }

            Budget budget = new Budget(userId, categoryId, amount, period, startDate, endDate);
            return budgetDAO.create(budget);
        } catch (SQLException e) {
            throw new ServiceException("Error creating budget", e);
        }
    }

    public Budget updateBudget(Budget budget) throws ServiceException {
        validateBudget(budget.getUserId(), budget.getCategoryId(), budget.getAmount(), budget.getPeriod(),
                budget.getStartDate(), budget.getEndDate());

        try {
            // Verify category exists
            Optional<Category> category = categoryDAO.findById(budget.getCategoryId());
            if (category.isEmpty()) {
                throw new ValidationException("Invalid category ID.");
            }

            boolean updated = budgetDAO.update(budget);
            if (!updated) {
                throw new ServiceException("Failed to update budget.");
            }
            return budget;
        } catch (SQLException e) {
            throw new ServiceException("Error updating budget", e);
        }
    }

    public Budget getBudgetById(Integer budgetId) throws ServiceException {
        try {
            return budgetDAO.findById(budgetId)
                    .orElseThrow(() -> new ValidationException("Budget not found."));
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving budget", e);
        }
    }

    public List<Budget> getUserBudgets(Integer userId) throws ServiceException {
        try {
            return budgetDAO.findByUserId(userId);
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving budgets for user", e);
        }
    }

    public List<Budget> getCurrentUserBudgets(Integer userId) throws ServiceException {
        try {
            return budgetDAO.findByUserIdAndPeriodAndDateRange(userId, Budget.BudgetPeriod.MONTHLY,
                    LocalDate.now().withDayOfMonth(1), LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving current budgets for user", e);
        }
    }

    public List<Budget> getBudgetsByCategoryId(Integer categoryId) throws ServiceException {
        try {
            return budgetDAO.findByCategoryId(categoryId);
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving budgets for category", e);
        }
    }

    public List<Budget> getBudgetsByDateRange(LocalDate startDate, LocalDate endDate) throws ServiceException {
        try {
            return budgetDAO.findByDateRange(startDate, endDate);
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving budgets for date range", e);
        }
    }

    public List<Budget> getBudgetsByUserIdAndDateRange(Integer userId, LocalDate startDate, LocalDate endDate)
            throws ServiceException {
        try {
            return budgetDAO.findByUserIdAndDateRange(userId, startDate, endDate);
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving budgets for user and date range", e);
        }
    }

    public static class BudgetStatus {
        private final Budget budget;
        private final BigDecimal spent;
        private final BigDecimal remaining;
        private final double percentageUsed;

        public BudgetStatus(Budget budget, BigDecimal spent, BigDecimal remaining, double percentageUsed) {
            this.budget = budget;
            this.spent = spent;
            this.remaining = remaining;
            this.percentageUsed = percentageUsed;
        }

        // Getters
        public Budget getBudget() {
            return budget;
        }

        public BigDecimal getSpent() {
            return spent;
        }

        public BigDecimal getRemaining() {
            return remaining;
        }

        public double getPercentageUsed() {
            return percentageUsed;
        }

        public boolean isOverBudget() {
            return percentageUsed > 100.0;
        }
    }

    /**
     * Checks the budget status for a given budget by calculating the total amount
     * spent within the budget's time frame and category. It returns a BudgetStatus
     * object that includes details about the total amount spent, remaining budget
     * amount, and the percentage of the budget that has been used.
     *
     * @param budget the Budget object for which to check the status
     * @return a BudgetStatus object containing the budget, total spent, remaining
     *         amount, and percentage used
     * @throws SQLException if there is an error accessing the database
     */

    public BudgetStatus checkBudgetStatus(Budget budget) throws ServiceException {
        LocalDate startDate = budget.getStartDate();
        LocalDate endDate = budget.getEndDate() != null ? budget.getEndDate() : LocalDate.now();

        try {
            BigDecimal totalSpent = transactionDAO.getSumByPeriodAndCategory(
                    budget.getUserId(),
                    budget.getCategoryId(),
                    startDate,
                    endDate);

            if (totalSpent == null)
                totalSpent = BigDecimal.ZERO;

            BigDecimal remaining = budget.getAmount().subtract(totalSpent);
            double percentageUsed = totalSpent
                    .multiply(new BigDecimal("100"))
                    .divide(budget.getAmount(), 2, RoundingMode.HALF_UP)
                    .doubleValue();

            return new BudgetStatus(budget, totalSpent, remaining, percentageUsed);
        } catch (SQLException e) {
            throw new ServiceException("Error checking budget status", e);
        }
    }

    public List<BudgetStatus> checkAllBudgets(Integer userId) throws ServiceException {
        List<BudgetStatus> statuses = new ArrayList<>();
        try {
            List<Budget> budgets = getUserBudgets(userId);

            for (Budget budget : budgets) {
                statuses.add(checkBudgetStatus(budget));
            }

            return statuses;
        } catch (ServiceException e) {
            throw new ServiceException("Error checking budgets", e);
        }
    }

    public boolean deleteBudget(Integer budgetId) throws ServiceException {
        try {
            boolean deleted = budgetDAO.delete(budgetId);
            if (!deleted) {
                throw new ValidationException("Budget not found or could not be deleted.");
            }
            return true;
        } catch (SQLException e) {
            throw new ServiceException("Error deleting budget", e);
        }
    }

    private void validateBudget(Integer userId, Integer categoryId, BigDecimal amount, Budget.BudgetPeriod period,
            LocalDate startDate, LocalDate endDate) throws ValidationException {
        if (userId == null) {
            throw new ValidationException("User ID cannot be null.");
        }
        if (categoryId == null) {
            throw new ValidationException("Category ID cannot be null.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero.");
        }
        if (period == null) {
            throw new ValidationException("Period cannot be null.");
        }
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start date and end date cannot be null.");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date.");
        }
    }
}
