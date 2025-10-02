package com.coheser.app.activitesfragments.spaces.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.databinding.FriendsSelectionItemViewBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad

class FriendsSelectionAdapter(var list: ArrayList<UserModel>, var listener: AdapterClickListener) :
    RecyclerView.Adapter<FriendsSelectionAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FriendsSelectionItemViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.ivProfile.controller = frescoImageLoad(
            holder.binding.root.context,
            item.username!!, item.getProfilePic(), holder.binding.ivProfile
        )
        if (item.isSelected) {
            holder.binding.tabProfile.alpha = 0.3f
            holder.binding.ivSelect.visibility = View.VISIBLE
        } else {
            holder.binding.tabProfile.alpha = 1f
            holder.binding.ivSelect.visibility = View.GONE
        }

        if (item.online == 1) {
            holder.binding.ivOnline.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.root.context,
                    R.drawable.d_online_circle_green
                )
            )
        } else {
            holder.binding.ivOnline.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.root.context,
                    R.drawable.d_offline_circle_green
                )
            )
        }

        holder.bind(position, listener, item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(var binding: FriendsSelectionItemViewBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(position: Int, listener: AdapterClickListener, `object`: Any?) {
            itemView.setOnClickListener { v: View? -> listener.onItemClick(v, position, `object`) }
        }
    }
}
