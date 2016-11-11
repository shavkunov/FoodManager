package ru.spbau.mit.foodmanager.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import static ru.spbau.mit.foodmanager.model.CategoryName.*;

enum CategoryName {firstDish, secondDish, salad, appetizer, baking, beverage, soup, pancake,
    dessert, barbeque, porridge,
    dinner, lunch, breakfast,
    European, American, Italian, Uzbek, Georgian, Indian, Caucasian, Russian, Asian}
    // and others tags

/**
 * Хранилище всех рецептов.
 */
public class CookBookStorage {
    /**
     * Загрузка базы данных.
     */
    public CookBookStorage() {
        // load Data Base
    }

    /**
     * Получение рецепта по его уникальному идентификатору.
     */
    public Recipe getRecipe(int ID) {
        // query to data base
        return null;
    }

    /**
     * Получение списка рецептов по фильтру, т.е. по префиксу.
     */
    public ArrayList<Recipe> getRecipiesByFilter(String filter) {
        // query to data base
        return null;
    }

    /**
     * Выбор случайного ужина из базы данных.
     */
    public static Recipe chooseRandomDinner() {
        Random r = new Random();
        // запрос на r-ую строку по тегу dinner, т.е. r-ый рецепт, в базе данных.

        Recipe res = null;
        return null;
    }

    /**
     * Выбор случайного обеда из базы данных.
     */
    public static Recipe chooseRandomLunch() {
        Random r = new Random();
        // запрос на r-ую строку по тегу Lunch, т.е. r-ый рецепт, в базе данных.

        Recipe res = null;
        return null;
    }

    /**
     * Выбор случайного завтрака из базы данных.
     */
    public static Recipe chooseRandomBreakfast() {
        Random r = new Random();
        // запрос на r-ую строку по тегу Breakfast, т.е. r-ый рецепт, в базе данных.

        Recipe res = null;
        return null;
    }

    public static ArrayList<Recipe> getRecipesOfCategory(int ID) {
        ArrayList<Recipe> res = null;
        // запрос в базу данных для блюд у которых есть ID

        return res;
    }

    public static LinkedList<Category> getRecipiesTypeOfDish() {
        LinkedList<Category> categories = new LinkedList<>();

        for (int order = firstDish.ordinal(); order < dinner.ordinal(); order++) {
            categories.add(new Category(order));
        }

        return categories;
    }

    public static LinkedList<Category> getRecipiesNationalKitchen() {
        LinkedList<Category> categories = new LinkedList<>();

        for (int order = European.ordinal(); order < CategoryName.values().length; order++) {
            categories.add(new Category(order));
        }

        return categories;
    }
}