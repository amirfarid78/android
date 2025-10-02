package com.coheser.app.activitesfragments.sendgift

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.walletandwithdraw.WalletModel
import com.coheser.app.interfaces.AdapterClickListener

class CoinRechargeAdapter(
    var context: Context,
    wallet_modelArrayList: ArrayList<WalletModel>,
    adapter_click_listener: AdapterClickListener
) : RecyclerView.Adapter<CoinRechargeAdapter.ViewHolder>() {
    var wallet_modelArrayList: ArrayList<WalletModel> = ArrayList()
    var adapter_click_listener: AdapterClickListener

    init {
        this.wallet_modelArrayList = wallet_modelArrayList
        this.adapter_click_listener = adapter_click_listener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_recharge_coins_list, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = wallet_modelArrayList[position]
        holder.coins.text = item.coins
        holder.price.text = item.price

        if (item.image != "") {
            val uri = Uri.parse(item.image)
            holder.image.setImageURI(uri)
        }

        holder.bind(position, item, adapter_click_listener)
    }

    override fun getItemCount(): Int {
        return wallet_modelArrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.iv_profile)
        var coins: TextView = itemView.findViewById(R.id.tv_coins)
        var price: TextView = itemView.findViewById(R.id.tv_price)

        fun bind(postion: Int, item: WalletModel?, listener: AdapterClickListener) {
            itemView.setOnClickListener { v -> listener.onItemClick(v, postion, item) }
        }
    }
}
