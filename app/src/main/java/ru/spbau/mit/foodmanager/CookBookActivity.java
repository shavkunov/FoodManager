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
import java.util.LinkedList;

public class CookBookActivity extends AppCompatActivity {

    ArrayList<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.cook_book);
        //Init List
        ListView listView = (ListView) findViewById(R.id.cook_book_list);
        categories = new ArrayList<>();
        CookBookStorage cookbook = new CookBookStorage(this);
        categories.addAll(cookbook.getRecipiesTypeOfDish());
        ArrayList<String> names = new ArrayList<>();
        for (Category c : categories) {
            names.add(c.getDescription());
        }
        Log.d("CookBookActivityLogs", "Get Names");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, names);
        Log.d("CookBookActivityLogs", "Created Adapter");
        listView.setAdapter(adapter);
        Log.d("CookBookActivityLogs", "Created List");
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
