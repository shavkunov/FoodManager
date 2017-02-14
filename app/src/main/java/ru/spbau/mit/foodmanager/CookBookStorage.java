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
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 * Хранилище всех рецептов. Singleton. В этом классе слишком много методов, которые можно отнести
 * к другим классам. Будет изменено после рефакторинга.
 */
public class CookBookStorage {
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
     * Сохранение пользовательских настроек в БД.
     * @param settings настройки хранятся как сериализованный instance MenuSettings в этой строчке.
     */
    public void saveUserSettings(String settings) {
        String addSettingsQuery = "INSERT INTO user_settings (user_ID, user_settings) VALUES ('" +
                                  userID + "', '" + settings + "')";

        try {
            refreshConnection();
            deleteUserSettings();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(addSettingsQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Получение пользовательских настроек в БД.
     * @return settings настройки хранятся как сериализованный instance MenuSettings в этой строчке.
     */
    public String getUserSettings() {
        String query = "SELECT user_settings FROM user_settings WHERE user_ID = '" + userID + "'";

        String userSettings = "";
        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                userSettings = rs.getString("user_settings");
                Log.d(LOG_TAG, userSettings);
            } else {
                userSettings = MenuSettings.getInstance(context).saveMenuSettings(context); // !!
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userSettings;
    }

    /**
     * Добавление рецепта в базу данных на сервере. Если одна из операций вставок провалилась,
     * то рецепт не будет встален полностью.
     * @param recipe добавление рецепта в базу данных на сервере.
     */
    public void addRecipeToDatabase(RecipeToChange recipe) {
        try {
            refreshConnection();
            connection.setAutoCommit(false);

            int recipeID = insertRecipeMainInformation(recipe);
            recipe.setID(recipeID);
            insertRecipeCategories(recipe);
            ArrayList<Integer> ingredientIDs = insertRecipeIngredients(recipe);
            insertRecipeIngredientRelation(recipe, ingredientIDs);
            ArrayList<Integer> stepIDs = setRecipeSteps(recipe);
            setRecipeImageStepRelation(stepIDs, recipe);

            connection.setAutoCommit(true);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Unable to insert new recipe");
            e.printStackTrace();
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
     * Вставка в таблицу Image изображений из инструкции приготовления блюда.
     * @param ids идентификаторы картинок загруженные в setRecipeSteps.
     */
    private void setRecipeImageStepRelation(ArrayList<Integer> ids, RecipeToChange recipe)
                                                                      throws Exception {
        for (int i = 0; i < ids.size(); i++) {
            String deletePreviousRelation = "DELETE FROM Image " +
                    "WHERE entity_type = 0 AND entity_ID = " + ids.get(i);

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
    }

    /**
     * Загрузка описаний шагов в инструкции готовки.
     * @param recipe рецепт, откуда берутся шагов.
     * @return Идентификаторы строк, куда были вставлены шаги.
     */
    private ArrayList<Integer> setRecipeSteps(RecipeToChange recipe) throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        for (Step s : recipe.getSteps()) {
            String insertStep = "INSERT INTO Step(recipe_ID, description) VALUES  (?, ?)";
            refreshConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(insertStep);
            preparedStatement.setInt(1, recipe.getID());
            preparedStatement.setString(2, s.getDescription());
            ids.add(preparedStatement.executeUpdate());
            preparedStatement.close();
        }

        return ids;
    }

    /**
     * Вставка нужных записей в таблицу связи Ingredient_to_recipe.
     * @param recipe рецепт, откуда берутся шагов.
     * @param ingredientIDs идентификаторы строке, где хранятся ингредиенты.
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

    private ArrayList<Integer> getRecipeIngredientIDs(RecipeToChange recipe) {
        String selectIngredientQuery = "SELECT Ingredient_ID FROM Ingredient_to_recipe " +
                                       "WHERE recipe_ID = " + recipe.getID();

        ArrayList<Integer> ids = new ArrayList<>();
        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(selectIngredientQuery);
            while(rs.next()) {
                ids.add(rs.getInt("Ingredient_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    private void deleteIngredients(ArrayList<Integer> ids) {
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

    private void deleteRecipeIngredients(RecipeToChange recipe) {
        ArrayList<Integer> ids = getRecipeIngredientIDs(recipe);
        deleteIngredientToRecipeRelation(recipe);
        deleteIngredients(ids);
    }

    public void changeRecipeIngredients(RecipeToChange recipe) {
        deleteRecipeIngredients(recipe);
        ArrayList<Integer> newIds = insertRecipeIngredients(recipe);
        insertRecipeIngredientRelation(recipe, newIds);
    }

    /**
     * Вставка ингредиентов в таблицу Ingredient.
     * @param recipe рецепт, откуда берутся шагов.
     * @return идентификаторы строке, где хранятся ингредиенты.
     */
    private ArrayList<Integer> insertRecipeIngredients(RecipeToChange recipe) {
        ArrayList<Integer> ids = new ArrayList<>();

        try {
            for (Ingredient ing : recipe.getIngredients()) {
                String insertIngredientQuery = "INSERT INTO Ingredient (recipe_ID, description) " +
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
     * Изменение категорий рецепта. У рецепта должен быть валидный ID.
     * В принципе, реализация могла быть только методом insertRecipeCategories, но тогда
     * всегда будет 2 SQL запроса, а этот метод делает их только при необходимости.
     * @param recipe у этого рецепта изменятся категории в БД.
     */
    public void changeRecipeCategories(RecipeToChange recipe) {
        deleteRecipeCategories(recipe);
        insertRecipeCategories(recipe);
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
     * Получение категорий, к которым принадлежит рецепт.
     * @param ID ID рецепта.
     */
    public ArrayList<Integer> getRecipeCategories(int ID) {
        String categoriesQuery = "SELECT category_ID FROM Recipe_to_category " +
                                 "WHERE recipe_ID = " + ID;

        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            ResultSet categories = stmt.executeQuery(categoriesQuery);
            ArrayList<Integer> ids = new ArrayList<>();

            while (categories.next()) {
                ids.add(categories.getInt("category_ID"));
            }

            stmt.close();
            return ids;
        } catch (SQLException e) {
            Log.d(LOG_TAG, "Unable to get categories of recipe");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Получение инструкции для готовки из двух таблиц -- Step и Image.
     * Картинки не загружаются.
     * @param ID ID рецепта.
     */
    public ArrayList<Step> getRecipeSteps(int ID) {
        String stepsQuery = "SELECT * FROM Step INNER JOIN Image ON " +
                            "Step.ID = Image.entity_ID " +
                            "WHERE Step.recipe_ID = " + ID + " AND Image.entity_type = 0";
        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            ResultSet steps = stmt.executeQuery(stepsQuery);
            ArrayList<Step> recipeSteps = new ArrayList<>();

            while (steps.next()) {
                String stepDescription = steps.getString("description");
                String imageURL = steps.getString("link");
                recipeSteps.add(new Step(stepDescription, imageURL));
            }

            stmt.close();
            return recipeSteps;

        } catch (Exception e) {
            Log.d(LOG_TAG, "Unable to get recipe steps");
            e.printStackTrace();
        }

        return null;
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
     * Получение ингредиентов из двух таблиц: Ingredient_to_recipe и Ingredient
     * @param ID ID рецепта.
     */
    public ArrayList<Ingredient> getRecipeIngredients(int ID) {
        String ingredientsQuery = "SELECT name, measure, quantity " +
                                  "FROM Ingredient_to_recipe AS itr " +
                                  "INNER JOIN Ingredient AS ing ON " +
                                  "itr.ingredient_ID = ing.ID " +
                                  "WHERE itr.recipe_ID = " + ID;
        try {
            refreshConnection();
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
            refreshConnection();
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
            refreshConnection();
            Statement stmt = connection.createStatement();
            ResultSet recipes = stmt.executeQuery(filterQuery);

            ArrayList<Recipe> res = new ArrayList<>();
            while (recipes.next()) {
                res.add(new Recipe(recipes.getInt("ID"),
                        recipes.getString("name"),
                        recipes.getString("description")));
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
     * Возвращает количество лайков рецепта.
     * @param recipe вернется количество лайков этого рецепта.
     * @return null если не удалось соединится с сервером, иначе количество лайков.
     */
    public Integer getRecipeLikes(Recipe recipe) {
        String likesQuery = "SELECT COUNT(*) AS total FROM Likes WHERE recipe_ID = " + recipe.getID();
        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(likesQuery);
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        String likesQuery = "SELECT COUNT(*) AS total FROM Likes WHERE recipe_ID = " +
                            recipe.getID() + " AND user_ID = '" + userID + "'";
        Log.d(LOG_TAG, likesQuery);
        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(likesQuery);
            int like = 0;
            if (rs.next())
                like = rs.getInt("total");
            Log.d(LOG_TAG, userID);
            Log.d(LOG_TAG, String.valueOf(recipe.getID()));
            Log.d(LOG_TAG, String.valueOf(like));
            stmt.close();
            return like == 1;
        } catch (SQLException e) {
            Log.d(LOG_TAG, "Не удалось получить лайк");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Метод ставит лайк пользователя.
     * @param recipe этому рецепту пользователь ставит лайк.
     * @return true если операция прошла успешно, иначе false.
     */
    public boolean setLike(Recipe recipe) {
        try {
            String setLikeQuery = "INSERT INTO Likes(user_ID, recipe_ID) VALUES ('" +
                                  userID + "', " + recipe.getID() + ")";

            refreshConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(setLikeQuery);
            stmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Метод убирает лайк пользователя. Предполагается, что лайк уже поставлен.
     * @param recipe убирает лайк у этого рецепта.
     * @return true, если операция прошла успешно, false иначе.
     */
    public boolean setNotLike(Recipe recipe) {
        try {
            String removeLikeQuery = "DELETE FROM Likes WHERE user_ID = '" + userID +
                    "' AND recipe_ID = " + recipe.getID();
            refreshConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(removeLikeQuery);
            stmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Добавить рецепт в избранное, предполагается, что его там нет.
     * @param recipe рецепт, который хотим добавить.
     */
    public void addToFavorites(Recipe recipe)  {
        String addQuery = "INSERT INTO Favorites(user_ID, recipe_ID) VALUES " +
                          "('" + userID + "', " + recipe.getID() + ")";

        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(addQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Сделать избранным конкретные рецепты.
     * @param ids в избранном хранятся id рецептов.
     */
    private void setRecipesIDsAsFavorites(ArrayList<Integer> ids) {
        clearAllFavorites();

        String insertRecipesQuery = "INSERT INTO Favorites(user_ID, recipe_ID) VALUES ";

        for (int i = 0; i < ids.size() - 1; i++) {
            String value = "('" + userID + "', " + ids.get(i) + "), ";
            insertRecipesQuery += value;
        }
        String lastValue = "('" + userID + "', " + ids.get(ids.size() - 1) + ");";
        insertRecipesQuery += lastValue;

        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(insertRecipesQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Удаление рецепта из избранного.
     * @param recipe удаление рецепта из избранного.
     */
    public void removeFromFavorites(Recipe recipe) {
        String removeQuery = "DELETE FROM Favorites WHERE recipe_ID = " + recipe.getID();
        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(removeQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Очистка избранного.
     */
    public void clearAllFavorites() {
        String clearFavoritesQuery = "DELETE FROM Favorites WHERE user_ID = '" + userID + "'";

        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(clearFavoritesQuery);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Получение рецептов в виде их идентификаторов из избранного.
     * @return пустой список если избранного нет.
     */
    private ArrayList<Integer> getRecipesIDFromFavorites() {
        String favoritesQuery = "SELECT recipe_ID FROM Favorites WHERE user_ID = '" + userID + "'";
        ArrayList<Integer> ids = new ArrayList<>();
        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(favoritesQuery);

            while (rs.next()) {
                ids.add(rs.getInt("recipe_ID"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    /**
     * Проверка на наличие рецепта в избранном.
     * @param recipe есть ли этот рецепт в избранном
     * @return true если рецепт есть, иначе false.
     */
    private boolean isRecipeFavorite(Recipe recipe) {
        ArrayList<Integer> ids = getRecipesIDFromFavorites();

        boolean answer = false;
        for (int ID : ids) {
            if (recipe.getID() == ID) {
                answer = true;
            }
        }

        return answer;
    }

    /**
     * Получение избранных рецептов.
     * @return пустой список если в избранном пусто.
     */
    public ArrayList<Recipe> getFavorites() {
        ArrayList<Integer> ids = getRecipesIDFromFavorites();
        ArrayList<Recipe> res = new ArrayList<>();

        for (int ID : ids) {
            res.add(getRecipe(ID));
        }

        return res;
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
     * Получение рецепта, который одновременно принадлежит некоторым категориям.
     * @param ids id категорий, к которым должен принадлежать рецепт.
     * @return рецепт или null если такого нет.
     */
    public Recipe getRecipeFromCategoriesIntersect(ArrayList<Integer> ids) {
        ArrayList<Recipe> recipesFromFirstCategory = getRecipesOfCategory(ids.get(0));
        ArrayList<Recipe> selectedRecipes = new ArrayList<>();

        HashMap<Integer, Integer> hm = new HashMap<>();
        for (int i = 1; i < ids.size(); i++) {
            hm.put(ids.get(i), 1);
        }
        for (Recipe r : recipesFromFirstCategory) {
            int size = ids.size();
            for (Integer ID : r.getCategoryID()) {
                if (hm.containsKey(ID)) {
                    size--;
                }
            }

            if (size == 0) {
                selectedRecipes.add(r);
            }
        }

        Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(selectedRecipes.size());
        return selectedRecipes.get(index);
    }

    /**
     * Получение всех рецептов категории.
     * @param ID идентификатор категории.
     * @return рецепты категории.
     */
    public ArrayList<Recipe> getRecipesOfCategory(int ID) {
        String categoryQuery = "SELECT recipe_ID " +
                               "FROM Recipe_to_category WHERE category_ID = " + ID;
        try {
            refreshConnection();
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

    /**
     * Получение категории по ID.
     * @param ID идентификатор категории, которую хотим получить.
     * @return инстанс класса Category
     */
    public Category getCategoryByID(int ID) {
        String categoryQuery = "SELECT name FROM Category WHERE ID = " + ID;

        try {
            refreshConnection();
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

    /**
     * Получение списка категорий.
     * @param categoryQuery запрос к БД какие именно категории нужно получить.
     * @return Linked List категорий, полученных из запроса.
     */
    private LinkedList<Category> getCategoryFromQuery(String categoryQuery) {
        LinkedList<Category> categories = new LinkedList<>();

        try {
            refreshConnection();
            Statement stmt = connection.createStatement();
            ResultSet category = stmt.executeQuery(categoryQuery);

            while (category.next()) {
                categories.add(new Category(category.getInt("ID"),
                                            category.getString("name"), null));
            }

            stmt.close();
            return categories;
        } catch (SQLException e) {
            Log.d(LOG_TAG, "Unable to get categories");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Получение списка категорий по типу блюда.
     * @return список категорий по типу блюда.
     */
    public LinkedList<Category> getRecipiesTypeOfDish() {
        String categoryQuery = "SELECT * FROM Category WHERE is_category_dish = 1";

        return getCategoryFromQuery(categoryQuery);
    }
    /**
     * Получение списка категорий по национальной кухне блюда.
     * @return список категорий по национальной кухне блюда.
     */
    public LinkedList<Category> getRecipiesNationalKitchen() {
        String categoryQuery = "SELECT * FROM Category WHERE is_national_kitchen = 1";

        return getCategoryFromQuery(categoryQuery);
    }
}