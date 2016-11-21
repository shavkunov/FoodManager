package ru.spbau.mit.foodmanager;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Step implements Serializable {
    /**
     * Шаг инструкции.
     */
    private String description;
    private Bitmap image;

    public Step(String description, Bitmap image) {
        this.description = description;
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public Bitmap getImage() {
        return image;
    }
}