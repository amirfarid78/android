package com.coheser.app.activitesfragments.livestreaming.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Animatable
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.coheser.app.R
import com.coheser.app.activitesfragments.EditTextSheetFragment
import com.coheser.app.activitesfragments.accounts.AccountUtils
import com.coheser.app.activitesfragments.livestreaming.StreamingConstants
import com.coheser.app.activitesfragments.livestreaming.activities.MultiViewLiveActivity
import com.coheser.app.activitesfragments.livestreaming.adapter.LiveCommentsAdapter
import com.coheser.app.activitesfragments.livestreaming.adapter.LiveUserViewAdapter
import com.coheser.app.activitesfragments.livestreaming.adapter.WishListGiftAdapter
import com.coheser.app.activitesfragments.livestreaming.model.CameraRequestModel
import com.coheser.app.activitesfragments.livestreaming.model.GiftUsers
import com.coheser.app.activitesfragments.livestreaming.model.GiftWishListModel
import com.coheser.app.activitesfragments.livestreaming.model.LiveCoinsModel
import com.coheser.app.activitesfragments.livestreaming.model.LiveCommentModel
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.activitesfragments.livestreaming.model.PkInvitation
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.activitesfragments.profile.analytics.DateOperations.getDate
import com.coheser.app.activitesfragments.sendgift.StickerGiftFragment
import com.coheser.app.activitesfragments.sendgift.GiftModel
import com.coheser.app.activitesfragments.shoping.ShopItemDetailA
import com.coheser.app.activitesfragments.shoping.adapter.StreamingProductsAdapter
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentMultipleStreamerListBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.StreamJoinModel
import com.coheser.app.models.StreamShowHeartModel
import com.coheser.app.models.UserModel
import com.coheser.app.models.UserOnlineModel
import com.coheser.app.simpleclasses.ApiRepository.callApiForFollowUnFollow
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.DateOprations.checkTimeDiffernce
import com.coheser.app.simpleclasses.Dialogs.cancelIndeterminentLoader
import com.coheser.app.simpleclasses.Dialogs.showIndeterminentLoader
import com.coheser.app.simpleclasses.Functions.checkLoginUser
import com.coheser.app.simpleclasses.Functions.checkProfileOpenValidation
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.getSuffix
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.TicTicApp
import com.coheser.app.simpleclasses.Variables
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.controller.ControllerListener
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.coheser.app.simpleclasses.Dialogs
import com.coheser.app.simpleclasses.Downloading.DownloadFiles
import com.coheser.app.simpleclasses.FileUtils
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.OnSwipeTouchListener
import com.coheser.app.simpleclasses.TicTicApp.Companion.appLevelContext
import com.coheser.app.viewModels.StreamingViewerFactory
import com.coheser.app.viewModels.StreamingViewerViewModel
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.APICallBack
import com.volley.plus.interfaces.Callback
import io.agora.rtc2.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.Serializable
import java.util.Calendar
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class MultipleStreamerListFragment : Fragment, View.OnClickListener {

    private val DELAY: Long = 20000
    var model: LiveUserModel? = null
    var myActivity: MultiViewLiveActivity? = null
    var rootref: DatabaseReference? = null
    var isLikeStream: Boolean = true
    var liveUserViewAdapter: LiveUserViewAdapter? = null
    var selfInvitehandler: CountDownTimer? = null
    var selfInviteRemainingTime: Int = 0
    var isFirstTimeFlip: Boolean = true
    var isSendHeart: Boolean = true
    lateinit var binding: FragmentMultipleStreamerListBinding
    var productChildListener: ValueEventListener? = null

    // initailze the adapter
    var productsList: ArrayList<ProductModel> = ArrayList()
    var productsAdapter: StreamingProductsAdapter? = null
    var taggedUserList: ArrayList<UserModel>? = ArrayList()
    var commentChildListener: ChildEventListener? = null
    var current_cal: Calendar? = null

    // initailze the adapter
    var dataList: ArrayList<LiveCommentModel> = ArrayList()
    var adapter: LiveCommentsAdapter? = null
    var cameraRequestEventListener: ValueEventListener? = null
    var isCameraConnect: Boolean = false
    var jointUserList: ArrayList<StreamJoinModel> = ArrayList()
    var joinValueEventListener: ChildEventListener? = null
    var coinValueEventListener: ValueEventListener? = null
    var senderCoinsList: ArrayList<LiveCoinsModel?> = ArrayList()
    var pkBattleCountDown: CountDownTimer? = null
    var winningHandler: Handler? = null
    var winningRunnable: Runnable? = null
    var pkInvitation: PkInvitation? = null
    var pklistener: ValueEventListener? = null
    var isAudioActivated: Boolean = true
    var isVideoActivated: Boolean = true
    var isbeautyActivated: Boolean = true
    var audio: MediaPlayer? = null
    var player: MediaPlayer? = null
    var checkVisible: Boolean = false
    var userLiveStatusListener: ValueEventListener? = null
    var blockValueEventListener: ValueEventListener? = null
    var likeValueEventListener: ChildEventListener? = null
    var streamerOnlineListener: ChildEventListener? = null
    private var timer: Timer? = Timer()
    var userRole: Int = Constants.CLIENT_ROLE_AUDIENCE

    lateinit var viewModel: StreamingViewerViewModel

    constructor(item: LiveUserModel, activity: MultiViewLiveActivity?) {
        this.model = item
        pkInvitation = item.pkInvitation
        this.myActivity = activity
    }

    constructor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_multiple_streamer_list,
            container,
            false
        )

        val viewModelFactory= StreamingViewerFactory(requireContext())
        viewModel=ViewModelProvider(this,viewModelFactory)[StreamingViewerViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.getRoot()
    }


    private fun ActionControl() {
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
                Log.d(com.coheser.app.Constants.tag, "start")

                if (binding.viewTwo === binding.viewflliper.currentView) {
                    if (binding.hostListLayout.visibility==View.VISIBLE) {

                        ObjectAnimator.ofFloat(
                            binding.hostListLayout,
                            "translationX",
                            binding.mainLayout.width.toFloat()
                        ).apply {
                            duration = 600
                            start()
                        }
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.hostListLayout.visibility = View.GONE
                        }, 600)
                    }else {
                        binding.viewflliper.showPrevious()
                    }
                } else {
                    binding.viewflliper.showPrevious()
                }
            }

            override fun onSwipeLeft() {
                binding.viewflliper.inAnimation = inAnim
                binding.viewflliper.outAnimation = outAnim
                Log.d(com.coheser.app.Constants.tag, "end")
                if (binding.viewTwo === binding.viewflliper.currentView) {
                    if (binding.hostListLayout.visibility==View.GONE) {

                        binding.hostListLayout.visibility=View.VISIBLE
                        binding.hostListLayout.translationX=(binding.mainLayout.width).toFloat()
                        ObjectAnimator.ofFloat(binding.hostListLayout, "translationX", (binding.mainLayout.width-(Functions.convertDpToPx(requireActivity(),120))).toFloat()).apply {
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

            override fun onDoubleClick(e: MotionEvent) {
                if (isSendHeart) {
                    isSendHeart = true
                    addLikeIntoStream()
                }
            }

            override fun onSingleClick() {
            }
        })

        if (isFirstTimeFlip) {
            isFirstTimeFlip = false
            if (binding.viewOne === binding.viewflliper.currentView) {
                binding.viewflliper.showNext()
            }
        }

        binding.tabMenu.setOnClickListener(this)
        binding.tabGift.setOnClickListener(this)
        binding.exclusiveRechargeBtn.setOnClickListener(this)
        binding.tabInviteAll.setOnClickListener(this)
        binding.tabCoHost.setOnClickListener(this)

    }

    fun addProductListener() {
        if (productChildListener == null) {

            printLog(com.coheser.app.Constants.tag, "addProductListener")

            productChildListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    printLog(com.coheser.app.Constants.tag, dataSnapshot.toString())
                    productsList.clear()
                    if (dataSnapshot.exists()) {
                        binding.productRecylerVeiw.visibility = View.VISIBLE
                        for (product: DataSnapshot in dataSnapshot.children) {
                            val model = product.getValue(ProductModel::class.java)
                            model?.let { productsList.add(it) }
                        }
                        productsAdapter!!.notifyDataSetChanged()
                    } else {
                        binding.productRecylerVeiw.visibility = View.GONE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    printLog(com.coheser.app.Constants.tag, databaseError.toString())
                    binding.productRecylerVeiw.visibility = View.GONE
                }
            }

            rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId())
                .child("productsList").addValueEventListener(productChildListener!!)
        }
    }

    fun removeProductListener() {
        if (rootref != null && productChildListener != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId())
                .child("productsList").removeEventListener(
                productChildListener!!
            )
            productChildListener = null
        }
    }

    fun initproductAdapter() {
        if(productsAdapter==null) {
            val linearLayoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.productRecylerVeiw.layoutManager = linearLayoutManager
            binding.productRecylerVeiw.setHasFixedSize(true)

            val snapHelper: SnapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(binding.productRecylerVeiw)

            productsAdapter = StreamingProductsAdapter(
                requireContext(),
                productsList,
                AdapterClickListener { view, pos, `object` ->
                    val model = `object` as ProductModel
                    val intent = Intent(context, ShopItemDetailA::class.java)
                    intent.putExtra("data", model as Serializable)
                    startActivity(intent)
                })
            binding.productRecylerVeiw.adapter = productsAdapter
        }
    }


    private fun sendComment() {
        val fragment = EditTextSheetFragment("OwnComment", (taggedUserList)!!) { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                if ((bundle.getString("action") == "sendComment")) {
                    taggedUserList =
                        bundle.getSerializable("taggedUserList") as ArrayList<UserModel>?
                    val message = bundle.getString("message")
                    binding.tvMessage.text = message
                    addMessages("comment")
                }
            }
        }
        val bundle = Bundle()
        bundle.putString("replyStr", "")
        fragment.arguments = bundle
        fragment.show(childFragmentManager, "EditTextSheetF")
    }


    private fun addLikeIntoStream() {
        val likeData = StreamShowHeartModel()
        likeData.setUserId(getSharedPreference(context).getString(Variables.U_ID, ""))
        likeData.setOtherUserId(model!!.getUserId())
        rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId()).child("LikesStream")
            .push().setValue(likeData).addOnCompleteListener(
            OnCompleteListener { task ->
                if (task.isComplete) {
                    isSendHeart = true
                }
            })
    }

    private fun InitControl() {
        rootref = FirebaseDatabase.getInstance().reference

        binding.btnfollow.setOnClickListener(this)

        binding.tvMessage.setOnClickListener(this)
        binding.tabLikeStreaming.setOnClickListener(this)


        binding.crossBtn.setOnClickListener(this)
        binding.crossBtn2.setOnClickListener(this)

        setUpJoinRecycler()
        initCommentAdapter()
        setWishListAdapter()
        initproductAdapter()

        connectStreaming()
        checkUserStatus()

    }


    fun ListCommentData() {
        current_cal = Calendar.getInstance()
        if (commentChildListener == null) {
            commentChildListener = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    val model = dataSnapshot.getValue(
                        LiveCommentModel::class.java
                    )
                    if (!model!!.comment.equals(
                            getString(R.string.streaming_welcome_3),
                            ignoreCase = true
                        )
                    ) {
                        dataList.add(model)

                        if (checkTimeDiffernce(current_cal!!, (model.commentTime)!!)) {
                            if (model.type.equals("gift", ignoreCase = true)) {
                                try {
                                    requireActivity().runOnUiThread(object : Runnable {
                                        override fun run() {
                                            ShowGiftAnimation(model)
                                        }
                                    })
                                } catch (e: Exception) {
                                }
                            }
                        }

                        adapter?.notifyDataSetChanged()
                        binding.recylerview.scrollToPosition(dataList.size - 1)

                    }
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
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId()).child("Chat")
                .limitToLast(1).addChildEventListener(commentChildListener!!)
        }
    }


    fun removeCommentListener() {
        if (rootref != null && commentChildListener != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId()).child("Chat")
                .removeEventListener(
                    commentChildListener!!
                )
            commentChildListener = null
        }
    }

    fun initCommentAdapter() {
        dataList.clear()
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recylerview.layoutManager = linearLayoutManager
        binding.recylerview.setHasFixedSize(false)
        adapter = LiveCommentsAdapter(requireContext(), dataList, object : AdapterClickListener {
            override fun onItemClick(view: View, pos: Int, `object`: Any) {
                val itemUpdate = dataList[pos]
                if (view.id == R.id.profileImage) {
                    openProfile(itemUpdate!!.userId)
                } else if (view.id == R.id.username) {
                    openProfile(itemUpdate!!.userId)
                } else if ((itemUpdate!!.type == "shareStream")) {
                    inviteFriendsForStream()
                } else {
                    openProfile(itemUpdate.userId)
                }
            }
        })

        binding.recylerview.adapter = adapter
    }


    private var currentPage = 0
    var wishListAdapter: WishListGiftAdapter?=null
    var wishDataList: ArrayList<GiftWishListModel> = ArrayList()
    fun setWishListAdapter(){
        if(wishListAdapter==null) {
            wishDataList.clear()
            if (model?.GiftWishList != null) {
                wishDataList.addAll(model?.GiftWishList!!)
            }
            if (wishDataList.size > 0) {
                binding.wishListRecycler.visibility = View.VISIBLE
                val linearLayoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                binding.wishListRecycler.layoutManager = linearLayoutManager
                binding.wishListRecycler.setHasFixedSize(true)
                wishListAdapter =
                    WishListGiftAdapter(requireContext(), wishDataList) { view, pos, `object` ->

                        val f = WishListBottomF.newInstance(
                            model!!,
                            WishListBottomF.fromJoiner,
                            object : FragmentCallBack {
                                override fun onResponce(bundle: Bundle) {
                                    if (bundle.getBoolean("isShow", false)) {
                                        val model = bundle.getParcelable("Data") as GiftModel?
                                        val counter = bundle.getString("count")
                                        addGiftComment("gift", counter, model)

                                        if (this@MultipleStreamerListFragment.model != null) {
                                            val userGift = (counter!!.toLong() * model!!.coin!!)
                                            val map: HashMap<String, Any> = HashMap<String, Any>()
                                            map["userCoins"] = "" + userGift
                                            rootref!!.child(StreamingConstants.liveStreamingUsers)
                                                .child(this@MultipleStreamerListFragment.model!!.getStreamingId())
                                                .updateChildren(map)

                                            if (this@MultipleStreamerListFragment.model!!.pkInvitation != null && this@MultipleStreamerListFragment.model!!.pkInvitation!!.pkStreamingId != null) {
                                                val streamingIds =
                                                    this@MultipleStreamerListFragment.model!!.pkInvitation!!.pkStreamingId!!.split(
                                                        "PK".toRegex()
                                                    )
                                                        .dropLastWhile { it.isEmpty() }
                                                        .toTypedArray()

                                                if (this@MultipleStreamerListFragment.model!!.getUserId()
                                                        .equals(
                                                            this@MultipleStreamerListFragment.model!!.pkInvitation!!.senderId,
                                                            ignoreCase = true
                                                        )
                                                ) {
                                                    rootref!!.child(StreamingConstants.liveStreamingUsers)
                                                        .child(streamingIds[0])
                                                        .child("pkInvitation").child("senderCoins")
                                                        .setValue(
                                                            this@MultipleStreamerListFragment.model!!.pkInvitation!!.senderCoins + userGift
                                                        )
                                                    rootref!!.child(StreamingConstants.liveStreamingUsers)
                                                        .child(streamingIds[1])
                                                        .child("pkInvitation").child("senderCoins")
                                                        .setValue(
                                                            this@MultipleStreamerListFragment.model!!.pkInvitation!!.senderCoins + userGift
                                                        )
                                                } else if (this@MultipleStreamerListFragment.model!!.getUserId()
                                                        .equals(
                                                            this@MultipleStreamerListFragment.model!!.pkInvitation!!.receiverId,
                                                            ignoreCase = true
                                                        )
                                                ) {
                                                    rootref!!.child(StreamingConstants.liveStreamingUsers)
                                                        .child(streamingIds[0])
                                                        .child("pkInvitation")
                                                        .child("receiverCoins").setValue(
                                                            this@MultipleStreamerListFragment.model!!.pkInvitation!!.receiverCoins + userGift
                                                        )
                                                    rootref!!.child(StreamingConstants.liveStreamingUsers)
                                                        .child(streamingIds[1])
                                                        .child("pkInvitation")
                                                        .child("receiverCoins").setValue(
                                                            this@MultipleStreamerListFragment.model!!.pkInvitation!!.receiverCoins + userGift
                                                        )
                                                }
                                            }
                                        }
                                    } else {
                                        if (bundle.getBoolean("showCount", false)) {
                                            val model = bundle.getParcelable("Data") as GiftModel?
                                            binding.tvGiftCount.text =
                                                " X " + bundle.getString("count") + " " + model!!.title

                                            binding.ivGiftCount.controller = frescoImageLoad(
                                                model.image,
                                                binding.ivGiftCount,
                                                false
                                            )

                                            binding.tabGiftCount.animate()
                                                .translationY(binding.animationCapture.y)
                                                .setDuration(700)
                                                .setListener(object : AnimatorListenerAdapter() {
                                                    override fun onAnimationStart(animation: Animator) {
                                                        super.onAnimationStart(animation)
                                                        binding.tabGiftCount.alpha = 1f
                                                    }

                                                    override fun onAnimationEnd(animation: Animator) {
                                                        super.onAnimationEnd(animation)
                                                        binding.tabGiftCount.clearAnimation()
                                                        binding.tabGiftCount.animate().alpha(0f)
                                                            .translationY(0f)
                                                            .setListener(object :
                                                                AnimatorListenerAdapter() {
                                                                override fun onAnimationEnd(
                                                                    animation: Animator
                                                                ) {
                                                                    super.onAnimationEnd(animation)
                                                                    binding.tabGiftCount.clearAnimation()
                                                                }
                                                            }).start()
                                                    }
                                                }).start()

                                        }
                                    }
                                }
                            })
                        f.show(childFragmentManager, "ShowOtherProfileBottomF")

                    }
                binding.wishListRecycler.adapter = wishListAdapter

                val snapHelper = PagerSnapHelper()
                snapHelper.attachToRecyclerView(binding.wishListRecycler)

                if (wishDataList.size > 1) {
                    startWishListScrolling()
                }

            } else {
                binding.wishListRecycler.visibility = View.GONE
            }
        }
    }

    private var job: Job? = null
    fun startWishListScrolling() {
        if (job?.isActive == true) return // 🔴 Prevent duplicate starts
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
            val intent = Intent(requireActivity(), ProfileActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
           requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        }
    }

    // send the comment to the live user
    fun addLiveStreamingShareMessage(type: String?) {
        val key = rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId()).child("Chat")
            .push().key
        val my_id = getSharedPreference(context).getString(Variables.U_ID, "")
        val my_name = getSharedPreference(context).getString(Variables.U_NAME, "")
        val my_image = getSharedPreference(context).getString(Variables.U_PIC, "")

        val c = Calendar.getInstance().time
        val formattedDate = Variables.df.format(c)

        val commentItem = LiveCommentModel()
        commentItem.key = key
        commentItem.userId = my_id
        commentItem.userName = my_name
        commentItem.userPicture = my_image
        commentItem.comment = ""
        commentItem.type = type
        commentItem.commentTime = formattedDate
        rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId()).child("Chat")
            .child((key)!!).setValue(commentItem)


        val model = CameraRequestModel()
        model.requestState = "1"
        rootref!!.child(StreamingConstants.liveStreamingUsers).child(this.model!!.getStreamingId())
            .child("CameraRequest")
            .child((getSharedPreference(context).getString(Variables.U_ID, "0"))!!)
            .setValue(model)
    }

    private fun addNodeCameraRequest() {
        if (cameraRequestEventListener == null) {
            cameraRequestEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val model = snapshot.getValue(
                            CameraRequestModel::class.java
                        )
                        if ((model!!.requestState == "2")) {
                            Toast.makeText(
                                context,
                                context!!.getString(R.string.camera_request_granted),
                                Toast.LENGTH_SHORT
                            ).show()
                            isCameraConnect = true
                        } else if ((model.requestState == "1")) {
                            isCameraConnect = false
                        } else {
                            isCameraConnect = false
                            stopBroadcast(userRole)
                            Toast.makeText(
                                context,
                                context!!.getString(R.string.camera_request_rejected),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    isCameraConnect = false
                }
            }
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId())
                .child("CameraRequest")
                .child((getSharedPreference(context).getString(Variables.U_ID, "0"))!!)
                .addValueEventListener(cameraRequestEventListener!!)
        }
    }

    private fun removeNodeCameraRequest() {
        if (rootref != null && cameraRequestEventListener != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId())
                .child("CameraRequest")
                .child((getSharedPreference(context).getString(Variables.U_ID, "0"))!!)
                .removeEventListener(cameraRequestEventListener!!)
            cameraRequestEventListener = null
        }
    }

    private fun setUpScreenData() {
        binding.tabMenu.visibility = View.GONE
        binding.tvMainUserName.text = model!!.getUserName()
        binding.ivMainProfile.controller = frescoImageLoad(model!!.getUserPicture(), binding.ivMainProfile, false)
        binding.profileLayout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                openProfile(model!!.userId)
            }
        })

        if (model!!.getIsVerified() == 1) {
            binding.ivVerified.visibility = View.VISIBLE
        } else {
            binding.ivVerified.visibility = View.GONE
        }


        if (model!!.getUserId().equals(
                Variables.sharedPreferences.getString(Variables.U_ID, ""),
                ignoreCase = true
            )
        ) {
            binding.tabGift.visibility = View.GONE
        } else {
            binding.tabGift.visibility = View.VISIBLE
        }


        if (model!!.pkInvitation != null && model!!.pkInvitation!!.pkStreamingId != null) {
            binding.tabCoHost.visibility = View.GONE
        } else if (model!!.isDualStreaming && model!!.isStreamJoinAllow) {
            binding.tabCoHost.visibility = View.VISIBLE
            addNodeCameraRequest()
        } else {
            binding.tabCoHost.visibility = View.GONE
            removeNodeCameraRequest()
        }

        if(wishListAdapter!=null){
            wishDataList.clear()
            wishDataList.addAll(model?.GiftWishList!!)
            wishListAdapter?.notifyDataSetChanged()
        }
    }


    private fun setUpJoinRecycler() {
        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.orientation = RecyclerView.VERTICAL
        binding.liveUserViewRecyclerView.layoutManager = layoutManager
        liveUserViewAdapter =
            LiveUserViewAdapter(requireContext(), jointUserList, object : AdapterClickListener {
                override fun onItemClick(view: View, pos: Int, `object`: Any) {
                }
            })
        binding.liveUserViewRecyclerView.adapter = liveUserViewAdapter
    }

    private fun ListenerJoinNode() {
        if (joinValueEventListener == null) {
            jointUserList.clear()
            joinValueEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val model = snapshot.getValue(
                        StreamJoinModel::class.java
                    )
                    model?.let { jointUserList.add(it) }

                    liveUserViewAdapter!!.notifyDataSetChanged()
                    binding.liveUserCount.text = getSuffix("" + jointUserList.size)
                    showjoinUser()
                    addJoinToQueue(model!!)
                    Log.d(com.coheser.app.Constants.tag,"in condotion : ${model.userName}")

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val model = snapshot.getValue(
                        StreamJoinModel::class.java
                    )
                    jointUserList.removeIf { it.userId == model?.userId }
                    liveUserViewAdapter!!.notifyDataSetChanged()
                    binding.liveUserCount.text = getSuffix("" + jointUserList.size)
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            }
            rootref!!.child(StreamingConstants.liveStreamingUsers)
                .child(model!!.getStreamingId()).child("JoinStream")
                .addChildEventListener(joinValueEventListener!!)
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

    fun removeJoinListener() {
        if (rootref != null && joinValueEventListener != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId()).child("JoinStream")
                .removeEventListener(
                    joinValueEventListener!!
                )
            joinValueEventListener = null
        }
    }

    private fun ListenerCoinNode() {
        if (coinValueEventListener == null) {
            coinValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    senderCoinsList.clear()
                    if (dataSnapshot.exists()) {
                        for (joinSnapsot: DataSnapshot in dataSnapshot.children) {
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
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId())
                .child(StreamingConstants.coinsStream).addValueEventListener(coinValueEventListener!!)
        }
    }

    fun setCoinsCount(){
        var maxCoins = 0
        for (item: LiveCoinsModel? in senderCoinsList) {
            maxCoins = maxCoins + (item!!.sendedCoins!!.toDouble()).toInt()
        }
        binding.tvCoinCount.text = "" + maxCoins
        if(model?.setGoalStream!=null) {
            binding.totalGoalCount.text = "/" + model?.setGoalStream?.goalAmount
            binding.receivedCoinsTxt.text =""+maxCoins
            val percentage = (maxCoins / model?.setGoalStream?.goalAmount!!.toInt()) * 100
            binding.goalPercentageBar.progress = percentage
            binding.goalPercentageTxt.text="$percentage%"
        }

    }


    fun removeCoinListener() {
        if (rootref != null && coinValueEventListener != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId())
                .child(StreamingConstants.coinsStream).removeEventListener(
                coinValueEventListener!!
            )
            coinValueEventListener = null
        }
    }

    private fun AddJoinNode() {
        val model = StreamJoinModel()
        val userModel=AccountUtils.getUserModel(getSharedPreference(context).getString(Variables.U_ID, "")!!)
        model.userId = userModel?.id
        model.userName = userModel?.username
        model.userPic = userModel?.getProfilePic()
        model.followersCount=""+userModel?.followers_count
        model.followingCount=""+userModel?.following_count
        if (pkInvitation != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child((pkInvitation!!.senderStreamingId)!!)
                .child("JoinStream")
                .child((getSharedPreference(context).getString(Variables.U_ID, ""))!!)
                .setValue(model)

            rootref!!.child(StreamingConstants.liveStreamingUsers).child((pkInvitation!!.receiverStreamingId)!!)
                .child("JoinStream")
                .child((getSharedPreference(context).getString(Variables.U_ID, ""))!!)
                .setValue(model)
        }
        else {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(this.model!!.getStreamingId()).child("JoinStream")
                .child((getSharedPreference(context).getString(Variables.U_ID, ""))!!)
                .setValue(model)
        }
    }

    private fun connectStreaming() {
        var streamingId: String? = model!!.getStreamingId()
        if (pkInvitation != null && pkInvitation!!.pkStreamingId != null) {
            streamingId = pkInvitation!!.pkStreamingId
            showPkBattleViews()
        }
        else {
            stopPkCountDown()
            hidePkBattleviews()
        }
        Log.d(com.coheser.app.Constants.tag, "connectStreaming StreamId:$streamingId")
        myActivity?.refreshStreamingConnection(streamingId)
    }

    private fun startBroadcast(role: Int) {
        Log.d(com.coheser.app.Constants.tag, "Stream: startBroadcast as $role")
        Log.d(com.coheser.app.Constants.tag, "Stream: startBroadcast with compare $userRole")
        val streamingId = model!!.getStreamingId()
        Log.d(com.coheser.app.Constants.tag, "startBroadcast StreamId:$streamingId")

        val surface = myActivity?.startBroadcast(streamingId, role)
        val ticTicApp = requireActivity().application as TicTicApp
        myActivity?.mVideoGridContainer!!.addUserVideoSurface(
            ticTicApp.engineConfig().uid!!.toInt(),
            surface,
            true
        )
    }

    private fun stopBroadcast(role: Int) {
        Log.d(com.coheser.app.Constants.tag, "Stream: stopBroadcast as $role")
        Log.d(com.coheser.app.Constants.tag, "Stream: stopBroadcast with compare $userRole")
        myActivity?.stopBroadcast(role)
        val ticTicApp =requireActivity().application as TicTicApp
        myActivity?.mVideoGridContainer!!.removeUserVideo(ticTicApp.engineConfig().uid!!.toInt(), true)
    }

    fun showPkBattleViews() {
        binding.pkProgressLayout.visibility = View.VISIBLE
        binding.pkbattleTimerLayout.visibility = View.VISIBLE
        binding.tabCoHost.visibility = View.GONE

        startPkCountDown()
        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                try {
                    if (requireActivity() != null) {
                       requireActivity().runOnUiThread(object : Runnable {
                            override fun run() {
                                showPkAnimation()
                            }
                        })
                    }
                } catch (e: Exception) {
                }
            }
        }, 2500)

        AddJoinNode()
    }

    fun hidePkBattleviews() {
        binding.pkProgressLayout.visibility = View.GONE
        binding.pkbattleTimerLayout.visibility = View.GONE
        binding.tabCoHost.visibility = View.VISIBLE
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
                        // com-specific logic to enable animation starting
                        anim.start()
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1800)
                            binding.pkgif.visibility = View.GONE
                        }
                        printLog(com.coheser.app.Constants.tag, "onFinalImageSet")
                    }
                }
            }

        val request = ImageRequestBuilder.newBuilderWithResourceId(R.raw.ic_pk_battle2)
            .build()
        val controller: DraweeController = Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .setAutoPlayAnimations(false)
            .setOldController(binding.pkgif.controller)
            .setControllerListener(controllerListener)
            .build()

        binding.pkgif.controller = controller
    }


    fun startPkCountDown() {
        if (pkBattleCountDown == null && model!!.pkInvitation != null) {
            rootref!!.child(Variables.onlineUser)
                .child(getSharedPreference(requireActivity()).getString(Variables.U_ID, "0")!!)
                .child("timeStamp").setValue(ServerValue.TIMESTAMP)

            rootref!!.child(Variables.onlineUser)
                .child(getSharedPreference(requireActivity()).getString(Variables.U_ID, "0")!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.child("timeStamp").value as Long
                    val dif = value - model!!.pkInvitation!!.timeStamp
                    val timeDiffernce = com.coheser.app.Constants.PkBattleTime - dif

                    pkBattleCountDown = object : CountDownTimer(timeDiffernce, 1000) {
                        override fun onTick(l: Long) {
                            binding.pkTimerTxt.text = getDate(l, "mm:ss")
                        }

                        override fun onFinish() {
                            hidePkBattleviews()

                            if (pkInvitation != null) {
                                showWinningGif()
                            }
                        }
                    }
                    pkBattleCountDown?.start()

                    printLog(com.coheser.app.Constants.tag, "difference...$value")
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    fun stopPkCountDown() {
        if (pkBattleCountDown != null) {
            pkBattleCountDown!!.cancel()
        }
    }

    private fun showWinningGif() {
        val user1Coins = pkInvitation!!.senderCoins
        val user2Coins = pkInvitation!!.receiverCoins

        var winningSide = "0"

        if (user1Coins > user2Coins) {
            if (pkInvitation!!.senderId.equals(model!!.userId, ignoreCase = true)) {
                winningSide = "1"
            } else {
                winningSide = "2"
            }
        } else if (user2Coins > user1Coins) {
            if (pkInvitation!!.receiverId.equals(model!!.userId, ignoreCase = true)) {
                winningSide = "1"
            } else {
                winningSide = "2"
            }
        }

        if ((winningSide == "1")) {
            binding.winningLayout.visibility = View.VISIBLE


            val request = ImageRequestBuilder.newBuilderWithResourceId(R.raw.ic_winning_stars)
                .build()
            val controller: DraweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(binding.winningGif1.controller)
                .setAutoPlayAnimations(true)
                .build()

            binding.winningGif1.controller = controller
        } else if ((winningSide == "2")) {
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

        if (winningHandler != null && winningRunnable != null) {
            winningHandler!!.removeCallbacks(winningRunnable!!)
        }

        winningHandler = Handler(Looper.getMainLooper())
        winningRunnable = object : Runnable {
            override fun run() {
                try {
                   requireActivity().runOnUiThread(object : Runnable {
                        override fun run() {
                            binding.winningLayout.visibility = View.GONE
                            showPkWinnerDialog()
                        }
                    })
                } catch (e: Exception) {
                }
            }
        }
        winningHandler!!.postDelayed(winningRunnable!!, 5000)
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
            val dialog = Dialog(requireContext())
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.item_pk_winner_dialog)
            dialog.window!!.setBackgroundDrawable(requireActivity()!!.getDrawable(R.drawable.d_round_white_background))

            val usernametxt = dialog.findViewById<TextView>(R.id.usernametxt)
            usernametxt.text = winnerUsername

            val cointxt = dialog.findViewById<TextView>(R.id.coinTxt)
            cointxt.text = "" + coins


            dialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
                override fun onDismiss(dialogInterface: DialogInterface) {
                }
            })
            dialog.show()
        }
    }

    fun addPkBattleStreamingListener() {
        if (pklistener == null) {
            pklistener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        pkInvitation = snapshot.getValue(PkInvitation::class.java)

                        if (model!!.pkInvitation != null && model!!.pkInvitation!!.pkStreamingId != null) {
                        } else if (pkInvitation != null && pkInvitation!!.pkStreamingId != null) {
                            model!!.pkInvitation = pkInvitation
                            myActivity?.removeStreamingConnection()
                            Log.d(
                                com.coheser.app.Constants.tag,
                                "connectStreaming pklistener snapshot.exists()"
                            )
                            connectStreaming()
                        }

                        if (model!!.userId.equals(pkInvitation!!.senderId, ignoreCase = true)) {
                            binding.coinCount1Txt.text = "" + pkInvitation!!.senderCoins
                            binding.coinCount2Txt.text = "" + pkInvitation!!.receiverCoins
                        }

                        if (model!!.userId.equals(pkInvitation!!.receiverId, ignoreCase = true)) {
                            binding.coinCount1Txt.text = "" + pkInvitation!!.receiverCoins
                            binding.coinCount2Txt.text = "" + pkInvitation!!.senderCoins
                        }

                        updatePkBar()
                    } else if (model!!.pkInvitation != null && model!!.pkInvitation!!.pkStreamingId != null) {
                        if ((pkInvitation!!.senderId == model!!.userId)) {
                            rootref!!.child(StreamingConstants.liveStreamingUsers)
                                .child((pkInvitation!!.receiverStreamingId)!!).child("JoinStream")
                                .child(
                                    (getSharedPreference(context).getString(
                                        Variables.U_ID,
                                        ""
                                    ))!!
                                ).removeValue()
                        } else {
                            rootref!!.child(StreamingConstants.liveStreamingUsers)
                                .child((pkInvitation!!.senderStreamingId)!!).child("JoinStream")
                                .child(
                                    (getSharedPreference(context).getString(
                                        Variables.U_ID,
                                        ""
                                    ))!!
                                ).removeValue()
                        }

                        binding.liveVideoGridLayout.removeUserVideo(
                            pkInvitation!!.senderId!!.toInt(),
                            false
                        )
                        binding.liveVideoGridLayout.removeUserVideo(
                            pkInvitation!!.receiverId!!.toInt(),
                            false
                        )

                        pkInvitation = null
                        model!!.pkInvitation = null
                        myActivity?.removeStreamingConnection()
                        Log.d(
                            com.coheser.app.Constants.tag,
                            "connectStreaming pklistener !snapshot.exists()"
                        )
                        connectStreaming()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
            rootref!!.child(StreamingConstants.liveStreamingUsers).child((model!!.streamingId)!!)
                .child("pkInvitation").addValueEventListener(pklistener!!)
        }
    }

    fun removePkBattleStreamingListener() {
        try {
            if (rootref != null && pklistener != null) {
                rootref!!.child(StreamingConstants.liveStreamingUsers).child((model!!.streamingId)!!)
                    .child("pkInvitation").removeEventListener(
                    pklistener!!
                )
            }
        } catch (e: Exception) {
        }
    }

    fun updatePkBar() {
        if (model!!.userId.equals(pkInvitation!!.senderId, ignoreCase = true)) {
            val user1Coins = pkInvitation!!.senderCoins
            val user2Coins = pkInvitation!!.receiverCoins

            val total = user1Coins + user2Coins
            if (total > 0) {
                val percentage1 = ((user1Coins * 100) / total)
                val percentage2 = 100 - percentage1

                printLog(com.coheser.app.Constants.tag, "$percentage1--$percentage2")

                binding.pkProgressbar.setFirstSectionPercentage( percentage1)
                binding.pkProgressbar.setSecondSectionPercentage(percentage2)
            } else {
                binding.pkProgressbar.setFirstSectionPercentage(50)
                binding.pkProgressbar.setSecondSectionPercentage(50)
            }
        } else if (model!!.userId.equals(pkInvitation!!.receiverId, ignoreCase = true)) {
            val user1Coins = pkInvitation!!.receiverCoins
            val user2Coins = pkInvitation!!.senderCoins

            val total = user1Coins + user2Coins
            if (total > 0) {
                val percentage1 = ((user1Coins * 100) / total)
                val percentage2 = 100 - percentage1

                printLog(com.coheser.app.Constants.tag, "$percentage1--$percentage2")

                binding.pkProgressbar.firstPercentage = percentage1
                binding.pkProgressbar.setFirstSectionPercentage(percentage2)
            } else {
                binding.pkProgressbar.setFirstSectionPercentage(50)
                binding.pkProgressbar.setFirstSectionPercentage(50)
            }
        }
    }

    // when user goes to offline then change the value status on firebase
    fun removeJoinNode() {
        if (rootref != null) {
            if (pkInvitation != null) {
                rootref!!.child(StreamingConstants.liveStreamingUsers).child((pkInvitation!!.receiverStreamingId)!!)
                    .child("JoinStream")
                    .child((getSharedPreference(context).getString(Variables.U_ID, ""))!!)
                    .removeValue()

                rootref!!.child(StreamingConstants.liveStreamingUsers).child((pkInvitation!!.senderStreamingId)!!)
                    .child("JoinStream")
                    .child((getSharedPreference(context).getString(Variables.U_ID, ""))!!)
                    .removeValue()
            } else {
                rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId())
                    .child("JoinStream")
                    .child((getSharedPreference(context).getString(Variables.U_ID, ""))!!)
                    .removeValue()
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.cross_btn -> {
               requireActivity().finish()
            }

            R.id.cross_btn2 -> {
               requireActivity().finish()
            }

            R.id.tabMenu -> {
                ShowDailogForJoinBroadcast()
            }

            R.id.tabLikeStreaming -> {
                if (isSendHeart) {
                    isSendHeart = true
                    addLikeIntoStream()
                }
            }

            R.id.tabGift -> {
                ShowGiftSheet()
            }

            R.id.exclusiveRechargeBtn ->{
                ShowExclusiveRecharge()
            }

            R.id.tabInviteAll -> {
                inviteFriendsForStream()
            }

            R.id.tabCoHost -> {
                if (isCameraConnect) {
                    ShowDailogForJoinBroadcast()
                } else {
                    sendCameraRequest()
                }
            }

            R.id.tvMessage -> {
                sendComment()
            }

            R.id.btnfollow -> {
                if (checkLoginUser(requireActivity())) followUnFollowUser()
            }
        }
    }

    private fun followUnFollowUser() {
        callApiForFollowUnFollow(requireActivity(),
            getSharedPreference(context).getString(Variables.U_ID, ""),
            model!!.getUserId(),
            object : APICallBack {
                override fun arrayData(arrayList: ArrayList<*>?) {
                }

                override fun onSuccess(responce: String) {
                    callApiForGetAllvideos(model!!.getUserId(), model!!.getUserName())
                }

                override fun onFail(responce: String) {
                }
            })
    }

    private fun callApiForGetAllvideos(userId: String?, userName: String?) {
        val parameters = JSONObject()
        try {
            if (getSharedPreference(context).getBoolean(Variables.IS_LOGIN, false)) {
                if (userId != null && userName != null) {
                    if ((userId == getSharedPreference(context).getString(Variables.U_ID, ""))) {
                        parameters.put("user_id", userId)
                    } else {
                        parameters.put(
                            "user_id",
                            getSharedPreference(context).getString(Variables.U_ID, "")
                        )
                        parameters.put("other_user_id", userId)
                    }
                } else {
                    parameters.put(
                        "user_id",
                        getSharedPreference(context).getString(Variables.U_ID, "")
                    )
                    parameters.put("username", userName)
                }
            } else {
                if (userId != null && userName != null) {
                    parameters.put("user_id", userId)
                } else {
                    parameters.put("username", userName)
                }
            }
        } catch (e: Exception) {
            Log.d(com.coheser.app.Constants.tag, "Exception: $e")
        }


        VolleyRequest.JsonPostRequest(
            requireActivity(),
            ApiLinks.showUserDetail,
            parameters,
            getHeaders(requireActivity()),
            object : Callback {
                override fun onResponce(resp: String) {
                    lifecycleScope.launch {
                        try {
                            val jsonObject = JSONObject(resp)
                            val code = jsonObject.optString("code")
                            if ((code == "200")) {
                                val msg = jsonObject.optJSONObject("msg")

                                val userDetailModel = getUserDataModel(msg.optJSONObject("User"))
                                val follow_status =
                                    userDetailModel.button!!.lowercase(Locale.getDefault())
                                if (userDetailModel.id != getSharedPreference(context).getString(
                                        Variables.U_ID,
                                        ""
                                    )
                                ) {
                                    if (follow_status.equals("following", ignoreCase = true)) {
                                        binding.btnfollow.visibility = View.GONE
                                    } else if (follow_status.equals("friends", ignoreCase = true)) {
                                        binding.btnfollow.visibility = View.GONE
                                    } else {
                                        binding.btnfollow.visibility = View.VISIBLE
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.d(com.coheser.app.Constants.tag, "Exception: $e")
                        }
                    }
                }
            })
    }

    private fun sendCameraRequest() {
        if (selfInvitehandler == null) {
            addLiveStreamingShareMessage("selfInviteForStream")
            selfInvitehandler = object : CountDownTimer((5 * 60 * 1000).toLong(), (1000).toLong()) {
                override fun onTick(l: Long) {
                    selfInviteRemainingTime = (l / 1000).toInt()
                }

                override fun onFinish() {
                    selfInviteRemainingTime = 0
                    selfInvitehandler = null
                }
            }.start()
        } else {
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.you_can_send_join_request_after) + " " + selfInviteRemainingTime + "sec",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun inviteFriendsForStream() {
        val f = InviteContactsToStreamFragment(
            model!!.getStreamingId(),
            "multiple",
            object : FragmentCallBack {
                override fun onResponce(bundle: Bundle) {
                    if (bundle.getBoolean("isShow", false)) {
                    }
                }
            })
        f.show(childFragmentManager, "InviteContactsToStreamF")
    }


    fun ShowDailogForJoinBroadcast() {
        val alertDialog = Dialog(requireContext())
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setContentView(R.layout.live_join_broadcast_view)
        alertDialog.window!!
            .setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.d_round_white_background
                )
            )

        val swith_camera_btn = alertDialog.findViewById<ImageView>(R.id.swith_camera_btn)
        val live_btn_mute_audio = alertDialog.findViewById<ImageView>(R.id.live_btn_mute_audio)
        val live_btn_beautification =
            alertDialog.findViewById<ImageView>(R.id.live_btn_beautification)
        val live_btn_mute_video = alertDialog.findViewById<ImageView>(R.id.live_btn_mute_video)
        val tab_cancel = alertDialog.findViewById<RelativeLayout>(R.id.tab_cancel)
        val closeBtn = alertDialog.findViewById<ImageView>(R.id.closeBtn)
        val tabClient = alertDialog.findViewById<LinearLayout>(R.id.tabClient)
        val tabSwitch = alertDialog.findViewById<LinearLayout>(R.id.tabSwitch)

        if (!model!!.isDualStreaming) {
            if (model!!.getOnlineType() != "oneTwoOne") {
                tabClient.visibility = View.GONE
                tabSwitch.visibility = View.GONE
            }
        }


        closeBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                alertDialog.dismiss()
            }
        })

        live_btn_mute_audio.isActivated = !isAudioActivated
        live_btn_mute_video.isActivated = !isVideoActivated
        live_btn_beautification.isActivated = !isbeautyActivated

        myActivity?.setBeautyEffectOptions(live_btn_mute_video.isActivated)

        tab_cancel.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                alertDialog.dismiss()
               requireActivity().onBackPressed()
            }
        })
        swith_camera_btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                alertDialog.dismiss()
                myActivity?.switchCamera()
            }
        })
        live_btn_mute_audio.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                alertDialog.dismiss()
                isAudioActivated = live_btn_mute_video.isActivated
                if (!isAudioActivated) return
                myActivity?.muteLocalAudioStream(isAudioActivated)
                view.isActivated = !isAudioActivated
            }
        })
        live_btn_beautification.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                alertDialog.dismiss()
                isbeautyActivated = view.isActivated
                view.isActivated = !isbeautyActivated
                myActivity?.setBeautyEffectOptions(isbeautyActivated)
            }
        })
        live_btn_mute_video.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                alertDialog.dismiss()
                isVideoActivated = view.isActivated
                if (isVideoActivated) {
                    userRole = Constants.CLIENT_ROLE_AUDIENCE
                    stopBroadcast(Constants.CLIENT_ROLE_AUDIENCE)
                } else {
                    userRole = Constants.CLIENT_ROLE_BROADCASTER
                    startBroadcast(Constants.CLIENT_ROLE_BROADCASTER)
                }
                view.isActivated = !isVideoActivated
            }
        })
        alertDialog.show()
    }

    fun ShowGiftSheet() {
        val giftFragment = StickerGiftFragment.newInstance(
            model?.userId!!,
            model?.streamingId!!,
            "",
            StickerGiftFragment.fromSendGift,
            object : FragmentCallBack {
                override fun onResponce(bundle: Bundle) {
                     if (bundle.getBoolean("isShow", false)) {
                        val model = bundle.getParcelable("Data") as GiftModel?
                        val counter = bundle.getString("count")
                        addGiftComment("gift", counter, model)

                        Log.d(com.coheser.app.Constants.tag, "Test : " + this@MultipleStreamerListFragment.model!!.getUserCoins())

                        if (this@MultipleStreamerListFragment.model != null) {
                            val userGift = (counter!!.toLong() * model!!.coin!!)
                            val map: HashMap<String, Any> = HashMap<String, Any>()
                            map["userCoins"] = "" + userGift
                            rootref!!.child(StreamingConstants.liveStreamingUsers).child(this@MultipleStreamerListFragment.model!!.getStreamingId())
                                .updateChildren(map)

                            if (this@MultipleStreamerListFragment.model!!.pkInvitation != null && this@MultipleStreamerListFragment.model!!.pkInvitation!!.pkStreamingId != null) {
                                val streamingIds =
                                    this@MultipleStreamerListFragment.model!!.pkInvitation!!.pkStreamingId!!.split("PK".toRegex())
                                        .dropLastWhile { it.isEmpty() }
                                        .toTypedArray()

                                if (this@MultipleStreamerListFragment.model!!.getUserId()
                                        .equals(this@MultipleStreamerListFragment.model!!.pkInvitation!!.senderId, ignoreCase = true)
                                ) {
                                    rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingIds[0])
                                        .child("pkInvitation").child("senderCoins").setValue(
                                        this@MultipleStreamerListFragment.model!!.pkInvitation!!.senderCoins + userGift
                                    )
                                    rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingIds[1])
                                        .child("pkInvitation").child("senderCoins").setValue(
                                        this@MultipleStreamerListFragment.model!!.pkInvitation!!.senderCoins + userGift
                                    )
                                } else if (this@MultipleStreamerListFragment.model!!.getUserId()
                                        .equals(this@MultipleStreamerListFragment.model!!.pkInvitation!!.receiverId, ignoreCase = true)
                                ) {
                                    rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingIds[0])
                                        .child("pkInvitation").child("receiverCoins").setValue(
                                        this@MultipleStreamerListFragment.model!!.pkInvitation!!.receiverCoins + userGift
                                    )
                                    rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingIds[1])
                                        .child("pkInvitation").child("receiverCoins").setValue(
                                        this@MultipleStreamerListFragment.model!!.pkInvitation!!.receiverCoins + userGift
                                    )
                                }
                            }
                        }
                    }
                    else {
                        if (bundle.getBoolean("showCount", false)) {
                            val model = bundle.getParcelable("Data") as GiftModel?
                            binding.tvGiftCount.text =
                                " X " + bundle.getString("count") + " " + model!!.title

                            binding.ivGiftCount.controller = frescoImageLoad(model.image, binding.ivGiftCount, false)

                            binding.tabGiftCount.animate()
                                .translationY(binding.animationCapture.y).setDuration(700)
                                .setListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationStart(animation: Animator) {
                                        super.onAnimationStart(animation)
                                        binding.tabGiftCount.alpha = 1f
                                    }

                                    override fun onAnimationEnd(animation: Animator) {
                                        super.onAnimationEnd(animation)
                                        binding.tabGiftCount.clearAnimation()
                                        binding.tabGiftCount.animate().alpha(0f).translationY(0f)
                                            .setListener(object : AnimatorListenerAdapter() {
                                                override fun onAnimationEnd(animation: Animator) {
                                                    super.onAnimationEnd(animation)
                                                    binding.tabGiftCount.clearAnimation()
                                                }
                                            }).start()
                                    }
                                }).start()

                        }
                    }
                }
            })
        giftFragment.show(childFragmentManager, "")
    }

    fun ShowExclusiveRecharge() {
        val exclusiveRechargeFragment = ExclusiveRechargeFragment.newInstance(
            object : FragmentCallBack {
                override fun onResponce(bundle: Bundle) {
                }
            })
        exclusiveRechargeFragment.show(childFragmentManager, "")
    }



    fun ShowGiftAnimation(item: LiveCommentModel) {


        binding.ivGiftProfile.controller = frescoImageLoad(
            item.userPicture, binding.ivGiftProfile, false
        )

        binding.ivGiftItem.controller =
            frescoImageLoad("" + item.giftPic, binding.ivGiftItem, false)
        binding.tvGiftTitle.text = item.userName
        binding.tvGiftCountTitle.text = getString(R.string.gave_you_a) + " " + item.giftName
        binding.tvSendGiftCount.text = "X " + item.giftCount

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


            showGiftAnimation(item.giftPic, item)

    }

    fun showGiftAnimation(gifUrl: String?, item: LiveCommentModel?) {

        val file = File(FileUtils.getAppFolder(appLevelContext!!) + Variables.APP_Gifts_Folder + item?.giftId!!+".mp4")
        if (file.exists()) {
            val animationViewF = AnimationViewF.newInstance(item?.giftId!!.toString())
            animationViewF.show(parentFragmentManager, "animationViewF")
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
                    Functions.printLog(com.coheser.app.Constants.tag,"downloaded file:"+file?.absolutePath)
                    if(file?.exists() == true) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val animationViewF =
                                AnimationViewF.newInstance(item?.giftId.toString())
                            animationViewF.show(parentFragmentManager, "animationViewF")
                        }

                    }
                }
            }
            Dialogs.showGiftDailog(requireActivity(), item.giftIcon)
        }

    }

    fun startAnimation(anim: Animatable, time: String?) {
        printLog(com.coheser.app.Constants.tag, "Time:$time")

        var sec = time!!.toInt()
        if (sec == 0) {
            sec = 2
        }
        val duration = (sec * 1000)
        anim.start()
        object : CountDownTimer(duration.toLong(), 300) {
            override fun onTick(l: Long) {
            }

            override fun onFinish() {
                anim.stop()
                binding.pkgiftGif.visibility = View.GONE
            }
        }.start()
    }


    private fun PlayGiftSound() {

        player = MediaPlayer.create(context, R.raw.gift_tone)
        player?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player?.setVolume(100f, 100f)
        player?.setOnPreparedListener(object : OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer) {
                mp.start()
            }
        })
        lifecycleScope.launch {
            delay(2000)
            onTuneStop()
        }

    }

    fun onTuneStop() {
        if (player?.isPlaying == true) {
            player?.stop()
            player?.release()
        }

    }

    // send the comment to the live user
    fun addMessages(type: String?) {
        val key = rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId()).child("Chat")
            .push().key
        val my_id = getSharedPreference(context).getString(Variables.U_ID, "")
        val my_name = getSharedPreference(context).getString(Variables.U_NAME, "")
        val my_image = getSharedPreference(context).getString(Variables.U_PIC, "")

        val c = Calendar.getInstance().time
        val formattedDate = Variables.df.format(c)

        val commentItem = LiveCommentModel()
        commentItem.key = key
        commentItem.userId = my_id
        commentItem.userName = my_name
        commentItem.userPicture = my_image
        commentItem.comment = binding.tvMessage.text.toString()
        commentItem.type = type
        commentItem.commentTime = formattedDate
        rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId()).child("Chat")
            .child((key)!!).setValue(commentItem)

        binding.tvMessage.text = getString(R.string.add_a_comment)
    }

    // send the comment to the live user
    fun addLikeComment(type: String?) {
        val key = rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId()).child("Chat")
            .push().key
        val my_id = getSharedPreference(context).getString(Variables.U_ID, "")
        val my_name = getSharedPreference(context).getString(Variables.U_NAME, "")
        val my_image = getSharedPreference(context).getString(Variables.U_PIC, "")

        val c = Calendar.getInstance().time
        val formattedDate = Variables.df.format(c)

        val commentItem = LiveCommentModel()
        commentItem.key = key
        commentItem.userId = my_id
        commentItem.userName = my_name
        commentItem.userPicture = my_image
        commentItem.comment = my_name + " " + getString(R.string.like_this_stream)
        commentItem.type = type
        commentItem.commentTime = formattedDate
        rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId()).child("Chat")
            .child((key)!!).setValue(commentItem)

        binding.tvMessage.text = getString(R.string.add_a_comment)
    }

    // send the comment to the live user
    fun addGiftComment(type: String?, count: String?, giftModel: GiftModel?) {
        val key = rootref!!.child(StreamingConstants.liveStreamingUsers).child(this.model!!.getStreamingId()).child("Chat")
            .push().key
        val my_id = getSharedPreference(context).getString(Variables.U_ID, "")
        val my_name = getSharedPreference(context).getString(Variables.U_NAME, "")
        val my_image = getSharedPreference(context).getString(Variables.U_PIC, "")

        val c = Calendar.getInstance().time
        val formattedDate = Variables.df.format(c)

        val commentItem = LiveCommentModel()
        commentItem.key = key
        commentItem.userId = my_id
        commentItem.userName = my_name
        commentItem.userPicture = my_image

        commentItem.giftName = giftModel!!.title
        commentItem.giftPic = giftModel.image
        commentItem.giftCount = count
        commentItem.giftId = giftModel.id.toString()
        commentItem.giftIcon = giftModel.icon
        commentItem.time = giftModel.time.toString()

        commentItem.comment = ""
        commentItem.type = type
        commentItem.commentTime = formattedDate

        rootref!!.child(StreamingConstants.liveStreamingUsers).child(this.model!!.getStreamingId()).child("Chat")
            .child((key)!!).setValue(commentItem)


        val coinsModel = LiveCoinsModel()
        coinsModel.userId = my_id
        coinsModel.userName = my_name
        coinsModel.userPicture = my_image
        coinsModel.sendedCoins = count!!.toDouble() * giftModel.coin!!
        coinsModel.giftPic=giftModel.image
        rootref!!.child(StreamingConstants.liveStreamingUsers).child(this.model!!.getStreamingId()).child(StreamingConstants.coinsStream)
            .child((my_id)!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val preModel = snapshot.getValue(
                        LiveCoinsModel::class.java
                    )
                    var totalCoins = preModel!!.sendedCoins.toDouble()
                    totalCoins = totalCoins + ((count.toDouble() * giftModel.coin!!))

                    val updateMap = HashMap<String, Any>()
                    updateMap["sendedCoins"] =totalCoins

                    rootref!!.child(StreamingConstants.liveStreamingUsers).child(this@MultipleStreamerListFragment.model!!.getStreamingId())
                        .child(StreamingConstants.coinsStream).child(
                        (my_id)
                    ).updateChildren(updateMap)
                } else {
                    rootref!!.child(StreamingConstants.liveStreamingUsers).child(this@MultipleStreamerListFragment.model!!.getStreamingId())
                        .child(StreamingConstants.coinsStream).child(
                        (my_id)
                    ).setValue(coinsModel)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })



        CoroutineScope(Dispatchers.Default).launch {
            if(model?.GiftWishList!=null) {
                for ((index,item) in model?.GiftWishList!!.withIndex()) {
                    if(item.id.equals(giftModel.id.toString())){
                        var isExist=false
                        for (users in item.AllGiftUsers!!){
                            if(users.userId.equals(my_id)){
                                users.count++
                                isExist=true
                                break
                            }
                        }
                        if(!isExist){
                            val giftUsers=GiftUsers()
                            giftUsers.userId=my_id
                            giftUsers.count=1
                            giftUsers.userName=my_name!!
                            giftUsers.userPicture=my_image!!
                            item.AllGiftUsers!!.add(giftUsers)
                        }

                        item.totalGiftReceived=(item.totalGiftReceived!!.toInt()+1).toString()
                        model?.GiftWishList!!.set(index,item)
                        rootref!!.child(StreamingConstants.liveStreamingUsers)
                            .child(model!!.getStreamingId())
                            .child("GiftWishList").setValue(model?.GiftWishList)
                    }
                }
            }
        }


        binding.tvMessage.text = getString(R.string.add_a_comment)
    }



    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        checkVisible = menuVisible
        if (menuVisible) {
            lifecycleScope.launch {
                delay(300)
                InitControl()
                ActionControl()
                lounchStreamerCam()
            }
        }else{
            removeNodeListener()
        }
    }


    private fun checkUserStatus() {
        if (userLiveStatusListener == null) {
            userLiveStatusListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        if (requireActivity() != null) {
                           requireActivity().runOnUiThread(object : Runnable {
                                override fun run() {
                                    if (snapshot.exists()) {
                                        model = snapshot.getValue(LiveUserModel::class.java)
                                        myActivity?.updateLiveModel((model)!!)
                                        binding.tabStreamView.visibility = View.VISIBLE
                                        binding.tabOfflineView.visibility = View.GONE


                                        Log.d(com.coheser.app.Constants.tag, "Stream: userChange")
                                        setUpScreenData()

                                    } else {
                                        binding.tabStreamView.visibility = View.GONE
                                        binding.tabOfflineView.visibility = View.VISIBLE
                                    }
                                }
                            })
                        }
                    } catch (e: Exception) {
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.tabStreamView.visibility = View.GONE
                    binding.tabOfflineView.visibility = View.VISIBLE
                }
            }
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId())
                .addValueEventListener(userLiveStatusListener!!)
        }
    }

    private fun removeUserStatus() {
        if (rootref != null && userLiveStatusListener != null) {
            rootref!!.child("LiveStreamingUsers").child(model!!.getStreamingId())
                .removeEventListener(
                    userLiveStatusListener!!
                )
            userLiveStatusListener = null
        }
    }

    private fun lounchStreamerCam() {
        InitNodeListener()
        addBlockStatusStream()
    }

    private fun InitNodeListener() {
        joinStream()
        AddJoinNode()
        ListenerCoinNode()
        ListenerJoinNode()
        ListCommentData()
        addProductListener()
        addLikeStream()
        addStreamerOnlineStatus()
        addPkBattleStreamingListener()
        callApiForGetAllvideos(model!!.getUserId(), model!!.getUserName())
    }

    private fun addBlockStatusStream() {
        if (blockValueEventListener == null) {
            blockValueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val blockStatus = snapshot.child("blockState").value as String?
                        performBlockAction(blockStatus)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }

            rootref!!.child("LiveStreamingUsers").child(model!!.getStreamingId())
                .child("BlockStreaming")
                .child((getSharedPreference(context).getString(Variables.U_ID, ""))!!)
                .addValueEventListener(blockValueEventListener!!)
        }
    }

    private fun performBlockAction(blockStatus: String?) {
        if ((blockStatus == "1")) {
            Toast.makeText(context, getString(R.string.your_are_blocked_on_this_stream), Toast.LENGTH_SHORT).show()
            binding.tabStreamView.visibility = View.GONE
            binding.tabOfflineView.visibility = View.VISIBLE
            removeNodeListener()
        }
    }

    private fun removeBlockStatusStream() {
        if (rootref != null && blockValueEventListener != null) {
            rootref!!.child("LiveStreamingUsers").child(model!!.getStreamingId())
                .child("BlockStreaming")
                .child((getSharedPreference(context).getString(Variables.U_ID, ""))!!)
                .removeEventListener(
                    blockValueEventListener!!
                )
            blockValueEventListener = null
        }
    }

    private fun addLikeStream() {
        if (likeValueEventListener == null) {
            var heartCounter = 0
            likeValueEventListener = object : ChildEventListener {

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        heartCounter = heartCounter + 1
                        binding.tvOtherUserLikes.text = getSuffix("" + heartCounter)
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
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId())
                .child("LikesStream")
                .addChildEventListener(likeValueEventListener!!)
        }
    }

    fun removeLikeStream() {
        if (rootref != null && likeValueEventListener != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).child(model!!.getStreamingId())
                .child("LikesStream").removeEventListener(
                likeValueEventListener!!
            )
            likeValueEventListener = null
        }
    }

    private fun joinStream() {
        val isBroadcaster = false
        isAudioActivated = !isBroadcaster
        isVideoActivated = !isBroadcaster
        isbeautyActivated = false
        myActivity?.setBeautyEffectOptions(isbeautyActivated)
        myActivity?.mVideoGridContainer = binding.liveVideoGridLayout
        myActivity?.videoGridMainLayout = binding.videoGridMainLayout
        myActivity?.mVideoGridContainer!!.setMainParentLayout(binding.videoGridMainLayout,model?.userPicture)
        myActivity?.mVideoGridContainer!!.setStatsManager(myActivity?.setStatsManager())
        myActivity?.setClientRole(userRole)
        if (isBroadcaster) {
            startBroadcast(userRole)
        }
        myActivity?.mVideoDimension = myActivity?.getconfigDimenIndex()
    }

    private fun addStreamerOnlineStatus() {
        if (streamerOnlineListener == null) {
            streamerOnlineListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (!(TextUtils.isEmpty(snapshot.value.toString()))) {
                        val itemUpdate = snapshot.getValue(UserOnlineModel::class.java)

                        if (model!!.getUserId().equals(itemUpdate!!.getUserId(), ignoreCase = true)) {
                            if (timer != null) {
                                cancelIndeterminentLoader()
                                timer!!.cancel()
                            }
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    try {
                        if (!(TextUtils.isEmpty(snapshot.value.toString()))) {
                           requireActivity().runOnUiThread(object : Runnable {
                                override fun run() {
                                    val itemUpdate = snapshot.getValue(UserOnlineModel::class.java)
                                    if (model!!.getUserId().equals(itemUpdate!!.getUserId(), ignoreCase = true)) {
                                        showIndeterminentLoader(
                                            requireActivity(),
                                            itemUpdate.getUserName() + " " + context!!.getString(R.string.single_is_week),
                                            false,
                                            false
                                        )
                                        timer!!.cancel()
                                        timer = Timer()
                                        timer!!.schedule(
                                            object : TimerTask() {
                                                override fun run() {
                                                   requireActivity().runOnUiThread(object :
                                                        Runnable {
                                                        override fun run() {
                                                            cancelIndeterminentLoader()
                                                            rootref!!.child("LiveStreamingUsers")
                                                                .child(
                                                                    model!!.getStreamingId()
                                                                ).removeValue()
                                                        }
                                                    })
                                                }
                                            },
                                            DELAY
                                        )
                                    }
                                }
                            })
                        }
                    } catch (e: Exception) {
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
            rootref!!.child(Variables.onlineUser).addChildEventListener(streamerOnlineListener!!)
        }
    }

    private fun removeStreamerOnlineStatus() {
        if (rootref != null && streamerOnlineListener != null) {
            rootref!!.child(Variables.onlineUser).removeEventListener(streamerOnlineListener!!)
            streamerOnlineListener = null
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        removeNodeListener()
    }

    private fun removeNodeListener() {
        myActivity?.removeStreamingConnection()
        removePkBattleStreamingListener()
        removeUserStatus()
        removeJoinNode()
        removeCoinListener()
        removeJoinListener()
        removeCommentListener()
        removeProductListener()
        removeLikeStream()
        removeStreamerOnlineStatus()

        removeBlockStatusStream()
        if (model!!.isDualStreaming) {
            removeNodeCameraRequest()
        }
    }
}