package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

/**
 * Хранилище всех рецептов. Singleton. В этом классе слишком много методов, которые можно отнести
 * к другим классам. Будет изменено после рефакторинга.
 */
public class CookBookStorage {
    public static final String LOCAL_IP = "192.168.211.199"; // my local IP address
    private static final String getRecipeCommand = "/getRecipe";
    private static final String getRecipeCategoriesCommand = "/getRecipeCategories";
    private static final String getRecipeIngredientsCommand = "/getRecipeIngredients";
    private static final String getRecipeStepsCommand = "/getRecipeSteps";
    private static final String getRecipesByFilterCommand = "/getRecipesByFilter";
    private static final String getUserSettingsCommand = "/getUserSettings";
    private static final String getUserLikeCommand = "/getUserLike";
    private static final String getRecipeLikesCommand = "/getRecipeLikes";
    private static final String getFavoritesCommand = "/getFavorites";
    private static final String getRecipesOfCategoryCommand = "/getRecipesOfCategory";
    private static final String getCategoryByIDCommand = "/getCategoryByID";
    private static final String getCategoriesListCommand = "/getCategoriesList";
    private static final String setUserLikeCommand = "/setLike";
    private static final String setUserNotLikeCommand = "/setNotLike";
    private static final String addToFavoritesCommand = "/addToFavorites";
    private static final String removeFromFavoritesCommand = "/removeFromFavorites";
    private static final String saveUserSettingsCommand = "/saveUserSettings";
    private static final int port = 48800; // free random port;
    private static final int HTTP_CONNECT_TIMEOUT_MS = 2000;
    private static final int HTTP_READ_TIMEOUT_MS = 2000;
    private static final int MAX_ATTEMPTS = 3;

    /**
     * Инстанс класса CookBookStorage.
     */
    private static CookBookStorage instance;

    /**
     * Вспомогательный тег для отладки.
     */
    private final String LOG_TAG = "CookBookStorageTAG";

    /**
     * Логин для хостинга картинок cloudinary.
     * Его можно собирать из трех частей, но я не думаю, что это нужно.
     */
    private final String CLOUDINARY_URL = "cloudinary://285162791646134:yGqzM1FdReQ8uPa1taEUZihoNgI@dxc952wrd";

    /**
     * Ссылка на базу данных на сервисе gearhost.
     */
    private final String databaseURL = "jdbc:mysql://mysql1.gear.host:3306/foodmanagertest";

    /**
     * Пользователь базы данных.
     */
    private String user = "foodmanagertest";

    /**
     * Пароль от базы данных.
     */
    private final String password = "Wc22_0f_0TA2";

    /**
     * Коннект к базе данных.
     */
    private Connection connection;

    /**
     * ID пользователя.
     */
    private String userID;

    /**
     * Контекст приложения.
     */
    private Context context;

    private CookBookStorage(Context context) {
        try {
            this.context = context;
            userID = Installation.getUserID(context);
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver");
            refreshConnection();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Problems with connection");
        }
    }

    // --------------------------------change-----------------------------------

    /**
     * Изменение информации о рецепте. Можно было изменить рецепт, удалив его и добавив новый,
     * но использование этого метода эффективнее в количестве SQL запросов.
     * @param recipe информация этого рецепта будет помещена в БД.
     * @throws Exception если есть проблемы с соединением.
     */
    public void changeRecipe(RecipeToChange recipe) throws Exception {
        refreshConnection();
        connection.setAutoCommit(false);
        changeRecipeMainInformation(recipe);
        changeRecipeCategories(recipe);
        changeRecipeIngredients(recipe);
        changeRecipeSteps(recipe);
        connection.setAutoCommit(true);
    }

    /**
     * Изменение шагов рецепта в БД.
     * @param recipe шаги этого рецепта станут хранится в БД.
     */
    public void changeRecipeSteps(RecipeToChange recipe) {
        ArrayList<Integer> recipeIDs = getRecipeStepIDs(recipe);
        deleteRecipeSteps(recipe);
        deleteRecipeImageStepRelation(recipeIDs);
        ArrayList<Integer> stepIDs = insertRecipeSteps(recipe);
        insertRecipeImageStepRelation(stepIDs, recipe);
    }

    /**
     * Изменение ингредиентов у рецепта.
     * @param recipe рецепт должен иметь также иметь валидный ID
     */
    public void changeRecipeIngredients(RecipeToChange recipe) {
        deleteRecipeIngredients(recipe);
        ArrayList<Integer> newIds = insertRecipeIngredients(recipe);
        insertRecipeIngredientRelation(recipe, newIds);
    }

    /**
     * У рецепта recipe будут заменены поля name и description в БД.
     * Рецепту поля необходим валидный ID.
     */
    public void changeRecipeMainInformation(RecipeToChange recipe) {
        String updateQuery = "UPDATE Recipe SET name = ?, description = ? WHERE ID = ?";
        try {
            refreshConnection();
            PreparedStatement stmt = connection.prepareStatement(updateQuery);
            stmt.setString(1, recipe.getName());
            stmt.setString(2, recipe.getDescription());
            stmt.setInt(3, recipe.getID());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Изменение категорий рецепта. У рецепта должен быть валидный ID.
     * В принципе, реализация могла быть только методом insertRecipeCategories, но тогда
     * всегда будет 2 SQL запроса, а этот метод делает их только при необходимости.
     * @param recipe у этого рецепта изменятся категории в БД.
     */
    public void changeRecipeCategories(RecipeToChange recipe) {
        deleteRecipeCategories(recipe);
        insertRecipeCategories(recipe);
    }

    // --------------------------------insert-----------------------------------

    /**
     * Добавление рецепта в базу данных на сервере. Если одна из операций вставок провалилась,
     * то рецепт не будет встален полностью.
     * @param recipe добавление рецепта в базу данных на сервере.
     */
    public void addRecipeToDatabase(RecipeToChange recipe) throws Exception {
        refreshConnection();
        connection.setAutoCommit(false);

        int recipeID = insertRecipeMainInformation(recipe);
        recipe.setID(recipeID);
        insertUserRecipeRelation(recipe);
        insertRecipeCategories(recipe);
        ArrayList<Integer> ingredientIDs = insertRecipeIngredients(recipe);
        insertRecipeIngredientRelation(recipe, ingredientIDs);
        ArrayList<Integer> stepIDs = insertRecipeSteps(recipe);
        insertRecipeImageStepRelation(stepIDs, recipe);

        connection.setAutoCommit(true);
    }

    /**
     * Связывание рецепта и пользователя.
     */
    public void insertUserRecipeRelation(RecipeToChange recipe) {
        String insertQuery = "INSERT INTO User_to_recipe VALUES ("
                            + recipe.getID() + ", '" + userID + "')";

        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(insertQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Вставка в таблицу Image изображений из инструкции приготовления блюда.
     * @param ids идентификаторы шагов загруженные в insertRecipeSteps.
     */
    private void insertRecipeImageStepRelation(ArrayList<Integer> ids, RecipeToChange recipe) {
        try {
            for (int i = 0; i < ids.size(); i++) {
                String insertRelation = "INSERT INTO Image(entity_type, entity_ID, link) " +
                        "VALUES (?, ?, ?)";

                Bitmap bitmap = recipe.getSteps().get(i).getImage();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapData = bos.toByteArray();
                ByteArrayInputStream bs = new ByteArrayInputStream(bitmapData);

                String link = uploadImage(bs);
                refreshConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(insertRelation);
                preparedStatement.setInt(1, 0);
                preparedStatement.setInt(2, ids.get(i));
                preparedStatement.setString(3, link);
                preparedStatement.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Загрузка описаний шагов в инструкции готовки.
     * @param recipe рецепт, откуда берутся шагов.
     * @return Идентификаторы строк, куда были вставлены шаги.
     */
    private ArrayList<Integer> insertRecipeSteps(RecipeToChange recipe) {
        ArrayList<Integer> ids = new ArrayList<>();

        try {
            for (Step s : recipe.getSteps()) {
                String insertStep = "INSERT INTO Step(recipe_ID, description) VALUES  (?, ?)";
                refreshConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(insertStep);
                preparedStatement.setInt(1, recipe.getID());
                preparedStatement.setString(2, s.getDescription());
                ids.add(preparedStatement.executeUpdate());
                preparedStatement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ids;
    }

    /**
     * Вставка нужных записей в таблицу связи Ingredient_to_recipe.
     * @param recipe рецепт, откуда берутся шагов.
     * @param ingredientIDs идентификаторы строк, где хранятся ингредиенты.
     */
    private void insertRecipeIngredientRelation(
            RecipeToChange recipe, ArrayList<Integer> ingredientIDs) {
        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            // ingredients.size() == ingredientIDs.size()
            for (int i = 0; i < recipe.getIngredients().size(); i++) {
                double quantity = recipe.getIngredients().get(i).getQuantity();
                int measureOrdinal = recipe.getIngredients().get(i).getMeasure().ordinal();
                String insertRelationQuery = "INSERT INTO Ingredient_to_recipe " +
                        "(Ingredient_ID, recipe_ID, measure, quantity) VALUES " +
                        "(" + ingredientIDs.get(i) + ", " + recipe.getID() +
                        ", " + measureOrdinal + ", " + quantity + ")";

                stmt.executeUpdate(insertRelationQuery);
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Вставка ингредиентов в таблицу Ingredient.
     * @param recipe рецепт, откуда берутся шагов.
     * @return идентификаторы строк, где хранятся ингредиенты.
     */
    private ArrayList<Integer> insertRecipeIngredients(RecipeToChange recipe) {
        ArrayList<Integer> ids = new ArrayList<>();

        try {
            for (Ingredient ing : recipe.getIngredients()) {
                String insertIngredientQuery = "INSERT INTO Ingredient (ID, name) " +
                        "VALUES (?, ?)";

                refreshConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(insertIngredientQuery);
                preparedStatement.setInt(1, recipe.getID());
                preparedStatement.setString(2, ing.getName());
                ids.add(preparedStatement.executeUpdate());
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    /**
     * Вставка основной информации рецепта(name, description)  в таблицу Recipe.
     * @param recipe рецепт, откуда берутся шагов.
     * @return номер строки, куда был вставлен рецепт.
     */
    private int insertRecipeMainInformation(RecipeToChange recipe) throws SQLException {
        refreshConnection();
        Statement stmt = connection.createStatement();
        String insertRecipeQuery = "INSERT INTO Recipe(name, description) " +
                "VALUES (" + recipe.getName() + ", '" +
                recipe.getDescription() + "')";

        int res = stmt.executeUpdate(insertRecipeQuery);
        stmt.close();
        return res;
    }

    /**
     * Вставка категорий рецепта в БД.
     * @param recipe рецепт, откуда берутся категории.
     */
    private void insertRecipeCategories(RecipeToChange recipe) {
        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            for (int categoryID : recipe.getCategoryID()) {
                String insertCategoryQuery = "INSERT INTO Recipe_to_category (recipe_ID, category_ID) "
                        + "VALUES (" + recipe.getID() + ", " + categoryID + ")";

                stmt.executeUpdate(insertCategoryQuery);
            }

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Добавить рецепт в избранное, предполагается, что его там нет.
     * @param recipe рецепт, который хотим добавить.
     */
    public void addToFavorites(Recipe recipe)  {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                                               addToFavoritesCommand);
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeInt(recipe.getID());
                output.writeObject(userID);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // --------------------------------delete-----------------------------------

    /**
     * Удаление рецепта из БД.
     * @throws Exception если есть проблемы с соединением.
     */
    public void deleteRecipe(RecipeToChange recipe) throws Exception {
        refreshConnection();
        connection.setAutoCommit(false);
        deleteRecipeMainInformation(recipe);
        deleteUserRecipeRelation(recipe);
        deleteRecipeCategories(recipe);
        deleteRecipeIngredients(recipe);
        deleteIngredientToRecipeRelation(recipe);
        deleteRecipeSteps(recipe);
        ArrayList<Integer> stepIDs = getRecipeStepIDs(recipe);
        deleteRecipeImageStepRelation(stepIDs);
        setNotLike(recipe.getID());
        removeFromFavorites(recipe.getID());
        connection.setAutoCommit(true);
    }

    /**
     * Удаление пользовательских настроек из БД.
     */
    private void deleteUserSettings() {
        String deleteSettingsQuery = "DELETE FROM user_settings WHERE user_ID = '" + userID + "'";

        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(deleteSettingsQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Удаление шагов рецепта из таблицы Step.
     * @param recipe у рецепта должен быть валидный ID.
     */
    private void deleteRecipeSteps(RecipeToChange recipe) {
        try {
            String deleteStepsQuery = "DELETE FROM Step WHERE recipe_ID = " + recipe.getID();
            refreshConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(deleteStepsQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Удаление картинок шагов рецепта.
     * @param ids идентификаторы шагов.
     */
    private void deleteRecipeImageStepRelation(ArrayList<Integer> ids) {
        String deleteImagesQuery = "DELETE FROM Image WHERE entity_type = 0 AND entity_ID IN (";
        for (int i = 0; i < ids.size() - 1; i++) {
            deleteImagesQuery += ids.get(i) + ", ";
        }
        deleteImagesQuery += ids.get(ids.size() - 1) + ")";
        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(deleteImagesQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Удаление ингредиентов рецепта из таблицы Ingredient_to_recipe
     * @param recipe рецепт должен иметь валидный ID.
     */
    private void deleteIngredientToRecipeRelation(RecipeToChange recipe) {
        String deleteQuery = "DELETE FROM Ingredient_to_recipe WHERE recipe_ID = " + recipe.getID();
        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(deleteQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Удаление ингредиентов из таблицы Ingredient.
     * @param ids идентификаторы ингредиентов.
     */
    private void deleteRecipeIngredientsFromIngredient(ArrayList<Integer> ids) {
        String deleteQuery = "DELETE FROM Ingredient WHERE ID IN (";
        for (int i = 0; i < ids.size() - 1; i++) {
            deleteQuery += ids.get(i) + ", ";
        }
        deleteQuery += ids.get(ids.size() - 1) + ")";
        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(deleteQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Удаление ингредиентов у рецепта.
     * @param recipe рецепт должен иметь также иметь валидный ID
     */
    private void deleteRecipeIngredients(RecipeToChange recipe) {
        ArrayList<Integer> ids = getRecipeIngredientIDs(recipe);
        deleteIngredientToRecipeRelation(recipe);
        deleteRecipeIngredientsFromIngredient(ids);
    }

    /**
     * Удаление рецепта из таблицы Recipe.
     * @param recipe удаление по его ID.
     */
    private void deleteRecipeMainInformation(RecipeToChange recipe) {
        try {
            String deleteQuery = "DELETE FROM Recipe WHERE ID = " + recipe.getID();
            refreshConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(deleteQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Удаление категорий рецепта. Рецепт должен иметь валидный ID.
     */
    private void deleteRecipeCategories(RecipeToChange recipe) {
        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            String deletePreviousCategoriesQuery = "DELETE FROM Recipe_to_category " +
                    "WHERE recipe_ID = " + recipe.getID();

            stmt.executeUpdate(deletePreviousCategoriesQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Удаление рецепта из избранного.
     * @param recipeID ID рецепта.
     */
    public void removeFromFavorites(int recipeID) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                                               removeFromFavoritesCommand);
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeInt(recipeID);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }
                break;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Не удалось убрать рецепт из избранного");
                e.printStackTrace();
            }
        }
    }

    /**
     * Удаление связи между рецептов и пользователем.
     */
    private void deleteUserRecipeRelation(RecipeToChange recipe) {
        String deleteQuery = "DELETE FROM User_to_recipe WHERE recipe_ID = " + recipe.getID();

        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(deleteQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -----------------------------геттеры-------------------------------

    /**
     * Получение рецепта по его уникальному идентификатору.
     */
    public Recipe getRecipe(int ID) {
        Recipe recipe = null;

        try {
            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                final HttpURLConnection connection =
                        openHttpURLConnectionForServerCommand(getRecipeCommand);

                ObjectOutputStream stream = new ObjectOutputStream(connection.getOutputStream());
                stream.writeInt(ID);
                stream.flush();
                stream.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                recipe = (Recipe) input.readObject();
                input.close();
                break;
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Unable to get recipe");
            e.printStackTrace();
        }

        return recipe;
    }

    /**
     * Получение категорий, к которым принадлежит рецепт.
     * @param ID ID рецепта.
     */
    public ArrayList<Integer> getRecipeCategories(int ID) {
        ArrayList<Integer> ids = null;
        try {
            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                final HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                                                     getRecipeCategoriesCommand);

                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeInt(ID);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                ids = (ArrayList<Integer>) input.readObject();
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Unable to get categories of recipe");
        }

        return ids;
    }

    /**
     * Получение ингредиентов из двух таблиц: Ingredient_to_recipe и Ingredient
     * @param ID ID рецепта.
     */
    public ArrayList<Ingredient> getRecipeIngredients(int ID) {
        ArrayList<Ingredient> recipeIngredients = new ArrayList<>();

        try {
            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                final HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                        getRecipeIngredientsCommand);

                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeInt(ID);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                recipeIngredients = (ArrayList<Ingredient>) input.readObject();

                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recipeIngredients;
    }

    /**
     * Получение инструкции для готовки из двух таблиц -- Step и Image.
     * Картинки не загружаются.
     * @param ID ID рецепта.
     */
    public ArrayList<Step> getRecipeSteps(int ID) {
        ArrayList<Step> recipeSteps = new ArrayList<>();

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                                               getRecipeStepsCommand);
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeInt(ID);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                ArrayList<ArrayList<String>> stepsData = (ArrayList<ArrayList<String>>)
                                                         input.readObject();

                for (ArrayList<String> data : stepsData) {
                    String stepDescription = data.get(0);
                    String imageURL = data.get(1);
                    recipeSteps.add(new Step(stepDescription, imageURL));
                }

                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return recipeSteps;
    }

    /**
     * Получение списка рецептов по фильтру, т.е. по префиксу.
     */
    public ArrayList<Recipe> getRecipesByFilter(String filter) {
        filter = filter.toLowerCase();
        if (filter.length() > 0) {
            filter = filter.substring(0, 1).toUpperCase() + filter.substring(1);
        }

        ArrayList<Recipe> res = new ArrayList<>();
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                        getRecipesByFilterCommand);

                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeObject(filter);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                res = (ArrayList<Recipe>) input.readObject();

            } catch (Exception e) {
                Log.d(LOG_TAG, "Unable to filter recipes");
                e.printStackTrace();
            }
        }

        return res;
    }

    /**
     * Получение идентификаторов ингредиентов рецепта.
     * @param recipe рецепт должен иметь валидный ID.
     * @return id всех ингредиентов, принадлежащих рецепту.
     */
    private ArrayList<Integer> getRecipeIngredientIDs(RecipeToChange recipe) {
        String selectIngredientQuery = "SELECT Ingredient_ID FROM Ingredient_to_recipe " +
                "WHERE recipe_ID = " + recipe.getID();

        ArrayList<Integer> ids = new ArrayList<>();
        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(selectIngredientQuery);
            while (rs.next()) {
                ids.add(rs.getInt("Ingredient_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    /**
     * Получение идентификаторов шагов рецепта.
     * @param recipe у рецепта должен быть валидный ID
     * @return ID шагов, принадлежащие рецепту.
     */
    private ArrayList<Integer> getRecipeStepIDs(RecipeToChange recipe) {
        ArrayList<Integer> ids = new ArrayList<>();
        try {
            String getStepsQuery = "SELECT ID FROM Step WHERE recipe_ID = " + recipe.getID();
            refreshConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(getStepsQuery);
            while (rs.next()) {
                ids.add(rs.getInt("ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    /**
     * Получение пользовательских настроек в БД.
     * @return settings настройки хранятся как сериализованный instance MenuSettings в этой строчке.
     */
    public String getUserSettings() {
        String userSettings = "";
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                        getUserSettingsCommand);

                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeObject(userID);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                userSettings = (String) input.readObject();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return userSettings;
    }

    /**
     * Получение инстанса класса CookBookStorage.
     * @param context контекст приложения.
     * @return инстанс класса.
     */
    public static CookBookStorage getInstance(Context context) {
        if (instance == null) {
            instance = new CookBookStorage(context);
        }

        return instance;
    }

    /**
     * Возвращает количество лайков рецепта.
     * @param recipe вернется количество лайков этого рецепта.
     * @return null если не удалось соединится с сервером, иначе количество лайков.
     */
    public Integer getRecipeLikes(Recipe recipe) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                                                                getRecipeLikesCommand);
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeInt(recipe.getID());
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                int likes = input.readInt();
                input.close();
                return likes;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Если пользователь поставил лайк то вернется True, иначе False. Null если не получилось
     * соединиться с сервером.
     * @param recipe вернется лайк пользователя этого рецепта.
     * @return если пользователь поставил лайк то вернется True, иначе False. Null если не получилось
     * соединиться с сервером.
     */
    public Boolean getUserLike(Recipe recipe) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(getUserLikeCommand);
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeInt(recipe.getID());
                output.writeObject(userID);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                int like = input.readInt();
                input.close();
                return like == 1;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Не удалось получить лайк");
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Получение избранных рецептов.
     * @return пустой список если в избранном пусто.
     */
    public ArrayList<Recipe> getFavorites() {
        ArrayList<Recipe> favorites = new ArrayList<>();
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                                                                            getFavoritesCommand);
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeObject(userID);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                favorites = (ArrayList<Recipe>) input.readObject();
                input.close();
                break;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Не удалось получить избранное");
                e.printStackTrace();
            }
        }

        return favorites;
    }

    /**
     * Получение всех рецептов категории.
     * @param ID идентификатор категории.
     * @return рецепты категории.
     */
    public ArrayList<Recipe> getRecipesOfCategory(int ID) {
        ArrayList<Recipe> recipes = new ArrayList<>();
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                                                                    getRecipesOfCategoryCommand);
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeInt(ID);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                recipes = (ArrayList<Recipe>) input.readObject();
                input.close();
                break;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Не удалось получить рецепты категории");
                e.printStackTrace();
            }
        }

        return recipes;
    }

    /**
     * Получение категории по ID.
     * @param ID идентификатор категории, которую хотим получить.
     * @return инстанс класса Category
     */
    public Category getCategoryByID(int ID) {
        Category category = null;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                        getCategoryByIDCommand);
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeInt(ID);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                String categoryName = (String) input.readObject();
                category = new Category(ID, categoryName, null);
                input.close();
                break;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Не удалось получить категорию");
                e.printStackTrace();
            }
        }

        return category;
    }

    /**
     * Получение списка категорий.
     * @param categoryType тип категорий, который хотим получить.
     * @return Linked List категорий, полученных из запроса.
     */
    private LinkedList<Category> getCategoryByType(String categoryType) {
        LinkedList<Category> categories = new LinkedList<>();

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                                                                       getCategoriesListCommand);
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeObject(categoryType);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                ArrayList<Integer> ids = (ArrayList<Integer>) input.readObject();
                ArrayList<String> names = (ArrayList<String>) input.readObject();
                for (int i = 0; i < ids.size(); i++) {
                    categories.add(new Category(ids.get(i), names.get(i), null));
                }

                input.close();
                break;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Не удалось получить список категорий");
                e.printStackTrace();
            }
        }

        return categories;
    }

    /**
     * Получение списка категорий по типу блюда.
     * @return список категорий по типу блюда.
     */
    public LinkedList<Category> getRecipesTypeOfDish() {
        String categoryType = "category_dish";

        return getCategoryByType(categoryType);
    }

    /**
     * Получение списка категорий по национальной кухне блюда.
     * @return список категорий по национальной кухне блюда.
     */
    public LinkedList<Category> getRecipesNationalKitchen() {
        String categoryType = "national_kitchen";

        return getCategoryByType(categoryType);
    }

    //----------------------------------rest-------------------------------

    /**
     * Сохранение пользовательских настроек в БД.
     * @param settings настройки хранятся как сериализованный instance MenuSettings в этой строчке.
     */
    public void saveUserSettings(String settings) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                        saveUserSettingsCommand);
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeObject(userID);
                output.writeObject(settings);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                break;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Не удалось сохранить настройки");
                e.printStackTrace();
            }
        }
    }

    /**
     * Загрузка изображения.
     * @param imageIn изображение.
     * @return онлайн ссылка на изображение в сервисе cloudinary
     */
    private String uploadImage(InputStream imageIn) throws Exception {
        Cloudinary cloudinary = new Cloudinary(CLOUDINARY_URL);
        Map result = cloudinary.uploader().upload(imageIn, ObjectUtils.emptyMap());
        JSONObject jsonObject = new JSONObject(result);

        return jsonObject.getString("url");
    }

    /**
     * Получение соединения с БД, если его нет по какой-то причине.
     * Я не уверен, что правильно это делаю. !!!!
     */
    private void refreshConnection() {
        try {
            while (connection == null || connection.isClosed())
                connection = DriverManager.getConnection(databaseURL, user, password);
        } catch (SQLException e) {
            Log.d(LOG_TAG, "Unable to refresh connection");
            e.printStackTrace();
        }
    }

    /**
     * Загрузка картинки шага.
     * @param step загрузка прямо в поле объекта.
     */
    public void downloadStepImage(Step step) {
        Log.d(LOG_TAG, step.getImageLink());
        try {
            Bitmap image = BitmapFactory.decodeStream((InputStream)new URL(step.getImageLink())
                    .getContent());

            step.setImage(image);
        } catch (IOException e) {
            Log.d(LOG_TAG, "Unable to load step image\ndescription: " + step.getDescription());
            e.printStackTrace();
        }
    }

    /**
     * Метод ставит лайк пользователя.
     * @param recipeID ID рецепта.
     * @return true если операция прошла успешно, иначе false.
     */
    public boolean setLike(int recipeID) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(setUserLikeCommand);
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeInt(recipeID);
                output.writeObject(userID);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                return true;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Не удалось поставить лайк");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Метод убирает лайк пользователя.
     * @param recipeID ID рецепта.
     * @return true, если операция прошла успешно, false иначе.
     */
    public boolean setNotLike(int recipeID) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                                               setUserNotLikeCommand);
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeInt(recipeID);
                output.writeObject(userID);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                return true;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Не удалось убрать лайк");
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Выбор случайного блюда категории.
     */
    public Recipe chooseRandomDishFromCategory(int categoryID) {
        String getRandomRecipeQuery = "SELECT recipe_ID FROM Recipe_to_category " +
                "WHERE category_ID = " + categoryID +
                " ORDER BY RAND() LIMIT 1";

        try {
            refreshConnection();
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

    /**
     * Является ли текущий пользователь владельцем рецепта.
     * @param recipe рецепт, который хотим проверить
     * @return true если recipe принадлежить пользователь и false иначе.
     */
    public boolean isUserOwnRecipe(Recipe recipe) {
        String selectQuery = "SELECT EXISTS(SELECT 1 FROM User_to_recipe WHERE recipe_ID = "
                             + recipe.getID() + " AND user_ID = '" + userID + "')";

        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(selectQuery);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static HttpURLConnection openHttpURLConnectionForServerCommand(String command) throws IOException {
        final String urlString = "http://" + LOCAL_IP + ':' + port + command;

        final URL url = new URL(urlString);

        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(HTTP_CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(HTTP_READ_TIMEOUT_MS);

        return connection;
    }
}