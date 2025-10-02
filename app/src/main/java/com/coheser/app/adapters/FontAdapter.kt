package com.coheser.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.databinding.ItemFontListBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.FontModel

class FontAdapter(
    var context: Context,
    private val dataList: ArrayList<FontModel>,
    var adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<FontAdapter.CustomViewHolder?>() {
    var selectedFont: FontModel? = null
    fun updateSelectedFont(selectedFont: FontModel?) {
        this.selectedFont = selectedFont
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_font_list, viewGroup, false)
        val binding = ItemFontListBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = dataList[i]
        if (item.name == selectedFont!!.name) {
            holder.binding.mainDiv.background = ContextCompat.getDrawable(
                context,
                R.drawable.d_rounded_white_border_2
            )
        } else {
            holder.binding.mainDiv.background = ContextCompat.getDrawable(
                context,
                R.drawable.d_rounded_white_border_1
            )
        }
        holder.binding.text.text = item.name
        val typeface = ResourcesCompat.getFont(
            context, item.font
        )
        holder.binding.text.typeface = typeface
        holder.bind(i, item, adapterClickListener)
    }

    inner class CustomViewHolder(val binding: ItemFontListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, item: FontModel?, listener: AdapterClickListener?) {
            itemView.setOnClickListener { v: View? ->
                adapterClickListener.onItemClick(
                    v,
                    position,
                    item
                )
            }
        }
    }
}
