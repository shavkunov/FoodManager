package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Хранилище всех рецептов.
 */
public class CookBookStorage {
    private final String LOG_TAG = "CookBookStorageLogs";
    private SQLiteDatabase db;

    /**
     * Загрузка базы данных.
     */
    public CookBookStorage(Context context) {
        DataBaseHelper helper = new DataBaseHelper(context);

        db = helper.openDatabase();
    }

    public void close() {
        db.close();
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    /**
     * Получение рецепта по его уникальному идентификатору.
     */
    public Recipe getRecipe(int ID) {
        Recipe res = new Recipe();

        /**
         * Получение имени и описание рецепты из таблицы Recipe.
         */
        Cursor mainData = db.query("Recipe", new String[] {"name", "description"},
                "ID = ?", new String[] { String.valueOf(ID) }, null, null, null);

        if (mainData != null) {
            if (mainData.moveToFirst()) {
                String recipeName = mainData.getString(mainData.getColumnIndex("name"));
                String recipeDescription = mainData.getString(mainData.getColumnIndex("description"));
                Log.d(LOG_TAG, "recipeName = " + recipeName);
                Log.d(LOG_TAG, "recipeDescription = " + recipeDescription);

                res.setName(recipeName);
                res.setDescription(recipeDescription);
                res.setID(ID);
            } else {

                Log.d(LOG_TAG, "mainData.moveToFirst() = null");
                return null;
            }
        } else {

            Log.d(LOG_TAG, "mainData = null");
            return null;
        }

        mainData.close();

        /**
         * Получение категорий, к которым принадлежит рецепт.
         */
        Cursor categories = db.query("Recipe_to_category", new String[] {"category_ID"},
                                     "recipe_ID = ?", new String[] { String.valueOf(ID) },
                                      null, null, null);

        if (categories != null) {
            if (categories.moveToFirst()) {
                ArrayList<Integer> ids = new ArrayList<>();

                do {
                    int categoryID = categories.getInt(categories.getColumnIndex("category_ID"));
                    ids.add(categoryID);
                    Log.d(LOG_TAG, "categoryID = " + categoryID);

                } while (categories.moveToNext());

                res.setCategoryID(ids);
            } else {
                return null;
            }
        } else {
            return null;
        }

        categories.close();

        /**
         * Получение инструкции для готовки из двух таблиц -- Step и Image.
         */
        String stepsQuery = "SELECT * FROM Step INNER JOIN Image ON " +
                            "Step.recipe_ID = Image.entity_ID " +
                            "WHERE Step.recipe_ID = ? AND Image.entity_type = 0";
        Cursor steps = db.rawQuery(stepsQuery, new String[] {String.valueOf(ID)});

        ArrayList<Step> recipeSteps = new ArrayList<>();
        if (steps != null) {
            if (steps.moveToFirst()) {
                do {

                    String description = steps.getString(steps.getColumnIndex("description"));
                    byte[] bytes = steps.getBlob(steps.getColumnIndex("source"));
                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Step newStep = new Step(description, bm);
                    recipeSteps.add(newStep);
                    Log.d(LOG_TAG, "step description = " + description);

                } while (steps.moveToNext());

            } else {
                return null;
            }
        } else {
            return null;
        }

        steps.close();
        res.setStepByStep(recipeSteps);

        /**
         * Получение ингредиентов из двух таблиц: Ingredient_to_recipe и Ingredient
         */
        String ingredientsQuery = "SELECT * FROM Ingredient_to_recipe itr " +
                                  "INNER JOIN Ingredient ing ON " +
                                  "itr.ingredient_ID = ing.ID " +
                                  "WHERE itr.recipe_ID = ?";
        Cursor ingredients = db.rawQuery(ingredientsQuery, new String[] {String.valueOf(ID)});

        ArrayList<Ingredient> recipeIngredients = new ArrayList<>();
        if (ingredients != null) {
            if (ingredients.moveToFirst()) {

                do {
                    String name = ingredients.getString(ingredients.getColumnIndex("name"));
                    Measure measure = Measure.values()[
                            ingredients.getInt(ingredients.getColumnIndex("measure"))];
                    double quantity = ingredients.getDouble(ingredients.getColumnIndex("quantity"));

                    Log.d(LOG_TAG, "Ingredient name = " + name);
                    Log.d(LOG_TAG, "Ingredient measure = " + measure.name());
                    Log.d(LOG_TAG, "Ingredient quantity = " + quantity);

                    Ingredient ingredient = new Ingredient(name, measure, quantity);
                    recipeIngredients.add(ingredient);
                } while (ingredients.moveToNext());

            } else {
                return null;
            }
        } else {
            return null;
        }

        res.setIngredients(recipeIngredients);

        return res;
    }

    /**
     * Получение списка рецептов по фильтру, т.е. по префиксу.
     */
    public ArrayList<Recipe> getRecipiesByFilter(String filter) {
        String filterQuery = "SELECT * FROM Recipe WHERE name LIKE " + filter + "%";
        Cursor recipes = db.rawQuery(filterQuery, null);

        ArrayList<Recipe> res = new ArrayList<>();
        if (recipes != null) {
            if (recipes.moveToFirst()) {
                do {
                    int recipeID = recipes.getInt(recipes.getColumnIndex("ID"));
                    res.add(getRecipe(recipeID));
                } while (recipes.moveToNext());

            } else {
                return null;
            }
        } else {
            return null;
        }

        return res;
    }

    /**
     * Выбор случайного блюда категории.
     */
    public Recipe chooseRandomDishFromCategory(CategoryName category) {
        ArrayList<Recipe> dishes = getRecipesOfCategory(category.ordinal());
        Random r = new Random();

        return dishes.get(Math.abs(r.nextInt()) % dishes.size());
    }

    public ArrayList<Recipe> getRecipesOfCategory(int ID) {
        ArrayList<Recipe> res = new ArrayList<>();

        String categoryQuery = "SELECT * FROM Recipe_to_category WHERE category_ID = " + ID;
        Cursor cursor = db.rawQuery(categoryQuery, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int recipeID = cursor.getInt(cursor.getColumnIndex("recipe_ID"));
                    res.add(getRecipe(recipeID));
                } while (cursor.moveToNext());

            } else {
                return null;
            }
        } else {
            return null;
        }

        return res;
    }

    public Category getCategoryByID(int ID) {
        Category c = new Category();

        String categoryQuery = "SELECT * FROM Category WHERE ID = " + ID;
        Cursor categoryCursor = db.rawQuery(categoryQuery, null);

        if (categoryCursor != null) {
            if (categoryCursor.moveToFirst()) {
                String description = categoryCursor.getString(
                                     categoryCursor.getColumnIndex("name"));

                Log.d(LOG_TAG, description);
                c.setDescription(description);
            } else {
                return null;
            }
        } else {
            return null;
        }
        categoryCursor.close();

        c.setID(ID);
        c.setRecipes(getRecipesOfCategory(ID));
        c.setCategoryImage(null); // пока картинок у нас никаких нет
        return c;
    }

    public LinkedList<Category> getRecipiesTypeOfDish() {
        LinkedList<Category> categories = new LinkedList<>();

        for (int order = CategoryName.entree.ordinal();
             order < CategoryName.dinner.ordinal(); order++) {
            categories.add(getCategoryByID(order));
            Log.d(LOG_TAG, "Order: " + order);
        }

        return categories;
    }

    public LinkedList<Category> getRecipiesNationalKitchen() {
        LinkedList<Category> categories = new LinkedList<>();

        for (int order = CategoryName.European.ordinal();
             order < CategoryName.values().length; order++) {
            categories.add(getCategoryByID(order));
        }

        return categories;
    }
}