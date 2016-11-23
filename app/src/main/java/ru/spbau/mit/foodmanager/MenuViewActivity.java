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
    private MenuStorage menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_view);
        menu = new MenuStorage(new CookBookStorage(this));
        allDayMenu = menu.getMenu();
        if (allDayMenu == null) {
            allDayMenu = new ArrayList<>();
            //TODO:Generate menu
        }
        //TODO: Сделать древовидный список.
        ListView listView = (ListView) findViewById(R.id.menu_view_list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, DAY_NAMES);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MenuViewActivity.this, CookBookCategoryActivity.class);
                intent.putExtra("RecipeCount", allDayMenu.get(i).size());
                for (int j = 0; j < allDayMenu.get(j).size(); j++) {
                    intent.putExtra("Recipe" + j, allDayMenu.get(i).get(j));
                }
                startActivity(intent);
            }
        });
    }
}
