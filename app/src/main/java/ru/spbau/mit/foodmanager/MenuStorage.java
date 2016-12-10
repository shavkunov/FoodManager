package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Хранит меню для каждого дня недели
 */
public class MenuStorage {
    private HashMap<Day, DayMenu> dayMenus;
    private CookBookStorage cookbook;
    static private MenuStorage instance;

    private MenuStorage(CookBookStorage cookbook) {
        dayMenus = new HashMap<>();
        this.cookbook = cookbook;
    }

    static public MenuStorage getInstance(Context context) {
        if (instance == null) {
            instance = new MenuStorage(CookBookStorage.getInstance(context));
        }
        return instance;
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
     * Генерирует меню на один день, учитывая настройки и имеющиеся рецепты
     * @param settings настройки меню на этот день
     * @param recipes Map из CategoryID в RecipeID. Если для какой-то категории есть рецепт, то будет использован именно он
     */
    public DayMenu generateDayMenu(DaySettings settings, HashMap<Integer, Integer> recipes) {
        ArrayList<DayMenu.Mealtime> dishesForDay = new ArrayList<>();
        for (DaySettings.MealtimeSettings mealtimeSettings : settings.getMealtimeSettings()) {
            ArrayList<Integer> mealtimeRecipes = new ArrayList<>();
            for (Integer categoryID : mealtimeSettings.dishesCategories()) {
                if (recipes.get(categoryID) == null) {
                    recipes.put(categoryID, cookbook.chooseRandomDishFromCategory(categoryID).getID());
                }
                mealtimeRecipes.add(recipes.get(categoryID));
            }
            dishesForDay.add(new DayMenu.Mealtime(mealtimeSettings.getName(), mealtimeRecipes));
        }
        return new DayMenu(dishesForDay);
    }

    /**
     * Генерирует меню на неделю
     */
    public void generateWeekMenu() {
        MenuSettings settings = MenuSettings.getInstance();
        Day firstCookingDay = Day.Monday;
        for (Day day : Day.values()) {
            if (settings.isCookingDay(day)) {
                firstCookingDay = day;
                break;
            }
        }
        if (!settings.isCookingDay(firstCookingDay)) {
            //TODO обработать случай, когда нет дней готовки.
        }
        //Получаем для каждого дня готовки список дней, на которые готовим.
        HashMap<Day, ArrayList<Day>> cookForDays = new HashMap<>();
        Day currentCookingDay = firstCookingDay;
        for (Integer day = firstCookingDay.ordinal(); day < Day.values().length; day++) {
            if (settings.isCookingDay(Day.values()[day])) {
                currentCookingDay = Day.values()[day];
                cookForDays.put(currentCookingDay, new ArrayList<Day>());
            }
            cookForDays.get(currentCookingDay).add(Day.values()[day]);
        }
        for (Integer day = 0; day < firstCookingDay.ordinal(); day++) {
            cookForDays.get(currentCookingDay).add(Day.values()[day]);
        }
        //Получаем рецепты для каждого дня готовки
        for (Day cookingDay : cookForDays.keySet()) {
            Log.d("CookingDay:", cookingDay.toString());
            //TODO Использовать множество категорий
            //Создаем меню на каждый день
            HashMap<Integer, Integer> categoryRecipes = new HashMap<>();
            for (Day day : cookForDays.get(cookingDay)) {
                Log.d("Day:", cookingDay.toString());
                DaySettings daySettings = settings.getDaySettings(day);
                dayMenus.put(day, null);
                if (daySettings != null) {
                    DayMenu dayMenu = generateDayMenu(daySettings, categoryRecipes);
                    Log.d("DayMenu:", dayMenu.toString());
                    dayMenus.put(day, dayMenu);
                }
            }
        }
        for (Day day : Day.values()) {
            Log.d("Generate result:", day.toString() + " " + dayMenus.get(day));
        }
    }
}