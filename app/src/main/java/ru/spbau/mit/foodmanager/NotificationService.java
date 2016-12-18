package ru.spbau.mit.foodmanager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.concurrent.TimeUnit;

/**
 * Сервис нотифицирования пользователя
 */
public class NotificationService extends Service {
    private static final int COOK_NOTIFICATION_ID = 0;
    private static final String COOK_NOTIFICATION_TITLE = "Время начинать готовить";
    private static final String COOK_NOTIFICATION_TEXT = "Сейчас самое время приготовить что-нибудь вкусное!";
    private NotificationManager notificationManager;
    private static Day lastCookDayNotify;
    private static Boolean notifyedToday;
    private Calendar calendar = Calendar.getInstance();
    private MenuSettings menuSettings = MenuSettings.getInstance(this);
    private NotificationSettings notificationSettings = NotificationSettings.getInstance(this);

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        while (true) {
            try {
                while (true) {
                    TimeUnit.SECONDS.sleep(1);
                    if (calendarDayToDay(calendar.get(Calendar.DAY_OF_WEEK)) != lastCookDayNotify) {
                        notifyedToday = false;
                    }
                    if (notificationSettings.getShowCookNotifications() && !notifyedToday
                            && notificationSettings.getShowCookNotifications()) {
                        sendCookNotification();
                    }
                }
            } catch (Exception e) {
                //Finish Service //Nope
            }
        }
        //return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Проверяет, нужно ли послать оповещение о времени готовить или нет
     */
    private void sendCookNotification() {
        long timeOfDay = System.currentTimeMillis() % calendar.get(Calendar.MILLISECONDS_IN_DAY);
        long notificationTimeBegin = notificationSettings.getTimeOfDayBeginCookNotifications();
        long notificationTimeEnd = notificationSettings.getTimeOfDayEndCookNotifications();
        Day day = calendarDayToDay(calendar.get(Calendar.DAY_OF_WEEK));
        if (timeOfDay > notificationTimeBegin &&
                timeOfDay < notificationTimeEnd &&
                menuSettings.isCookingDay(day)) {
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, COOK_NOTIFICATION_ID, intent, 0);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
            notificationBuilder.setContentTitle(COOK_NOTIFICATION_TITLE);
            notificationBuilder.setContentText(COOK_NOTIFICATION_TEXT);
            notificationBuilder.setContentIntent(pendingIntent);
            notificationManager.notify(COOK_NOTIFICATION_ID, notificationBuilder.build());
            lastCookDayNotify = day; //TODO делать нормальную проверку, т.к. если только один день  готовки, то все плохо.
            notifyedToday = true;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Day calendarDayToDay (int calendarDay) {
        switch (calendarDay) {
            case Calendar.MONDAY :
                return Day.Monday;
            case Calendar.TUESDAY:
                return Day.Tuesday;
            case Calendar.WEDNESDAY :
                return Day.Wednesday;
            case Calendar.THURSDAY :
                return Day.Thursday;
            case Calendar.FRIDAY :
                return Day.Friday;
            case Calendar.SATURDAY :
                return Day.Saturday;
            case Calendar.SUNDAY :
                return Day.Sunday;
        }
        return null;
    }

}
