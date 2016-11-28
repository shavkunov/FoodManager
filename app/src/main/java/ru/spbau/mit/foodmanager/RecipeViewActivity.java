package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class RecipeViewActivity extends AppCompatActivity {
    private static final String INGRIDIENT_LIST_DIVIDER = " - ";
    private static final String INGRIDIENT_LIST_COUNT_DIVIDER = "        ";
    private static Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_view);
        Intent intent = getIntent();
        CookBookStorage cookbook = new CookBookStorage(this);
        recipe = cookbook.getRecipe(intent.getIntExtra("Recipe", -1));
        //Header
        TextView nameView = (TextView)findViewById(R.id.recipe_header_name);
        ImageView photoView = (ImageView)findViewById(R.id.recipe_header_photo);
        LinearLayout categoryList = (LinearLayout)findViewById(R.id.recipe_header_tags);
        for (Integer id : recipe.getCategoryID()) {
            Button categoryNameView = new Button(this);
            //TextView categoryNameView = new TextView(this);
            categoryNameView.setBackgroundColor(0x00_00_00_00);
            categoryNameView.setTextAppearance(this, R.style.RecipeView_CategoryShowStyle);
            categoryNameView.setText(cookbook.getCategoryByID(id).getDescription());
            categoryList.addView(categoryNameView);
        }

        //Body
        TextView descriptionView = (TextView)findViewById(R.id.recipe_body_description);
        TextView ingridientsView = (TextView)findViewById(R.id.recipe_body_ingredients);

        nameView.setText(recipe.getName());
        descriptionView.setText(recipe.getDescription());
        StringBuilder ingridientList = new StringBuilder();
        for (Ingredient i : recipe.getIngredients()) {
            if (i.getQuantity() >= 0.0) {
                ingridientList = ingridientList.append(INGRIDIENT_LIST_DIVIDER + i.getName() +
                        INGRIDIENT_LIST_COUNT_DIVIDER + i.getQuantity() + " " +
                        i.getTypeName() + "\n");
            } else {
                ingridientList = ingridientList.append(INGRIDIENT_LIST_DIVIDER + i.getName() +
                        INGRIDIENT_LIST_COUNT_DIVIDER +
                        i.getTypeName() + "\n");
            }
        }
        ingridientsView.setText(ingridientList);
        ArrayList<Step> steps = recipe.getStepByStep();
        photoView.setImageBitmap(steps.get(steps.size()-1).getImage());
    }

    public void onCookClick(View v) {
        Intent intent = new Intent(this, StepViewActivity.class);
        intent.putExtra("Recipe", recipe.getID());
        startActivity(intent);
    }
}
