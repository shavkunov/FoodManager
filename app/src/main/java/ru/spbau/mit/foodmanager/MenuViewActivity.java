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

public class MenuViewActivity extends AppCompatActivity {
    static private String[] DAY_NAMES = {
            "Понедельник",
            "Вторник",
            "Среда",
            "Четверг",
            "Пятница",
            "Суббота",
            "Воскресенье"};
    private ArrayList<ArrayList<Recipe>> allDayMenu;
    private ArrayList<Recipe> recipes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_view);
        CookBookStorage cookbook = new CookBookStorage(this);
        MenuStorage menu = new MenuStorage(cookbook);
        allDayMenu = menu.getMenu();
        if (allDayMenu == null) {
            menu.setNewWeekMenu();
            allDayMenu = menu.getMenu();
        }
        //TODO: Сделать древовидный список.
        recipes = new ArrayList<>();
        for (ArrayList<Recipe> rc : allDayMenu) {
            for (Recipe r : rc) {
                if (r != null) {
                    recipes.add(r);
                }
            }
        }
        ArrayList<String> recipeNames = new ArrayList<>();
        for (Recipe r : recipes) {
            recipeNames.add(r.getName());
        }
        cookbook.close();
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
