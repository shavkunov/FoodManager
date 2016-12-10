package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

public class MenuViewActivity extends AppCompatActivity {
    private HashMap<Day, DayMenu> allDayMenu;
    private CookBookStorage cookbook;
    private MenuStorage menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_view);
        cookbook = CookBookStorage.getInstance(this);
        menu = MenuStorage.getInstance(this);
        allDayMenu = menu.getMenu();
        showRecipes();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public void onGenerateBtnClick(View v) {
        menu.generateWeekMenu();
        allDayMenu = menu.getMenu();
        showRecipes();
    }

    private void showRecipes() {
        ListView daysListView = (ListView) findViewById(R.id.menu_view_list_view);
        BaseAdapter adapter = new MenuViewAdapter(this, allDayMenu);
        daysListView.setAdapter(adapter);
    }
}
