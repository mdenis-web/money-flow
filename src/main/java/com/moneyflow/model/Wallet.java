package com.moneyflow.model;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Wallet {
    private List<Income> incomes = new ArrayList<>();
    private List<Expense> expenses = new ArrayList<>();
    private Map<String, Category> categories = new LinkedHashMap<>();

    public void addIncome(Income income) {
        incomes.add(income);
        ensureCategory(income.getCategory());
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
        ensureCategory(expense.getCategory());
    }

    public void addCategory(String name) {
        categories.computeIfAbsent(name, Category::new);
    }

    public void setBudget(String category, double amount) {
        categories.computeIfAbsent(category, Category::new).setBudget(amount);
    }

    public void deleteBudget(String category) {
        Category savedCategory = categories.get(category);
        if (savedCategory != null) {
            savedCategory.setBudget(null);
        }
    }

    public void deleteCategory(String name) {
        categories.remove(name);
    }

    public List<Income> getIncomes() {
        return incomes;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public Map<String, Category> getCategories() {
        return categories;
    }

    public double totalIncome() {
        return incomes.stream().mapToDouble(Income::getAmount).sum();
    }

    public double totalExpense() {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    public double balance() {
        return totalIncome() - totalExpense();
    }

    public Map<String, Double> incomeByCategory() {
        return incomes.stream().collect(Collectors.groupingBy(
            Income::getCategory,
            LinkedHashMap::new,
            Collectors.summingDouble(Income::getAmount)));
    }

    public Map<String, Double> expenseByCategory() {
        return expenses.stream().collect(Collectors.groupingBy(
            Expense::getCategory,
            LinkedHashMap::new,
            Collectors.summingDouble(Expense::getAmount)));
    }

    public double spentIn(String category) {
        return expenses.stream()
            .filter(expense -> expense.getCategory().equals(category))
            .mapToDouble(Expense::getAmount).sum();
    }

    public Map<String, Double> budgetRemaining() {
        Map<String, Double> result = new LinkedHashMap<>();
        for (Category category : categories.values()) {
            if (category.getBudget() != null) {
                result.put(category.getName(), category.getBudget() - spentIn(category.getName()));
            }
        }
        return result;
    }

    public Double getBudget(String category) {
        Category savedCategory = categories.get(category);
        return savedCategory == null ? null : savedCategory.getBudget();
    }

    public List<Expense> expensesBetween(LocalDate from, LocalDate to) {
        return expenses.stream()
            .filter(expense -> !expense.getDate().toLocalDate().isBefore(from)
                && !expense.getDate().toLocalDate().isAfter(to))
            .toList();
    }

    public List<Income> incomesBetween(LocalDate from, LocalDate to) {
        return incomes.stream()
            .filter(income -> !income.getDate().toLocalDate().isBefore(from)
                && !income.getDate().toLocalDate().isAfter(to))
            .toList();
    }

    private void ensureCategory(String name) {
        categories.computeIfAbsent(name, Category::new);
    }

    public class Alert {
        public boolean isOverBudget(String category) {
            Double budget = getBudget(category);
            return budget != null && spentIn(category) > budget;
        }
    }
}
