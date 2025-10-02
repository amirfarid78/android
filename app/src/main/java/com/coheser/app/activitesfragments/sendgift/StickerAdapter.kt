package com.coheser.app.activitesfragments.sendgift

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.databinding.ItemSendGifLayoutBinding
import com.coheser.app.simpleclasses.Functions.frescoImageLoad

class StickerAdapter(
    var context: Context,
    val from:String,
    gif_list: List<GiftModel>,
    listener: OnItemClickListener
) : RecyclerView.Adapter<StickerAdapter.CustomViewHolder>() {
    var gifList: List<GiftModel> = ArrayList()
    private val listener: OnItemClickListener

    init {
        this.gifList = gif_list
        this.listener = listener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val binding = ItemSendGifLayoutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
         return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return gifList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val model = gifList[i]


        holder.binding.gifImage.controller =
            frescoImageLoad(model.icon, holder.binding.gifImage, false)
        holder.binding.nameTxt.text = model.title
        holder.binding.coinTxt.text = ""+model.coin
        if(from == StickerGiftFragment.fromSendGift) {

            if (model.isSelected) {
                holder.binding.sendBtn.visibility = View.VISIBLE
                holder.binding.nameTxt.visibility = View.GONE
            } else {
                holder.binding.sendBtn.visibility = View.GONE
                holder.binding.nameTxt.visibility = View.VISIBLE
            }
        }
        else{
            if(model.isSelected){
                holder.binding.mainLayout.background = ContextCompat.getDrawable(context,R.drawable.d_round_colord_outline_6)
            }
            else{
                holder.binding.mainLayout.background=null
            }
        }

        holder.bind(i, model, listener)
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int, view: View?, item: GiftModel?)
    }

    inner class CustomViewHolder(val binding: ItemSendGifLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, item: GiftModel?, listener: OnItemClickListener) {
            itemView.setOnClickListener { v -> listener.onItemClick(position, v, item) }

            binding.sendBtn.setOnClickListener { view -> listener.onItemClick(position, view, item) }
        }
    }
}