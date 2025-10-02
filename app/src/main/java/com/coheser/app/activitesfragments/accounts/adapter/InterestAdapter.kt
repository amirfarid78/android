package com.coheser.app.activitesfragments.accounts.adapter

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.activitiesfragments.accounts.model.InterestModel
import com.coheser.app.databinding.InterestItemBinding
import com.google.android.material.chip.Chip

class InterestAdapter(
    var context: Context,
    var dataList: ArrayList<InterestModel>,
    var adapterClickListener: com.coheser.app.interfaces.AdapterClickListener3
) : RecyclerView.Adapter<InterestAdapter.MyViewHolder>() {
    var binding: InterestItemBinding? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InterestAdapter.MyViewHolder {
        binding = InterestItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding!!)
    }

    inner class MyViewHolder(var binding: InterestItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onBindViewHolder(holder: InterestAdapter.MyViewHolder, position: Int) {
        var model = dataList.get(position)
        var intrstList = model.userIntrest
        holder.binding.secTitle.text = model.secTitle

        // Clear any existing chips to avoid duplicates
        holder.binding.chipGroup.removeAllViews()

        // Dynamically create chips for each interest and add to the ChipGroup
        for ((index, interest) in intrstList.withIndex()) {
            val chip = Chip(
                ContextThemeWrapper(
                    holder.binding.chipGroup.context,
                    R.style.CustomChipTheme
                )
            ).apply {

                if (interest.selected.equals("1")) {

                    text = interest.title
                    isCloseIconVisible = false
                    isClickable = true
                    isCheckable = false
                    setTextColor(resources.getColor(R.color.white))
                    setChipBackgroundColorResource(R.color.appColor)
                    setChipStrokeColorResource(R.color.appColor)
                    chipStrokeWidth = 1f

                }
                else {

                    text = interest.title
                    isCloseIconVisible = false
                    isClickable = true
                    isCheckable = false
                    setChipBackgroundColorResource(android.R.color.white)
                    setChipStrokeColorResource(R.color.graycolor2)
                    chipStrokeWidth = 1f
                }


            }
            chip.setOnClickListener {
                adapterClickListener.onItemClick(it, position,index, interest)
            }


            holder.binding.chipGroup.addView(chip)
        }

    }

    override fun getItemCount(): Int {
        return dataList.size
    }


}