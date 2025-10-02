package com.coheser.app.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.sendgift.GiftHistoryModel
import com.coheser.app.databinding.ItemVideoGiftBinding
import com.coheser.app.simpleclasses.Functions.frescoImageLoad

/**
 * Created by yurr on 3/20/2018.
 */
class VideoGiftsAdapter(
    val datalist: ArrayList<GiftHistoryModel>
) : RecyclerView.Adapter<VideoGiftsAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val binding:ItemVideoGiftBinding =DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context),
            R.layout.item_video_gift, viewGroup, false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = datalist.get(position)

        if (item.gift.icon!!.isEmpty()){
            holder.binding.userImage.controller = frescoImageLoad(
                "${item.gift.image}", R.drawable.ic_user_icon, holder.binding.userImage, false)
            Log.d(Constants.tag,"image gift :${item.gift.image}")
        }else{
            holder.binding.userImage.controller = frescoImageLoad(
                "${item.gift.icon}", R.drawable.ic_user_icon, holder.binding.userImage, false)
            Log.d(Constants.tag,"icon gift :${item.gift.icon}")
        }


    }

    inner class CustomViewHolder(val binding: ItemVideoGiftBinding) : RecyclerView.ViewHolder(
        binding.root) {

    }


}