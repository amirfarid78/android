package com.coheser.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getSuffix
import com.facebook.drawee.view.SimpleDraweeView

/**
 * Created by qboxus on 3/19/2019.
 */
class UsersAdapter : RecyclerView.Adapter<UsersAdapter.CustomViewHolder> {

    var context: Context
    var datalist: MutableList<UserModel>
    var adapterClickListener: AdapterClickListener
    var from: String? = ""

    constructor(
        context: Context,
        datalist: MutableList<UserModel>,
        from: String?,
        adapterClickListener: AdapterClickListener
    ) {
        this.context = context
        this.datalist = datalist
        this.from = from
        this.adapterClickListener = adapterClickListener
    }

    constructor(
        context: Context,
        arrayList: MutableList<UserModel>,
        adapterClickListener: AdapterClickListener
    ) {
        this.context = context
        datalist = arrayList
        this.adapterClickListener = adapterClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_users_list2, viewGroup, false)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        holder.setIsRecyclable(false)
        val item = datalist[i]
        holder.image.controller =
            frescoImageLoad(item.getProfilePic(), R.drawable.ic_user_icon, holder.image, false)
        if (item.business == 1) {
            holder.usernameTxt.text = item.first_name + " " + item.last_name
        } else {
            holder.usernameTxt.text = item.username
        }
        if (item.first_name != "") holder.nameTxt.text =
            item.first_name + " " + item.last_name else holder.nameTxt.visibility = View.GONE
        if (item.isSelected) {
            holder.tickbtn.visibility = View.VISIBLE
        } else {
            if (from != null && from == "@shops") {
                holder.tvFollowBtn.visibility = View.GONE
                holder.unFriendBtn.visibility = View.GONE
                holder.tickbtn.visibility = View.GONE
            } else {
                if (item.button != null) {
                    if (item.button == "follow" || item.button == "follow back") {
                        holder.tvFollowBtn.visibility = View.VISIBLE
                        holder.tvFollowBtn.text = item.button
                    } else if (item.button == "following" || item.button == "Friends") {
                        holder.unFriendBtn.visibility = View.VISIBLE
                        holder.unFriendBtn.text = item.button
                    }
                    holder.tickbtn.visibility = View.GONE
                }
            }
        }
        holder.unFriendBtn.setOnClickListener { view ->
            adapterClickListener.onItemClick(
                view,
                holder.absoluteAdapterPosition,
                item
            )
        }
        holder.tvFollowBtn.setOnClickListener { view ->
            adapterClickListener.onItemClick(
                view,
                holder.absoluteAdapterPosition,
                item
            )
        }
        holder.followerVideoTxt.text =
            getSuffix("" + item.followers_count) + " " + context.getString(R.string.followers) + ". " + item.video_count + " " + context.getString(
                R.string.videos
            )
        holder.bind(i, item, adapterClickListener)
    }

    inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: SimpleDraweeView
        var usernameTxt: TextView
        var nameTxt: TextView
        var followerVideoTxt: TextView
        var tvFollowBtn: TextView
        var unFriendBtn: TextView
        var tickbtn: ImageView

        init {
            image = view.findViewById(R.id.image)
            tickbtn = view.findViewById(R.id.tickbtn)
            usernameTxt = view.findViewById(R.id.username_txt)
            followerVideoTxt = view.findViewById(R.id.follower_video_txt)
            nameTxt = view.findViewById(R.id.name_txt)
            tvFollowBtn = view.findViewById(R.id.tvFollowBtn)
            unFriendBtn = view.findViewById(R.id.unFriendBtn)
        }

        fun bind(pos: Int, item: Any?, listener: AdapterClickListener) {
            itemView.setOnClickListener { v: View? -> listener.onItemClick(v, pos, item) }
        }
    }

    fun updateData(newData: MutableList<UserModel>) {
        datalist = newData
        notifyDataSetChanged()
    }
}
