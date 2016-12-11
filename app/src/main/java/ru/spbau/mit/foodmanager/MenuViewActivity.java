package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

public class MenuViewActivity extends AppCompatActivity {
    private HashMap<Day, DayMenu> allDayMenu;
    private CookBookStorage cookbook;
    private MenuStorage menu;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_view);

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        cookbook = CookBookStorage.getInstance(this);
        menu = MenuStorage.getInstance(this);
        allDayMenu = menu.getMenu();
        showRecipes();
    }

    public void onGenerateBtnClick(View v) {
        menu.generateWeekMenu();
        allDayMenu = menu.getMenu();
        showRecipes();
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
                generateMealtimeView(menu, i, dayMealtimes);
            }
            parent.addView(view);
        }
    }

    private void generateMealtimeView(DayMenu menu, Integer position, LinearLayout parent) {
        LinearLayout mealtimeElement = (LinearLayout) inflater.inflate(
                R.layout.menu_view_day_menu_list_element, null);
        TextView mealtimeName = (TextView) mealtimeElement.findViewById(
                R.id.menu_day_mealtime_name);
        LinearLayout recipeList = (LinearLayout) mealtimeElement.findViewById(
                R.id.menu_day_mealtime_dishes);
        DayMenu.Mealtime mealtime = menu.getMealtimes().get(position);

        mealtimeName.setText(mealtime.getName());
        for (Integer i = 0; i < mealtime.getRecipes().size(); i++) {
            generateRecipeView(mealtime, i, recipeList);
        }
        parent.addView(mealtimeElement);
    }

    private void generateRecipeView(DayMenu.Mealtime mealtime, Integer position, LinearLayout parent) {
        TextView recipeName = new TextView(this);
        final Integer recipeID = mealtime.getRecipes().get(position);

        recipeName.setText(cookbook.getRecipe(recipeID).getName());
        recipeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuViewActivity.this, RecipeViewActivity.class);
                intent.putExtra("Recipe", recipeID);
                startActivity(intent);
            }
        });
        parent.addView(recipeName);
    }
}
