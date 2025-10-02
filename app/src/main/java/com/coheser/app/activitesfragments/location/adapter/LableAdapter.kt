package com.coheser.app.activitesfragments.location.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.location.DeliveryAddress
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions

class LableAdapter(
    var context: Context,
    mlist: MutableList<DeliveryAddress>,
    adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<LableAdapter.MyViewHolder>() {
    var mlist : MutableList<DeliveryAddress>
    var adapterClickListener: AdapterClickListener

    init {
        this.mlist = mlist
        this.adapterClickListener = adapterClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.label_items, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = mlist[position]

        if (Functions.isStringHasValue(model?.label!!)){
            holder.labelTitle.text = model?.label
        }else{
            val title = model?.location_string?.substringBefore(",")
            holder.labelTitle.text = title
        }
        holder.locationAddress.text = model?.location_string

        Log.d(Constants.tag,"label list size in adapter : ${mlist.size}")


        if (model?.label?.toLowerCase().equals("work")){
            holder.imageLoc.setBackgroundResource(R.drawable.ic_work)
        }else if (model?.label?.toLowerCase().equals("home")){
            holder.imageLoc.setBackgroundResource(R.drawable.ic_home1)
        }else{
            holder.imageLoc.setBackgroundResource(R.drawable.ic_location_new)
        }

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
        var labelTitle: TextView
        var locationAddress: TextView
        var imageLoc : ImageView

        init {
            labelTitle = itemView.findViewById(R.id.labelTitle)
            locationAddress = itemView.findViewById(R.id.address)
            imageLoc = itemView.findViewById(R.id.iconLoc)
        }
    }
}
