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

    public String getTypeName() {
        switch (type.ordinal()) {
            case 0:
                return "гр.";

            case 1:
                return "мл.";

            case 2:
                return "шт.";

            case 3:
                return "ч.л.";
        }
        return null;
    }

    public double getQuantity() {
        return quantity;
    }
}
