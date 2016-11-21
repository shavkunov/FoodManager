package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class CookBookCategoryActivity extends AppCompatActivity {
    private Category category;
    private ArrayList<Recipe> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cook_book_category);
        Intent task = getIntent();
        category = (Category)task.getSerializableExtra("Category");
        recipes = category.getRecipes();
        //ListInitialize

        ArrayList<String> names = new ArrayList<>();
        for (Recipe r : recipes) {
            names.add(r.getName());
        }
        ListView listView = (ListView) findViewById(R.id.);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.cookbook_category_list_element, names);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(CookBookCategoryActivity.this, CookBookCategoryActivity.class);
                intent.putExtra("Recipe", recipes.get(i));
                startActivity(intent);
            }
        });
    }
}
