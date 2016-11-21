package ru.spbau.mit.foodmanager;

import java.io.Serializable;

public class Ingredient implements Serializable {
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
