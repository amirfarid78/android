package com.coheser.app.activitesfragments.shoping.adapter

import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.models.GalleryModel
import com.facebook.drawee.view.SimpleDraweeView

class GalleryAdapter(
    private val items: List<GalleryModel>,
    val listener: com.coheser.app.interfaces.AdapterClickListener
) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        if (item.actualUri.endsWith(".mp4")) { // Video file
            val thumbnail = ThumbnailUtils.createVideoThumbnail(
                item.thumbnailUri,
                MediaStore.Video.Thumbnails.MINI_KIND
            )
            holder.imageView.setImageBitmap(thumbnail)
        } else { // Image file
            holder.imageView.setImageURI(Uri.parse(item.actualUri))
        }

        if (item.isSelected) {
            holder.selectImg.visibility = View.VISIBLE
            holder.counterTxt.text = "${item.selectionCount}"

        } else {
            holder.selectImg.visibility = View.GONE
        }

        holder.onBind(position, item)
    }



    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: SimpleDraweeView = itemView.findViewById(R.id.imageView)
        val checkImg: ImageView = itemView.findViewById(R.id.checkImg)
        val selectImg: RelativeLayout = itemView.findViewById(R.id.selectImg)
        val counterTxt: TextView = itemView.findViewById(R.id.counter)

        fun onBind(postion: Int, model: GalleryModel) {
            imageView.setOnClickListener {
                listener.onItemClick(it, postion, model)
            }
        }
    }
}