package ru.spbau.cloudmanager;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.StrictMode;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

//везде нумерация с нуля.
public class CloudManager {
    private final String CLOUDINARY_URL = "cloudinary://285162791646134:yGqzM1FdReQ8uPa1taEUZihoNgI@dxc952wrd";
    private final String TAG = "CloudManagerTAG";
    private final String url = "jdbc:mysql://mysql1.gear.host:3306/foodmanagertest";
    private final String user = "foodmanagertest";
    private final String password = "Wc22_0f_0TA2";
    private Connection connection;
    private AssetManager manager;

    /**
     * Инициализация разрешений для создания соединения с БД. Загрузка драйвера.
     * @throws Exception не получилось найти драйвер для БД или не удалось установить соединение.
     */
    public CloudManager(Context context) throws Exception {
        manager = context.getAssets();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(url, user, password);
    }

    /**
     * Скрипт был задуман, чтобы загружать рецепты из source. Старые данные удаляем.
     */
    private void dropExistingTables(Statement stmt) throws SQLException {
        // only for MySQL!
        String queryDisableForeignChecks = "SET FOREIGN_KEY_CHECKS = 0";
        String queryDeleteCategory = "DROP TABLE IF EXISTS Category";
        String queryDeleteRecipe = "DROP TABLE IF EXISTS Recipe";
        String queryDeleteStep = "DROP TABLE IF EXISTS Step";
        String queryDeleteRecipeToCategory = "DROP TABLE IF EXISTS Recipe_to_category";
        String queryDeleteImage = "DROP TABLE IF EXISTS Image";
        String queryDeleteIngredient = "DROP TABLE IF EXISTS Ingredient";
        String queryDeleteIngredientToRecipe = "DROP TABLE IF EXISTS Ingredient_to_recipe";
        String queryEnableForeignChecks = "SET FOREIGN_KEY_CHECKS = 1";
        stmt.executeUpdate(queryDisableForeignChecks);
        stmt.executeUpdate(queryDeleteCategory);
        stmt.executeUpdate(queryDeleteRecipe);
        stmt.executeUpdate(queryDeleteRecipeToCategory);
        stmt.executeUpdate(queryDeleteStep);
        stmt.executeUpdate(queryDeleteImage);
        stmt.executeUpdate(queryDeleteIngredientToRecipe);
        stmt.executeUpdate(queryDeleteIngredient);
        stmt.executeUpdate(queryEnableForeignChecks);
    }

    /**
     * Создание таблицы Category.
     */
    private void createTableCategory(Statement stmt) throws Exception {
        String queryCreationOfCategory = "CREATE TABLE Category (" +
                "ID INTEGER PRIMARY KEY NOT NULL, " +
                "name TEXT NOT NULL, " +
                "is_national_kitchen INTEGER, " + // группировка по национальной кухне
                "is_category_dish INTEGER, " + // по категории блюда: салаты, закуски и тд.
                "is_meal INTEGER)"; // обед, завтрак, ужин

        stmt.executeUpdate(queryCreationOfCategory);

        String categoriesPath = "source/categories";
        BufferedReader readerCategories = getReaderFromPath(categoriesPath);

        String category;
        for (int i = 0; (category = readerCategories.readLine()) != null; i++) {
            int is_national_kitchen = 0;
            int is_category_dish = 0;
            int is_meal = 0;

            // set category type
            if (category.contains("кухня")) {
                is_national_kitchen = 1;
            } else if (category.equals("Обеды") || category.equals("Ужины")
                                                || category.equals("Завтраки")) {
                is_meal = 1;
            } else {
                is_category_dish = 1;
            }

            String insertCategory = "INSERT INTO Category " +
                                    "(ID, name, is_national_kitchen, is_category_dish, is_meal) " +
                                    "VALUES (" + i + ", '" + category + "', " +
                                    is_national_kitchen + ", " + is_category_dish
                                    + ", " + is_meal + ")";
            stmt.executeUpdate(insertCategory);
        }
    }

    /**
     * Создание остальных таблиц. Наполнение будет происходит в методе setupCloudServices
     */
    private void createOtherEmptyTables(Statement stmt) throws SQLException {
        String queryCreationRecipe = "CREATE TABLE Recipe (" +
                "ID INTEGER PRIMARY KEY NOT NULL, " +
                "name TEXT NOT NULL, " +
                "description TEXT NOT NULL)";

        stmt.executeUpdate(queryCreationRecipe);

        String queryCreationStep = "CREATE TABLE Step (" +
                "ID INTEGER PRIMARY KEY NOT NULL, " +
                "recipe_ID INTEGER NOT NULL, " +
                "description TEXT NOT NULL, " +
                "FOREIGN KEY (recipe_ID) REFERENCES Recipe(ID))";

        stmt.executeUpdate(queryCreationStep);

        String queryCreationRecipeToCategory = "CREATE TABLE Recipe_to_category (" +
                "recipe_ID INTEGER NOT NULL, " +
                "category_ID INTEGER NOT NULL, " +
                "FOREIGN KEY (recipe_ID) REFERENCES Recipe(ID), " +
                "FOREIGN KEY (category_ID) REFERENCES Category(ID))";

        stmt.executeUpdate(queryCreationRecipeToCategory);

        String queryCreationImage = "CREATE TABLE Image (" +
                "ID INTEGER PRIMARY KEY NOT NULL, " +
                "entity_type INTEGER NOT NULL, " +
                "entity_ID INTEGER NOT NULL, " +
                "link TEXT NOT NULL)";

        stmt.executeUpdate(queryCreationImage);

        String queryCreationOfIngredient = "CREATE TABLE Ingredient " +
                "(ID INTEGER PRIMARY KEY NOT NULL, " +
                "name TEXT NOT NULL)";

        stmt.executeUpdate(queryCreationOfIngredient);

        String queryCreationOfIngredientToRecipe = "CREATE TABLE Ingredient_to_recipe (" +
                "Ingredient_ID INTEGER NOT NULL, " +
                "recipe_ID INTEGER NOT NULL, " +
                "measure INTEGER NOT NULL, " +
                "quantity REAL NOT NULL, " +
                "FOREIGN KEY (Ingredient_ID) REFERENCES Ingredient(ID), " +
                "FOREIGN KEY (recipe_ID) REFERENCES Recipe(ID))";

        stmt.executeUpdate(queryCreationOfIngredientToRecipe);

        String queryCreationOfTableLikes = "CREATE TABLE Likes (" +
                                           "user_ID TEXT NOT NULL, " +
                                           "recipe_ID INTEGER NOT NULL, " +
                                           "FOREIGN KEY (recipe_ID) REFERENCES Recipe(ID))";
        stmt.executeUpdate(queryCreationOfTableLikes);
    }

    /**
     * Загрузка изображения.
     * @param path путь до изображения в папке assets.
     * @return онлайн ссылка на изображение в сервисе cloudinary
     */
    private String uploadImage(String path) throws Exception {
        InputStream imageIn = manager.open(path);

        Log.d(TAG, path);
        Cloudinary cloudinary = new Cloudinary(CLOUDINARY_URL);
        Map result = cloudinary.uploader().upload(imageIn, ObjectUtils.emptyMap());
        JSONObject jsonObject = new JSONObject(result);
        String link = jsonObject.getString("url");
        Log.d(TAG, link);

        return link;
    }

    /**
     * Получение ридера файла.
     * @param path путь до файла в папке assets.
     * @return ридер для чтения файла.
     */
    private BufferedReader getReaderFromPath(String path) throws IOException {
        InputStream is = manager.open(path);
        return new BufferedReader(new InputStreamReader(is, "UTF-8"));
    }

    /**
     * Вставка картинки в базу данных.
     * @param number это одновременно и ID шага и ключ.
     * @param link ссылка на изображение.
     * @throws SQLException
     */
    private void insertImage(int number, String link) throws SQLException {
        String insertImage = "INSERT INTO Image VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = null;
        preparedStatement = connection.prepareStatement(insertImage);
        preparedStatement.setInt(1, number); // картинок столько же сколько и шагов
        preparedStatement.setInt(2, 0);
        preparedStatement.setInt(3, number); // ID шага
        preparedStatement.setString(4, link);
        preparedStatement.execute();
    }

    /**
     * Создание БД на сервере.
     */
    public void setupCloudServices() throws Exception {
        connection.setAutoCommit(false);
        Statement stmt = connection.createStatement();
        dropExistingTables(stmt);
        createTableCategory(stmt);
        createOtherEmptyTables(stmt);

        int numberOfAllStep = 0;
        int numberOfAllIngredients = 0;
        String recipesPath = "source/recipes";
        Log.d(TAG, "Created empty tables");
        for (String recipeID : manager.list(recipesPath)) {
            Log.d("CloudManagerTAG", recipeID);

            String descFilePath = "source/recipes/" + recipeID + "/description";
            BufferedReader readerDescription = getReaderFromPath(descFilePath);
            String description = readerDescription.readLine();

            String fileWithNamePath = "source/recipes/" + recipeID + "/name";
            BufferedReader readerRecipeName = getReaderFromPath(fileWithNamePath);
            String name = readerRecipeName.readLine();

            String insertRecipe = "INSERT INTO Recipe (ID, name, description) VALUES " +
                                  "(" + Integer.parseInt(recipeID) + ", '" + name + "', '"
                                  + description + "')";

            stmt.executeUpdate(insertRecipe);

            String stepsPath = "source/recipes/" + recipeID + "/stepbystep/stepbystep";
            BufferedReader readerSteps = getReaderFromPath(stepsPath);
            String step;

            // считается, что количество шагов = количество картинок
            // загрузили шаги интструкции
            for (int i = 1; (step = readerSteps.readLine()) != null; i++) {
                String insertStep = "INSERT INTO Step (ID, recipe_ID, description) VALUES " +
                                    "(" + numberOfAllStep + ", " + Integer.parseInt(recipeID)
                                    + ", '" + step + "')";

                stmt.executeUpdate(insertStep);

                String stepImagePath = "source/recipes/" + recipeID + "/stepbystep/" + i + ".jpg";
                String link = uploadImage(stepImagePath);
                insertImage(numberOfAllStep, link);

                numberOfAllStep++;
            }

            // загрузка категорий рецепта
            String recipeCategoriesPath = "source/recipes/" + recipeID + "/category";
            BufferedReader readerRecipeCategories = getReaderFromPath(recipeCategoriesPath);
            String recipeCategory;
            while ((recipeCategory = readerRecipeCategories.readLine()) != null) {
                String queryFindCategoryID = "SELECT ID FROM Category WHERE name = '"
                                             + recipeCategory + "'";
                ResultSet entry = stmt.executeQuery(queryFindCategoryID);
                int categoryID = -1;
                if (entry.next())
                    categoryID = entry.getInt("ID");
                entry.close();

                String insertRecipeToCategory = "INSERT INTO Recipe_to_category " +
                                                "(recipe_ID, category_ID) VALUES " +
                                                "(" + Integer.parseInt(recipeID) + ", "
                                                + categoryID + ")";
                stmt.executeUpdate(insertRecipeToCategory);
            }

            // загрузка ингредиентов рецепта
            String recipeIngredientsPath = "source/recipes/" + recipeID + "/ingredients";
            BufferedReader readerRecipeIngredients = getReaderFromPath(recipeIngredientsPath);
            String ingredientLine;
            while ((ingredientLine = readerRecipeIngredients.readLine()) != null) {
                String ingredientName = null;
                String quantityWithKind = null;
                for (int index = 0; index < ingredientLine.length(); index++) {
                    String two = ingredientLine.substring(index, index + 3);
                    if (two.equals(" — ") || two.equals(" - ")) {
                        ingredientName = ingredientLine.substring(0, index);
                        quantityWithKind = ingredientLine.substring(index + 3);
                        break;
                    }
                }

                String insertIngredient = "INSERT INTO Ingredient (ID, name) VALUES " +
                                          "(" + numberOfAllIngredients + ", '"
                                          + ingredientName + "')";

                stmt.executeUpdate(insertIngredient);

                Measure measure = null;
                if (quantityWithKind.endsWith("зуб")) {
                    measure = Measure.cloves;
                }

                if (quantityWithKind.endsWith("ст.л.")) {
                    measure = Measure.tablespoon;
                }

                if (quantityWithKind.endsWith("ч.л.")) {
                    measure = Measure.teaspoon;
                }

                if (quantityWithKind.endsWith("шт")) {
                    measure = Measure.apiece;
                }

                if (quantityWithKind.endsWith("мл")) {
                    measure = Measure.ml;
                }

                if (quantityWithKind.endsWith("г")) {
                    measure = Measure.gr;
                }

                if (quantityWithKind.endsWith("по вкусу")) {
                    measure = Measure.byTaste;
                }

                if (quantityWithKind.endsWith("щепотка")) {
                    measure = Measure.pinch;
                }

                String quantityString = null;
                if (measure != Measure.pinch && measure != Measure.byTaste) {
                    quantityString = quantityWithKind.substring(0, quantityWithKind.indexOf(' '));
                }

                double quantity = -1;
                if (quantityString != null) {
                    quantityString = quantityString.replace(',', '.');
                    quantity = Double.parseDouble(quantityString);
                }

                String insertIngredientToRecipe = "INSERT INTO Ingredient_to_recipe (" +
                                                  "ingredient_ID, recipe_ID, measure, quantity) " +
                                                  "VALUES (" + numberOfAllIngredients + ", "
                                                   + Integer.parseInt(recipeID) + ", " +
                                                   measure.ordinal() + ", " + quantity + ")";

                stmt.executeUpdate(insertIngredientToRecipe);
                numberOfAllIngredients++;
            }

        }

        stmt.close();
        connection.commit();
        connection.close();
    }
}