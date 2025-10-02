package com.coheser.app.activitesfragments.livestreaming.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.StreamingConstants
import io.agora.rtc2.video.VideoEncoderConfiguration.VideoDimensions
import kotlin.math.min

class ResolutionAdapter(private val mContext: Context, var selected: Int) :
    RecyclerView.Adapter<ResolutionAdapter.ResolutionHolder>() {
    private val mItems = ArrayList<ResolutionItem>()

    init {
        initData(mContext)
    }

    private fun initData(context: Context) {
        val size = StreamingConstants.VIDEO_DIMENSIONS.size
        val labels = context.resources.getStringArray(R.array.string_array_resolutions)
        val minSize = min(
            size.toDouble(),
            min(labels.size.toDouble(), StreamingConstants.VIDEO_DIMENSIONS.size.toDouble())
        ).toInt()
        for (i in 0 until minSize) {
            val item = ResolutionItem(labels[i], StreamingConstants.VIDEO_DIMENSIONS[i])
            mItems.add(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResolutionAdapter.ResolutionHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.dimension_item, parent, false)
        return ResolutionHolder(view)
    }

    override fun onBindViewHolder(holder: ResolutionHolder, position: Int) {
        val item = mItems[position]
        val content = (holder as ResolutionHolder).resolution
        content.text = item.label

        content.setOnClickListener { v: View? ->
            selected = position
            notifyDataSetChanged()
        }

        if (position == selected) content.isSelected = true
        else content.isSelected = false
    }


    override fun getItemCount(): Int {
        return mItems.size
    }

    inner class ResolutionHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var resolution: TextView = itemView.findViewById(R.id.resolution)
    }

    private class ResolutionItem(var label: String, var dimension: VideoDimensions)
}
