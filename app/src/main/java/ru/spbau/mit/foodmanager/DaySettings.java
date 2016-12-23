package ru.spbau.mit.foodmanager;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Настройки меню определенного дня
 */

public class DaySettings implements Serializable {
    private ArrayList<MealtimeSettings> mealtimeSettings = new ArrayList<>();
    private static Context context;
    static private ArrayList<MealtimeSettings> presets;
    private static final String presetsFilename = "Presets";
    static {
        presets = new ArrayList<>();
        {
            ArrayList<Integer> categories = new ArrayList<>();
            categories.add(10);
            presets.add(new MealtimeSettings("Завтрак", categories));
        }
        {
            ArrayList<Integer> categories = new ArrayList<>();
            categories.add(9);
            //categories.add(1);
            //categories.add(0);
            presets.add(new MealtimeSettings("Обед", categories));
        }
        {
            ArrayList<Integer> categories = new ArrayList<>();
            categories.add(8);
            presets.add(new MealtimeSettings("Ужин", categories));
        }

        loadPresets();
    }

    public static void savePresets() {
        try {
            FileOutputStream output = context.openFileOutput(
                    presetsFilename, Context.MODE_PRIVATE);

            ObjectOutputStream outputStream = new ObjectOutputStream(output);
            outputStream.writeObject(presets);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadPresets() {
        File settings = new File(context.getFilesDir(), presetsFilename);
        if (settings.exists()) {
            try {
                FileInputStream input = context.openFileInput(presetsFilename);
                ObjectInputStream inputStream = new ObjectInputStream(input);
                presets = (ArrayList<MealtimeSettings>) inputStream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static public ArrayList<MealtimeSettings> getMealtimePresets() {
        return presets;
    }

    public DaySettings(ArrayList<MealtimeSettings> settings, Context context) {
        this.context = context;
        mealtimeSettings = new ArrayList<>();
        for (MealtimeSettings s : settings) {
            mealtimeSettings.add(new MealtimeSettings(s.getName(), s.dishesCategories()));
        }
    }

    public ArrayList<MealtimeSettings> getMealtimeSettings() {
        return mealtimeSettings;
    }

    public ArrayList<Integer> getCategoryIDs() {
        ArrayList<Integer> result = new ArrayList<>();
        for (MealtimeSettings settigs : mealtimeSettings) {
            result.addAll(settigs.dishesCategories());
        }
        return result;
    }

    public static class MealtimeSettings implements Serializable {
        private String name;
        //TODO: Использовать объединение ID категорий
        private ArrayList<Integer> categories;

        public MealtimeSettings(String name, ArrayList<Integer> settings) {
            this.name = name;
            this.categories = new ArrayList<>(settings);
        }

        public void clone(MealtimeSettings settings) {
            this.name = settings.name;
            this.categories = settings.categories;
        }

        public String getName() {
            return name;
        }

        public ArrayList<Integer> dishesCategories() {
            return categories;
        }
    }
}
