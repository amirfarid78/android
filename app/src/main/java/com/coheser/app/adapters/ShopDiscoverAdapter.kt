package com.coheser.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.databinding.ItemStreamingDiscoverLayoutBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad

class ShopDiscoverAdapter(
    var context: Context,
    var dataList: ArrayList<UserModel>,
    var adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<ShopDiscoverAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {

        val binding= ItemStreamingDiscoverLayoutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = dataList[i]

        holder.binding.profileblurImage.controller = frescoImageLoad(
            item.getProfilePic(),
            holder.binding.profileblurImage,
            false
        )
        holder.binding.ivProfile.controller = frescoImageLoad(
            item.getProfilePic(),
            holder.binding.ivProfile,
            false
        )
        holder.binding.lottieGif.visibility=View.GONE
        holder.binding.tvName.text = item.username

        holder.bind(i, item, adapterClickListener)
    }

    inner class CustomViewHolder(val binding: ItemStreamingDiscoverLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int, item: Any?, listener: AdapterClickListener) {
            itemView.setOnClickListener { v: View? ->
                listener.onItemClick(v, position, item)
            }
        }
    }
}