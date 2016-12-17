package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;

public class MenuViewActivity extends AppCompatActivity {
    private final static int REQUEST_ADD = 0;
    private final static int REQUEST_EDIT = 1;
    private HashMap<Day, DayMenu> allDayMenu;
    private CookBookStorage cookbook;
    private MenuStorage menu;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_view);
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        cookbook = CookBookStorage.getInstance();
        menu = MenuStorage.getInstance();
        allDayMenu = menu.getMenu();
        showRecipes();
    }

    public void onGenerateBtnClick(View v) {
        menu.generateWeekMenu();
        allDayMenu = menu.getMenu();
        showRecipes();
    }

    public void onActivityResult(int requestCode, int errorCode, Intent resultContainer) {
        if (errorCode == RESULT_OK) {
            Day day = (Day) resultContainer.getSerializableExtra("Day");
            Integer mealtimePos = resultContainer.getIntExtra("MealtimePosition", -1);
            Integer result = resultContainer.getIntExtra("Result", -1);
            switch (requestCode) {
                case REQUEST_ADD :
                    allDayMenu.get(day).getMealtimes().get(mealtimePos).getRecipes().add(result);
                    showRecipes();
                    break;
                case REQUEST_EDIT :
                    Integer position = resultContainer.getIntExtra("Position", -1);
                    allDayMenu.get(day).getMealtimes().get(mealtimePos).getRecipes().set(position, result);
                    showRecipes();
                    break;
            }
        }
    }

    private void showRecipes() {
        LinearLayout daysListView = (LinearLayout) findViewById(R.id.menu_view_days);
        daysListView.removeAllViews();
        for (Day d : Day.values()) {
            generateDayMenuView(d, daysListView);
        }
    }

    private void generateDayMenuView(Day day, LinearLayout parent) {
        View view = inflater.inflate(R.layout.menu_view_list_element, null);
        DayMenu menu = allDayMenu.get(day);
        TextView dayName = (TextView) view.findViewById(R.id.menu_day_name);
        LinearLayout dayMealtimes = (LinearLayout) view.findViewById(R.id.menu_day_mealtimes);

        if (menu != null) {
            dayName.setText(MenuStorage.getDayNames()[day.ordinal()]);
            for (Integer i = 0; i < menu.getMealtimes().size(); i++) {
                generateMealtimeView(menu, i, dayMealtimes, day);
            }
            parent.addView(view);
        }
    }

    private void generateMealtimeView(DayMenu menu, final Integer position, LinearLayout parent,
                                      final Day day) {
        LinearLayout mealtimeElement = (LinearLayout) inflater.inflate(
                R.layout.menu_view_day_menu_list_element, null);
        TextView mealtimeName = (TextView) mealtimeElement.findViewById(
                R.id.menu_day_mealtime_name);
        LinearLayout recipeList = (LinearLayout) mealtimeElement.findViewById(
                R.id.menu_day_mealtime_dishes);
        DayMenu.Mealtime mealtime = menu.getMealtimes().get(position);
        ImageButton addDish = (ImageButton) mealtimeElement.findViewById(R.id.menu_day_mealtime_dish_add);


        mealtimeName.setText(mealtime.getName());
        for (Integer i = 0; i < mealtime.getRecipes().size(); i++) {
            generateRecipeView(mealtime, i, recipeList, day, position);
        }
        addDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent task = new Intent(MenuViewActivity.this, CookBookActivity.class);
                task.putExtra("Target", CookBookActivity.TARGET_RECIPE);
                task.putExtra("Day", day);
                task.putExtra("MealtimePosition", position);
                startActivityForResult(task, REQUEST_ADD);
            }
        });
        parent.addView(mealtimeElement);
    }

    private void generateRecipeView(final DayMenu.Mealtime mealtime, final Integer position, LinearLayout parent,
                                    final Day day, final Integer mealtimePos) {
        View recipeView = inflater.inflate(R.layout.menu_view_day_menu_meltime_list_element, null);
        TextView recipeName = (TextView) recipeView.findViewById(R.id.menu_day_mealtime_dish_name);
        final Integer recipeID = mealtime.getRecipes().get(position);
        ImageButton editDish = (ImageButton) recipeView.findViewById(R.id.menu_day_mealtime_dish_edit);
        ImageButton deleteDish = (ImageButton) recipeView.findViewById(R.id.menu_day_mealtime_dish_delete);

        recipeName.setText(cookbook.getRecipe(recipeID).getName());
        recipeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuViewActivity.this, RecipeViewActivity.class);
                intent.putExtra("Recipe", recipeID);
                startActivity(intent);
            }
        });
        deleteDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allDayMenu.get(day).getMealtimes().get(mealtimePos).getRecipes().remove((int)position);
                Log.d("Try to remove", "Try tot remove");
                showRecipes(); //We need to do this because positions in other buttons now broken
            }
        });
        editDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent task = new Intent(MenuViewActivity.this, CookBookActivity.class);
                task.putExtra("Target", CookBookActivity.TARGET_RECIPE);
                task.putExtra("Day", day);
                task.putExtra("MealtimePosition", mealtimePos);
                task.putExtra("Position", position);
                startActivityForResult(task, REQUEST_EDIT);
            }
        });
        parent.addView(recipeView);
    }
}
