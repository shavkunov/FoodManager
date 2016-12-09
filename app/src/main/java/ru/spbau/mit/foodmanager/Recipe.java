package ru.spbau.mit.foodmanager;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

public class Recipe {
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