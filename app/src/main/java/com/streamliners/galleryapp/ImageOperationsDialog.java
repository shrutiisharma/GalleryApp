package com.streamliners.galleryapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.streamliners.galleryapp.databinding.ChipColorBinding;
import com.streamliners.galleryapp.databinding.ChipLabelBinding;
import com.streamliners.galleryapp.databinding.DialogImageOperationsBinding;
import com.streamliners.galleryapp.helpers.ItemHelper;
import com.streamliners.galleryapp.models.Item;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ImageOperationsDialog implements ItemHelper.OnCompleteListener{

    private Context context;
    private OnCompleteListener listener;
    private DialogImageOperationsBinding b;
    private LayoutInflater inflater;
    private boolean isCustomLabel;
    private AlertDialog dialog;
    private String url;
    private Item item;
    private boolean isAlreadyChecked;


    /**
     * Inflate & Show the Dialog
     * @param context : To show the dialog, context is needed; hence, it is passed.
     * @param listener : For creating asynchronous callback.
     */
    void show(Context context, OnCompleteListener listener) {

        if (!initializingDialog(context,listener)){
            return;
        }

        //Handle events
        handleDimensionsInput();
        handleShareImageEvent();

        //Hide errors for ET
        hideErrorsForET();
    }


    /**
     * To initialize the dialog
     * @param context : To show the dialog, context is needed; hence, it is passed.
     * @param listener : For creating asynchronous callback.
     * @return boolean true
     */
    private boolean initializingDialog(Context context,OnCompleteListener listener){
        this.context=context;
        this.listener = listener;

        //Inflate Dialog's Layout
        if (context instanceof GalleryActivity){
            inflater = ((GalleryActivity)context).getLayoutInflater();
            b = DialogImageOperationsBinding.inflate(inflater);
        }
        else {
            dialog.dismiss();
            listener.onError("Cast Exception:");
            return false;
        }

        //Create and Show Dialog:
        dialog= new MaterialAlertDialogBuilder(context,R.style.CustomDialogTheme)
                .setView(b.getRoot())
                .setCancelable(false)
                .show();

        return true;
    }




    //Utils-------------------------------------------------------------------------------------------------

    /**
     * To hide errors for edit text fields
     */
    private void hideErrorsForET() {
        b.widthET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                b.widthET.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    /**
     * Hide Keyboard
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(b.widthTIL.getWindowToken(), 0);
    }




    //Step 1 : Input Dimensions-----------------------------------------------------------------------------

    /**
     * To handle user input of dimensions
     */
    private void handleDimensionsInput(){
        b.fetchImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get strings from ET
                String widthStr = Objects.requireNonNull(b.widthET.getText()).toString().trim(), heightStr = Objects.requireNonNull(b.heightET.getText()).toString().trim();

                //Guard Code
                if (widthStr.isEmpty() && heightStr.isEmpty()) {
                    b.widthET.setError("Please enter at least one dimension!");
                    return;
                }

                //Update UI
                b.inputDimensionsRoot.setVisibility(View.GONE);
                b.progressIndicatorRoot.setVisibility(View.VISIBLE);

                //Hide Keyboard
                hideKeyboard();

                //Square image
                if (widthStr.isEmpty()){
                    int height = Integer.parseInt(heightStr);
                    try {
                        fetchRandomImage(height);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (heightStr.isEmpty()){
                    int width = Integer.parseInt(widthStr);
                    try {
                        fetchRandomImage(width);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //Rectangular Image
                else {
                    int height = Integer.parseInt(heightStr);
                    int width = Integer.parseInt(widthStr);
                    try {
                        fetchRandomImage(width, height);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }





    //Step 2 : Fetch Image----------------------------------------------------------------------------

    /**
     * Rectangular Image
     * To fetch random rectangular image
     * @param width of the rectangle
     * @param height of the rectangle
     */
    private void fetchRandomImage (int width, int height) throws IOException {
        new ItemHelper()
                .fetchData(width, height, context, this);
    }

    /**
     * Square Image
     * To fetch random square image
     * @param x : side of the square
     */
    private void fetchRandomImage(int x) throws IOException {
        new ItemHelper()
                .fetchData(x, context, this);
    }


    /**
     * Fetch image from device
     * @param url Image url
     * @param context To show the dialog, context is needed; hence, it is passed.
     * @param listener For creating asynchronous callback.
     */
    public void fetchDataFromDevice(String url,Context context,OnCompleteListener listener){
        this.listener = listener;
        this.context = context;

        if (context instanceof GalleryActivity) {
            inflater = ((GalleryActivity) context).getLayoutInflater();
            b = DialogImageOperationsBinding.inflate(inflater);
        } else {
            dialog.dismiss();
            listener.onError("Cast Exception");
            return;
        }

        dialog = new MaterialAlertDialogBuilder(context)
                .setView(b.getRoot())
                .show();

        b.inputDimensionsRoot.setVisibility(View.GONE);
        b.progressSubtitle.setText(R.string.fetching_image);
        b.progressIndicatorRoot.setVisibility(View.VISIBLE);

        new ItemHelper()
                .fetchData(url,context,this);

    }




    //Step 3: Show Data------------------------------------------------------------------------------------------

    /**
     * To show data in the dialog
     * @param url : url of the image in cache
     * @param colors : colors in the chips
     * @param labels : labels in the chips
     */
    private void showData(String url, Set<Integer> colors, List<String> labels) {
        this.url = url;

        b.progressIndicatorRoot.setVisibility(View.GONE);
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customLabelTIL.setVisibility(View.GONE);

        //Setting image view in binding
        Glide.with(context)
                .load(url)
                .into(b.imageView);

        inflateColorChips(colors);
        inflateLabelChips(labels);

        handleCustomLabelInput();
        handleAddImageEvent();
        handleShareImageEvent();
    }


    /**
     * Label Chips
     * @param labels : list of labels of the random image
     */
    private void inflateLabelChips(List<String> labels) {
        for (String label : labels){
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
            binding.getRoot().setText(label);
            b.labelChips.addView(binding.getRoot());

            //For preSelected Label chips on edit:
            if (item != null && item.label.equals(label)) {
                binding.getRoot().setChecked(true);
                isAlreadyChecked = true;
            }
        }
    }

    /**
     * Handle Custom Label Input
     */
    private void handleCustomLabelInput() {
        ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
        binding.getRoot().setText(R.string.custom);
        b.labelChips.addView(binding.getRoot());

        if(item!=null && !isAlreadyChecked){
            binding.getRoot().setChecked(true);
            b.customLabelTIL.setVisibility(View.VISIBLE);
            b.customLabelET.setText(item.label);
            isCustomLabel = true;
        }

        binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                b.customLabelTIL.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                isCustomLabel = isChecked;
            }
        });
    }

    /**
     * Color Chips
     * @param colors : set of colors present in the random image
     */
    private void inflateColorChips(Set<Integer> colors) {
        for (int color : colors){
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            b.colorChips.addView(binding.getRoot());

            //For preSelected Color chips on edit:
            if(item != null && item.color == color){
                binding.getRoot().setChecked(true);
            }
        }
    }





    //Handle onClick Events -------------------------------------------------------------------------------------------

    /**
     * Handle AddImage Button Clicked Event
     */
    private void handleAddImageEvent() {
        b.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorChipId = b.colorChips.getCheckedChipId()
                        , labelChipId = b.labelChips.getCheckedChipId();

                //Guard Code
                if (colorChipId == -1 || labelChipId == -1){
                    Toast.makeText(context, "Please choose color & label!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String label;

                //Get color & label
                if (isCustomLabel){
                    label = Objects.requireNonNull(b.customLabelET.getText()).toString().trim();
                    if (label.isEmpty()){
                        Toast.makeText(context, "Please enter custom label!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    label = ((Chip) b.labelChips.findViewById(labelChipId)).getText().toString();
                }

                int color = Objects.requireNonNull(((Chip) b.colorChips.findViewById(colorChipId))
                        .getChipBackgroundColor()).getDefaultColor();

                //Send Callback
                listener.onImageAdded(new Item(url, color, label));
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
                            .load(url)
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





    //Handle Edit Card Event -----------------------------------------------------------------------------------------

    /**
     * Fetch image to edit
     * @param context To show the dialog, context is needed; hence, it is passed.
     * @param item item to edit
     * @param listener For creating asynchronous callback.
     */
    public void editFetchImage(Context context,Item item,OnCompleteListener listener){
        this.url = item.url;
        this.item = item;

        if(!initializingDialog(context,listener)){
            return;
        }

        b.dialogHeader.setText(R.string.edit_image);
        b.addBtn.setText(R.string.update);
        b.progressSubtitle.setText(R.string.loading_image);
        b.choosePaletteTitle.setText(R.string.choose_a_new_palette_color);
        b.chooseLabelTitle.setText(R.string.choose_a_new_label);
        editCard(url);
    }


    /**
     * To edit card
     * @param imageUrl url of image
     */
    private void editCard(String imageUrl) {
        b.inputDimensionsRoot.setVisibility(View.GONE);
        b.progressIndicatorRoot.setVisibility(View.VISIBLE);

        new ItemHelper().editCard(imageUrl, context, this);
    }





    //ItemHelper Callbacks----------------------------------------------------------------------------------------

    /**
     * onFetched Method from interface onCompleteListener
     * @param url url of random image
     * @param colors set of colors
     * @param labels list of labels
     */
    @Override
    public void onFetched(String url, Set<Integer> colors, List<String> labels) {
        //Bind Data
        showData(url, colors, labels);
    }


    /**
     * onError Method from interface onCompleteListener
     * @param error error in fetching image/palette colors/labels
     */
    @Override
    public void onError(String error) {
        //Notify Error
        dialog.dismiss();
        listener.onError(error);
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
