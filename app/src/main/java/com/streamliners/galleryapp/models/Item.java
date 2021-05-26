package com.streamliners.galleryapp.models;

import android.graphics.Bitmap;

public class Item {

    public Bitmap image;
    public int color;
    public String label;

    /**
     * Constructor
     * @param image : random image generated
     * @param color : color selected
     * @param label : label selected
     */
    public Item(Bitmap image, int color, String label) {
        this.image = image;
        this.color = color;
        this.label = label;
    }
}
