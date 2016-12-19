package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static ArrayList<Integer> loaderResources;
    private static Random random = new Random();
    public static int getRandomLoaderResource() {
        if (loaderResources == null) {
            loaderResources = new ArrayList<>();
            loaderResources.add(R.drawable.loading_animation);
            loaderResources.add(R.drawable.loading_animation2);
            loaderResources.add(R.drawable.loading_animation3);
        }
        int resID = loaderResources.get(Math.abs(random.nextInt()) % loaderResources.size());
        return resID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        startService(new Intent(this, NotificationService.class));
    }

    public void onOpenCookBookClick(View view) {
        Intent intent = new Intent(this, CookBookActivity.class);
        startActivity(intent);
    }

    public void onWeekMenuClick(View view) {
        Intent intent = new  Intent(this, MenuViewActivity.class);
        startActivity(intent);
    }

    public void onShoppingListClick(View view) {
        Intent intent = new  Intent(this, ShoppingListActivity.class);
        startActivity(intent);
    }

    public void onSettingsClick(View view) {
        Intent intent = new  Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onAddRecipeClick(View view) {
        Intent intent = new Intent(this, EditRecipeActivity.class);
        startActivity(intent);
    }
}
