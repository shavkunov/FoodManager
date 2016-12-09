package ru.spbau.mit.foodmanager;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

public class Category {
    private String description;
    private int ID;
    private CookBookStorage cookbook;
    private Bitmap image;

    public Category(int ID, String description, CookBookStorage cookbook, Bitmap image) {
        this.ID = ID;
        this.description = description;
        this.cookbook = cookbook;
        this.image = image;
    }

    public int getID() {
        return ID;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<Recipe> getRecipes() {
        return cookbook.getRecipesOfCategory(this.ID);
    }

    public Bitmap getImage() {
        return image;
    }
}
