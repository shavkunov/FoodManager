package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Хранит меню для каждого дня недели
 */
public class MenuStorage {
    private HashMap<Day, DayMenu> dayMenus;
    private CookBookStorage cookbook;

    public MenuStorage(CookBookStorage cookbook) {
        dayMenus = new HashMap<>();
        this.cookbook = cookbook;
    }

    /**
     * Возвращает HashMap, где ключ - день, значение - DayMenu
     */
    public HashMap<Day, DayMenu> getMenu() {
        return dayMenus;
    }

    /**
     * Записывает новое меню на определенный день
     */
    public void setNewDayMenu(Day day, DayMenu dayMenu) {
        if (day != null) {
            dayMenus.put(day,dayMenu);
        }
    }

    /**
     * Генерирует меню на неделю
     */
    public void generateWeekMenu() {
        //TODO
    }
}