package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ShoppingListActivity extends AppCompatActivity {

    private CookBookStorage cookbook;
    private HashMap<String, Double> productCount;
    private ArrayList<String> productNames;
    private ArrayList<Recipe> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);
        //Init List
        cookbook = CookBookStorage.getInstance();
        MenuStorage menu = MenuStorage.getInstance();
        HashMap<Day, DayMenu> menuRecipes = menu.getMenu();
        recipes = new ArrayList<>();
        for(DayMenu rs : menuRecipes.values()) {
            for (Integer id : rs.getDishes()) {
                recipes.add(cookbook.getRecipe(id));
            }
        }
        productCount = new HashMap<>();
        for(Recipe r : recipes) {
            if (r != null) {
				for(Ingredient i : cookbook.getRecipeIngredients(r.getID())) {
					if (productCount.get(i.getName()) == null) {
						productCount.put(i.getName(), 0.0);
					}
					productCount.put(i.getName(),
							i.getQuantity() + productCount.get(i.getName()));
				}
			}
        }
        productNames = new ArrayList<>();
        for (String name : productCount.keySet()) {
            productNames.add(name);
        }
        ListView listView = (ListView) findViewById(R.id.shopping_list_view);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, productNames);
        listView.setAdapter(adapter);
    }
}
