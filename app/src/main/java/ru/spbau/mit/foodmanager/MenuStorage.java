package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Реализация хранилища меню.
 */
public class MenuStorage {
    /**
     * Список рецептов на неделю.
     */
    private ArrayList<ArrayList<Recipe>> listDishes;
    private CookBookStorage cookbook;

    MenuStorage(CookBookStorage cookbook) {
        listDishes = new ArrayList<>(7);
        this.cookbook = cookbook;
    }

    /**
     * Создание меню для одного дня.
     * Гарантируется, что рецепты не будут совпадать с уже выбранными.
     * Если такая выборка невозможна(если рецептов будет много, то такой проблемы не возникнет)
     * то можно в дальнейшем реализовать с повторениями.
     * @param day день в который нужно сгенерировать меню
     */
    private void generateDayMenu(Day day) {
        HashMap<Recipe, Integer> m = new HashMap<>();

        for (int i = 0; i < listDishes.size(); i++) {
            ArrayList<Recipe> curDay = listDishes.get(i);

            if (curDay != null) {
                for (int j = 0; j < curDay.size(); j++) {
                    Recipe curRecipe = curDay.get(j);
                    m.put(curRecipe, 1);
                }
            }
        }

        Recipe breakfast = cookbook.chooseRandomDishFromCategory(CategoryName.breakfast);
        while (m.get(breakfast) != null) {
            breakfast = cookbook.chooseRandomDishFromCategory(CategoryName.breakfast);
        }

        Recipe lunch = cookbook.chooseRandomDishFromCategory(CategoryName.lunch);
        while (m.get(breakfast) != null) {
            lunch = cookbook.chooseRandomDishFromCategory(CategoryName.lunch);
        }

        Recipe dinner = cookbook.chooseRandomDishFromCategory(CategoryName.dinner);
        while (m.get(breakfast) != null) {
            dinner = cookbook.chooseRandomDishFromCategory(CategoryName.dinner);
        }

        ArrayList<Recipe> dayMenu = new ArrayList<>();
        dayMenu.add(breakfast);
        dayMenu.add(lunch);
        dayMenu.add(dinner);

        listDishes.add(dayMenu);
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
     */
    public void setNewWeekMenu() {
        for (Day day : Day.values()) {
            generateDayMenu(day);
        }
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