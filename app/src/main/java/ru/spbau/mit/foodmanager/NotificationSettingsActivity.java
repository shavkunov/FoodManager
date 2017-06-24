package ru.spbau.mit.foodmanager;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

public class NotificationSettingsActivity extends AppCompatActivity {
    private NotificationSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_settings);

        settings = NotificationSettings.getInstance(this);
        Switch switchCookNotify = (Switch) findViewById(R.id.notification_settings_toggle_cook_notify);
        showTimes();
        switchCookNotify.setChecked(settings.getShowCookNotifications());
        switchCookNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settings.setShowCookNotifications(b, NotificationSettingsActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationSettings.saveNotificationSettings(this);
    }

    private void showTimes() {
        TextView cookNotifyFromHours = (TextView) findViewById(R.id.notification_settings_time_cook_notify_from_hours);
        TextView cookNotifyFromMinutes = (TextView) findViewById(R.id.notification_settings_time_cook_notify_from_minutes);
        TextView cookNotifyToHours = (TextView) findViewById(R.id.notification_settings_time_cook_notify_to_hours);
        TextView cookNotifyToMinutes = (TextView) findViewById(R.id.notification_settings_time_cook_notify_to_minutes);
        String s;
        s = ((Long)settings.getHourOfDayBeginCookNotifications()).toString();
        if (s.length() < 2) {
            s = "0".concat(s);
        }
        cookNotifyFromHours.setText(s);
        s = ((Long)settings.getHourOfDayEndCookNotifications()).toString();
        if (s.length() < 2) {
            s = "0".concat(s);
        }
        cookNotifyToHours.setText(s);
        s = ((Long)settings.getMinuteOfDayBeginCookNotifications()).toString();
        if (s.length() < 2) {
            s = "0".concat(s);
        }
        cookNotifyFromMinutes.setText(s);
        s = ((Long)settings.getMinuteOfDayEndCookNotifications()).toString();
        if (s.length() < 2) {
            s = "0".concat(s);
        }
        cookNotifyToMinutes.setText(s);
    }

    public void onCookNotifyTimeFromEditClick(View v) {
        //final TextView cookNotifyFromHours = (TextView) findViewById(R.id.notification_settings_time_cook_notify_from_hours);
        //final TextView cookNotifyFromMinutes = (TextView) findViewById(R.id.notification_settings_time_cook_notify_from_minutes);
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                    settings.setTimeOfDayBeginCookNotifications(hour, minute, NotificationSettingsActivity.this);
                    showTimes();
            }
        },
                (int)settings.getHourOfDayBeginCookNotifications(),
                (int)settings.getMinuteOfDayBeginCookNotifications(), true);
        dialog.show();
    }

    public void onCookNotifyTimeToEditClick(View v) {
        //final TextView cookNotifyToHours = (TextView) findViewById(R.id.notification_settings_time_cook_notify_to_hours);
        //final TextView cookNotifyToMinutes = (TextView) findViewById(R.id.notification_settings_time_cook_notify_to_minutes);
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                settings.setTimeOfDayEndCookNotifications(hour, minute, NotificationSettingsActivity.this);
                showTimes();
            }
        },
                (int)settings.getHourOfDayEndCookNotifications(),
                (int)settings.getMinuteOfDayEndCookNotifications(), true);
        dialog.show();
    }
}
