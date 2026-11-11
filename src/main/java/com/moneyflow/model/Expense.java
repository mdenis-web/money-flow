package com.moneyflow.model;

import java.time.LocalDateTime;

public class Expense extends Transaction {
    public Expense() {
    }

    public Expense(String category, double amount, LocalDateTime date) {
        super(category, amount, date);
    }

    public Expense(String category, double amount) {
        super(category, amount, LocalDateTime.now());
    }

    @Override
    public TransactionType getType() {
        return TransactionType.EXPENSE;
    }
}
