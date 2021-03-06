package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class RecipeViewActivity extends AppCompatActivity {
    private static final String INGRIDIENT_LIST_DIVIDER = " - ";
    private static final String INGRIDIENT_LIST_COUNT_DIVIDER = "        ";
    private static final int IMAGES_WIDTH = 300; //dp
    private static final int IMAGES_HEIGHT = 200; //dp
    private static final int CATEGORY_NAME_PADDING = 12; //dp
    private Recipe recipe;
    private ArrayList<Step> steps;
    private ArrayList<Integer> categories;
    private ArrayList<Ingredient> ingredients;
    private HashMap<Integer, String> categoryDescriptions;
    private GifImageView loaderAnimation;
    private LinearLayout informationLayout;
    private ImageButton likeBtn;
    private ImageButton favoriteBtn;
    private ImageButton deleteBtn;
    private ImageButton editBtn;
    private TextView likeCounter;
    private int likeCount;
    private boolean liked;
    private boolean inFavourites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_view);

        //Init loaderAnimation
        loaderAnimation = (GifImageView) findViewById(R.id.loader_animation_view);
        loaderAnimation.setGifImageResource(LoaderAnimationSelector.getRandomLoaderResource());
        loaderAnimation.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        informationLayout = (LinearLayout) findViewById(R.id.information_layout);
        //Init like & selected btn
        likeBtn = (ImageButton) findViewById(R.id.recipe_like);
        likeCounter = (TextView) findViewById(R.id.recipe_like_count);
        favoriteBtn = (ImageButton) findViewById(R.id.recipe_add_to_favorites);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    public void onEditClick(View v) {
        Intent intent = new Intent(this, EditRecipeActivity.class);
        intent.putExtra("RecipeID", recipe.getID());
        startActivity(intent);
    }

    public void onDeleteClick(View v) {
        boolean complete = false;
        while(!complete) {
            try {
                CookBookStorage.getInstance(this).deleteRecipe(new RecipeToChange(recipe.getID(), "", ""));
                complete = true;
            }
            catch (Exception e) {
                //Repeat
            }
        }
        finish();
    }

    public void onLikeClick(View v) {
        if (liked) {
            likeBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.like_off));
            CookBookStorage.getInstance(this).setNotLike(recipe.getID());
        } else {
            likeBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.like_on));
            CookBookStorage.getInstance(this).setLike(recipe.getID());
        }
        liked = !liked;
        likeCounter.setText(String.valueOf(CookBookStorage.getInstance(this).getRecipeLikes(recipe)));
    }

    public void onFavoriteClick(View v) {
        if (inFavourites) {
            favoriteBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.favourite_off));
            CookBookStorage.getInstance(this).removeFromFavorites(recipe.getID());
        } else {
            favoriteBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.favourite_on));
            CookBookStorage.getInstance(this).addToFavorites(recipe);
        }
        inFavourites = !inFavourites;
    }
    private void showRecipe() {
        ////Header
        TextView nameView = (TextView)findViewById(R.id.recipe_header_name);
        LinearLayout categoryList = (LinearLayout)findViewById(R.id.recipe_header_tags);
        //Categories
        categoryList.removeAllViews();
        for (final Integer id : categories) {
            LinearLayout categoryNameLayout = new LinearLayout(this);
            categoryNameLayout.setPadding(dpToPix(CATEGORY_NAME_PADDING), 0, 0, 0);
            TextView categoryNameView = new TextView(this);
            //TextView categoryNameView = new TextView(this);
            categoryNameView.setBackgroundColor(0x00_00_00_00);
            categoryNameView.setTextAppearance(this, R.style.RecipeView_CategoryShowStyle); //I can't found another method
            categoryNameView.setText(categoryDescriptions.get(id));
            categoryNameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(RecipeViewActivity.this, CookBookCategoryActivity.class);
                    intent.putExtra("Category", id);
                    startActivity(intent);
                }
            });
            categoryNameLayout.addView(categoryNameView);
            categoryList.addView(categoryNameLayout);
        }

        //Body
        TextView descriptionView = (TextView)findViewById(R.id.recipe_body_description);
        TextView ingridientsView = (TextView)findViewById(R.id.recipe_body_ingredients);

        nameView.setText(recipe.getName());
        descriptionView.setText(recipe.getDescription());
        StringBuilder ingredientList = new StringBuilder();
        for (Ingredient i : ingredients) {
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

    private int dpToPix(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
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
                    informationLayout.setVisibility(View.INVISIBLE);
                }
            });
            final CookBookStorage cookbook = CookBookStorage.getInstance(RecipeViewActivity.this);
            boolean loadingComplete = false;
            while (!loadingComplete) {
                try {
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
                    ingredients = cookbook.getRecipeIngredients(recipeID);
                    likeCount = cookbook.getRecipeLikes(recipe);
                    categoryDescriptions = new HashMap<>();
                    for (Integer id : categories) {
                        categoryDescriptions.put(id, cookbook.getCategoryByID(id).getDescription());
                    }
                    loadingComplete = true;
                }
                catch (Throwable e) {
                    //Repeat
                }
            }
            final boolean userPermission = cookbook.isUserOwnRecipe(recipe);
            RecipeViewActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showRecipe();
                    //Check user permissions
                    deleteBtn = (ImageButton) findViewById(R.id.recipe_delete);
                    editBtn = (ImageButton) findViewById(R.id.recipe_edit);
                    if (userPermission) {
                        deleteBtn.setVisibility(View.INVISIBLE);
                        editBtn.setVisibility(View.INVISIBLE);
                    }
                    loaderAnimation.setVisibility(View.INVISIBLE);
                    informationLayout.setVisibility(View.VISIBLE);
                    loaderAnimation.setGifImageResource(LoaderAnimationSelector.getRandomLoaderResource());
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
                    likeCounter.setText(String.valueOf(likeCount));
                }
            });

            //Image loader
            final int widthInPx = dpToPix(IMAGES_WIDTH);
            final int heightInPx = dpToPix(IMAGES_HEIGHT);
            final LinearLayout recipeImages = (LinearLayout)findViewById(R.id.recipe_body_images);
            RecipeViewActivity.this.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            recipeImages.removeAllViews();
                        }
                    }
            );
            for (final Step s : steps) {
                cookbook.downloadStepImage(s);
                RecipeViewActivity.this.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                ImageView photo = new ImageView(RecipeViewActivity.this);
                                photo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                photo.setLayoutParams(new LinearLayout.LayoutParams(
                                        Math.min(widthInPx, s.getImage().getWidth()),
                                        Math.min(heightInPx, s.getImage().getHeight())));
                                photo.setImageBitmap(s.getImage());
                                recipeImages.addView(photo);
                            }
                        }
                );
            }
            RecipeViewActivity.this.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            ImageView photoView = (ImageView)findViewById(R.id.recipe_header_photo);
                            photoView.setImageBitmap(steps.get(steps.size()-1).getImage());
                        }
                    }
            );
        }
    }
}
