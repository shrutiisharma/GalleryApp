package com.streamliners.galleryapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.streamliners.galleryapp.databinding.ChipColorBinding;
import com.streamliners.galleryapp.databinding.ChipLabelBinding;
import com.streamliners.galleryapp.databinding.DialogAddImageBinding;
import com.streamliners.galleryapp.models.Item;

import java.util.List;
import java.util.Set;

public class AddImageDialog implements ItemHelper.OnCompleteListener{

    private Context context;
    private OnCompleteListener listener;
    private DialogAddImageBinding b;
    private LayoutInflater inflater;
    private boolean isCustomLabel;
    private Bitmap image;
    private AlertDialog dialog;



    /**
     * Inflate & Show the Dialog
     * @param context : To show the dialog, context is needed; hence, it is passed.
     * @param listener : For creating asynchronous callback.
     */
    void show(Context context, OnCompleteListener listener) {
        this.context = context;
        this.listener = listener;

        //Inflate Dialog's Layout
        if (context instanceof GalleryActivity) {
            inflater = ((GalleryActivity) context).getLayoutInflater();
            b = DialogAddImageBinding.inflate(inflater);
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

        //Handle events
        handleDimensionsInput();

        //Hide errors for ET
        hideErrorsForET();
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




    //Step 1 : Input Dimensions-----------------------------------------------------------------------------

    /**
     * To handle user input of dimensions
     */
    private void handleDimensionsInput(){
        b.fetchImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get strings from ET
                String widthStr = b.widthET.getText().toString().trim(), heightStr = b.heightET.getText().toString().trim();

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
                    fetchRandomImage(height);
                } else if (heightStr.isEmpty()){
                    int width = Integer.parseInt(widthStr);
                    fetchRandomImage(width);
                }

                //Rectangular Image
                else {
                    int height = Integer.parseInt(heightStr);
                    int width = Integer.parseInt(widthStr);
                    fetchRandomImage(width, height);
                }
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





    //Step 2 : Fetch Random Image----------------------------------------------------------------------------

    /**
     * Rectangular Image
     * To fetch random rectangular image
     * @param width of the rectangle
     * @param height of the rectangle
     */
    private void fetchRandomImage(int width, int height) {
        new ItemHelper()
                .fetchData(width, height, context, this);
    }

    /**
     * Square Image
     * To fetch random square image
     * @param x : side of the square
     */
    private void fetchRandomImage(int x) {
        new ItemHelper()
                .fetchData(x, context, this);
    }






    //Step 3: Show Data------------------------------------------------------------------------------------------

    /**
     * To show data in the dialog
     * @param image : random image
     * @param colors : colors in the chips
     * @param labels : labels in the chips
     */
    private void showData(Bitmap image, Set<Integer> colors, List<String> labels) {
        this.image = image;

        b.imageView.setImageBitmap(image);
        inflateColorChips(colors);
        inflateLabelChips(labels);
        handleCustomLabelInput();
        handleAddImageEvent();

        b.progressIndicatorRoot.setVisibility(View.GONE);
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customLabelTIL.setVisibility(View.GONE);
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
        }
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

    /**
     * Color Chips
     * @param colors : set of colors present in the random image
     */
    private void inflateColorChips(Set<Integer> colors) {
        for (int color : colors){
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            b.colorChips.addView(binding.getRoot());
        }
    }

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
                    label = b.customLabelET.getText().toString().trim();
                    if (label.isEmpty()){
                        Toast.makeText(context, "Please enter custom label!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    label = ((Chip) b.labelChips.findViewById(labelChipId)).getText().toString();
                }

                int color = ((Chip) b.colorChips.findViewById(colorChipId))
                        .getChipBackgroundColor().getDefaultColor();

                //Send Callback
                listener.onImageAdded(new Item(image, color, label));
                dialog.dismiss();
            }
        });
    }





    //ItemHelper Callbacks----------------------------------------------------------------------------------------

    /**
     * onFetched Method from interface onCompleteListener
     * @param image random image
     * @param colors set of colors
     * @param labels list of labels
     */
    @Override
    public void onFetched(Bitmap image, Set<Integer> colors, List<String> labels) {
        //Bind Data
        showData(image, colors, labels);
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
