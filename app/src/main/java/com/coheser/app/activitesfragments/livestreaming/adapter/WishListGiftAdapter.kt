package com.coheser.app.activitesfragments.livestreaming.adapter

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.model.GiftWishListModel
import com.coheser.app.databinding.ItemWishlistGifts2Binding
import com.coheser.app.interfaces.AdapterClickListener
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.imagepipeline.request.ImageRequestBuilder

class WishListGiftAdapter(
    var context: Context,
    user_dataList: ArrayList<GiftWishListModel>,
    listener: AdapterClickListener
) : RecyclerView.Adapter<WishListGiftAdapter.CustomViewHolder>() {
    var user_dataList: ArrayList<GiftWishListModel> = ArrayList()

    var adapterClickListener: AdapterClickListener

    init {
        this.user_dataList = user_dataList
        this.adapterClickListener = listener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val binding = DataBindingUtil.inflate<ItemWishlistGifts2Binding>(
            LayoutInflater
                .from(viewGroup.context), R.layout.item_wishlist_gifts2, viewGroup, false
        )
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return user_dataList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = user_dataList[i]
        holder.bind(i, adapterClickListener, item)

        holder.binding.giftReceiveCountTxt.text = item.totalGiftReceived
        holder.binding.giftwantTxt.text = "/"+item.totalGiftWant

        val percentage=(item.totalGiftReceived.toInt()*100)/item.totalGiftWant.toInt()
        holder.binding.prograssbar.progress=percentage

        if (!TextUtils.isEmpty(item.giftImage)) {
            val request =
                ImageRequestBuilder.newBuilderWithSource(Uri.parse(item.giftImage)).build()
            val controller: DraweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(holder.binding.giftImage.controller)
                .build()
            holder.binding.giftImage.controller = controller
        }
    }

    inner class CustomViewHolder(var binding: ItemWishlistGifts2Binding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(position: Int, listener: AdapterClickListener, `object`: Any?) {
            binding.giftCard.setOnClickListener { v: View? ->
                listener.onItemClick(
                    v,
                    position,
                    `object`
                )
            }
        }
    }

}