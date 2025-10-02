package com.coheser.app.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.example.WithDrawalModel
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.analytics.DateOperations
import com.coheser.app.databinding.ItemWithdrawalsHistoryBinding
import com.coheser.app.interfaces.AdapterClickListener

class WidthDrawHistoryAdapter(
    var context: Context,
    datalist: MutableList<WithDrawalModel>,
    listener: AdapterClickListener
) : RecyclerView.Adapter<WidthDrawHistoryAdapter.CustomViewHolder>() {
    var dataList : MutableList<WithDrawalModel>
    var adapterClicklistener: AdapterClickListener

    init {
        dataList = datalist
        adapterClicklistener = listener
    }


    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewtype: Int
    ): CustomViewHolder {
        val binding: ItemWithdrawalsHistoryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(viewGroup.context),
            R.layout.item_withdrawals_history,
            viewGroup,
            false
        )
        return CustomViewHolder(binding)

    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = dataList[position]

        Log.d(Constants.tag,"size in list : ${dataList.size}")
        holder.binding.orderDateTxt.text = DateOperations.changeDateFormat(
            "yyyy-MM-dd hh:mm:ss",
            "MMM dd, yyyy HH:mm a",
            item.WithdrawRequest!!.created!!
        )

        if (item.WithdrawRequest!!.status == 0) {

            holder.binding.statusTxt.text = "Pending"
        } else if (item.WithdrawRequest!!.status == 1) {
            holder.binding.statusTxt.text = "Completed"
        } else if (item.WithdrawRequest!!.status == 2) {
            holder.binding.statusTxt.text = "Declined"
        }


        holder.binding.priceTxt.text = Constants.CURRENCY + item.WithdrawRequest!!.amount!!
        holder.bind(position, item, adapterClicklistener)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class CustomViewHolder(val binding: ItemWithdrawalsHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pos: Int, item: WithDrawalModel, listener: AdapterClickListener) {

        }

    }

}