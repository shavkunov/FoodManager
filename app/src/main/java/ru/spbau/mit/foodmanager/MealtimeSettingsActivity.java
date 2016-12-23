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
import java.util.HashMap;

public class MealtimeSettingsActivity extends AppCompatActivity {
    private static final int REQUEST_ADD = 0;
    private static final int REQUEST_EDIT = 1;
    private CookBookStorage cookbook;
    private ArrayList<Integer> categories = new ArrayList<>();
    private HashMap<Integer, View> views = new HashMap<>();
    private LayoutInflater inflater;
    private LinearLayout dishes;
    private Intent task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mealtime_settings);

        cookbook = CookBookStorage.getInstance(this);
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dishes = (LinearLayout) findViewById(R.id.mealtime_settings_dishes);
        task = getIntent();
        DaySettings.MealtimeSettings settings = (DaySettings.MealtimeSettings)
                task.getSerializableExtra("Settings");

        if (settings != null) {
            EditText name = (EditText) findViewById(R.id.mealtime_settings_name);
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
                Intent intent = new Intent(MealtimeSettingsActivity.this, CookBookActivity.class);
                intent.putExtra("Target", CookBookActivity.TARGET_CATEGORY);
                startActivityForResult(intent, REQUEST_ADD);
            }
        });
    }

    public void onActivityResult(int requestCode, int errorCode, Intent resultContainer) {
        switch (requestCode) {
            case REQUEST_ADD :
                if (errorCode == RESULT_OK) {
                    Integer result = resultContainer.getIntExtra("Result", -1);
                    categories.add(result);
                    dishes.addView(generateDishView(dishes, result));
                }
                break;
            case REQUEST_EDIT :
                if (errorCode == RESULT_OK) {
                    Integer result = resultContainer.getIntExtra("Result", -1);
                    Integer position = resultContainer.getIntExtra("Position", -1);
                    categories.set(position, result);
                    TextView categoryName = (TextView) views.get(position).findViewById(R.id.mealtime_settings_dish_name);
                    categoryName.setText(cookbook.getCategoryByID(result).getDescription());
                }
                break;
        }
    }

    private View generateDishView(final LinearLayout parent, final Integer id) {
        final View dish  = inflater.inflate(R.layout.mealtime_settings_dish, null);
        TextView dishName = (TextView) dish.findViewById(R.id.mealtime_settings_dish_name);
        ImageButton editBtn = (ImageButton) dish.findViewById(R.id.mealtime_settings_dish_edit);
        ImageButton delBtn = (ImageButton) dish.findViewById(R.id.mealtime_settings_dish_delete);
        Category category = cookbook.getCategoryByID(id);
        final Integer position = categories.indexOf(id); //TODO fix possible problems

        dishName.setText(category.getDescription());
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MealtimeSettingsActivity.this, CookBookActivity.class);
                intent.putExtra("Target", CookBookActivity.TARGET_CATEGORY);
                intent.putExtra("Position", position);
                startActivityForResult(intent, REQUEST_EDIT);
            }
        });
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categories.remove(id);
                parent.removeView(dish);
            }
        });
        views.put(position, dish);
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
