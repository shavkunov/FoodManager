package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Хранилище всех рецептов.
 */
public class CookBookStorage {
    private static CookBookStorage instance;
    private final String LOG_TAG = "CookBookStorageLogs";
    private Random r;

    private final String url = "jdbc:mysql://mysql1.gear.host:3306/foodmanagertest";
    private String user = "foodmanagertest";
    private final String password = "Wc22_0f_0TA2";
    Connection c;

    private CookBookStorage(Context context) {
        try {
            c = DriverManager.getConnection(url, user, password);
            c.setAutoCommit(false);
        } catch (SQLException e) {
            System.out.println("Problems with connection");
        }
        r = new Random();
    }

    public static CookBookStorage getInstance(Context context) {
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
        String categoriesQuery = "SELECT category_ID FROM Recipe_to_category " +
                                 "WHERE recipe_ID = " + ID;

        try {
            Statement stmt = c.createStatement();
            ResultSet categories = stmt.executeQuery(categoriesQuery);
            ArrayList<Integer> ids = new ArrayList<>();

            while (categories.next()) {
                ids.add(categories.getInt("category_ID"));
            }

            stmt.close();
        } catch (SQLException e) {
            System.out.println("Unable to get categories of recipe");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Получение инструкции для готовки из двух таблиц -- Step и Image.
     * @param ID ID рецепта.
     */
    public ArrayList<Step> getRecipeSteps(int ID) {
        String stepsQuery = "SELECT * FROM Step INNER JOIN Image ON " +
                            "Step.ID = Image.entity_ID " +
                            "WHERE Step.recipe_ID = " + ID + " AND Image.entity_type = 0";
        try {
            Statement stmt = c.createStatement();
            ResultSet steps = stmt.executeQuery(stepsQuery);
            ArrayList<Step> recipeSteps = new ArrayList<>();

            while (steps.next()) {
                String stepDescription = steps.getString("description");
                String imageID = steps.getString("drive_ID");

                // TODO : load from google drive
                Bitmap bm = null;
                recipeSteps.add(new Step(stepDescription, bm));
            }

            stmt.close();
            return recipeSteps;

        } catch (SQLException e) {
            System.out.println("Unable to get recipe steps");
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Получение ингредиентов из двух таблиц: Ingredient_to_recipe и Ingredient
     * @param ID ID рецепта.
     */
    public ArrayList<Ingredient> getRecipeIngredients(int ID) {
        String ingredientsQuery = "SELECT * FROM Ingredient_to_recipe AS itr " +
                                  "INNER JOIN Ingredient AS ing ON " +
                                  "itr.ingredient_ID = ing.ID " +
                                  "WHERE itr.recipe_ID = " + ID;
        try {
            Statement stmt = c.createStatement();
            ResultSet ingredients = stmt.executeQuery(ingredientsQuery);
            ArrayList<Ingredient> recipeIngredients = new ArrayList<>();

            while (ingredients.next()) {
                String name = ingredients.getString("name");
                Measure measure = Measure.values()[ingredients.getInt("measure")];
                double quantity = ingredients.getDouble("quantity");

                recipeIngredients.add(new Ingredient(name, measure, quantity));
            }

            stmt.close();
            return recipeIngredients;
        } catch (SQLException e) {
            System.out.println("Unable to get recipe ingredients");
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Получение рецепта по его уникальному идентификатору.
     */
    public Recipe getRecipe(int ID) {
        String recipeQuery = "SELECT name, description FROM Recipe WHERE ID = " + ID;

        try {
            Statement stmt = c.createStatement();
            ResultSet mainData = stmt.executeQuery(recipeQuery);

            String recipeName = null;
            String recipeDescription = null;
            if (mainData.next()) {
                recipeName = mainData.getString("name");
                recipeDescription = mainData.getString("description");
            } else {
                return null;
            }

            stmt.close();
            return new Recipe(ID, recipeDescription, recipeName);

        } catch (SQLException e) {
            System.out.println("Unable to get recipe main information");
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Получение списка рецептов по фильтру, т.е. по префиксу.
     */
    public ArrayList<Recipe> getRecipesByFilter(String filter) {
        String filterQuery = "SELECT * FROM Recipe WHERE name LIKE " + filter + "%";
        try {
            Statement stmt = c.createStatement();
            ResultSet recipes = stmt.executeQuery(filterQuery);

            ArrayList<Recipe> res = new ArrayList<>();
            while (recipes.next()) {
                res.add(getRecipe(recipes.getInt("ID")));
            }

            stmt.close();
            return res;
        } catch (SQLException e) {
            System.out.println("Unable to filter recipes");
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Выбор случайного блюда категории.
     */
    public Recipe chooseRandomDishFromCategory(int categoryID) {
        ArrayList<Recipe> dishes = getRecipesOfCategory(categoryID);

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

    private LinkedList<Category> getCategoryFromQuery(String categoryQuery) {
        LinkedList<Category> categories = new LinkedList<>();

        Cursor cursor = db.rawQuery(categoryQuery, null);

        while (cursor.moveToNext()) {
            int categoryID = cursor.getInt(cursor.getColumnIndex("ID"));
            categories.add(getCategoryByID(categoryID));
        }

        return categories;
    }

    public LinkedList<Category> getRecipiesTypeOfDish() {
        String categoryQuery = "SELECT ID FROM Category WHERE is_category_dish = 1";

        return getCategoryFromQuery(categoryQuery);
    }

    public LinkedList<Category> getRecipiesNationalKitchen() {
        String categoryQuery = "SELECT ID FROM Category WHERE is_national_kitchen = 1";

        return getCategoryFromQuery(categoryQuery);
    }
}