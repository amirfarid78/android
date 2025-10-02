package com.coheser.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.facebook.drawee.view.SimpleDraweeView

class BlockUserAdapter(
    private val list: MutableList<UserModel>,
    private val click: AdapterClickListener
) : RecyclerView.Adapter<BlockUserAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_block_user, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.tvUserName.text = model.username
        holder.ivProfileImg.controller = frescoImageLoad(
            model.getProfilePic(),
            R.drawable.ic_user_icon,
            holder.ivProfileImg,
            false
        )
        holder.bind(position, model, click)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUserName: TextView
        var ivProfileImg: SimpleDraweeView
        var tabBlock: RelativeLayout
        var mainLayout: RelativeLayout

        init {
            tvUserName = itemView.findViewById(R.id.tvUserName)
            ivProfileImg = itemView.findViewById(R.id.ivProfile)
            tabBlock = itemView.findViewById(R.id.block_layout)
            mainLayout = itemView.findViewById(R.id.mainLayout)
        }

        fun bind(pos: Int, model: Any?, listener: AdapterClickListener) {
            tabBlock.setOnClickListener { v -> listener.onItemClick(v, pos, model) }
            mainLayout.setOnClickListener { v -> listener.onItemClick(v, pos, model) }
        }
    }
}
