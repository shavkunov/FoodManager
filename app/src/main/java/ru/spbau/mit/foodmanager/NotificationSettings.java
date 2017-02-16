package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.icu.util.Calendar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
/**
 * Настройки сервера нотификации
 */

public class NotificationSettings implements Serializable {
    private Boolean showCookNotifications;
    private long timeOfDayBeginCookNotifications;
    private long timeOfDayEndCookNotifications;
    private static NotificationSettings instance;
    private static final String notificationSettingsFilename = "NotificationSettings";

    static public NotificationSettings getInstance(Context context) {
        loadNotificationSettings(context);

        if (instance == null) {
            instance = new NotificationSettings();
            saveNotificationSettings(context);
        }

        return instance;
    }

    public static void saveNotificationSettings(Context context) {
        try {
            FileOutputStream output = context.openFileOutput(
                    notificationSettingsFilename, Context.MODE_PRIVATE);

            ObjectOutputStream outputStream = new ObjectOutputStream(output);
            outputStream.writeObject(instance);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void loadNotificationSettings(Context context) {
        File settings = new File(context.getFilesDir(), notificationSettingsFilename);
        if (settings.exists()) {
            try {
                FileInputStream input = context.openFileInput(notificationSettingsFilename);
                ObjectInputStream inputStream = new ObjectInputStream(input);
                instance = (NotificationSettings) inputStream.readObject();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private NotificationSettings() {
        showCookNotifications= true;
        timeOfDayBeginCookNotifications = 0;
        timeOfDayEndCookNotifications = Calendar.getInstance().get(Calendar.MILLISECONDS_IN_DAY);
    }

    public void setShowCookNotifications(Boolean b, Context c) {
        showCookNotifications = b;
        saveNotificationSettings(c);
    }

    public void setTimeOfDayBeginCookNotifications(long timeOfDay, Context c) {
        timeOfDayBeginCookNotifications = timeOfDay % Calendar.MILLISECONDS_IN_DAY;
        saveNotificationSettings(c);
    }

    public void setTimeOfDayBeginCookNotifications(long hour, long minute, Context c) {
        hour = hour % 24;
        minute = minute % 60;
        timeOfDayBeginCookNotifications = TimeUnit.HOURS.toMillis(hour) + TimeUnit.MINUTES.toMillis(minute);
        saveNotificationSettings(c);
    }

    public void setTimeOfDayEndCookNotifications(long timeOfDay, Context c) {
        timeOfDayEndCookNotifications = timeOfDay % Calendar.MILLISECONDS_IN_DAY;
        saveNotificationSettings(c);
    }
    public void setTimeOfDayEndCookNotifications(long hour, long minute, Context c) {
        hour = hour % 24;
        minute = minute % 60;
        timeOfDayEndCookNotifications = TimeUnit.HOURS.toMillis(hour) + TimeUnit.MINUTES.toMillis(minute);
        saveNotificationSettings(c);
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
