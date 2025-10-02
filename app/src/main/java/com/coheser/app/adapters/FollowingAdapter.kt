package com.coheser.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.databinding.ItemFollowingBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions.capitalizeEachWord
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.MyViewHolder

/**
 * Created by qboxus on 3/20/2018.
 */
class FollowingAdapter(
    var context: Context,
    var datalist: MutableList<UserModel>,
    var listener: AdapterClickListener
) : RecyclerView.Adapter<FollowingAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val binding: ItemFollowingBinding = DataBindingUtil.inflate(
            LayoutInflater.from(viewGroup.context),
            R.layout.item_following,
            viewGroup,
            false
        )

        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    inner class CustomViewHolder(val binding: ItemFollowingBinding) : MyViewHolder(binding.root) {

        override fun onBind(pos: Int, item: Any?, listener: AdapterClickListener) {
            binding.mainlayout.setOnClickListener { v: View? -> listener.onItemClick(v, pos, item) }
            binding.actionTxt.setOnClickListener { v: View? -> listener.onItemClick(v, pos, item) }

        }
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        holder.setIsRecyclable(false)
        val item = datalist[i]

            holder.binding.userName.text = item.username
            holder.binding.userinfoTxt.text = item.first_name + " " + item.last_name


        holder.binding.userImage.controller = frescoImageLoad(
            item.getProfilePic(),
            R.drawable.ic_user_icon,
            holder.binding.userImage,
            false
        )


       holder.binding.actionTxt.text = capitalizeEachWord(item.button!!)
        if (item.button != null && (item.button.equals(
                "follow",
                ignoreCase = true
            ) || item.button.equals("follow back", ignoreCase = true))
        ) {
            holder.binding.actionTxt.background =
                ContextCompat.getDrawable(context, R.drawable.d_round_colord_6)
            holder.binding.actionTxt.setTextColor(ContextCompat.getColor(context, R.color.whiteColor))
        } else if (item.button != null &&
            (item.button.equals("following", ignoreCase = true) || item.button.equals(
                "friends",
                ignoreCase = true
            ))
        ) {
            holder.binding.actionTxt.background =
                ContextCompat.getDrawable(context, R.drawable.d_gray_border)
            holder.binding.actionTxt.setTextColor(ContextCompat.getColor(context, R.color.black))
        } else if (item.button != null && item.button.equals("0", ignoreCase = true)) {
            holder.binding.actionTxt.visibility = View.GONE
        }
        holder.onBind(i, datalist[i], listener)
    }
}