import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

//везде нумерация с нуля.
public class DatabaseCreator {
    public static void createBase() throws SQLException, IOException {
        File db = new File("content.db");
        if (db.exists()) {
            db.delete();
        }

        Connection c = DriverManager.getConnection("jdbc:sqlite:content.db");
        c.setAutoCommit(false);
        Statement stmt = c.createStatement();

        String queryCreationOfCategory = "CREATE TABLE Category (" +
                                         "ID INTEGER PRIMARY KEY NOT NULL, " +
                                         "name TEXT NOT NULL)";

        stmt.executeUpdate(queryCreationOfCategory);

        File categories = new File("source/categories");
        BufferedReader readerCategories = new BufferedReader(new FileReader(categories));

        String category;
        for (int i = 0; (category = readerCategories.readLine()) != null; i++) {
            String insertCategory = "INSERT INTO Category (ID, name) VALUES (" + i + ", '" + category + "')";
            stmt.executeUpdate(insertCategory);
        }

        String queryCreationRecipe = "CREATE TABLE Recipe (" +
                                     "ID INTEGER PRIMARY KEY NOT NULL, " +
                                     "name TEXT NOT NULL, " +
                                     "description TEXT NOT NULL)";

        stmt.executeUpdate(queryCreationRecipe);

        String queryCreationOfRecipeToWeekMenu = "CREATE TABLE Recipe_to_week_menu (" +
                                                 "recipe_ID INTEGER PRIMARY KEY NOT NULL," +
                                                 "date TEXT NOT NULL, " +
                                                 "FOREIGN KEY (recipe_ID) REFERENCES Recipe(ID))";

        stmt.executeUpdate(queryCreationOfRecipeToWeekMenu);

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
                                    "source BLOB NOT NULL)";

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

                File stepImage = new File("source/recipes/" + recipe.getName() + "/stepbystep/" + i + ".jpg");
                FileInputStream fis = new FileInputStream(stepImage);
                String insertImage = "INSERT INTO Image VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatament = c.prepareStatement(insertImage);

                preparedStatament.setInt(1, numberOfAllStep); // картинок столько же сколько и шагов
                preparedStatament.setInt(2, 0);
                preparedStatament.setInt(3, numberOfAllStep); // ID шага
                preparedStatament.setBinaryStream(4, fis, (int) stepImage.length());
                preparedStatament.execute();

                fis.close();
                numberOfAllStep++;
            }

            File recipeCategories = new File("source/recipes/" + recipe.getName() + "/category");
            BufferedReader readerRecipeCategories = new BufferedReader(new FileReader(recipeCategories));
            String recipeCategory;
            while ((recipeCategory = readerRecipeCategories.readLine()) != null) {
                String queryFindCategoryID = "SELECT * FROM Category WHERE name = '" + recipeCategory + "'";
                ResultSet entry = stmt.executeQuery(queryFindCategoryID);
                int categoryID = entry.getInt("ID");
                entry.close();

                String insertRecipeToCategory = "INSERT INTO Recipe_to_category (recipe_ID, category_ID) VALUES " +
                                    "(" + recipeID + ", " + categoryID + ")";
                stmt.executeUpdate(insertRecipeToCategory);
            }

            File recipeIngredients = new File("source/recipes/" + recipe.getName() + "/ingredients");
            BufferedReader readerRecipeIngredients = new BufferedReader(new FileReader(recipeIngredients));
            String ingredientLine;
            while ((ingredientLine = readerRecipeIngredients.readLine()) != null) {
                //System.out.println(recipe.getName());
                //System.out.println(ingredientLine);
                String ingredientName = null;
                String quantityWithKind = null;
                for (int index = 0; index < ingredientLine.length(); index++) {
                    //System.out.println(ingredientLine.charAt(index));
                    String two = ingredientLine.substring(index, index + 3);
                    if (two.equals(" — ") || two.equals(" - ")) {
                        ingredientName = ingredientLine.substring(0, index);
                        quantityWithKind = ingredientLine.substring(index + 3);
                        //System.out.println(ingredientName + " $ " + quantityWithKind);
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