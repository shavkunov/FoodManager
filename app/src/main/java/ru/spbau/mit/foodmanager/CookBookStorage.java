package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import static ru.spbau.mit.foodmanager.Commands.*;

/**
 * Хранилище всех рецептов. Singleton. В этом классе слишком много методов, которые можно отнести
 * к другим классам. Будет изменено после рефакторинга.
 */
public class CookBookStorage {
    public static final String SERVER_IP = "138.68.91.54";
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
     */
    private final String CLOUDINARY_URL = "cloudinary://285162791646134:yGqzM1FdReQ8uPa1taEUZihoNgI@dxc952wrd";

    /**
     * ID пользователя.
     */
    private String userID;

    private CookBookStorage(Context context) {
        userID = Installation.getUserID(context);
    }

    /**
     * Изменение информации о рецепте. Можно было изменить рецепт, удалив его и добавив новый,
     * но использование этого метода эффективнее в количестве SQL запросов.
     * @param recipe информация этого рецепта будет помещена в БД.
     */
    public void changeRecipe(RecipeToChange recipe) {
        try {
            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                HttpURLConnection connection = storeRecipeInformation(recipe, changeRecipeCommand);

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                break;
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Unable to change recipe");
            e.printStackTrace();
        }
    }

    /**
     * Добавление рецепта в базу данных на сервере. Если одна из операций вставок провалилась,
     * то рецепт не будет встален полностью.
     * @param recipe добавление рецепта в базу данных на сервере.
     */
    public void insertRecipe(RecipeToChange recipe) {
        try {
            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                HttpURLConnection connection = storeRecipeInformation(recipe, insertRecipeCommand);

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                break;
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Unable to insert recipe");
            e.printStackTrace();
        }
    }

    private HttpURLConnection storeRecipeInformation(RecipeToChange recipe, String command) throws IOException {
        final HttpURLConnection connection =
                openHttpURLConnectionForServerCommand(command);

        ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
        output.writeObject(recipe.getName());
        output.writeObject(recipe.getDescription());
        output.writeObject(recipe.getCategoryIDs());
        output.writeObject(userID);
        output.writeObject(recipe.getIngredients());

        ArrayList<String> descriptions = new ArrayList<>();
        ArrayList<ByteArrayInputStream> transformedImages = new ArrayList<>();
        for (Step step : recipe.getSteps()) {
            descriptions.add(step.getDescription());
            transformedImages.add(convertImage(step.getImage()));
        }

        output.writeObject(descriptions);
        output.writeObject(transformedImages);

        if (command.equals(deleteRecipeCommand) || command.equals(changeRecipeCommand)) {
            output.writeInt(recipe.getID());
        }
        output.flush();
        output.close();

        return connection;
    }

    private ByteArrayInputStream convertImage(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapData = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapData);

        return bs;
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

    /**
     * Удаление рецепта из БД.
     */
    public void deleteRecipe(RecipeToChange recipe) {
        try {
            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                HttpURLConnection connection = storeRecipeInformation(recipe, deleteRecipeCommand);

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                break;
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Unable to delete recipe");
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
        Recipe recipe = null;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                                                                           getRandomDishCommand);
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeInt(categoryID);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                recipe = (Recipe) input.readObject();
                input.close();

                break;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Не удалось получить случайный рецепт категории");
                e.printStackTrace();
            }
        }

        return recipe;
    }

    /**
     * Является ли текущий пользователь владельцем рецепта.
     * @param recipe рецепт, который хотим проверить
     * @return true если recipe принадлежить пользователь и false иначе.
     */
    public boolean isUserOwnRecipe(Recipe recipe) {
        boolean answer = false;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection connection = openHttpURLConnectionForServerCommand(
                                                                         isUserOwnRecipeCommand);
                ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                output.writeInt(recipe.getID());
                output.writeObject(userID);
                output.flush();
                output.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    continue;
                }

                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                answer = input.readBoolean();
                input.close();

                break;
            } catch (Exception e) {
                Log.d(LOG_TAG, "Не удалось проверить принадлежность рецепта");
                e.printStackTrace();
            }
        }

        return answer;
    }

    private static HttpURLConnection openHttpURLConnectionForServerCommand(String command) throws IOException {
        final String urlString = "http://" + SERVER_IP + ':' + port + command;

        final URL url = new URL(urlString);

        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(HTTP_CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(HTTP_READ_TIMEOUT_MS);

        return connection;
    }
}