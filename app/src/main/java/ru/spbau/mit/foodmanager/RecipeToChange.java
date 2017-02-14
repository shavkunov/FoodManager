package ru.spbau.mit.foodmanager;

import java.util.ArrayList;

/**
 * Класс, созданный специально для добавления и удаления рецепта в БД.
 */
public class RecipeToChange {
    /** Название рецепта */
    private String name;

    /** Описание рецепта */
    private String description;

    /** ID добавляемого рецепта */
    private int ID;

    /** Содержит идентификаторы категорий, к которым принадлежит блюдо */
    private ArrayList<Integer> categoryID;

    /** Пошаговая инструкция готовки блюда */
    private ArrayList<Step> steps;

    /** Ингредиенты необходимые для приготовления блюда */
    private ArrayList<Ingredient> ingredients;

    public RecipeToChange(int ID, String description, String name) {
        categoryID = null;
        steps = null;
        ingredients = null;
        this.ID = ID;
        this.description = description;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) { this.description = description; }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
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
