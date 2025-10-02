package com.coheser.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.coheser.app.R;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.models.UserModel;
import com.coheser.app.simpleclasses.Functions;

import java.util.ArrayList;

public class UserShareProfileAdapter extends RecyclerView.Adapter<UserShareProfileAdapter.CustomViewHolder> {


    public AdapterClickListener listener;
    ArrayList<UserModel> datalist;

    public UserShareProfileAdapter(ArrayList<UserModel> datalist, AdapterClickListener listener) {
        this.datalist = datalist;
        this.listener = listener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_user_share_profile_list_view, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {

        UserModel item = datalist.get(i);

        holder.userName.setText(item.username);
        holder.fullName.setText(item.first_name + " " + item.last_name);

        holder.userImage.setController(Functions.frescoImageLoad(item.getProfilePic(), R.drawable.ic_user_icon, holder.userImage, false));
        if (item.isSelected) {
            holder.ivSelection.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_selection));
        } else {
            holder.ivSelection.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_unselection));
        }


        holder.bind(i, datalist.get(i), listener);

    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView userImage;
        TextView userName;
        TextView fullName;
        ImageView ivSelection;
        RelativeLayout mainlayout;


        public CustomViewHolder(View view) {
            super(view);
            ivSelection = view.findViewById(R.id.ivSelection);
            userImage = view.findViewById(R.id.user_image);
            userName = view.findViewById(R.id.userName);
            fullName = view.findViewById(R.id.fullName);
            mainlayout = view.findViewById(R.id.mainlayout);
        }

        public void bind(final int pos, final UserModel item, final AdapterClickListener listener) {


            mainlayout.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });

        }


    }


}