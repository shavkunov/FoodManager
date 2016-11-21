package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Хранилище всех рецептов.
 */
public class CookBookStorage {
    final String LOG_TAG = "myLogs";

    private DataBaseHelper helper;
    private SQLiteDatabase db;

    /**
     * Загрузка базы данных.
     */
    public CookBookStorage(Context context) {
        helper = new DataBaseHelper(context);

        db = helper.openDatabase();
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
        // replace this crap with inner join
        Cursor steps = db.query("Step", new String[] {"ID", "description"}, "recipe_ID = ?",
                                new String[] { String.valueOf(ID) }, null, null, null);

        ArrayList<Step> recipeSteps = new ArrayList<>();
        if (steps != null) {
            if (steps.moveToFirst()) {
                do {
                    int id = steps.getInt(steps.getColumnIndex("ID"));
                    String description = steps.getString(steps.getColumnIndex("description"));

                    Cursor image = db.query("Image", new String[] {"source"},
                                            "entity_type = ? AND entity_ID = ?",
                                            new String[] {"0", String.valueOf(id)},
                                            null, null, null);

                    if (image != null) {
                        if (image.moveToFirst()) {
                            byte[] bytes = image.getBlob(image.getColumnIndex("source"));
                            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            Step newStep = new Step(description, bm);
                            recipeSteps.add(newStep);
                            Log.d(LOG_TAG, "step description = " + description);

                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }

                    image.close();

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
        // query to data base
        return null;
    }

    /**
     * Выбор случайного ужина из базы данных.
     */
    public static Recipe chooseRandomDinner() {
        Random r = new Random();
        // запрос на r-ую строку по тегу dinner, т.е. r-ый рецепт, в базе данных.

        Recipe res = null;
        return null;
    }

    /**
     * Выбор случайного обеда из базы данных.
     */
    public static Recipe chooseRandomLunch() {
        Random r = new Random();
        // запрос на r-ую строку по тегу Lunch, т.е. r-ый рецепт, в базе данных.

        Recipe res = null;
        return null;
    }

    /**
     * Выбор случайного завтрака из базы данных.
     */
    public static Recipe chooseRandomBreakfast() {
        Random r = new Random();
        // запрос на r-ую строку по тегу Breakfast, т.е. r-ый рецепт, в базе данных.

        Recipe res = null;
        return null;
    }

    public static ArrayList<Recipe> getRecipesOfCategory(int ID) {
        ArrayList<Recipe> res = null;
        // запрос в базу данных для блюд у которых есть ID

        return res;
    }

    public static LinkedList<Category> getRecipiesTypeOfDish() {
        LinkedList<Category> categories = new LinkedList<>();

        for (int order = CategoryName.firstDish.ordinal(); order < CategoryName.dinner.ordinal(); order++) {
            categories.add(new Category(order));
        }

        return categories;
    }

    public static LinkedList<Category> getRecipiesNationalKitchen() {
        LinkedList<Category> categories = new LinkedList<>();

        for (int order = CategoryName.European.ordinal(); order < CategoryName.values().length; order++) {
            categories.add(new Category(order));
        }

        return categories;
    }
}