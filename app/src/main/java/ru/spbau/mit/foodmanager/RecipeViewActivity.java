package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class RecipeViewActivity extends AppCompatActivity {
    private static final String INGRIDIENT_LIST_DIVIDER = " - ";
    private static final String INGRIDIENT_LIST_COUNT_DIVIDER = "        ";
    private static final int IMAGES_WIDTH = 200; //dp
    private static final int IMAGES_HEIGHT = 150; //dp
    private static Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_view);
        Intent intent = getIntent();
        CookBookStorage cookbook = new CookBookStorage(this);
        recipe = cookbook.getRecipe(intent.getIntExtra("Recipe", -1));
        ArrayList<Step> steps = recipe.getStepByStep();
        //Header
        TextView nameView = (TextView)findViewById(R.id.recipe_header_name);
        ImageView photoView = (ImageView)findViewById(R.id.recipe_header_photo);
        LinearLayout categoryList = (LinearLayout)findViewById(R.id.recipe_header_tags);
        photoView.setImageBitmap(steps.get(steps.size()-1).getImage());
        for (final Integer id : recipe.getCategoryID()) {
            Button categoryNameView = new Button(this);
            //TextView categoryNameView = new TextView(this);
            categoryNameView.setBackgroundColor(0x00_00_00_00);
            categoryNameView.setTextAppearance(this, R.style.RecipeView_CategoryShowStyle); //I can't found another method
            categoryNameView.setText(cookbook.getCategoryByID(id).getDescription());
            categoryNameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(RecipeViewActivity.this, CookBookCategoryActivity.class);
                    intent.putExtra("Category", id);
                    startActivity(intent);
                }
            });
            categoryList.addView(categoryNameView);
        }

        //Body
        TextView descriptionView = (TextView)findViewById(R.id.recipe_body_description);
        TextView ingridientsView = (TextView)findViewById(R.id.recipe_body_ingredients);
        LinearLayout recipeImages = (LinearLayout)findViewById(R.id.recipe_body_images);
        int widthInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, IMAGES_WIDTH, getResources().getDisplayMetrics());
        int heightInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, IMAGES_HEIGHT, getResources().getDisplayMetrics());
        for (Step step : steps) {
            ImageView photo = new ImageView(this);
            photo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            photo.setLayoutParams(new LinearLayout.LayoutParams(widthInPx, heightInPx));
            photo.setImageBitmap(step.getImage());
            recipeImages.addView(photo);
        }

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

    }

    public void onCookClick(View v) {
        Intent intent = new Intent(this, StepViewActivity.class);
        intent.putExtra("Recipe", recipe.getID());
        startActivity(intent);
    }
}
