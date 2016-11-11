package ru.spbau.mit.foodmanager.model;

import android.media.Image;

import java.util.ArrayList;

import static ru.spbau.mit.foodmanager.model.Tag.*;

public class CategoryNationalKitchen {
    private Image categoryImage;
    private ArrayList<Tag> tags;
    private int ID = 2;

    CategoryNationalKitchen() {
        categoryImage = null;
        tags = new ArrayList<>();
        tags.add(European);
        tags.add(American);
        tags.add(Italian);
        tags.add(Uzbek);
        tags.add(Georgian);
        tags.add(Indian);
        tags.add(Caucasian);
        tags.add(Russian);
        tags.add(Asian);
    }

    public int getID() {
        return ID;
    }

    public ArrayList<ArrayList<Recipe>> getRecipies() {
        return CookBookStorage.getRecipiesByTags(tags);
    }

    public String getCategoryType() {
        return "Кухня";
    }
}
