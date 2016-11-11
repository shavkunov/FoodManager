package ru.spbau.mit.foodmanager.model;

import android.media.Image;

import java.util.ArrayList;

import static ru.spbau.mit.foodmanager.model.Tag.*;

public class CategoryTypeOfDish {
    private Image categoryImage;
    private ArrayList<Tag> tags;
    private int ID = 1;

    CategoryTypeOfDish() {
        categoryImage = null;
        tags = new ArrayList<>();
        tags.add(firstDish);
        tags.add(secondDish);
        tags.add(salad);
        tags.add(appetizer);
        tags.add(baking);
        tags.add(beverage);
        tags.add(soup);
        tags.add(pancake);
        tags.add(dessert);
        tags.add(barbeque);
        tags.add(porridge);
    }

    public int getID() {
        return ID;
    }

    public ArrayList<ArrayList<Recipe>> getRecipies() {
        return CookBookStorage.getRecipiesByTags(tags);
    }

    public String getCategoryType() {
        return "Категория блюд";
    }
}
