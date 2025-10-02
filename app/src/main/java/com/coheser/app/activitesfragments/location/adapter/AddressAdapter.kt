package com.coheser.app.activitesfragments.location.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.location.DeliveryAddress
import com.coheser.app.databinding.AddressItemBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables

class AddressAdapter(
    var context: Context,
    var from: String,
    val mlist: MutableList<DeliveryAddress>,
    val adapterClickListener: AdapterClickListener
) : RecyclerView.Adapter<AddressAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding: AddressItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.address_item, parent, false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = mlist[position]

        if (from.equals("address")) {
            if (model?.id.equals(
                    Functions.getSettingsPreference(context).getString(Variables.selectedId, "")
                )
            ) {
                holder.binding.mainLay.setBackgroundColor(context.resources.getColor(R.color.appColor_15))
                holder.binding.locIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.appColor),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )



            } else {
                holder.binding.mainLay.setBackgroundColor(context.resources.getColor(R.color.white))
                holder.binding.locIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.darkgray),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )


            }
            holder.binding.editBtn.visibility = View.VISIBLE
        } else {
            holder.binding.mainLay.setBackgroundColor(context.resources.getColor(R.color.white))
            holder.binding.editBtn.visibility = View.GONE
        }


        holder.binding.editBtn.setOnClickListener {
            adapterClickListener.onItemClick(
                holder.binding.editBtn,
                position,
                model
            )
        }
        holder.binding.dataLay.setOnClickListener {
            adapterClickListener.onItemClick(
                holder.binding.dataLay,
                position,
                model
            )


        }


        holder.binding.locationName.text = Functions.getAddressLable(model)
        holder.binding.locationAddressText.text = Functions.getAddressString(model)


        if (model?.label?.toLowerCase().equals("work")) {
            holder.binding.locIcon.setBackgroundResource(R.drawable.ic_work)
        } else if (model?.label?.toLowerCase().equals("home")) {
            holder.binding.locIcon.setBackgroundResource(R.drawable.ic_home1)
        } else {
            holder.binding.locIcon.setBackgroundResource(R.drawable.ic_location_new)
        }
        Log.d(Constants.tag," title:${model?.city}  state:${model?.state} postalCode : ${model?.zip}")

    }

    override fun getItemCount(): Int {
        return mlist.size
    }

    inner class MyViewHolder(val binding: AddressItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    private fun moveItemToTop(position: Int) {
        val selectedItem = mlist[position]

        // Remove the item and notify the adapter about the item removal
        mlist.removeAt(position)
        notifyItemRemoved(position)

        // Add the item to the top of the list and notify the adapter about the item insertion
        mlist.add(0, selectedItem)
        notifyItemInserted(0)

        // Scroll to the top to show the newly added item
        (context as? Activity)?.findViewById<RecyclerView>(R.id.recyclerView)
            ?.smoothScrollToPosition(0)
    }
}
