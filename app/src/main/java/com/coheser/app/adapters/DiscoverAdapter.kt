package com.coheser.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.models.DiscoverModel
import com.coheser.app.models.HomeModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getSuffix
import com.coheser.app.simpleclasses.Functions.printLog
import com.facebook.drawee.view.SimpleDraweeView

/**
 * Created by qboxus on 3/20/2018.
 */
class DiscoverAdapter(
    var context: Context,
    var datalist: MutableList<DiscoverModel>,
    listener: OnItemClickListener
) : RecyclerView.Adapter<DiscoverAdapter.CustomViewHolder>() {

    var listener: OnItemClickListener
    init {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewtype: Int
    ): CustomViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_discover_layout, viewGroup, false)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = datalist[i]
        holder.title.text = context.getString(R.string.hash) + item.title
        holder.viewsTxt.text =
            getSuffix(item.videos_count) + context.getString(R.string.posts)
        val adapter = HorizontalAdapter(context, i, item.arrayList)
        val layoutManager = GridLayoutManager(holder.itemView.context, 1)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        holder.horizontal_reycerview.layoutManager = layoutManager
        holder.horizontal_reycerview.adapter = adapter
        holder.bind(i, item.arrayList)
    }

    interface OnItemClickListener {
        fun onItemClick(
            view: View?,
            video_list: ArrayList<HomeModel?>,
            main_position: Int,
            child_position: Int
        )
    }

    inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var horizontal_reycerview: RecyclerView
        var title: TextView
        var viewsTxt: TextView
        var hashtagLayout: RelativeLayout

        init {
            horizontal_reycerview = view.findViewById(R.id.horizontal_recylerview)
            title = view.findViewById(R.id.title)
            viewsTxt = view.findViewById(R.id.views_txt)
            hashtagLayout = view.findViewById(R.id.hashtag_layout)
        }

        fun bind(pos: Int, datalist: ArrayList<HomeModel?>) {
            hashtagLayout.setOnClickListener { v -> listener.onItemClick(v, datalist, pos, -1) }
        }
    }

    internal inner class HorizontalAdapter(
        var context: Context,
        var main_position: Int,
        var datalist: ArrayList<HomeModel?>
    ) : RecyclerView.Adapter<HorizontalAdapter.CustomViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_discover_horizontal_layout, viewGroup, false)
            return CustomViewHolder(view)
        }

        override fun getItemCount(): Int {
            return datalist.size
        }

        override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
            holder.setIsRecyclable(false)
            val item = datalist[i]
            holder.bind(i, datalist)
            if (item != null) {
                holder.tab_more_txt.visibility = View.GONE
                try {
                    if (Constants.IS_SHOW_GIF) {
                        holder.video_thumbnail.controller =
                            frescoImageLoad(item.getGif(), holder.video_thumbnail, true)
                    } else {
                        holder.video_thumbnail.controller = frescoImageLoad(
                            item.getThum(),
                            R.drawable.image_placeholder,
                            holder.video_thumbnail,
                            false
                        )
                    }
                } catch (e: Exception) {
                    printLog(Constants.tag, e.toString())
                }
            } else {
                holder.tab_more_txt.visibility = View.VISIBLE
                holder.video_thumbnail.visibility = View.GONE
            }
        }

        internal inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var video_thumbnail: SimpleDraweeView
            var tab_more_txt: TextView

            init {
                video_thumbnail = view.findViewById(R.id.video_thumbnail)
                tab_more_txt = view.findViewById(R.id.tab_more_txt)
            }

            fun bind(pos: Int, datalist: ArrayList<HomeModel?>) {
                itemView.setOnClickListener { v: View? ->
                    listener.onItemClick(
                        itemView,
                        datalist,
                        main_position,
                        pos
                    )
                }
            }
        }
    }
}