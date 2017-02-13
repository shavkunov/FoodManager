package ru.spbau.mit.foodmanager;

import java.util.ArrayList;

public class RecipeToInsert {
    /** ID добавляемого рецепта */
    private int ID;

    /** Содержит идентификаторы категорий, к которым принадлежит блюдо */
    private ArrayList<Integer> categoryID;

    /** Пошаговая инструкция готовки блюда */
    private ArrayList<Step> steps;

    /** Ингредиенты необходимые для приготовления блюда */
    private ArrayList<Ingredient> ingredients;

    public RecipeToInsert(int ID) {
        categoryID = null;
        steps = null;
        ingredients = null;
        this.ID = ID;
    }

    public int getID() {
        return ID;
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
