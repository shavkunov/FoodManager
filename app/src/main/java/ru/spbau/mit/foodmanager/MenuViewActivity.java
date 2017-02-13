package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

public class MenuViewActivity extends AppCompatActivity {
    private final static int REQUEST_ADD = 0;
    private final static int REQUEST_EDIT = 1;
    private HashMap<Day, DayMenu> allDayMenu;
    private HashMap<Integer, Recipe> recipes;
    private CookBookStorage cookbook;
    private MenuStorage menu;
    private LayoutInflater inflater;
    private GifImageView loaderAnimation;
    private LinearLayout informationLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_view);
        //Init loaderAnimation
        loaderAnimation = (GifImageView) findViewById(R.id.loader_animation_view);
        loaderAnimation.setGifImageResource(loaderAnimationSelector.getRandomLoaderResource());
        loaderAnimation.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        informationLayout = (LinearLayout) findViewById(R.id.information_layout);

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        cookbook = CookBookStorage.getInstance(this);
        menu = MenuStorage.getInstance(this);
        recipes = new HashMap<>();
        //Init Task
        ContentLoader contentLoader = new ContentLoader(false);
        Thread loader = new Thread(contentLoader);
        loader.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MenuStorage.saveMenuStorage(this);
    }

    public void onGenerateBtnClick(View v) {
        //Init Task
        ContentLoader contentLoader = new ContentLoader(true);
        Thread loader = new Thread(contentLoader);
        loader.start();
    }

    public void onActivityResult(int requestCode, int errorCode, Intent resultContainer) {
        if (errorCode == RESULT_OK) {
            Day day = (Day) resultContainer.getSerializableExtra("Day");
            Integer mealtimePos = resultContainer.getIntExtra("MealtimePosition", -1);
            Integer result = resultContainer.getIntExtra("Result", -1);
            switch (requestCode) {
                case REQUEST_ADD :
                    allDayMenu.get(day).getMealtimes().get(mealtimePos).getRecipeIDs().add(result);
                    //TODO upload recipe
                    showRecipes();
                    break;
                case REQUEST_EDIT :
                    Integer position = resultContainer.getIntExtra("Position", -1);
                    allDayMenu.get(day).getMealtimes().get(mealtimePos).getRecipeIDs().set(position, result);
                    //TODO upload recipe
                    showRecipes();
                    break;
            }
        }
    }

    private void showRecipes() {
        LinearLayout daysListView = (LinearLayout) findViewById(R.id.menu_view_days);
        daysListView.removeAllViews();
        for (Day d : Day.values()) {
            generateDayMenuView(d, daysListView);
        }
    }

    private void generateDayMenuView(Day day, LinearLayout parent) {
        View view = inflater.inflate(R.layout.menu_view_list_element, null);
        DayMenu menu = allDayMenu.get(day);
        TextView dayName = (TextView) view.findViewById(R.id.menu_day_name);
        LinearLayout dayMealtimes = (LinearLayout) view.findViewById(R.id.menu_day_mealtimes);

        if (menu != null) {
            dayName.setText(MenuStorage.getDayNames()[day.ordinal()]);
            for (Integer i = 0; i < menu.getMealtimes().size(); i++) {
                generateMealtimeView(menu, i, dayMealtimes, day);
            }
            parent.addView(view);
        }
    }

    private void generateMealtimeView(DayMenu menu, final Integer position, LinearLayout parent,
                                      final Day day) {
        LinearLayout mealtimeElement = (LinearLayout) inflater.inflate(
                R.layout.menu_view_day_menu_list_element, null);
        TextView mealtimeName = (TextView) mealtimeElement.findViewById(
                R.id.menu_day_mealtime_name);
        LinearLayout recipeList = (LinearLayout) mealtimeElement.findViewById(
                R.id.menu_day_mealtime_dishes);
        Mealtime mealtime = menu.getMealtimes().get(position);
        ImageButton addDish = (ImageButton) mealtimeElement.findViewById(R.id.menu_day_mealtime_dish_add);


        mealtimeName.setText(mealtime.getName());
        for (Integer i = 0; i < mealtime.getRecipeIDs().size(); i++) {
            generateRecipeView(mealtime, i, recipeList, day, position);
        }
        addDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent task = new Intent(MenuViewActivity.this, CookBookActivity.class);
                task.putExtra("Target", CookBookActivity.TARGET_RECIPE);
                task.putExtra("Day", day);
                task.putExtra("MealtimePosition", position);
                startActivityForResult(task, REQUEST_ADD);
            }
        });
        parent.addView(mealtimeElement);
    }

    private void generateRecipeView(final Mealtime mealtime, final Integer position, LinearLayout parent,
                                    final Day day, final Integer mealtimePos) {
        View recipeView = inflater.inflate(R.layout.menu_view_day_menu_meltime_list_element, null);
        TextView recipeName = (TextView) recipeView.findViewById(R.id.menu_day_mealtime_dish_name);
        final Integer recipeID = mealtime.getRecipeIDs().get(position); //WTF
        ImageButton editDish = (ImageButton) recipeView.findViewById(R.id.menu_day_mealtime_dish_edit);
        ImageButton deleteDish = (ImageButton) recipeView.findViewById(R.id.menu_day_mealtime_dish_delete);

        recipeName.setText(recipes.get(recipeID).getName());
        recipeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuViewActivity.this, RecipeViewActivity.class);
                intent.putExtra("Recipe", recipeID);
                startActivity(intent);
            }
        });
        deleteDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allDayMenu.get(day).getMealtimes().get(mealtimePos).getRecipeIDs().remove((int)position);
                Log.d("Try to remove", "Try tot remove");
                showRecipes(); //We need to do this because positions in other buttons now broken
            }
        });
        editDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent task = new Intent(MenuViewActivity.this, CookBookActivity.class);
                task.putExtra("Target", CookBookActivity.TARGET_RECIPE);
                task.putExtra("Day", day);
                task.putExtra("MealtimePosition", mealtimePos);
                task.putExtra("Position", position);
                startActivityForResult(task, REQUEST_EDIT);
            }
        });
        parent.addView(recipeView);
    }

    public class ContentLoader implements Runnable {
        private boolean generateNewMenu;

        public ContentLoader(boolean generate) {
            generateNewMenu = generate;
        }

        @Override
        public void run() {
            MenuViewActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loaderAnimation.setVisibility(View.VISIBLE);
                    informationLayout.setVisibility(View.INVISIBLE);
                }
            });
            if (generateNewMenu) {
                menu.generateWeekMenu(MenuViewActivity.this);
            }
            allDayMenu = menu.getMenu();
            for (DayMenu dm : allDayMenu.values()) {
                if (dm != null) {
                    for (Mealtime m : dm.getMealtimes()) {
                        for (Integer id : m.getRecipeIDs()) {
                            if (recipes.get(id) == null) {
                                recipes.put(id, cookbook.getRecipe(id));
                            }
                        }
                    }
                }
            }

            MenuViewActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showRecipes();
                    loaderAnimation.setVisibility(View.INVISIBLE);
                    informationLayout.setVisibility(View.VISIBLE);
                    loaderAnimation.setGifImageResource(loaderAnimationSelector.getRandomLoaderResource());
                }
            });
        }
    }
}
