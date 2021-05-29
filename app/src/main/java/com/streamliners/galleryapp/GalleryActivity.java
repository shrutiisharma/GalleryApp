package com.streamliners.galleryapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.streamliners.galleryapp.databinding.ActivityGalleryBinding;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.models.Item;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    //create object for view binding
    ActivityGalleryBinding b;

    SharedPreferences preferences;
    List<Item> items = new ArrayList<>();

    private static final int REQUEST_LOAD_IMAGE = 0;

    private ItemCardBinding binding;

    /**
     * It initialises the activity.
     * @param savedInstanceState : reference to a Bundle object that is passed into the onCreate method of every Android Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        setTitle("Gallery");

        preferences = getPreferences(MODE_PRIVATE);
        loadSharedPreferences();

    }



    //Actions Menu methods-----------------------------------------------------------------------------

    /**
     * To inflate optionsMenu
     * @param menu : menu layout
     * @return true : it signifies that we have handled this event
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);
        return true;
    }


    /**
     * Handle click events
     * @param item : item (addImage icon here) present in the menu
     * @return true if the addImage icon is pressed else false
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_image){
            showAddImageDialog();
            return true;                                            //return true signifies that we have handled this event.
        }
        if(item.getItemId() == R.id.add_image_from_device){
            addFromDevice();
            return true;
        }
        return false;
    }




    //Fetch Image from Internet------------------------------------------------------------------------

    /**
     * To show addImage dialog
     */
    @SuppressLint("SourceLockedOrientationActivity")
    private void showAddImageDialog() {
        if (this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            // To set the screen orientation in portrait mode
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        new AddImageDialog()
                .show(this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        inflateViewForItem(item);
                        shareCard();
                        items.add(item);
                    }

                    @Override
                    public void onError(String error) {
                        new MaterialAlertDialogBuilder(GalleryActivity.this)
                                .setTitle("Error")
                                .setMessage(error)
                                .show();
                    }
                });
    }

    /**
     * To inflate view for the item
     * @param item to be added in the gallery activity in a card view
     */
    private void inflateViewForItem(Item item) {

        // Inflate Data
        binding = ItemCardBinding.inflate(getLayoutInflater());

        //Bind Data
        Glide.with(this)
                .load(item.url)
                .into(binding.imageView);
        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);


        //Add it to the list
        b.list.addView(binding.getRoot());

        shareCard();

        if (items.isEmpty()) {
            b.noItemsTV.setVisibility(View.VISIBLE);
        } else {
            b.noItemsTV.setVisibility(View.GONE);
        }
    }




    //Fetch Image from Device---------------------------------------------------------------------------
    /**
     * To add image from the device
     */
    private void addFromDevice() {

        //Send the data
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        //noinspection deprecation
        startActivityForResult(intent, REQUEST_LOAD_IMAGE);
    }

    /**
     * Gets the result (image from device here)
     * @param requestCode : the request code of this intent
     * @param resultCode : the purpose for which this process/action was done/performed,
     *                     if fulfilled then RESULT_OK
     *                     else RESULT_CANCELLED
     * @param data : the intent we passed earlier (in addFromDevice method)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            //get data
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            String uri = selectedImage.toString();

            //show data
            new AddImageFromDeviceDialog().show(this, uri, new AddImageFromDeviceDialog.OnCompleteListener() {
                @Override
                public void onImageAdded(Item item) {
                    items.add(item);
                    inflateViewForItem(item);
                    b.noItemsTV.setVisibility(View.GONE);
                }

                @Override
                public void onError(String error) {
                    new MaterialAlertDialogBuilder(GalleryActivity.this)
                            .setTitle("ERROR")
                            .setMessage(error)
                            .show();
                }
            });
        }
    }




    //Share Image ---------------------------------------------------------------------------------------
    private void shareCard() {
        binding.shareCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap icon = loadBitmapFromView(b.list);

                // Calling the intent to share the bitmap
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpeg");

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "title");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values);


                OutputStream outputStream;
                try {
                    outputStream = GalleryActivity.this.getContentResolver().openOutputStream(uri);
                    icon.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                } catch (Exception e) {
                    System.err.println(e.toString());
                }

                share.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(share, "Share Image"));
            }
        });
    }

    public static Bitmap loadBitmapFromView(View v) {
        Bitmap bitmap;
        v.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return bitmap;
    }




    //Shared Preferences -------------------------------------------------------------------------------

    /**
     * To get Json for the Item
     * We use the GSON library to convert the model class to JSON String and we save that JSON String into the SharedPreferences.
     * @param item to be added in the gallery activity in a card view
     * @return json representation of item as a string
     */
    private String itemToJson(Item item){
        Gson json = new Gson();
        return json.toJson(item);
    }


    /**
     * To get Item from Json
     * We read back that JSON String and convert it back to the object when we want to read it.
     * @param string from which the object is to be deserialized into an object of the specified class i.e Item class here
     * @return an object of class Item from the string
     */
    private Item jsonToItem(String string){
        Gson json2= new Gson();
        return json2.fromJson(string, Item.class);
    }


    /**
     * To get data back from sharedPreferences.
     */
    private void loadSharedPreferences() {
        int itemCount = preferences.getInt(Constants.NO_OF_IMG,0);

        for (int i = 1; i <= itemCount; i++){

            //Make a new item and get objects from json
            Item item = jsonToItem(preferences.getString(Constants.ITEMS + i, ""));

            items.add(item);
            inflateViewForItem(item);
        }
    }


    /**
     * To save the data when the activity is in Pause state
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Putting all the objects in the shared preferences
        int itemCount = 0;
        for (Item item : items) {

            // Check for the item
            if (item != null) {

                // incrementing the index
                itemCount++;

                // Saving the item in the shared preferences
                preferences.edit()
                        .putString(Constants.ITEMS + itemCount, itemToJson(item))
                        .apply();
            }
        }
        preferences.edit()
                .putInt(Constants.NO_OF_IMG, itemCount)
                .apply();
    }


}