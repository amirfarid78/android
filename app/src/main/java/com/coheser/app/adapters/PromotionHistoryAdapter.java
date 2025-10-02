package com.coheser.app.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.databinding.PromotionHistoryItemViewBinding;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.models.PromotionHistoryModel;
import com.coheser.app.simpleclasses.DateOprations;
import com.coheser.app.simpleclasses.Functions;

import java.util.ArrayList;

public class PromotionHistoryAdapter extends RecyclerView.Adapter<PromotionHistoryAdapter.ViewHolder> {

    ArrayList<PromotionHistoryModel> datalist;
    AdapterClickListener adapterClickListener;

    public PromotionHistoryAdapter(ArrayList<PromotionHistoryModel> arrayList, AdapterClickListener adapterClickListener) {
        datalist = arrayList;
        this.adapterClickListener = adapterClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        PromotionHistoryItemViewBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.promotion_history_item_view, viewGroup, false);
        return new ViewHolder(binding);
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int i) {
        PromotionHistoryModel item = datalist.get(i);
        holder.binding.ivVideo.setController(Functions.frescoImageLoad(item.getVideo_thumb(), R.drawable.image_placeholder, holder.binding.ivVideo, false));

        holder.binding.tvDuration.setText(DateOprations.getDurationInDays("yyyy-MM-dd HH:mm:ss", item.getStart_datetime(), item.getEnd_datetime()) + " " + holder.binding.getRoot().getContext().getString(R.string.day));
        holder.binding.tvCoins.setText(Functions.getSuffix(item.getCoin()));
        holder.binding.tvCoinsSpent.setText(Functions.getSuffix(item.getCoins_consumed()));
        holder.binding.tvLinkClicks.setText(Functions.getSuffix(item.getDestination_tap()));
        holder.binding.tvVideoViews.setText(Functions.getSuffix(item.getVideo_views()));
//        if (checkIsPromotionCompleted(DateOprations.getDurationInPoints("yyyy-MM-dd HH:mm:ss", DateOprations.getCurrentDate("yyyy-MM-dd HH:mm:ss"), item.getEnd_datetime()))) {
//            holder.binding.tabStatus.setVisibility(View.VISIBLE);
//            holder.binding.btnCancle.setVisibility(View.GONE);
//        } else {
//            holder.binding.tabStatus.setVisibility(View.GONE);
//            holder.binding.btnCancle.setVisibility(View.VISIBLE);
//        }
        if (item.getStatus().equalsIgnoreCase("active")){
            holder.binding.tabStatus.setVisibility(View.GONE);
            holder.binding.btnCancle.setVisibility(View.VISIBLE);
            holder.binding.btnCancle.setText(holder.binding.getRoot().getContext().getString(R.string.stop));
        } else if (item.getStatus().equalsIgnoreCase("stopped")) {
            holder.binding.tabStatus.setVisibility(View.GONE);
            holder.binding.btnCancle.setVisibility(View.VISIBLE);
            holder.binding.btnCancle.setText(item.getStatus());
        } else{
            holder.binding.tabStatus.setVisibility(View.VISIBLE);
            holder.binding.btnCancle.setVisibility(View.GONE);
        }

        holder.bind(i, item, adapterClickListener);

    }

    private boolean checkIsPromotionCompleted(String durationInDays) {
        try {
            Log.d(Constants.tag, "durationInDays: " + durationInDays);
            double number = Double.parseDouble(durationInDays);
            return number <= 0;
        } catch (Exception e) {
            return false;
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        PromotionHistoryItemViewBinding binding;

        public ViewHolder(PromotionHistoryItemViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final int pos, final Object item, final AdapterClickListener listener) {


            binding.btnPromoteAgain.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });
            binding.btnCancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,pos,item);
                }
            });
            binding.mainLay.setOnClickListener(v -> {
                listener.onItemClick(v,pos,item);
            });
        }


    }


}
