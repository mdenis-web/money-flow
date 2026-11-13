package com.moneyflow.service;

import com.moneyflow.exception.NotFoundException;
import com.moneyflow.storage.JsonFileStorage;
import org.junit.jupiter.api.*;

import java.nio.file.*;

class FinanceManagerTest {
    private FinanceManager financeManager;
    private AuthService auth;

    @BeforeEach
    void setup() throws Exception {
        Path path = Files.createTempFile("t", ".json");
        Files.deleteIfExists(path);
        auth = new AuthService(new JsonFileStorage(path));
        financeManager = new FinanceManager(auth);
        auth.register("u1", "pass");
        auth.register("u2", "pass");
        financeManager.login("u1", "pass");
    }

    @Test
    void addIncomeAndExpense() {
        financeManager.addIncome("Salary", 1000);
        financeManager.addExpense("Food", 300);
        Assertions.assertEquals(700, financeManager.getWallet().balance());
    }

    @Test
    void setBudget() {
        financeManager.setBudget("Food", 500);
        Assertions.assertEquals(500, financeManager.getWallet().getBudget("Food"));
    }

    @Test
    void editBudget() {
        financeManager.setBudget("Food", 500);
        financeManager.editBudget("Food", 800);
        Assertions.assertEquals(800, financeManager.getWallet().getBudget("Food"));
    }

    @Test
    void editNonExistingBudgetThrows() {
        Assertions.assertThrows(NotFoundException.class, () -> financeManager.editBudget("xxx", 10));
    }

    @Test
    void transferWorks() {
        financeManager.addIncome("Salary", 1000);
        financeManager.transfer("u2", 300);
        Assertions.assertEquals(700, financeManager.getWallet().balance());
        Assertions.assertEquals(300, auth.find("u2").get().getWallet().balance());
    }

    @Test
    void transferToSelfThrows() {
        financeManager.addIncome("Salary", 1000);
        Assertions.assertThrows(RuntimeException.class, () -> financeManager.transfer("u1", 10));
    }

    @Test
    void transferToUnknownThrows() {
        financeManager.addIncome("Salary", 1000);
        Assertions.assertThrows(NotFoundException.class, () -> financeManager.transfer("ghost", 10));
    }
}
