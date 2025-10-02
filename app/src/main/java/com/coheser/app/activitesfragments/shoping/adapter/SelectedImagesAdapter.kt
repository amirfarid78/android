package com.coheser.app.activitesfragments.shoping.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.Utils.PhotoViewHolder
import com.coheser.app.databinding.ItemSelectedImagesListBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad

class SelectedImagesAdapter(
    var context: Context,
    productModels: ArrayList<Uri?>,
    adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<PhotoViewHolder>() {
    var datalist: ArrayList<Uri?> = ArrayList()
    var adapterClickListener: AdapterClickListener

    init {
        this.datalist = productModels
        this.adapterClickListener = adapterClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = DataBindingUtil.inflate<ItemSelectedImagesListBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_selected_images_list,
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val string = datalist[position]
        if (string != null) {
            holder.binding.Image.setImageURI(string)
            holder.binding.deleteImageBtn.visibility = View.VISIBLE
        } else {
            holder.binding.Image.controller = frescoImageLoad(
                context.getDrawable(R.drawable.ic_circle_add), holder.binding.Image, false
            )
            holder.binding.deleteImageBtn.visibility = View.GONE
        }

        holder.bind(position, string, adapterClickListener)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }
}
