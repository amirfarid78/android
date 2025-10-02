package com.coheser.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.databinding.ItemSoundLayoutBinding
import com.coheser.app.models.SoundsModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad

/**
 * Created by qboxus on 3/19/2019.
 */
class FavouriteSoundAdapter(
    var context: Context,
    var datalist: ArrayList<SoundsModel>,
    var listener: OnItemClickListener
) : RecyclerView.Adapter<FavouriteSoundAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val binding = ItemSoundLayoutBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        holder.setIsRecyclable(false)
        val item = datalist[i]
        try {
            holder.binding.soundName.text = item.name
            holder.binding.descriptionTxt.text = item.description
            holder.binding.durationTimeTxt.text = item.duration
            holder.binding.soundImage.controller = frescoImageLoad(
                item.thum,
                R.drawable.ractengle_solid_primary,
                holder.binding.soundImage,
                false
            )
            holder.binding.favBtn.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_my_favourite
                )
            )
            holder.bind(i, datalist[i], listener)
        } catch (e: Exception) {
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, postion: Int, item: SoundsModel?)
    }

    inner class CustomViewHolder(val binding: ItemSoundLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pos: Int, item: SoundsModel?, listener: OnItemClickListener) {
            itemView.setOnClickListener { v: View? -> listener.onItemClick(v, pos, item) }
            binding.done.setOnClickListener { v: View? -> listener.onItemClick(v, pos, item) }
            binding.favBtn.setOnClickListener { v: View? -> listener.onItemClick(v, pos, item) }
        }
    }
}
