package com.coheser.app.activitesfragments.profile.usersstory

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.chat.ChatActivity
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentStoryItemBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.StoryModel
import com.coheser.app.models.StoryVideoModel
import com.coheser.app.simpleclasses.CountDownTimerPausable
import com.coheser.app.simpleclasses.DateOprations.getTimeAgoOrg
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getDevidedChunks
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.isWebUrl
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Functions.showToastOnTop
import com.coheser.app.simpleclasses.Functions.showVideoDurationInSec
import com.coheser.app.simpleclasses.OnStoryTouchListener
import com.coheser.app.simpleclasses.OnSwipeTouchListener
import com.coheser.app.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.json.JSONObject
import java.util.Calendar

class StoryItemF(
    var allDataList: ArrayList<StoryModel>?,
    var currentPagePosition: Int,
    var callBack: FragmentCallBack
) : Fragment(), Player.Listener {
    ///Story VIew
    lateinit var bindingRef: FragmentStoryItemBinding
    var exoplayer: ExoPlayer? = null
    var selectedStoryItem: StoryModel? = null
    var pBarList: ArrayList<ProgressBar> = ArrayList()
    var maxProgressTime: Int = 10000
    var currentIndex: Int = 0
    var targetIndex: Int = 0
    var currentTimer: CountDownTimerPausable? = null
    var rootref: DatabaseReference? = null
    private var adduserInbox: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bindingRef =
            DataBindingUtil.inflate(inflater, R.layout.fragment_story_item, container, false)
        initControl()
        actionControl()
        return bindingRef.getRoot()
    }

    private fun initControl() {
        rootref = FirebaseDatabase.getInstance().reference
        adduserInbox = FirebaseDatabase.getInstance().reference
        selectedStoryItem = allDataList!![currentPagePosition]
        setuplinearLayoutWithProgress()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun actionControl() {
        bindingRef.mediaContainer.setOnTouchListener(object :
            OnStoryTouchListener(bindingRef.root.context) {
            override fun onSingleClick(e: MotionEvent) {
                val x = e.x

                if (x < (bindingRef.mediaContainer.width * 0.5)) {
                    Log.d(Constants.tag, "OnLeft click")
                } else {
                    Log.d(Constants.tag, "OnRight click")
                    moveToRightChunk()
                }
            }

            override fun onButtonReleased() {
                Log.d(Constants.tag, "onReleased Press")
                performResumeAction()
            }

            override fun onButtonPressed(e: MotionEvent) {
                Log.d(Constants.tag, "onPressed Press")
                performStopAction()
            }
        })

        bindingRef.ivOption.setOnClickListener(DebounceClickHandler { view ->
            showDeleteVideo(
                view,
                bindingRef.root.context
            )
        })

        bindingRef.ivLike.setOnClickListener(DebounceClickHandler { openStoryEmoticons() })

        bindingRef.ivSend.setOnClickListener(DebounceClickHandler {
            if (!(bindingRef.etMessage.text.toString().isEmpty())) {
                sendStoryComment(selectedStoryItem, selectedStoryItem!!.videoList!![currentIndex])
            }
        })

        KeyboardVisibilityEvent.setEventListener(
            activity
        ) { isOpen ->
            if (isOpen) {
                performStopAction()
            } else {
                performResumeAction()
            }
        }
    }

    private fun openStoryEmoticons() {
        performStopAction()
        val fragment = StoryEmoticonF.newInstance { bundle ->
            if (bundle.getBoolean("isShow")) {
                val emojiCode = bundle.getString("data")
                sendStoryLike(
                    selectedStoryItem,
                    selectedStoryItem!!.videoList!![currentIndex],
                    emojiCode
                )
            } else {
                performResumeAction()
            }
        }
        fragment.show(childFragmentManager, "StoryEmoticonF")
    }

    private fun moveToRightChunk() {
        if (currentIndex < targetIndex) {
            completeChunkProgress(100)
            currentIndex = currentIndex + 1
            moveToVideoChunk()
        } else {
            MoveToNextUserVideos()
        }
    }

    private fun completeChunkProgress(progress: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pBarList[currentIndex].setProgress(progress, true)
        } else {
            pBarList[currentIndex].progress = progress
        }
    }

    private fun showDeleteVideo(view: View, context: Context) {
        val wrapper: Context = ContextThemeWrapper(context, R.style.AlertDialogCustom)
        val popup = PopupMenu(wrapper, view)

        popup.menuInflater.inflate(R.menu.menu_playlist, popup.menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.gravity = Gravity.TOP or Gravity.RIGHT
        }

        popup.show()
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuDelete -> {
                    deleteStoryItem()
                }
            }
            true
        }
    }

    // this method will upload the image in chhat
    fun sendStoryLike(selectedStory: StoryModel?, storyModel: StoryVideoModel, emojiCode: String?) {
        showLoader(activity, false, false)
        val c = Calendar.getInstance().time
        val formattedDate = Variables.df.format(c)

        val senderId = getSharedPreference(
            bindingRef.root.context
        ).getString(Variables.U_ID, "")
        val receiverId = selectedStory!!.id

        val dref = rootref!!.child("chat").child("$senderId-$receiverId").push()
        val key = dref.key

        val current_user_ref = "chat/$senderId-$receiverId"
        val chat_user_ref = "chat/$receiverId-$senderId"


        val `object` = JSONObject()
        try {
            `object`.put("storyId", storyModel.id)
            `object`.put("storyGif", storyModel.gif)
            `object`.put("storyUrl", storyModel.video)
            `object`.put("storyEmoticon", "" + emojiCode)
        } catch (e: Exception) {
        }

        val message_user_map = HashMap<String,String>()
        message_user_map["receiver_id"] = ""+receiverId
        message_user_map["sender_id"] = ""+senderId
        message_user_map["chat_id"] = ""+key
        message_user_map["text"] = "" + `object`
        message_user_map["type"] = "storyLike"
        message_user_map["pic_url"] = ""+selectedStory.getProfilePic()
        message_user_map["status"] = "0"
        message_user_map["time"] = ""
        message_user_map["sender_name"] = ""+getSharedPreference(bindingRef.root.context).getString(Variables.U_NAME, "")
        message_user_map["timestamp"] = formattedDate
        val user_map: HashMap<String, Any> = HashMap()

        user_map["$current_user_ref/$key"] = message_user_map
        user_map["$chat_user_ref/$key"] = message_user_map

        rootref!!.updateChildren(
            user_map,
            DatabaseReference.CompletionListener { databaseError, databaseReference ->
                cancelLoader()
                performResumeAction()
                val inbox_sender_ref = "Inbox/$senderId/$receiverId"
                val inbox_receiver_ref = "Inbox/$receiverId/$senderId"

                val messageForPush = (getSharedPreference(
                    bindingRef.root.context
                ).getString(Variables.U_NAME, "")
                        + " liked your story...")

                val sendermap: HashMap<String, Any> = HashMap()
                sendermap["rid"] = ""+senderId
                sendermap["name"] = ""+ getSharedPreference(bindingRef.root.context).getString(Variables.U_NAME, "")
                sendermap["pic"] = ""+getSharedPreference(bindingRef.root.context).getString(Variables.U_PIC, "")
                sendermap["msg"] = "" + messageForPush
                sendermap["status"] = "0"
                sendermap["timestamp"] = -1 * System.currentTimeMillis()
                sendermap["date"] = formattedDate

                val receivermap: HashMap<String, Any> = HashMap()
                receivermap["rid"] = ""+receiverId
                receivermap["name"] = ""+selectedStory.username
                receivermap["pic"] = ""+selectedStory.getProfilePic()
                receivermap["msg"] = "" + messageForPush
                receivermap["status"] = "1"
                receivermap["timestamp"] = -1 * System.currentTimeMillis()
                receivermap["date"] = formattedDate

                val both_user_map: HashMap<String, Any> = HashMap()
                both_user_map[inbox_sender_ref] = receivermap
                both_user_map[inbox_receiver_ref] = sendermap
                adduserInbox!!.updateChildren(both_user_map).addOnCompleteListener {
                    ChatActivity.sendPushNotification(
                        activity, getSharedPreference(
                            bindingRef.root.context
                        ).getString(Variables.U_NAME, ""), "Send an gif image....",
                        receiverId, senderId
                    )
                }
            })
    }


    // this method will upload the image in chhat
    fun sendStoryComment(selectedStory: StoryModel?, storyModel: StoryVideoModel) {
        showLoader(activity, false, false)
        val c = Calendar.getInstance().time
        val formattedDate = Variables.df.format(c)

        val senderId = getSharedPreference(
            bindingRef.root.context
        ).getString(Variables.U_ID, "")
        val receiverId = selectedStory!!.id

        val dref = rootref!!.child("chat").child("$senderId-$receiverId").push()
        val key = dref.key

        val current_user_ref = "chat/$senderId-$receiverId"
        val chat_user_ref = "chat/$receiverId-$senderId"


        val `object` = JSONObject()
        try {
            `object`.put("storyId", storyModel.id)
            `object`.put("storyGif", storyModel.gif)
            `object`.put("storyUrl", storyModel.video)
            `object`.put("storyComment", "" + bindingRef.etMessage.text.toString())
        } catch (e: Exception) {
        }


        val message_user_map: HashMap<String, Any> = HashMap()
        message_user_map["receiver_id"] = ""+receiverId
        message_user_map["sender_id"] = ""+senderId
        message_user_map["chat_id"] = ""+key
        message_user_map["text"] = "" + `object`
        message_user_map["type"] = "storyComment"
        message_user_map["pic_url"] = ""+selectedStory.getProfilePic()
        message_user_map["status"] = "0"
        message_user_map["time"] = ""
        message_user_map["sender_name"] = ""+getSharedPreference(bindingRef.root.context).getString(Variables.U_NAME, "")
        message_user_map["timestamp"] = formattedDate
        val user_map: HashMap<String, Any> = HashMap()

        user_map["$current_user_ref/$key"] = message_user_map
        user_map["$chat_user_ref/$key"] = message_user_map

        rootref!!.updateChildren(
            user_map,
            DatabaseReference.CompletionListener { databaseError, databaseReference ->
                cancelLoader()
                bindingRef.etMessage.setText("")
                val inbox_sender_ref = "Inbox/$senderId/$receiverId"
                val inbox_receiver_ref = "Inbox/$receiverId/$senderId"

                val messageForPush = (getSharedPreference(
                    bindingRef.root.context
                ).getString(Variables.U_NAME, "")
                        + " commented on your story...")

                val sendermap: HashMap<String, Any> = HashMap()
                sendermap["rid"] = ""+senderId
                sendermap["name"] = ""+getSharedPreference(
                    bindingRef.root.context
                ).getString(Variables.U_NAME, "")
                sendermap["pic"] = ""+getSharedPreference(
                    bindingRef.root.context
                ).getString(Variables.U_PIC, "")
                sendermap["msg"] = "" + messageForPush
                sendermap["status"] = "0"
                sendermap["timestamp"] = -1 * System.currentTimeMillis()
                sendermap["date"] = formattedDate

                val receivermap: HashMap<String, Any> = HashMap()
                receivermap["rid"] = ""+receiverId
                receivermap["name"] = ""+selectedStory.username
                receivermap["pic"] = ""+selectedStory.getProfilePic()
                receivermap["msg"] = "" + messageForPush
                receivermap["status"] = "1"
                receivermap["timestamp"] = -1 * System.currentTimeMillis()
                receivermap["date"] = formattedDate

                val both_user_map: HashMap<String, Any> = HashMap()
                both_user_map[inbox_sender_ref] = receivermap
                both_user_map[inbox_receiver_ref] = sendermap
                adduserInbox!!.updateChildren(both_user_map).addOnCompleteListener {
                    ChatActivity.sendPushNotification(
                        activity, getSharedPreference(
                            bindingRef.root.context
                        ).getString(Variables.U_NAME, ""), "Send an gif image....",
                        receiverId, senderId
                    )
                }
            })
    }


    private fun deleteStoryItem() {
        val parameters = JSONObject()
        try {
            parameters.put("video_id", selectedStoryItem!!.videoList!![currentIndex].id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.deleteVideo, parameters, getHeaders(
                activity
            )
        ) { resp ->
            checkStatus(activity, resp)
            cancelLoader()
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code == "200") {
                    val bundle = Bundle()
                    bundle.putBoolean("isShow", true)
                    bundle.putString("action", "deleteItem")
                    bundle.putInt("itemPos", currentIndex)
                    callBack.onResponce(bundle)
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception callBack: $e")
            }
        }
    }


    private fun performResumeAction() {
        if (currentTimer != null) {
            currentTimer!!.start()
        }
        if (exoplayer != null) {
            exoplayer!!.play()
        }
    }

    private fun performStopAction() {
        if (currentTimer != null) {
            if (!(currentTimer!!.isPaused)) {
                currentTimer!!.pause()
            }
        }

        if (exoplayer != null) {
            exoplayer!!.pause()
        }
    }


    private fun setuplinearLayoutWithProgress() {
        Log.d(Constants.tag, "onSetup StoryProgress")
        bindingRef.progressView.removeAllViews()
        bindingRef.progressView.weightSum = selectedStoryItem!!.videoList?.size?.toFloat()!!

        //delete option manage
        if (selectedStoryItem!!.id == getSharedPreference(
                bindingRef.root.context
            ).getString(Variables.U_ID, "")
        ) {
            bindingRef.ivOption.visibility = View.VISIBLE
        } else {
            bindingRef.ivOption.visibility = View.GONE
        }


        showProfileData()
        setupVideoProgresses(selectedStoryItem)
        startFirstTimeVideo()
    }


    private fun startFirstTimeVideo() {
        if (selectedStoryItem!!.videoList!!.size > currentIndex) {
            if (currentTimer != null) {
                currentTimer!!.cancel()
                currentTimer = null
            }
            showMedia()
            currentTimer = object : CountDownTimerPausable(
                (maxProgressTime * 1000).toLong(),
                getDevidedChunks(maxProgressTime, 100)
            ) {
                override fun onTick(millisUntilFinished: Long) {
                    val progress =
                        (millisUntilFinished / getDevidedChunks(maxProgressTime, 100)).toInt()
                    CDTimerInnerProgress = (100 - progress)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        pBarList[currentIndex].setProgress(CDTimerInnerProgress, true)
                    } else {
                        pBarList[currentIndex].progress = CDTimerInnerProgress
                    }
                }

                override fun onFinish() {
                    if (currentIndex < targetIndex) {
                        currentIndex = currentIndex + 1
                        moveToVideoChunk()
                    } else {
                        MoveToNextUserVideos()
                    }
                }
            }
        } else {
            storyProgressComplete()
        }
    }

    private fun moveToVideoChunk() {
        if (currentTimer != null) {
            currentTimer!!.cancel()
            currentTimer = null
        }
        exoplayer!!.release()
        exoplayer = null
        showMedia()
        currentTimer = object : CountDownTimerPausable(
            (maxProgressTime * 1000).toLong(),
            getDevidedChunks(maxProgressTime, 100)
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val progress =
                    (millisUntilFinished / getDevidedChunks(maxProgressTime, 100)).toInt()
                CDTimerInnerProgress = (100 - progress)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    pBarList[currentIndex].setProgress(CDTimerInnerProgress, true)
                } else {
                    pBarList[currentIndex].progress = CDTimerInnerProgress
                }
            }

            override fun onFinish() {
                if (currentIndex < targetIndex) {
                    currentIndex = currentIndex + 1
                    moveToVideoChunk()
                } else {
                    MoveToNextUserVideos()
                }
            }
        }
    }


    private fun MoveToNextUserVideos() {
        currentPagePosition = currentPagePosition + 1
        if (currentTimer != null) {
            currentTimer!!.cancel()
            currentTimer = null
        }
        if (currentPagePosition == (allDataList!!.size)) {
            requireActivity().onBackPressed()
        } else {
            ViewStoryA.mPager.setCurrentItem(currentPagePosition, true)
        }
    }

    private fun setupVideoProgresses(storyModel: StoryModel?) {
        for (i in storyModel!!.videoList!!.indices) {
            val pBar = ProgressBar(
                bindingRef.root.context,
                null,
                android.R.attr.progressBarStyleHorizontal
            )
            pBar.max = 100
            val lp = LinearLayout.LayoutParams(
                0,
                resources.getDimension(com.intuit.sdp.R.dimen._10sdp).toInt()
            )
            lp.weight = 1f
            if (storyModel.videoList!!.size > (i + 1)) {
                lp.marginEnd = resources.getDimension(com.intuit.sdp.R.dimen._2sdp).toInt()
            }
            pBar.layoutParams = lp
            pBar.progress = 0
            val progressDrawable = pBar.progressDrawable.mutate()
            progressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            pBar.progressDrawable = progressDrawable
            pBarList.add(pBar)
            bindingRef.progressView.addView(pBarList[i])
        }

        targetIndex = selectedStoryItem!!.videoList!!.size - 1
    }


    private fun showProfileData() {
        val profileUrl = selectedStoryItem!!.getProfilePic()
        val name = selectedStoryItem!!.username

        bindingRef.profilePic.controller =
            frescoImageLoad(profileUrl, bindingRef.profilePic, false)
        bindingRef.userName.text = "" + name

        if (selectedStoryItem!!.id == getSharedPreference(
                bindingRef.root.context
            ).getString(Variables.U_ID, "")
        ) {
            bindingRef.bottomLayout.visibility = View.GONE
        } else {
            bindingRef.bottomLayout.visibility = View.VISIBLE
        }
    }


    var CDTimerInnerProgress: Int = 0

    private fun showMedia() {
        try {
            val time = getTimeAgoOrg(selectedStoryItem!!.videoList!![currentIndex].created)
            bindingRef.time.text = "" + time

            showStoryPlayer()
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception showMedia: $e")
        }
    }


    private fun showStoryPlayer() {
        performStopAction()

        val videoAttachment = "" + selectedStoryItem!!.videoList!![currentIndex].video
        if (isWebUrl(videoAttachment)) {
            initExoPlayer(videoAttachment)
        } else {
            Log.d(Constants.tag, "videoAttachment: $videoAttachment")
            Handler(Looper.getMainLooper()).postDelayed({
                showToastOnTop(
                    activity,
                    null,
                    bindingRef.root.context.getString(R.string.invalid_video_url)
                )
                MoveToNextUserVideos()
            }, 1500)
        }
    }


    private fun storyProgressComplete() {
        Log.d(Constants.tag, "Complete Story Progress")
        if (currentTimer != null) {
            currentTimer!!.cancel()
            currentTimer = null
        }
        Log.d(
            Constants.tag,
            currentPagePosition.toString() + " currentPagePosition: " + allDataList!!.size
        )
        if (currentPagePosition == (allDataList!!.size)) {
            requireActivity().onBackPressed()
        } else {
            currentPagePosition = currentPagePosition + 1
            ViewStoryA.mPager.setCurrentItem(currentPagePosition, true)
        }
    }


    private fun initExoPlayer(videoAttachment: String?) {
        if (exoplayer == null && videoAttachment != null) {
            Log.d(Constants.tag, "Check Exo player Init: $videoAttachment")
            maxProgressTime = showVideoDurationInSec(videoAttachment)
            val trackSelector = DefaultTrackSelector(bindingRef.root.context)
            val loadControl = DefaultLoadControl()
            exoplayer = ExoPlayer.Builder(bindingRef.root.context)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .build()
            exoplayer?.setMediaItem(MediaItem.fromUri(videoAttachment))
            exoplayer?.prepare()
            exoplayer?.addListener(this@StoryItemF)
            exoplayer?.setRepeatMode(Player.REPEAT_MODE_ALL)

            try {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build()
                exoplayer?.setAudioAttributes(audioAttributes, true)
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception audio focus : $e")
            }
            setPlayer()
            requireActivity().runOnUiThread {
                bindingRef.playerview.findViewById<View>(R.id.exo_play).visibility = View.GONE
                if (exoplayer != null) {
                    bindingRef.playerview.player = exoplayer
                }
            }
        } else {
            Log.d(Constants.tag, "initExoPlayer: ")
        }
    }


    override fun onPause() {
        super.onPause()
        if (currentTimer != null) {
            currentTimer!!.cancel()
            currentTimer = null
        }
        if (exoplayer != null) {
            exoplayer!!.playWhenReady = false
            bindingRef.playerview.findViewById<View>(R.id.exo_play).alpha = 1f
        }
    }


    override fun onStop() {
        super.onStop()
        if (exoplayer != null) {
            exoplayer!!.playWhenReady = false
            bindingRef.playerview.findViewById<View>(R.id.exo_play).alpha = 1f
        }
    }


    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING) {
            bindingRef.progressBar.visibility = View.VISIBLE
            Log.d(Constants.tag, " buffering ")
        } else if (playbackState == Player.STATE_READY) {
            bindingRef.progressBar.visibility = View.GONE
            performResumeAction()
            Log.d(Constants.tag, " ready ")
        }
    }

    fun setPlayer() {
        if (exoplayer != null) {
            exoplayer!!.playWhenReady = true
            bindingRef.playerview.findViewById<View>(R.id.exo_play).alpha = 0f
            bindingRef.playerview.setOnTouchListener(object :
                OnSwipeTouchListener(bindingRef.root.context) {
                override fun onSingleClick() {
                    if (!exoplayer!!.playWhenReady) {
                        exoplayer!!.playWhenReady = true
                        bindingRef.playerview.findViewById<View>(R.id.exo_play).alpha = 0f
                    } else {
                        exoplayer!!.playWhenReady = false
                        bindingRef.playerview.findViewById<View>(R.id.exo_play).alpha = 1f
                    }
                }

                override fun onDoubleClick(e: MotionEvent) {
                    if (!exoplayer!!.playWhenReady) {
                        exoplayer!!.playWhenReady = true
                    }
                }
            })
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (exoplayer != null) {
            exoplayer!!.playWhenReady = false
            bindingRef.playerview.findViewById<View>(R.id.exo_play).alpha = 1f
            exoplayer!!.removeListener(this)
            exoplayer!!.release()
            exoplayer = null
        }
    }
}