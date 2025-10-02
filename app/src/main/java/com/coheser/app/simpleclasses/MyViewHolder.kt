package com.coheser.app.simpleclasses

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.interfaces.AdapterClickListener

abstract class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun onBind(pos: Int, item: Any?, listener: AdapterClickListener)

}