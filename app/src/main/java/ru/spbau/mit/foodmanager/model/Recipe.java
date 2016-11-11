package ru.spbau.mit.foodmanager.model;

import android.media.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

enum Tag {firstDish, secondDish, salad, appetizer, baking, beverage, soup, pancake,
          dessert, barbeque, porridge,
          dinner, lunch, breakfast,
          European, American, Italian, Uzbek, Georgian, Indian, Caucasian, Russian, Asian}
          // and others tags

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
     * Какими свойствами обладает блюдо.
     */
    private ArrayList<Tag> property;

    /**
     * Информация, которую нужно отобразить пользователю для каждого шага.
     */
    private StepInformation info;

    /**
     * Все картинки в рецепте, которые нужно отобразить.
     */
    private ArrayList<Image> allImages;

    /**
     * Ингредиенты! Пока каждый ингредиент будет измеряться в граммах.
     */
    private ArrayList<Ingredient> ingredients;

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

    Recipe() {
        /*
        TODO: Load all information about Recipe;
         */
    }

    public class StepInformation {
        /**
         * Step я называю всю информацию, которая должна быть на отдельном экране.
         * На каждом экране будет инструкция по приготовлению.
         * Move это отдельный пункт этой инструкции.
         */

        /**
         * Пошаговая инструкция, как готовить блюдо.
         */
        private ArrayList<String> moveDescription;

        /**
         * Картинки, которые нужно отобразить в соответствующий шаг.
         * Т.е. на каждый отдельный экран хранится список номеров картинок для этого экрана.
         */
        private ArrayList<ArrayList<Integer>> moveImages;

        /**
         * Номер текущего экрана.
         */
        private int numberOfStep;

        /**
         * Номер текущего шага.
         */
        private int numberOfMove;

        /**
         * Количество шагов на каждом экране.
         */
        private ArrayList<Integer> moveAmount;

        StepInformation() {
            // TODO load moveDescription
            // TODO load moveAmount
            // TODO load moveImages
            numberOfStep = 1;
            numberOfMove = 1;
        }

        /**
         * Step information знает все, что нужно отобразить на каждом экране.
         * @return возвращает список шагов, которые нужно отобразить на текущем экране.
         */
        public LinkedList<Move> getNextStep() {
            LinkedList<Move> res = new LinkedList<>();

            for (; numberOfMove <= moveAmount.get(numberOfStep); numberOfMove++) {
                Move newMove = new Move(moveDescription.get(numberOfStep), numberOfMove);

                for (int imageID : moveImages.get(numberOfStep)) {
                    Image im = Recipe.this.allImages.get(imageID);
                    newMove.addImage(im);
                }

                res.add(newMove);
            }

            numberOfStep++;
            return res;
        }

        public int getNumberOfSteps() {
            return moveAmount.size();
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

    public StepInformation getStepByStep() {
        return info;
    }
}