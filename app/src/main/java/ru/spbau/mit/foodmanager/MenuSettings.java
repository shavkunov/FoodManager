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

public class MenuSettings implements Serializable {
    private HashMap<Day, DaySettings> settingsByDay;
    private HashMap<Day, Boolean> isCookingDay;
    private Context context;
    private static MenuSettings instance;
    private static final String menuSettingsFilename = "MenuSettings";

    private MenuSettings(Context context) {
        this.context = context;
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

    public void saveMenuSettings() {
        File settings = new File(context.getFilesDir(), menuSettingsFilename);
        try {
            FileOutputStream output = context.openFileOutput(
                                      menuSettingsFilename, Context.MODE_PRIVATE);

            ObjectOutputStream outputStream = new ObjectOutputStream(output);
            outputStream.writeObject(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMenuSettings() {
        File settings = new File(context.getFilesDir(), menuSettingsFilename);
        if (settings.exists()) {
            try {
                FileInputStream input = context.openFileInput(menuSettingsFilename);
                ObjectInputStream inputStream = new ObjectInputStream(input);
                instance = (MenuSettings) inputStream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public MenuSettings getInstance(Context context) {
        loadMenuSettings();

        if (instance == null) {
            instance = new MenuSettings(context);
            saveMenuSettings();
        }

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
