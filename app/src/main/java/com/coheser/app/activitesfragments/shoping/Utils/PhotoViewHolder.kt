package com.coheser.app.activitesfragments.shoping.Utils

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.databinding.ItemSelectedImagesListBinding
import com.coheser.app.interfaces.AdapterClickListener

class PhotoViewHolder(binding: ItemSelectedImagesListBinding) :
    RecyclerView.ViewHolder(binding.getRoot()) {
    var binding: ItemSelectedImagesListBinding

    init {
        this.binding = binding
    }

    fun bind(position: Int, item: Uri?, listener: AdapterClickListener) {
        binding.mainLayout.setOnClickListener { v -> listener.onItemClick(v, position, item) }
        binding.deleteImageBtn.setOnClickListener { v -> listener.onItemClick(v, position, item) }
    }
}
