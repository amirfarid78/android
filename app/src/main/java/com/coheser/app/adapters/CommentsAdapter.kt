package com.coheser.app.adapters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hendraanggrian.appcompat.widget.SocialView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.databinding.ItemCommentLayoutBinding
import com.coheser.app.databinding.ItemCommentReplyLayoutBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.CommentModel
import com.coheser.app.simpleclasses.DateOprations.changeDateLatterFormat
import com.coheser.app.simpleclasses.FriendsTagHelper
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getSuffix

class CommentsAdapter // meker the onitemclick listener interface and this interface is impliment in Chatinbox activity
// for to do action when user click on item
    (
    var context: Context,
    private val dataList: ArrayList<CommentModel?>,
    var listener: OnItemClickListener,
    var replyItemClickListener: onRelyItemCLickListener,
    var linkClickListener: LinkClickListener,
    var callBack: FragmentCallBack
) : RecyclerView.Adapter<CommentsAdapter.CustomViewHolder>() {
    var commentsReplyAdapter: Comments_Reply_Adapter? = null
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemCommentLayoutBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val item = dataList[i]
        holder.binding.username.text = item!!.user_name
        holder.binding.userPic.controller = frescoImageLoad(item.profile_pic, holder.binding.userPic, false)
        if (item.isVerified.toString() == "1") {
            holder.binding.ivVarified.visibility = View.VISIBLE
        } else {
            holder.binding.ivVarified.visibility = View.GONE
        }
        holder.binding.likeTxt.text = getSuffix(item.like_count)
        val date = changeDateLatterFormat("yyyy-MM-dd hh:mm:ssZZ", context, item.created + "+0000")
        holder.binding.tvMessageData.text = date
        holder.binding.message.text = item.comments
        FriendsTagHelper.Creator.create(
            ContextCompat.getColor(
                holder.itemView.context,
                R.color.whiteColor
            ), ContextCompat.getColor(holder.itemView.context, R.color.appColor)
        ) { friendsTag ->
            var friendsTag = friendsTag
            if (friendsTag.contains("@")) {
                Log.d(Constants.tag, "Friends $friendsTag")
                if (friendsTag[0] == '@') {
                    friendsTag = friendsTag.substring(1)
                    openUserProfile(friendsTag)
                }
            }
        }.handle(holder.binding.message)
        holder.binding.message.scrollTo(0, 0)
        if (item.isExpand) {
            holder.binding.lessLayout.visibility = View.VISIBLE
            holder.binding.replyCount.visibility = View.GONE
        } else {
            holder.binding.lessLayout.visibility = View.GONE
            holder.binding.replyCount.visibility = View.VISIBLE
        }
        if (item.arrayList != null && item.arrayList.size > 0 && !item.isExpand) {
            holder.binding.replyCount.visibility = View.VISIBLE
            holder.binding.replyCount.text =
                context.getString(R.string.view_replies) + " (" + item.arrayList.size + ")"
        } else {
            holder.binding.replyCount.visibility = View.GONE
        }
        if (item.userId == item.videoOwnerId) {
            holder.binding.tabCreator.visibility = View.VISIBLE
        } else {
            holder.binding.tabCreator.visibility = View.GONE
        }
        if (item.pin_comment_id == "1") {
            holder.binding.tabPinned.visibility = View.VISIBLE
        } else {
            holder.binding.tabPinned.visibility = View.GONE
        }
        if (item.isLikedByOwner == "1") {
            holder.binding.tabLikedByCreator.visibility = View.VISIBLE
        } else {
            holder.binding.tabLikedByCreator.visibility = View.GONE
        }
        commentsReplyAdapter = Comments_Reply_Adapter(context, item.arrayList)
        holder.binding.replyRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        holder.binding.replyRecyclerView.adapter = commentsReplyAdapter
        holder.binding.replyRecyclerView.setHasFixedSize(false)
        holder.bind(i, item, listener)
    }

    private fun openUserProfile(friendsTag: String) {
        val bundle = Bundle()
        bundle.putBoolean("isShow", true)
        bundle.putString("name", friendsTag)
        callBack.onResponce(bundle)
    }

    interface LinkClickListener {
        fun onLinkClicked(view: SocialView?, matchedText: String?)
    }

    interface OnItemClickListener {
        fun onItemClick(positon: Int, item: CommentModel?, view: View?)
        fun onItemLongPress(positon: Int, item: CommentModel?, view: View?)
    }

    interface onRelyItemCLickListener {
        fun onItemClick(arrayList: ArrayList<CommentModel>?, postion: Int, view: View?)
        fun onItemLongPress(arrayList: ArrayList<CommentModel>?, postion: Int, view: View?)
    }

    inner class CustomViewHolder(val binding: ItemCommentLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(postion: Int, item: CommentModel?, listener: OnItemClickListener) {
            itemView.setOnClickListener { v: View? -> listener.onItemClick(postion, item, v) }
            binding.tabUserPic.setOnClickListener { view: View? ->
                listener.onItemClick(
                    postion,
                    item,
                    view
                )
            }
            binding.userPic.setOnClickListener { v: View? -> listener.onItemClick(postion, item, v) }
            binding.username.setOnClickListener { v: View? -> listener.onItemClick(postion, item, v) }
            binding.messageLayout.setOnLongClickListener { view ->
                listener.onItemLongPress(postion, item, view)
                false
            }
            binding.likeLayout.setOnClickListener { v: View? -> listener.onItemClick(postion, item, v) }
            binding.tabMessageReply.setOnClickListener { v: View? ->
                listener.onItemClick(
                    postion,
                    item,
                    v
                )
            }
            binding.replyCount.setOnClickListener { v: View? -> listener.onItemClick(postion, item, v) }
            binding.showLessTxt.setOnClickListener { v: View? -> listener.onItemClick(postion, item, v) }
        }
    }

    inner class Comments_Reply_Adapter(
        var context: Context,
        private val dataList: ArrayList<CommentModel>
    ) : RecyclerView.Adapter<Comments_Reply_Adapter.CustomViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CustomViewHolder {
            val bindingReply = ItemCommentReplyLayoutBinding.inflate(
                LayoutInflater.from(viewGroup.context), viewGroup, false
            )
            return CustomViewHolder(bindingReply)
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
            val item = dataList[i]
            holder.bindingReply.username.text = item.replay_user_name
            holder.bindingReply.userPic.controller =
                frescoImageLoad(item.replay_user_url, holder.bindingReply.userPic, false)
            holder.bindingReply.message.text = item.comment_reply
            val date =
                changeDateLatterFormat("yyyy-MM-dd hh:mm:ssZZ", context, item.created + "+0000")
            holder.bindingReply.tvMessageData.text = date
            if (item.userId == item.videoOwnerId) {
                holder.bindingReply.tabCreator.visibility = View.VISIBLE
            } else {
                holder.bindingReply.tabCreator.visibility = View.GONE
            }
            if (item.isLikedByOwner == "1") {
                holder.bindingReply.tabLikedByCreator.visibility = View.VISIBLE
            } else {
                holder.bindingReply.tabLikedByCreator.visibility = View.GONE
            }
            if (item.isVerified.toString() == "1") {
                holder.bindingReply.ivVarified.visibility = View.VISIBLE
            } else {
                holder.bindingReply.ivVarified.visibility = View.GONE
            }
            holder.bindingReply.likeTxt.text = getSuffix(item.reply_liked_count)
            holder.bindingReply.message.setOnMentionClickListener { view, text ->
                linkClickListener.onLinkClicked(
                    view,
                    text.toString()
                )
            }
            holder.bind(i, dataList, replyItemClickListener)
        }

        inner class CustomViewHolder(val bindingReply: ItemCommentReplyLayoutBinding) : RecyclerView.ViewHolder(bindingReply.root) {

            fun bind(
                postion: Int,
                datalist: ArrayList<CommentModel>?,
                listener: onRelyItemCLickListener?
            ) {
                itemView.setOnClickListener { v: View? ->
                    replyItemClickListener.onItemClick(
                        datalist,
                        postion,
                        v
                    )
                }
                bindingReply.userPic.setOnClickListener { v: View? ->
                    replyItemClickListener.onItemClick(
                        datalist,
                        postion,
                        v
                    )
                }
                bindingReply.username.setOnClickListener { v: View? ->
                    replyItemClickListener.onItemClick(
                        datalist,
                        postion,
                        v
                    )
                }
                bindingReply.tabMessageReply.setOnClickListener { v: View? ->
                    replyItemClickListener.onItemClick(
                        datalist,
                        postion,
                        v
                    )
                }
                bindingReply.likeLayout.setOnClickListener { v: View? ->
                    replyItemClickListener.onItemClick(
                        datalist,
                        postion,
                        v
                    )
                }
                bindingReply.replyLayout.setOnLongClickListener { view ->
                    replyItemClickListener.onItemLongPress(datalist, postion, view)
                    false
                }
            }
        }
    }
}