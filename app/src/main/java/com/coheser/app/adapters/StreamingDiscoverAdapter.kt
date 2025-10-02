package com.coheser.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.databinding.ItemStreamingDiscoverLayoutBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad

class StreamingDiscoverAdapter(
    var context: Context,
    var dataList: ArrayList<LiveUserModel>,
    var adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<StreamingDiscoverAdapter.CustomViewHolder>() {
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
            item.getUserPicture(),
            holder.binding.profileblurImage,
            false
        )
        holder.binding.ivProfile.controller = frescoImageLoad(
            item.getUserPicture(),
            holder.binding.ivProfile,
            false
        )
        holder.binding.lottieGif.playAnimation()
        holder.binding.tvName.text = item.getUserName()

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