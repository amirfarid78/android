package com.coheser.app.activitesfragments.shoping.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.databinding.HoriRecylerItemBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getProductPrice

class HorizontalProductsAdapter(
    var context: Context,
    productModels: ArrayList<ProductModel>,
    adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<HorizontalProductsAdapter.MyViewholder?>() {
    var datalist: ArrayList<ProductModel> = ArrayList()
    var adapterClickListener: AdapterClickListener


    init {
        this.datalist = productModels
        this.adapterClickListener = adapterClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewholder {
        val binding = DataBindingUtil.inflate<HoriRecylerItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.hori_recyler_item,
            parent,
            false
        )
        return MyViewholder(binding)
    }

    override fun onBindViewHolder(holder: MyViewholder, position: Int) {
        val productModel = datalist[position]
        if (productModel.productImage.size > 0) {
            holder.binding.productImage.controller =
                frescoImageLoad(
                    productModel.productImage[0].image,
                    holder.binding.productImage,
                    false
                )
        }
        holder.binding.priceTxt.text =
            Constants.productShowingCurrency + getProductPrice(
                productModel
            )
        holder.bind(position, productModel, adapterClickListener)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    inner class MyViewholder(var binding: HoriRecylerItemBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(position: Int, item: ProductModel?, listener: AdapterClickListener) {
            binding.mainLayout.setOnClickListener { v: View? ->
                listener.onItemClick(v, position, item)
            }
        }
    }
}
