package com.coheser.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.databinding.ItemFollowersShareLayoutBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad

/**
 * Created by qboxus on 3/20/2018.
 */
class FollowingShareAdapter(
    var context: Context,
    var datalist: ArrayList<UserModel>,
    var adapter_clickListener: AdapterClickListener
) : RecyclerView.Adapter<FollowingShareAdapter.CustomViewHolder?>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val binding  = ItemFollowersShareLayoutBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        holder.setIsRecyclable(false)
        val item = datalist[i]
        holder.binding.userName.text = item.username
        holder.binding.userImage.controller = frescoImageLoad(
            item.getProfilePic(),
            R.drawable.ic_user_icon,
            holder.binding.userImage,
            false
        )
        if (item.isSelected) {
            holder.binding.tickIcon.visibility = View.VISIBLE
            holder.binding.userImage.alpha = 0.5.toFloat()
        } else {
            holder.binding.tickIcon.visibility = View.GONE
            holder.binding.userImage.alpha = 1.0.toFloat()
        }
        holder.bind(i, datalist[i], adapter_clickListener)
    }

    inner class CustomViewHolder(val binding: ItemFollowersShareLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pos: Int, item: UserModel?, listener: AdapterClickListener) {
            itemView.setOnClickListener { v: View? -> listener.onItemClick(v, pos, item) }
        }
    }
}