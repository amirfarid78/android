package com.coheser.app.activitesfragments.livestreaming.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.databinding.ItemInvitePkBattleBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.imagepipeline.request.ImageRequestBuilder

class PkBattleAdapter(
    var context: Context,
    user_dataList: ArrayList<LiveUserModel>,
    listener: AdapterClickListener
) : RecyclerView.Adapter<PkBattleAdapter.CustomViewHolder>() {
    var user_dataList: ArrayList<LiveUserModel> = ArrayList()

    var adapterClickListener: AdapterClickListener

    init {
        this.user_dataList = user_dataList
        this.adapterClickListener = listener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val binding = DataBindingUtil.inflate<ItemInvitePkBattleBinding>(
            LayoutInflater
                .from(viewGroup.context), R.layout.item_invite_pk_battle, viewGroup, false
        )
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return user_dataList.size
    }

    fun filter(filter_list: ArrayList<LiveUserModel>) {
        this.user_dataList = filter_list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = user_dataList[i]
        holder.bind(i, adapterClickListener, item)
        holder.binding.userName.text = item.getUserName()
        holder.binding.userPhone.text = item.getDescription()

        if (item.getUserPicture() != null && item.getUserPicture() != "") {
            val request =
                ImageRequestBuilder.newBuilderWithSource(Uri.parse(item.getUserPicture())).build()
            val controller: DraweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(holder.binding.userImage.controller)
                .build()
            holder.binding.userImage.controller = controller
        } else {
            holder.binding.userImage.setImageResource(R.drawable.ic_user_icon)
        }
    }

    inner class CustomViewHolder(var binding: ItemInvitePkBattleBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(position: Int, listener: AdapterClickListener, `object`: Any?) {
            binding.actionTxt.setOnClickListener { v: View? ->
                listener.onItemClick(
                    v,
                    position,
                    `object`
                )
            }
        }
    }
}