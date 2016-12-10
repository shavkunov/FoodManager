package ru.spbau.mit.foodmanager;


import java.util.HashMap;

public class MenuSettings {
    private HashMap<Day, DaySettings> settingsByDay;
    private HashMap<Day, Boolean> isCookingDay;
    static final private MenuSettings instance = new MenuSettings();

    private MenuSettings() {
        //TODO: Загружать и сохранять в БД
        settingsByDay = new HashMap<>();
        isCookingDay = new HashMap<>();
        for (Day d : Day.values()) {
            isCookingDay.put(d, true);
        }
    }

    static public MenuSettings getInstance() {
        return instance;
    }

    public DaySettings getDaySettings(Day day) {
        return settingsByDay.get(day);
    }

    public HashMap<Day, DaySettings> getSettingsForAllDays() {
        return settingsByDay;
    }

    public void setDaySettings(Day d, DaySettings settings) {
        //TODO обновлять БД
        settingsByDay.put(d, settings);
    }
}
