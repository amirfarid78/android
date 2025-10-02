package com.coheser.app.activitesfragments.spaces.adapters

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.spaces.models.HomeUserModel
import com.coheser.app.databinding.RiseHandUserItemViewBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.realpacific.clickshrinkeffect.applyClickShrink

class RiseHandUsersAdapter(var list: ArrayList<HomeUserModel>, var listener: AdapterClickListener) :
    RecyclerView.Adapter<RiseHandUsersAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RiseHandUserItemViewBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.tvName.text =
            item.userModel?.first_name + " " + item.userModel?.last_name

        holder.binding.ivProfile.controller = frescoImageLoad(
            holder.binding.root.context,
            item.userModel?.username!!, item.userModel?.getProfilePic(), holder.binding.ivProfile
        )


        if (item.riseHand == "2") {
            holder.binding.tabAddToSpeak.background = ContextCompat.getDrawable(
                holder.binding.root.context,
                R.drawable.d_round_gray25
            )
            holder.binding.ivAdd.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.root.context,
                    R.drawable.ic_tick
                )
            )
            holder.binding.ivAdd.setColorFilter(
                ContextCompat.getColor(
                    holder.binding.root.context,
                    R.color.appColor
                ), PorterDuff.Mode.MULTIPLY
            )
            holder.binding.ivMice.setColorFilter(
                ContextCompat.getColor(
                    holder.binding.root.context,
                    R.color.appColor
                ), PorterDuff.Mode.MULTIPLY
            )
        } else {
            holder.binding.tabAddToSpeak.background = ContextCompat.getDrawable(
                holder.binding.root.context,
                R.drawable.button_rounded_background
            )
            holder.binding.ivAdd.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.root.context,
                    R.drawable.ic_add
                )
            )
            holder.binding.ivAdd.setColorFilter(
                ContextCompat.getColor(
                    holder.binding.root.context,
                    R.color.white
                ), PorterDuff.Mode.MULTIPLY
            )
            holder.binding.ivMice.setColorFilter(
                ContextCompat.getColor(
                    holder.binding.root.context,
                    R.color.white
                ), PorterDuff.Mode.MULTIPLY
            )
        }

        holder.bind(position, listener, item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(var binding: RiseHandUserItemViewBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(position: Int, listener: AdapterClickListener, `object`: Any?) {
            binding.tabAddToSpeak.setOnClickListener { v: View? ->
                listener.onItemClick(
                    v,
                    position,
                    `object`
                )
            }
            binding.tabAddToSpeak.applyClickShrink()

            binding.ivProfile.setOnClickListener { v: View? ->
                listener.onItemClick(
                    v,
                    position,
                    `object`
                )
            }
            binding.ivProfile.applyClickShrink()
        }
    }
}
