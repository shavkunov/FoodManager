package ru.spbau.mit.foodmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class StepViewActivity extends AppCompatActivity {
    private Recipe recipe;
    private ArrayList<Step> steps;
    private Integer stepPosition = 0;
    private GifImageView loaderAnimation;
    private LinearLayout informationLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.step_view);
        //Init loaderAnimation
        loaderAnimation = (GifImageView) findViewById(R.id.loader_animation_view);
        loaderAnimation.setGifImageResource(MainActivity.getRandomLoaderResource());
        loaderAnimation.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        informationLayout = (LinearLayout) findViewById(R.id.information_layout);
        //Init Task
        Intent i = getIntent();
        ContentLoader contentLoader = new ContentLoader(i.getIntExtra("Recipe", -1));
        Thread loader = new Thread(contentLoader);
        loader.start();

    }

    private void showRecipe() {
        TextView recipeName = (TextView)findViewById(R.id.step_view_recipe_name);
        recipeName.setText(recipe.getName());
        TextView stepCount = (TextView)findViewById(R.id.step_view_step_count);
        stepCount.setText(((Integer)steps.size()).toString());
        showStep(stepPosition);
    }

    private void showStep(int id) {
        TextView description = (TextView)findViewById(R.id.step_view_step_text);
        description.setText(steps.get(id).getDescription());
        ImageView image = (ImageView)findViewById(R.id.step_view_step_image);
        image.setImageBitmap(steps.get(id).getImage());
        TextView counter = (TextView)findViewById(R.id.step_view_position);
        counter.setText(((Integer)(id+1)).toString());
    }

    public void onNextBtnClick(View v) {
        if (stepPosition < steps.size() - 1) {
            stepPosition++;
            showStep(stepPosition);
        }
    }

    public void onPrevBtnClick(View v) {
        if (stepPosition > 0) {
            stepPosition--;
            showStep(stepPosition);
        }
    }
    public class ContentLoader implements Runnable {
        private int recipeID;
        public ContentLoader(int id) {
            this.recipeID = id;
        }

        @Override
        public void run() {
            StepViewActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loaderAnimation.setVisibility(View.VISIBLE);
                    informationLayout.setVisibility(View.INVISIBLE);
                }
            });
            steps = null;
            while (steps == null) {
                try {
                    CookBookStorage cookbook = CookBookStorage.getInstance(StepViewActivity.this);
                    recipe = cookbook.getRecipe(recipeID);
                    steps = cookbook.getRecipeSteps(recipe.getID());
                }
                catch (Throwable e) {
                    //Repeat
                }
            }
            StepViewActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showRecipe();
                    loaderAnimation.setVisibility(View.INVISIBLE);
                    informationLayout.setVisibility(View.VISIBLE);
                    loaderAnimation.setGifImageResource(MainActivity.getRandomLoaderResource());
                }
            });
        }
    }
}
