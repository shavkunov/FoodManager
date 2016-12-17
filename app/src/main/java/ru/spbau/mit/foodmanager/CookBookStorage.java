package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
    private Connection connection;

    private CookBookStorage() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Problems with connection");
        }
    }

    public static CookBookStorage getInstance() {
        if (instance == null) {
            instance = new CookBookStorage();
        }

        return instance;
    }

    /**
     * Добавление рецепта в базу данных на сервере. Если одна из операций вставок провалилась,
     * то рецепт не будет встален полностью.
     * @param recipe добавление рецепта в базу данных на сервере.
     */
    public void addRecipeToDatabase(Recipe recipe) {
        try {
            connection.setAutoCommit(false);
            Statement stmt = connection.createStatement();

            int recipeID = insertRecipe(stmt, recipe);
            recipe.setID(recipeID);
            insertRecipeCategories(stmt, recipe);
            ArrayList<Integer> ingredientIDs = insertIngredients(stmt, recipe);
            insertRecipeIngredientRelation(stmt, recipe, ingredientIDs);
            ArrayList<Integer> stepIDs = insertSteps(stmt, recipe);
            insertImageStepRelation(stepIDs);

            stmt.close();
            connection.setAutoCommit(true);

        } catch (SQLException e) {
            Log.d(LOG_TAG, "Unable to insert new recipe");
            e.printStackTrace();
        }
    }

    /**
     * Вставка в таблицу Image изображений из инструкции приготовления блюда.
     * @param ids идентификаторы картинок загруженные в insertSteps.
     */
    private void insertImageStepRelation(ArrayList<Integer> ids) throws SQLException {

        for (int stepID : ids) {
            String insertRelation = "INSERT INTO Image VALUES (?, ?, ?)";
            String driveID = null; // TODO : upload to google drive
            PreparedStatement preparedStatement = connection.prepareStatement(insertRelation);
            preparedStatement.setInt(1, 0);
            preparedStatement.setInt(2, stepID);
            preparedStatement.setString(3, driveID);
            preparedStatement.execute();
        }
    }

    /**
     * Загрузка описаний шагов в инструкции готовки.
     * @param stmt Statement в котором выполняется операция.
     * @param recipe рецепт, откуда берутся шагов.
     * @return Идентификаторы строк, куда были вставлены шаги.
     */
    private ArrayList<Integer> insertSteps(Statement stmt, Recipe recipe) throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        for (Step s : recipe.getSteps()) {
            String insertStep = "INSERT INTO Step VALUES (recipe_ID, description) (" +
                                recipe.getID() + ", '" + s.getDescription() + "')";

            ids.add(stmt.executeUpdate(insertStep));
        }

        return ids;
    }

    /**
     * Вставка нужных записей в таблицу связи Ingredient_to_recipe.
     * @param stmt Statement в котором выполняется операция.
     * @param recipe рецепт, откуда берутся шагов.
     * @param ingredientIDs идентификаторы строке, где хранятся ингредиенты.
     */
    private void insertRecipeIngredientRelation(
            Statement stmt, Recipe recipe, ArrayList<Integer> ingredientIDs) throws SQLException {

        // ingredients.size() == ingredientIDs.size()
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            double quantity = recipe.getIngredients().get(i).getQuantity();
            int measureOrdinal = recipe.getIngredients().get(i).getMeasure().ordinal();
            String insertRelationQuery = "INSERT INTO Ingredient_to_recipe " +
                                         "(recipe_ID, measure, quantity) VALUES " +
                                         "(" + ingredientIDs.get(i) + ", " + recipe.getID() +
                                         ", " + measureOrdinal + ", " + quantity + ")";

            stmt.executeUpdate(insertRelationQuery);
        }
    }

    /**
     * Вставка ингредиентов в таблицу Ingredient.
     * @param stmt Statement в котором выполняется операция.
     * @param recipe рецепт, откуда берутся шагов.
     * @return идентификаторы строке, где хранятся ингредиенты.
     */
    private ArrayList<Integer> insertIngredients(Statement stmt, Recipe recipe)
            throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();
        for (Ingredient ing : recipe.getIngredients()) {
            String insertIngredientQuery = "INSERT INTO Ingredient (recipe_ID, description " +
                                           "VALUES (" + recipe.getID() + ", '"
                                           + ing.getName() + "')";

            ids.add(stmt.executeUpdate(insertIngredientQuery));
        }

        return ids;
    }

    /**
     * Вставка рецепта в таблицу Recipe.
     * @param stmt Statement в котором выполняется операция.
     * @param recipe рецепт, откуда берутся шагов.
     * @return номер строки, куда был вставлен рецепт.
     */
    private int insertRecipe(Statement stmt, Recipe recipe) throws SQLException {
        String insertRecipeQuery = "INSERT INTO Recipe(name, description) " +
                                   "VALUES (" + recipe.getName() + ", '" +
                                   recipe.getDescription() + "')";

        return stmt.executeUpdate(insertRecipeQuery);
    }

    /**
     * Вставка категорий рецепта
     * @param stmt Statement в котором выполняется операция.
     * @param recipe рецепт, откуда берутся шагов.
     */
    private void insertRecipeCategories(Statement stmt, Recipe recipe) throws SQLException {
        for (int categoryID : recipe.getCategoryID()) {
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