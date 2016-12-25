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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ShoppingListActivity extends AppCompatActivity {

    private CookBookStorage cookbook;
    private ArrayList<Recipe> recipes;
    private ArrayList<Ingredient> ingredients;
    private GifImageView loaderAnimation;
    private LinearLayout informationLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);
        //Init loaderAnimation
        loaderAnimation = (GifImageView) findViewById(R.id.loader_animation_view);
        loaderAnimation.setGifImageResource(MainActivity.getRandomLoaderResource());
        loaderAnimation.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        informationLayout = (LinearLayout) findViewById(R.id.information_layout);
        //Init cookbook
        cookbook = CookBookStorage.getInstance(this);
        //Init Task
        ContentLoader contentLoader = new ContentLoader();
        Thread loader = new Thread(contentLoader);
        loader.start();
    }

    private void showIngredients() {
        ListView listView = (ListView) findViewById(R.id.shopping_list_view);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        IngredientAdapter adapter = new IngredientAdapter(ingredients);
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

    public class ContentLoader implements Runnable {

        public ContentLoader() {
        }

        @Override
        public void run() {
            ShoppingListActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loaderAnimation.setVisibility(View.VISIBLE);
                    informationLayout.setVisibility(View.INVISIBLE);
                }
            });
            MenuStorage menu = MenuStorage.getInstance(ShoppingListActivity.this);
            HashMap<Day, DayMenu> menuRecipes = menu.getMenu();
            recipes = new ArrayList<>();
            for (DayMenu rs : menuRecipes.values()) {
                for (Integer id : rs.getDishes()) {
                    recipes.add(cookbook.getRecipe(id));
                    //TODO: do in background
                }
            }
            ArrayList<Ingredient> allIngredients = new ArrayList<>();
            for (Recipe r : recipes) {
                allIngredients.addAll(cookbook.getRecipeIngredients(r.getID()));
            }
            ingredients = new ArrayList<>();
            for (Ingredient i : allIngredients) {
                Boolean newIngredient = true;
                for (Ingredient j : ingredients) {
                    if (i.getName().equals(j.getName()) && i.getMeasure().equals(j.getMeasure())) {
                        newIngredient = false;
                        j.setQuantity(j.getQuantity() + i.getQuantity());
                        break;
                    }
                }
                if (newIngredient) {
                    ingredients.add(i);
                }
            }

            Collections.sort(ingredients, new Comparator<Ingredient>() {
                @Override
                public int compare(Ingredient i1, Ingredient i2) {
                    return i1.getName().compareTo(i2.getName());
                }
            });

            ShoppingListActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showIngredients();
                    loaderAnimation.setVisibility(View.INVISIBLE);
                    informationLayout.setVisibility(View.VISIBLE);
                    loaderAnimation.setGifImageResource(MainActivity.getRandomLoaderResource());
                }
            });
        }
    }
}
