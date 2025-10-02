package com.coheser.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.coheser.app.R
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad

class HomeSuggestionAdapter(
    var datalist: ArrayList<UserModel>,
    var listener: AdapterClickListener
) : RecyclerView.Adapter<HomeSuggestionAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_home_suggestion_follower, viewGroup, false)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var userImage: SimpleDraweeView = view.findViewById(R.id.user_image)
        var userGif: SimpleDraweeView = view.findViewById(R.id.userGif)
        var tvName: TextView = view.findViewById(R.id.tvName)
        var tvUserName: TextView = view.findViewById(R.id.tvUserName)
        var tvFollowBtn: TextView = view.findViewById(R.id.tvFollowBtn)
        var ivCross: ImageView = view.findViewById(R.id.ivCross)

        fun bind(pos: Int, item: UserModel?, listener: AdapterClickListener) {
            tvFollowBtn.setOnClickListener { v: View? ->
                listener.onItemClick(v, pos, item)
            }

            userImage.setOnClickListener { v: View? ->
                listener.onItemClick(v, pos, item)
            }

            ivCross.setOnClickListener { v: View? ->
                listener.onItemClick(v, pos, item)
            }
        }
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        holder.setIsRecyclable(false)

        val item = datalist[i]

        holder.tvUserName.text = item.username
        holder.tvName.text = item.first_name + " " + item.last_name

        holder.userImage.controller = frescoImageLoad(
            item.getProfilePic(),
            R.drawable.ic_user_icon,
            holder.userImage,
            false
        )


        if (!item.getProfileGif()!!.isEmpty()) {
            holder.userGif.controller = frescoImageLoad(
                item.getProfileGif(),
                holder.userGif,
                true
            )
        }


        holder.bind(i, datalist[i], listener)
    }
}