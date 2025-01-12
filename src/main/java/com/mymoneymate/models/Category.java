package com.mymoneymate.models;

public class Category {
    private Integer id;
    private String name;
    private TransactionType type;
    private String description;

    public enum TransactionType {
        INCOME,
        EXPENSE
    }

    // Constructor
    public Category(String name, TransactionType type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}