package com.coheser.app.activitesfragments.shoping.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.databinding.ItemProfileProductsLayoutBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getProductPrice

class ProfileProductsAdapter(
    var context: Context,
    productModels: ArrayList<ProductModel>,
    adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<ProfileProductsAdapter.MyViewholder>() {
    var datalist: ArrayList<ProductModel> = ArrayList()
    var adapterClickListener: AdapterClickListener

    init {
        this.datalist = productModels
        this.adapterClickListener = adapterClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewholder {
        val binding = ItemProfileProductsLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        holder.binding.productName.text = productModel.product.title

        holder.binding.productPrice.text =
            Constants.productShowingCurrency + getProductPrice(
                productModel
            )


        holder.itemView.setOnClickListener {
            adapterClickListener.onItemClick(it,position,productModel)
        }
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    inner class MyViewholder(var binding: ItemProfileProductsLayoutBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {

    }
}
