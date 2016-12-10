package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

public class MenuSettingsActivity extends AppCompatActivity {
    private MenuSettings menuSettings;
    private HashMap<Day, View> daySettingViews;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_settings);
        menuSettings = MenuSettings.getInstance();
        LinearLayout daysSettings = (LinearLayout) findViewById(R.id.menu_settings_days);
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        daySettingViews = new HashMap<>();
        for (Day d : Day.values()) {
            View v = inflater.inflate(R.layout.menu_settings_day, null);
            ((TextView)v.findViewById(R.id.menu_settings_day_name))
                    .setText(MenuStorage.getDayNames()[d.ordinal()]);
            daySettingViews.put(d, v);
            daysSettings.addView(v);
            Log.d("DEBUG", v.toString());
            Log.d("DEBUG", daysSettings.getViewTreeObserver().toString());
        }
        fillDays();
    }

    private void fillDays() {
        for (final Day d : Day.values()) {
            View v = daySettingViews.get(d);
            CheckBox isCookingDay = (CheckBox) v.findViewById(R.id.menu_settings_day_is_cookingday);
            ImageButton addMealtime = (ImageButton) v.findViewById(R.id.menu_settings_day_mealtime_add);
            final LinearLayout mealtimes = (LinearLayout) v.findViewById(R.id.menu_settings_day_mealtimes);

            isCookingDay.setChecked(menuSettings.isCookingDay(d));
            for (final DaySettings.MealtimeSettings settings : menuSettings.getDaySettings(d).getMealtimeSettings()) {
                final View mealtimeView = inflater.inflate(R.layout.menu_settings_day_mealtime, null);
                ImageButton editMealtime = (ImageButton)
                        mealtimeView.findViewById(R.id.menu_settings_day_mealtime_edit);
                ImageButton deleteMealtime = (ImageButton)
                        mealtimeView.findViewById(R.id.menu_settings_day_mealtime_delete);
                TextView mealtimeName = (TextView)
                        mealtimeView.findViewById(R.id.menu_settings_day_mealtime_name);

                mealtimeName.setText(settings.getName());
                deleteMealtime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menuSettings.getDaySettings(d).getMealtimeSettings().remove(settings);
                        mealtimes.removeView(mealtimeView);
                    }
                });

                mealtimes.addView(mealtimeView);
            }

            isCookingDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    menuSettings.setCookingDay(d, b);
                }
            });


        }
    }
}
