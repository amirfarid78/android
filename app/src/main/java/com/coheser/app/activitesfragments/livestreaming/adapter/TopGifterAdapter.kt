package com.coheser.app.activitesfragments.livestreaming.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.model.GiftUsers
import com.coheser.app.databinding.ItemTopGifterBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad

class TopGifterAdapter(
    var context: Context,
    user_dataList: List<GiftUsers>,
    listener: AdapterClickListener
) : RecyclerView.Adapter<TopGifterAdapter.CustomViewHolder>() {
    var user_dataList: List<GiftUsers> = ArrayList()
    var adapterClickListener: AdapterClickListener
    init {
        this.user_dataList = user_dataList
        this.adapterClickListener = listener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val binding = DataBindingUtil.inflate<ItemTopGifterBinding>(
            LayoutInflater
                .from(viewGroup.context), R.layout.item_top_gifter, viewGroup, false
        )
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return user_dataList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {

        val item = user_dataList[i]
        if (!TextUtils.isEmpty(item.userPicture)) {
            holder.binding.ivMainProfile.controller = frescoImageLoad(item.userPicture,
                holder.binding.ivMainProfile, false)
        }

    }

    inner class CustomViewHolder(var binding: ItemTopGifterBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
    }

}