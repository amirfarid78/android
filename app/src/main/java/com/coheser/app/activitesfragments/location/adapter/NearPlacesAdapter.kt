package com.coheser.app.activitesfragments.location.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.location.AddressPlacesModel
import com.coheser.app.interfaces.AdapterClickListener

class NearPlacesAdapter(
    var context: Context,
    mlist: MutableList<AddressPlacesModel>,
    adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<NearPlacesAdapter.MyViewHolder>() {
    var mlist : MutableList<AddressPlacesModel>
    var adapterClickListener: AdapterClickListener

    init {
        this.mlist = mlist
        this.adapterClickListener = adapterClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_places, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = mlist[position]
        holder.locationTitle.text = model.title
        holder.locationAddress.text = model.address
        holder.itemView.setOnClickListener {
            adapterClickListener.onItemClick(
                holder.itemView,
                position,
                model
            )
        }
    }

    override fun getItemCount(): Int {
        return mlist.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var locationTitle: TextView
        var locationAddress: TextView

        init {
            locationTitle = itemView.findViewById(R.id.locationName)
            locationAddress = itemView.findViewById(R.id.locationAddressText)
        }
    }
}
