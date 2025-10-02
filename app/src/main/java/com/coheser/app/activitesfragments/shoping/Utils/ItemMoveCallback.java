package com.coheser.app.activitesfragments.shoping.Utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.coheser.app.Constants;
import com.coheser.app.simpleclasses.Functions;

public class ItemMoveCallback extends ItemTouchHelper.Callback {

    public interface ItemTouchHelperContract {

        void onRowMoved(int fromPosition, int toPosition);
        void onRowSelected(PhotoViewHolder myViewHolder);
        void onRowClear(PhotoViewHolder myViewHolder);


    }



    private final ItemTouchHelperContract itemTouchListener;

    public ItemMoveCallback(ItemTouchHelperContract itemTouchListener) {
        this.itemTouchListener = itemTouchListener;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }



    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        Functions.printLog(Constants.tag,"onSwiped");
    }


    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, 0);
    }


    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        itemTouchListener.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void clearView(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        Functions.printLog(Constants.tag,"clearView");
        if (viewHolder instanceof PhotoViewHolder) {
            PhotoViewHolder myViewHolder=
                    (PhotoViewHolder) viewHolder;
            itemTouchListener.onRowClear(myViewHolder);
        }
    }


}
