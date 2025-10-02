package com.coheser.app.activitesfragments.spaces.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.activitesfragments.spaces.models.RoomModel
import com.coheser.app.databinding.ItemRoomLayoutBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad

class MainHomeAdapter(
    var context: Context,
    var datalist: ArrayList<RoomModel>,
    var mainlistener: AdapterClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

                val binding = ItemRoomLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val mainHomeModel = holder
            val item = datalist[position]

            mainHomeModel.binding.roomNameTxt.text =
                item.userList!!.size.toString() + ". " + item.title

            mainHomeModel.binding.topicTxt.text = item.topicModels!![0].title!!


            if(item.userList!!.isNotEmpty()){
                mainHomeModel.binding.profileblurImage.controller =
                    frescoImageLoad(
                        mainHomeModel.binding.root.context,
                        item.userList!![0].userModel?.username!!,
                        item.userList!![0].userModel?.getProfilePic(),
                        mainHomeModel.binding.profileblurImage
                    )
            }

            mainHomeModel.binding.ivProfileOne.visibility = View.GONE
            mainHomeModel.binding.ivProfileSecond.visibility = View.GONE
            mainHomeModel.binding.ivProfilethird.visibility = View.GONE
            mainHomeModel.binding.ivProfileforth.visibility = View.GONE

            for ((index,model) in item.userList!!.withIndex()){
                if(index==0){
                    mainHomeModel.binding.ivProfileOne.visibility = View.VISIBLE
                    mainHomeModel.binding.ivProfileOne.controller =
                        frescoImageLoad(
                            mainHomeModel.binding.root.context,
                            model.userModel?.username!!,
                            model.userModel?.getProfilePic(),
                            mainHomeModel.binding.ivProfileOne
                        )
                }
                else if(index==1){
                    mainHomeModel.binding.ivProfileSecond.visibility = View.VISIBLE
                    mainHomeModel.binding.ivProfileSecond.controller =
                        frescoImageLoad(
                            mainHomeModel.binding.root.context,
                            model.userModel?.username!!,
                            model.userModel?.getProfilePic(),
                            mainHomeModel.binding.ivProfileSecond
                        )
                }
                else if(index==2){
                    mainHomeModel.binding.ivProfilethird.visibility = View.VISIBLE
                    mainHomeModel.binding.ivProfilethird.controller =
                        frescoImageLoad(
                            mainHomeModel.binding.root.context,
                            model.userModel?.username!!,
                            model.userModel?.getProfilePic(),
                            mainHomeModel.binding.ivProfilethird
                        )
                }
                else if(index==3){
                    mainHomeModel.binding.ivProfileforth.visibility = View.VISIBLE
                    mainHomeModel.binding.ivProfileforth.controller =
                        frescoImageLoad(
                            mainHomeModel.binding.root.context,
                            model.userModel?.username!!,
                            model.userModel?.getProfilePic(),
                            mainHomeModel.binding.ivProfilethird
                        )
                    break
                }
            }


            mainHomeModel.bind(position, mainlistener, item)
        }
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    override fun getItemViewType(position: Int): Int {
        if (datalist[position] is RoomModel) {
            return typeRoom
        }
        return typeRoom
    }

    inner class ViewHolder(var binding: ItemRoomLayoutBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(position: Int, listener: AdapterClickListener, `object`: Any?) {
            binding.tabView.setOnClickListener { v: View? ->
                listener.onItemClick(
                    v,
                    position,
                    `object`
                )
            }
        }
    }


    companion object {
        private const val typeRoom = 1
    }
}
