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
    private static CookBookStorage instance;
    private final String LOG_TAG = "CookBookStorageLogs";
    private SQLiteDatabase db;
    private Random r;

    /**
     * Загрузка базы данных.
     */
    private CookBookStorage(Context context) {
        DataBaseHelper helper = new DataBaseHelper(context);
        db = helper.openDatabase();
        r = new Random();
    }

    public CookBookStorage getInstance(Context context) {
        if (instance == null) {
            instance = new CookBookStorage(context);
        }

        return instance;
    }

    /**
     * Получение категорий, к которым принадлежит рецепт.
     * @param ID ID рецепта.
     */
    public ArrayList<Integer> getRecipeCategories(int ID) {
        Cursor categories = db.query("Recipe_to_category", new String[] {"category_ID"},
                                     "recipe_ID = ?", new String[] { String.valueOf(ID) },
                                     null, null, null);

        ArrayList<Integer> ids = new ArrayList<>();
        if (categories != null && categories.moveToFirst()) {
            do {
                int categoryID = categories.getInt(categories.getColumnIndex("category_ID"));
                ids.add(categoryID);

            } while (categories.moveToNext());

        } else {
            return null;
        }

        categories.close();
        return ids;
    }

    /**
     * Получение инструкции для готовки из двух таблиц -- Step и Image.
     * @param ID ID рецепта.
     */
    public ArrayList<Step> getRecipeSteps(int ID) {
        String stepsQuery = "SELECT * FROM Step INNER JOIN Image ON " +
                "Step.ID = Image.entity_ID " +
                "WHERE Step.recipe_ID = ? AND Image.entity_type = 0";
        Cursor steps = db.rawQuery(stepsQuery, new String[] {String.valueOf(ID)});

        ArrayList<Step> recipeSteps = new ArrayList<>();
        if (steps != null && steps.moveToFirst()) {
            do {
                String description = steps.getString(steps.getColumnIndex("description"));
                byte[] bytes = steps.getBlob(steps.getColumnIndex("source"));
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Step newStep = new Step(description, bm);
                recipeSteps.add(newStep);

            } while (steps.moveToNext());

        } else {
            return null;
        }

        steps.close();
        return recipeSteps;
    }

    /**
     * Получение ингредиентов из двух таблиц: Ingredient_to_recipe и Ingredient
     * @param ID ID рецепта.
     */
    public ArrayList<Ingredient> getRecipeIngredients(int ID) {
        String ingredientsQuery = "SELECT * FROM Ingredient_to_recipe itr " +
                                  "INNER JOIN Ingredient ing ON " +
                                  "itr.ingredient_ID = ing.ID " +
                                  "WHERE itr.recipe_ID = ?";
        Cursor ingredients = db.rawQuery(ingredientsQuery, new String[] {String.valueOf(ID)});

        ArrayList<Ingredient> recipeIngredients = new ArrayList<>();
        if (ingredients != null && ingredients.moveToFirst()) {
            do {
                String name = ingredients.getString(ingredients.getColumnIndex("name"));
                Measure measure = Measure.values()[
                        ingredients.getInt(ingredients.getColumnIndex("measure"))];
                double quantity = ingredients.getDouble(ingredients.getColumnIndex("quantity"));

                Ingredient ingredient = new Ingredient(name, measure, quantity);
                recipeIngredients.add(ingredient);
            } while (ingredients.moveToNext());

        } else {
            return null;
        }

        ingredients.close();
        return recipeIngredients;
    }

    /**
     * Получение рецепта по его уникальному идентификатору.
     */
    public Recipe getRecipe(int ID) {
        Recipe res = null;

        Cursor mainData = db.query("Recipe", new String[] {"name", "description"},
                "ID = ?", new String[] { String.valueOf(ID) }, null, null, null);

        if (mainData != null && mainData.moveToFirst()) {
            String recipeName = mainData.getString(mainData.getColumnIndex("name"));
            String recipeDescription = mainData.getString(mainData.getColumnIndex("description"));

            res = new Recipe(ID, recipeDescription, recipeName);
        } else {
            return null;
        }

        mainData.close();
        return res;
    }

    /**
     * Получение списка рецептов по фильтру, т.е. по префиксу.
     * TODO : оптимизировать.
     */
    public ArrayList<Recipe> getRecipiesByFilter(String filter) {
        String filterQuery = "SELECT * FROM Recipe WHERE name LIKE " + filter + "%";
        Cursor recipes = db.rawQuery(filterQuery, null);

        ArrayList<Recipe> res = new ArrayList<>();
        if (recipes != null && recipes.moveToFirst()) {
            do {
                int recipeID = recipes.getInt(recipes.getColumnIndex("ID"));
                res.add(getRecipe(recipeID));
            } while (recipes.moveToNext());

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

        return dishes.get(Math.abs(r.nextInt()) % dishes.size());
    }

    public ArrayList<Recipe> getRecipesOfCategory(int ID) {
        ArrayList<Recipe> res = new ArrayList<>();

        String categoryQuery = "SELECT * FROM Recipe_to_category WHERE category_ID = " + ID;
        Cursor cursor = db.rawQuery(categoryQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int recipeID = cursor.getInt(cursor.getColumnIndex("recipe_ID"));
                res.add(getRecipe(recipeID));
            } while (cursor.moveToNext());

        } else {
            return null;
        }

        return res;
    }

    public Category getCategoryByID(int ID) {
        Category c = null;

        String categoryQuery = "SELECT * FROM Category WHERE ID = " + ID;
        Cursor categoryCursor = db.rawQuery(categoryQuery, null);

        if (categoryCursor != null && categoryCursor.moveToFirst()) {
            String description = categoryCursor.getString(
                                 categoryCursor.getColumnIndex("name"));

            c = new Category(ID, description, this, null); // пока картинок категорий у нас нет
        } else {
            return null;
        }

        categoryCursor.close();
        return c;
    }

    public LinkedList<Category> getRecipiesTypeOfDish() {
        LinkedList<Category> categories = new LinkedList<>();

        for (int order = CategoryName.entree.ordinal();
             order < CategoryName.dinner.ordinal(); order++) {
            categories.add(getCategoryByID(order));
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