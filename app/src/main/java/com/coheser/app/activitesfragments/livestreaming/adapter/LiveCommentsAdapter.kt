package com.coheser.app.activitesfragments.livestreaming.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.model.LiveCommentModel
import com.coheser.app.databinding.ItemLiveGiftLayoutBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.facebook.drawee.view.SimpleDraweeView
import java.util.Locale

class LiveCommentsAdapter(
    var context: Context,
    private val dataList: ArrayList<LiveCommentModel>,
    private val listener: AdapterClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewtype: Int): RecyclerView.ViewHolder {
        val view: View
        if (viewtype == PRIMARY_ALERT) {
            view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_live_primary_alert_layout, viewGroup, false)
            return AlertViewHolder(view)
        }
        if (viewtype == LIKE_STREAM) {
            view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_live_like_layout, viewGroup, false)
            return LikeViewHolder(view)
        } else if (viewtype == GIFT_STREAM) {
            val inflater = LayoutInflater.from(viewGroup.context)
            val binding = ItemLiveGiftLayoutBinding.inflate(inflater, viewGroup, false)
            return GiftViewHolder(binding)
        } else if (viewtype == SHARE_STREAM) {
            view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_share_live_stream_layout, viewGroup, false)
            return ShareStreamViewHolder(view)
        } else if (viewtype == SELF_INVITE_FOR_STREAM) {
            view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_self_join_stream_request_layout, viewGroup, false)
            return SelfInvitationViewHolder(view)
        } else if (viewtype == JOINED_STREAMING) {
            view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_live_join_comment_layout, viewGroup, false)
            return JoinedStreamingHolder(view)
        } else {
            view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_live_comment_layout, viewGroup, false)
            return CommentViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position].type.equals("alert", ignoreCase = true)) {
            PRIMARY_ALERT
        } else if (dataList[position].type.equals("like", ignoreCase = true)) {
            LIKE_STREAM
        } else if (dataList[position].type.equals("gift", ignoreCase = true)) {
            GIFT_STREAM
        } else if (dataList[position].type.equals("shareStream", ignoreCase = true)) {
            SHARE_STREAM
        } else if (dataList[position].type.equals("joined", ignoreCase = true)) {
            JOINED_STREAMING
        } else if (dataList[position].type.equals("selfInviteForStream", ignoreCase = true)) {
            SELF_INVITE_FOR_STREAM
        } else {
            COMMENT_STREAM
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
        val item = dataList[i]

        if (holder is CommentViewHolder) {
            val holderItem = holder
            holderItem.username.text = item.userName

            holderItem.message.text = item.comment
            holderItem.userPic.controller = frescoImageLoad(
                item.userPicture,
                holderItem.userPic,
                false
            )

            holderItem.bind(i, item, listener)
        } else if (holder is JoinedStreamingHolder) {
            val holderItem = holder
            holderItem.username.text = item.userName
            holderItem.userPic.controller = frescoImageLoad(
                item.userPicture,
                holderItem.userPic,
                false
            )

            holderItem.bind(i, item, listener)
        } else if (holder is LikeViewHolder) {
            val holderItem = holder
            holderItem.tvTitle.text = item.comment

            holderItem.bind(i, item, listener)
        } else if (holder is GiftViewHolder) {
            val holderItem = holder
            holderItem.binding.profileImage.controller = frescoImageLoad(
                item.userPicture + "",
                holderItem.binding.profileImage,
                false
            )
            holderItem.binding.username.text = item.userName
            holderItem.binding.message.text = (context.getString(R.string.send).lowercase(Locale.getDefault()))+ " " + item.giftName
            holderItem.binding.giftCount.text = item.giftCount
            holderItem.binding.ivGift.controller = frescoImageLoad(
                item.giftPic + "",
                holderItem.binding.ivGift,
                false
            )
            holderItem.bind(i, item, listener)
        } else if (holder is ShareStreamViewHolder) {
            holder.bind(i, item, listener)
        } else if (holder is SelfInvitationViewHolder) {
            val holderItem = holder
            holderItem.tvName.text =
                item.userName + " " + holderItem.itemView.context.getString(R.string.want_to_join_live_stream)
            holderItem.ivProfile.controller = frescoImageLoad(
                item.userPicture,
                holderItem.ivProfile,
                false
            )
            holderItem.bind(i, item, listener)
        } else if (holder is AlertViewHolder) {
            val holderItem = holder
            holderItem.tvTitle.text = item.comment

            holderItem.bind(i, item, listener)
        }
    }


    interface OnItemClickListener {
        fun onItemClick(positon: Int, item: Any?, view: View?)
    }

    private inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var username: TextView = view.findViewById(R.id.username)
        var message: TextView = view.findViewById(R.id.message)
        var userPic: SimpleDraweeView = view.findViewById(R.id.profileImage)

        fun bind(postion: Int, item: LiveCommentModel?, listener: AdapterClickListener) {
            itemView.setOnClickListener { v: View? ->
                listener.onItemClick(v, postion, item)
            }

            userPic.setOnClickListener { view -> listener.onItemClick(view, postion, item) }

            username.setOnClickListener { view -> listener.onItemClick(view, postion, item) }
        }
    }

    private inner class LikeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle: TextView = view.findViewById(R.id.tvTitle)

        fun bind(postion: Int, item: LiveCommentModel, listener: AdapterClickListener) {
            itemView.setOnClickListener { v: View? ->
                listener.onItemClick(v, postion, item)
                Log.d(Constants.tag, "click" + item.type)
            }
        }
    }

    private inner class GiftViewHolder(val binding: ItemLiveGiftLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(postion: Int, item: LiveCommentModel?, listener: AdapterClickListener) {
            itemView.setOnClickListener { v: View? ->
                listener.onItemClick(v, postion, item)
            }
        }
    }


    private inner class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle: TextView = view.findViewById(R.id.tvTitle)

        fun bind(postion: Int, item: LiveCommentModel?, listener: AdapterClickListener) {
            itemView.setOnClickListener { v: View? ->
                listener.onItemClick(v, postion, item)
            }
        }
    }

    private inner class ShareStreamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tabShareStream: View = view.findViewById(R.id.tabShareStream)

        fun bind(postion: Int, item: LiveCommentModel?, listener: AdapterClickListener) {
            tabShareStream.setOnClickListener { v: View? ->
                listener.onItemClick(v, postion, item)
            }
        }
    }


    private inner class SelfInvitationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tabAcceptInvitation: View = view.findViewById(R.id.tabAcceptInvitation)
        var tvName: TextView = view.findViewById(R.id.tvName)
        var ivProfile: SimpleDraweeView = view.findViewById(R.id.profileImage)

        fun bind(postion: Int, item: LiveCommentModel?, listener: AdapterClickListener) {
            tabAcceptInvitation.setOnClickListener { v: View? ->
                listener.onItemClick(v, postion, item)
            }

            ivProfile.setOnClickListener { view -> listener.onItemClick(view, postion, item) }
        }
    }


    private inner class JoinedStreamingHolder(view: View) : RecyclerView.ViewHolder(view) {
        var username: TextView = view.findViewById(R.id.username)
        var userPic: SimpleDraweeView = view.findViewById(R.id.profileImage)

        fun bind(postion: Int, item: LiveCommentModel, listener: AdapterClickListener) {
            itemView.setOnClickListener { v: View? ->
                Log.d(Constants.tag, "click" + item.type)
                listener.onItemClick(v, postion, item)
            }

            userPic.setOnClickListener { view -> listener.onItemClick(view, postion, item) }

            username.setOnClickListener { view -> listener.onItemClick(view, postion, item) }
        }
    }


    companion object {
        //comment types
        private const val PRIMARY_ALERT = 1
        private const val LIKE_STREAM = 2
        private const val COMMENT_STREAM = 3
        private const val GIFT_STREAM = 4
        private const val SHARE_STREAM = 5
        private const val SELF_INVITE_FOR_STREAM = 6
        private const val JOINED_STREAMING = 7
    }
}