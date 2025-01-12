package com.mymoneymate.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Budget {
    private Integer id;
    private Integer userId;
    private Integer categoryId;
    private BigDecimal amount;
    private BudgetPeriod period;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;

    public enum BudgetPeriod {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    // Constructor
    public Budget(Integer userId, Integer categoryId, BigDecimal amount, BudgetPeriod period, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public BudgetPeriod getPeriod() { return period; }
    public void setPeriod(BudgetPeriod period) { this.period = period; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}