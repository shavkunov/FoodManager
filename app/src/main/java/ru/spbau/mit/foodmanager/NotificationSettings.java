package ru.spbau.mit.foodmanager;

import android.icu.util.Calendar;

/**
 * Настройки сервера нотификации
 */

public class NotificationSettings {
    private Boolean showCookNotifications;
    private long timeOfDayBeginCookNotifications;
    private long timeOfDayEndCookNotifications;
    private static final NotificationSettings instance = new NotificationSettings();

    static public NotificationSettings getInstance() {
        return instance;
    }

    private NotificationSettings() {
        showCookNotifications= true;
        timeOfDayBeginCookNotifications = 0;
        timeOfDayEndCookNotifications = Calendar.getInstance().get(Calendar.MILLISECONDS_IN_DAY);
        //TODO: Init from file
    }

    public void setShowCookNotifications(Boolean b) {
        showCookNotifications = b;
        //TODO: Save to file
    }

    public void setTimeOfDayBeginCookNotifications(long timeOfDay) {
        timeOfDayBeginCookNotifications = timeOfDay % Calendar.MILLISECONDS_IN_DAY;
        //TODO:Save in file
    }

    public void setTimeOfDayEndCookNotifications(long timeOfDay) {
        timeOfDayEndCookNotifications = timeOfDay % Calendar.MILLISECONDS_IN_DAY;
        //TODO:Save in file
    }

    public Boolean getShowCookNotifications() {
        return showCookNotifications;
    }

    public long getTimeOfDayBeginCookNotifications() {
        return timeOfDayBeginCookNotifications;
    }

    public long getTimeOfDayEndCookNotifications() {
        return timeOfDayEndCookNotifications;
    }
}
