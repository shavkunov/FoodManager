package ru.spbau.mit.foodmanager;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

public class Category {
    private String description;
    private int ID;
    private ArrayList<Recipe> recipes;
    private Bitmap image;

    public Category() {}

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(ArrayList<Recipe> recipes) {
        this.recipes = recipes;
    }

    public Bitmap getCategoryImage() {
        return image;
    }

    public void setCategoryImage(Bitmap image) {
        this.image = image;
    }
}
