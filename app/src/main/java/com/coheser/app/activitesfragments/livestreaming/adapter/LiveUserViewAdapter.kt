package com.coheser.app.activitesfragments.livestreaming.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.StreamJoinModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.facebook.drawee.view.SimpleDraweeView

class LiveUserViewAdapter(
    var context: Context,
    var dataList: ArrayList<StreamJoinModel>,
    var adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<LiveUserViewAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.item_live_view_layout, null)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = dataList[i]


        holder.tvName.text = item.userName
        holder.ivProfile.controller =
            frescoImageLoad(item.userPic, holder.ivProfile, false)
        holder.bind(i, item, adapterClickListener)
    }


    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivProfile: SimpleDraweeView = itemView.findViewById(R.id.ivProfile)
        var tvName: TextView = itemView.findViewById(R.id.tvName)

        fun bind(position: Int, item: Any?, listener: AdapterClickListener) {
            itemView.setOnClickListener { v: View? ->
                listener.onItemClick(v, position, item)
            }
        }
    }
}