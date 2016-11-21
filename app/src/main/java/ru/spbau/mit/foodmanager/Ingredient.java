package ru.spbau.mit.foodmanager;

public class Ingredient {
    private String name;
    private Measure type;
    private double quantity;

    public Ingredient(String name, Measure type, double amount) {
        this.name = name;
        this.type = type;
        this.quantity = amount;
    }

    public String getName() {
        return name;
    }

    public Measure getType() {
        return type;
    }

    public double getQuantity() {
        return quantity;
    }
}
