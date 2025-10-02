package com.coheser.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.HomeModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getSuffix
import com.coheser.app.simpleclasses.Functions.isStringHasValue
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.databinding.ItemMyvideoLayoutBinding
import com.coheser.app.simpleclasses.MyViewHolder

/**
 * Created by qboxus on 3/20/2018.
 */
class MyVideosAdapter(
    var context: Context,
    private val dataList: MutableList<HomeModel>,
    var whereFrom: String,
    var adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<MyVideosAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val binding: ItemMyvideoLayoutBinding = ItemMyvideoLayoutBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = dataList[i]
        try {
            if (Constants.IS_SHOW_GIF) {
                holder.binding.thumbImage.controller =
                    frescoImageLoad(item.getGif(), holder.binding.thumbImage, true)
            } else if (isStringHasValue(item.getThum())) {
                holder.binding.thumbImage.controller =
                    frescoImageLoad(item.getThum(), holder.binding.thumbImage, false)
            }
        } catch (e: Exception) {
            printLog(Constants.tag, e.toString())
        }
        if (whereFrom == "myProfile") {
            if (item.pin == "1") {
                holder.binding.tabPinned.visibility = View.VISIBLE
            } else {
                holder.binding.tabPinned.visibility = View.GONE
            }
        } else {
            holder.binding.tabPinned.visibility = View.GONE
        }
        holder.binding.viewTxt.text = item.views
        holder.binding.viewTxt.text = getSuffix(item.views)
        holder.onBind(i, item, adapterClickListener)
    }

    inner class CustomViewHolder(val binding: ItemMyvideoLayoutBinding) : MyViewHolder(binding.root) {

        override fun onBind(pos: Int, item: Any?, listener: AdapterClickListener) {
            itemView.setOnClickListener { v: View? -> listener.onItemClick(v, pos, item) }
        }
    }
}