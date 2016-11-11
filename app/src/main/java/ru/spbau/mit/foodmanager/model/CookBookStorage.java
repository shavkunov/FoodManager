package ru.spbau.mit.foodmanager.model;

import java.util.ArrayList;
import java.util.Random;

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
     * По списку тегов получение списка списков рецептов. Для каждого тега -- свой список.
     */
    public static ArrayList<ArrayList<Recipe>> getRecipiesByTags(ArrayList<Tag> tags){
        for (Tag t : tags) {
            // for each tag -- query to data base.
        }

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
}