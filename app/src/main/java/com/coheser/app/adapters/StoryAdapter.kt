package com.coheser.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.databinding.ItemHomeStoryLayoutBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.StoryModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.MyViewHolder

class StoryAdapter(
    var dataList: ArrayList<StoryModel>,
    var adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<StoryAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val binding= ItemHomeStoryLayoutBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val itemModel = dataList[i]

        holder.binding.tvUserPic.text = itemModel.username
        holder.binding.ivUserPic.controller = frescoImageLoad(
            itemModel.getProfilePic(),
            R.drawable.ic_user_icon,
            holder.binding.ivUserPic,
            false
        )
        holder.binding.circleStatusBar.counts=itemModel.videoList.size

        holder.onBind(i, itemModel, adapterClickListener)
    }


    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class CustomViewHolder(val binding: ItemHomeStoryLayoutBinding) : MyViewHolder(binding.root) {

        override fun onBind(pos: Int, item: Any?, listener: AdapterClickListener) {
            binding.tabUserPic.setOnClickListener { v: View? ->
                adapterClickListener.onItemClick(v, pos, item)
            }
        }
    }
}