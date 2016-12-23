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
    private static MenuSettings instance;
    private static final String menuSettingsFilename = "MenuSettings";

    private MenuSettings(Context context) {
        settingsByDay = new HashMap<>();
        isCookingDay = new HashMap<>();
        for (Day d : Day.values()) {
            isCookingDay.put(d, true);
            //DEBUG ONLY
            ArrayList<DaySettings.MealtimeSettings> mealtieSettings = new ArrayList<>();
            mealtieSettings.add(DaySettings.getMealtimePresets(context).get(0));
            mealtieSettings.add(DaySettings.getMealtimePresets(context).get(1));
            mealtieSettings.add(DaySettings.getMealtimePresets(context).get(2));
            DaySettings settings = new DaySettings(mealtieSettings);
            settingsByDay.put(d, settings);
            //DEBUG ONLY
        }
    }

    public static void saveMenuSettings(Context context) {
        try {
            FileOutputStream output = context.openFileOutput(
                                      menuSettingsFilename, Context.MODE_PRIVATE);

            ObjectOutputStream outputStream = new ObjectOutputStream(output);
            outputStream.writeObject(instance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadMenuSettings(Context context) {
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

    public static MenuSettings getInstance(Context context) {
        loadMenuSettings(context);

        if (instance == null) {
            instance = new MenuSettings(context);
            saveMenuSettings(context);
        }

        return instance;
    }

    public DaySettings getDaySettings(Day day) {
        return settingsByDay.get(day);
    }

    public Boolean isCookingDay(Day day) {
        return isCookingDay.get(day);
    }

    public void setCookingDay(Day day, Boolean isCookingDay, Context c) {
        this.isCookingDay.put(day, isCookingDay);
        saveMenuSettings(c);
    }
}
