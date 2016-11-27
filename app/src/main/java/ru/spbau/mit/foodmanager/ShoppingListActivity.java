package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ShoppingListActivity extends AppCompatActivity {

    private HashMap<String, Double> productCount;
    private ArrayList<String> productNames;
    private ArrayList<Recipe> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);
        //Init List
        MenuStorage menu = new MenuStorage(new CookBookStorage(this));
        ArrayList<ArrayList<Recipe>> menuRecipes = menu.getMenu();
        if (menuRecipes == null) {
            menuRecipes = new ArrayList<>();
        }
        recipes = new ArrayList<>();
        for(ArrayList<Recipe> rs : menuRecipes) {
            recipes.addAll(rs);
        }
        productCount = new HashMap<>();
        for(Recipe r : recipes) {
            for(Ingredient i : r.getIngredients()) {
                if (productCount.get(i.getName()) == null) {
                    productCount.put(i.getName(), 0.0);
                }
                productCount.put(i.getName(),
                        i.getQuantity() + productCount.get(i.getName()));
            }
        }
        productNames = new ArrayList<>();
        for (String name : productCount.keySet()) {
            productNames.add(name);
        }
        ListView listView = (ListView) findViewById(R.id.shopping_list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, productNames);
        listView.setAdapter(adapter);
    }
}
