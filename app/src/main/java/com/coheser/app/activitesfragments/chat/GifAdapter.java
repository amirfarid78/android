package com.coheser.app.activitesfragments.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/20/2018.
 */

public class GifAdapter extends RecyclerView.Adapter<GifAdapter.CustomViewHolder> {
    public Context context;
    ArrayList<String> gifList;
    private final OnItemClickListener listener;

    public GifAdapter(Context context, ArrayList<String> datalist, OnItemClickListener listener) {
        this.context = context;
        this.gifList = datalist;
        this.listener = listener;

    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_gif_layout, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return gifList.size();
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {
        holder.bind(gifList.get(i), listener);

        String url = Variables.GIF_FIRSTPART + gifList.get(i) + Variables.GIF_SECONDPART2;
        Functions.printLog(Constants.tag, "gif" + url);
        holder.gifImage.setController(Functions.frescoGifLoad(url, R.drawable.ractengle_solid_lightblack, holder.gifImage));
    }


    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView gifImage;

        public CustomViewHolder(View view) {
            super(view);
            gifImage = view.findViewById(R.id.gif_image);
        }

        public void bind(final String item, final OnItemClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(item);

            });


        }

    }


}