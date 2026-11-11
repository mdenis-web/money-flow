package com.moneyflow.model;

public class Category {
    private String name;
    private Double budget;

    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, Double budget) {
        this.name = name;
        this.budget = budget;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }
}
