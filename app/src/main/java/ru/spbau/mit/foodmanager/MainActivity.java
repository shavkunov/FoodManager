package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        new Thread(new Runnable() {
            @Override
            public void run() {
                CookBookStorage.getInstance(this);
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
