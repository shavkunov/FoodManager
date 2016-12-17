package ru.spbau.mit.foodmanager;

import android.icu.util.Calendar;
import java.util.concurrent.TimeUnit;
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
        //TODO: Сохранять в и загружать из БД
    }

    public void setShowCookNotifications(Boolean b) {
        showCookNotifications = b;
        //TODO: Save to file
    }

    public void setTimeOfDayBeginCookNotifications(long timeOfDay) {
        timeOfDayBeginCookNotifications = timeOfDay % Calendar.MILLISECONDS_IN_DAY;
        //TODO:Save in file
    }

    public void setTimeOfDayBeginCookNotifications(long hour, long minute) {
        hour = hour % 24;
        minute = minute % 60;
        timeOfDayBeginCookNotifications = TimeUnit.HOURS.toMillis(hour) + TimeUnit.MINUTES.toMillis(minute);
        //TODO:Save in file
    }

    public void setTimeOfDayEndCookNotifications(long timeOfDay) {
        timeOfDayEndCookNotifications = timeOfDay % Calendar.MILLISECONDS_IN_DAY;
        //TODO:Save in file
    }
    public void setTimeOfDayEndCookNotifications(long hour, long minute) {
        hour = hour % 24;
        minute = minute % 60;
        timeOfDayEndCookNotifications = TimeUnit.HOURS.toMillis(hour) + TimeUnit.MINUTES.toMillis(minute);
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

    public long getHourOfDayBeginCookNotifications() {
        return TimeUnit.MILLISECONDS.toHours(timeOfDayBeginCookNotifications);
    }

    public long getHourOfDayEndCookNotifications() {
        return TimeUnit.MILLISECONDS.toHours(timeOfDayEndCookNotifications);
    }

    public long getMinuteOfDayBeginCookNotifications() {
        return TimeUnit.MILLISECONDS.toMinutes(timeOfDayBeginCookNotifications) % 60;
    }

    public long getMinuteOfDayEndCookNotifications() {
        return TimeUnit.MILLISECONDS.toMinutes(timeOfDayEndCookNotifications) % 60;
    }
}
