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
        switch (type) {
            case gr:
                return "гр.";

            case ml:
                return "мл.";

            case apiece:
                return "шт.";

            case teaspoon:
                return "ч.л.";

            case tablespoon:
                return "ст.л.";

            case pinch:
                return "щепотка";

            case byTaste:
                return "по вкусу";

            case cloves:
                return "зуб.";
        }
        return null;
    }

    public Measure getMeasure() {
        return type;
    }

    public double getQuantity() {
        return quantity;
    }
}
