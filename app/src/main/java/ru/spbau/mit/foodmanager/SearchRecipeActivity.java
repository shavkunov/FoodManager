package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class SearchRecipeActivity extends AppCompatActivity {

    private ArrayList<Recipe> recipes;
    private Intent task;
    private GifImageView loaderAnimation;
    private ListView recipesList;
    private Thread searchingThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_recipe);
        final EditText searchText = (EditText)findViewById(R.id.search_text);

        //Init loaderAnimation
        loaderAnimation = (GifImageView) findViewById(R.id.loader_animation_view);
        loaderAnimation.setGifImageResource(LoaderAnimationSelector.getRandomLoaderResource());
        loaderAnimation.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        loaderAnimation.setVisibility(View.INVISIBLE);
        recipesList = (ListView) findViewById(R.id.search_recipes_list);
        task = getIntent();


        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (searchingThread != null) {
                    searchingThread.interrupt();
                }
                searchingThread = new Thread(new ContentLoader(editable.toString()));
                searchingThread.start();
            }
        });
    }

    private void showRecipes() {
        Log.d("ShowRecipies", "Show");
        ArrayList<String> names = new ArrayList<>();
        if (recipes != null) {
            for (Recipe r : recipes) {
                names.add(r.getName());
            }
        }
        Log.d("names", ((Integer)names.size()).toString());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, names);
        recipesList.setAdapter(adapter);
        recipesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                task.putExtra("Result", recipes.get(i).getID());
                setResult(RESULT_OK, task);
                finish();
            }
        });
    }

    public class ContentLoader implements Runnable {
        private String searchFilter;
        public ContentLoader(String searchFilter) {
            this.searchFilter = searchFilter;
        }

        @Override
        public void run() {
            SearchRecipeActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //loaderAnimation.setVisibility(View.VISIBLE);
                }
            });

            recipes = null;
            while (recipes == null) {
                recipes = CookBookStorage.getInstance(SearchRecipeActivity.this).getRecipesByFilter(searchFilter);
            }
            SearchRecipeActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //loaderAnimation.setVisibility(View.INVISIBLE);
                    showRecipes();
                    loaderAnimation.setGifImageResource(LoaderAnimationSelector.getRandomLoaderResource());
                }
            });
        }
    }
}
