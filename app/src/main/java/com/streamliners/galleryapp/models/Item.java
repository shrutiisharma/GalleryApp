package com.streamliners.galleryapp.models;


public class Item {

    public String url;
    public int color;
    public String label;

    /**
     * Constructor
     * @param url : url of random image generated
     * @param color : color selected
     * @param label : label selected
     */
    public Item(String url, int color, String label) {
        this.url = url;
        this.color = color;
        this.label = label;
    }
}
