package com.coheser.app.activitesfragments.livestreaming.adapter

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.model.ContactsDataModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Variables
import com.facebook.drawee.view.SimpleDraweeView

class Contacts_Adapter(
    var context: Context,
    user_dataList: ArrayList<ContactsDataModel>,
    listener: OnItemClickListener
) : RecyclerView.Adapter<Contacts_Adapter.CustomViewHolder>() {
    var user_dataList: ArrayList<ContactsDataModel> = ArrayList()
    private val listener: OnItemClickListener

    init {
        this.user_dataList = user_dataList
        this.listener = listener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.item_contacts, viewGroup, false)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return user_dataList.size
    }

    fun filter(filter_list: ArrayList<ContactsDataModel>) {
        this.user_dataList = filter_list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = user_dataList[i]
        holder.bind(item, i, listener)
        if (item.isexits!!) {
            holder.contactimage.setImageDrawable(
                ContextCompat.getDrawable(
                    context, R.drawable.ic_circle_primary
                )
            )
        } else {
            holder.contactimage.setImageDrawable(
                ContextCompat.getDrawable(
                    context, R.drawable.ic_un_selected
                )
            )
        }
        if (item.picture!!.contains(Variables.http) || item.picture!!.contains(".png")) {
            holder.user_image.controller = frescoImageLoad(
                item.picture,
                holder.user_image,
                false
            )
            holder.name_single_letter.visibility = View.GONE
        } else {
            holder.user_image.controller = frescoImageLoad(
                ColorDrawable(item.imagecolor),
                holder.user_image,
                false
            )
            holder.name_single_letter.text = item.username!!.substring(0, 1)
            holder.name_single_letter.visibility = View.VISIBLE
        }
        holder.email.text = item.firstName + " " + item.lastName
        holder.user_name.text = item.username

        if (item.verified!!.equals("1")) {
            holder.ivVarified.visibility = View.VISIBLE
        } else {
            holder.ivVarified.visibility = View.GONE
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: ContactsDataModel, position: Int)
    }

    inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var user_name: TextView = view.findViewById(R.id.username)
        var name_single_letter: TextView = view.findViewById(R.id.name_single_letter)
        var email: TextView = view.findViewById(R.id.email)
        var contactimage: ImageView = view.findViewById(R.id.contactimage)
        var ivVarified: ImageView = view.findViewById(R.id.ivVarified)
        var user_image: SimpleDraweeView = view.findViewById(R.id.userimage)

        fun bind(item: ContactsDataModel, postion: Int, listener: OnItemClickListener) {
            itemView.setOnClickListener { listener.onItemClick(item, postion) }
        }
    }
}