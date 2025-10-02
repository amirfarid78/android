package com.coheser.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.databinding.DurationTempBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.TimerDuration

class DurationTimeAdapter(private val durations: List<TimerDuration>,val listner : AdapterClickListener) :
    RecyclerView.Adapter<DurationTimeAdapter.MyViewHolder>() {

    private var selectedPosition = 0 // Track selected position
    class MyViewHolder(val binding: DurationTempBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DurationTimeAdapter.MyViewHolder {
        val binding = DurationTempBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DurationTimeAdapter.MyViewHolder, position: Int) {
        val model = durations[position]
        holder.binding.durationTxt.text = model.title

        holder.binding.durationTxt.setTextColor(
            if (position == selectedPosition) {
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            } else {
                ContextCompat.getColor(holder.itemView.context, R.color.gray)
            }
        )

        holder.itemView.setOnClickListener {
            // Update selected position and refresh UI
            val previousSelected = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)
            listner.onItemClick(it, holder.absoluteAdapterPosition, model)
            (holder.itemView.parent as? RecyclerView)?.smoothScrollToPosition(holder.absoluteAdapterPosition)
            listner.onItemClick(it,holder.absoluteAdapterPosition,model)
        }
    }

    override fun getItemCount(): Int {
        return durations.size
    }
    // New method to update selectedPosition from activity
    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }
}