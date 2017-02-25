package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (CookBookStorage.getInstance(MainActivity.this) == null);
                startService(new Intent(MainActivity.this, NotificationService.class));
            }
        }).start();
    }

    public void onOpenCookBookClick(View view) {
        Intent intent = new Intent(this, CookBookActivity.class);
        startActivity(intent);
    }

    public void onWeekMenuClick(View view) {
        Intent intent = new Intent(this, MenuViewActivity.class);
        startActivity(intent);
    }

    public void onShoppingListClick(View view) {
        Intent intent = new Intent(this, ShoppingListActivity.class);
        startActivity(intent);
    }

    public void onSettingsClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onFavouritesClick(View view) {
        Intent intent = new Intent(this, CookBookCategoryActivity.class);
        intent.putExtra("Target", CookBookCategoryActivity.TARGET_FAVOURITES);
        startActivity(intent);
    }

    public void onAddRecipeClick(View view) {
        Intent intent = new Intent(this, EditRecipeActivity.class);
        startActivity(intent);
    }
}
