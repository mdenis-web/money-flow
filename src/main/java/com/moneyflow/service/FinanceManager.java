package com.moneyflow.service;

import com.moneyflow.exception.AuthException;
import com.moneyflow.exception.NotFoundException;
import com.moneyflow.exception.ValidationException;
import com.moneyflow.logging.Logger;
import com.moneyflow.logging.LoggerFactory;
import com.moneyflow.model.*;
import com.moneyflow.util.Validator;

import java.util.Map;

public class FinanceManager {
    private static final Logger logger = LoggerFactory.getLogger(FinanceManager.class);

    private final AuthService auth;
    private User currentUser;

    public FinanceManager(AuthService auth) {
        this.auth = auth;
    }

    public void login(String login, String password) {
        this.currentUser = auth.login(login, password);
        logger.info("Session opened for user '{}'", login);
    }

    public void logout() {
        if (currentUser != null) {
            logger.info("Session closed for user '{}'", currentUser.getLogin());
        }
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    private Wallet wallet() {
        if (currentUser == null) {
            logger.warn("Attempt to perform an operation without authentication");
            throw new AuthException("Please log in first");
        }
        return currentUser.getWallet();
    }

    private String user() {
        return currentUser == null ? "anonymous" : currentUser.getLogin();
    }

    public void addIncome(String category, double amount) {
        try {
            Validator.requireNotEmpty(category, "category");
            Validator.requirePositive(amount, "amount");
            wallet().addIncome(new Income(category, amount));
            logger.info("Income added: user='{}', category='{}', amount={}",
                user(), category, amount);
        } catch (RuntimeException exception) {
            logger.warn("Failed to add income (user='{}', category='{}', amount={}): {}",
                user(), category, amount, exception.getMessage());
            throw exception;
        }
    }

    public void addExpense(String category, double amount) {
        try {
            Validator.requireNotEmpty(category, "category");
            Validator.requirePositive(amount, "amount");
            wallet().addExpense(new Expense(category, amount));
            logger.info("Expense added: user='{}', category='{}', amount={}",
                user(), category, amount);
        } catch (RuntimeException exception) {
            logger.warn("Failed to add expense (user='{}', category='{}', amount={}): {}",
                user(), category, amount, exception.getMessage());
            throw exception;
        }
    }

    public void addCategory(String name) {
        try {
            Validator.requireNotEmpty(name, "name");
            wallet().addCategory(name);
            logger.info("Category created: user='{}', name='{}'", user(), name);
        } catch (RuntimeException exception) {
            logger.warn("Failed to create category (user='{}', name='{}'): {}",
                user(), name, exception.getMessage());
            throw exception;
        }
    }

    public void setBudget(String category, double amount) {
        try {
            Validator.requireNotEmpty(category, "category");
            Validator.requirePositive(amount, "amount");
            wallet().setBudget(category, amount);
            logger.info("Budget set: user='{}', category='{}', amount={}",
                user(), category, amount);
        } catch (RuntimeException exception) {
            logger.warn("Failed to set budget (user='{}', category='{}', amount={}): {}",
                user(), category, amount, exception.getMessage());
            throw exception;
        }
    }

    public void editBudget(String category, double amount) {
        if (!wallet().getCategories().containsKey(category)) {
            logger.warn("Attempt to edit a non-existent budget: user='{}', category='{}'",
                user(), category);
            throw new NotFoundException("Category not found: " + category);
        }
        Double old = wallet().getBudget(category);
        setBudget(category, amount);
        logger.info("Budget updated: user='{}', category='{}', old={}, new={}",
            user(), category, old, amount);
    }

    public void deleteBudget(String category) {
        wallet().deleteBudget(category);
        logger.info("Budget deleted: user='{}', category='{}'", user(), category);
    }

    public void deleteCategory(String category) {
        if (!wallet().getCategories().containsKey(category)) {
            logger.warn("Attempt to delete a non-existent category: user='{}', category='{}'",
                user(), category);
            throw new NotFoundException("Category not found: " + category);
        }
        wallet().deleteCategory(category);
        logger.info("Category deleted: user='{}', category='{}'", user(), category);
    }

    public void transfer(String recipientLogin, double amount) {
        try {
            Validator.requirePositive(amount, "amount");
            if (currentUser == null) {
                logger.warn("Transfer attempt without authentication");
                throw new AuthException("Please log in first");
            }
            if (recipientLogin.equals(currentUser.getLogin())) {
                logger.warn("Transfer to self rejected: user='{}'", user());
                throw new ValidationException("Cannot transfer to yourself");
            }

            User recipient = auth.find(recipientLogin)
                .orElseThrow(() -> {
                    logger.warn("Transfer rejected: recipient '{}' not found", recipientLogin);
                    return new NotFoundException("Recipient not found: " + recipientLogin);
                });

            String senderCat = "Transfer → " + recipientLogin;
            String recCat = "Transfer ← " + currentUser.getLogin();

            currentUser.getWallet().addExpense(new Expense(senderCat, amount));
            recipient.getWallet().addIncome(new Income(recCat, amount));

            logger.info("Transfer completed: from='{}', to='{}', amount={}",
                user(), recipientLogin, amount);
        } catch (RuntimeException exception) {
            logger.warn("Transfer failed (from='{}', to='{}', amount={}): {}",
                user(), recipientLogin, amount, exception.getMessage());
            throw exception;
        }
    }

    public Map<String, Double> budgetRemaining() {
        return wallet().budgetRemaining();
    }

    public Wallet getWallet() {
        return wallet();
    }
}
