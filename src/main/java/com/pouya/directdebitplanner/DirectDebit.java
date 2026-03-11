package com.pouya.directdebitplanner;

public class DirectDebit {

    private int id;
    private String name;
    private double amount;
    private String dueDate;
    private String category;
    private String notes;

    public DirectDebit(int id, String name, double amount, String dueDate, String category, String notes) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.dueDate = dueDate;
        this.category = category;
        this.notes = notes;
    }

    public DirectDebit(String name, double amount, String dueDate, String category, String notes) {
        this.name = name;
        this.amount = amount;
        this.dueDate = dueDate;
        this.category = category;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getCategory() {
        return category;
    }

    public String getNotes() {
        return notes;
    }
}