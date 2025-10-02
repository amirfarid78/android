package com.coheser.app.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.models.UserModel;
import com.coheser.app.simpleclasses.Functions;

import java.util.ArrayList;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.CustomViewHolder> {


    public OnItemClickListener listener;
    ArrayList<UserModel> datalist;

    public SuggestionAdapter(ArrayList<UserModel> arrayList, OnItemClickListener listener) {
        datalist = arrayList;
        this.listener = listener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_suggestion_follower, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        UserModel item = datalist.get(i);

        holder.tvName.setText(item.first_name + " " + item.last_name);

        holder.userImage.setController(Functions.frescoImageLoad(item.getProfilePic(), R.drawable.ic_user_icon, holder.userImage, false));

        holder.bind(i, datalist.get(i), listener);
        Log.d(Constants.tag,"data :"+ item.first_name);

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int postion, UserModel item);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView userImage;
        TextView tvName, tvFollowBtn;
        ImageView ivCross;

        public CustomViewHolder(View view) {
            super(view);
            ivCross = view.findViewById(R.id.ivCross);
            userImage = view.findViewById(R.id.user_image);
            tvName = view.findViewById(R.id.tvName);
            tvFollowBtn = view.findViewById(R.id.tvFollowBtn);
        }

        public void bind(final int pos, final UserModel item, final OnItemClickListener listener) {


            tvFollowBtn.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);

            });

            userImage.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);

            });

            ivCross.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);

            });

        }


    }

}