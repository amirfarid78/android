package com.coheser.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.coheser.app.R;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.models.HomeModel;
import com.coheser.app.models.HomeSelectionModel;
import com.coheser.app.simpleclasses.Functions;

import java.util.ArrayList;

public class VideoPlaylistReorderAdapter extends RecyclerView.Adapter<VideoPlaylistReorderAdapter.CustomViewHolder> {

    public Context context;
    AdapterClickListener adapterClickListener;
    private final ArrayList<HomeSelectionModel> dataList;

    public VideoPlaylistReorderAdapter(Context context, ArrayList<HomeSelectionModel> dataList, AdapterClickListener adapterClickListener) {
        this.context = context;
        this.dataList = dataList;
        this.adapterClickListener = adapterClickListener;

    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_video_playlist_reorder_layout, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {
        final HomeSelectionModel itemSelected = dataList.get(i);
        HomeModel item = itemSelected.getModel();
        holder.setIsRecyclable(false);


        holder.thumbImage.setController(Functions.frescoImageLoad(item.getThum(), holder.thumbImage, false));
        holder.tvDescription.setText(item.getVideoDescription());
        holder.tvView.setText(Functions.getSuffix(item.views) + " " + holder.itemView.getContext().getString(R.string.views));


        holder.bind(i, item, adapterClickListener);

    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView thumbImage;
        TextView tvDescription, tvView;

        public CustomViewHolder(View view) {
            super(view);
            tvView = view.findViewById(R.id.tvView);
            thumbImage = view.findViewById(R.id.thumb_image);
            tvDescription = view.findViewById(R.id.tvDescription);

        }

        public void bind(final int position, final HomeModel item, final AdapterClickListener listener) {
            itemView.setOnClickListener(v -> {
                adapterClickListener.onItemClick(v, position, item);

            });

        }

    }

}
