package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void onOpenCookBookClick(View view) {
        Intent intent = new Intent(this, CookBookActivity.class);
        startActivity(intent);
    }

    public void onWeekMenuClick(View view) {
        Intent intent = new  Intent(this, MenuViewActivity.class);
        startActivity(intent);
    }
}
