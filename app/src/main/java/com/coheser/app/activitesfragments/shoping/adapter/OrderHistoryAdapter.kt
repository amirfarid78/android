package com.coheser.app.activitesfragments.shoping.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.models.OrderHistoryModel
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad

class OrderHistoryAdapter(
    var context: Context,
    datalist: ArrayList<OrderHistoryModel>,
    listener: AdapterClickListener
) : RecyclerView.Adapter<OrderHistoryAdapter.CustomViewHolder>() {
    var dataList: ArrayList<OrderHistoryModel> = ArrayList()
    var adapterClicklistener: AdapterClickListener

    init {
        this.dataList = datalist
        this.adapterClicklistener = listener
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.item_order_history_layout, null)
        view.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
        val viewHolder: CustomViewHolder = CustomViewHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = dataList[position]

        holder.orderImg.controller = frescoImageLoad(
            item.product.productImage!!.first()!!.image,
            R.drawable.image_placeholder,
            holder.orderImg,
            false
        )

        holder.orderid.text = item.order.id
        holder.orderDateTxt.text = item.order.created

        holder.priceTxt.text =
            Constants.productShowingCurrency + item.product.price
        holder.orderDetailTitle.text = item.product.title
        holder.orderDetailQuantity.text = "${(item.order.total!!.toDouble() / item.product.price!!.toDouble()).toInt()}"
        holder.totalPriceTv.text = Constants.productShowingCurrency + item.order.total

        holder.bind(position, item, adapterClicklistener)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var orderid: TextView = view.findViewById(R.id.order_id_txt)
        var orderDateTxt: TextView = view.findViewById(R.id.order_date_txt)
        var orderDetailTitle: TextView = view.findViewById(R.id.order_detail_title)
        var priceTxt: TextView = view.findViewById(R.id.order_detail_price)
        var orderDetailQuantity: TextView = view.findViewById(R.id.order_detail_quantity)
        var totalPriceTv: TextView = view.findViewById(R.id.total_price_tv)
        var orderDetailLayout: RelativeLayout = view.findViewById(R.id.order_detail_main_d)
        var orderImg: SimpleDraweeView = view.findViewById(R.id.order_image)

        fun bind(pos: Int, item: OrderHistoryModel?, onClickListner: AdapterClickListener) {
            orderDetailLayout.setOnClickListener { view: View? ->
                onClickListner.onItemClick(
                    view,
                    pos,
                    item
                )
            }
        }
    }
}