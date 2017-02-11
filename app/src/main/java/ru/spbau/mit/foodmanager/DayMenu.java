package ru.spbau.mit.foodmanager;

import java.util.ArrayList;

/**
 * Описывет меню на день.
 */

public class DayMenu {
    private ArrayList<Mealtime> mealtimes;

    /**
     * Просто сохраняет список Mealtime
     */
    public DayMenu(ArrayList<Mealtime> dishes) {
        mealtimes = dishes;
    }

    /**
     * Возвращает сохраненный список Mealtime
     */
    public ArrayList<Mealtime> getMealtimes() {
        return mealtimes;
    }

    /**
     * Возвращает список всех рецептов из Mealtime
     */
    public ArrayList<Integer> getDishes() {
        ArrayList<Integer> result = new ArrayList<>();
        for (Mealtime m : mealtimes) {
            result.addAll(m.getRecipeIDs());
        }
        return result;
    }

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

}
