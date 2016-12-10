package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MealtimeSettingsActivity extends AppCompatActivity {
    private CookBookStorage cookbook;
    private ArrayList<Integer> categories = new ArrayList<>();
    private LayoutInflater inflater;
    private Intent task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mealtime_settings);

        cookbook = CookBookStorage.getInstance(this);
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        task = getIntent();
        DaySettings.MealtimeSettings settings = (DaySettings.MealtimeSettings)
                task.getSerializableExtra("Settings");

        if (settings != null) {
            EditText name = (EditText) findViewById(R.id.mealtime_settings_name);
            LinearLayout dishes = (LinearLayout) findViewById(R.id.mealtime_settings_dishes);
            categories.addAll(settings.dishesCategories());

            name.setText(settings.getName());
            for (Integer categoryID : categories) {
                dishes.addView(generateDishView(dishes, categoryID));
            }
        }

        ImageButton addBtn = (ImageButton) findViewById(R.id.mealtime_settings_add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO call category choose
            }
        });
    }

    private View generateDishView(final LinearLayout parent, final Integer id) {
        final View dish  = inflater.inflate(R.layout.mealtime_settings_dish, null);
        TextView dishName = (TextView) dish.findViewById(R.id.mealtime_settings_dish_name);
        ImageButton editBtn = (ImageButton) dish.findViewById(R.id.mealtime_settings_dish_edit);
        ImageButton delBtn = (ImageButton) dish.findViewById(R.id.mealtime_settings_dish_delete);
        Category category = cookbook.getCategoryByID(id);

        dishName.setText(category.getDescription());
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO call category choose
            }
        });
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categories.remove(id);
                parent.removeView(dish);
            }
        });
        return dish;
    }

    public void onSaveClick(View v) {
        EditText name = (EditText) findViewById(R.id.mealtime_settings_name);
        returnResult(new DaySettings.MealtimeSettings(
                name.getText().toString(),
                categories));
    }
    private void returnResult(DaySettings.MealtimeSettings result) {
        Intent resultIntent = task;
        resultIntent.putExtra("Result", result);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
