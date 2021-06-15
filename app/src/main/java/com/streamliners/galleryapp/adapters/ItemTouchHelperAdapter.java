package com.streamliners.galleryapp.adapters;

public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);
    void onItemDelete(int position);
}
