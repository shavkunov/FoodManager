package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class CookBookCategoryActivity extends AppCompatActivity {
    public static final int TARGET_FAVOURITES = 10;
    private Category category;
    private Intent task;
    private Integer target;
    private ArrayList<Recipe> recipes;
    private GifImageView loaderAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cook_book_category);
        //Init loaderAnimation
        loaderAnimation = (GifImageView) findViewById(R.id.loader_animation_view);
        loaderAnimation.setGifImageResource(MainActivity.getRandomLoaderResource());
        loaderAnimation.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //Task init
        task = getIntent();
        target =  task.getIntExtra("Target",CookBookActivity.TARGET_NO);
        ContentLoader contentLoader = new ContentLoader(task.getIntExtra("Category", -1));
        Thread loader = new Thread(contentLoader);
        loader.start();
        //category = CookBookStorage.getInstance().getCategoryByID(task.getIntExtra("Category", -1));
    }

    private void showRecipies() {
        Log.d("ShowRecipies", "Show");
        ListView listView = (ListView) findViewById(R.id.cook_book_category_list);
        ArrayList<String> names = new ArrayList<>();
        if (recipes != null) {
            for (Recipe r : recipes) {
                names.add(r.getName());
            }
        }
        Log.d("names", ((Integer)names.size()).toString());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent;
                switch (target) {
                    case CookBookActivity.TARGET_NO :
                        intent = new Intent(CookBookCategoryActivity.this, RecipeViewActivity.class);
                        intent.putExtra("Recipe", recipes.get(i).getID());
                        startActivity(intent);
                        break;
                    case TARGET_FAVOURITES :
                        intent = new Intent(CookBookCategoryActivity.this, RecipeViewActivity.class);
                        intent.putExtra("Recipe", recipes.get(i).getID());
                        startActivity(intent);
                        break;
                    case  CookBookActivity.TARGET_RECIPE :
                        intent = task;
                        intent.putExtra("Result", recipes.get(i).getID());
                        setResult(RESULT_OK, intent);
                        finish();
                        break;
                }
            }
        });
    }

    public class ContentLoader implements Runnable {
        private int categoryID;
        public ContentLoader(int id) {
            this.categoryID = id;
        }

        @Override
        public void run() {
            Log.d("Find categories", "Find!");
            CookBookCategoryActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loaderAnimation.setVisibility(View.VISIBLE);
                }
            });
            if (target != TARGET_FAVOURITES) {
                category = CookBookStorage.getInstance().getCategoryByID(categoryID);
                if (category != null) {
                    recipes = category.getRecipes();
                }
            } else {
                //TODO init with favourite recipes
            }
            CookBookCategoryActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loaderAnimation.setVisibility(View.INVISIBLE);
                    showRecipies();
                    loaderAnimation.setGifImageResource(MainActivity.getRandomLoaderResource());
                }
            });
        }
    }
}
