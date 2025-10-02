package com.coheser.app.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.coheser.app.R;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.models.HomeModel;
import com.coheser.app.simpleclasses.Functions;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/20/2018.
 */

public class VideosListAdapter extends RecyclerView.Adapter<VideosListAdapter.CustomViewHolder> {
    public Context context;

    ArrayList<HomeModel> datalist;
    AdapterClickListener adapterClickListener;

    public VideosListAdapter(Context context, ArrayList<HomeModel> arrayList, AdapterClickListener adapterClickListener) {
        this.context = context;
        datalist = arrayList;
        this.adapterClickListener = adapterClickListener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_search_video_layout, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        final HomeModel item = datalist.get(i);

        holder.usernameTxt.setText(item.getUserModel().username);
        holder.descriptionTxt.setText(item.getVideoDescription());

        Uri uriThum = Uri.parse(item.getThum());
        holder.image.setImageURI(uriThum);

        Uri uriProfile = Uri.parse(item.getUserModel().getProfilePic());
        holder.userImage.setImageURI(uriProfile);

        holder.firstLastNameTxt.setText(item.getUserModel().first_name + " " + item.getUserModel().last_name);
        holder.likesCountTxt.setText(Functions.getSuffix(item.like_count));
        holder.viewText.setText(Functions.getSuffix(item.views));

        holder.bind(i, item, adapterClickListener);

    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageView image, userImage;

        TextView usernameTxt, descriptionTxt, firstLastNameTxt, likesCountTxt, viewText;


        public CustomViewHolder(View view) {
            super(view);
            userImage = view.findViewById(R.id.user_image);
            image = view.findViewById(R.id.image);
            usernameTxt = view.findViewById(R.id.username_txt);
            descriptionTxt = view.findViewById(R.id.description_txt);
            viewText = view.findViewById(R.id.view_txt);

            firstLastNameTxt = view.findViewById(R.id.first_last_name_txt);
            likesCountTxt = view.findViewById(R.id.likes_count_txt);
        }

        public void bind(final int pos, final HomeModel item, final AdapterClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);

            });


        }


    }
    public void updateData(ArrayList<HomeModel> newData) {
        datalist = newData;
        notifyDataSetChanged();
    }


}