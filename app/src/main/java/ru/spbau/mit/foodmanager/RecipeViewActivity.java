package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class RecipeViewActivity extends AppCompatActivity {
    private static final String INGRIDIENT_LIST_DIVIDER = " - ";
    private static final String INGRIDIENT_LIST_COUNT_DIVIDER = "        ";
    private static final int IMAGES_WIDTH = 200; //dp
    private static final int IMAGES_HEIGHT = 150; //dp
    private Recipe recipe;
    private ArrayList<Step> steps;
    private ArrayList<Integer> categories;
    private GifImageView loaderAnimation;
    private ImageButton likeBtn;
    private ImageButton favoriteBtn;
    private TextView likeCounter;
    private boolean liked;
    private boolean inFavourites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_view);

        //Init loaderAnimation
        loaderAnimation = (GifImageView) findViewById(R.id.loader_animation_view);
        loaderAnimation.setGifImageResource(MainActivity.getRandomLoaderResource());
        loaderAnimation.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //Init like & selected btn
        likeBtn = (ImageButton) findViewById(R.id.recipe_like);
        likeCounter = (TextView) findViewById(R.id.recipe_like_count);
        favoriteBtn = (ImageButton) findViewById(R.id.recipe_add_to_favorites);
        //Init Task
        Intent intent = getIntent();
        ContentLoader contentLoader = new ContentLoader(intent.getIntExtra("Recipe", -1));
        Thread loader = new Thread(contentLoader);
        loader.start();
   }

    public void onCookClick(View v) {
        Intent intent = new Intent(this, StepViewActivity.class);
        intent.putExtra("Recipe", recipe.getID());
        startActivity(intent);
    }

    public void onLikeClick(View v) {
        if (liked) {
            likeBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.like_off));
        } else {
            likeBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.like_on));
        }
        liked = !liked;
        CookBookStorage.getInstance(this).setLike(recipe, liked); // TODO
        likeCounter.setText(String.valueOf(CookBookStorage.getInstance(this).getRecipeLikes(recipe)));
    }

    public void onFavoriteClick(View v) {
        if (!inFavourites) {
            if (inFavourites) {
                favoriteBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.favourite_off));
            } else {
                favoriteBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.favourite_on));
            }
            inFavourites = !inFavourites;
        }
        if (inFavourites) {
            CookBookStorage.getInstance(this).addToFavorites(recipe);
        }
    }
    private void showRecipe() {
        //Header
        CookBookStorage cookbook = CookBookStorage.getInstance(this);
        TextView nameView = (TextView)findViewById(R.id.recipe_header_name);
        ImageView photoView = (ImageView)findViewById(R.id.recipe_header_photo);
        LinearLayout categoryList = (LinearLayout)findViewById(R.id.recipe_header_tags);
        photoView.setImageBitmap(steps.get(steps.size()-1).getImage());
        for (final Integer id : categories) {
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
        StringBuilder ingredientList = new StringBuilder();
        for (Ingredient i : cookbook.getRecipeIngredients(recipe.getID())) {
            if (i.getQuantity() >= 0.0) {
                ingredientList = ingredientList.append(INGRIDIENT_LIST_DIVIDER + i.getName() +
                        INGRIDIENT_LIST_COUNT_DIVIDER + i.getQuantity() + " " +
                        i.getTypeName() + "\n");
            } else {
                ingredientList = ingredientList.append(INGRIDIENT_LIST_DIVIDER + i.getName() +
                        INGRIDIENT_LIST_COUNT_DIVIDER +
                        i.getTypeName() + "\n");
            }
        }
        ingridientsView.setText(ingredientList.toString());
    }

    public class ContentLoader implements Runnable {
        private int recipeID;
        public ContentLoader(int id) {
            this.recipeID = id;
        }

        @Override
        public void run() {
            RecipeViewActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loaderAnimation.setVisibility(View.VISIBLE);
                }
            });
            final CookBookStorage cookbook = CookBookStorage.getInstance(RecipeViewActivity.this);
            recipe = cookbook.getRecipe(recipeID);
            steps = cookbook.getRecipeSteps(recipe.getID());
            categories = cookbook.getRecipeCategories(recipe.getID());
            liked = cookbook.getUserLike(recipe);
            inFavourites = false;
            for (Recipe r : cookbook.getFavorites()) {
                if (r.getID() == recipe.getID()) {
                    inFavourites = true;
                }
            }
            RecipeViewActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loaderAnimation.setVisibility(View.INVISIBLE);
                    showRecipe();
                    loaderAnimation.setGifImageResource(MainActivity.getRandomLoaderResource());
                    if (!liked) {
                        likeBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.like_off));
                    } else {
                        likeBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.like_on));
                    }
                    if (!inFavourites) {
                        favoriteBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.favourite_off));
                    } else {
                        favoriteBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.favourite_on));
                    }
                    likeCounter.setText(String.valueOf(cookbook.getRecipeLikes(recipe)));
                }
            });
        }
    }
}
