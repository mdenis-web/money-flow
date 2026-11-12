package com.moneyflow.util;

import com.moneyflow.exception.ValidationException;

import java.util.Locale;

public final class Validator {
    private Validator() {
    }

    public static void requireNotEmpty(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Field '" + field + "' must not be empty");
        }
    }

    public static void requirePositive(double amount, String field) {
        if (amount <= 0) {
            throw new ValidationException("Field '" + field + "' must be > 0");
        }
    }

    public static double parseAmount(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Amount is not specified");
        }
        String normalized = raw.replace(",", "").replace(" ", "");
        try {
            double value = Double.parseDouble(normalized);
            if (value <= 0) {
                throw new ValidationException("Amount must be > 0");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid amount: " + raw);
        }
    }

    public static String format(double amount) {
        return String.format(Locale.US, "%,.2f", amount);
    }
}
