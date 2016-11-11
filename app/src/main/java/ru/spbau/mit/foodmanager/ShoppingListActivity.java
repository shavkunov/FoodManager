package ru.spbau.mit.foodmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ShoppingListActivity extends AppCompatActivity {

    static String[] productNames = {"name1", "name2", "name3"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);
        //Init List
        ListView listView = (ListView) findViewById(R.id.shopping_list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.shopping_list_element, productNames);
        listView.setAdapter(adapter);
    }
}
