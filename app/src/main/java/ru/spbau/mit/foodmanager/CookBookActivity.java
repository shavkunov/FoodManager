package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedList;

public class CookBookActivity extends AppCompatActivity {

    ArrayList<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cook_book);

        //Init List
        categories = new ArrayList<>();
        categories.addAll(CookBookStorage.getRecipiesTypeOfDish());

        ArrayList<String> names = new ArrayList<>();
        for (Category c : categories) {
            names.add(c.categoryDescription());
        }

        ListView listView = (ListView) findViewById(R.id.cook_book_category_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.cookbook_list_element, names);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(CookBookActivity.this, CookBookCategoryActivity.class);
                intent.putExtra("Recipe", categories.get(i));
                startActivity(intent);
            }
        });
    }
}
