package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ChooseMealtimePresetActivity extends AppCompatActivity {
    private static final int REQUEST_ADD = 0;
    private static final int REQUEST_EDIT = 1;
    private MenuSettings menuSettings;
    private ArrayList<DaySettings.MealtimeSettings> presets;
    private HashMap<DaySettings.MealtimeSettings, View> presetView;
    private LayoutInflater inflater;
    private LinearLayout presetList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_mealtime_preset);
        menuSettings = MenuSettings.getInstance(this);
        presets = DaySettings.getMealtimePresets(this);
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        presetList = (LinearLayout) findViewById(R.id.choose_mealtime_preset_list);
        presetView = new HashMap<>();
        fillPresets();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DaySettings.savePresets(this);
    }

    private void fillPresets() {
        final LinearLayout v = presetList;
        for (final DaySettings.MealtimeSettings settings : presets) {
            v.addView(generatePresetView(v, settings));
        }
        ImageButton addBtn = (ImageButton) findViewById(R.id.choose_mealtime_preset_add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseMealtimePresetActivity.this,
                        MealtimeSettingsActivity.class);
                startActivityForResult(intent, REQUEST_ADD);
            }
        });
    }

    public void onActivityResult(int requestCode, int errorCode, Intent resultContainer) {
        switch (requestCode) {
            case REQUEST_ADD :
                if (errorCode == RESULT_OK) {
                    DaySettings.MealtimeSettings result =
                            (DaySettings.MealtimeSettings) resultContainer.getSerializableExtra("Result");
                    presets.add(result);
                    presetList.addView(generatePresetView(presetList, result));
                }
                break;
            case REQUEST_EDIT :
                if (errorCode == RESULT_OK) {
                    DaySettings.MealtimeSettings result =
                            (DaySettings.MealtimeSettings) resultContainer.getSerializableExtra("Result");
                    Integer position = resultContainer.getIntExtra("Position", -1);
                    presets.get(position).clone(result);
                    TextView mealtimeName = (TextView) presetView.get(presets.get(position))
                            .findViewById(R.id.menu_settings_day_mealtime_name);
                    mealtimeName.setText(result.getName());
                }
                break;
        }
    }

    private View generatePresetView(final LinearLayout v, final DaySettings.MealtimeSettings settings) {
        final View preset = inflater.inflate(R.layout.menu_settings_day_mealtime, null);
        TextView mealtimeName = (TextView) preset.findViewById(R.id.menu_settings_day_mealtime_name);
        ImageButton editBtn = (ImageButton) preset.findViewById(R.id.menu_settings_day_mealtime_edit);
        ImageButton delBtn = (ImageButton) preset.findViewById(R.id.menu_settings_day_mealtime_delete);
        final Integer position = presets.indexOf(settings);

        mealtimeName.setText(settings.getName());
        mealtimeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnResult(settings);
            }
        });
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseMealtimePresetActivity.this,
                        MealtimeSettingsActivity.class);
                intent.putExtra("Settings", settings);
                intent.putExtra("Position", position);
                startActivityForResult(intent, REQUEST_EDIT);
            }
        });
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presets.remove(settings);
                v.removeView(preset);
            }
        });
        presetView.put(settings, preset);
        return preset;
    }

    private void returnResult(DaySettings.MealtimeSettings result) {
        Intent intent = getIntent();
        intent.putExtra("Result", result);
        setResult(RESULT_OK, intent);
        finish();
    }
}
