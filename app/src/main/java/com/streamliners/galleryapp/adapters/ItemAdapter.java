package com.streamliners.galleryapp.adapters;

import android.content.Context;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.streamliners.galleryapp.GalleryActivity;
import com.streamliners.galleryapp.R;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.models.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> implements ItemTouchHelperAdapter{

    private final Context context;
    private final List<Item> allItems;
    private List<Item> visibleItems;
    public String url;
    public int index;
    public ItemCardBinding itemCardBinding;
    public ItemTouchHelper mItemTouchHelper;
    public int mode;
    public List<ItemViewHolder> holderList = new ArrayList<>();



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

        holderList.add(holder);
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

        ((GalleryActivity)context).findViewById(R.id.emptySearchResult).setVisibility(View.GONE);

        //No query, show all items
        if (query.trim().isEmpty()){
            visibleItems = allItems;
            ((GalleryActivity)context).findViewById(R.id.noItemsTV).setVisibility(View.GONE);
            notifyDataSetChanged();
            return;
        }

        //filter & add to visibleCourses
        query = query.toLowerCase();
        List<Item> temp = new ArrayList<>();

        for (Item item : allItems) {
            if (item.label.toLowerCase().contains(query))
                temp.add(item);
        }

        if(temp.size() == 0){
            ((GalleryActivity)context).findViewById(R.id.emptySearchResult).setVisibility(View.VISIBLE);
            ((GalleryActivity)context).findViewById(R.id.noItemsTV).setVisibility(View.GONE);
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
     * Utility class
     * to add swipe to dismiss and drag & drop support to RecyclerView.
     * It works with a RecyclerView and a Callback class,
     * which configures what type of interactions are enabled and
     * also receives events when user performs these actions.
     * @param itemTouchHelper helper for touch actions
     */
    public void setListItemAdapterHelper(ItemTouchHelper itemTouchHelper){
        mItemTouchHelper = itemTouchHelper;
    }

    /**
     * To Move Items For Drag-Drop Functionality
     * @param fromPosition initial position
     * @param toPosition final position
     */
    @Override
    public void onItemMove(int fromPosition, int toPosition) {

        Item fromItem=allItems.get(fromPosition);
        allItems.remove(fromItem);
        allItems.add(toPosition,fromItem);
        visibleItems = allItems;
        notifyItemMoved(fromPosition, toPosition);
    }

    /**
     * To swipe delete item
     * @param position of item
     */
    @Override
    public void onItemDelete(int position){
    }





    /**
     * ViewHolder
     * Represents view holder for the recycler view
     */
    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, View.OnTouchListener, GestureDetector.OnGestureListener{

        //declare view binding object
        ItemCardBinding b;
        GestureDetector gestureDetector;

        /**
         * To give binding to the holder
         * @param b binding of the view
         */
        public ItemViewHolder(ItemCardBinding b) {
            super(b.getRoot());
            this.b = b;
            gestureDetector = new GestureDetector(b.getRoot().getContext(), this);
            eventListenerHandler();
        }


        /**
         * To handle events of drag & drop FAB
         */
        public void eventListenerHandler() {
            if(mode == 0){
                b.imageView.setOnTouchListener(null);
                b.title.setOnTouchListener(null);
                b.title.setOnCreateContextMenuListener(this);
                b.imageView.setOnCreateContextMenuListener(this);
            }
            else if(mode == 1){
                b.title.setOnCreateContextMenuListener(null);
                b.imageView.setOnCreateContextMenuListener(null);
                b.title.setOnTouchListener(this);
                b.imageView.setOnTouchListener(this);
            }
        }

        /**
         * Context Menu For Edit, Delete, Share Options
         * @param menu To show a context menu on long click
         * @param v basic building block for user interface components
         * @param menuInfo Additional information regarding the creation of the context menu
         */
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAbsoluteAdapterPosition(), R.id.editCard,0,"Edit");
            menu.add(this.getAbsoluteAdapterPosition(), R.id.deleteCard,0,"Delete");
            menu.add(this.getAbsoluteAdapterPosition(),R.id.shareCard,0,"Share");
            url = allItems.get(this.getAbsoluteAdapterPosition()).url;
            index = this.getAbsoluteAdapterPosition();
            itemCardBinding = b;
        }

        /**
         * Called when a touch event is dispatched to a view. This allows listeners to
         * get a chance to respond before the target view.
         *
         * @param v     The view the touch event has been dispatched to.
         * @param event The MotionEvent object containing full information about
         *              the event.
         * @return True if the listener has consumed the event, false otherwise.
         */
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }

        /**
         * Long Press for Drag & Drop action
         * @param e : long press motion event
         */
        @Override
        public void onLongPress(MotionEvent e) {
            if(mode == 1)
                mItemTouchHelper.startDrag(this);
        }


        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }
}
