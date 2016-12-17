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
    private Category category;
    private Intent task;
    private Integer target;
    private ArrayList<Recipe> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cook_book_category);
        task = getIntent();
        target =  task.getIntExtra("Target",CookBookActivity.TARGET_NO);
        category = CookBookStorage.getInstance().getCategoryByID(task.getIntExtra("Category", -1));
        if (category != null) {
            recipes = category.getRecipes();
        }
        //ListInitialize
        ListView listView = (ListView) findViewById(R.id.cook_book_category_list);
        ArrayList<String> names = new ArrayList<>();
        if (recipes != null) {
            for (Recipe r : recipes) {
                names.add(r.getName());
            }
        }
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
}
