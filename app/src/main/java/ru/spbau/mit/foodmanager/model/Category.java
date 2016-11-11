package ru.spbau.mit.foodmanager.model;

import android.media.Image;

import java.util.ArrayList;

public class Category {
    private String description;
    private int ID;
    private ArrayList<Recipe> recipes;
    private Image categoryImage;

    public Category(int ID) {
        this.ID = ID;
        recipes = CookBookStorage.getRecipesOfCategory(ID);
        description = null; // Загрузка из файла, который будет называться ID
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
