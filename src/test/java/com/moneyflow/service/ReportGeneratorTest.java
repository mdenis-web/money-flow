package com.moneyflow.service;

import com.moneyflow.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {
    private final ReportGenerator reportGenerator = new ReportGenerator();

    @Test
    void fullReportContainsTotals() {
        Wallet wallet = new Wallet();
        wallet.addIncome(new Income("Salary", 60000));
        wallet.addExpense(new Expense("Food", 500));
        String report = reportGenerator.fullReport(wallet);
        assertTrue(report.contains("60,000"));
        assertTrue(report.contains("500"));
    }

    @Test
    void categoryReportForUnknownCategory() {
        Wallet wallet = new Wallet();
        String report = reportGenerator.categoryReport(wallet, "unknown");
        assertTrue(report.contains("0"));
    }

    @Test
    void categoriesReportWarnsUnknown() {
        Wallet wallet = new Wallet();
        wallet.addCategory("A");
        String report = reportGenerator.categoriesReport(wallet, java.util.List.of("A", "B"));
        assertTrue(report.contains("not found"));
    }

    @Test
    void periodReport() {
        Wallet wallet = new Wallet();
        Income oldInc = new Income("A", 100, LocalDate.now().minusDays(10).atStartOfDay());
        Income newInc = new Income("A", 200);
        wallet.addIncome(oldInc);
        wallet.addIncome(newInc);
        String report = reportGenerator.periodReport(wallet, LocalDate.now().minusDays(1), LocalDate.now());
        assertTrue(report.contains("200"));
    }
}
