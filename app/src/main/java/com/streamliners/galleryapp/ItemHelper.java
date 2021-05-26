package com.streamliners.galleryapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemHelper {

    private Context context;
    private OnCompleteListener listener;

    private String rectangularImageURL = "https://picsum.photos/%d/%d"
            , squareImageURL = "https://picsum.photos/%d";

    private Bitmap bitmap;
    private Set<Integer> colors;



    // Triggers --------------------------------------------------------------------------------------

    /**
     * For Rectangular image
     * @param x width of rectangle
     * @param y height of rectangle
     * @param context context of the current state of the application
     *                As Glide needs context, hence we defined it
     * @param listener  for creating asynchronous callback
     *                  Listeners are used for any type of asynchronous event
     *                  in order to implement the code to run when an event occurs
     */
    void fetchData(int x, int y, Context context, OnCompleteListener listener){

        this.context = context;
        this.listener = listener;

        //...fetch here & when done,
        //Call listener.onFetched(image, colors, labels);

        fetchImage(String.format(rectangularImageURL, x, y));
    }


    /**
     * For Square image
     * @param x side of square
     * @param context context of the current state of the application
     *                As Glide needs context, hence we defined it
     * @param listener  for creating asynchronous callback
     *                  Listeners are used for any type of asynchronous event
     *                  in order to implement the code to run when an event occurs
     */
    void fetchData(int x, Context context, OnCompleteListener listener){

        this.context = context;
        this.listener = listener;

        //...fetch here & when done,
        //Call listener.onFetched(image, colors, labels);

        fetchImage(String.format(squareImageURL, x));
    }




    // ImageFetcher ----------------------------------------------------------------------------------

    /**
     * To Fetch Image
     * @param url The image is fetched from the Internet using Glide through this url.
     */
    void fetchImage(String url){
        Glide.with(context)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmap = resource;
                        extractPaletteFromBitmap();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        listener.onError("Image Load Failed!");
                    }
                });
    }




    // PaletteHelper ---------------------------------------------------------------------------------

    /**
     * To extract palette from bitmap
     *
     */
    private void extractPaletteFromBitmap() {

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                // Use generated instance
                colors = getColorsFromPalette(p);

                labelImage();
            }
        });
    }


    /**
     * To get colors from palette
     * @param p palette of colors present in the image
     * @return set of integers which are the colors
     */
    private Set<Integer> getColorsFromPalette(Palette p) {

        // We have used set as there is no duplicacy in set
        // so we don't have to remove zero
        colors = new HashSet<>();

        colors.add(p.getVibrantColor(0));
        colors.add(p.getLightVibrantColor(0));
        colors.add(p.getDarkVibrantColor(0));

        colors.add(p.getMutedColor(0));
        colors.add(p.getLightMutedColor(0));
        colors.add(p.getDarkMutedColor(0));

        colors.remove(0);

        return colors;
    }




    // LabelFetcher ------------------------------------------------------------------------

    /**
     * To get labels for the image.
     */
    private void labelImage() {
        //image from bitmap
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        // Task completed successfully
                        List<String> strings = new ArrayList<>();
                        for(ImageLabel label : labels){
                            strings.add(label.getText());
                        }
                        listener.onFetched(bitmap, colors, strings);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        listener.onError(e.toString());
                    }
                });
    }




    // Listener -----------------------------------------------------------------------------

    /**
     * Interface taken because interface is a group of related methods with empty bodies and
     * we have to define the process to be performed using them.
     *
     * The fetching of Image, Palette Colors & Labels are 3 asynchronous tasks so each have different callbacks.
     * Therefore we have to implement listener, which will then call the onFetched method when the data will be completely loaded.
     * The 3 asyncTasks can't be implemented parallelly so we implement them sequentially, as done above.
     */
    interface OnCompleteListener{
        void onFetched(Bitmap image, Set<Integer>colors, List<String> labels);
        void onError(String error);
    }
}
