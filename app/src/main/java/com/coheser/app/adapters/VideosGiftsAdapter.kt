package com.coheser.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.sendgift.GiftHistoryModel
import com.coheser.app.databinding.ItemVideoGiftListBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad

/**
 * Created by yurr on 3/20/2018.
 */
class VideosGiftsAdapter(
    val context: Context,
    val datalist: ArrayList<GiftHistoryModel>,
    val listener: AdapterClickListener
) : RecyclerView.Adapter<VideosGiftsAdapter.CustomViewHolder>() {


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val binding:ItemVideoGiftListBinding =DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context),
            R.layout.item_video_gift_list, viewGroup, false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = datalist.get(position)

        holder.binding.username.text = "@${item.user.username}"

        if(item.count>1) {
            holder.binding.message.text = context.getString(R.string.sent_a_count_gif, ""+item?.count)
        }
        else{
            holder.binding.message.text = context.getString(R.string.sent_a_gift) 
        }

        holder.binding.userImage.controller = frescoImageLoad(
            item.user.getProfilePic(), R.drawable.ic_user_icon, holder.binding.userImage, false)


        holder.binding.giftImage.controller = frescoImageLoad(
            "${item.gift.icon}", R.drawable.image_placeholder, holder.binding.giftImage, false)


        holder.bind(position, listener)
    }

    inner class CustomViewHolder(val binding:ItemVideoGiftListBinding) : RecyclerView.ViewHolder(
        binding.root) {
        fun bind(position: Int,listener: AdapterClickListener) {
        }
    }

}