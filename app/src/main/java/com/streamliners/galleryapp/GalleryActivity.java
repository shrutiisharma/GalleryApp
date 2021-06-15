package com.streamliners.galleryapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.streamliners.galleryapp.adapters.ItemAdapter;
import com.streamliners.galleryapp.databinding.ActivityGalleryBinding;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.helpers.ItemTouchHelperCallback;
import com.streamliners.galleryapp.models.Item;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    //create object for view binding
    ActivityGalleryBinding b;

    SharedPreferences preferences;
    Gson gson = new Gson();
    List<Item> items = new ArrayList<>();

    private static final int REQUEST_LOAD_IMAGE = 0;

    ItemAdapter adapter;

    ItemTouchHelper itemTouchHelper;
    Context context = this;

    ItemCardBinding binding;
    int mode = 0;
    private static final int RESULT = 1001 ;

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

        if(savedInstanceState != null){
            savedInstance(savedInstanceState);
        }else{
            loadSharedPreferences();
        }

        enableDisableDrag();
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

        //Search functionality
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView)menu.findItem(R.id.search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

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

        if (item.getItemId() == R.id.sort_Alphabetically){
            adapter.sortItemsAlphabetically();
            return true;
        }

        return false;
    }



    //Fetch Image from Internet------------------------------------------------------------------------

    /**
     * To show addImage dialog
     */
    private void showAddImageDialog() {
        new ImageOperationsDialog()
                .show(this, new ImageOperationsDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        items.add(item);
                        inflateViewForItem();
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
     */
    private void inflateViewForItem() {

        adapter = new ItemAdapter(this, items);

        b.list.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper1 = new ItemTouchHelper(simpleItemTouchCallback);
        adapter.setListItemAdapterHelper(itemTouchHelper);
        itemTouchHelper1.attachToRecyclerView(b.list);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        adapter.setListItemAdapterHelper(itemTouchHelper);

        b.list.setAdapter(adapter);
        dragDropButtonRestore();

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
            new ImageOperationsDialog().fetchDataFromDevice(uri, this, new ImageOperationsDialog.OnCompleteListener() {
                @Override
                public void onImageAdded(Item item) {
                    items.add(item);
                    inflateViewForItem();
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





    //Swipe to Remove & Drag and Drop Functionality ------------------------------------------------------------------------

    /**
     * Callback for Item
     * To implement swipe to remove & drag and drop functionality
     *//*
    private void callbackForItem() {
        itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(b.list);
        adapter.setListItemAdapterHelper(itemTouchHelper);
    }*/


    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback( 0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

            int position = viewHolder.getAbsoluteAdapterPosition();
            items.remove(position);

            Toast.makeText(GalleryActivity.this, "Image Removed!", Toast.LENGTH_SHORT).show();
            if (items.isEmpty())
                b.list.setVisibility(View.VISIBLE);
            
            adapter.notifyDataSetChanged();
        }
    };





    private void enableDisableDrag() {
        b.dragListener.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForColorStateLists")
            @Override
            public void onClick(View v) {
                if(mode == 0){
                    mode = 1;
                    adapter.mode = 1;
                    Toast.makeText(context,"Drag Enabled!",Toast.LENGTH_SHORT).show();
                    List<ItemAdapter.ItemViewHolder> holders = adapter.holderList;
                    b.dragListener.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
                    b.dragListener.setRippleColor(getResources().getColorStateList(R.color.purple_700));

                    b.dragListener.setImageResource(R.drawable.ic_swap_vertical_circle);
                    for(int i = 0; i < holders.size(); i++){
                        holders.get(i).eventListenerHandler();
                    }
                    itemTouchHelper.attachToRecyclerView(b.list);
                }
                else{
                    mode = 0;
                    adapter.mode = 0;
                    Toast.makeText(context,"Drag Disabled!",Toast.LENGTH_SHORT).show();
                    List<ItemAdapter.ItemViewHolder> holders = adapter.holderList;
                    for(int i = 0; i < holders.size(); i++){
                        holders.get(i).eventListenerHandler();
                    }
                    b.dragListener.setBackgroundTintList(getResources().getColorStateList(R.color.purple_700));
                    b.dragListener.setRippleColor(getResources().getColorStateList(R.color.purple_500));
                    b.dragListener.setImageResource(R.drawable.ic_swap_vertical_circle_unselected);
                    itemTouchHelper.attachToRecyclerView(null);
                }

            }
        });
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    void dragDropButtonRestore() {
        if (mode == 1) {
            mode = 1;
            adapter.mode = 1;
            List<ItemAdapter.ItemViewHolder> holders = adapter.holderList;
            b.dragListener.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
            b.dragListener.setRippleColor(getResources().getColorStateList(R.color.purple_700));

            b.dragListener.setImageResource(R.drawable.ic_swap_vertical_circle);
            for (int i = 0; i < holders.size(); i++) {
                holders.get(i).eventListenerHandler();
            }
            itemTouchHelper.attachToRecyclerView(b.list);
        } else {
            mode = 0;
            adapter.mode = 0;
            List<ItemAdapter.ItemViewHolder> holders = adapter.holderList;
            for (int i = 0; i < holders.size(); i++) {
                holders.get(i).eventListenerHandler();
            }
            b.dragListener.setBackgroundTintList(getResources().getColorStateList(R.color.purple_700));
            b.dragListener.setRippleColor(getResources().getColorStateList(R.color.purple_500));
            b.dragListener.setImageResource(R.drawable.ic_swap_vertical_circle_unselected);
            itemTouchHelper.attachToRecyclerView(null);
        }
    }


    @Override
    //Handling events of Context Menu Item Selection:
    public boolean onContextItemSelected (MenuItem item) {
        //For edit image option:
        if (item.getItemId() == R.id.editCard){
            editImage();
            return true;
        }


        //For delete image option:
        if (item.getItemId() == R.id.deleteCard){
            Toast.makeText(this, "Swipe to delete the item!", Toast.LENGTH_LONG).show();
            return true;
        }

        //For share image option:
        if(item.getItemId() == R.id.shareCard){
            sharePermission();
            return true;
        }
        return super.onContextItemSelected(item);
    }


    private void editImage() {
        int index = adapter.index;
        binding = adapter.itemCardBinding;
        new ImageOperationsDialog().editFetchImage(this, items.get(index), new ImageOperationsDialog.OnCompleteListener() {
            @Override
            public void onImageAdded(Item item) {
                items.set(index,item);
                adapter.notifyDataSetChanged();
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


    private void sharePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

                requestPermissions(permission, RESULT);
            }

            else
            {
                shareItem(binding);
            }
        }
        else{
            shareItem(binding);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
            shareItem(binding);
        }
        else{
            Toast.makeText(this, "Storage permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareItem(ItemCardBinding binding){

        Bitmap icon = loadBitmapFromView(binding.getRoot());


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
            outputStream = getContentResolver().openOutputStream(uri);
            icon.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (Exception e) {
            System.err.println(e.toString());
        }

        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "Share Image"));
    }



    /**
     * To Load Bitmap From View
     * @param view to load the view
     * @return bitmap i.e screenshot of card
     */
    public static Bitmap loadBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }


    /**
     * This method will save the item card so that when the screen is rotated the data is not lost.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String json = gson.toJson(items);
        outState.putString(Constants.NO_OF_IMG, json);
    }

    /**
     * This method contains the saved instance data and it will prevent loss of data when the screen
     *  is rotated.
     * @param savedInstanceState : reference to a Bundle object that is passed into the onCreate method of every Android Activity
     */
    private void savedInstance(Bundle savedInstanceState) {
        //b.noItemsTV.setVisibility(View.GONE);
        String json = savedInstanceState.getString(Constants.NO_OF_IMG, null);
        items = gson.fromJson(json, new TypeToken<List<Item>>() {
        }.getType());
        if(items != null){
            b.noItemsTV.setVisibility(View.GONE);
            inflateViewForItem();
        }else{
            items = new ArrayList<>();
            b.noItemsTV.setVisibility(View.VISIBLE);
        }
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
            inflateViewForItem();
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