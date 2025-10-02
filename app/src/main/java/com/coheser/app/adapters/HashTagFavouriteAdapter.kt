package com.coheser.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.databinding.ItemHashtagFavouriteListBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.HashTagModel
import com.coheser.app.simpleclasses.Functions.getSuffix
import java.util.Locale

class HashTagFavouriteAdapter(
    var context: Context,
    var datalist: ArrayList<HashTagModel>,
    var adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<HashTagFavouriteAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val binding = ItemHashtagFavouriteListBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        holder.setIsRecyclable(false)
        val item = datalist[i]
        holder.binding.nameTxt.text = "#" + item.name
        val videoCount = Integer.valueOf(item.videos_count)
        if (videoCount > 1) {
            holder.binding.viewsTxt.text =
                getSuffix("" + videoCount) + " " + holder.itemView.context.getString(
                    R.string.videos
                ).lowercase(Locale.getDefault())
        } else {
            holder.binding.viewsTxt.text =
                getSuffix("" + videoCount) + " " + holder.itemView.context.getString(
                    R.string.video
                ).lowercase(Locale.getDefault())
        }
        holder.bind(i, item, adapterClickListener)
    }

    inner class CustomViewHolder(val binding: ItemHashtagFavouriteListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pos: Int, item: Any?, listener: AdapterClickListener) {
            itemView.setOnClickListener { v: View? -> listener.onItemClick(v, pos, item) }
        }
    }
}
