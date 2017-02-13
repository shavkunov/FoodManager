package ru.spbau.mit.foodmanager;

import android.graphics.Bitmap;

public class Step {
    /**
     * Шаг инструкции.
     */
    private String description;
    private String imageLink;
    private Bitmap image;

    public Step(String description, String imageLink) {
        this.description = description;
        this.imageLink = imageLink;
        this.image = null;
    }

    public Step(String description, Bitmap image) {
        this.description = description;
        this.image = image;
        this.imageLink = null;
    }

    public String getDescription() {
        return description;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) { this.image = image; }

    public String getImageLink() { return imageLink; }
}