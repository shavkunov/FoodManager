package ru.spbau.mit.foodmanager;

import java.io.Serializable;
import java.util.ArrayList;

public class Recipe implements Serializable {
    /** Название рецепта */
    private String name;

    /** Описание рецепта */
    private String description;

    /** ID рецепта */
    private int ID;

    /** Содержит идентификаторы категорий, к которым принадлежит блюдо */
    private ArrayList<Integer> categoryID;

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

    public void setCategoryID(ArrayList<Integer> categoryID) {
        this.categoryID = categoryID;
    }

    public ArrayList<Integer> getCategoryID() {
        return categoryID;
    }
}