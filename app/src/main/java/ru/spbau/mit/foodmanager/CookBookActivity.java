package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

public class CookBookActivity extends AppCompatActivity {
    private Intent task;
    public static final int TARGET_NO = 0;
    public static final int TARGET_CATEGORY = 1;
    public static final int TARGET_RECIPE = 2;
    private Integer target;
    private static final String[] CATEGORY_TYPES =
            {"Тип блюда",
            "Национальные кухни"};
    private ArrayList<Category> categories;
    private CookBookStorage cookbook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cook_book);

        task = getIntent();
        target = task.getIntExtra("Target", TARGET_NO);
        //Init List
        categories = new ArrayList<>();
        cookbook =  CookBookStorage.getInstance();
        categories.addAll(cookbook.getRecipiesTypeOfDish());
        //Group by init
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, CATEGORY_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner groupingBy = (Spinner) findViewById(R.id.cook_book_group_by);
        groupingBy.setAdapter(adapter);
        groupingBy.setSelection(0);
        groupingBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                categories.clear();
                switch (i) {
                    case 0: categories.addAll(cookbook.getRecipiesTypeOfDish());
                        break;
                    case 1: categories.addAll(cookbook.getRecipiesNationalKitchen());
                        break;
                }
                showCategories();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //categories.clear();
            }
        });

        showCategories();
    }

    public void onActivityResult(int requestCode, int errorCode, Intent resultContainer) {
        if (errorCode == RESULT_OK) {
            setResult(RESULT_OK, resultContainer);
            finish();
        }
    }

    private void showCategories() {
        ListView listView = (ListView) findViewById(R.id.cook_book_list);
        //TODO: сделать собственные адаптеры для CookBook и Category
        ArrayList<String> names = new ArrayList<>();
        for (Category c : categories) {
            names.add(c.getDescription());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent;
                switch (target) {
                    case TARGET_NO:
                        intent = new Intent(CookBookActivity.this, CookBookCategoryActivity.class);
                        intent.putExtra("Category", categories.get(i).getID());
                        startActivity(intent);
                        break;
                    case TARGET_RECIPE:
                        intent = new Intent(CookBookActivity.this, CookBookCategoryActivity.class);
                        intent.putExtras(task);
                        intent.putExtra("Category", categories.get(i).getID());
                        startActivityForResult(intent, TARGET_RECIPE);
                        break;
                    case TARGET_CATEGORY:
                        intent = task;
                        intent.putExtra("Result", categories.get(i).getID());
                        setResult(RESULT_OK, intent);
                        finish();
                        break;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
