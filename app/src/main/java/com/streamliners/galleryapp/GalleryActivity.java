package com.streamliners.galleryapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.streamliners.galleryapp.databinding.ActivityGalleryBinding;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.models.Item;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    //create object for view binding
    ActivityGalleryBinding b;

    SharedPreferences preferences;
    List<Item> items = new ArrayList<>();

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



    //Actions Menu methods------------------------------------------------

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
        return false;
    }


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
                        items.add(item);
                        inflateViewForItem(item);
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
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

        //Bind Data
        Glide.with(this)
                .load(item.url)
                .into(binding.imageView);
        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);


        //Add it to the list
        b.list.addView(binding.getRoot());

        if (items.isEmpty()) {
            b.noItemsTV.setVisibility(View.VISIBLE);
        } else {
            b.noItemsTV.setVisibility(View.GONE);
        }
    }




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
            Item item= jsonToItem(preferences.getString(Constants.ITEMS + i, ""));
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