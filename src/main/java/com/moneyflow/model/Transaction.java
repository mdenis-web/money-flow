package com.moneyflow.model;

import java.time.LocalDateTime;
import java.util.Comparator;

public abstract class Transaction {
    protected String category;
    protected double amount;
    protected LocalDateTime date;

    protected Transaction() {
    }

    protected Transaction(String category, double amount, LocalDateTime date) {
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public abstract TransactionType getType();

    public static class DateComparator implements Comparator<Transaction> {
        @Override
        public int compare(Transaction a, Transaction b) {
            return a.getDate().compareTo(b.getDate());
        }
    }
}
