package com.coheser.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.ReportTypeModel
import com.coheser.app.simpleclasses.Functions.printLog

class ReportTypeAdapter(
    var context: Context,
    private val dataList: MutableList<ReportTypeModel>,
    private val listener: AdapterClickListener
) : RecyclerView.Adapter<ReportTypeAdapter.CustomViewHolder?>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): CustomViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_report_list, viewGroup, false)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = dataList[i]
        holder.setIsRecyclable(false)
        holder.bind(i, item, listener)
        holder.reportName.text = item.title
        printLog(Constants.tag, item.title)
    }


    inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var reportName: TextView
        var rltReport: RelativeLayout

        init {
            reportName = view.findViewById(R.id.report_name)
            rltReport = view.findViewById(R.id.rlt_report)
        }

        fun bind(postion: Int, item: ReportTypeModel?, listener: AdapterClickListener) {
            rltReport.setOnClickListener { v: View? -> listener.onItemClick(v,postion, item) }
        }
    }
}
