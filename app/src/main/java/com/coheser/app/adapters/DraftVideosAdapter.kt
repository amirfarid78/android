package com.coheser.app.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.databinding.ItemGalleryvideoLayoutBinding
import com.coheser.app.models.DraftVideoModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import java.io.File

/**
 * Created by qboxus on 3/20/2018.
 */
class DraftVideosAdapter(
    var context: Context,
    private val dataList: ArrayList<DraftVideoModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<DraftVideosAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val binding = ItemGalleryvideoLayoutBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = dataList[i]
        holder.binding.viewTxt.text = item.video_time
        if (item.video_path != null && item.video_path != "") {

            //video_path
            val uri = Uri.fromFile(File(item.video_path))
            holder.binding.thumbImage.controller =
                frescoImageLoad(uri, false)
        }
        holder.bind(i, item, listener)
    }

    interface OnItemClickListener {
        fun onItemClick(postion: Int, item: DraftVideoModel?, view: View?)
    }

    inner class CustomViewHolder(val binding : ItemGalleryvideoLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int, item: DraftVideoModel?, listener: OnItemClickListener) {
            itemView.setOnClickListener { v: View? -> listener.onItemClick(position, item, v) }
            binding.crossBtn.setOnClickListener { v: View? -> listener.onItemClick(position, item, v) }
        }
    }
}