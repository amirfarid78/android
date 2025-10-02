package com.coheser.app.activitesfragments.shoping.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coheser.app.R;
import com.coheser.app.activitesfragments.shoping.models.ProductAttribute;
import com.coheser.app.activitesfragments.shoping.models.ProductAttributeVariation;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class ProductVariationAdapter extends RecyclerView.Adapter<ProductVariationAdapter.CustomViewHolder >{
    public Context context;

    ArrayList<ProductAttribute> datalist;
    ArrayList<ProductAttributeVariation> selectedList;

    public interface OnItemClickListener {
        void onItemClick(int parent_pos,int child_pos);
    }

    public OnItemClickListener listener;

    public ProductVariationAdapter(Context context, ArrayList<ProductAttributeVariation> selected_list, ArrayList<ProductAttribute> arrayList, OnItemClickListener listener) {
        this.context = context;
        this.selectedList =selected_list;
        datalist = arrayList;
        this.listener = listener;
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_variation_layout, viewGroup, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        RecyclerView horizontalReycerview;
        TextView title;

        public CustomViewHolder(View view) {
            super(view);
            horizontalReycerview = view.findViewById(R.id.horizontal_recylerview);
            title = view.findViewById(R.id.title);
        }


    }


    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {

        ProductAttribute item = datalist.get(i);

        holder.title.setText(item.getName());
        HorizontalAdapter adapter = new HorizontalAdapter(context, selectedList, i,item);
        holder.horizontalReycerview.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.horizontalReycerview.setAdapter(adapter);


    }



    class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.CustomViewHolder> {
        public Context context;

        ArrayList<ProductAttributeVariation> datalist;
        int parentPos;
        ProductAttribute product_variations_model;
        ArrayList<ProductAttributeVariation> selectedList;
        public HorizontalAdapter(Context context, ArrayList<ProductAttributeVariation> selected_list, int parent_pos, ProductAttribute item) {
            this.context = context;
            datalist = item.getProductAttributeVariation();
            this.selectedList =selected_list;
            product_variations_model=item;
            this.parentPos =parent_pos;
        }

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_variation_item_layout, viewGroup, false);
            CustomViewHolder viewHolder = new CustomViewHolder(view);
            return viewHolder;
        }

        @Override
        public int getItemCount() {
            return datalist.size();
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {

            TextView variationNameTxt;


            public CustomViewHolder(View view) {
                super(view);
                variationNameTxt = view.findViewById(R.id.variation_name_txt);

            }

            public void bind(final int pos, final ArrayList<ProductAttributeVariation> datalist) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(parentPos,pos);
                    }
                });
            }


        }

        @Override
        public void onBindViewHolder(final CustomViewHolder holder, final int i) {
            holder.setIsRecyclable(false);

            try {

                holder.variationNameTxt.setText(datalist.get(i).getValue());

                if(selectedList.contains(datalist.get(i)))
                {
                    holder.variationNameTxt.setBackground(ContextCompat.getDrawable(context, R.drawable.border_black_not_rounded));
                    holder.variationNameTxt.setTextColor(ContextCompat.getColor(context, R.color.black));
                }
                else {
                    holder.variationNameTxt.setBackground(ContextCompat.getDrawable(context, R.drawable.d_gray_border));
                    holder.variationNameTxt.setTextColor(ContextCompat.getColor(context, R.color.black));
                }


                holder.bind(i, datalist);

            }catch (Exception e){

            }
        }

    }


}