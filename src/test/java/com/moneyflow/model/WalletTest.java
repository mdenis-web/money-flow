package com.moneyflow.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {
    @Test
    void totalsAndBalance() {
        Wallet wallet = new Wallet();
        wallet.addIncome(new Income("Salary", 1000));
        wallet.addExpense(new Expense("Food", 300));
        assertEquals(1000, wallet.totalIncome());
        assertEquals(300, wallet.totalExpense());
        assertEquals(700, wallet.balance());
    }

    @Test
    void budgetRemaining() {
        Wallet wallet = new Wallet();
        wallet.setBudget("Food", 500);
        wallet.addExpense(new Expense("Food", 300));
        assertEquals(200, wallet.budgetRemaining().get("Food"));
    }

    @Test
    void overBudget() {
        Wallet wallet = new Wallet();
        wallet.setBudget("Food", 100);
        wallet.addExpense(new Expense("Food", 150));
        assertTrue(wallet.new Alert().isOverBudget("Food"));
    }

    @Test
    void expenseByCategory() {
        Wallet wallet = new Wallet();
        wallet.addExpense(new Expense("A", 10));
        wallet.addExpense(new Expense("A", 20));
        wallet.addExpense(new Expense("B", 5));
        assertEquals(30, wallet.expenseByCategory().get("A"));
    }

    @Test
    void transferBetweenUsers() {
        Wallet walletA = new Wallet(), walletB = new Wallet();
        walletA.addIncome(new Income("Salary", 1000));
        walletA.addExpense(new Expense("transfer", 200));
        walletB.addIncome(new Income("transfer", 200));
        assertEquals(800, walletA.balance());
        assertEquals(200, walletB.balance());
    }
}
