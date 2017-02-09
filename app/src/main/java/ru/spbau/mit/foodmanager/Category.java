package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;

public class Category {
    private String description;
    private int ID;
    private Bitmap image;

    public Category(int ID, String description, Bitmap image) {
        this.ID = ID;
        this.description = description;
        this.image = image;
    }

    public int getID() {
        return ID;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<Recipe> getRecipes(Context context) {
        return CookBookStorage.getInstance(context).getRecipesOfCategory(this.ID);
    }

    public Bitmap getImage() {
        return image;
    }
}
