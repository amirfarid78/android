package com.coheser.app.activitesfragments.livestreaming.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.EditTextSheetFragment
import com.coheser.app.activitesfragments.livestreaming.StreamingConstants
import com.coheser.app.activitesfragments.livestreaming.adapter.LiveCommentsAdapter
import com.coheser.app.activitesfragments.livestreaming.adapter.LiveUserViewAdapter
import com.coheser.app.activitesfragments.livestreaming.adapter.WishListGiftAdapter
import com.coheser.app.activitesfragments.livestreaming.fragments.GoalDetailF
import com.coheser.app.activitesfragments.livestreaming.fragments.InviteContactsToStreamFragment
import com.coheser.app.activitesfragments.livestreaming.fragments.PkBattleInviteFragment
import com.coheser.app.activitesfragments.livestreaming.fragments.PkBattleInviteSendFragment
import com.coheser.app.activitesfragments.livestreaming.fragments.ShowOtherProfileBottomF
import com.coheser.app.activitesfragments.livestreaming.fragments.ShowProfileBottomF
import com.coheser.app.activitesfragments.livestreaming.fragments.StreamerOptionsBottomSheet
import com.coheser.app.activitesfragments.livestreaming.fragments.StreamingStartF
import com.coheser.app.activitesfragments.livestreaming.fragments.WishListBottomF
import com.coheser.app.activitesfragments.livestreaming.model.CameraRequestModel
import com.coheser.app.activitesfragments.livestreaming.model.GiftWishListModel
import com.coheser.app.activitesfragments.livestreaming.model.LiveCoinsModel
import com.coheser.app.activitesfragments.livestreaming.model.LiveCommentModel
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.activitesfragments.livestreaming.model.PkInvitation
import com.coheser.app.activitesfragments.livestreaming.stats.LocalStatsData
import com.coheser.app.activitesfragments.livestreaming.stats.RemoteStatsData
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.activitesfragments.profile.analytics.DateOperations.getDate
import com.coheser.app.activitesfragments.shoping.ShopItemDetailA
import com.coheser.app.activitesfragments.shoping.adapter.StreamingProductsAdapter
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.ActivityMulticastStreamerBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.StreamJoinModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.DateOprations.checkTimeDiffernce
import com.coheser.app.simpleclasses.DateOprations.getCurrentDate
import com.coheser.app.simpleclasses.Dialogs
import com.coheser.app.simpleclasses.FirebaseFunction.deleteImageFromFirebase
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkProfileOpenValidation
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.getSuffix
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.OnSwipeTouchListener
import com.coheser.app.simpleclasses.Variables
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.controller.ControllerListener
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.coheser.app.activitesfragments.livestreaming.fragments.AnimationViewF
import com.coheser.app.simpleclasses.Downloading.DownloadFiles
import com.coheser.app.simpleclasses.FileUtils
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.convertDpToPx
import com.coheser.app.simpleclasses.TicTicApp.Companion.appLevelContext
import com.volley.plus.VPackages.VolleyRequest
import io.agora.rtc2.IRtcEngineEventHandler.LocalVideoStats
import io.agora.rtc2.IRtcEngineEventHandler.RemoteAudioStats
import io.agora.rtc2.IRtcEngineEventHandler.RemoteVideoStats
import io.agora.rtc2.IRtcEngineEventHandler.RtcStats
import io.agora.rtc2.video.VideoEncoderConfiguration.VideoDimensions
import io.paperdb.Paper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class MulticastStreamerActivity : RtcBaseActivity(), View.OnClickListener {

    private val DELAY: Long = 20000
    var pkInvitation: PkInvitation? = null
    var rootref: DatabaseReference? = null
    var streamingId: String? = null
    lateinit var model: LiveUserModel
    lateinit var context: Context
    var isFirstTimeFlip: Boolean = true
    var liveUserViewAdapter: LiveUserViewAdapter? = null

    lateinit var binding: ActivityMulticastStreamerBinding

    var dataList: ArrayList<LiveCommentModel> = ArrayList()
    var adapter: LiveCommentsAdapter? = null
    var productChildListener: ValueEventListener? = null
    var productsList: ArrayList<ProductModel> = ArrayList()
    var productsAdapter: StreamingProductsAdapter? = null
    var commentChildListener: ChildEventListener? = null
    var current_cal: Calendar? = null
    var jointUserList: ArrayList<StreamJoinModel> = ArrayList()
    var joinValueEventListener: ValueEventListener? = null
    var likeValueEventListener: ChildEventListener? = null
    var heartCounter: Int = 0

    var wishDataList: ArrayList<GiftWishListModel> = ArrayList()

    var coinValueEventListener: ValueEventListener? = null
    var senderCoinsList: ArrayList<LiveCoinsModel?> = ArrayList()

    var broadcastValueEventListener: ValueEventListener? = null
    var pkInvitationListener: ValueEventListener? = null
    var pkBattleInviteSendF: PkBattleInviteSendFragment? = null
    var pkInvitationDialog: Dialog? = null
    var pkInvitationTimer: CountDownTimer? = null
    var pkBattleCountDown: CountDownTimer? = null
    var alertTimer: CountDownTimer? = null
    var taggedUserList: ArrayList<UserModel>? = ArrayList()
    var isAudioMute: Boolean = false
    var isVideoActivated: Boolean = true
    var isbeautyActivated: Boolean = true
    var connectCheckListener: ValueEventListener? = null
    var connectedRef: DatabaseReference? = null
    var audio: MediaPlayer? = null
    var player: MediaPlayer? = null
    var cameraRequestEventListener: ValueEventListener? = null
    private var mVideoDimension: VideoDimensions? = null
    private var timer = Timer()
    var streamingOnTime:Long=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE), this, javaClass, false)

        binding = DataBindingUtil.setContentView(this@MulticastStreamerActivity, R.layout.activity_multicast_streamer)
        context = this@MulticastStreamerActivity
        rootref = FirebaseDatabase.getInstance().reference

        if (intent != null) {
            model = intent.getParcelableExtra("data")!!
            streamingId=model?.streamingId
        }

        InitControl()
        OpenStreamingStartScreen()

    }


    private fun InitControl() {

        binding.liveVideoGridLayout.setStatsManager(statsManager())
        rtcEngine()?.setClientRole(io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER)
        startBroadcast()

        binding.tabpk.setOnClickListener(this)
        binding.ivVideoRequest.setOnClickListener(this)
        binding.ivMainProfile.setOnClickListener(this)
        binding.viewerImage1.setOnClickListener(this)
        binding.viewerImage2.setOnClickListener(this)
        binding.viewerImage3.setOnClickListener(this)
        binding.tabmoreStream.setOnClickListener(this)
        binding.liveVideoGridLayout.setMainParentLayout(binding.videoGridMainLayout,model?.userPicture)


        binding.crossBtn.setOnClickListener(this)
        binding.crossBtn2.setOnClickListener(this)
        binding.notifyCrossBtn.setOnClickListener(this)
        binding.goalLayout.setOnClickListener(this)

        binding.tabEffects.setOnClickListener(this)
        binding.tabShareStream.setOnClickListener(this)
        binding.notifyBtn.setOnClickListener(this)

        val inAnim = AnimationUtils.loadAnimation(context, R.anim.in_from_right)
        val outAnim = AnimationUtils.loadAnimation(context, R.anim.out_to_left)
        val inPrevAnim = AnimationUtils.loadAnimation(context, R.anim.in_from_left)
        val outPrevAnim = AnimationUtils.loadAnimation(context, R.anim.out_to_right)


        binding.mainLayout.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onSwipeTop() {
            }

            override fun onSwipeRight() {
                binding.viewflliper.inAnimation = inPrevAnim
                binding.viewflliper.outAnimation = outPrevAnim
                Log.d(Constants.tag, "start")

                if (binding.viewTwo === binding.viewflliper.currentView) {
                    if (binding.hostListLayout.visibility==View.VISIBLE) {

                        ObjectAnimator.ofFloat(binding.hostListLayout, "translationX", binding.mainLayout.width.toFloat()).apply {
                            duration = 600
                            start()
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(600)
                            binding.hostListLayout.visibility=View.GONE
                        }


                    } else {
                        binding.viewflliper.showPrevious()
                    }
                } else {
                    binding.viewflliper.showPrevious()
                }
            }

            override fun onSwipeLeft() {
                binding.viewflliper.inAnimation = inAnim
                binding.viewflliper.outAnimation = outAnim
                Log.d(Constants.tag, "end")
                if (binding.viewTwo === binding.viewflliper.currentView) {
                    if (binding.hostListLayout.visibility==View.GONE) {

                        binding.hostListLayout.visibility=View.VISIBLE
                        binding.hostListLayout.translationX=(binding.mainLayout.width).toFloat()
                        ObjectAnimator.ofFloat(binding.hostListLayout, "translationX", (binding.mainLayout.width-(convertDpToPx(this@MulticastStreamerActivity,120))).toFloat()).apply {
                            duration = 600 // 1 second
                            start()
                        }

                    }
                } else {
                    binding.viewflliper.showNext()
                }
            }

            override fun onSwipeBottom() {
            }

            fun onDoubleClick() {
            }

            override fun onSingleClick() {
            }
        })

    }

    fun OpenStreamingStartScreen(){
        val fragment=StreamingStartF.newInstance(model,object :FragmentCallBack{
            override fun onResponce(bundle: Bundle?) {
                if(bundle!=null){
                    val type=bundle.getString("type")
                    if(type.equals("flipCamera")){
                        rtcEngine()?.switchCamera()
                    }
                   else if(type.equals("enhance")){
                        isbeautyActivated=!isbeautyActivated
                        rtcEngine()?.setBeautyEffectOptions(isbeautyActivated, StreamingConstants.DEFAULT_BEAUTY_OPTIONS)
                    }
                    else{
                        startLive()
                    }
                }
            }
        })
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainLayout, fragment) // Use .add() if you don't want to replace
            .addToBackStack(null) // Optional: Adds to back stack
            .commit()
    }


    fun startLive() {

        streamingOnTime=System.currentTimeMillis()
        binding.crossBtn.visibility=View.VISIBLE

        if (isFirstTimeFlip) {
            isFirstTimeFlip = false
            if (binding.viewOne === binding.viewflliper.currentView) {
                binding.viewflliper.showNext()
            }
        }

        initCommentAdapter()
        setUpJoinRecycler()
        setWishListAdapter()

        initData()

        rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).keepSynced(true)
        rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).onDisconnect().removeValue()

        addFirebaseNode()
        broadcasterlistenerNode()
        addStreamInternetConnection()
        addNodeCameraRequest()


        ListenerCoinNode()
        addLikeStream()
        ListenerJoinNode()
        ListCommentData()
        addProductListener()
        addPkInvitationListener()

        setUpScreenData()
    }


    private fun setUpScreenData() {
        binding.topBtnLayout.visibility = View.VISIBLE
        binding.bottomBtnLayout.visibility = View.VISIBLE
        val verified = getSharedPreference(context).getString(Variables.U_WALLET, "0")
        if (verified == "1") {
            binding.ivVerified.visibility = View.VISIBLE
        } else {
            binding.ivVerified.visibility = View.GONE
        }
        binding.tvMainUserName.text = model.userName

        Functions.printLog(Constants.tag, "userPicture" + model.userPicture)
        binding.ivMainProfile.controller = frescoImageLoad(model.userPicture, binding.ivMainProfile, false)

        if(model.setGoalStream!=null){
            binding.goalLayout.visibility=View.VISIBLE
            binding.totalGoalCount.text="/"+model.setGoalStream?.goalAmount
        }
        else{
            binding.goalLayout.visibility=View.GONE
        }

    }

    fun initCommentAdapter() {
        dataList.clear()
        val linearLayoutManager = LinearLayoutManager(context)
        binding.recylerview.layoutManager = linearLayoutManager
        binding.recylerview.setHasFixedSize(true)
        adapter = LiveCommentsAdapter(context!!, dataList) { view, pos, `object` ->
            val itemUpdate = dataList[pos]
            if (view.id == R.id.profileImage) {
                openProfile(itemUpdate!!.userId)
            } else if (view.id == R.id.username) {
                openProfile(itemUpdate!!.userId)
            } else if (itemUpdate!!.type == "shareStream") {
                inviteFriendsForStream()
            } else if (itemUpdate.type == "selfInviteForStream") {
                if (model!!.getDuetConnectedUserId() != null && !(TextUtils.isEmpty(
                        model!!.getDuetConnectedUserId()
                    ))
                ) {
                    Toast.makeText(
                        context,
                        context!!.getString(R.string.user_already_connect_to_streaming),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showCameraRequest(itemUpdate.userId!!)
                }
            } else {
                openProfile(itemUpdate.userId)
            }
        }
        binding.recylerview.adapter = adapter
    }


    private var currentPage = 0
    var wishListAdapter:WishListGiftAdapter?=null
    fun setWishListAdapter(){
        wishDataList.clear()
        if(model.GiftWishList!=null){
            wishDataList.addAll(model.GiftWishList!!)
        }
        if(wishDataList.size>0){
            binding.wishListRecycler.visibility=View.VISIBLE
            val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.wishListRecycler.layoutManager = linearLayoutManager
            binding.wishListRecycler.setHasFixedSize(true)
            wishListAdapter = WishListGiftAdapter(context!!, wishDataList) { view, pos, `object` ->

                val f = WishListBottomF.newInstance(model, WishListBottomF.fromAdmin,object : FragmentCallBack{
                    override fun onResponce(bundle: Bundle?) {
                    }
                })
                f.show(supportFragmentManager, "ShowOtherProfileBottomF")

            }
            binding.wishListRecycler.adapter = wishListAdapter
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(binding.wishListRecycler)

            if(wishDataList.size>1) {
                startWishListScrolling()
            }
        }
        else{
            binding.wishListRecycler.visibility=View.GONE
        }

    }

    private var job: Job? = null
    fun startWishListScrolling() {
        if (job?.isActive == true) return
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                if (currentPage < wishListAdapter?.itemCount?.minus(1) ?: 0) {
                    currentPage++  // Move to the next page
                } else {
                    currentPage = 0  // Reset to first page
                }
                binding.wishListRecycler.smoothScrollToPosition(currentPage)
                delay(3000) // Wait for 3 seconds
            }
        }
    }

    fun openProfile(userId: String?) {
        if (checkProfileOpenValidation(userId)) {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        }
    }

    fun addProductListener() {
        if (productChildListener == null) {
            initproductAdapter()
            printLog(Constants.tag, "addProductListener")

            productChildListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    printLog(Constants.tag, dataSnapshot.toString())
                    productsList.clear()
                    if (dataSnapshot.exists()) {
                        binding.productRecylerVeiw.visibility = View.VISIBLE
                        for (product in dataSnapshot.children) {
                            val model = product.getValue(ProductModel::class.java)
                            productsList.add(model!!)
                        }
                        productsAdapter!!.notifyDataSetChanged()
                    } else {
                        binding.productRecylerVeiw.visibility = View.GONE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    printLog(Constants.tag, databaseError.toString())
                    binding.productRecylerVeiw.visibility = View.GONE
                }
            }

            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("productsList")
                .addValueEventListener(productChildListener!!)
        }
    }

    fun removeProductListener() {
        if (rootref != null && productChildListener != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("productsList")
                .removeEventListener(
                    productChildListener!!
                )
            productChildListener = null
        }
    }

    fun initproductAdapter() {
        val linearLayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.productRecylerVeiw.layoutManager = linearLayoutManager
        binding.productRecylerVeiw.setHasFixedSize(true)

        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.productRecylerVeiw)


        productsAdapter = StreamingProductsAdapter(
            context!!,
            productsList!!,
            AdapterClickListener { view, pos, `object` ->
                val model = `object` as ProductModel
                val intent = Intent(this@MulticastStreamerActivity, ShopItemDetailA::class.java)
                intent.putExtra("data", model)
                startActivity(intent)
            })
        binding.productRecylerVeiw.adapter = productsAdapter
    }

    private fun showCameraRequest(requestedUserId: String) {
        val alertDialog = Dialog(context)
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setContentView(R.layout.camera_request_broadcast_view)
        alertDialog.window!!
            .setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.d_round_white_background
                )
            )

        val tabAccept = alertDialog.findViewById<RelativeLayout>(R.id.tabAccept)
        val tabReject = alertDialog.findViewById<RelativeLayout>(R.id.tabReject)
        val closeBtn = alertDialog.findViewById<ImageView>(R.id.closeBtn)

        closeBtn.setOnClickListener {
            alertDialog.dismiss()
            val duetConnectedUserMap = HashMap<String, Any>()
            duetConnectedUserMap["duetConnectedUserId"] = ""
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!)
                .updateChildren(duetConnectedUserMap).addOnCompleteListener { task ->
                    runOnUiThread {
                        if (task.isComplete) {
                            sendCameraRequest("0", requestedUserId)
                        }
                    }
                }
        }
        tabAccept.setOnClickListener {
            alertDialog.dismiss()
            val duetConnectedUserMap = HashMap<String, Any>()
            duetConnectedUserMap["duetConnectedUserId"] = requestedUserId
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!)
                .updateChildren(duetConnectedUserMap).addOnCompleteListener { task ->
                    runOnUiThread {
                        if (task.isComplete) {
                            sendCameraRequest("2", requestedUserId)
                        }
                    }
                }
        }
        tabReject.setOnClickListener {
            alertDialog.dismiss()
            val duetConnectedUserMap = HashMap<String, Any>()
            duetConnectedUserMap["duetConnectedUserId"] = ""
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!)
                .updateChildren(duetConnectedUserMap).addOnCompleteListener { task ->
                    runOnUiThread {
                        if (task.isComplete) {
                            sendCameraRequest("0", requestedUserId)
                        }
                    }
                }
        }
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun sendCameraRequest(type: String, requestedUserId: String) {
        val model = CameraRequestModel()
        model.requestState = type
        rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!)
            .child("CameraRequest")
            .child(requestedUserId)
            .setValue(model).addOnCompleteListener { task ->
                if (task.isComplete) {
                    runOnUiThread {
                        if (type == "2") {
                            Toast.makeText(
                                context,
                                context!!.getString(R.string.camera_request_accepted),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.camera_request_sended),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
    }

    fun ListCommentData() {
        current_cal = Calendar.getInstance()
        if (commentChildListener == null) {
            startAlertTimer()


            commentChildListener = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    val model = dataSnapshot.getValue(
                        LiveCommentModel::class.java
                    )
                    dataList.add(model!!)
                    if (checkTimeDiffernce(current_cal!!, model!!.commentTime!!)) {
                        if (model.type.equals("gift", ignoreCase = true)) {
                            this@MulticastStreamerActivity.runOnUiThread { ShowGiftAnimation(model) }
                        }
                    }

                    adapter!!.notifyDataSetChanged()
                    binding.recylerview.scrollToPosition(dataList.size - 1)
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("Chat")
                .addChildEventListener(commentChildListener!!)
        }
    }

    fun removeCommentListener() {
        if (rootref != null && commentChildListener != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("Chat")
                .removeEventListener(
                    commentChildListener!!
                )
            commentChildListener = null
        }
    }

    private fun setUpJoinRecycler() {
        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.orientation = RecyclerView.VERTICAL
        binding.liveUserViewRecyclerView.layoutManager = layoutManager
        liveUserViewAdapter = LiveUserViewAdapter(context, jointUserList) { view, pos, `object` ->
            val model = `object` as StreamJoinModel
            model?.userName?.let { Log.d(Constants.tag, it) }
        }
        binding.liveUserViewRecyclerView.adapter = liveUserViewAdapter
    }

    private fun ListenerJoinNode() {
        rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("JoinStream")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(Constants.tag, "Child Added: " + snapshot.value.toString())
                    if (!(TextUtils.isEmpty(snapshot.value.toString()))) {
                        val model = snapshot.getValue(StreamJoinModel::class.java)
                        jointUserList.add(model!!)
                        showjoinUser()
                        addJoinToQueue(model)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                }


                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Log.d(Constants.tag, "Child Removed: " + snapshot.value.toString())
                    val removedModel = snapshot.getValue(StreamJoinModel::class.java)
                    jointUserList.removeAll { it.userId == removedModel?.userId }
                    showjoinUser()
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(Constants.tag, "Child Moved: " + snapshot.value.toString())
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(Constants.tag, "Child Listener Cancelled: " + databaseError.message)
                }
            })
    }
    fun removeJoinListener() {
        if (rootref != null && joinValueEventListener != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("JoinStream")
                .removeEventListener(
                    joinValueEventListener!!
                )
            joinValueEventListener = null
        }
    }
    private val joinQueue = ArrayList<StreamJoinModel>()
    private var isAnimating = false

    fun addJoinToQueue(model: StreamJoinModel) {
        joinQueue.add(model)
        processNextJoinAnimation()
    }

    private fun processNextJoinAnimation() {
        if (isAnimating || joinQueue.isEmpty()) return

        isAnimating = true
        // Get the first element from the ArrayList (acting as a queue)
        val model = joinQueue[0]

        showJoinAnim(model) {
            // Remove the first element once its animation is complete
            joinQueue.removeAt(0)
            isAnimating = false
            processNextJoinAnimation()
        }
    }

    fun showJoinAnim(model: StreamJoinModel, onAnimationComplete: () -> Unit) {
        binding.joinAnimLay.visibility = View.VISIBLE
        binding.joinedusernameTxt.text = model.userName
        binding.joinCountTxt.text =  getSuffix("" + jointUserList.size)

        val screenWidth = resources.displayMetrics.widthPixels.toFloat()

        binding.joinAnimLay.translationX = screenWidth

        binding.joinAnimLay.animate()
            .translationX(0f)
            .setDuration(1500)
            .withEndAction {

                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    binding.joinAnimLay.animate()
                        .translationX(-screenWidth)
                        .setDuration(500)
                        .withEndAction {
                            binding.joinAnimLay.visibility = View.GONE
                            onAnimationComplete()
                        }
                        .start()
                }

            }
            .start()
    }

    fun showjoinUser() {
        when (jointUserList.size) {
            0 -> {
                binding.viewerImage1.visibility = View.GONE
                binding.viewerImage2.visibility = View.GONE
                binding.viewerImage3.visibility = View.GONE
            }

            1 -> {
                binding.viewerImage1.visibility = View.VISIBLE
                binding.viewerImage2.visibility = View.GONE
                binding.viewerImage3.visibility = View.GONE
                binding.viewerImage1.controller = frescoImageLoad(
                    jointUserList.get(0)!!.userPic, binding.viewerImage1, false
                )
            }

            2 -> {
                binding.viewerImage1.controller = frescoImageLoad(
                    jointUserList.get(0)!!.userPic, binding.viewerImage1, false
                )
                binding.viewerImage2.controller = frescoImageLoad(
                    jointUserList.get(1)!!.userPic, binding.viewerImage2, false
                )

                binding.viewerImage1.visibility = View.VISIBLE
                binding.viewerImage2.visibility = View.VISIBLE
                binding.viewerImage3.visibility = View.GONE
            }

            3 -> {
                binding.viewerImage1.controller = frescoImageLoad(
                    jointUserList.get(0)!!.userPic, binding.viewerImage1, false
                )
                binding.viewerImage2.controller = frescoImageLoad(
                    jointUserList.get(1)!!.userPic, binding.viewerImage2, false
                )
                binding.viewerImage3.controller = frescoImageLoad(
                    jointUserList.get(2)!!.userPic, binding.viewerImage3, false
                )

                binding.viewerImage1.visibility = View.VISIBLE
                binding.viewerImage2.visibility = View.VISIBLE
                binding.viewerImage3.visibility = View.VISIBLE
            }
        }
        liveUserViewAdapter!!.notifyDataSetChanged()
        binding.liveUserCount.text = getSuffix("" + jointUserList.size)
    }


    private fun addLikeStream() {
        if (likeValueEventListener == null) {
            likeValueEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        this@MulticastStreamerActivity.runOnUiThread {
                            heartCounter = heartCounter + 1
                            binding.tvOtherUserLikes.text = getSuffix("" + heartCounter)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("LikesStream")
                .addChildEventListener(likeValueEventListener!!)
        }
    }

    private fun ListenerCoinNode() {
        if(coinValueEventListener==null) {
            coinValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    senderCoinsList.clear()
                    if (dataSnapshot.exists()) {
                        for (joinSnapsot in dataSnapshot.children) {
                            if (!(TextUtils.isEmpty(joinSnapsot.value.toString()))) {
                                val model = joinSnapsot.getValue(
                                    LiveCoinsModel::class.java
                                )
                                senderCoinsList.add(model)
                            }
                        }
                        setCoinsCount()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child(StreamingConstants.coinsStream)
                .addValueEventListener(coinValueEventListener!!)
        }
    }

    fun setCoinsCount(){
        var maxCoins = 0
        for (item: LiveCoinsModel? in senderCoinsList) {
            maxCoins = maxCoins + (item!!.sendedCoins!!.toDouble()).toInt()
        }
        binding.tvCoinCount.text = "" + maxCoins
        if(model.setGoalStream!=null) {
            binding.totalGoalCount.text = "/" + model.setGoalStream?.goalAmount
            binding.receivedCoinsTxt.text =""+maxCoins
            val percentage = (maxCoins / model.setGoalStream?.goalAmount!!.toInt()) * 100
            binding.goalPercentageBar.progress = percentage
            binding.goalPercentageTxt.text="$percentage%"
        }

    }

    fun removeCoinListener() {
        if (rootref != null && coinValueEventListener != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child(StreamingConstants.coinsStream)
                .removeEventListener(
                    coinValueEventListener!!
                )
        }
    }



    private fun initData() {
        mVideoDimension = StreamingConstants.VIDEO_DIMENSIONS[config().videoDimenIndex]
    }

    private fun startBroadcast() {
        rtcEngine()?.setClientRole(io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER)
        val surface = prepareRtcVideo(config().uid!!.toInt(), true)
        binding.liveVideoGridLayout.addUserVideoSurface(config().uid!!.toInt(), surface, true)
    }

    private fun stopBroadcast() {
        rtcEngine()?.setClientRole(io.agora.rtc2.Constants.CLIENT_ROLE_AUDIENCE)
        removeRtcVideo(config().uid!!.toInt(), true)
        binding.liveVideoGridLayout.removeUserVideo(config().uid!!.toInt(), true)
    }

    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
        val map = HashMap<String, Any>()
        map["streamUid"] = uid
        rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).updateChildren(map)
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        runOnUiThread {
            printLog(Constants.tag, "onUserJoined$uid")
            renderRemoteUser(uid)
        }
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        runOnUiThread {
            printLog(Constants.tag, "onUserOffline$uid")
            if (pkInvitation != null) {
                if (pkInvitation!!.senderId == "" + uid || pkInvitation!!.receiverId == "" + uid) {
                    removeRemoteUser(uid)
                }
            } else {
                removeRemoteUser(uid)
            }
        }
    }

    override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
        runOnUiThread { renderRemoteUser(uid) }
    }

    private fun renderRemoteUser(uid: Int) {
        val surface = prepareRtcVideo(uid, false)
        binding.liveVideoGridLayout.addUserVideoSurface(uid, surface, false)
    }

    private fun removeRemoteUser(uid: Int) {
        removeRtcVideo(uid, false)
        binding.liveVideoGridLayout.removeUserVideo(uid, false)
        stopPkBattleStreaming()
    }

    override fun onLocalVideoStats(stats: LocalVideoStats) {
        if (!statsManager().isEnabled) return

        val data = statsManager().getStatsData(config().uid!!.toInt()) as LocalStatsData
            ?: return

        data.width = mVideoDimension!!.width
        data.height = mVideoDimension!!.height
        data.framerate = stats.sentFrameRate
    }

    override fun onRtcStats(stats: RtcStats) {
        if (streamingId != "" && (streamingId != null)) {
            Paper.book("MyLiveStreaming")
                .write(streamingId!!, getCurrentDate("yyyy-MM-dd HH:mm:ss"))
        }

        if (!statsManager().isEnabled) return
        val data = statsManager().getStatsData(config().uid!!.toInt()) as LocalStatsData
            ?: return

        data.lastMileDelay = stats.lastmileDelay
        data.videoSendBitrate = stats.txVideoKBitRate
        data.videoRecvBitrate = stats.rxVideoKBitRate
        data.audioSendBitrate = stats.txAudioKBitRate
        data.audioRecvBitrate = stats.rxAudioKBitRate
        data.cpuApp = stats.cpuAppUsage
        data.cpuTotal = stats.cpuAppUsage
        data.sendLoss = stats.txPacketLossRate
        data.recvLoss = stats.rxPacketLossRate
    }

    // check the network quality
    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
        if (!statsManager().isEnabled) return

        printLog(Constants.tag, "onNetworkQuality:$txQuality:$rxQuality")
        val data = statsManager().getStatsData(uid) ?: return

        data.sendQuality = statsManager().qualityToString(txQuality)
        data.recvQuality = statsManager().qualityToString(rxQuality)
    }

    override fun onRemoteVideoStats(stats: RemoteVideoStats) {
        if (!statsManager().isEnabled) return

        val data = statsManager().getStatsData(stats.uid) as RemoteStatsData ?: return

        data.width = stats.width
        data.height = stats.height
        data.framerate = stats.rendererOutputFrameRate
        data.videoDelay = stats.delay
    }

    override fun onRemoteAudioStats(stats: RemoteAudioStats) {
        if (!statsManager().isEnabled) return

        val data = statsManager().getStatsData(stats.uid) as RemoteStatsData ?: return

        data.audioNetDelay = stats.networkTransportDelay
        data.audioNetJitter = stats.jitterBufferDelay
        data.audioLoss = stats.audioLossRate
        data.audioQuality = statsManager().qualityToString(stats.quality)
    }

    override fun finish() {
        super.finish()
        statsManager().clearAllData()
    }

    override fun onDestroy() {
        super.onDestroy()

        model.userPicture?.let { it1 -> deleteImageFromFirebase(it1) }


        if (alertTimer != null) {
            alertTimer!!.cancel()
        }

        broadcastRemoveListener()
        removeNode()
        removeNodeCameraRequest()
        removeStreamInternetConnection()

        removePkInvitationListener()
        removeCoinListener()
        removeLikeStream()
        removeJoinListener()
        removeCommentListener()
        removeProductListener()
    }

    fun removeLikeStream() {
        if (rootref != null && likeValueEventListener != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("LikesStream")
                .removeEventListener(
                    likeValueEventListener!!
                )
            likeValueEventListener = null
        }
    }

    fun addFirebaseNode() {
        model?.setUserCoins("0")
        model?.setIsVerified(
            getSharedPreference(this@MulticastStreamerActivity).getInt(
                Variables.IS_VERIFIED,
                0
            )
        )
        model?.setDuetConnectedUserId("")
        model?.streamUid = -1
        rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).setValue(model)
    }


    fun startAlertTimer() {
        addMessages("alert", getString(R.string.streaming_welcome_1))
        alertTimer = object : CountDownTimer(20000, 5000) {
            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished >= 10000 && millisUntilFinished < 15000) {
                    addMessages("alert", getString(R.string.streaming_welcome_2))
                }
            }

            override fun onFinish() {
                if (binding.notifyFollowerLayout.visibility == View.VISIBLE) {
                    addMessages("alert", getString(R.string.streaming_welcome_3))
                }
            }
        }.start()
    }

    // when user goes to offline then change the value status on firebase
    fun removeNode() {
        if (pkInvitation != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(pkInvitation!!.senderStreamingId!!)
                .child("pkInvitation").removeValue()
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(pkInvitation!!.receiverStreamingId!!)
                .child("pkInvitation").removeValue()
        }
        rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).removeValue()
    }

    fun broadcasterlistenerNode() {
        Handler(Looper.getMainLooper()).postDelayed({
            broadcastValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                       val liveModel = dataSnapshot.getValue(LiveUserModel::class.java)
                        liveModel.let {
                            model=it!!


                            if(wishListAdapter!=null){
                                wishDataList.clear()
                                wishDataList.addAll(model.GiftWishList!!)
                                wishListAdapter?.notifyDataSetChanged()
                            }

                        }
                    } else {
                        this@MulticastStreamerActivity.runOnUiThread {
                            Toast.makeText(
                                context,
                                context!!.getString(R.string.your_live_channel_is_close),
                                Toast.LENGTH_SHORT
                            ).show()
                            onBackPressed()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!)
                .addValueEventListener(broadcastValueEventListener!!)
        }, 5000)
    }

    fun broadcastRemoveListener() {
        if (rootref != null && broadcastValueEventListener != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).removeEventListener(
                broadcastValueEventListener!!
            )
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tabShareStream -> {
                inviteFriendsForStream()
            }

            R.id.ivMainProfile->{
                openMyProfile()
            }
            R.id.viewerImage1->{
                openOtherProfile(0)
            }
            R.id.viewerImage2->{
                openOtherProfile(1)
            }
            R.id.viewerImage3->{
                openOtherProfile(2)
            }


            R.id.tabEffects -> {
                isbeautyActivated=!isbeautyActivated
                rtcEngine()?.setBeautyEffectOptions(isbeautyActivated, StreamingConstants.DEFAULT_BEAUTY_OPTIONS)

            }


            R.id.cross_btn -> {
                showEndLiveDialog()
            }

            R.id.cross_btn2 -> {
                if (pkInvitation != null) {
                    removePkBattle()
                } else {
                    showEndLiveDialog()
                }
            }


            R.id.notifyBtn -> {
                binding.notifyFollowerLayout.visibility = View.GONE
                sendLiveNotification()
            }

            R.id.notifyCrossBtn -> {
                binding.notifyFollowerLayout.visibility = View.GONE
            }

            R.id.goalLayout -> {
                showGoalDetail()
            }
            R.id.tvMessage -> {
                sendComment()
            }

            R.id.ivVideoRequest -> {
                if (model!!.getDuetConnectedUserId() != null
                    && !(TextUtils.isEmpty(model!!.getDuetConnectedUserId()))
                ) {
                    showCameraRequest(model!!.getDuetConnectedUserId())
                } else {
                    Toast.makeText(
                        context,
                        context!!.getString(R.string.no_user_connected),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }



            R.id.tabpk -> {
                openPkBattelInviteF()
            }
            R.id.tabmoreStream ->{
                openMoreStreamingOption()
            }

        }
    }

    fun openMyProfile() {
        val f = ShowProfileBottomF.newInstance(model!!)
        f.show(supportFragmentManager, "ShowProfileBottomF")
    }

    fun openOtherProfile(postion:Int) {
        val f = ShowOtherProfileBottomF.newInstance(jointUserList.get(postion))
        f.show(supportFragmentManager, "ShowOtherProfileBottomF")
    }

    fun openMoreStreamingOption() {
        val f = StreamerOptionsBottomSheet.newInstance("", { bundle ->
            if (bundle != null) {
                val type = bundle.getString("type")
                when(type){
                    "comment"->{
                        sendComment()
                    }
                    "flipCamera"->{
                        rtcEngine()?.switchCamera()
                    }
                    "muteStreaming"->{
                        isAudioMute = !isAudioMute
                        Functions.printLog(Constants.tag,"isAudioMute$isAudioMute")
                        rtcEngine()?.muteLocalAudioStream(isAudioMute)
                    }
                    "pauseLive"->{
                        isVideoActivated = !isVideoActivated
                        if (!isVideoActivated) {
                            stopBroadcast()
                        } else {
                            startBroadcast()
                        }
                    }
                    "joinInvitation" -> {
                        updateJoinInvitationStatus()
                    }

                }
            }
        },isAudioMute,isVideoActivated,model!!.isStreamJoinAllow)
        f.show(supportFragmentManager, "StreamerOptionsBottomSheet")
    }


    fun showGoalDetail() {
        val f = GoalDetailF.newInstance(model!!)
        f.show(supportFragmentManager, "GoalDetailF")
    }

    fun addPkInvitationListener() {
        if (pkInvitationListener == null) {
            pkInvitationListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.d(Constants.tag, snapshot.toString())

                        pkInvitation = snapshot.getValue(PkInvitation::class.java)

                        if (pkInvitation!!.action != null && pkInvitation!!.action == "1") {
                            if (!config().channelName.equals(
                                    pkInvitation!!.pkStreamingId,
                                    ignoreCase = true
                                )
                            ) {
                                acceptPkBattleInvitation()
                                hideInvitationViews()
                            }

                            if (model.userId.equals(pkInvitation!!.senderId, ignoreCase = true)) {
                                binding.coinCount1Txt.text = "" + pkInvitation!!.senderCoins
                                binding.coinCount2Txt.text = "" + pkInvitation!!.receiverCoins
                            }

                            if (model.userId.equals(pkInvitation!!.receiverId, ignoreCase = true)) {
                                binding.coinCount1Txt.text = "" + pkInvitation!!.receiverCoins
                                binding.coinCount2Txt.text = "" + pkInvitation!!.senderCoins
                            }

                            updatePkBar()
                        } else if (pkInvitation!!.receiverId != null && pkInvitation!!.receiverId == model.userId) {
                            showPkInvitationDialog()
                        }
                    } else {
                        pkInvitation = null
                        hideInvitationViews()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("pkInvitation")
                .addValueEventListener(pkInvitationListener!!)
        }
    }

    fun updatePkBar() {
        if (model?.userId.equals(pkInvitation!!.senderId, ignoreCase = true)) {
            val user1Coins = pkInvitation!!.senderCoins
            val user2Coins = pkInvitation!!.receiverCoins

            val total = user1Coins + user2Coins
            if (total > 0) {
                val percentage1 = ((user1Coins * 100) / total)
                val percentage2 = 100 - percentage1

                printLog(Constants.tag, "$percentage1--$percentage2")

                binding.pkProgressbar.setFirstSectionPercentage(percentage1)
                binding.pkProgressbar.setSecondSectionPercentage(percentage2)
            }
            else {
                binding.pkProgressbar.setFirstSectionPercentage(50)
                binding.pkProgressbar.setSecondSectionPercentage(50)
            }
        }
        else if (model?.userId.equals(pkInvitation!!.receiverId, ignoreCase = true)) {
            val user1Coins = pkInvitation!!.receiverCoins
            val user2Coins = pkInvitation!!.senderCoins

            val total = user1Coins + user2Coins
            if (total > 0) {
                val percentage1 = ((user1Coins * 100) / total)
                val percentage2 = 100 - percentage1

                printLog(Constants.tag, "$percentage1--$percentage2")

                binding.pkProgressbar.setFirstSectionPercentage(percentage1)
                binding.pkProgressbar.setSecondSectionPercentage(percentage2)
            } else {
                binding.pkProgressbar.setFirstSectionPercentage(50)
                binding.pkProgressbar.setSecondSectionPercentage(50)
            }
        }
    }

    fun removePkInvitationListener() {
        if (pkInvitationListener != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("pkInvitation")
                .removeEventListener(
                    pkInvitationListener!!
                )
        }
    }

    fun openPkBattelInviteF() {
        val f = PkBattleInviteFragment.newInstance { bundle ->
            if (bundle != null) {
                val liveUserModel = bundle.getParcelable<LiveUserModel>("data")
                rootref!!.child(StreamingConstants.liveStreamingUsers).child(liveUserModel!!.streamingId!!)
                    .child("pkInvitation")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                val map = HashMap<String, Any?>()
                                map["senderId"] = model.userId
                                map["senderName"] = model.userName
                                map["senderPic"] = model.userPicture
                                map["senderStreamingId"] = streamingId

                                map["receiverStreamingId"] = liveUserModel.getStreamingId()
                                map["receiverId"] = liveUserModel.getUserId()
                                map["receiverName"] = liveUserModel.getUserName()
                                map["receiverPic"] = liveUserModel.getUserPicture()

                                map["action"] = "0"
                                map["senderCoins"] = 0
                                map["receiverCoins"] = 0
                                rootref!!.child(StreamingConstants.liveStreamingUsers)
                                    .child(liveUserModel.streamingId!!).child("pkInvitation")
                                    .setValue(map)
                                rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!)
                                    .child("pkInvitation").setValue(map)
                                openPkBattelInviteSendF(liveUserModel)
                            } else {
                                showToast(
                                    this@MulticastStreamerActivity,
                                    "The host is busy now. please try again later!"
                                )
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            }
        }
        f.show(supportFragmentManager, "PkBattleInviteF")
    }

    fun openPkBattelInviteSendF(liveUserModel: LiveUserModel?) {
        pkBattleInviteSendF = PkBattleInviteSendFragment.newInstance(streamingId, liveUserModel) { }
        pkBattleInviteSendF!!.show(supportFragmentManager, "PkBattleInviteSendF")
    }

    fun showPkInvitationDialog() {
        pkInvitationDialog = Dialog(this@MulticastStreamerActivity)
        pkInvitationDialog!!.setCancelable(false)
        pkInvitationDialog!!.setContentView(R.layout.show_double_button_new_popup_dialog)
        pkInvitationDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvtitle = pkInvitationDialog!!.findViewById<TextView>(R.id.tvtitle)
        val tvMessage = pkInvitationDialog!!.findViewById<TextView>(R.id.tvMessage)
        val tvNegative = pkInvitationDialog!!.findViewById<TextView>(R.id.tvNegative)
        val tvPositive = pkInvitationDialog!!.findViewById<TextView>(R.id.tvPositive)


        tvtitle.text = "PK Invitation"
        tvMessage.text = pkInvitation!!.senderName + " invite you to PK"
        tvNegative.text = "Reject"

        if (pkInvitationTimer != null) pkInvitationTimer!!.cancel()

        pkInvitationTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(l: Long) {
                tvPositive.text = "Accept(" + (l / 1000).toInt() + ")"
            }

            override fun onFinish() {
                pkInvitationDialog!!.dismiss()
                rootref!!.child(StreamingConstants.liveStreamingUsers).child(pkInvitation!!.senderStreamingId!!)
                    .child("pkInvitation").removeValue()
                rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("pkInvitation")
                    .removeValue()
            }
        }
        pkInvitationTimer!!.start()

        tvNegative.setOnClickListener {
            pkInvitationDialog!!.dismiss()
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(pkInvitation!!.senderStreamingId!!)
                .child("pkInvitation").removeValue()
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("pkInvitation")
                .removeValue()
        }
        tvPositive.setOnClickListener {
            pkInvitationDialog!!.dismiss()
            val pkStreamingID = pkInvitation!!.senderStreamingId + "PK" + streamingId

            val c = Calendar.getInstance().time
            val formattedDate = Variables.df.format(c)


            val map = HashMap<String, Any>()
            map["action"] = "1"
            map["pkStreamingId"] = pkStreamingID
            map["pkStreamingTime"] = formattedDate
            map["timeStamp"] = ServerValue.TIMESTAMP

            rootref!!.child(StreamingConstants.liveStreamingUsers).child(pkInvitation!!.senderStreamingId!!)
                .child("pkInvitation").updateChildren(map)
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("pkInvitation")
                .updateChildren(map)
        }

        pkInvitationDialog!!.setOnDismissListener { if (pkInvitationTimer != null) pkInvitationTimer!!.cancel() }
        pkInvitationDialog!!.show()
    }


    fun acceptPkBattleInvitation() {
        rtcEngine()?.leaveChannel()
        config().uid = getSharedPreference(this).getString(Variables.U_ID, "")
        config().channelName = pkInvitation!!.pkStreamingId
        rtcEngine()?.joinChannel(null, config().channelName, "", config().uid!!.toInt())

        startPkCountDown()
        CoroutineScope(Dispatchers.Main).launch {
            delay(2500)
            showPkAnimation()
        }
    }

    fun hideInvitationViews() {
        if (pkBattleInviteSendF != null) {
            pkBattleInviteSendF!!.dismiss()
        }

        if (pkInvitationDialog != null) {
            pkInvitationDialog!!.dismiss()
        }
    }

    fun showPkAnimation() {
        binding.pkgif.visibility = View.VISIBLE
        val controllerListener: ControllerListener<ImageInfo> =
            object : BaseControllerListener<ImageInfo>() {
                override fun onFinalImageSet(
                    id: String,
                    imageInfo: ImageInfo?,
                    anim: Animatable?
                ) {
                    if (anim != null) {
                        anim.start()
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1800)
                            binding.pkgif.visibility = View.GONE
                        }
                        printLog(Constants.tag, "onFinalImageSet")
                    }
                }
            }

        val request = ImageRequestBuilder.newBuilderWithResourceId(R.raw.ic_pk_battle2)
            .build()
        val controller: DraweeController = Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .setAutoPlayAnimations(false)
            .setOldController(binding.pkgif.controller)
            .setControllerListener(controllerListener!!)
            .build()

        binding.pkgif.controller = controller
    }

    fun removePkBattle() {
        if (pkInvitation != null) {
            if (model.userId.equals(
                    getSharedPreference(this).getString(Variables.U_ID, ""),
                    ignoreCase = true
                )
            ) {
                if (model.userId.equals(pkInvitation!!.senderId, ignoreCase = true)) {
                    removeRemoteUser(pkInvitation!!.receiverId!!.toInt())
                } else {
                    removeRemoteUser(pkInvitation!!.senderId!!.toInt())
                }
            } else {
                removeRemoteUser(model.userId!!.toInt())
            }
        }
    }

    fun stopPkBattleStreaming() {
        rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("pkInvitation")
            .removeValue()
        rtcEngine()?.leaveChannel()
        config().uid = getSharedPreference(this)
            .getString(Variables.U_ID, "")
        config().channelName = streamingId
        rtcEngine()?.joinChannel(null, config().channelName, "", config().uid!!.toInt())

        stopPkCountDown()
        hidePkBattleViews()
    }

    fun showPkBattleViews() {
        binding.pkProgressLayout.visibility = View.VISIBLE
        binding.pkbattleTimerLayout.visibility = View.VISIBLE
        binding.tabpk.isEnabled = false
    }

    fun hidePkBattleViews() {
        binding.pkProgressLayout.visibility = View.GONE
        binding.pkbattleTimerLayout.visibility = View.GONE
        binding.tabpk.isEnabled = true
    }

    fun startPkCountDown() {
        showPkBattleViews()

        if (pkBattleCountDown == null) {
            rootref!!.child(Variables.onlineUser).child(getSharedPreference(applicationContext).getString(Variables.U_ID, "0")!!).child("timeStamp").setValue(ServerValue.TIMESTAMP)

            rootref!!.child(Variables.onlineUser).child(
            getSharedPreference(applicationContext).getString(Variables.U_ID, "0")!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val value = snapshot.child("timeStamp").value as Long
                        val dif = value - pkInvitation!!.timeStamp
                        val timeDiffernce = Constants.PkBattleTime - dif

                        pkBattleCountDown = object : CountDownTimer(timeDiffernce, 1000) {
                            override fun onTick(l: Long) {
                                binding.pkTimerTxt.text = getDate(l, "mm:ss")
                            }

                            override fun onFinish() {
                                hidePkBattleViews()
                                if (pkInvitation != null) {
                                    showWinningGif()
                                }
                            }
                        }
                        pkBattleCountDown!!.start()

                        printLog(Constants.tag, "difference...$value")
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }

    fun stopPkCountDown() {
        if (pkBattleCountDown != null) {
            pkBattleCountDown!!.cancel()
            pkBattleCountDown = null
        }
    }

    fun showPkWinnerDialog() {
        var winnerUsername: String? = null
        var coins = 0
        val user1Coins = pkInvitation!!.senderCoins
        val user2Coins = pkInvitation!!.receiverCoins

        if (user1Coins > user2Coins) {
            winnerUsername = pkInvitation!!.senderName
            coins = user1Coins
        } else if (user2Coins > user1Coins) {
            winnerUsername = pkInvitation!!.receiverName
            coins = user2Coins
        }

        if (winnerUsername != null) {
            val dialog = Dialog(this@MulticastStreamerActivity)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.item_pk_winner_dialog)
            dialog.window!!.setBackgroundDrawable(getDrawable(R.drawable.d_round_white_background))

            val usernametxt = dialog.findViewById<TextView>(R.id.usernametxt)
            usernametxt.text = winnerUsername

            val cointxt = dialog.findViewById<TextView>(R.id.coinTxt)
            cointxt.text = "" + coins


            dialog.setOnDismissListener { removePkBattle() }
            dialog.show()
        } else {
            removePkBattle()
        }
    }

    private var animationJob:Job? = null
    private fun showWinningGif() {
        val user1Coins = pkInvitation!!.senderCoins
        val user2Coins = pkInvitation!!.receiverCoins

        var winningSide = "0"

        if (user1Coins > user2Coins) {
            winningSide = if (pkInvitation!!.senderId.equals(
                    getSharedPreference(
                        this
                    ).getString(Variables.U_ID, ""), ignoreCase = true
                )
            ) {
                "1"
            } else {
                "2"
            }
        } else if (user2Coins > user1Coins) {
            winningSide = if (pkInvitation!!.receiverId.equals(
                    getSharedPreference(
                        this
                    ).getString(Variables.U_ID, ""), ignoreCase = true
                )
            ) {
                "1"
            } else {
                "2"
            }
        }

        if (winningSide == "1") {
            binding.winningLayout.visibility = View.VISIBLE
            val request = ImageRequestBuilder.newBuilderWithResourceId(R.raw.ic_winning_stars)
                .build()
            val controller: DraweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(binding.winningGif1.controller)
                .setAutoPlayAnimations(true)
                .build()

            binding.winningGif1.controller = controller
        } else if (winningSide == "2") {
            binding.winningLayout.visibility = View.VISIBLE

            val request = ImageRequestBuilder.newBuilderWithResourceId(R.raw.ic_winning_stars)
                .build()
            val controller: DraweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(binding.winningGif2.controller)
                .setAutoPlayAnimations(true)
                .build()

            binding.winningGif2.controller = controller
        }

        if(animationJob?.isActive == true) return
        animationJob= CoroutineScope(Dispatchers.Main).launch {
            delay(5000)
            try {
                binding.winningLayout.visibility = View.GONE
                showPkWinnerDialog()
            } catch (e: Exception) {
            }
        }
    }

    private fun updateJoinInvitationStatus() {
        val mapData = HashMap<String, Any>()
        if (model != null) {
            if (model!!.isStreamJoinAllow) {
                mapData["streamJoinAllow"] = false
            } else {
                mapData["streamJoinAllow"] = true
            }

            showLoader(this, false, false)
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId())
                .updateChildren(mapData).addOnCompleteListener { task ->
                runOnUiThread {
                    if (task.isComplete) {
                        cancelLoader()
                    }
                }
            }
        }
    }

    private fun inviteFriendsForStream() {
        val f = InviteContactsToStreamFragment(streamingId, "multiple") { bundle ->
            if (bundle.getBoolean("isShow", false)) {
            }
        }
        f.show(supportFragmentManager, "InviteContactsToStreamF")
    }

    private fun sendComment() {
        val fragment = EditTextSheetFragment(EditTextSheetFragment.commentTypeOwn, taggedUserList!!) { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                if (bundle.getString("action") == "sendComment") {
                    taggedUserList =
                        bundle.getSerializable("taggedUserList") as ArrayList<UserModel>?
                    val message = bundle.getString("message")
                    addMessages("comment", message)
                }
            }
        }
        val bundle = Bundle()
        bundle.putString("replyStr", "")
        fragment.arguments = bundle
        fragment.show(supportFragmentManager, "EditTextSheetF")
    }

    private fun addStreamInternetConnection() {
        if (connectCheckListener == null) {
            connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")

            connectCheckListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java)!!
                    if (connected) {
                        Log.d(Constants.tag, "connected")
                        timer.cancel()
                    } else {
                        Log.d(Constants.tag, "not connected")
                        timer.cancel()
                        timer = Timer()
                        timer.schedule(
                            object : TimerTask() {
                                override fun run() {
                                    runOnUiThread { onBackPressed() }
                                }
                            },
                            DELAY
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(Constants.tag, "Listener was cancelled")
                }
            }
            connectedRef!!.addValueEventListener(connectCheckListener!!)
        }
    }

    fun removeStreamInternetConnection() {
        if (connectedRef != null && connectCheckListener != null) {
            connectedRef!!.removeEventListener(connectCheckListener!!)
        }
    }

    fun ShowGiftAnimation(item: LiveCommentModel?) {
        printLog(Constants.tag, "ShowGiftAnimation(LiveCommentModel item)")

        binding.ivGiftProfile.controller = frescoImageLoad(
            item?.userPicture, binding.ivGiftProfile, false
        )

        binding.ivGiftItem.controller =
            frescoImageLoad("" + item?.giftPic, binding.ivGiftItem, false)
        binding.tvGiftTitle.text = item?.userName
        binding.tvGiftCountTitle.text = getString(R.string.gave_you_a) + " " + item?.giftName
        binding.tvSendGiftCount.text = "X " + item?.giftCount

        binding.tabGiftMain.animate().alpha(1f).translationX(binding.animationGiftCapture.x)
            .setDuration(3000).setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    binding.tabGiftMain.animate().translationY(binding.animationCapture.y)
                        .setDuration(1000).setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            binding.tabGiftMain.clearAnimation()
                            binding.tabGiftMain.animate().alpha(0f)
                                .translationY(binding.animationResetAnimation.y).translationX(
                                binding.animationResetAnimation.x
                            ).setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    super.onAnimationEnd(animation)
                                    binding.tabGiftMain.clearAnimation()
                                }
                            }).start()
                        }
                    }).start()
                }


                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    PlayGiftSound()
                }
            }).start()


        showGiftAnimation(item?.giftPic!!, item)

    }

    fun showGiftAnimation(gifUrl: String?, item: LiveCommentModel?) {

        val file = File(FileUtils.getAppFolder(appLevelContext!!) + Variables.APP_Gifts_Folder + item?.giftId!!+".mp4")
        if (file.exists()) {
            val animationViewF = AnimationViewF.newInstance(item?.giftId!!.toString())
            animationViewF.show(supportFragmentManager, "animationViewF")
        }else {
            if(gifUrl?.contains(".mp4") == true){
                CoroutineScope(Dispatchers.IO).launch{
                    val outputDirectory=File(FileUtils.getAppFolder(appLevelContext!!) + Variables.APP_Gifts_Folder)
                    val file= DownloadFiles.downloadFileWithProgress(gifUrl.toString(),
                        item?.giftId.toString(),
                        "mp4",
                        outputDirectory,
                        progressCallback = { bytesRead, contentLength ->

                        })
                    printLog(com.coheser.app.Constants.tag,"downloaded file:"+file?.absolutePath)
                    if(file?.exists() == true) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val animationViewF =
                                AnimationViewF.newInstance(item?.giftId.toString())
                            animationViewF.show(supportFragmentManager, "animationViewF")
                        }

                    }
                }
            }
            Dialogs.showGiftDailog(this, item.giftIcon)
        }


    }

    private fun PlayGiftSound() {
        player = MediaPlayer.create(applicationContext, R.raw.gift_tone)
        player!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player!!.setVolume(100f, 100f)
        player!!.setOnPreparedListener(OnPreparedListener { mp -> mp.start() })
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            if (player != null && player!!.isPlaying) {
                player!!.stop()
            }
        }
    }


    // send the comment to the live user
    fun addMessages(type: String?, message: String?) {
        val key = rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("Chat").push().key
        val my_id = getSharedPreference(this).getString(Variables.U_ID, "")
        val my_name = getSharedPreference(this).getString(Variables.U_NAME, "")
        val my_image = getSharedPreference(this).getString(Variables.U_PIC, "")

        val c = Calendar.getInstance().time
        val formattedDate = Variables.df.format(c)

        val commentItem = LiveCommentModel()
        commentItem.key = key
        commentItem.userId = my_id
        commentItem.userName = my_name
        commentItem.userPicture = my_image
        commentItem.comment = message
        commentItem.type = type
        commentItem.giftCount=""
        commentItem.giftPic=""
        commentItem.commentTime = formattedDate
        rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("Chat").child(key!!)
            .setValue(commentItem)
    }


    fun showEndLiveDialog(){
        Dialogs.showDoubleButtonAlert(this,
            null,
            getString(R.string.longer_live_videos_may_reach_more_viewers_end_the_live_video),
            getString(R.string.end_now),
            getString(R.string.cancel_),
            true,
            object:FragmentCallBack{
            override fun onResponce(bundle: Bundle?) {
             if(bundle?.getBoolean("isShow",false)==false){
                 onBackPressed()
             }
            }
        });
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if(streamingOnTime>0) {
            val intent = Intent(this@MulticastStreamerActivity, EndStreamingStatsA::class.java)
            intent.putExtra("likeCount", "" + heartCounter)
            intent.putExtra("viewersCount", "" + jointUserList.size)
            intent.putExtra("commentsCount", "" + dataList.size)
            intent.putExtra("wishList", wishDataList)
            intent.putExtra("joinTime", streamingOnTime)
            intent.putParcelableArrayListExtra("senderCoinsList", senderCoinsList)
            startActivity(intent)
        }
        finish()
    }

    // send notification to all of it follower when user live
    fun sendLiveNotification() {
        val params = JSONObject()
        try {
            params.put("user_id", getSharedPreference(this).getString(Variables.U_ID, ""))
            params.put("live_streaming_id", streamingId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this@MulticastStreamerActivity,
            ApiLinks.sendLiveStreamPushNotfication,
            params,
            getHeaders(context)
        ) { }
    }

    private fun addNodeCameraRequest() {
        if (cameraRequestEventListener == null) {
            cameraRequestEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        runOnUiThread {
                            if (model!!.getDuetConnectedUserId() != null && !(TextUtils.isEmpty(
                                    model!!.getDuetConnectedUserId()
                                ))
                            ) {
                                val model = snapshot.child(
                                    model!!.getDuetConnectedUserId()
                                ).getValue(
                                    CameraRequestModel::class.java
                                )
                                if (model!!.requestState == "1") {
                                    binding.ivVideoRequest.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context!!, R.drawable.ic_camera_request_r
                                        )
                                    )
                                    binding.ivVideoRequest.visibility = View.VISIBLE

                                    if (this@MulticastStreamerActivity.model!!.getDuetConnectedUserId() != null
                                        && !(TextUtils.isEmpty(this@MulticastStreamerActivity.model!!.getDuetConnectedUserId()))
                                    ) {
                                        showCameraRequest(this@MulticastStreamerActivity.model!!.getDuetConnectedUserId())
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context!!.getString(R.string.no_user_connected),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else if (model.requestState == "2") {
                                    binding.ivVideoRequest.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context!!, R.drawable.ic_camera_request_a
                                        )
                                    )
                                    binding.ivVideoRequest.visibility = View.VISIBLE
                                } else {
                                    binding.ivVideoRequest.visibility = View.GONE
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("CameraRequest")
                .addValueEventListener(cameraRequestEventListener!!)
        }
    }

    private fun removeNodeCameraRequest() {
        if (rootref != null && cameraRequestEventListener != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("CameraRequest")
                .removeEventListener(
                    cameraRequestEventListener!!
                )
            cameraRequestEventListener = null
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    hideKeyboard(v)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}
