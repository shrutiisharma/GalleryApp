package com.streamliners.galleryapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.models.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private final Context context;
    private final List<Item> allItems;
    private List<Item> visibleItems;



    /**
     * Constructor
     * To initiate the object with
     * @param context context for inflating purpose
     * @param allItems list of all items(cards)
     */
    public ItemAdapter(Context context, List<Item> allItems){
        this.context = context;
        this.allItems = allItems;
        this.visibleItems = allItems;
    }




    /**
     * Inflates layout & returns viewHolder for given type
     * Called when RecyclerView needs a new {@link ItemViewHolder} of the given type to represent
     * an item.
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(ItemViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary View#findViewById(int) calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate layout
        ItemCardBinding binding = ItemCardBinding.inflate(LayoutInflater.from(context), parent, false);

        //Create & return viewHolder
        return new ItemViewHolder(binding);
    }

    /**
     * Binds data of given position to the view in viewHolder
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ItemViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ItemViewHolder#getBindingAdapterPosition()} which
     * will have the updated adapter position.
     * <p>
     * Override {@link #ItemAdapter(Context, List)} ViewHolder(ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ItemViewHolder holder, int position) {
        Item item = visibleItems.get(position);

        //inflate & bind data in card
        holder.b.title.setText(item.label);
        holder.b.title.setBackgroundColor(item.color);
        Glide.with(context)
                .asBitmap()
                .load(item.url)
                .into(holder.b.imageView);
    }

    /**
     * Returns the number of data items/size to display
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     *          Return the length of the list
     */
    @Override
    public int getItemCount() {
        return visibleItems.size();
    }





    /**
     * To filter the list
     * @param query for search action
     */
    public void filter(String query) {

        //No query, show all items
        if (query.trim().isEmpty()){
            visibleItems = allItems;
            notifyDataSetChanged();
            return;
        }

        //filter & add to visibleCourses
        List<Item> temp = new ArrayList<>();
        query = query.toLowerCase();

        for (Item item : allItems) {
            if (item.label.toLowerCase().contains(query))
                temp.add(item);
        }

        visibleItems = temp;

        //Refresh list
        notifyDataSetChanged();
    }



    /**
     * To sort the list alphabetically
     */
    public void sortItemsAlphabetically() {

        //noinspection Convert2Lambda
        Collections.sort(allItems, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.label.compareTo(o2.label);
            }
        });

        visibleItems = allItems;
        notifyDataSetChanged();
    }


    /**
     * To Move Items For Drag-Drop Functionality
     * @param fromPosition initial position
     * @param toPosition final position
     */
    public void onItemMove(int fromPosition, int toPosition) {

        Collections.swap(allItems, allItems.indexOf(visibleItems.get(fromPosition)), allItems.indexOf(visibleItems.get(toPosition)));
        Collections.swap(visibleItems, fromPosition, toPosition);

        notifyItemMoved(fromPosition, toPosition);
    }



    /**
     * ViewHolder
     * Represents view holder for the recycler view
     */
    static class ItemViewHolder extends RecyclerView.ViewHolder{
        //declare view binding object
        ItemCardBinding b;

        /**
         * To give binding to the holder
         * @param b binding of the view
         */
        public ItemViewHolder(ItemCardBinding b) {
            super(b.getRoot());
            this.b = b;
        }

    }
}
