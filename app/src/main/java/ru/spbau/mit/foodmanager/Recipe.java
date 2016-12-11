package ru.spbau.mit.foodmanager;

import android.graphics.Bitmap;

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

    /** Пошаговая инструкция готовки блюда */
    private ArrayList<Step> steps;

    /** Ингредиенты необходимые для приготовления блюда */
    private ArrayList<Ingredient> ingredients;

    public Recipe(int ID, String description, String name) {
        this.ID = ID;
        this.description = description;
        this.name = name;
        this.steps = null;
        this.ingredients = null;
        this.categoryID = null;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setCategoryID(ArrayList<Integer> categoryID) {
        this.categoryID = categoryID;
    }

    public ArrayList<Integer> getCategoryID() {
        return categoryID;
    }

    public void setSteps(ArrayList<Step> steps) {
        this.steps = steps;
    }

    public ArrayList<Step> getSteps() {
        return steps;
    }
}