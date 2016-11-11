package ru.spbau.mit.foodmanager.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Список покупок.
 */
public class ShoppingListStorage {
    /**
     * Список ингредиентов.
     */
    private ArrayList<Recipe.Ingredient> ingredients;

    /**
     * "Галочки". Если купили уже что-то в магазине -- отмечаем это.
     */
    private ArrayList<Boolean> isChecked;

    /**
     * Загрузка уже существующего списка.
     */
    public ShoppingListStorage() {
        //load from file
    }

    /**
     * Создание списка от меню.
     */
    public ShoppingListStorage(ArrayList<ArrayList<Recipe>> menu) {
        // TODO
    }

    /**
     * Группировка ингредиентов по типу. Овощи, Фрукты и тд.
     * @return списки групп.
     */
    public ArrayList<ArrayList<Recipe.Ingredient>> groupByType() {
        return null;
    }

    /**
     * Подсчет примерного веса всех покупок.
     */
    public Double approximateWeight() {
        return 0.0;
    }

    /**
     * Подсчет примерной стоимости всех покупок.
     */
    public Double approximateCostOf() {
        return 0.0;
    }
}