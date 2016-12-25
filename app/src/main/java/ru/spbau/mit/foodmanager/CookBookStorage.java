package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    /**
     * Название файла, где будут храниться избранные рецепты пользователя.
     */
    private final String favoritesFileName = "Favorites";

    private CookBookStorage(Context context) {
        try {
            this.context = context;
            userID = Installation.getUserID(context);
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(databaseURL, user, password);
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
     * Добавление рецепта в базу данных на сервере. Если одна из операций вставок провалилась,
     * то рецепт не будет встален полностью.
     * @param recipe добавление рецепта в базу данных на сервере.
     */
    public void addRecipeToDatabase(Recipe recipe) {
        try {
            while (connection == null || connection.isClosed())
                connection = DriverManager.getConnection(databaseURL, user, password);
            connection.setAutoCommit(false);
            Statement stmt = connection.createStatement();

            int recipeID = insertRecipe(stmt, recipe);
            recipe.setID(recipeID);
            insertRecipeCategories(stmt, recipe);
            ArrayList<Integer> ingredientIDs = insertIngredients(stmt, recipe);
            insertRecipeIngredientRelation(stmt, recipe, ingredientIDs);
            ArrayList<Integer> stepIDs = insertSteps(stmt, recipe);
            insertImageStepRelation(stepIDs, recipe);

            stmt.close();
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
     * Вставка в таблицу Image изображений из инструкции приготовления блюда.
     * @param ids идентификаторы картинок загруженные в insertSteps.
     */
    private void insertImageStepRelation(ArrayList<Integer> ids, Recipe recipe) throws Exception {

        for (int i = 0; i < ids.size(); i++) {
            String insertRelation = "INSERT INTO Image VALUES (?, ?, ?)";

            Bitmap bitmap = recipe.getSteps().get(i).getImage();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

            String link = uploadImage(bs);
            PreparedStatement preparedStatement = connection.prepareStatement(insertRelation);
            preparedStatement.setInt(1, 0);
            preparedStatement.setInt(2, ids.get(i));
            preparedStatement.setString(3, link);
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
            while (connection == null || connection.isClosed())
                connection = DriverManager.getConnection(databaseURL, user, password);
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
     * @param ID ID рецепта.
     */
    public ArrayList<Step> getRecipeSteps(int ID) {
        String stepsQuery = "SELECT * FROM Step INNER JOIN Image ON " +
                            "Step.ID = Image.entity_ID " +
                            "WHERE Step.recipe_ID = " + ID + " AND Image.entity_type = 0";
        try {
            while (connection == null || connection.isClosed())
                connection = DriverManager.getConnection(databaseURL, user, password);
            Statement stmt = connection.createStatement();
            ResultSet steps = stmt.executeQuery(stepsQuery);
            ArrayList<Step> recipeSteps = new ArrayList<>();

            while (steps.next()) {
                String stepDescription = steps.getString("description");
                String imageURL = steps.getString("link");

                URL url = new URL(imageURL);
                Log.d(LOG_TAG, imageURL);
                Bitmap image = BitmapFactory.decodeStream((InputStream)new URL(imageURL).getContent());
                recipeSteps.add(new Step(stepDescription, image));
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
     * Получение ингредиентов из двух таблиц: Ingredient_to_recipe и Ingredient
     * @param ID ID рецепта.
     */
    public ArrayList<Ingredient> getRecipeIngredients(int ID) {
        String ingredientsQuery = "SELECT * FROM Ingredient_to_recipe AS itr " +
                                  "INNER JOIN Ingredient AS ing ON " +
                                  "itr.ingredient_ID = ing.ID " +
                                  "WHERE itr.recipe_ID = " + ID;
        try {
            while (connection == null || connection.isClosed())
                connection = DriverManager.getConnection(databaseURL, user, password);
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
            while (connection == null || connection.isClosed())
                connection = DriverManager.getConnection(databaseURL, user, password);
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
            while (connection == null || connection.isClosed())
                connection = DriverManager.getConnection(databaseURL, user, password);
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
     * Возвращает количество лайков рецепта.
     * @param recipe вернется количество лайков этого рецепта.
     * @return null если не удалось соединится с сервером, иначе количество лайков.
     */
    public Integer getRecipeLikes(Recipe recipe) {
        String likesQuery = "SELECT COUNT(*) AS total FROM Likes WHERE recipe_ID = " + recipe.getID();
        try {
            while (connection == null || connection.isClosed())
                connection = DriverManager.getConnection(databaseURL, user, password);
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
            while (connection == null || connection.isClosed())
                connection = DriverManager.getConnection(databaseURL, user, password);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(likesQuery);
            int like = 0;
            if (rs.next())
                like = rs.getInt("total");
            Log.d(LOG_TAG, userID);
            Log.d(LOG_TAG, String.valueOf(recipe.getID()));
            Log.d(LOG_TAG, String.valueOf(like));
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
     * @param state true, тогда поставится лайк, иначе не поставится
     * @return true если операция прошла успешно, иначе false.
     */
    public boolean setLike(Recipe recipe, boolean state) {
        Boolean userLike = getUserLike(recipe);
        if (userLike == null) {
            return false;
        }

        //состояние не изменилось
        // пользователь уже поставил лайк и нужно поставить
        // пользователь не ставил лайк и не нужно ставить.
        if (userLike == state) {
            return true;
        }

        // пользователь не ставил лайк и надо поставить лайк.
        if (!userLike && state) {
            try {
                String setLikeQuery = "INSERT INTO Likes(user_ID, recipe_ID) VALUES ('" +
                                      userID + "', " + recipe.getID() + ")";

                while (connection == null || connection.isClosed())
                    connection = DriverManager.getConnection(databaseURL, user, password);
                Statement stmt = connection.createStatement();
                stmt.executeUpdate(setLikeQuery);

                stmt.close();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        }

        // пользователь поставил лайк и надо убрать лайк.
        try {
            String removeLikeQuery = "DELETE FROM Likes WHERE user_ID = '" + userID +
                                     "' AND recipe_ID = " + recipe.getID();
            while (connection == null || connection.isClosed())
                connection = DriverManager.getConnection(databaseURL, user, password);
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
     * Добавить рецепт в избранное.
     * @param recipe рецепт, который хотим добавить.
     */
    public void addToFavorites(Recipe recipe)  {
        if (isRecipeFavorite(recipe)) {
            return;
        }

        ArrayList<Integer> ids = getRecipesIDFromFavorites();
        ids.add(recipe.getID());
        addIDsToFavorites(ids);
    }

    /**
     * Сделать избранным конкретные рецепты.
     * @param ids в избранном хранятся id рецептов.
     */
    private void addIDsToFavorites(ArrayList<Integer> ids) {
        File favorites = new File(context.getFilesDir(), favoritesFileName);

        FileOutputStream outputStream = null;

        try {
            favorites.createNewFile();
            outputStream = context.openFileOutput(favoritesFileName, Context.MODE_PRIVATE);

            String data = "";
            if (ids.size() > 0) {
                data = String.valueOf(ids.get(0));
                for (int i = 1; i < ids.size(); i++) {
                    data += " " + ids.get(i);
                }
            }

            outputStream.write(data.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Удаление рецепта из избранного.
     * @param recipe удаление рецепта из избранного.
     */
    public void removeFromFavorites(Recipe recipe) {
        ArrayList<Integer> ids = getRecipesIDFromFavorites();
        ids.remove(recipe.getID());
        addIDsToFavorites(ids);
    }

    /**
     * Очистка избранного.
     */
    public void clearAllFavorites() {
        ArrayList<Integer> ids = new ArrayList<>();
        addIDsToFavorites(ids);
    }

    /**
     * Получение рецептов в виде их идентификаторов из избранного.
     * @return пустой список если избранного нет.
     */
    private ArrayList<Integer> getRecipesIDFromFavorites() {
        ArrayList<Integer> res = new ArrayList<>();

        File favorites = new File(context.getFilesDir(), favoritesFileName);
        if (!favorites.exists()) {
            return res;
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                                context.openFileInput(favoritesFileName)));

            String line = br.readLine();

            for (String number : line.split(" ")) {
                int ID = Integer.parseInt(number);
                res.add(ID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return res;
        }

        return res;
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
            while (connection == null || connection.isClosed())
                connection = DriverManager.getConnection(databaseURL, user, password);
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
        String categoryQuery = "SELECT * FROM Recipe_to_category WHERE category_ID = " + ID;
        try {
            while (connection == null || connection.isClosed())
                connection = DriverManager.getConnection(databaseURL, user, password);
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
        String categoryQuery = "SELECT * FROM Category WHERE ID = " + ID;

        try {
            while (connection == null || connection.isClosed())
                connection = DriverManager.getConnection(databaseURL, user, password);
            Statement stmt = connection.createStatement();
            ResultSet category = stmt.executeQuery(categoryQuery);
            if (category.next()) {
                String description = category.getString("name");
                // пока картинок категорий у нас нет
                Category c =  new Category(ID, description, null, context);
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
            while (connection == null || connection.isClosed())
                connection = DriverManager.getConnection(databaseURL, user, password);
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

    /**
     * Получение списка категорий по типу блюда.
     * @return список категорий по типу блюда.
     */
    public LinkedList<Category> getRecipiesTypeOfDish() {
        String categoryQuery = "SELECT ID FROM Category WHERE is_category_dish = 1";

        return getCategoryFromQuery(categoryQuery);
    }
    /**
     * Получение списка категорий по национальной кухне блюда.
     * @return список категорий по национальной кухне блюда.
     */
    public LinkedList<Category> getRecipiesNationalKitchen() {
        String categoryQuery = "SELECT ID FROM Category WHERE is_national_kitchen = 1";

        return getCategoryFromQuery(categoryQuery);
    }
}