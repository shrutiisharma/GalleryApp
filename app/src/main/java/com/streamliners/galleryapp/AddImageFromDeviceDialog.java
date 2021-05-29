package com.streamliners.galleryapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.streamliners.galleryapp.databinding.ChipColorBinding;
import com.streamliners.galleryapp.databinding.ChipLabelBinding;
import com.streamliners.galleryapp.databinding.DialogAddDeviceImageBinding;
import com.streamliners.galleryapp.models.Item;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddImageFromDeviceDialog {

    private Context context;
    private OnCompleteListener listener;
    private String imageUrl;
    private DialogAddDeviceImageBinding b;
    private AlertDialog dialog;
    private LayoutInflater inflater;
    private Bitmap bitmap;
    private Set<Integer> colors;
    private boolean isCustomLabel;



    /**
     * Inflate & Show the Dialog
     * @param context : To show the dialog, context is needed; hence, it is passed.
     * @param imageUrl : selected image
     * @param listener : For creating asynchronous callback.
     */
    void show(Context context, String imageUrl, OnCompleteListener listener){

        this.context = context;
        this.imageUrl = imageUrl;
        this.listener = listener;

        //Inflate Dialog's Layout
        if (context instanceof GalleryActivity) {
            inflater = ((GalleryActivity) context).getLayoutInflater();
            b = DialogAddDeviceImageBinding.inflate(inflater);
        }
        else{
            dialog.dismiss();
            listener.onError("Cast Exception.");
            return;
        }

        //Create & show dialog
        dialog = new MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
                .setView(b.getRoot())
                .show();

        fetchImage(imageUrl);
        handleAddButton();
        handleShareImageEvent();
    }




    // ImageFetcher ----------------------------------------------------------------------------------

    /**
     * To fetch image
     * @param url : The image is fetched from the device's gallery using Glide through this url.
     */
    private void fetchImage(String url){
        Glide.with(context)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@Nullable Bitmap resource,@Nullable Transition<? super Bitmap> transition) {
                        bitmap = resource;
                        extractPaletteFromBitmap();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }




    // PaletteHelper ---------------------------------------------------------------------------------

    /**
     * To extract palette from bitmap
     */
    private void extractPaletteFromBitmap(){
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
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

    /**
     * Color Chips
     * @param colors : set of colors present in the image
     */
    private void inflateColorChips(Set<Integer> colors) {
        for (int color : colors){
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            b.colorChips.addView(binding.getRoot());
        }
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
                        inflateColorChips(colors);
                        inflateLabelChips(strings);
                        b.imageView.setImageBitmap(bitmap);
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

    /**
     * Label Chips
     * @param labels : list of labels of the image from device
     */
    private void inflateLabelChips(List<String> labels) {
        for (String label : labels){
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
            binding.getRoot().setText(label);
            b.labelChips.addView(binding.getRoot());
        }
        handleCustomLabelInput();
    }

    /**
     * Handle Custom Label Input
     */
    private void handleCustomLabelInput() {
        ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
        binding.getRoot().setText(R.string.custom);
        b.labelChips.addView(binding.getRoot());

        binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                b.customLabelTIL.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                isCustomLabel = isChecked;
            }
        });
    }



    //Handle onClick Events ------------------------------------------------------------------

    /**
     * Handle AddImage Button Clicked Event
     */
    private void handleAddButton() {
        b.imageView.setImageBitmap(bitmap);
        b.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorChipId = b.colorChips.getCheckedChipId()
                        ,labelChipId = b.labelChips.getCheckedChipId();

                //Guard Code
                if(colorChipId== -1 || labelChipId== -1){
                    Toast.makeText(context,"Please choose color and label!",Toast.LENGTH_SHORT).show();
                    return;
                }

                //Get Color and Label:
                String label;
                if(isCustomLabel){
                    label = b.customLabelET.getText().toString().trim();
                    if(label.isEmpty()){
                        Toast.makeText(context,"Please enter custom label!",Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                else {
                    label = ((Chip) b.labelChips.findViewById(labelChipId)).getText().toString();
                }

                int color = ((Chip) b.colorChips.findViewById(colorChipId)).getChipBackgroundColor().getDefaultColor();

                //Send Callback
                listener.onImageAdded (new Item(imageUrl, color, label));
                dialog.dismiss();
            }
        });
    }


    /**
     * Handle ShareImage Button Clicked Event
     */
    private void handleShareImageEvent() {
        b.shareImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Glide.with(context)
                            .asBitmap()
                            .load(imageUrl)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    // Calling the intent to share the bitmap
                                    Bitmap icon = resource;
                                    Intent share = new Intent(Intent.ACTION_SEND);
                                    share.setType("image/jpeg");

                                    ContentValues values = new ContentValues();
                                    values.put(MediaStore.Images.Media.TITLE, "title");
                                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                                    Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            values);


                                    OutputStream outputStream;
                                    try {
                                        outputStream = context.getContentResolver().openOutputStream(uri);
                                        icon.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                        outputStream.close();
                                    } catch (Exception e) {
                                        System.err.println(e.toString());
                                    }

                                    share.putExtra(Intent.EXTRA_STREAM, uri);
                                    context.startActivity(Intent.createChooser(share, "Share Image"));
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });

                } catch (Exception e) {
                    Log.e("Error on sharing", e + " ");
                    Toast.makeText(context, "App not Installed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




    /**
     * Listener
     * To pass the selected final data to the gallery activity, listener is created.
     */
    interface OnCompleteListener{
        void onImageAdded(Item item);
        void onError(String error);
    }
}
