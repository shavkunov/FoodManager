package ru.spbau.mit.foodmanager;


import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class MenuSettings implements Serializable {
    private HashMap<Day, DaySettings> settingsByDay;
    private HashMap<Day, Boolean> isCookingDay;
    private static MenuSettings instance;

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

    /**
     * Сохраняет instance MenuSettings в БД.
     */
    public static String saveMenuSettings(Context context) {
        try {
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
            objectOutputStream.writeObject(instance);
            objectOutputStream.flush();

            String serializedInstance = objectOutputStream.toString();
            CookBookStorage.getInstance(context).saveUserSettings(serializedInstance);
            return serializedInstance;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Загрузка поля instance из БД.
     */
    public static void loadMenuSettings(Context context) {
        String serializedInstance = CookBookStorage.getInstance(context).getUserSettings();
        byte bytes[] = serializedInstance.getBytes();
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteInputStream);
            instance = (MenuSettings) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
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
