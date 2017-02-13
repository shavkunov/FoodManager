package ru.spbau.mit.foodmanager;

import android.content.Intent;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Описывет меню на день.
 */

public class DayMenu implements Serializable{
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
}
