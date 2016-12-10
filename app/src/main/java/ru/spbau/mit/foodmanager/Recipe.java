package ru.spbau.mit.foodmanager;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

public class Recipe implements Serializable {
    /**
     * Название рецепта.
     */
    private String name;

    /**
     * Его описание.
     */
    private String description;

    /**
     * ID рецепта.
     */
    private int ID;

    public Recipe(int ID, String description, String name) {
        this.ID = ID;
        this.description = description;
        this.name = name;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}