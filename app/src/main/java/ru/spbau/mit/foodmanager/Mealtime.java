package ru.spbau.mit.foodmanager;

import java.util.ArrayList;

/**
 * Описывает один прием пищи
 */
public class Mealtime {
    private ArrayList<Integer> recipeIDs;
    private String name;
    //Нужно ли время?

    /**
     * Задает название и блюда приема пищи
     */
    public Mealtime(String name, ArrayList<Integer> recipes) {
        this.name = name;
        this.recipeIDs = recipes;
    }

    /**
     * Возвращает имя приема пищи
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает список ID блюд приема пищи
     */
    public ArrayList<Integer> getRecipeIDs() {
        return recipeIDs;
    }
}
