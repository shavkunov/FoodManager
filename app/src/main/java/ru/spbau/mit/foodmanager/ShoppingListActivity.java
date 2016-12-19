package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

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
        for (DayMenu rs : menuRecipes.values()) {
            for (Integer id : rs.getDishes()) {
                recipes.add(cookbook.getRecipe(id));
                //TODO: do in background
            }
        }
        ArrayList<Ingredient> allIngredients = new ArrayList<>();
        productCount = new HashMap<>();
        for (Recipe r : recipes) {
            allIngredients.addAll(cookbook.getRecipeIngredients(r.getID()));
        }
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        for (Ingredient i : allIngredients) {
            Boolean newIngredient = true;
            for (Ingredient j : ingredients) {
                if (i.getName().equals(j.getName()) && i.getMeasure().equals(j.getName())) {
                    newIngredient = false;
                    j.setQuantity(j.getQuantity() + i.getQuantity());
                }
            }
            if (newIngredient) {
                ingredients.add(i);
            }
        }
        ListView listView = (ListView) findViewById(R.id.shopping_list_view);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        IngredientAdapter adapter = new IngredientAdapter(ingredients);
        //ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
        //        android.R.layout.simple_list_item_multiple_choice, productNames);
        listView.setAdapter(adapter);
    }

    public class IngredientAdapter extends BaseAdapter {
        private ArrayList<Ingredient> ingredients;
        private HashMap<Integer, Boolean> isChecked;
        private LayoutInflater inflater;

        public IngredientAdapter(ArrayList<Ingredient> ing) {
            ingredients = ing;
            inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            isChecked = new HashMap<>();
        }

        @Override
        public int getCount() {
            return ingredients.size();
        }

        @Override
        public Object getItem(int i) {
            return ingredients.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = inflater.inflate(R.layout.shopping_list_element, null);
            }
            TextView name = (TextView) view.findViewById(R.id.shopping_list_element_text);
            TextView count = (TextView) view.findViewById(R.id.shopping_list_element_count);
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.shopping_list_element_checkbox);
            name.setText(ingredients.get(i).getName());
            String countStr = "";
            if (ingredients.get(i).getQuantity() > 0) {
                countStr = ((Double)ingredients.get(i).getQuantity()).toString() + " ";
            }
            countStr = countStr + ingredients.get(i).getTypeName();
            count.setText(countStr);
            if (isChecked.get(i) == null) {
                isChecked.put(i, false);
            }
            checkBox.setChecked(isChecked.get(i));
            return view;
        }

        public class OnIngredientCheckedListener implements CompoundButton.OnCheckedChangeListener {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isChecked.put((Integer)compoundButton.getTag(), b);
            }
        }
    }
}
