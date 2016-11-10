package ru.spbau.mit.foodmanager.model;

import java.util.List;

enum Day {Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday}

/**
 * Реализация хранилища меню.
 */
public class MenuStorage {
    public class Menu {
        private List<List<Recipe>> listDishes;
    }

    public Menu getDayMenu(Day day) {
        return null;
    }

    public void setDayMenu(Day day, Menu menu) {
    }

    public List<Recipe> getDayMenuList() {
        return null;
    }
}
