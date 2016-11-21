package ru.spbau.mit.foodmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        CookBookStorage cookbook = new CookBookStorage(this);
        Recipe r = cookbook.getRecipe(1);
    }
}
