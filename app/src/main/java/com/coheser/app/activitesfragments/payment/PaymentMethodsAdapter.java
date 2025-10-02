package com.coheser.app.activitesfragments.payment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coheser.app.R;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.models.Card;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;

import java.util.ArrayList;

public class PaymentMethodsAdapter extends RecyclerView.Adapter<PaymentMethodsAdapter.ViewHolder> {

    Context context;
    ArrayList<Card> cardArrayList = new ArrayList<>();
    AdapterClickListener adapterClickListener;
    boolean isEdit = false;

    public PaymentMethodsAdapter(Context context, ArrayList<Card> cardArrayList, AdapterClickListener adapterClickListener) {
        this.context = context;
        this.cardArrayList = cardArrayList;
        this.adapterClickListener = adapterClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_paymentmethods_list, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Card item = cardArrayList.get(position);

        if (isEdit) {
            holder.rledit.setVisibility(View.VISIBLE);
        } else {
            holder.rledit.setVisibility(View.GONE);
        }


        holder.tvMasterCard.setText(item.getBrand() + " (" + item.getCard() + ")");
        if (item.getBrand().equalsIgnoreCase("visa")) {
            holder.cardImage.setImageResource(R.drawable.ic_visa_card);
        } else if (item.getBrand().equalsIgnoreCase("mastercard")) {
            holder.cardImage.setImageResource(R.drawable.ic_mastercard);
        } else {
            holder.cardImage.setImageResource(R.drawable.ic_card_any);
        }
        holder.tvUserName.setText(Functions.getSharedPreference(context).getString(Variables.U_NAME,""));


        holder.tvDate.setText(item.getExpMonth()+"/"+item.getExpYear());
        holder.bind(position, item, adapterClickListener);
    }

    public void enableEdit(boolean b) {
        isEdit = b;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return cardArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        TextView tvMasterCard, tvUserName, tvDate;
        RelativeLayout rledit;
        ImageView cardImage;
        LinearLayout mainLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMasterCard = itemView.findViewById(R.id.tvMasterCard);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            rledit = itemView.findViewById(R.id.rledit);
            tvDate = itemView.findViewById(R.id.tvDate);
            mainLayout = itemView.findViewById(R.id.mainLayout);
            cardImage = itemView.findViewById(R.id.cardImage);

        }

        public void bind(final int item, final Card model,
                         final AdapterClickListener listener) {

            rledit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // This is OnClick of any list Item
                    listener.onItemClick(v, item, model);
                }

            });

            mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // This is OnClick of any list Item
                    listener.onItemClick(v, item, model);
                }

            });

        }
    }
}
