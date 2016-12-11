package ru.spbau.mit.foodmanager;

import android.graphics.Bitmap;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Хранилище всех рецептов.
 */
public class CookBookStorage {
    private static CookBookStorage instance;
    private final String LOG_TAG = "CookBookStorageLogs";

    private final String url = "jdbc:mysql://mysql1.gear.host:3306/foodmanagertest";
    private String user = "foodmanagertest";
    private final String password = "Wc22_0f_0TA2";
    Connection connection;

    private CookBookStorage() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            Log.d(LOG_TAG, "Problems with connection");
        }
    }

    public static CookBookStorage getInstance() {
        if (instance == null) {
            instance = new CookBookStorage();
        }

        return instance;
    }

    public void addRecipeToDatabase(Recipe recipe) {
        try {
            connection.setAutoCommit(false);
            Statement stmt = connection.createStatement();

            insertRecipeCategories(stmt, recipe);

            stmt.close();
            connection.setAutoCommit(true);

        } catch (SQLException e) {
            Log.d(LOG_TAG, "Unable to insert new recipe");
            e.printStackTrace();
        }
    }

    private void insertRecipeCategories(Statement stmt, Recipe recipe) throws SQLException {
        ArrayList<Integer> categoryIDs = recipe.getCategoryID();
        for (int categoryID : categoryIDs) {
            String insertCategoryQuery = "INSERT INTO Recipe_to_category (recipe_ID, category_ID) "
                                       + "VALUES (" + recipe.getID() + ", " + categoryID + ")";

            stmt.executeUpdate(insertCategoryQuery);
        }
    }

    /**
     * Получение категорий, к которым принадлежит рецепт.
     * @param ID ID рецепта.
     */
    public ArrayList<Integer> getRecipeCategories(int ID) {
        String categoriesQuery = "SELECT category_ID FROM Recipe_to_category " +
                                 "WHERE recipe_ID = " + ID;

        try {
            Statement stmt = connection.createStatement();
            ResultSet categories = stmt.executeQuery(categoriesQuery);
            ArrayList<Integer> ids = new ArrayList<>();

            while (categories.next()) {
                ids.add(categories.getInt("category_ID"));
            }

            stmt.close();
        } catch (SQLException e) {
            Log.d(LOG_TAG, "Unable to get categories of recipe");
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
            Statement stmt = connection.createStatement();
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
            Log.d(LOG_TAG, "Unable to get recipe steps");
            e.printStackTrace();
        }

        return null;
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
            Statement stmt = connection.createStatement();
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
            Log.d(LOG_TAG, "Unable to get recipe ingredients");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Получение рецепта по его уникальному идентификатору.
     */
    public Recipe getRecipe(int ID) {
        String recipeQuery = "SELECT name, description FROM Recipe WHERE ID = " + ID;

        try {
            Statement stmt = connection.createStatement();
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
            Log.d(LOG_TAG, "Unable to get recipe main information");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Получение списка рецептов по фильтру, т.е. по префиксу.
     */
    public ArrayList<Recipe> getRecipesByFilter(String filter) {
        String filterQuery = "SELECT * FROM Recipe WHERE name LIKE " + filter + "%";
        try {
            Statement stmt = connection.createStatement();
            ResultSet recipes = stmt.executeQuery(filterQuery);

            ArrayList<Recipe> res = new ArrayList<>();
            while (recipes.next()) {
                res.add(getRecipe(recipes.getInt("ID")));
            }

            stmt.close();
            return res;
        } catch (SQLException e) {
            Log.d(LOG_TAG, "Unable to filter recipes");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Выбор случайного блюда категории.
     */
    public Recipe chooseRandomDishFromCategory(int categoryID) {
        String getRandomRecipeQuery = "SELECT recipe_ID FROM Recipe_to_category " +
                                      "WHERE category_ID = " + categoryID +
                                      " ORDER BY RAND() LIMIT 1";

        try {
            Statement stmt = connection.createStatement();
            ResultSet recipe = stmt.executeQuery(getRandomRecipeQuery);

            int recipeID = 0;
            if (recipe.next()) {
                recipeID = recipe.getInt("recipe_ID");
            }

            stmt.close();
            return getRecipe(recipeID);
        } catch (SQLException e) {
            Log.d(LOG_TAG, "Unable to get random dish");
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Recipe> getRecipesOfCategory(int ID) {
        String categoryQuery = "SELECT * FROM Recipe_to_category WHERE category_ID = " + ID;
        try {
            Statement stmt = connection.createStatement();
            ResultSet recipes = stmt.executeQuery(categoryQuery);

            ArrayList<Recipe> res = new ArrayList<>();
            while (recipes.next()) {
                res.add(getRecipe(recipes.getInt("recipe_ID")));
            }

            stmt.close();
            return res;
        } catch (SQLException e) {
            Log.d(LOG_TAG, "Unable to get recipes of category");
            e.printStackTrace();
        }

        return null;
    }

    public Category getCategoryByID(int ID) {
        String categoryQuery = "SELECT * FROM Category WHERE ID = " + ID;

        try {
            Statement stmt = connection.createStatement();
            ResultSet category = stmt.executeQuery(categoryQuery);
            if (category.next()) {
                String description = category.getString("name");
                // пока картинок категорий у нас нет
                Category c =  new Category(ID, description, null);
                stmt.close();
                return c;
            } else {
                stmt.close();
                return null;
            }

        } catch (SQLException e) {
            Log.d(LOG_TAG, "Unable to get category");
            e.printStackTrace();
        }

        return null;
    }

    private LinkedList<Category> getCategoryFromQuery(String categoryQuery) {
        LinkedList<Category> categories = new LinkedList<>();

        try {
            Statement stmt = connection.createStatement();
            ResultSet category = stmt.executeQuery(categoryQuery);

            while (category.next()) {
                categories.add(getCategoryByID(category.getInt("ID")));
            }

            stmt.close();
            return categories;
        } catch (SQLException e) {
            Log.d(LOG_TAG, "Unable to get categories");
            e.printStackTrace();
        }

        return null;
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