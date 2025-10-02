package com.coheser.app.activitesfragments.sendgift

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.databinding.ImageSliderLayoutItemBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.smarteist.autoimageslider.SliderViewAdapter

class SendGiftVHAdapter(list: List<MutableList<GiftModel>>, from:String, callBack: FragmentCallBack) :
    SliderViewAdapter<SendGiftVHAdapter.SliderAdapterVH>() {
     var from:String
    var callBack: FragmentCallBack
    private var list: List<MutableList<GiftModel>> = ArrayList()

    init {
        this.from=from
        this.list = list
        this.callBack = callBack
    }

    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        val binding = ImageSliderLayoutItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
       return SliderAdapterVH(binding)
    }

    override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int) {
        val data_list = list[position]

        val layoutManager = GridLayoutManager(viewHolder.itemView.context, 4)
        layoutManager.orientation = RecyclerView.VERTICAL
        viewHolder.binding.recylerview.layoutManager = layoutManager
        viewHolder.adapter = StickerAdapter(viewHolder.itemView.context,from, data_list,
            object : StickerAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, view: View?, item: GiftModel?) {
                    if (view!!.id == R.id.sendBtn) {
                        val bundle = Bundle()
                        bundle.putBoolean("isShow", true)
                        bundle.putBoolean("isSend", true)
                        bundle.putParcelable("Data", item)
                        callBack.onResponce(bundle)
                    } else {
                        for (i in data_list.indices) {
                            val model = data_list[i]
                            if (model.id === item!!.id) {
                                if (model.isSelected) {
                                    model.isSelected = false
                                    model.count = 0
                                    data_list[i] = model
                                } else {
                                    model.isSelected = true
                                    model.count = 1
                                    data_list[i] = model

                                    val bundle = Bundle()
                                    bundle.putBoolean("isShow", true)
                                    bundle.putParcelable("Data", model)
                                    callBack.onResponce(bundle)
                                }
                            } else {
                                model.isSelected = false
                                model.count = 0
                                data_list[i] = model
                            }
                            viewHolder.adapter!!.notifyDataSetChanged()
                        }
                    }
                }
            })
        viewHolder.binding.recylerview.adapter = viewHolder.adapter
    }


    override fun getCount(): Int {
        //slider view count could be dynamic size
        return list.size
    }


    inner class SliderAdapterVH(val binding: ImageSliderLayoutItemBinding) : ViewHolder(binding.root) {
        var adapter: StickerAdapter? = null
    }
}
