package ru.spbau.mit.foodmanager.model;

import android.media.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Recipe {
    /**
     * Название рецепта.
     */
    private String name;

    /**
     * Его короткое описание.
     */
    private String shortDescription;

    /**
     * Его более детальное описание.
     */
    private String fullDescription;

    /**
     * Уникальный номер рецепта.
     */
    private long ID;

    /**
     * Содержит идентификаторы категорий, к которым принадлежит блюдо.
     */
    private ArrayList<Integer> categoryID;

    /**
     * Все картинки в рецепте, которые нужно отобразить.
     */
    private ArrayList<Image> allImages;

    /**
     * Ингредиенты! Пока каждый ингредиент будет измеряться в граммах.
     */
    private ArrayList<Ingredient> ingredients;

    /**
     * Описание каждого шага в инструкции готовки блюда.
     */
    private ArrayList<String> moveDescription;

    /**
     * Картинки, которые нужно отобразить в соответствующий шаг.
     * Т.е. на каждый отдельный экран хранится список номеров картинок для этого экрана.
     */
    private ArrayList<ArrayList<Integer>> moveImages;

    /**
     * Количество шагов на каждом экране.
     */
    private ArrayList<Integer> moveAmount;

    /**
     * @return возвращает список, где каждый элемент -- то что нужно отобразить на каждом экране.
     */
    public ArrayList<LinkedList<Move>> getStepByStep() {
        ArrayList<LinkedList<Move>> res = new ArrayList<>();

        int numberOfMove = 0;
        int numberOfStep = 0;
        for (int i = 0; i < moveAmount.size(); i++) {

            LinkedList<Move> curMove = new LinkedList<>();

            for (; numberOfMove <= moveAmount.get(numberOfStep); numberOfMove++) {
                Move newMove = new Move(moveDescription.get(numberOfStep), numberOfMove);

                for (int imageID : moveImages.get(numberOfStep)) {
                    Image im = Recipe.this.allImages.get(imageID);
                    newMove.addImage(im);
                }

                curMove.add(newMove);
            }

            numberOfStep++;
            res.add(curMove);
        }

        return res;
    }

    public int getNumberOfSteps() {
        return moveAmount.size();
    }

    Recipe() {
        /*
        TODO: Load all information about Recipe;
         */
    }

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public long getID() {
        return ID;
    }

    public class Move {
        /**
         * Каждый такой пункт инструкции будем отображать пока просто.
         * Т.е. сначала описание, а потом иллюстрации, если они нужны.
         */
        private String description;
        private int moveNumber;
        private LinkedList<Image> images;

        Move(String s, int moveNumber) {
            this.moveNumber = moveNumber;
            description = s;
            images = new LinkedList<>();
        }

        public void addImage(Image im) {
            images.add(im);
        }

        public String getDescription() {
            return description;
        }

        public LinkedList<Image> getImages() {
            return images;
        }
    }

    public class Ingredient {
        private String name;
        private int quantity;

        Ingredient(String s, int i) {
            name = s;
            quantity = i;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}