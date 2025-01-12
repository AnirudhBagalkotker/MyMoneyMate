package com.mymoneymate.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class TransactionTest {
    
    @Test
    public void testTransactionCreation() {
        Transaction transaction = new Transaction(
            1,                          // userId
            new BigDecimal("100.50"),   // amount
            1,                          // categoryId
            "Test transaction",         // description
            LocalDate.now(),            // transactionDate
            Category.TransactionType.EXPENSE  // type
        );
        
        assertNotNull(transaction);
        assertEquals(new BigDecimal("100.50"), transaction.getAmount());
        assertEquals("Test transaction", transaction.getDescription());
        assertEquals(Category.TransactionType.EXPENSE, transaction.getType());
        assertNotNull(transaction.getCreatedAt());
    }
}