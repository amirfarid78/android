package com.coheser.app.activitesfragments.shoping.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.models.CategoryModel
import com.coheser.app.databinding.ItemCategoryListBinding
import com.coheser.app.interfaces.AdapterClickListener
import java.util.Locale

class ProductCategoryAdapter(
    var context: Context,
    datalist: ArrayList<CategoryModel>,
    listener: AdapterClickListener
) : RecyclerView.Adapter<ProductCategoryAdapter.CustomViewHolder>(), Filterable {
    var dataList = ArrayList<CategoryModel>()
    var tempList = ArrayList<CategoryModel>()
    var adapterClicklistener: AdapterClickListener

    init {
        dataList = datalist
        tempList = datalist
        adapterClicklistener = listener
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewtype: Int
    ): CustomViewHolder {
        val binding:ItemCategoryListBinding = DataBindingUtil.inflate(
            LayoutInflater.from(viewGroup.getContext()),
            R.layout.item_category_list,
            viewGroup,
            false
        )
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = tempList[position]
        holder.binding.title.setText(item.title)
        holder.bind(position, item, adapterClicklistener)
    }

    override fun getItemCount(): Int {
        return tempList.size
    }

    class CustomViewHolder(binding: ItemCategoryListBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        var binding: ItemCategoryListBinding

        init {
            this.binding = binding
        }

        fun bind(pos: Int, model: Any?, listener: AdapterClickListener) {
            binding.mainlayout.setOnClickListener(View.OnClickListener { v ->
                listener.onItemClick(
                    v,
                    pos,
                    model
                )
            })
        }

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    tempList.clear()
                    tempList.addAll(dataList)
                } else {
                    val filteredList: ArrayList<CategoryModel> = ArrayList()
                    filteredList.clear()
                    for (row in dataList) {
                        if (row.title!!.toLowerCase()
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) filteredList.add(row)
                    }
                    tempList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = tempList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                tempList = filterResults.values as ArrayList<CategoryModel>
                notifyDataSetChanged()
            }
        }
    }


}