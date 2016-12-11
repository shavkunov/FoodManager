import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

//везде нумерация с нуля.
public class DatabaseCreator {
    public static void createBase() throws Exception {
        final String url = "jdbc:mysql://mysql1.gear.host:3306/foodmanagertest";
        final String user = "foodmanagertest";
        final String password = "Wc22_0f_0TA2";

        Connection c = DriverManager.getConnection(url, user, password);
        c.setAutoCommit(false);
        Statement stmt = c.createStatement();

        // only for MySQL!
        String queryDisableForeignChecks = "SET FOREIGN_KEY_CHECKS = 0";
        String queryDeleteCategory = "DROP TABLE IF EXISTS Category";
        String queryDeleteRecipe = "DROP TABLE IF EXISTS Recipe";
        String queryDeleteRecipeToWeekMenu = "DROP TABLE IF EXISTS Recipe_to_week_menu";
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
        stmt.executeUpdate(queryDeleteRecipeToWeekMenu);
        stmt.executeUpdate(queryDeleteStep);
        stmt.executeUpdate(queryDeleteImage);
        stmt.executeUpdate(queryDeleteIngredientToRecipe);
        stmt.executeUpdate(queryDeleteIngredient);
        stmt.executeQuery(queryEnableForeignChecks);

        String queryCreationOfCategory = "CREATE TABLE Category (" +
                                         "ID INTEGER PRIMARY KEY NOT NULL, " +
                                         "name TEXT NOT NULL, " +
                                         "is_national_kitchen INTEGER, " + // группировка по национальной кухне
                                         "is_category_dish INTEGER, " + // по категории блюда: салаты, закуски и тд.
                                         "is_meal INTEGER)"; // обед, завтрак, ужин

        stmt.executeUpdate(queryCreationOfCategory);

        File categories = new File("source/categories");
        BufferedReader readerCategories = new BufferedReader(new FileReader(categories));

        String category;
        for (int i = 0; (category = readerCategories.readLine()) != null; i++) {
            int is_national_kitchen = 0;
            int is_category_dish = 0;
            int is_meal = 0;

            // set category type
            if (category.contains("кухня")) {
                is_national_kitchen = 1;
            } else if (category.equals("Обеды") || category.equals("Ужины") || category.equals("Завтраки")) {
                is_meal = 1;
            } else {
                is_category_dish = 1;
            }

            String insertCategory = "INSERT INTO Category (ID, name, is_national_kitchen, is_category_dish, is_meal) " +
                                    "VALUES (" + i + ", '" + category + "', " +
                                    is_national_kitchen + ", " + is_category_dish + ", " + is_meal + ")";
            stmt.executeUpdate(insertCategory);
        }

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
                                    "drive_ID TEXT NOT NULL)";

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

        int numberOfAllStep = 0;
        int numberOfAllIngredients = 0;
        File recipesDir = new File("source/recipes");
        DriveHelper.setUp();
        for (int recipeID = 0; recipeID < recipesDir.listFiles().length; recipeID++) {
            File recipe = recipesDir.listFiles()[recipeID];

            String description = new String(Files.readAllBytes(Paths.get(
                                            "source/recipes/" + recipe.getName() + "/description")));

            File fileWithName = new File("source/recipes/" + recipe.getName() + "/name");
            BufferedReader readerRecipeName = new BufferedReader(new FileReader(fileWithName));
            String name = readerRecipeName.readLine();

            String insertRecipe = "INSERT INTO Recipe (ID, name, description) VALUES " +
                                  "(" + recipeID + ", '" + name + "', '" + description + "')";

            stmt.executeUpdate(insertRecipe);

            File stepbystep = new File("source/recipes/" + recipe.getName() + "/stepbystep/stepbystep");
            BufferedReader readerSteps = new BufferedReader(new FileReader(stepbystep));
            String step;

            // считается, что количество шагов = количество картинок
            for (int i = 1; (step = readerSteps.readLine()) != null; i++) {
                String insertStep = "INSERT INTO Step (ID, recipe_ID, description) VALUES " +
                                    "(" + numberOfAllStep + ", " + recipeID + ", '" + step + "')";

                stmt.executeUpdate(insertStep);

                String stepImagePath = "source/recipes/" + recipe.getName() + "/stepbystep/" + i + ".jpg";
                String filename = recipe.getName() + "StepNumber" + i + ".jpg";

                String driveID = DriveHelper.uploadFile(filename, stepImagePath);
                //System.out.println(driveID);

                String insertImage = "INSERT INTO Image VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatament = c.prepareStatement(insertImage);
                preparedStatament.setInt(1, numberOfAllStep); // картинок столько же сколько и шагов
                preparedStatament.setInt(2, 0);
                preparedStatament.setInt(3, numberOfAllStep); // ID шага
                preparedStatament.setString(4, driveID);
                preparedStatament.execute();

                numberOfAllStep++;
            }

            File recipeCategories = new File("source/recipes/" + recipe.getName() + "/category");
            BufferedReader readerRecipeCategories = new BufferedReader(new FileReader(recipeCategories));
            String recipeCategory;
            while ((recipeCategory = readerRecipeCategories.readLine()) != null) {
                String queryFindCategoryID = "SELECT ID FROM Category WHERE name = '" + recipeCategory + "'";
                ResultSet entry = stmt.executeQuery(queryFindCategoryID);
                int categoryID = -1;
                if (entry.next())
                    categoryID = entry.getInt("ID");
                entry.close();

                String insertRecipeToCategory = "INSERT INTO Recipe_to_category (recipe_ID, category_ID) VALUES " +
                                    "(" + recipeID + ", " + categoryID + ")";
                stmt.executeUpdate(insertRecipeToCategory);
            }

            File recipeIngredients = new File("source/recipes/" + recipe.getName() + "/ingredients");
            BufferedReader readerRecipeIngredients = new BufferedReader(new FileReader(recipeIngredients));
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
                                          "(" + numberOfAllIngredients + ", '" + ingredientName + "')";

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
                                                  "ingredient_ID, recipe_ID, measure, quantity) VALUES " +
                                                  "(" + numberOfAllIngredients + ", " + recipeID + ", " +
                                                   measure.ordinal() + ", " + quantity + ")";

                stmt.executeUpdate(insertIngredientToRecipe);
                numberOfAllIngredients++;
            }

        }

        stmt.close();
        c.commit();
        c.close();
    }
}