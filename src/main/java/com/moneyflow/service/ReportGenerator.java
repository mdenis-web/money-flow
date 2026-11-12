package com.moneyflow.service;

import com.moneyflow.logging.Logger;
import com.moneyflow.logging.LoggerFactory;
import com.moneyflow.model.*;
import com.moneyflow.util.Validator;

import java.time.LocalDate;
import java.util.*;

public class ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

    public String fullReport(Wallet wallet) {
        logger.info("Generating full report: income={}, expense={}",
            Validator.format(wallet.totalIncome()), Validator.format(wallet.totalExpense()));
        StringBuilder builder = new StringBuilder();
        builder.append("=== REPORT ===\n");
        builder.append("Total income: ").append(Validator.format(wallet.totalIncome())).append('\n');

        builder.append("Income by category:\n");
        Map<String, Double> income = wallet.incomeByCategory();
        if (income.isEmpty()) {
            builder.append("  (no data)\n");
        }
        income.forEach((key, value) -> builder.append("  ").append(key).append(": ")
            .append(Validator.format(value)).append('\n'));

        builder.append("Total expense: ").append(Validator.format(wallet.totalExpense())).append('\n');

        builder.append("Expenses by category:\n");
        Map<String, Double> expense = wallet.expenseByCategory();
        if (expense.isEmpty()) builder.append("  (no data)\n");
        expense.forEach((key, value) -> builder.append("  ").append(key).append(": ")
            .append(Validator.format(value)).append('\n'));

        builder.append("Budget by category:\n");
        Map<String, Double> remaining = wallet.budgetRemaining();
        if (remaining.isEmpty()) {
            builder.append("  (no budgets set)\n");
        } else {
            remaining.forEach((key, value) -> builder.append("  ").append(key).append(": ")
                .append(Validator.format(wallet.getBudget(key)))
                .append(", Remaining budget: ")
                .append(Validator.format(value)).append('\n'));
        }

        builder.append("Current balance: ").append(Validator.format(wallet.balance())).append('\n');
        return builder.toString();
    }

    public String categoryReport(Wallet wallet, String category) {
        logger.info("Category report: '{}'", category);
        StringBuilder builder = new StringBuilder();
        builder.append("=== CATEGORY REPORT: ").append(category).append(" ===\n");
        double income = wallet.incomeByCategory().getOrDefault(category, 0.0);
        double expense = wallet.expenseByCategory().getOrDefault(category, 0.0);
        builder.append("Income: ").append(Validator.format(income)).append('\n');
        builder.append("Expense: ").append(Validator.format(expense)).append('\n');
        Double budget = wallet.getBudget(category);
        if (budget != null) {
            builder.append("Budget: ").append(Validator.format(budget))
                .append(", Remaining: ").append(Validator.format(budget - expense)).append('\n');
        } else {
            builder.append("Budget not set\n");
        }
        return builder.toString();
    }

    public String categoriesReport(Wallet wallet, List<String> categories) {
        logger.info("Multi-category report: {}", categories);
        StringBuilder builder = new StringBuilder();
        builder.append("=== CATEGORIES REPORT ===\n");
        double totalIncome = 0, totalExpense = 0;
        Set<String> known = wallet.getCategories().keySet();

        for (String category : categories) {
            if (!known.contains(category)) {
                builder.append("! Category not found: ").append(category).append('\n');
                logger.warn("Category not found in report: '{}'", category);
                continue;
            }
            double income = wallet.incomeByCategory().getOrDefault(category, 0.0);
            double expense = wallet.expenseByCategory().getOrDefault(category, 0.0);
            totalIncome += income;
            totalExpense += expense;
            builder.append(category).append(": income ").append(Validator.format(income))
                .append(", expense ").append(Validator.format(expense)).append('\n');
        }
        builder.append("TOTAL income: ").append(Validator.format(totalIncome))
            .append(", TOTAL expense: ").append(Validator.format(totalExpense)).append('\n');
        return builder.toString();
    }

    public String periodReport(Wallet wallet, LocalDate from, LocalDate to) {
        logger.info("Period report: {} .. {}", from, to);
        StringBuilder builder = new StringBuilder();
        builder.append("=== PERIOD REPORT ").append(from).append(" .. ").append(to).append(" ===\n");
        double income = wallet.incomesBetween(from, to).stream().mapToDouble(Income::getAmount).sum();
        double expense = wallet.expensesBetween(from, to).stream().mapToDouble(Expense::getAmount).sum();
        builder.append("Income: ").append(Validator.format(income)).append('\n');
        builder.append("Expense: ").append(Validator.format(expense)).append('\n');
        builder.append("Difference: ").append(Validator.format(income - expense)).append('\n');
        return builder.toString();
    }
}
