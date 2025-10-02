package com.coheser.app.activitesfragments.shoping.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.models.OrderHistoryModel
import com.coheser.app.databinding.ItemOrderDetailBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad


class OrderDetailAdapter(
    var context: Context,
    var model: OrderHistoryModel,
    var adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<OrderDetailAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewtype: Int): CustomViewHolder {
        val binding = DataBindingUtil.inflate<ItemOrderDetailBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_order_detail,
            parent,
            false
        )
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        holder.binding.orderImage.controller =
            frescoImageLoad(
                model.product.productImage!!.first()!!.image,
                R.drawable.image_placeholder,
                holder.binding.orderImage,
                false
            )

        holder.binding.orderDetailPrice.text = Constants.productShowingCurrency + model.order.total
        holder.binding.orderDetailTitle.text = model.product.title
        holder.binding.orderDetailQuantity.text = "${(model.order.total!!.toDouble() / model.product.price!!.toDouble()).toInt()}"
//        holder.binding.orderDetailSize.text = item.product_attritube_combination_id


        if (model.order.status == "2") {
            holder.binding.ratingbar.visibility = View.VISIBLE
        } else {
            holder.binding.ratingbar.visibility = View.GONE
        }

//        if (item.productRating != null) {
//            holder.binding.ratingbar.rating = item.productRating.star.toInt().toFloat()
//        }

        holder.bind(position, model, adapterClickListener)
    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class CustomViewHolder(var binding: ItemOrderDetailBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(position: Int, item: OrderHistoryModel?, listener: AdapterClickListener) {
            binding.ratingLayout.setOnClickListener { v: View? ->
                listener.onItemClick(v, position, item)
            }
        }
    }
}