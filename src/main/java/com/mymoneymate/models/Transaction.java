package com.mymoneymate.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Transaction {
    private Integer id;
    private Integer userId;
    private BigDecimal amount;
    private Integer categoryId;
    private String description;
    private LocalDate transactionDate;
    private Category.TransactionType type;
    private LocalDateTime createdAt;

    // Constructor
    public Transaction(Integer userId, BigDecimal amount, Integer categoryId, String description,
            LocalDate transactionDate, Category.TransactionType type) {
        this.userId = userId;
        this.amount = amount;
        this.categoryId = categoryId;
        this.description = description;
        this.transactionDate = transactionDate;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Category.TransactionType getType() {
        return type;
    }

    public void setType(Category.TransactionType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAmountString() {
        return "₹" + amount.toString();
    }
}