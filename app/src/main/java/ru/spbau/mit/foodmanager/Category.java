package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

public class Category {
    private String description;
    private int ID;
    private Bitmap image;
    private Context context;

    public Category(int ID, String description, Bitmap image, Context context) {
        this.ID = ID;
        this.description = description;
        this.image = image;
        this.context = context;
    }

    public int getID() {
        return ID;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<Recipe> getRecipes() {
        return CookBookStorage.getInstance(context).getRecipesOfCategory(this.ID);
    }

    public Bitmap getImage() {
        return image;
    }
}
