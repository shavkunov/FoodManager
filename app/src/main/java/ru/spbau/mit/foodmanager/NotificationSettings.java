package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.icu.util.Calendar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;
/**
 * Настройки сервера нотификации
 */

public class NotificationSettings {
    private Boolean showCookNotifications;
    private long timeOfDayBeginCookNotifications;
    private long timeOfDayEndCookNotifications;
    private static Context context;
    private static NotificationSettings instance;
    private static final String notificationSettingsFilename = "NotificationSettings";

    static public NotificationSettings getInstance(Context context) {
        loadNotificationSettings();

        if (instance == null) {
            instance = new NotificationSettings(context);
            saveNotificationSettings();
        }

        return instance;
    }

    public static void saveNotificationSettings() {
        try {
            FileOutputStream output = context.openFileOutput(
                    notificationSettingsFilename, Context.MODE_PRIVATE);

            ObjectOutputStream outputStream = new ObjectOutputStream(output);
            outputStream.writeObject(instance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadNotificationSettings() {
        File settings = new File(context.getFilesDir(), notificationSettingsFilename);
        if (settings.exists()) {
            try {
                FileInputStream input = context.openFileInput(notificationSettingsFilename);
                ObjectInputStream inputStream = new ObjectInputStream(input);
                instance = (NotificationSettings) inputStream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private NotificationSettings(Context context) {
        NotificationSettings.context = context;
        showCookNotifications= true;
        timeOfDayBeginCookNotifications = 0;
        timeOfDayEndCookNotifications = Calendar.getInstance().get(Calendar.MILLISECONDS_IN_DAY);
    }

    public void setShowCookNotifications(Boolean b) {
        showCookNotifications = b;
        saveNotificationSettings();
    }

    public void setTimeOfDayBeginCookNotifications(long timeOfDay) {
        timeOfDayBeginCookNotifications = timeOfDay % Calendar.MILLISECONDS_IN_DAY;
        saveNotificationSettings();
    }

    public void setTimeOfDayBeginCookNotifications(long hour, long minute) {
        hour = hour % 24;
        minute = minute % 60;
        timeOfDayBeginCookNotifications = TimeUnit.HOURS.toMillis(hour) + TimeUnit.MINUTES.toMillis(minute);
        saveNotificationSettings();
    }

    public void setTimeOfDayEndCookNotifications(long timeOfDay) {
        timeOfDayEndCookNotifications = timeOfDay % Calendar.MILLISECONDS_IN_DAY;
        saveNotificationSettings();
    }
    public void setTimeOfDayEndCookNotifications(long hour, long minute) {
        hour = hour % 24;
        minute = minute % 60;
        timeOfDayEndCookNotifications = TimeUnit.HOURS.toMillis(hour) + TimeUnit.MINUTES.toMillis(minute);
        saveNotificationSettings();
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
