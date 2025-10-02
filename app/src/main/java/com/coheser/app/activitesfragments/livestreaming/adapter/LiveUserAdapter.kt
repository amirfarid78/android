package com.coheser.app.activitesfragments.livestreaming.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.facebook.drawee.view.SimpleDraweeView

class LiveUserAdapter(
    var context: Context,
    var dataList: ArrayList<LiveUserModel>,
    var adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<LiveUserAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_live_layout, viewGroup, false)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = dataList[i]

        if (TextUtils.isEmpty(item.getJoinStreamPrice()) || item.getJoinStreamPrice() == "0") {
            holder.ivLocker.visibility = View.GONE
        } else {
            holder.ivLocker.visibility = View.VISIBLE
        }
        holder.ivProfile.controller = frescoImageLoad(
            item.getUserPicture(),
            holder.ivProfile,
            false
        )

        if (item.getIsVerified() == 1) {
            holder.ivVerified.visibility = View.VISIBLE
        } else {
            holder.ivVerified.visibility = View.GONE
        }
        holder.tvName.text = item.getUserName()
        if (item.getOnlineType() == "multicast") {
            if (item.isDualStreaming) {
                holder.tvLive.text = holder.itemView.context.getString(R.string.public_live)
            } else {
                holder.tvLive.text = holder.itemView.context.getString(R.string.live)
            }
        } else {
            holder.tvLive.text = holder.itemView.context.getString(R.string.private_live)
        }


        holder.bind(i, item, adapterClickListener)
    }


    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivProfile: SimpleDraweeView = itemView.findViewById(R.id.ivProfile)
        var tvName: TextView = itemView.findViewById(R.id.tvName)
        var tvLive: TextView = itemView.findViewById(R.id.tvLive)
        var ivLocker: ImageView = itemView.findViewById(R.id.ivLocker)
        var ivVerified: ImageView = itemView.findViewById(R.id.ivVerified)

        fun bind(position: Int, item: Any?, listener: AdapterClickListener) {
            itemView.setOnClickListener { v: View? ->
                listener.onItemClick(v, position, item)
            }
        }
    }
}