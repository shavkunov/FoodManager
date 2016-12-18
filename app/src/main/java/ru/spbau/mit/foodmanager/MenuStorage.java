package ru.spbau.mit.foodmanager;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Хранит меню для каждого дня недели
 */
public class MenuStorage implements Serializable {
    private HashMap<Day, DayMenu> dayMenus;
    private static Context context;
    private static MenuStorage instance;
    private static final String menuStorageFilename = "MenuStorage";
    static final private String[] DAY_NAMES = {
            "Понедельник",
            "Вторник",
            "Среда",
            "Четверг",
            "Пятница",
            "Суббота",
            "Воскресенье"};

    private MenuStorage(Context context) {
        //TODO Загружать из и сохранять в БД инстанс
        dayMenus = new HashMap<>();
        MenuStorage.context = context;
    }

    static public String[] getDayNames() {
        return DAY_NAMES;
    }

    static public MenuStorage getInstance(Context context) {
        loadMenuSettings();

        if (instance == null) {
            instance = new MenuStorage(context);
            saveMenuSettings();
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

    public static void saveMenuSettings() {
        try {
            FileOutputStream output = context.openFileOutput(
                    menuStorageFilename, Context.MODE_PRIVATE);

            ObjectOutputStream outputStream = new ObjectOutputStream(output);
            outputStream.writeObject(instance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadMenuSettings() {
        File settings = new File(context.getFilesDir(), menuStorageFilename);
        if (settings.exists()) {
            try {
                FileInputStream input = context.openFileInput(menuStorageFilename);
                ObjectInputStream inputStream = new ObjectInputStream(input);
                instance = (MenuStorage) inputStream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Генерирует меню на один день, учитывая настройки и имеющиеся рецепты
     * @param settings настройки меню на этот день
     * @param recipes Map из CategoryID в RecipeID. Если для какой-то категории есть рецепт,
     * то будет использован именно он
     */
    public DayMenu generateDayMenu(DaySettings settings, HashMap<Integer, Integer> recipes) {
        ArrayList<DayMenu.Mealtime> dishesForDay = new ArrayList<>();
        for (DaySettings.MealtimeSettings mealtimeSettings : settings.getMealtimeSettings()) {
            ArrayList<Integer> mealtimeRecipes = new ArrayList<>();
            for (Integer categoryID : mealtimeSettings.dishesCategories()) {
                if (recipes.get(categoryID) == null) {
                    CookBookStorage cookbook = CookBookStorage.getInstance(context);
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
        MenuSettings settings = MenuSettings.getInstance(context);
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
            //TODO Использовать множество категорий
            //Создаем меню на каждый день
            HashMap<Integer, Integer> categoryRecipes = new HashMap<>();
            for (Day day : cookForDays.get(cookingDay)) {
                DaySettings daySettings = settings.getDaySettings(day);
                dayMenus.put(day, null);
                if (daySettings != null) {
                    DayMenu dayMenu = generateDayMenu(daySettings, categoryRecipes);
                    dayMenus.put(day, dayMenu);
                }
            }
        }
    }
}