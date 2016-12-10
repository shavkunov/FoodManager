package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

public class MenuViewActivity extends AppCompatActivity {
    static private String[] DAY_NAMES = {
            "Понедельник",
            "Вторник",
            "Среда",
            "Четверг",
            "Пятница",
            "Суббота",
            "Воскресенье"};
    private HashMap<Day, DayMenu> allDayMenu;
    private ArrayList<Recipe> recipes;
    private CookBookStorage cookbook;
    private MenuStorage menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_view);
        recipes = new ArrayList<>();
        cookbook = CookBookStorage.getInstance(this);
        menu = MenuStorage.getInstance(this);
        allDayMenu = menu.getMenu();
        //TODO: Группировка по приемам пищи
        for (DayMenu rc : allDayMenu.values()) {
            for (Integer r : rc.getDishes()) {
                recipes.add(cookbook.getRecipe(r));
            }
        }
        showRecipes();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public void onGenerateBtnClick(View v) {
        recipes = new ArrayList<>(); //Copy and paste but who cares?
        menu.generateWeekMenu();
        allDayMenu = menu.getMenu();
        for (DayMenu dayMenu : allDayMenu.values()) {
            if (dayMenu != null) {
                for (Integer r : dayMenu.getDishes()) {
                    recipes.add(cookbook.getRecipe(r));
                }
            }
        }
        showRecipes();
    }

    private void showRecipes() {
        ArrayList<String> recipeNames = new ArrayList<>();
        for (Recipe r : recipes) {
            recipeNames.add(r.getName());
        }
        ListView listView = (ListView) findViewById(R.id.menu_view_list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, recipeNames);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MenuViewActivity.this, RecipeViewActivity.class);
                intent.putExtra("Recipe", recipes.get(i).getID());
                startActivity(intent);
            }
        });
    }
}
