package ru.spbau.mit.foodmanager;

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
     * Содержит идентификаторы категорий, к которым принадлежит блюдо.
     */
    private ArrayList<Integer> categoryID;

    /**
     * Пошаговая инструкция готовки блюда.
     */
    private ArrayList<Step> steps;

    /**
     * ID рецепта.
     */
    private int ID;

    /**
     * Ингредиенты необходимые для приготовления блюда.
     */
    private ArrayList<Ingredient> ingredients;

    /**
     * Возвращает пошаговую инструкцию готовки блюда.
     */
    public ArrayList<Step> getStepByStep() {
        return steps;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setStepByStep(ArrayList<Step> steps) {
        this.steps = steps;
    }

    Recipe() {}

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Integer> getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(ArrayList<Integer> categoryID) {
        this.categoryID = categoryID;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }
}