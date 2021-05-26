package com.streamliners.galleryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.streamliners.galleryapp.databinding.ActivityGalleryBinding;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.models.Item;

public class GalleryActivity extends AppCompatActivity {

    //create object for view binding
    ActivityGalleryBinding b;


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
    private void showAddImageDialog() {
        new AddImageDialog()
                .show(this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
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
        binding.imageView.setImageBitmap(item.image);
        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);

        //Add it to the list
        b.list.addView(binding.getRoot());
    }

}