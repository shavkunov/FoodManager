package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class MenuSettingsActivity extends AppCompatActivity {
    private static final int REQUEST_ADD = 0;
    private static final int REQUEST_EDIT = 1;
    private MenuSettings menuSettings;
    private HashMap<Day, View> daySettingViews;
    private HashMap<Day, ArrayList<DaySettings.MealtimeSettings>> mealtimeSettings;
    private HashMap<Day, HashMap<DaySettings.MealtimeSettings, View>> mealtimeSettingsViews;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_settings);
        menuSettings = MenuSettings.getInstance(this);
        LinearLayout daysSettings = (LinearLayout) findViewById(R.id.menu_settings_days);
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        daySettingViews = new HashMap<>();
        for (Day d : Day.values()) {
            View v = inflater.inflate(R.layout.menu_settings_day, null);
            ((TextView)v.findViewById(R.id.menu_settings_day_name))
                    .setText(MenuStorage.getDayNames()[d.ordinal()]);
            daySettingViews.put(d, v);
            daysSettings.addView(v);
        }
        fillDays();
    }

    public void onActivityResult(int requestCode, int errorCode, Intent resultContainer) {
        if (errorCode == RESULT_OK) {
            DaySettings.MealtimeSettings result;
            Day d;
            switch (requestCode) {
                case REQUEST_ADD :
                    d = (Day) resultContainer.getSerializableExtra("Day");
                    result = (DaySettings.MealtimeSettings)
                            resultContainer.getSerializableExtra("Result");
                    LinearLayout mealtimes = (LinearLayout)
                            daySettingViews.get(d).findViewById(R.id.menu_settings_day_mealtimes);
                    DaySettings.MealtimeSettings settings = new DaySettings.MealtimeSettings(
                            result.getName(), result.dishesCategories());
                    mealtimeSettings.get(d).add(settings);
                    mealtimeSettingsViews.get(d).put(settings,
                            generateMealtimeView(d, mealtimes, settings));
                    break;
                case  REQUEST_EDIT :
                    d = (Day) resultContainer.getSerializableExtra("Day");
                    result = (DaySettings.MealtimeSettings)
                            resultContainer.getSerializableExtra("Result");
                    Integer position = resultContainer.getIntExtra("Position", -1);
                    TextView mealtimeName = (TextView)
                            mealtimeSettingsViews.get(d).get(mealtimeSettings.get(d).get(position))
                            .findViewById(R.id.menu_settings_day_mealtime_name);
                    mealtimeName.setText(result.getName());
                    mealtimeSettings.get(d).get(position).clone(result);
                    break;
            }
        }
    }

    private void fillDays() {
        mealtimeSettingsViews = new HashMap<>();
        mealtimeSettings = new HashMap<>();
        for (final Day d : Day.values()) {
            View v = daySettingViews.get(d);
            CheckBox isCookingDay = (CheckBox) v.findViewById(R.id.menu_settings_day_is_cookingday);
            ImageButton addMealtime = (ImageButton) v.findViewById(R.id.menu_settings_day_mealtime_add);
            LinearLayout mealtimes = (LinearLayout) v.findViewById(R.id.menu_settings_day_mealtimes);

            mealtimeSettingsViews.put(d, new HashMap<DaySettings.MealtimeSettings, View>());
            isCookingDay.setChecked(menuSettings.isCookingDay(d));
            mealtimeSettings.put(d, menuSettings.getDaySettings(d).getMealtimeSettings());
            for (DaySettings.MealtimeSettings settings : mealtimeSettings.get(d)) {
                mealtimeSettingsViews.get(d).put(settings,
                        generateMealtimeView(d, mealtimes, settings));
            }

            isCookingDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    menuSettings.setCookingDay(d, b);
                }
            });

            addMealtime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MenuSettingsActivity.this,
                            ChooseMealtimePresetActivity.class);
                    intent.putExtra("Day", d);
                    startActivityForResult(intent, REQUEST_ADD);
                }
            });
        }
    }

    private View generateMealtimeView (final Day d, final LinearLayout mealtimes,
                                       final DaySettings.MealtimeSettings settings) {
        final View mealtimeView = inflater.inflate(R.layout.menu_settings_day_mealtime, null);
        ImageButton editMealtime = (ImageButton)
                mealtimeView.findViewById(R.id.menu_settings_day_mealtime_edit);
        ImageButton deleteMealtime = (ImageButton)
                mealtimeView.findViewById(R.id.menu_settings_day_mealtime_delete);
        TextView mealtimeName = (TextView)
                mealtimeView.findViewById(R.id.menu_settings_day_mealtime_name);
        final Integer position = mealtimeSettings.get(d).indexOf(settings);

        mealtimeName.setText(settings.getName());
        deleteMealtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSettings.getDaySettings(d).getMealtimeSettings().remove(settings);
                mealtimes.removeView(mealtimeView);
            }
        });
        editMealtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuSettingsActivity.this,
                        ChooseMealtimePresetActivity.class);
                intent.putExtra("Day", d);
                intent.putExtra("Position", position);
                startActivityForResult(intent, REQUEST_EDIT);
            }
        });

        mealtimes.addView(mealtimeView);
        return mealtimeView;
    }
}
