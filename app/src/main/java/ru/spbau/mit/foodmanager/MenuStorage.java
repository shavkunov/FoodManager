package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Реализация хранилища меню. Кроме названий методов здесь ничего не читать!
 * Олег, настало твое время.
 */
public class MenuStorage {
    /**
     * Список рецептов на неделю.
     */
    private ArrayList<ArrayList<Recipe>> listDishes;
    private CookBookStorage cookbook;

    MenuStorage(CookBookStorage cookbook) {
        listDishes = null;
        this.cookbook = cookbook;
    }

    /**
     * TODO
     */
    private void generateDayMenu(Day day) {
        HashMap<Recipe, Integer> m = new HashMap<>();

        // TODO Implementation
    }

    /**
     * @return возвращает меню. i-ая запись в списке соответствует списку рецептов на i-ый день.
     * Внимание: запись в списке может означать null, это значит, что нет меню на этот день.
     * Сделано это для того, чтобы мы могли создавать меню, например, только на один день.
     */
    public ArrayList<ArrayList<Recipe>> getMenu() {
        setNewWeekMenu();
        return listDishes;
    }

    /**
     * Используется ТОЛЬКО в случае, если пользователя не устраивает конкретный день в меню.
     * @param day метод заново создает меню для этого дня.
     */
    public void setNewDayMenu(Day day) {
        generateDayMenu(day);
    }

    /**
     * Создание меню на неделю.
     * TODO
     */
    public void setNewWeekMenu() {
        // TODO : Implementation
    }

    /**
     * @param day создание меню только для одного дня на неделе.
     */
    public void setOnlyOneDayMenu(Day day) {
        for (Day d : Day.values()) {
            if (d == day) {
                generateDayMenu(d);
            } else {
                listDishes.set(d.ordinal(), null);
            }
        }
    }
}