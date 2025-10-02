package com.coheser.app.activitesfragments.spaces.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.activitesfragments.spaces.models.UserSuggestionModel
import com.coheser.app.databinding.UserProfileSuggestionItemViewBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad

class ProfileSuggestionAdapter(
    var list: ArrayList<UserSuggestionModel>,
    var listener: AdapterClickListener
) : RecyclerView.Adapter<ProfileSuggestionAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = UserProfileSuggestionItemViewBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.ivProfile.controller = frescoImageLoad(
            holder.binding.root.context,
            item.userModel?.username!!, item.userModel?.getProfilePic(), holder.binding.ivProfile
        )

        holder.binding.tvFullName.text = item.userModel?.first_name + " " + item.userModel?.last_name
        holder.binding.tvBio.text = item.userModel?.bio
        holder.binding.tvFollow.text = item.userModel?.button


        holder.bind(position, listener, item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(var binding: UserProfileSuggestionItemViewBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun bind(position: Int, listener: AdapterClickListener, `object`: Any?) {
            binding.tabFollow.setOnClickListener { v: View? ->
                listener.onItemClick(
                    v,
                    position,
                    `object`
                )
            }
            binding.tabProfile.setOnClickListener { v: View? ->
                listener.onItemClick(
                    v,
                    position,
                    `object`
                )
            }
            binding.tabRemove.setOnClickListener { v: View? ->
                listener.onItemClick(
                    v,
                    position,
                    `object`
                )
            }
        }
    }
}
