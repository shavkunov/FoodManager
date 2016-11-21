package ru.spbau.mit.foodmanager;

import android.media.Image;

import java.io.Serializable;
import java.util.ArrayList;

public class Category implements Serializable {
    private String description;
    private int ID;
    private ArrayList<Recipe> recipes;
    private Image categoryImage;

    public Category(int ID) {
        this.ID = ID;
        recipes = CookBookStorage.getRecipesOfCategory(ID);
        description = null; // query to CookBook
        categoryImage = null;
    }

    public String categoryDescription() {
        return description;
    }

    public ArrayList<Recipe> getRecipes() {
        return recipes;
    }

    public Image getCategoryImage() {
        return categoryImage;
    }
}
