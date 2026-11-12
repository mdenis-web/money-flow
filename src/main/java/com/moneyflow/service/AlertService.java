package com.moneyflow.service;

import com.moneyflow.logging.Logger;
import com.moneyflow.logging.LoggerFactory;
import com.moneyflow.model.Wallet;
import com.moneyflow.util.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlertService {
    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    public List<String> check(Wallet wallet) {
        List<String> alerts = new ArrayList<>();

        if (wallet.totalExpense() > wallet.totalIncome()) {
            String message = "⚠ Expenses (" + Validator.format(wallet.totalExpense()) +
                ") exceed income (" + Validator.format(wallet.totalIncome()) + ")";
            alerts.add(message);
            logger.warn("ALERT: {}", message);
        }

        Map<String, Double> spent = wallet.expenseByCategory();
        for (Map.Entry<String, Double> entry : wallet.budgetRemaining().entrySet()) {
            String category = entry.getKey();
            double remaining = entry.getValue();
            Double budget = wallet.getBudget(category);
            if (budget == null || budget <= 0) {
                continue;
            }

            double used = spent.getOrDefault(category, 0.0);
            double ratio = used / budget;

            if (remaining < 0) {
                String message = "⚠ BUDGET EXCEEDED [" + category + "]: spent " +
                    Validator.format(used) + " of " + Validator.format(budget) +
                    ", overspend " + Validator.format(-remaining);
                alerts.add(message);
                logger.warn("ALERT: {}", message);
            } else if (ratio >= 0.8) {
                String message = "⚠ 80% LIMIT REACHED [" + category + "]: spent " +
                    Validator.format(used) + " of " + Validator.format(budget);
                alerts.add(message);
                logger.warn("ALERT: {}", message);
            }
        }

        if (wallet.balance() <= 0) {
            String message = "⚠ Wallet balance: " + Validator.format(wallet.balance());
            alerts.add(message);
            logger.warn("ALERT: {}", message);
        }

        if (!alerts.isEmpty()) {
            logger.info("Alerts generated: {}", alerts.size());
        }
        return alerts;
    }
}
