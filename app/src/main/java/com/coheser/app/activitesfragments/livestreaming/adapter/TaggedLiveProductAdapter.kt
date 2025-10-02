package com.coheser.app.activitesfragments.livestreaming.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.databinding.TagProductLiveItemsBinding
import com.coheser.app.simpleclasses.Functions

class TaggedLiveProductAdapter(
    var context: Context,
    mlist: ArrayList<com.coheser.app.activitesfragments.shoping.models.ProductModel>,
    listener: com.coheser.app.interfaces.AdapterClickListener
) : RecyclerView.Adapter<TaggedLiveProductAdapter.MyViewHolder>() {
    var mlist = ArrayList<com.coheser.app.activitesfragments.shoping.models.ProductModel>()
    var listener: com.coheser.app.interfaces.AdapterClickListener

    init {
        this.mlist = mlist
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val binding: TagProductLiveItemsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.tag_product_live_items,
            parent,
            false
        )
        return MyViewHolder(binding)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = mlist[position]



            holder.binding.nameTxt.text = model.product?.title

              holder.binding.storeName.text = "from " + model.user?.username


            val price = model.product?.price?.toDouble() ?: 1
            holder.binding.priceTxt.text = com.coheser.app.Constants.CURRENCY+ price + " Commision 10%"

            if(!model.productImage.isEmpty())
            {
                holder.binding.image.setController(
                    Functions.frescoImageLoad(
                        model.productImage.get(0)!!.image,
                        holder.binding.image,
                        false
                    )
                )
            } else
            {
                holder.binding.image.setController(
                    Functions.frescoImageLoad(
                        ContextCompat.getDrawable(context,R.drawable.foodthumbnail),
                        holder.binding.image,
                        false
                    )
                )
            }


        holder.bind(position, model, listener)
    }

    override fun getItemCount(): Int {
        return mlist.size
    }


    inner class MyViewHolder(val binding: TagProductLiveItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            pos: Int,
            item: com.coheser.app.activitesfragments.shoping.models.ProductModel,
            listener: com.coheser.app.interfaces.AdapterClickListener
        ) {
            binding.removeBtn.setOnClickListener { view ->
                listener.onItemClick(view, pos, item)
            }
        }

    }
}
