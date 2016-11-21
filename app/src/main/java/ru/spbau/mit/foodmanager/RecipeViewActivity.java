package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class RecipeViewActivity extends AppCompatActivity {
    private static final String INGRIDIENT_LIST_DIVIDER = " - ";
    private static final String INGRIDIENT_LIST_COUNT_DIVIDER = " = ";
    private static final String INGRIGIENT_LIST_COUNT_METRIC = " гр.";
    private static Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_view);
        Intent intent = getIntent();
        recipe = (Recipe) intent.getSerializableExtra("Recipe");
        TextView nameView = (TextView)findViewById(R.id.recipe_header_name);
        ImageView photoView = (ImageView)findViewById(R.id.recipe_header_photo);
        TextView descriptionView = (TextView)findViewById(R.id.recipe_body_description);
        TextView ingridientsView = (TextView)findViewById(R.id.recipe_body_ingredients);

        nameView.setText(recipe.getName());
        descriptionView.setText(recipe.getDescription());
        StringBuilder ingridientList = new StringBuilder();
        for (Ingredient i : recipe.getIngredients()) {
            ingridientList = ingridientList.append(INGRIDIENT_LIST_DIVIDER + i.getName() +
                    INGRIDIENT_LIST_COUNT_DIVIDER + i.getQuantity() +
                    INGRIGIENT_LIST_COUNT_METRIC + "\n");
        }
        ingridientsView.setText(ingridientList);
        //photoView.setImageBitmap();
    }
}
