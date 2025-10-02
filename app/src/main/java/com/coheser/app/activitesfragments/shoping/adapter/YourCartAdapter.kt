package com.coheser.app.activitesfragments.shoping.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getProductPrice

class YourCartAdapter(
    var context: Context,
    var datalist: ArrayList<ProductModel>,
    var adapterClicklistener: AdapterClickListener
) : RecyclerView.Adapter<YourCartAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.adapter_cart_layout, null)
        val viewHolder: CustomViewHolder = CustomViewHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = datalist[position]
        if (item.productImage != null && !item.productImage.isEmpty()) {
            holder.ivProduct.controller = frescoImageLoad(
                item.productImage[0].image,
                R.drawable.image_placeholder,
                holder.ivProduct,
                false
            )
        }

        holder.tvPrice.text =
            Constants.productShowingCurrency + getProductPrice(
                item
            )

        val builder = StringBuilder()
        for (i in item.productAttribute.indices) {
            val productAttribute = item.productAttribute[i]
            builder.append(productAttribute.name + ":")
            builder.append(productAttribute.productAttributeVariation[0].value)
            builder.append(" ")
        }
        holder.tvSize.text = builder.toString()


        holder.tvTitle.text = item.product.title
        holder.tvQuantity.text = "" + item.product.count

        holder.bind(position, item, adapterClicklistener)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle: TextView = view.findViewById(R.id.tv_title)
        var tvSize: TextView = view.findViewById(R.id.tv_size)
        var tvPrice: TextView = view.findViewById(R.id.tv_current_price)
        var tvQuantity: TextView = view.findViewById(R.id.tv_quantity)
        var ivProduct: SimpleDraweeView = view.findViewById(R.id.iv_shirt)
        var rlPlus: RelativeLayout = view.findViewById(R.id.rl_plus)
        var rlMinus: RelativeLayout = view.findViewById(R.id.rl_minus)
        var deleteProductBtn: ImageView = view.findViewById(R.id.delete_product_btn)

        fun bind(pos: Int, productModel: ProductModel?, listener: AdapterClickListener?) {
            rlPlus.setOnClickListener { v: View? ->
                adapterClicklistener.onItemClick(
                    v,
                    pos,
                    productModel
                )
            }
            rlMinus.setOnClickListener { v: View? ->
                adapterClicklistener.onItemClick(
                    v,
                    pos,
                    productModel
                )
            }
            deleteProductBtn.setOnClickListener { v: View? ->
                adapterClicklistener.onItemClick(
                    v,
                    pos,
                    productModel
                )
            }
        }
    }
}

