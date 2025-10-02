package com.coheser.app.activitesfragments.spaces.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.spaces.models.HomeUserModel
import com.coheser.app.databinding.CurrentSpeakerItemViewBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.realpacific.clickshrinkeffect.applyClickShrink

class CurrentSpeakerRoomAdapter(
    var list: ArrayList<HomeUserModel>,
    var listener: AdapterClickListener
) : RecyclerView.Adapter<CurrentSpeakerRoomAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<CurrentSpeakerItemViewBinding>(
            LayoutInflater
                .from(parent.context), R.layout.current_speaker_item_view, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.tvUsername.text = item.userModel?.username

        holder.binding.ivProfile.controller = frescoImageLoad(
            holder.binding.root.context,
            ""+item.userModel?.username,
            item.userModel?.getProfilePic(),
            holder.binding.ivProfile
        )


        if (item.userRoleType == "1") {
            holder.binding.ivModerator.visibility = View.VISIBLE
        } else {
            holder.binding.ivModerator.visibility = View.GONE
        }

        if (item.userRoleType == "1") {
            if (item.mice == "1") {
                holder.binding.ivMuteMice.visibility = View.GONE
            } else {
                holder.binding.ivMuteMice.visibility = View.VISIBLE
            }

            holder.binding.ivRiseHand.visibility = View.GONE
        } else if (item.userRoleType == "2") {
            if (item.mice == "1") {
                holder.binding.ivMuteMice.visibility = View.GONE
            } else {
                holder.binding.ivMuteMice.visibility = View.VISIBLE
            }

            holder.binding.ivRiseHand.visibility = View.GONE
        } else {
            holder.binding.ivMuteMice.visibility = View.GONE

            if (item.riseHand == "1") {
                holder.binding.ivRiseHand.visibility = View.VISIBLE
            } else {
                holder.binding.ivRiseHand.visibility = View.GONE
            }
        }


        holder.bind(position, listener, item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(var binding: CurrentSpeakerItemViewBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(position: Int, listener: AdapterClickListener, `object`: Any?) {
            binding.tabMain.setOnClickListener { v: View? ->
                listener.onItemClick(
                    v,
                    position,
                    `object`
                )
            }
            binding.tabMain.applyClickShrink()
        }
    }
}
