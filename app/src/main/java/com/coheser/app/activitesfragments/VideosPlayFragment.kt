package com.coheser.app.activitesfragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoSize
import com.like.LikeButton
import com.like.OnLikeListener
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.comments.CommentVideoFragment
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.activitesfragments.profile.ReportTypeActivity
import com.coheser.app.activitesfragments.profile.videopromotion.VideoPromoteStepsActivity
import com.coheser.app.activitesfragments.shoping.TaggedProductsListFragment
import com.coheser.app.activitesfragments.soundlists.VideoSoundActivity
import com.coheser.app.activitesfragments.videorecording.PostVideoActivity
import com.coheser.app.activitesfragments.videorecording.VideoRecoderDuetActivity
import com.coheser.app.adapters.ViewPagerStatAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.AlertLabelEditorBinding
import com.coheser.app.databinding.ItemHomeHeightedLayoutBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.interfaces.FragmentDataSend
import com.coheser.app.models.CommentModel
import com.coheser.app.models.HomeModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.ApiRepository
import com.coheser.app.simpleclasses.DataHolder
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.Dialogs
import com.coheser.app.simpleclasses.Downloading.DownloadFiles
import com.coheser.app.simpleclasses.FileUtils
import com.coheser.app.simpleclasses.FriendsTagHelper
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.OnSwipeTouchListener
import com.coheser.app.simpleclasses.PermissionUtils
import com.coheser.app.simpleclasses.ShowMoreLess
import com.coheser.app.simpleclasses.TicTicApp
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.simpleclasses.VerticalViewPager
import com.coheser.app.viewModels.VideoPlayViewModel
import com.volley.plus.interfaces.APICallBack
import io.paperdb.Paper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class VideosPlayFragment : Fragment, Player.Listener, FragmentDataSend {

    lateinit var binding: ItemHomeHeightedLayoutBinding

    var menuPager: VerticalViewPager ?= null
    var item: HomeModel ?= null
    var fragmentCallBack: FragmentCallBack ?= null
    var showad = false
    var fragmentContainerId = 0

    private val viewModel: VideoPlayViewModel by viewModel()

    constructor(
        showad: Boolean,
        item: HomeModel,
        menuPager: VerticalViewPager?,
        fragmentCallBack: FragmentCallBack?,
        fragmentContainerId: Int
    ) {
        this.showad = showad
        this.item = item
        this.menuPager = menuPager
        this.fragmentCallBack = fragmentCallBack
        this.fragmentContainerId = fragmentContainerId
    }

    constructor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.item_home_heighted_layout, container, false)
        updateImmediateViewChange()
        initializePlayer()
        initalize_views()
        Log.d(Constants.tag,"videolista")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setObserveAble()
    }

    fun setObserveAble() {

        viewModel.videoDetailLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let {
                        item = it
                        setData()
                    }
                }
                is ApiResponce.Error -> {

                }
                is ApiResponce.Loading -> {

                }

            }
        })


        viewModel.destinationTapLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponce.Success -> {
                    Functions.cancelLoader()
                    openWebUrl(
                        "${item?.promotionModel?.action_button}",
                        "${item?.promotionModel?.website_url}")
                }
                is ApiResponce.Error -> {
                    Functions.cancelLoader()
                }
                is ApiResponce.Loading -> {
                    Functions.showLoader(activity, false, false)
                }

            }
        })

        viewModel.pinVideoLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponce.Success -> {
                    Functions.cancelLoader()
                    onPinSuccess()
                }
                is ApiResponce.Error -> {
                    Functions.cancelLoader()
                }
                is ApiResponce.Loading -> {
                    Functions.showLoader(activity, false, false)
                }

            }
        })

        viewModel.notInterestedLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponce.Success -> {
                    Functions.cancelLoader()
                    menuPager?.let {
                        val pagerAdapter = it.adapter as ViewPagerStatAdapter
                        val bundle = Bundle()
                        bundle.putString("action", "removeList")
                        fragmentCallBack?.onResponce(bundle)
                        pagerAdapter.refreshStateSet(true)
                        pagerAdapter.removeFragment(it.currentItem)
                        pagerAdapter.refreshStateSet(false)
                    }
                }
                is ApiResponce.Error -> {
                    Functions.cancelLoader()
                }
                is ApiResponce.Loading -> {
                    Functions.showLoader(activity, false, false)
                }

            }
        })

        viewModel.downloadVideoLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponce.Success -> {
                    Functions.cancelLoader()
                    it.data?.let {
                        downLoadFromUrl(it)
                    }
                }
                is ApiResponce.Error -> {
                    Functions.cancelLoader()
                }
                is ApiResponce.Loading -> {
                    Functions.showLoader(activity, false, false)
                }

            }
        })

        viewModel.deleteWaterMarkLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let {

                    }
                }
                is ApiResponce.Error -> {

                }
                is ApiResponce.Loading -> {

                }

            }
        })

        viewModel.repostLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponce.Success -> {
                    Functions.cancelLoader()
                    Functions.showToast(binding.root.context, "Successfully repost video!")
                    if (item?.repost != null && item?.repost == "0") {
                        item?.repost = "1"
                        try {
                            val msg = JSONObject(it.data)
                            val video = msg.getJSONObject("Video")
                            item?.repost_video_id = video.optString("repost_video_id", "0")
                            item?.repost_user_id = video.optString("repost_user_id", "0")
                        } catch (e: Exception) {
                            Log.d(Constants.tag, "Exception: $e")
                        }
                    } else {
                        item?.repost = "0"
                    }
                    setData()

                }
                is ApiResponce.Error -> {
                    Functions.cancelLoader()
                }
                is ApiResponce.Loading -> {
                    Functions.showLoader(activity, false, false)
                }

            }
        })

        viewModel.followLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponce.Success -> {
                    Functions.cancelLoader()
                    it.data?.let {
                        val follow_status = it.button!!.lowercase(Locale.getDefault())
                        item?.userModel?.button = it.button!!.lowercase(Locale.getDefault())
                        setFollowBtnStatus(it.id, follow_status)
                    }
                }
                is ApiResponce.Error -> {
                    Functions.cancelLoader()
                }
                is ApiResponce.Loading -> {
                    Functions.showLoader(activity, false, false)
                }

            }
        })
    }




    private fun updateImmediateViewChange() {

        try {
            if (item?.playlistId.equals("0"))
            {
               binding.ViewForPlaylist.setVisibility(View.GONE);
            }
            else
            {
                binding.ViewForPlaylist.setVisibility(View.VISIBLE);
            }
        }
        catch ( e:Exception){
            binding.ViewForPlaylist.setVisibility(View.GONE);
        }


        try {

            if (item!=null)
            {
                item?.let { model->
                    if (model.promotionModel != null && model.promotionModel!!.id != null) {
                        val destination = "${model.promotionModel!!.destination}"
                        binding.tabPromotionText.visibility = View.VISIBLE
                        if (destination == "website") {
                            updatePromotionSiteAction(binding.btnWebsiteMove)
                        } else if (destination == "follower") {
                            updatePromotionFollowAction(binding.btnWebsiteMove)
                        } else {
                            updatePromotionVideoViewAction(binding.btnWebsiteMove)
                        }
                    }
                }
            } else
            {
                binding.tabPromotionText.visibility = View.GONE
                binding.btnWebsiteMove.visibility = View.GONE
            }


        } catch (e: Exception) {
            binding.tabPromotionText.visibility = View.GONE
            binding.btnWebsiteMove.visibility = View.GONE
        }
    }

    private fun updatePromotionVideoViewAction(btnPromote: Button) {
        btnPromote.visibility = View.GONE
        btnPromote.setOnClickListener { }
    }

    private fun updatePromotionSiteAction(btnPromote: Button) {
        btnPromote.visibility = View.VISIBLE
        val actionButton = "${item?.promotionModel?.action_button}"
        btnPromote.text = when {
            actionButton.equals("Shop now", ignoreCase = true) -> binding.root.context.getString(R.string.shop_now)
            actionButton.equals("Sign up", ignoreCase = true) -> binding.root.context.getString(R.string.sign_up)
            actionButton.equals("Contact us", ignoreCase = true) -> binding.root.context.getString(R.string.contact_us)
            actionButton.equals("Apply now", ignoreCase = true) -> binding.root.context.getString(R.string.apply_now)
            actionButton.equals("Book now", ignoreCase = true) -> binding.root.context.getString(R.string.book_now)
            else -> binding.root.context.getString(R.string.learn_more)
        }
        btnPromote.setOnClickListener {
            item?.promotionModel?.id?.let { it1 -> viewModel.destinationTap(it1) }
        }
    }


    fun openWebUrl(title: String?, url: String?) {
        val intent = Intent(activity, WebviewActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }



    val mPermissionResult: ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        object : ActivityResultCallback<Map<String, Boolean>> {
            @RequiresApi(api = Build.VERSION_CODES.M)
            override fun onActivityResult(result: Map<String, Boolean>) {
                var allPermissionClear = true
                val blockPermissionCheck = mutableListOf<String>()
                for (key in result.keys) {
                    if (!result[key]!!) {
                        allPermissionClear = false
                        blockPermissionCheck.add(Functions.getPermissionStatus(requireActivity(), key))
                    }
                }
                if (blockPermissionCheck.contains("blocked")) {
                    Functions.showPermissionSetting(requireActivity(),
                        binding.root.context.getString(R.string.we_need_camera_and_recording_permission_for_make_video_on_sound))
                } else if (allPermissionClear) {
                    openSoundByScreen()
                }
            }
        }
    )



    private fun updatePromotionFollowAction(btnPromote: Button) {
        var follow_status = "${item?.userModel?.button}"
        if (follow_status.equals("null")) {
            follow_status = ""
        }
        btnPromote.visibility = when {
            follow_status.equals("following", ignoreCase = true) ||
                    follow_status.equals("friends", ignoreCase = true) -> View.GONE

            follow_status.equals("follow back", ignoreCase = true) -> {
                if (Variables.followMapList.containsKey("${item?.user_id}")) View.GONE else View.VISIBLE
            }
            else -> {
                if (Variables.followMapList.containsKey("${item?.user_id}")) View.GONE else View.VISIBLE
            }
        }
        btnPromote.setOnClickListener { }
    }

    var animationRunning = false
    fun initalize_views() {
        binding.ivAddFollow.setOnClickListener(DebounceClickHandler {
            if (Functions.checkLoginUser(activity)) {
                item?.let { it1 -> viewModel.followUser(it1) }
            }
        })
        binding.duetOpenVideo.setOnClickListener(DebounceClickHandler { openDuetVideo(item) })
        binding.userPic.setOnClickListener(DebounceClickHandler {
            onPause()

                openProfile(item)

        })
        binding.animateRlt.setOnClickListener(DebounceClickHandler {
            if (Functions.checkLoginUser(activity)) {
                binding.animateRlt.visibility = View.GONE
                likeVideo(item)
            }
        })
        binding.username.setOnClickListener(DebounceClickHandler {
            onPause()
            openProfile(item)
        })

        binding.productLayout.setOnClickListener {
            if(item?.tagProductList!=null && !item?.tagProductList?.isEmpty()!!) {
                val fragment = TaggedProductsListFragment.newInstance(object : FragmentCallBack {
                    override fun onResponce(bundle: Bundle?) {
                    }
                })
                val args = Bundle()
                args.putParcelable("data", item)
                fragment.arguments = args
                fragment.show(childFragmentManager, "TaggedProductsListF")
            }

        }
        binding.skipBtn.setOnClickListener(DebounceClickHandler { hideAd() })

        binding.locationLayout.setOnClickListener(DebounceClickHandler {
            onPause()
            openLocationVideo()
        })

        binding.commentLayout.setOnClickListener(DebounceClickHandler {
            if (Functions.checkLoginUser(activity)) {
                openComment(item)
            }
        })
        binding.sharedLayout.setOnClickListener(DebounceClickHandler { openShareVideoView() })
        binding.soundImageLayout.setOnClickListener(DebounceClickHandler(View.OnClickListener { view ->
            if (item == null || item?.user_id == null) {
                return@OnClickListener
            }
            if (item?.promotionModel != null && item?.promotionModel?.id != null) {
                Dialogs.showToastOnTop(
                    activity,
                    view,
                    binding.root.context.getString(R.string.video_ads_do_not_support_this_feature)
                )
                return@OnClickListener
            }

           try {
               activity?.let {
                   val takePermissionUtils = PermissionUtils(it, mPermissionResult)
                   if (takePermissionUtils.isCameraRecordingPermissionGranted) {
                       openSoundByScreen()
                   } else {
                       takePermissionUtils.showCameraRecordingPermissionDailog(
                           view.context.getString(
                               R.string.we_need_camera_and_recording_permission_for_make_video_on_sound
                           )
                       )
                   }
               }
           }catch (e:Exception){

           }
        }))
        binding.likebtn.setOnLikeListener(object : OnLikeListener {
            override fun liked(likeButton: LikeButton) {
                likeVideo(item)
            }

            override fun unLiked(likeButton: LikeButton) {
                likeVideo(item)
            }
        })
        binding.tabFavourite.setOnLikeListener(object : OnLikeListener {
            override fun liked(likeButton: LikeButton) {
                if (Functions.checkLoginUser(activity)) {
                    favouriteVideo(item)
                }
            }

            override fun unLiked(likeButton: LikeButton) {
                if (Functions.checkLoginUser(activity)) {
                    favouriteVideo(item)
                }
            }
        })


        CoroutineScope(Dispatchers.Main).launch {
            delay(200)
            setData()
        }

    }

    private fun setFollowBtnStatus(id: String?, follow_status: String?) {
        var follow_status = follow_status
        if (Functions.getSharedPreference(binding.root.context).getBoolean(Variables.IS_LOGIN, false)) {
            if (!id.equals(
                    Functions.getSharedPreference(binding.root.context)
                        .getString(Variables.U_ID, ""), ignoreCase = true
                )
            ) {
                if (follow_status == null) {
                    follow_status = ""
                }
                if (follow_status.equals("following", ignoreCase = true)) {
                    binding.ivAddFollow.visibility = View.GONE
                } else if (follow_status.equals("friends", ignoreCase = true)) {
                    binding.ivAddFollow.visibility = View.GONE
                } else if (follow_status.equals("follow back", ignoreCase = true)) {
                    if (Variables.followMapList.containsKey("${item?.user_id}")) {
                        binding.ivAddFollow.visibility = View.GONE
                    } else {
                        binding.ivAddFollow.visibility = View.VISIBLE
                    }
                } else {
                    if (Variables.followMapList.containsKey("${item?.user_id}")) {
                        binding.ivAddFollow.visibility = View.GONE
                    } else {
                        binding.ivAddFollow.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun openShareVideoView() {
        if (item == null || item?.user_id == null) {
            return
        }
        val fragment = VideoActionFragment("${item?.video_id}") { bundle ->
            when (bundle.getString("action")) {
                "save" -> {
                    FileUtils.createAppNameVideoDirectory(binding.root.context)
                   lifecycleScope.launch {
                        delay(500)
                        item?.video_id?.let { viewModel.downloadVideo(it) }
                    }
                }

                "repost" -> {
                    if (Functions.checkLoginUser(activity)) {
                        item?.video_id?.let { viewModel.repostVideo(it) }
                    }
                }

                "duet" -> {
                    if (Functions.checkLoginUser(activity)) {
                        item?.let { duetVideo(it) }
                    }
                }

                "privacy" -> {
                    onPause()
                    if (Functions.checkLoginUser(activity)) {
                        item?.let { openVideoSetting(it) }
                    }
                }

                "delete" -> {
                    if (Functions.checkLoginUser(activity)) {
                        item?.let { deleteListVideo(it) }
                    }
                }

                "editVideo" -> {
                    if (Functions.checkLoginUser(activity)) {
                        item?.let { openEditVideo(it) }
                    }
                }

                "favourite" -> {
                    if (Functions.checkLoginUser(activity)) {
                        favouriteVideo(item)
                    }
                }

                "not_intrested" -> {
                    if (Functions.checkLoginUser(activity)) {
                        item?.video_id?.let { viewModel.notInterestedVideo(it) }
                    }
                }

                "report" -> {
                    if (Functions.checkLoginUser(activity)) {
                        openVideoReport(item)
                    }
                }

                "promotion" -> {
                    if (Functions.checkLoginUser(activity)) {
                        item?.let { openVideoPromotion(it) }
                    }
                }

                "pinned" -> {
                    if (Functions.checkLoginUser(activity)) {
                        var pinnedVideo = Paper.book("PinnedVideo").read<HashMap<String?, HomeModel?>>("pinnedVideo") ?: HashMap()
                        if (pinnedVideo.containsKey("${item?.video_id}")) {
                            item?.let {
                                currentPinStatus = if (it.pin == "1") "0" else "1"
                                it.video_id?.let { viewModel.pinVideo(it, currentPinStatus) }
                            }
                        } else {
                            if (pinnedVideo.keys.size < 3) {
                                item?.let {
                                    currentPinStatus = if (it.pin == "1") "0" else "1"
                                    it.video_id?.let { viewModel.pinVideo(it, currentPinStatus) }
                                }
                            } else {
                                Toast.makeText(
                                    binding.root.context,
                                    binding.root.context.getString(R.string.only_three_video_pinned_is_allow),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
        val bundle = Bundle()
        bundle.putString("videoId", "${item?.video_id}")
        bundle.putString("userId", "${item?.userModel?.id}")
        bundle.putString("userName", "${item?.userModel?.username}")
        bundle.putString("userPic", "${item?.userModel?.getProfilePic()}")
        bundle.putString("fullName", "${item?.userModel?.first_name} ${item?.userModel?.last_name}")
        bundle.putParcelable("data", item)
        fragment.arguments = bundle
        fragment.show(childFragmentManager, "VideoActionF")
    }

    fun setData() {
         if(this::binding.isInitialized && item!=null) {

            if (activity == null)
                return
            else
            {
                item?.let { model->
                    binding.thumbImage.controller = Functions.frescoImageLoad(model.getThum(), binding.thumbImage, false)
                    binding.username.text = Functions.showUsernameOnVideoSection(model)
                    binding.userPic.controller = Functions.frescoImageLoad(model.userModel?.getProfilePic(), binding.userPic, false)

                    if (model.repost == "1") {
                        binding.tabRepost.visibility = View.VISIBLE
                        binding.ivRepostUser.controller = Functions.frescoImageLoad(
                            Functions.getSharedPreference(binding.root.context).getString(Variables.U_PIC, "null"), binding.ivRepostUser, false)
                    }
                    else {
                        binding.tabRepost.visibility = View.GONE
                    }
                    if (model.sound_name == null || model.sound_name == "" || model.sound_name == "null") {
                        model.setSound_pic(model.userModel?.getProfilePic())
                    }
                    setFollowBtnStatus(model.user_id, model.userModel?.button)
                    if (model.getSound_pic() == Constants.BASE_URL) {
                        binding.soundImage.controller = Functions.frescoImageLoad(
                            model.userModel?.getProfilePic(),
                            R.drawable.ic_round_music,
                            binding.soundImage,
                            false
                        )
                    }

                    else {
                        binding.soundImage.controller = Functions.frescoImageLoad(
                            model.getSound_pic(),
                            R.drawable.ic_round_music,
                            binding.soundImage,
                            false
                        )
                    }

                    Functions.printLog(Constants.tag, "tagProductList size: ${model.tagProductList?.size}")

                    if (model.tagProductList != null && !model.tagProductList!!.isEmpty()) {
                        binding.productLayout.visibility = View.VISIBLE
                        binding.tvProductName.text = "Order  ${model.tagProductList!![0].product.taggedName}"

                    }
                    else {
                        binding.productLayout.visibility=View.GONE
                    }

                    try {
                        if (!Functions.isStringHasValue(model.location_name)) {
                            binding.locationLayout.visibility = View.GONE
                        } else {
                            binding.locationLayout.visibility = View.VISIBLE
                            binding.locationTxt.text = "${model.location_name}"
                        }
                    }
                    catch (e: NullPointerException) {
                        e.printStackTrace()
                        Log.d(Constants.tag, e.message!!)
                    }

                    FriendsTagHelper.Creator.create(ContextCompat.getColor(binding.root.context, R.color.whiteColor), ContextCompat.getColor(binding.root.context, R.color.whiteColor)) { friendsTag ->
                        var friendsTag = friendsTag
                        onPause()
                        if (friendsTag.contains("#")) {
                            if (friendsTag[0] == '#') {
                                friendsTag = friendsTag.substring(1)
                                openHashtag(friendsTag)
                            }
                        } else if (friendsTag.contains("@")) {
                            if (friendsTag[0] == '@') {
                                friendsTag = friendsTag.substring(1)
                                openUserProfile(friendsTag)
                            }
                        }
                    }.handle(binding.descTxt)
                    val builder = ShowMoreLess.Builder(binding.root.context)
                        .textLengthAndLengthType(2, ShowMoreLess.TYPE_LINE)
                        .showMoreLabel(binding.root.context.getString(R.string.show_more))
                        .showLessLabel(binding.root.context.getString(R.string.show_less))
                        .showMoreLabelColor(Color.parseColor("#ffffff"))
                        .showLessLabelColor(Color.parseColor("#ffffff"))
                        .labelUnderLine(false)
                        .expandAnimation(true)
                        .enableLinkify(true)
                        .textClickable(false, false).build()
                    builder.addShowMoreLess(binding.descTxt, "${model.getVideoDescription()}", false)
                    binding.descTxt.scrollTo(0,0)
                    binding.descTxt.setOnClickListener {
                        if (builder.getContentExpandStatus()) builder.addShowMoreLess(
                            binding.descTxt,
                            "${model.getVideoDescription()}",
                            false
                        ) else builder.addShowMoreLess(
                            binding.descTxt, "${model.getVideoDescription()}", true
                        )
                    }
                    setLikeData()
                    setFavouriteData()
                    binding.tvShare.text = Functions.getSuffix("${model.share}")
                    if (("${model.user_id}").equals(Functions.getSharedPreference(binding.root.context).getString(Variables.U_ID, ""), ignoreCase = true)) {

                        binding.shareIcon.setImageDrawable(ContextCompat.getDrawable(binding.root.context, R.drawable.ic_black_dots))
                    }

                    else {
                        binding.shareIcon.setImageDrawable(ContextCompat.getDrawable(binding.root.context, R.drawable.ic_share))
                    }

                    if (model.allow_comments != null && model.allow_comments.equals("false", ignoreCase = true)) {

                        binding.commentLayout.visibility = View.GONE
                    }
                    else {
                        binding.commentLayout.visibility = View.VISIBLE
                    }


                    binding.commentTxt.text = Functions.getSuffix(model.video_comment_count)
                    if (model.userModel?.verified != null && model.userModel?.verified==1) {
                        binding.varifiedBtn.visibility = View.VISIBLE
                    }

                    else {
                        binding.varifiedBtn.visibility = View.GONE
                    }
                    if (model.duet_video_id != null && model.duet_video_id != "" && model.duet_video_id != "0") {
                        binding.duetLayoutUsername.visibility = View.VISIBLE
                        binding.duetUsername.text = "${model.duet_username}"
                    }
                    if (Functions.getSharedPreference(binding.root.context).getBoolean(Variables.IS_LOGIN, false)) {
                        binding.animateRlt.visibility = View.GONE
                    }
                }
            }
        }
    }

    fun setLikeData() {
        try {
            if ("${item?.liked}" == "1") {
                binding.likebtn.animate().start()
                binding.likebtn.setLikeDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context, R.drawable.ic_heart_gradient
                    )
                )
                binding.likebtn.isLiked = true
            } else {
                binding.likebtn.setLikeDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context, R.drawable.ic_unliked
                    )
                )
                binding.likebtn.isLiked = false
                binding.likebtn.animate().cancel()
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
        }
        binding.likeTxt.text = Functions.getSuffix("${item?.like_count}")
    }

    fun setFavouriteData() {
        try {
            if ("${item?.favourite}" == "1") {
                binding.tabFavourite.animate().start()
                binding.tabFavourite.setLikeDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context, R.drawable.ic_favourite
                    )
                )
                binding.tabFavourite.isLiked = true
            } else {
                binding.tabFavourite.setLikeDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context, R.drawable.ic_unfavourite
                    )
                )
                binding.tabFavourite.isLiked = false
                binding.tabFavourite.animate().cancel()
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
        }
        binding.tvFavourite.text = Functions.getSuffix(item!!.favourite_count)
    }

    private fun openVideoPromotion(item: HomeModel) {
        onPause()
        val intent = Intent(activity, VideoPromoteStepsActivity::class.java)
        intent.putExtra("modelData", item)
        startActivity(intent)
         requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    var currentPinStatus = "0"
    fun onPinSuccess(){
        try {
            item?.pin = currentPinStatus
            val bundle = Bundle()
            bundle.putString("action", "pinned")
            menuPager?.let {
                bundle.putInt("position", it.currentItem)
            }
            bundle.putString("pin", currentPinStatus)
            fragmentCallBack?.onResponce(bundle)

            var pinnedVideo = HashMap<String?, HomeModel?>()
            pinnedVideo = Paper.book("PinnedVideo").read("pinnedVideo") ?: HashMap()
            val itemUpdate = pinnedVideo[item?.video_id]
            pinnedVideo[itemUpdate!!.video_id] = itemUpdate
            Paper.book("PinnedVideo").write("pinnedVideo", pinnedVideo)
            menuPager?.let {
                val pagerAdapter = it.adapter as ViewPagerStatAdapter
                pagerAdapter.refreshStateSet(true)
                pagerAdapter.notifyDataSetChanged()
                pagerAdapter.refreshStateSet(false)
            }

        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun openSoundByScreen() {
        try {
            if (item==null || item!!.sound_id == null || item!!.sound_id == "0" || item!!.sound_id == "null") {
                return
            }
            val intent = Intent(activity, VideoSoundActivity::class.java)
            intent.putExtra("data", item)
             requireActivity().startActivity(intent)
        } catch (e: Exception) {
        }
    }

    private fun deleteListVideo(item: HomeModel?) {
        Functions.showLoader(activity, false, false)
        ApiRepository.callApiForDeleteVideo(activity, "${item?.video_id}", object : APICallBack {
            override fun arrayData(arrayList: ArrayList<*>?) {
                //return data in case of array list
            }

            override fun onSuccess(responce: String) {
                menuPager?.let {
                    val pagerAdapter = it.adapter as ViewPagerStatAdapter
                    val bundle = Bundle()
                    bundle.putString("action", "deleteVideo")
                    bundle.putInt("position", it.currentItem)
                    fragmentCallBack?.onResponce(bundle)
                    pagerAdapter.refreshStateSet(true)
                    pagerAdapter.removeFragment(it.currentItem)
                    pagerAdapter.refreshStateSet(false)
                }
            }

            override fun onFail(responce: String) {}
        })
    }


    private var resultVideoSettingCallback = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data!=null && data.getBooleanExtra("isShow", false)) {
                item?.let { viewModel.getVideoDetails(it) }
            }
        }
    }

    private fun openVideoSetting(item: HomeModel) {
        val intent = Intent(binding.root.context, PrivacyVideoSettingActivity::class.java)
        intent.putExtra("video_id", item.video_id)
        intent.putExtra("privacy_value", item.privacy_type)
        intent.putExtra("duet_value", item.allow_duet)
        intent.putExtra("comment_value", item.allow_comments)
        intent.putExtra("duet_video_id", item.duet_video_id)
        resultVideoSettingCallback.launch(intent)
        activity?.overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }




    var exoplayer: ExoPlayer? = null
    private fun initializePlayer() {
        if (exoplayer == null && item!=null ) {
            try {
                exoplayer = ExoPlayer.Builder(binding.root.context).setTrackSelector(
                    DefaultTrackSelector(
                        binding.root.context
                    )
                ).setLoadControl(Functions.getExoControler()).build()
                val videoURI = Uri.parse("${item!!.getVideo_url()}")
                val mediaItem = MediaItem.fromUri(videoURI)
                exoplayer!!.setMediaItem(mediaItem)
                exoplayer!!.prepare()
                if (item!!.promotionModel != null && item!!.promotionModel!!.id != null) {
                    exoplayer!!.repeatMode = Player.REPEAT_MODE_OFF
                } else {
                    exoplayer!!.repeatMode = Player.REPEAT_MODE_ALL
                }
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build()
                exoplayer!!.setAudioAttributes(audioAttributes, true)
                exoplayer!!.addListener(this@VideosPlayFragment)
                 requireActivity().runOnUiThread {
                    if (exoplayer != null) {
                        binding.playerview.player = exoplayer
                    }
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }
        }
    }

    fun setPlayer(isVisibleToUser: Boolean) {
        if (exoplayer != null) {
            if (exoplayer != null) {
                if (isVisibleToUser) {
                    exoplayer!!.playWhenReady = true
                } else {
                    exoplayer!!.playWhenReady = false
                    binding.playerview.findViewById<View>(R.id.exo_play).alpha = 1f
                }
            }
            binding.playerview.setOnTouchListener(object : OnSwipeTouchListener(binding.root.context) {
                override fun onSwipeLeft() {
                    openProfile(item)
                }

                override fun onLongClick() {
                    if (isVisibleToUser) {
                        showVideoOption(item)
                    }
                }

                override fun onSingleClick() {
                    if (!exoplayer!!.playWhenReady) {
                        exoplayer!!.playWhenReady = true
                        binding.playerview.findViewById<View>(R.id.exo_play).alpha = 0f
                    } else {
                        exoplayer!!.playWhenReady = false
                        binding.playerview.findViewById<View>(R.id.exo_play).alpha = 1f
                    }
                }

                override fun onDoubleClick(e: MotionEvent) {
                    if (!exoplayer!!.playWhenReady) {
                        exoplayer!!.playWhenReady = true
                    }
                    if (Functions.checkLoginUser(activity)) {
                        if (!animationRunning) {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(200)
                                if (!item!!.liked.equals("1", ignoreCase = true)) {
                                    likeVideo(item)
                                }
                                showHeartOnDoubleTap(item, binding.mainlayout, e)
                            }

                        }
                    }
                }
            })

            if ((item!!.promote != null && item!!.promote == "1") && showad) {
                item!!.promote = "0"
                showAd()
            } else {
                hideAd()
            }


        }
    }

    fun updateVideoView() {
        if (this::binding.isInitialized){
            if (Functions.getSharedPreference(binding.root.context).getBoolean(Variables.IS_LOGIN, false) && exoplayer != null) {
                val percentage = (exoplayer!!.currentPosition * 100 / exoplayer!!.duration).toInt()
                item?.video_id?.let {
                    ApiRepository.callApiForUpdateView(requireActivity(),
                        it, percentage)
                }
            }
        }
    }


    var isAddAlreadyShow: Boolean = false
    fun showAd() {
        binding.sideMenu.visibility = View.GONE
        binding.videoInfoLayout.visibility = View.GONE
        binding.soundImageLayout.visibility = View.GONE
        binding.skipBtn.setVisibility(View.VISIBLE)

        val bundle = Bundle()
        bundle.putString("action", "showad")
        fragmentCallBack!!.onResponce(bundle)

        countdownTimer(true)
    }


    var countDownTimer: CountDownTimer? = null
    fun countdownTimer(isStart: Boolean) {
        if (isStart) {
            if (countDownTimer == null) {
                countDownTimer = object : CountDownTimer((7 * 1000).toLong(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        if (activity != null) {
                            activity!!.runOnUiThread {
                                hideAd()
                                countdownTimer(false)
                            }
                        }
                    }
                }
                (countDownTimer as CountDownTimer).start()
            }
        } else {
            if (countDownTimer != null) {
                countDownTimer!!.cancel()
                countDownTimer = null
            }
        }
    }

    // hide the ad of video after some time
    fun hideAd() {
        isAddAlreadyShow = true
        binding.sideMenu.visibility = View.VISIBLE
        binding.videoInfoLayout.visibility = View.VISIBLE
        binding.soundImageLayout.visibility = View.VISIBLE

        binding.skipBtn.setVisibility(View.GONE)

        val bundle = Bundle()
        bundle.putString("action", "hidead")
        fragmentCallBack!!.onResponce(bundle)
    }


    var isVisibleToUser = false
    override fun setMenuVisibility(visible: Boolean) {
        isVisibleToUser = visible
        CoroutineScope(Dispatchers.Main).launch {
            delay(200)
            if (exoplayer != null && visible) {
                setPlayer(isVisibleToUser)
                item?.let {
                    viewModel.getVideoDetails(it)
                }
            }
        if (visible) {
            if (activity != null) {
                if (item != null && item!!.user_id != null) {
                    setLikeData()
                    setFavouriteData()
                    if (item!!.block == "1") {
                        binding.tvBlockVideoMessage.text = "" + item!!.aws_label
                        binding.tabBlockVideo.visibility = View.VISIBLE
                        onStop()
                    } else {
                        binding.tabBlockVideo.visibility = View.GONE
                    }
                }
            }
        }
        }
    }

    fun mainMenuVisibility(isvisible: Boolean) {
        if (exoplayer != null && isvisible) {
            exoplayer!!.playWhenReady = true
        } else if (exoplayer != null && !isvisible) {
            exoplayer!!.playWhenReady = false
            binding.playerview.findViewById<View>(R.id.exo_play).alpha = 1f
        }
    }

    // when we swipe for another video this will relaese the privious player
    fun releasePriviousPlayer() {
        if (exoplayer != null) {
            exoplayer!!.removeListener(this)
            exoplayer!!.release()
            exoplayer = null
        }
    }

    override fun onDestroy() {
        releasePriviousPlayer()
        super.onDestroy()
    }




    private fun openDuetVideo(item: HomeModel?) {
        val intent = Intent(activity, WatchVideosActivity::class.java)
        intent.putExtra("video_id", item!!.duet_video_id)
        intent.putExtra("position", 0)
        intent.putExtra("pageCount", 0)
        intent.putExtra(
            "userId",
            Functions.getSharedPreference(binding.root.context).getString(Variables.U_ID, "")
        )
        intent.putExtra("whereFrom", Variables.IdVideo)
        startActivity(intent)
    }

    // this will open the profile of user which have uploaded the currenlty running video
    private fun openHashtag(tag: String) {
        val intent = Intent(binding.root.context, TagedVideosActivity::class.java)
        intent.putExtra("tag", tag)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openLocationVideo() {
        val intent = Intent(binding.root.context, NearbyVideosActivity::class.java)
        intent.putExtra("subCategory", item!!.getLocation_string())
        intent.putExtra("placeId", item!!.locationId)
        intent.putExtra("locationName", item!!.location_name)
        intent.putExtra("lat", item!!.getLat())
        intent.putExtra("lng", item!!.getLng())
        intent.putExtra("locImage", item!!.location_image)
        Log.d(Constants.tag,"location image url : ${item!!.location_image}")
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    // this will open the profile of user which have uploaded the currenlty running video
    private fun openUserProfile(tag: String) {
        if (Functions.checkProfileOpenValidationByUserName(tag)) {
            val intent = Intent(binding.root.context, ProfileActivity::class.java)
            intent.putExtra("user_name", tag)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        }
    }

    override fun onPause() {
        super.onPause()
        if (exoplayer != null) {
            exoplayer!!.playWhenReady = false
            binding.playerview.findViewById<View>(R.id.exo_play).alpha = 1f
        }
    }

    override fun onStop() {
        super.onStop()
        if (exoplayer != null) {
            exoplayer!!.playWhenReady = false
            binding.playerview.findViewById<View>(R.id.exo_play).alpha = 1f
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super<Player.Listener>.onPlayerError(error)
        Log.d(Constants.tag, "Exception player: " + error.message)
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        super<Player.Listener>.onVideoSizeChanged(videoSize)
        Functions.printLog(
            Constants.tag,
            "${item?.video_id} width:" + videoSize.width + " height:" + videoSize.height
        )
        if (videoSize.width > videoSize.height) {
            binding.playerview.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        } else if (videoSize.height > 800) {
            binding.playerview.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        } else {
            binding.playerview.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING) {
            binding.pBar.visibility = View.VISIBLE
        } else if (playbackState == Player.STATE_READY) {
            binding.thumbImage.visibility = View.GONE
            binding.pBar.visibility = View.GONE
        } else if (playbackState == Player.STATE_ENDED) {
            if (item!!.promotionModel != null && item!!.promotionModel!!.id != null) {
                binding.tabPromotionEndView.visibility = View.VISIBLE
                updatePromotionSiteAction(binding.btnWebsiteMoveSecond)
                binding.tabInnerPromotionEndView.visibility = View.VISIBLE
                binding.tvReplay.setOnClickListener {
                    exoplayer!!.seekTo(0)
                    exoplayer!!.playWhenReady = true
                    binding.tabPromotionEndView.visibility = View.GONE
                }
            }
        }
    }

    // show a heart animation on double tap
    fun showHeartOnDoubleTap(
        item: HomeModel?,
        mainlayout: RelativeLayout?,
        e: MotionEvent
    ): Boolean {
        try {
             requireActivity().runOnUiThread {
                val x = e.x.toInt()
                val y = e.y.toInt()
                val lp = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                 requireNotNull(TicTicApp.appLevelContext) {
                     val iv = ImageView(TicTicApp.appLevelContext)
                     lp.setMargins(x, y, 0, 0)
                     iv.layoutParams = lp
                     if ("${item?.liked}" == "1") {
                         iv.setImageDrawable(
                             ContextCompat.getDrawable(
                                 binding.root.context,
                                 R.drawable.ic_heart_gradient
                             )
                         )
                     }
                     mainlayout!!.addView(iv)
                     iv.animate().alpha(0f).translationY(-200f).setDuration(500)
                         .setListener(object : AnimatorListenerAdapter() {
                             override fun onAnimationEnd(animation: Animator) {
                                 super.onAnimationEnd(animation)
                                 if (iv != null && mainlayout != null) {
                                     mainlayout.removeView(iv)
                                 }
                             }

                             override fun onAnimationCancel(animation: Animator) {
                                 super.onAnimationCancel(animation)
                                 if (iv != null && mainlayout != null) {
                                     mainlayout.removeView(iv)
                                 }
                             }
                         }).start()
                 }
            }
        } catch (excep: Exception) {
            Functions.printLog(Constants.tag, "Exception : $excep")
        }
        return true
    }

    // this function will call for like the video and Call an Api for like the video
    fun likeVideo(home_model: HomeModel?) {
        if (home_model == null || home_model.liked == null || home_model.like_count == null || home_model.liked == "null" || home_model.like_count == "null") {
            return
        }
        var action = home_model.liked
        if (action == "1") {
            action = "0"
            home_model.like_count = "" + (Functions.parseInterger(home_model.like_count) - 1)
        } else {
            action = "1"
            home_model.like_count = "" + (Functions.parseInterger(home_model.like_count) + 1)
        }
        home_model.liked = action
        setLikeData()
        ApiRepository.callApiForLikeVideo(activity, home_model.video_id, action, null)
    }

    fun sendComment(
        message: String,
        tagedUser: ArrayList<UserModel>,
        callback: FragmentCallBack
    ) {
        if (TextUtils.isEmpty(message)) {
            Functions.showToast(requireActivity(), "Please type your comment")
        } else if (Functions.checkLoginUser(activity)) {
            item?.video_id?.let { sendComments(it, message, tagedUser, callback) }
        }
    }

    // this function will call an api to upload your comment
    fun sendComments(
        videoId: String,
        comment: String,
        tagedUser: ArrayList<UserModel>,
        callback: FragmentCallBack
    ) {
        val fragment_data_send = arrayOf<FragmentDataSend?>(this)
        val comment_count = intArrayOf(
            Functions.parseInterger(
                item!!.video_comment_count
            )
        )
        ApiRepository.callApiForSendComment(
            requireActivity(),
            videoId,
            comment,
            tagedUser,
            object : APICallBack {
                override fun arrayData(arrayList: ArrayList<*>) {
                    val arrayList1 = arrayList as ArrayList<CommentModel>
                    for (item in arrayList1) {
                        comment_count[0]++
                        if (fragment_data_send[0] != null) {
                            fragment_data_send[0]!!.onDataSent(comment_count[0].toString())
                        }
                    }
                    val bundle = Bundle()
                    bundle.putString("type", "sended")
                    callback.onResponce(bundle)
                }

                override fun onSuccess(s: String) {
                    val bundle = Bundle()
                    bundle.putString("type", "sended")
                    callback.onResponce(bundle)
                }

                override fun onFail(s: String) {
                    val bundle = Bundle()
                    bundle.putString("type", "failed")
                    callback.onResponce(bundle)
                }
            })
    }

    // this will open the comment screen
    fun openComment(item: HomeModel?) {
        if (item == null || item.user_id == null || item.apply_privacy_model == null) {
            return
        }
        val comment_counnt = Functions.parseInterger(item.video_comment_count)
        val fragment_data_send: FragmentDataSend = this

        val fragment = CommentVideoFragment(comment_counnt, fragment_data_send)
        val args = Bundle()
        args.putString("video_id", item.video_id)
        args.putString("user_id", item.user_id)
        args.putParcelable("data", item)
        fragment.arguments = args
        fragment.show(childFragmentManager, "CommentF")
    }

    // this will open the profile of user which have uploaded the currenlty running video
    private fun openProfile(item: HomeModel?) {
        if (item == null || item.user_id == null) {
            return
        }
        if (Functions.checkProfileOpenValidation(item.user_id)) {

           videoListCallback = FragmentCallBack { bundle ->
                if (bundle.getBoolean("isShow")) {
                    item?.let { viewModel.getVideoDetails(it) }
                }
            }

                 val intent =  Intent(activity, ProfileActivity::class.java)
                 intent.putExtra("user_id", item.userModel?.id)
                 intent.putExtra("user_name", item.userModel?.username)
                 intent.putExtra("user_pic", item.userModel?.getProfilePic())
                 intent.putExtra("userModel", item.userModel)

            try {
                resultCallback?.launch(intent)
          }
            catch (e:Exception){
                startActivity(intent)
            }
            finally {
                requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }

       }


    }

    var resultCallback = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
                item?.let { viewModel.getVideoDetails(it) }
            }
        }
    }

    // show the diolge of video options
    private fun showVideoOption(homeModel: HomeModel?) {
        val alertDialog = Dialog(binding.root.context)
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val videoOptionBinding=AlertLabelEditorBinding.inflate(layoutInflater)
        alertDialog.setContentView(videoOptionBinding.root)
        alertDialog.window?.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    binding.root.context,
                    R.drawable.d_round_white_background
                )
            )

        if (homeModel!!.favourite != null && homeModel.favourite == "1")
            videoOptionBinding.favUnfavTxt.text = binding.root.context.getString(R.string.added_to_favourite)
        else
            videoOptionBinding.favUnfavTxt.text = binding.root.context.getString(R.string.add_to_favourite)

        if (homeModel.user_id.equals(
                Functions.getSharedPreference(binding.root.context).getString(Variables.U_ID, ""),
                ignoreCase = true
            )
        ) {
            videoOptionBinding.btnReport.visibility = View.GONE
            videoOptionBinding.btnNotInsterested.visibility = View.GONE
            videoOptionBinding.btnDelete.visibility = View.VISIBLE
        }

        videoOptionBinding.btnSave.setOnClickListener {
            alertDialog.dismiss()
            lifecycleScope.launch {
                delay(500)
                item?.video_id?.let { viewModel.downloadVideo(it) }
            }
        }
        videoOptionBinding.btnAddToFav.setOnClickListener {
            alertDialog.dismiss()
            if (Functions.checkLoginUser(activity)) {
                favouriteVideo(item)
            }
        }
        videoOptionBinding.btnNotInsterested.setOnClickListener {
            alertDialog.dismiss()
            if (Functions.checkLoginUser(activity)) {
                item?.video_id?.let { it1 -> viewModel.notInterestedVideo(it1) }

            }
        }
        videoOptionBinding.btnReport.setOnClickListener {
            alertDialog.dismiss()
            if (Functions.checkLoginUser(activity)) {
                openVideoReport(item)
            }
        }
        videoOptionBinding.btnDelete.setOnClickListener {
            alertDialog.dismiss()
            if (Functions.checkLoginUser(activity)) {
                deleteListVideo(item)
            }
        }

        alertDialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.7).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT)

        alertDialog.show()
    }

    // this method will be favourite the video
    fun favouriteVideo(item: HomeModel?) {
        if (item == null || item.favourite == null || item.favourite_count == null || item.favourite == "null" || item.favourite_count == "null") {
            return
        }
        var action = item.favourite
        if (action == "1") {
            action = "0"
            item.favourite_count = "" + (Functions.parseInterger(item.favourite_count) - 1)
        } else {
            action = "1"
            item.favourite_count = "" + (Functions.parseInterger(item.favourite_count) + 1)
        }
        item.favourite = action
        setFavouriteData()
        ApiRepository.callApiForFavouriteVideo(activity, item.video_id, action, null)
    }


    fun openVideoReport(home_model: HomeModel?) {
        onPause()
        val intent = Intent(binding.root.context, ReportTypeActivity::class.java)
        intent.putExtra("id", home_model!!.video_id)
        intent.putExtra("type", "video")
        intent.putExtra("isFrom", false)
        startActivity(intent)
         requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    fun openEditVideo(home_model: HomeModel?) {
        onPause()

        val intent = Intent(binding.root.context, PostVideoActivity::class.java)

        val bundle=Bundle()
        bundle.putParcelable("data", home_model)
        DataHolder.instance?.data=bundle

        intent.putExtra("from", "edit")
        try {
            resultCallback?.launch(intent)
        }
        catch (e:Exception){
            startActivity(intent)
        }

        requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    fun downLoadFromUrl(download_url:String){
        if (!download_url.isEmpty()) {
           val download_url = FileUtils.correctDownloadURL(download_url)

            var downloadDirectory = ""
            downloadDirectory = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                FileUtils.getAppFolder(binding.root.context)
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString() + File.separator + binding.root.context.getString(R.string.app_name) + File.separator + Variables.VideoDirectory + File.separator
            }
            Dialogs.showDeterminentLoader(
                activity,
                false,
                false
            )

            CoroutineScope(Dispatchers.IO).launch{
                val file=DownloadFiles.downloadFileWithProgress(download_url,
                    item?.video_id.toString(),
                    "mp4",
                    File(downloadDirectory),
                    progressCallback = {byteRead,contentLenth->
                        val prog =
                            (byteRead * 100 / contentLenth).toInt()
                        CoroutineScope(Dispatchers.Main).launch {
                            Dialogs.showLoadingProgress(prog)
                          }
                    })

                if(file?.exists() == true){
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        downloadAEVideo(downloadDirectory, item?.video_id + ".mp4")
                    } else {
                        viewModel.deleteWaterMarkVideo(download_url)
                        scanFile(downloadDirectory)
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        Dialogs.cancelDeterminentLoader()
                        Toast.makeText(activity, "Video Saved", Toast.LENGTH_LONG).show()
                    }
                }else {
                    CoroutineScope(Dispatchers.Main).launch {
                        Dialogs.cancelDeterminentLoader()
                     }
                }
            }


        }
    }

    fun downloadAEVideo(path: String, videoName: String) {
        val valuesvideos: ContentValues
        valuesvideos = ContentValues()
        valuesvideos.put(
            MediaStore.MediaColumns.RELATIVE_PATH,
            Environment.DIRECTORY_DCIM + File.separator + binding.root.context.getString(R.string.app_name) + File.separator + Variables.VideoDirectory
        )
        valuesvideos.put(MediaStore.MediaColumns.TITLE, videoName)
        valuesvideos.put(MediaStore.MediaColumns.DISPLAY_NAME, videoName)
        valuesvideos.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        valuesvideos.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
        valuesvideos.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
        valuesvideos.put(MediaStore.MediaColumns.IS_PENDING, 1)
        val resolver =  requireActivity().contentResolver
        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uriSavedVideo = resolver.insert(collection, valuesvideos)
        val pfd: ParcelFileDescriptor?
        try {
            pfd =  requireActivity().contentResolver.openFileDescriptor(uriSavedVideo!!, "w")
            val out = FileOutputStream(pfd!!.fileDescriptor)
            val imageFile = File(path + videoName)
            val `in` = FileInputStream(imageFile)
            val buf = ByteArray(1024)
            var len: Int
            while (`in`.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            out.close()
            `in`.close()
            pfd.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        valuesvideos.clear()
        valuesvideos.put(MediaStore.MediaColumns.IS_PENDING, 0)
         requireActivity().contentResolver.update(uriSavedVideo!!, valuesvideos, null, null)
    }


    fun scanFile(downloadDirectory: String) {
        MediaScannerConnection.scanFile(
            activity, arrayOf(downloadDirectory + item!!.video_id + ".mp4"),
            null
        ) { path, uri -> }
    }


    fun duetVideo(item: HomeModel) {
        Functions.printLog(Constants.tag, item.getVideo_url())
        if (item.getVideo_url() != null) {
            var downloadedFile = item.getVideo_url()
            if (downloadedFile!!.contains("file://")) {
                downloadedFile = item.getVideo_url()!!.replace("file://", "")
                val file = File(downloadedFile)
                if (file.exists()) {
                    val outputPath = FileUtils.getAppFolder(binding.root.context) + item.video_id + ".mp4"
                    copyDirectoryOneLocationToAnotherLocation(file, File(outputPath))
                }
            }
            val deletePath = FileUtils.getAppFolder(binding.root.context) + item.video_id + ".mp4"
            val deleteFile = File(deletePath)
            if (deleteFile.exists()) {
                openDuetRecording(item)
                return
            }
            Dialogs.showDeterminentLoader(
                activity,
                false,
                false
            )

            val outputDir=File(FileUtils.getAppFolder(binding.root.context))
            CoroutineScope(Dispatchers.IO).launch{
                val file=DownloadFiles.downloadFileWithProgress(item.getVideo_url().toString(),
                    item?.video_id.toString(),
                    "mp4",
                    outputDir,
                    progressCallback = {byteRead,contentLenth->
                        val prog =
                            (byteRead * 100 / contentLenth).toInt()
                        CoroutineScope(Dispatchers.Main).launch {
                            Dialogs.showLoadingProgress(prog)
                        }
                    })

                if(file?.exists() == true){
                    CoroutineScope(Dispatchers.Main).launch {
                        Dialogs.cancelDeterminentLoader()
                        openDuetRecording(item)
                       }
                }
            }

        }
    }

    private fun copyDirectoryOneLocationToAnotherLocation(
        sourceLocation: File,
        targetLocation: File
    ) {
        try {
            if (sourceLocation.isDirectory) {
                if (!targetLocation.exists()) {
                    targetLocation.mkdir()
                }
                val children = sourceLocation.list()
                for (i in sourceLocation.listFiles().indices) {
                    copyDirectoryOneLocationToAnotherLocation(
                        File(sourceLocation, children[i]),
                        File(targetLocation, children[i])
                    )
                }
            } else {
                val `in`: InputStream = FileInputStream(sourceLocation)
                val out: OutputStream = FileOutputStream(targetLocation)

                // Copy the bits from instream to outstream
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                `in`.close()
                out.close()
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
        }
    }

    fun openDuetRecording(item: HomeModel?) {
        val isOpenGLSupported = Functions.isOpenGLVersionSupported(binding.root.context, 0x00030001)
        if (isOpenGLSupported) {
            val intent = Intent(activity, VideoRecoderDuetActivity::class.java)
            intent.putExtra("data", item)
            startActivity(intent)
        } else {
            Toast.makeText(
                binding.root.context,
                binding.root.context.getString(R.string.your_device_opengl_verison_is_not_compatible_to_use_this_feature),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDataSent(yourData: String) {
        val comment_count = Functions.parseInterger(yourData)
        item?.video_comment_count = "" + comment_count
        binding.commentTxt.text = Functions.getSuffix(item!!.video_comment_count)
    }


    override fun onDetach() {
        super.onDetach()
        mPermissionResult.unregister()
    }

    companion object {
        @JvmField
        var videoListCallback: FragmentCallBack? = null
    }

}
