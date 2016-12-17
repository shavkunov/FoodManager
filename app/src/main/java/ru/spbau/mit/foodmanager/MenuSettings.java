package ru.spbau.mit.foodmanager;


import java.util.ArrayList;
import java.util.HashMap;

public class MenuSettings {
    private HashMap<Day, DaySettings> settingsByDay;
    private HashMap<Day, Boolean> isCookingDay;
    static final private MenuSettings instance = new MenuSettings();

    private MenuSettings() {
        //TODO: Загружать из и сохранять в БД инстанс
        settingsByDay = new HashMap<>();
        isCookingDay = new HashMap<>();
        for (Day d : Day.values()) {
            isCookingDay.put(d, true);
            //DEBUG ONLY
            ArrayList<DaySettings.MealtimeSettings> mealtieSettings = new ArrayList<>();
            mealtieSettings.add(DaySettings.getMealtimePresets().get(0));
            mealtieSettings.add(DaySettings.getMealtimePresets().get(1));
            mealtieSettings.add(DaySettings.getMealtimePresets().get(2));
            DaySettings settings = new DaySettings(mealtieSettings);
            settingsByDay.put(d, settings);
            //DEBUG ONLY
        }
    }

    static public MenuSettings getInstance() {
        return instance;
    }

    public DaySettings getDaySettings(Day day) {
        return settingsByDay.get(day);
    }

    public Boolean isCookingDay(Day day) {
        return isCookingDay.get(day);
    }

    public void setCookingDay(Day day, Boolean isCookingDay) {
        this.isCookingDay.put(day, isCookingDay);
    }
}
