package com.moneyflow.model;

import java.time.LocalDateTime;

public class Income extends Transaction {
    public Income() {
    }

    public Income(String category, double amount, LocalDateTime date) {
        super(category, amount, date);
    }

    public Income(String category, double amount) {
        super(category, amount, LocalDateTime.now());
    }

    @Override
    public TransactionType getType() {
        return TransactionType.INCOME;
    }
}
