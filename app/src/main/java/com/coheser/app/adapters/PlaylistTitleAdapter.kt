package com.coheser.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.PlaylistTitleModel

class PlaylistTitleAdapter(
    private val dataList: MutableList<PlaylistTitleModel>,
    var adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<PlaylistTitleAdapter.CustomViewHolder?>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_playlist_title_layout, viewGroup, false)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = dataList[i]
        holder.tvTitle.text = item.name
        if (item.id == "0") {
            holder.tvTitle.visibility = View.GONE
            holder.ivTitle.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.ic_add_round
                )
            )
        } else {
            holder.tvTitle.visibility = View.VISIBLE
            holder.ivTitle.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.ic_playlist_add
                )
            )
        }
        holder.bind(i, item, adapterClickListener)
    }

    inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle: TextView = view.findViewById(R.id.tvTitle)
        var ivTitle: ImageView = view.findViewById(R.id.ivTitle)

        fun bind(position: Int, item: Any?, listener: AdapterClickListener?) {
            itemView.setOnClickListener { v: View? ->
                adapterClickListener.onItemClick(v, position, item)
            }
        }
    }
}