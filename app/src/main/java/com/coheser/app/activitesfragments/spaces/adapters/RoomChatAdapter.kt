package com.coheser.app.activitesfragments.spaces.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.activitesfragments.chat.ChatModel
import com.coheser.app.databinding.ItemRoomChatBinding
import com.coheser.app.interfaces.AdapterClickListener2
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.parseInterger
import com.coheser.app.simpleclasses.Variables
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RoomChatAdapter(
    private val mDataSet: List<ChatModel>,
    var myID: String,
    var adapterClickListener: AdapterClickListener2
) : RecyclerView.Adapter<RoomChatAdapter.ViewHolder>() {
    var todayDay: Int = 0

    init {
        val cal = Calendar.getInstance()
        todayDay = cal[Calendar.DAY_OF_MONTH]
    }


    // this is the all types of view that is used in the chat
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): ViewHolder {
        val binding =
            ItemRoomChatBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = mDataSet[position]

        if (chat.type == "text") {
            holder.binding.tvFullname.text = chat.sender_name
            holder.binding.ivProfile.controller = frescoImageLoad(
                holder.binding.root.context,
                chat.sender_name, chat.pic_url, holder.binding.ivProfile
            )
            holder.binding.tvmessage.text = chat.text
            holder.binding.datetxt.text = changeDate(chat.timestamp)


            holder.bind(position, adapterClickListener, chat)
        }
    }


    override fun getItemViewType(position: Int): Int {
        return 0
    }


    inner class ViewHolder(var binding: ItemRoomChatBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(position: Int, listener: AdapterClickListener2, `object`: Any?) {
            binding.mainTab.setOnClickListener { v: View? ->
                listener.onItemClick(
                    v,
                    position,
                    `object`
                )
            }
        }
    }


    // change the date into (today ,yesterday and date)
    private fun changeDate(date: String): String {
        try {
            val currenttime = System.currentTimeMillis()
            var databasedate: Long = 0
            var d: Date? = null
            try {
                d = Variables.df.parse(date)
                databasedate = d.time
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val difference = currenttime - databasedate
            if (difference < 86400000) {
                val chatday = parseInterger(date.substring(0, 2))
                val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                if (todayDay == chatday) return sdf.format(d)
                else if ((todayDay - chatday) == 1) return "Yesterday " + sdf.format(d)
            } else if (difference < 172800000) {
                val chatday = parseInterger(date.substring(0, 2))
                val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                if ((todayDay - chatday) == 1) return "Yesterday " + sdf.format(d)
            }

            val sdf = SimpleDateFormat("MMM-dd-yyyy hh:mm a", Locale.ENGLISH)
            return sdf.format(d)
        } catch (e: Exception) {
            return date
        }
    }
}
