package com.mymoneymate.services;

import com.mymoneymate.dao.TransactionDAO;
import com.mymoneymate.models.Transaction;
import com.mymoneymate.models.Category;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class ReportService {
	private final TransactionDAO transactionDAO;

	public ReportService(TransactionDAO transactionDAO) {
		this.transactionDAO = transactionDAO;
	}

	public static class MonthlyReport {
		private final LocalDate month;
		private final BigDecimal totalIncome;
		private final BigDecimal totalExpenses;
		private final BigDecimal netSavings;
		private final Map<String, BigDecimal> categoryTotals;

		public MonthlyReport(LocalDate month, BigDecimal totalIncome, BigDecimal totalExpenses,
				Map<String, BigDecimal> categoryTotals) {
			this.month = month;
			this.totalIncome = totalIncome;
			this.totalExpenses = totalExpenses;
			this.netSavings = totalIncome.subtract(totalExpenses);
			this.categoryTotals = categoryTotals;
		}

		// Getters
		public LocalDate getMonth() {
			return month;
		}

		public BigDecimal getTotalIncome() {
			return totalIncome;
		}

		public BigDecimal getTotalExpenses() {
			return totalExpenses;
		}

		public BigDecimal getNetSavings() {
			return netSavings;
		}

		public Map<String, BigDecimal> getCategoryTotals() {
			return categoryTotals;
		}
	}

	public MonthlyReport generateMonthlyReport(Integer userId, LocalDate month) throws SQLException {
		LocalDate startDate = month.withDayOfMonth(1);
		LocalDate endDate = month.with(TemporalAdjusters.lastDayOfMonth());

		BigDecimal totalIncome = transactionDAO.getSumByTypeAndPeriod(
				userId, Category.TransactionType.INCOME, startDate, endDate);
		BigDecimal totalExpenses = transactionDAO.getSumByTypeAndPeriod(
				userId, Category.TransactionType.EXPENSE, startDate, endDate);

		Map<String, BigDecimal> categoryTotals = transactionDAO.getCategoryTotalsByPeriod(
				userId, startDate, endDate);

		return new MonthlyReport(month,
				totalIncome != null ? totalIncome : BigDecimal.ZERO,
				totalExpenses != null ? totalExpenses : BigDecimal.ZERO,
				categoryTotals);
	}

	public List<MonthlyReport> generateAnnualReport(Integer userId, int year) throws SQLException {
		List<MonthlyReport> reports = new ArrayList<>();
		for (int month = 1; month <= 12; month++) {
			LocalDate date = LocalDate.of(year, month, 1);
			reports.add(generateMonthlyReport(userId, date));
		}
		return reports;
	}

	public static class SpendingTrend {
		private final List<LocalDate> dates;
		private final List<BigDecimal> amounts;
		private final String category;

		public SpendingTrend(List<LocalDate> dates, List<BigDecimal> amounts, String category) {
			this.dates = dates;
			this.amounts = amounts;
			this.category = category;
		}

		// Getters
		public List<LocalDate> getDates() {
			return dates;
		}

		public List<BigDecimal> getAmounts() {
			return amounts;
		}

		public String getCategory() {
			return category;
		}
	}

	public SpendingTrend analyzeSpendingTrend(Integer userId, Integer categoryId,
			LocalDate startDate, LocalDate endDate) throws SQLException {
		List<Transaction> transactions = transactionDAO.findByPeriodAndCategory(
				userId, categoryId, startDate, endDate);

		// Sort transactions by date
		transactions.sort(Comparator.comparing(Transaction::getTransactionDate));

		List<LocalDate> dates = new ArrayList<>();
		List<BigDecimal> amounts = new ArrayList<>();
		String categoryName = transactions.isEmpty() ? "Unknown" : transactions.get(0).getType().toString();

		for (Transaction transaction : transactions) {
			dates.add(transaction.getTransactionDate());
			amounts.add(transaction.getAmount());
		}

		return new SpendingTrend(dates, amounts, categoryName);
	}
}