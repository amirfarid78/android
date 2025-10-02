package com.coheser.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.interfaces.AdapterClickListener

/**
 * Created by qboxus on 3/19/2019.
 */
class RecentSearchAdapter(var datalist: MutableList<String>, var listener: AdapterClickListener) :
    RecyclerView.Adapter<RecentSearchAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_recent_search_list, viewGroup, false)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = datalist[i]
        holder.nameTxt.text = item
        holder.bind(i, item, listener)
    }

    fun filter(filter_list: ArrayList<String>?) {
        datalist.clear()
        datalist.addAll(filter_list!!)
        notifyDataSetChanged()
    }

    inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var nameTxt: TextView
        var deleteBtn: ImageButton

        init {
            nameTxt = view.findViewById(R.id.name_txt)
            deleteBtn = view.findViewById(R.id.delete_btn)
        }

        fun bind(pos: Int, item: Any?, listener: AdapterClickListener) {
            itemView.setOnClickListener { v: View? -> listener.onItemClick(v, pos, item) }
            deleteBtn.setOnClickListener { v: View? -> listener.onItemClick(v, pos, item) }
        }
    }
}
