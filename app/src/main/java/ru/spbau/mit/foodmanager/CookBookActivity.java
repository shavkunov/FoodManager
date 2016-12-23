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
    public static final int GROUPINGBY_TYPE_OF_DISH = 0;
    public static final int GROUPINGBY_NATIONAL_KITCHEN = 1;
    public static final int TARGET_NO = 0;
    public static final int TARGET_CATEGORY = 1;
    public static final int TARGET_RECIPE = 2;
    private Integer target;
    private static final String[] CATEGORY_TYPES =
            {"Тип блюда",
            "Национальные кухни"};
    private ArrayList<Category> categories;
    private CookBookStorage cookbook;
    private GifImageView loaderAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cook_book);

        //Init loaderAnimation
        loaderAnimation = (GifImageView) findViewById(R.id.loader_animation_view);
        loaderAnimation.setGifImageResource(MainActivity.getRandomLoaderResource());
        loaderAnimation.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //Init Task
        task = getIntent();
        target = task.getIntExtra("Target", TARGET_NO);
        //Init Categories
        categories = new ArrayList<>();
        cookbook = CookBookStorage.getInstance(this);
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
                ContentLoader contentLoader;
                Thread loader;
                switch (i) {
                    case 0:
                        contentLoader = new ContentLoader(GROUPINGBY_TYPE_OF_DISH);
                        loader = new Thread(contentLoader);
                        loader.start();
                        break;
                    case 1:
                        contentLoader = new ContentLoader(GROUPINGBY_NATIONAL_KITCHEN);
                        loader = new Thread(contentLoader);
                        loader.start();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //categories.clear();
            }
        });
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

    public class ContentLoader implements Runnable {
        private int groupingBy;
        public ContentLoader(int groupingBy) {
            this.groupingBy = groupingBy;
        }

        @Override
        public void run() {
            CookBookActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loaderAnimation.setVisibility(View.VISIBLE);
                }
            });
            categories.clear();
            switch (groupingBy) {
                case GROUPINGBY_TYPE_OF_DISH:
                    categories.addAll(cookbook.getRecipiesTypeOfDish());
                    break;
                case GROUPINGBY_NATIONAL_KITCHEN:
                    categories.addAll(cookbook.getRecipiesNationalKitchen());
                    break;
            }
            CookBookActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loaderAnimation.setVisibility(View.INVISIBLE);
                    showCategories();
                    loaderAnimation.setGifImageResource(MainActivity.getRandomLoaderResource());
                }
            });
        }
    }
}
