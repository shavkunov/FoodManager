package ru.spbau.mit.foodmanager.model;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Настройки пользователя.
 */
public class SettingsStorage {
    SharedPreferences sPref;
    /**
     * Любая настройка пользователя выражается строкой
     * и ее численным значением, который хочет видеть юзер.
     * Т.е. если нравится острое то единица, если не нравится то 0.
     * В дальнейшем можно заменить на систему звезд.
     */
    private HashMap<String, Integer> preferences;

    /**
     * Загрузка из Preferences Android.
     */
    public SettingsStorage() {
        // load from Preferences
    }

    /**
     * Получение информации о предпочтении блюда пользователем.
     * @param dishProperty возможная замена на tag
     * @return true, если пользователю нравится блюдо, false иначе.
     */
    public boolean getUserPreference(String dishProperty) {
        return preferences.get(dishProperty) != 0;
    }

    // эти методы можно реализовать только внутри Activity
    public void savePreferences() {
    }

    public void loadPreferences() {
    }
}