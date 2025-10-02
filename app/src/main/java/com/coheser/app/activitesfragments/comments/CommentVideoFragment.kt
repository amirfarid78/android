package com.coheser.app.activitesfragments.comments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.activitesfragments.sendgift.GiftHistoryModel
import com.coheser.app.activitesfragments.sendgift.StickerGiftFragment
import com.coheser.app.adapters.CommentsAdapter
import com.coheser.app.adapters.VideoGiftsAdapter
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentCommentBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.interfaces.FragmentDataSend
import com.coheser.app.models.CommentModel
import com.coheser.app.models.HomeModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.ApiRepository
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.Dialogs
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.hendraanggrian.appcompat.widget.SocialView
import com.coheser.app.activitesfragments.EditTextSheetFragment
import com.coheser.app.activitesfragments.livestreaming.fragments.AnimationViewF
import com.coheser.app.activitesfragments.sendgift.GiftModel
import com.coheser.app.simpleclasses.Downloading.DownloadFiles
import com.coheser.app.simpleclasses.FileUtils
import com.coheser.app.simpleclasses.TicTicApp.Companion.appLevelContext
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.APICallBack
import com.volley.plus.interfaces.Callback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

class CommentVideoFragment : BottomSheetDialogFragment {
    lateinit var myContext: Context
    var adapter: CommentsAdapter? = null
    var dataList = ArrayList<CommentModel?>()
    lateinit var item: HomeModel
    var videoUserId = ""
    lateinit var videoId: String
    lateinit var userId: String

    var isSendAllow = true
    var replyStatus: String? = null
    var selectedComment: CommentModel?=null
    var selectedCommentPosition = 0
    var selectedReplyComment: CommentModel? = null
    var selectedReplyCommentPosition = 0
    var pageCount = 0
    var ispostFinsh = false
    var linearLayoutManager: LinearLayoutManager? = null
    private var mBehavior: BottomSheetBehavior<*>? = null
    var dialog: BottomSheetDialog? = null

    constructor(count: Int, fragmentDataSend: FragmentDataSend?) {
        commentCount = count
        this.fragmentDataSend = fragmentDataSend
    }

    constructor()

    lateinit var binding: FragmentCommentBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(getContext(), R.layout.fragment_comment, null)
        dialog!!.setContentView(view)
        mBehavior = BottomSheetBehavior.from(view.parent as View)
        mBehavior!!.setHideable(false)
        mBehavior!!.setDraggable(false)
        mBehavior!!.setPeekHeight(view.context.resources.getDimension(R.dimen._450sdp).toInt(), true)
        mBehavior!!.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState != BottomSheetBehavior.STATE_EXPANDED) {
                    mBehavior!!.setState(BottomSheetBehavior.STATE_EXPANDED)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        return (dialog)!!
    }

    var fragmentDataSend: FragmentDataSend? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCommentBinding.inflate(layoutInflater, container, false)
        myContext = binding.root.context


        binding.writeLayout.setOnClickListener(DebounceClickHandler(object :
            View.OnClickListener {
            override fun onClick(view: View) {
                replyStatus = null
                hitComment()
            }
        }))

        binding.goBack.setOnClickListener(DebounceClickHandler({ v: View? -> dismiss() }))
        val bundle = arguments
        if (bundle != null) {
            videoId = bundle.getString("video_id").toString()
            userId = bundle.getString("user_id").toString()
            item = bundle.getParcelable<Parcelable>("data") as HomeModel
            videoUserId = item.video_user_id!!
            Log.d(com.coheser.app.Constants.tag,"videoOwnerID $videoUserId")
        }
        if (Functions.isShowContentPrivacy(
                myContext,
                item.apply_privacy_model!!.videoComment,
                item.userModel?.button.equals("friends", ignoreCase = true)
            )
        ) {
            binding.mentionBtn.visibility = View.VISIBLE
            isSendAllow = true
        } else {
            binding.mentionBtn.visibility = View.GONE
            isSendAllow = false
        }
        linearLayoutManager = LinearLayoutManager(myContext)
        linearLayoutManager!!.orientation = RecyclerView.VERTICAL
        binding.recylerview.layoutManager = linearLayoutManager
        binding.recylerview.setHasFixedSize(true)
        adapter = CommentsAdapter(
            myContext,
            dataList,
            object  :CommentsAdapter.OnItemClickListener{
                override fun onItemClick(positon: Int, item: CommentModel?, view: View?) {
                    selectedCommentPosition = positon
                    selectedComment = dataList[selectedCommentPosition]!!
                    when (view!!.id) {
                        R.id.tabUserPic, R.id.user_pic, R.id.username -> {
                            openProfile(selectedComment)
                        }

                        R.id.tabMessageReply -> {
                            if (Functions.checkLoginUser(activity)) {
                                replyStatus = "reply"
                                selectedReplyComment = null
                                hitComment()
                            }
                        }

                        R.id.like_layout -> {
                            if (Functions.checkLoginUser(activity)) {
                                likeComment(selectedCommentPosition, selectedComment!!)
                            }
                        }

                        R.id.reply_count -> {
                            if (selectedComment!!.isExpand) {
                                selectedComment!!.isExpand = false
                            } else {
                                selectedComment!!.isExpand = true
                            }
                            dataList[selectedCommentPosition] = selectedComment
                            adapter!!.notifyDataSetChanged()

                        }

                        R.id.show_less_txt -> {
                            selectedComment!!.isExpand = false
                            dataList[selectedCommentPosition] = selectedComment
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                }

                override fun onItemLongPress(positon: Int, item: CommentModel?, view: View?) {
                    selectedCommentPosition = positon
                    selectedComment = dataList[selectedCommentPosition]!!
                    when (view!!.id) {
                        R.id.message_layout -> openCommentSetting(
                            selectedComment!!,
                            selectedCommentPosition
                        )
                    }
                }

            },object :CommentsAdapter.onRelyItemCLickListener{
                override fun onItemClick(
                    arrayList: ArrayList<CommentModel>?,
                    postion: Int,
                    view: View?
                ) {
                    selectedReplyCommentPosition = postion
                    selectedReplyComment = arrayList?.get(selectedReplyCommentPosition)
                    when (view!!.id) {
                        R.id.user_pic, R.id.username -> openProfile(arrayList?.get(selectedReplyCommentPosition))
                        R.id.tabMessageReply -> {
                            replyStatus = "commentReply"
                            hitComment()
                        }

                        R.id.like_layout -> if (Functions.checkLoginUser(
                                activity
                            )
                        ) {
                            likeCommentReply()
                        }
                    }
                }

                override fun onItemLongPress(
                    arrayList: ArrayList<CommentModel>?,
                    postion: Int,
                    view: View?
                ) {
                    selectedReplyCommentPosition = postion
                    selectedReplyComment = arrayList?.get(selectedReplyCommentPosition)
                    when (view!!.id) {
                        R.id.reply_layout -> {
                            Functions.copyCode(view.context, selectedReplyComment!!.comment_reply)
                        }
                    }
                }

            }, object :CommentsAdapter.LinkClickListener{
                override fun onLinkClicked(view: SocialView?, matchedText: String?) {
                    if (matchedText != null) {
                        openProfileByUsername(matchedText)
                    }
                }

            }, object : FragmentCallBack{
                override fun onResponce(bundle: Bundle?) {
                    if (bundle!!.getBoolean("isShow")) {
                        openTagUser(bundle.getString("name"))
                    }
                }

            }
        )

        binding.recylerview.adapter = adapter
        binding.recylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var userScrolled = false
            var scrollOutitems = 0
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollOutitems = linearLayoutManager!!.findLastVisibleItemPosition()
                if (userScrolled && (scrollOutitems == dataList.size - 1)) {
                    userScrolled = false
                    if (binding.loadMoreProgress.visibility != View.VISIBLE && !ispostFinsh) {
                        binding.loadMoreProgress.visibility = View.VISIBLE
                        pageCount = pageCount + 1
                        getAllComments()
                    }
                }
            }
        })

        if (item.apply_privacy_model!!.videoComment.equals("everyone", ignoreCase = true) ||
            (item.apply_privacy_model!!.videoComment.equals("friend", ignoreCase = true) &&
                    item.userModel?.button.equals("friends", ignoreCase = true))
        ) {
            binding.writeLayout.visibility = View.VISIBLE
            getAllComments()
        }
        else {
            binding.noDataLoader.visibility = View.GONE
            binding.writeLayout.visibility = View.GONE
            binding.tvNoCommentData.text =
                binding.root.context.getString(R.string.comments_are_turned_off)
            binding.commentCount.text = "0 " + myContext.getString(R.string.comments)
            binding.tvNoCommentData.visibility = View.VISIBLE
            binding.recylerview.visibility = View.GONE
        }
        if (commentCount > 0) binding.commentCount.text = "$commentCount " + myContext.getString(R.string.comments)

        getGiftList()
        binding.sendGiftBtn.setOnClickListener(DebounceClickHandler( {
            openGiftScreen()
        }))

        binding.giftLayout.setOnClickListener(DebounceClickHandler({
            openVideoGifts()
        }))

        binding.mentionBtn.setOnClickListener{
            openFriends()
        }
        return binding.root
    }



    fun openGiftScreen(){
        val giftFragment = StickerGiftFragment.newInstance(
            videoUserId,
            "",
            videoId,
            StickerGiftFragment.fromSendGift,
            object : FragmentCallBack {
                override fun onResponce(bundle: Bundle) {
                    if(bundle.getBoolean("isShow",false)) {
                        val giftModel = bundle.getParcelable<GiftModel>("Data")
                        getGiftList()

                        val file = File(FileUtils.getAppFolder(appLevelContext!!) + Variables.APP_Gifts_Folder + giftModel?.id!!+".mp4")
                        if (file.exists()) {
                            val animationViewF = AnimationViewF.newInstance(giftModel?.id!!.toString())
                            animationViewF.show(parentFragmentManager, "animationViewF")
                        }else {
                            if(giftModel.image?.contains(".mp4") == true){
                                CoroutineScope(Dispatchers.IO).launch{
                                    val outputDirectory=File(FileUtils.getAppFolder(appLevelContext!!) + Variables.APP_Gifts_Folder)
                                    val file=DownloadFiles.downloadFileWithProgress(giftModel?.image.toString(),
                                        giftModel?.id!!.toString(),
                                        "mp4",
                                        outputDirectory,
                                        progressCallback = { bytesRead, contentLength ->

                                        })
                                    Functions.printLog(Constants.tag,"downloaded file:"+file?.absolutePath)
                                    if(file?.exists() == true) {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            val animationViewF =
                                                AnimationViewF.newInstance(giftModel?.id!!.toString())
                                            animationViewF.show(parentFragmentManager, "animationViewF")
                                        }

                                    }
                                }
                            }
                            Dialogs.showGiftDailog(requireActivity(), giftModel?.icon)
                        }
                    }
                }
            })
        giftFragment.show(childFragmentManager, "giftFragment")
    }

    fun openVideoGifts(){
        val giftFragment = VideoGiftsFragment.newInstance(giftList,object :FragmentCallBack{
            override fun onResponce(bundle: Bundle?) {
                openGiftScreen()
            }
        })
        giftFragment.show(childFragmentManager, "VideoGiftsFragment")
    }

    val giftList=ArrayList<GiftHistoryModel>()
    fun getGiftList() {
        val parameters = JSONObject()
        try {
            parameters.put("video_id", videoId)
            parameters.put("starting_point", "0")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.showSentGiftsAgainstVideo, parameters, Functions.getHeaders(
                activity
            ), object : Callback {
                override fun onResponce(resp: String) {
                    try {
                        val response = JSONObject(resp)
                        val code = response.optString("code")
                        if (code == "200") {
                            val msg = response.getJSONArray("msg")
                            val temp_list = ArrayList<GiftHistoryModel>()
                            for (i in 0 until msg.length()) {
                                val data = msg.getJSONObject(i)
                                val GiftSend=data.optJSONObject("GiftSend")
                                val senderID=GiftSend.optString("sender_id")
                                val giftId=GiftSend.optString("gift_id")
                                val video_id=GiftSend.optString("video_id")


                                    var model:GiftHistoryModel?=null

                                    if(!video_id.equals("0")){
                                        model = temp_list.find { it.giftSend.giftId == giftId.toInt() && it.user.id == senderID}
                                    }
                                    if(model!=null){
                                        model.count++
                                        temp_list.remove(model)
                                        temp_list.add(model)
                                    }
                                    else {
                                        model=Gson().fromJson(data.toString(),GiftHistoryModel::class.java)
                                        temp_list.add(model)
                                    }

                            }
                            giftList.clear()
                            giftList.addAll(temp_list)
                        }

                    } catch (e: Exception) {
                        Log.d(Constants.tag, "Exception: comment$e")
                    } finally {
                        if(giftList.isNotEmpty()) {
                            binding.giftMainLayout.visibility=View.VISIBLE

                            val adapter = VideoGiftsAdapter(giftList);
                            binding.giftRecylerView.adapter = adapter

                        }
                    }
                }
            })
    }


    private fun openFriends() {
        val fragment = CommentTagedFriendsFragment(
            Functions.getSharedPreference(
                binding.root.context
            ).getString(Variables.U_ID, "")
        ) { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                val arrayList = bundle.getSerializable("data") as ArrayList<UserModel>
                taggedUserList.addAll(arrayList)
                hitComment()

            }
        }
        fragment.show(childFragmentManager, "CommentTagedFriendsF")
    }


    private fun openTagUser(tag: String?) {
        if (Functions.checkProfileOpenValidationByUserName(tag)) {
            val intent = Intent(myContext, ProfileActivity::class.java)
            intent.putExtra("user_name", tag)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
        }
    }

    var taggedUserList: ArrayList<UserModel> = ArrayList()
    var commentType = "OwnComment"
    private fun hitComment() {
           var replyStr = ""
            if (replyStatus == null) {
                commentType = "OwnComment"
            } else if ((replyStatus == "commentReply")) {
                replyStr =
                    myContext.getString(R.string.reply_to) + " " + selectedReplyComment!!.replay_user_name
                commentType = "replyComment"
            } else {
                replyStr = myContext.getString(R.string.reply_to) + " " + selectedComment!!.user_name
                commentType = "replyComment"
            }
            val fragment = EditTextSheetFragment(commentType, taggedUserList, object : FragmentCallBack {
                override fun onResponce(bundle: Bundle) {
                    if (bundle.getBoolean("isShow", false)) {
                        if ((bundle.getString("action") == "sendComment")) {
                            taggedUserList = bundle.getSerializable("taggedUserList") as ArrayList<UserModel>
                            val message = bundle.getString("message")
                            sendComment("" + message)
                        }
                    }
                }
            })
            val bundle = Bundle()
            bundle.putString("replyStr", replyStr)
            fragment.arguments = bundle
            fragment.show(childFragmentManager, "EditTextSheetF")

    }



    private fun sendComment(message: String) {
        var message = message
        if (!TextUtils.isEmpty(message)) {
            if (Functions.checkLoginUser(activity)) {
                if (replyStatus == null) {
                    sendComments(videoId, message)
                } else if ((replyStatus == "commentReply")) {
                    message =
                        myContext.getString(R.string.replied_to) + " " + "@" + selectedReplyComment!!.replay_user_name + " " + message
                    sendCommentsReply(
                        selectedReplyComment!!.parent_comment_id,
                        message,
                        videoId,
                        selectedReplyComment!!.videoOwnerId
                    )
                } else {
                    Log.d(com.coheser.app.Constants.tag, "HitAPI here comment_id " + selectedComment!!.comment_id)
                    sendCommentsReply(
                        selectedComment!!.comment_id,
                        message,
                        videoId,
                        selectedComment!!.videoOwnerId
                    )
                }
            }
        }
    }

    private fun openCommentSetting(item: CommentModel, positon: Int) {
        val fragment = CommentSettingFragment(
                item,
                object : FragmentCallBack {
                    override fun onResponce(bundle: Bundle) {
                        if (bundle.getBoolean("isShow", false)) {
                            if ((bundle.getString("action") == "copyText")) {
                                Functions.copyCode(myContext, item.comments)
                            } else if ((bundle.getString("action") == "pinComment")) {
                                if (Integer.valueOf(item!!.pin_comment_id) > 0) {
                                    if ((item.pin_comment_id == item.comment_id)) {
                                        hitApiPinComment(item, "unpin")
                                    } else {
                                        replacePreviousPinned(item, positon)
                                    }
                                } else {
                                    hitApiPinComment(item, "pin")
                                }
                            } else if ((bundle.getString("action") == "deleteComment")) {
                                hitApiCommentDelete(item, positon)
                            }
                        }
                    }
                })
        fragment.show(parentFragmentManager, "CommentSettingF")
    }

    private fun replacePreviousPinned(item: CommentModel, positon: Int) {
        Dialogs.showDoubleButtonAlert(myContext, myContext.getString(R.string.pin_this_comment),
            myContext.getString(R.string.pinning_description),
            myContext.getString(R.string.cancel_), myContext.getString(R.string.pin_and_replace),
            false, object : FragmentCallBack {
                override fun onResponce(bundle: Bundle) {
                    if (bundle.getBoolean("isShow", false)) {
                        hitApiPinComment(item, "pin")
                    }
                }
            })
    }

    private fun hitApiPinComment(item: CommentModel, pinHitStatus: String) {
        val parameters = JSONObject()
        try {
            parameters.put("video_id", item.video_id)
            var commentPin: String? = ""
            if ((pinHitStatus == "unpin")) {
                commentPin = "0"
            } else {
                commentPin = item.comment_id
            }
            parameters.put("pin_comment_id", commentPin)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Functions.showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity, com.coheser.app.apiclasses.ApiLinks.pinComment, parameters, Functions.getHeaders(
                activity
            ), object : Callback {
                override fun onResponce(resp: String) {
                    Functions.checkStatus(activity, resp)
                    Functions.cancelLoader()
                    try {
                        val response = JSONObject(resp)
                        val code = response.optString("code")
                        if ((code == "200")) {
                            if ((pinHitStatus == "pin")) {
                                val msgObj = response.getJSONObject("msg")
                                val videoObj = msgObj.getJSONObject("Video")
                                val pinnedCommentId = videoObj.optString("pin_comment_id")
                                for (itemDataUpdate: CommentModel? in dataList) {
                                    itemDataUpdate!!.pin_comment_id = pinnedCommentId
                                    dataList[dataList.indexOf(itemDataUpdate)] = itemDataUpdate
                                }
                            } else {
                                for (itemDataUpdate: CommentModel? in dataList) {
                                    itemDataUpdate!!.pin_comment_id = "0"
                                    dataList[dataList.indexOf(itemDataUpdate)] = itemDataUpdate
                                }
                            }
                            adapter!!.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        Log.d(com.coheser.app.Constants.tag, "Exception: $e")
                    }
                }
            })
    }

    private fun hitApiCommentDelete(item: CommentModel, position: Int) {
        val parameters = JSONObject()
        try {
            parameters.put("id", item.comment_id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Functions.showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity, com.coheser.app.apiclasses.ApiLinks.deleteVideoComment, parameters, Functions.getHeaders(
                activity
            ), object : Callback {
                override fun onResponce(resp: String) {
                    Functions.checkStatus(activity, resp)
                    Functions.cancelLoader()
                    try {
                        val response = JSONObject(resp)
                        val code = response.optString("code")
                        if ((code == "200")) {
                            if ((item!!.comment_id == item.pin_comment_id)) {
                                for (itemDataUpdate: CommentModel? in dataList) {
                                    itemDataUpdate!!.pin_comment_id = "0"
                                    dataList[dataList.indexOf(itemDataUpdate)] = itemDataUpdate
                                }
                                dataList.removeAt(position)
                            } else {
                                dataList.removeAt(position)
                            }
                            adapter!!.notifyDataSetChanged()
                            commentCount = dataList.size
                            binding.commentCount.text =
                                commentCount.toString() + " " + myContext.getString(R.string.comments)
                            if (fragmentDataSend != null) fragmentDataSend!!.onDataSent("" + commentCount)
                        }
                    } catch (e: Exception) {
                        Log.d(com.coheser.app.Constants.tag, "Exception: $e")
                    }
                }
            })
    }

    private fun likeCommentReply() {
        val itemUpdate = dataList[selectedCommentPosition]
        val replyList = itemUpdate!!.arrayList
        val itemReplyUpdate = replyList[selectedReplyCommentPosition]
        var action = itemReplyUpdate.comment_reply_liked
        if (action != null) {
            if ((action == "1")) {
                action = "0"
                itemReplyUpdate.reply_liked_count =
                    "" + (Functions.parseInterger(itemReplyUpdate.reply_liked_count) - 1)
            } else {
                action = "1"
                itemReplyUpdate.reply_liked_count =
                    "" + (Functions.parseInterger(itemReplyUpdate.reply_liked_count) + 1)
            }
        }
        itemReplyUpdate.comment_reply_liked = action
        ApiRepository.callApiForLikeCommentReply(
            activity, itemReplyUpdate.comment_reply_id, videoId, object : APICallBack {
                override fun arrayData(arrayList: ArrayList<*>?) {}
                override fun onSuccess(responce: String) {
                    try {
                        val jsonObject = JSONObject(responce)
                        if ((jsonObject.optString("code") == "200")) {
                            val msgObj = jsonObject.getJSONObject("msg")
                            val videoLikeComment = msgObj.getJSONObject("VideoCommentLike")
                            itemReplyUpdate.isLikedByOwner = videoLikeComment.optString("owner_like")
                            replyList[selectedReplyCommentPosition] = itemReplyUpdate
                            itemUpdate.arrayList = replyList
                            dataList[selectedCommentPosition] = itemUpdate
                            adapter!!.notifyDataSetChanged()

                        }
                        else if ((jsonObject.optString("msg") == "unfavourite")) {
                                itemReplyUpdate.isLikedByOwner = "0"
                                replyList[selectedReplyCommentPosition] = itemReplyUpdate
                                itemUpdate.arrayList = replyList
                                dataList[selectedCommentPosition] = itemUpdate
                                adapter!!.notifyDataSetChanged()
                        }

                    } catch (e: Exception) {
                        Log.d(com.coheser.app.Constants.tag, "Exception: $e")
                    }
                }

                override fun onFail(responce: String) {}
            })
    }

    private fun likeComment(positon: Int, item: CommentModel) {
        var action = item.liked
        if (action != null) {
            if ((action == "1")) {
                action = "0"
                item.like_count = "" + (Functions.parseInterger(item.like_count) - 1)
            } else {
                action = "1"
                item.like_count = "" + (Functions.parseInterger(item.like_count) + 1)
            }

            if ((userId == item.videoOwnerId)) {
                if ((item.userId == item.videoOwnerId)) {
                    item.isLikedByOwner = "1"
                } else {
                    item.isLikedByOwner = "0"
                }
            }
            item.liked = action
            ApiRepository.callApiForLikeComment(activity, item.comment_id, object : APICallBack {
                override fun arrayData(arrayList: ArrayList<*>) {
                    Log.d(com.coheser.app.Constants.tag, "DataCheck: " + arrayList.size)
                }

                override fun onSuccess(responce: String) {
                    try {
                        val jsonObject = JSONObject(responce)
                        if ((jsonObject.optString("code") == "200")) {
                            if ((jsonObject.optString("msg") == "unfavourite")) {
                                if ((userId == item.videoOwnerId)) {
                                    item.isLikedByOwner = "0"
                                }
                                dataList[positon] = item
                                adapter!!.notifyDataSetChanged()
                            } else {
                                val msgObj = jsonObject.getJSONObject("msg")
                                val videoLikeComment = msgObj.getJSONObject("VideoCommentLike")
                                if ((userId == item.videoOwnerId)) {
                                    item.isLikedByOwner = videoLikeComment.optString("owner_like")
                                }
                                dataList[positon] = item
                                adapter!!.notifyDataSetChanged()
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(com.coheser.app.Constants.tag, "Exception: $e")
                    }
                }

                override fun onFail(responce: String) {}
            })
        }
    }

    fun getAllComments() {
            if (dataList.isEmpty()) {
                binding.noDataLoader.visibility = View.VISIBLE
            }
            val parameters = JSONObject()
            try {
                parameters.put("video_id", videoId)

                parameters.put("starting_point", "" + pageCount)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            VolleyRequest.JsonPostRequest(
                activity, com.coheser.app.apiclasses.ApiLinks.showVideoComments, parameters, Functions.getHeaders(
                    activity
                ), object : Callback {
                    override fun onResponce(resp: String) {
                        Functions.checkStatus(activity, resp)
                        binding.noDataLoader.visibility = View.GONE
                        var pinnedCommentModel: CommentModel? = null
                        val temp_list = ArrayList<CommentModel?>()
                        try {
                            val response = JSONObject(resp)
                            val code = response.optString("code")
                            if ((code == "200")) {
                                val msgArray = response.getJSONArray("msg")
                                for (i in 0 until msgArray.length()) {
                                    val itemdata = msgArray.optJSONObject(i)
                                    val videoComment = itemdata.optJSONObject("VideoComment")
                                    val userDetailModel = getUserDataModel(itemdata.optJSONObject("User"))

                                    val videoCommentReply = itemdata.optJSONArray("Children")
                                    val replyList = ArrayList<CommentModel>()
                                    if (videoCommentReply!=null && videoCommentReply.length() > 0) {
                                        for (j in 0 until videoCommentReply.length()) {
                                            val jsonObject = videoCommentReply.getJSONObject(j)
                                            val userDetailModelReply = getUserDataModel(jsonObject.optJSONObject("User"))
                                            val VideoComment = jsonObject.optJSONObject("VideoComment")

                                            val comment_model = CommentModel()
                                            comment_model.comment_reply_id = VideoComment.optString("id")
                                            comment_model.reply_liked_count = VideoComment.optString("like_count")
                                            comment_model.comment_reply_liked = VideoComment.optString("like")
                                            comment_model.comment_reply = VideoComment.optString("comment")
                                            comment_model.created = VideoComment.optString("created")
                                            comment_model.videoOwnerId = videoUserId
                                            Log.d(com.coheser.app.Constants.tag,"videoOwnerID $videoUserId")

                                            comment_model.replay_user_name = userDetailModelReply.username
                                            comment_model.replay_user_url = userDetailModelReply.getProfilePic()
                                            comment_model.userId = VideoComment.optString("user_id")
                                            comment_model.isVerified = userDetailModelReply.verified
                                            comment_model.parent_comment_id = videoComment.optString("id")
                                            comment_model.isLikedByOwner = VideoComment.optString("owner_like")
                                            replyList.add(comment_model)
                                        }
                                    }
                                    val iteme = CommentModel()
                                    iteme.isLikedByOwner = videoComment.optString("owner_like")
                                    iteme.videoOwnerId = videoUserId
                                    Log.d(com.coheser.app.Constants.tag,"videoOwnerID $videoUserId")

                                    iteme.pin_comment_id = videoComment.optString("pin", "0")
                                    iteme.userId = userDetailModel.id
                                    iteme.isVerified = userDetailModel.verified
                                    iteme.user_name = userDetailModel.username
                                    iteme.first_name = userDetailModel.first_name
                                    iteme.last_name = userDetailModel.last_name
                                    iteme.arraylist_size = videoCommentReply.length().toString()
                                    iteme.profile_pic = userDetailModel.getProfilePic()
                                    iteme.arrayList = replyList
                                    iteme.video_id = videoComment.optString("video_id")
                                    iteme.comments = videoComment.optString("comment")
                                    iteme.liked = videoComment.optString("like")
                                    iteme.like_count = videoComment.optString("like_count")
                                    iteme.comment_id = videoComment.optString("id")
                                    iteme.created = videoComment.optString("created")
                                    if ((iteme.comment_id == iteme.pin_comment_id)) {
                                        pinnedCommentModel = iteme
                                    } else {
                                        temp_list.add(iteme)
                                    }
                                }
                                if (pageCount == 0) {
                                    dataList.clear()
                                    dataList.addAll(temp_list)
                                } else {
                                    dataList.addAll(temp_list)
                                }
                                if (pinnedCommentModel != null) {
                                    dataList.add(0, pinnedCommentModel)
                                }
                                adapter!!.notifyDataSetChanged()
                            }

                            if (dataList.isEmpty()) {
                                binding.tvNoCommentData.visibility = View.VISIBLE
                            } else {
                                binding.tvNoCommentData.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            Log.d(com.coheser.app.Constants.tag, "Exception: comment$e")
                        } finally {
                            binding.loadMoreProgress.visibility = View.GONE
                        }
                    }
                })
        }


    // this function will call an api to upload your comment reply
    private fun sendCommentsReply(
        commentId: String,
        message: String,
        videoId: String?,
        videoOwnerId: String
    ) {
        ApiRepository.callApiForSendCommentReply(
            activity,
            commentId,
            message,
            videoId,
            videoOwnerId,
            taggedUserList,
            object : APICallBack {
                override fun arrayData(arrayList: ArrayList<*>) {
                  //  binding.tvComment.text = myContext.getString(R.string.leave_a_comment)
                    val itemUpdate = dataList[selectedCommentPosition]
                    val replyList = itemUpdate!!.arrayList
                    for (itemReply: com.coheser.app.models.CommentModel in arrayList as ArrayList<com.coheser.app.models.CommentModel>) {
                        replyList.add(0, itemReply)
                    }
                    itemUpdate.arrayList = replyList
                    itemUpdate.item_count_replies = "" + itemUpdate.arrayList.size
                    dataList[selectedCommentPosition] = itemUpdate
                    adapter!!.notifyDataSetChanged()
                    replyStatus = null
                    selectedComment = null
                    selectedReplyComment = null
                }

                override fun onSuccess(responce: String) {
                    // this will return a string responce
                }

                override fun onFail(responce: String) {
                    // this will return the failed responce
                }
            })
    }

    // this function will call an api to upload your comment
    fun sendComments(video_id: String, comment: String) {
        binding.mentionBtn.visibility = View.GONE
        binding.sendProgress.visibility = View.VISIBLE
        ApiRepository.callApiForSendComment(
            requireActivity(),
            video_id,
            comment,
            taggedUserList,
            object : APICallBack {
                override fun arrayData(arrayList: ArrayList<*>) {
                    binding.mentionBtn.visibility = View.VISIBLE
                    binding.sendProgress.visibility = View.GONE
                    binding.tvNoCommentData.visibility = View.GONE
                    for (item: CommentModel? in arrayList as ArrayList<com.coheser.app.models.CommentModel?>) {
                        dataList.add(0, item)
                        commentCount++
                        binding.commentCount.text =
                            commentCount.toString() + " " + myContext.getString(R.string.comments)
                        if (fragmentDataSend != null) fragmentDataSend!!.onDataSent("" + commentCount)
                    }
                    adapter!!.notifyDataSetChanged()
                    selectedComment = null
                }

                override fun onSuccess(responce: String) {
                    // this will return a string responce
                    binding.mentionBtn.visibility = View.VISIBLE
                    binding.sendProgress.visibility = View.GONE
                }

                override fun onFail(responce: String) {
                    binding.mentionBtn.visibility = View.VISIBLE
                    binding.sendProgress.visibility = View.GONE
                    // this will return the failed responce
                }
            })
    }

    // get the profile data by sending the username instead of id
    private fun openProfileByUsername(username: String) {
        if (Functions.checkProfileOpenValidationByUserName(username)) {
            val intent = Intent(myContext, ProfileActivity::class.java)
            intent.putExtra("user_name", username)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        } else {
            dismiss()
        }
    }

    private fun openProfile(commentModel: CommentModel?) {
        if (Functions.checkProfileOpenValidation(commentModel!!.userId)) {
            val intent = Intent(myContext, ProfileActivity::class.java)
            intent.putExtra("user_id", commentModel.userId)
            intent.putExtra("user_name", commentModel.user_name)
            intent.putExtra("user_pic", commentModel.profile_pic)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
        } else {
            dismiss()
        }
    }

    companion object {
        private var commentCount = 0
    }


}
