package ru.spbau.mit.foodmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CookBookActivity extends AppCompatActivity {

    final static String[] categoryNames = {"Test Category 1", "Test Category 2",
                                           "Test Category 3", "Test Category 4"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cook_book);

        //Init List
        ListView listView = (ListView) findViewById(R.id.cook_bookList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.cookbook_list_element, categoryNames);
        listView.setAdapter(adapter);
    }
}
