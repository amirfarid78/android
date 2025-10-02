package com.coheser.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.databinding.ItemHashtagListBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.HashTagModel
import com.coheser.app.simpleclasses.Functions.getSuffix

/**
 * Created by qboxus on 3/19/2019.
 */
class HashTagAdapter(
    var context: Context,
    var datalist: ArrayList<HashTagModel>,
    var adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<HashTagAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewtype: Int
    ): CustomViewHolder {
        val binding = ItemHashtagListBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        holder.setIsRecyclable(false)
        val item = datalist[i]
        holder.binding.nameTxt.text = item.name
        holder.binding.viewsTxt.text = getSuffix(item.videos_count)
        holder.bind(i, item, adapterClickListener)
    }

    inner class CustomViewHolder(val binding: ItemHashtagListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pos: Int, item: Any?, listener: AdapterClickListener) {
            itemView.setOnClickListener { v: View? -> listener.onItemClick(v, pos, item) }
        }
    }
}
