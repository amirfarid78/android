package com.coheser.app.activitesfragments.profile

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Dialog
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.WebviewActivity
import com.coheser.app.activitesfragments.accounts.AccountUtils
import com.coheser.app.activitesfragments.chat.ChatActivity
import com.coheser.app.activitesfragments.profile.followtabs.NotificationPriorityFragment
import com.coheser.app.activitesfragments.profile.likedvideos.LikedVideoFragment
import com.coheser.app.activitesfragments.profile.usersstory.ViewStoryA
import com.coheser.app.activitesfragments.profile.uservideos.UserVideoFragment
import com.coheser.app.activitesfragments.shoping.ShopA
import com.coheser.app.adapters.SuggestionAdapter
import com.coheser.app.adapters.ViewPagerAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityProfileBinding
import com.coheser.app.models.StoryModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.Functions.checkLoginUser
import com.coheser.app.simpleclasses.Functions.checkProfileOpenValidation
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.isShowContentPrivacy
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.OthersProfileViewModel
import java.util.Locale
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileActivity : AppCompatLocaleActivity() {

    var adapterSuggestion: SuggestionAdapter? = null
    var notificationType: String? = "1"
    var rootref: DatabaseReference? = null
    var fragmentUserVides: UserVideoFragment? = null
    var fragmentLikesVides: LikedVideoFragment? = null
    lateinit var binding: ActivityProfileBinding
    var isSuggestion = true
    var suggestionList = ArrayList<UserModel>()
    private var adapter: ViewPagerAdapter? = null

    private val viewModel: OthersProfileViewModel by viewModel()
    var isLikeVideoShow:Boolean = false

    var resultFollowCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {

            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode == RESULT_OK) {
                    val data = result?.data
                    if (data!!.getBooleanExtra("isShow", false)) {
                        viewModel.getUserDetails()
                    }
                }
            }
        })

    var userDetailModel:UserModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@ProfileActivity).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile_)
        binding.viewModel=viewModel
        binding.lifecycleOwner = this

        binding.notificationBtn.visibility = View.GONE
        if (intent.hasExtra("user_id")) {
            viewModel.userId = intent.getStringExtra("user_id")
        }

        if (intent.hasExtra("user_name")) {
            viewModel.userName = intent.getStringExtra("user_name")
        }
        if (intent.hasExtra("user_pic")) {
            viewModel.userPic = intent.getStringExtra("user_pic")
        }

        if(intent.hasExtra("userModel")){
            intent.getParcelableExtra<UserModel>("userModel")?.let {
                viewModel.setData(it)
            userDetailModel=it
            }
            setData()
        }


        val model=AccountUtils.getRecentProfileData(this)
        if (model!=null && model.id.equals(viewModel.userId)){
            viewModel.setData(model)
            userDetailModel=model
            setData()
        }
       else{
            binding.shimmerRoot.shimmerViewContainer.startShimmer()
        }


        init()
        setObserver()
    }


    public override fun onResume() {
        super.onResume()
        viewModel.getUserDetails()
    }

    fun setObserver(){
        viewModel.userDetailLiveData.observe(this,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {

                            userDetailModel = it


                            AccountUtils.saveRecentProfileData(this@ProfileActivity, it)

                            if (userDetailModel?.privacySettingModel?.likedVideos!!.toLowerCase().equals("only_me")) {
                                isLikeVideoShow = false
                            } else {
                                isLikeVideoShow = true
                            }
                            viewModel.isDirectMessage = isShowContentPrivacy(
                                this@ProfileActivity, userDetailModel?.privacySettingModel?.directMessage,
                                userDetailModel?.button.equals("friends", ignoreCase = true)
                            )

                            if (binding.bottomTabs.tabs.tabCount == 0) {
                                SetTabs()
                            }
                            setData()



                        }
                    }

                    binding.shimmerRoot.shimmerViewContainer.visibility = View.GONE
                    binding.dataLayout.visibility = View.VISIBLE

                }
                else -> {
                    binding.shimmerRoot.shimmerViewContainer.visibility = View.GONE
                    binding.dataLayout.visibility = View.VISIBLE
                }
            }
        })

        viewModel.blockUserLiveData.observe(this,{

            when(it){

                is ApiResponce.Success ->{
                    it.data?.let {

                        if(it.equals("1")){
                            userDetailModel?.block=it
                            showToast(this@ProfileActivity, getString(R.string.user_blocked))
                            viewModel.getUserDetails()
                        }

                }
                }

                is ApiResponce.Error ->{
                    if(it.message.equals("deleted")){
                            userDetailModel?.block="0"
                            showToast(this@ProfileActivity, getString(R.string.user_unblocked))
                        }
                        viewModel.getUserDetails()
                }

                else -> {}

            }
        })

        viewModel.suggesstionLiveData.observe(this,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        suggestionList.clear()
                        suggestionList.addAll(it)
                        adapterSuggestion?.notifyDataSetChanged()
                    }
                    hideSugestionButtonProgress()
                    if (suggestionList.isEmpty()) {
                        binding.tvNoSuggestionFound.visibility = View.VISIBLE
                    } else {
                        binding.tvNoSuggestionFound.visibility = View.GONE
                    }
                }
                is ApiResponce.Error -> {
                    hideSugestionButtonProgress()
                    if (suggestionList.isEmpty()) {
                        binding.tvNoSuggestionFound.visibility = View.VISIBLE
                    } else {
                        binding.tvNoSuggestionFound.visibility = View.GONE
                    }
                }

                else -> {}
            }

        })

        viewModel.followLiveData.observe(this,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let { userModel->
                        viewModel.getUserDetails()
                    }

                }
                else -> {}
            }
        })

        viewModel.followSuggesstionLiveData.observe(this,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let { userModel->
                        if (userModel != null) {
                            for (item in suggestionList) {
                                if(item.id.equals(userModel.id)){
                                    suggestionList.remove(item)
                                    adapterSuggestion?.notifyDataSetChanged()
                                    break
                                }
                            }
                        }
                    }

                }
                else -> {}
            }
        })

    }



    private fun showVideoOption() {
        val alertDialog = Dialog(this@ProfileActivity)
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setContentView(R.layout.item_report_user_dialog)
        val tabReportUser = alertDialog.findViewById<RelativeLayout>(R.id.tabReportUser)
        val tabBlockUser = alertDialog.findViewById<RelativeLayout>(R.id.tabBlockUser)
        val tabShareProfile = alertDialog.findViewById<RelativeLayout>(R.id.tabShareProfile)
        val tvBlockUser = alertDialog.findViewById<TextView>(R.id.tvBlockUser)


        if (userDetailModel?.block == "1")
            tvBlockUser!!.setText(getString(R.string.unblock_user))
        else
            tvBlockUser!!.setText(getString(R.string.block_user))

        tabShareProfile.setOnClickListener { v: View? ->
            alertDialog.dismiss()
            if (checkLoginUser(this@ProfileActivity)) {
                shareProfile()
            }
        }
        tabReportUser.setOnClickListener { v: View? ->
            alertDialog.dismiss()
            if (checkLoginUser(this@ProfileActivity)) {
                openUserReport()
            }
        }
        tabBlockUser.setOnClickListener { v: View? ->
            alertDialog.dismiss()
            if (checkLoginUser(this@ProfileActivity)) {
                viewModel.blockUser()
            }
        }
        alertDialog.show()
    }


    fun openUserReport() {
        val intent = Intent(this@ProfileActivity, ReportTypeActivity::class.java)
        intent.putExtra("id", viewModel.userId)
        intent.putExtra("type", "user")
        intent.putExtra("isFrom", false)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    private fun showLoadingProgressSuggestionButton() {
        val request = ImageRequestBuilder.newBuilderWithResourceId(R.raw.ic_progress_animation)
            .build()
        val controller: DraweeController = Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .setOldController(binding.suggestionBtn.controller)
            .setAutoPlayAnimations(true)
            .build()
        binding.suggestionBtn.controller = controller
    }

    private fun openChatF() {
        val intent = Intent(this@ProfileActivity, ChatActivity::class.java)
        intent.putExtra("user_id", viewModel.userId)
        intent.putExtra("user_name", viewModel.userName)
        intent.putExtra("user_pic", viewModel.userPic)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openProfileShareTab() {
        var isGif = false
        val mediaURL: String?
        if (userDetailModel?.getProfileGif() != null && userDetailModel?.getProfileGif() != Constants.BASE_URL) {
            isGif = true
            mediaURL = userDetailModel?.getProfileGif()
        } else {
            isGif = false
            mediaURL = userDetailModel?.getProfilePic()
        }
        val fragment = ShareAndViewProfileFragment(
                isGif,
                mediaURL,
                viewModel.userId,
                viewModel.userName,
            ) { bundle ->
                if (bundle.getString("action") == "profileShareMessage") {
                    if (checkLoginUser(this@ProfileActivity)) {
                        // firebase sharing
                    }
                }
            }
        fragment.show(supportFragmentManager, "")
    }

    private fun selectNotificationPriority() {
        var isFriend = false
        isFriend = binding.tvFollowBtn.visibility == View.GONE
        val f =
            NotificationPriorityFragment(
                notificationType,
                isFriend,
                viewModel.userName,
                viewModel.userId
            ) { bundle ->
                if (bundle.getBoolean("isShow", false)) {
                    notificationType = bundle.getString("type")
                    setUpNotificationIcon(notificationType)
                } else {
                    viewModel.getUserDetails()
                }
            }
        f.show(supportFragmentManager, "")
    }

    private fun setUpNotificationIcon(type: String?) {
        if (type.equals("1", ignoreCase = true)) {
            binding.notificationBtn.setImageDrawable(
                ContextCompat.getDrawable(
                    this@ProfileActivity, R.drawable.ic_live_notification
                )
            )
            binding.notificationBtn.visibility = View.GONE
        } else if (type.equals("0", ignoreCase = true)) {
            binding.notificationBtn.setImageDrawable(
                ContextCompat.getDrawable(
                    this@ProfileActivity, R.drawable.ic_mute_notification
                )
            )
            binding.notificationBtn.visibility = View.GONE
        }
    }

    private fun OpenSuggestionScreen() {
        val intent = Intent(this@ProfileActivity, FollowsMainTabActivity::class.java)
        intent.putExtra("id", viewModel.userId)
        intent.putExtra("from_where", "suggestion")
        intent.putExtra("userName", viewModel.userName)
        intent.putExtra("followingCount", userDetailModel?.following_count)
        intent.putExtra("followerCount", userDetailModel?.followers_count)
        resultFollowCallback.launch(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun hideSugestionButtonProgress() {
        val request = ImageRequestBuilder.newBuilderWithResourceId(R.drawable.ic_bottom_arrow)
            .build()
        val controller: DraweeController = Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .setOldController(binding.suggestionBtn.controller)
            .build()
        binding.suggestionBtn.controller = controller
    }

    fun init() {
        rootref = FirebaseDatabase.getInstance().reference

        binding.userImage.setOnClickListener(DebounceClickHandler {
            if (binding.circleStatusBar.visibility === View.VISIBLE) {
                openStoryDetail()
            } else {
                openProfileShareTab()
            }
        })


        binding.tabAllSuggestion.setOnClickListener(DebounceClickHandler { OpenSuggestionScreen() })
        binding.tabLink.setOnClickListener(DebounceClickHandler {
            openWebUrl(
                getString(R.string.web_browser),
                binding.tvLink.text.toString()
            )
        })
        binding.suggestionBtn.setOnClickListener(DebounceClickHandler {
            if (isSuggestion) {
                binding.suggestionBtn.animate().rotation(180f).setDuration(300)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            binding.tabSuggestion.visibility = View.VISIBLE
                            if (suggestionList.isEmpty()) {
                                showLoadingProgressSuggestionButton()
                                viewModel.getSuggesstionList()
                            }
                        }
                    }).start()
                isSuggestion = false
            } else {
                binding.suggestionBtn.animate().rotation(0f).setDuration(300)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            binding.tabSuggestion.visibility = View.GONE
                        }
                    }).start()
                isSuggestion = true
            }
        })
        binding.messageBtn.setOnClickListener {
            if (checkLoginUser(this@ProfileActivity)) {
                openChatF()
            }
        }
        binding.shopBtn.setOnClickListener { openShop() }
        binding.menuBtn.setOnClickListener(DebounceClickHandler { showVideoOption() })
        binding.notificationBtn.setOnClickListener(DebounceClickHandler { selectNotificationPriority() })
        binding.backBtn.setOnClickListener(DebounceClickHandler { onBackPressed() })
        binding.unFriendBtn.setOnClickListener(DebounceClickHandler {
            if (checkLoginUser(this@ProfileActivity)) {
                binding.unFriendBtn.visibility = View.GONE
                binding.shopBtn.visibility = View.GONE
                binding.tvFollowBtn.visibility = View.VISIBLE
                viewModel.followUser()
            }
        })
        binding.tvFollowBtn.setOnClickListener(DebounceClickHandler {
            if (checkLoginUser(this@ProfileActivity)) {
                binding.unFriendBtn.visibility = View.VISIBLE
                binding.shopBtn.visibility = View.VISIBLE
                binding.tvFollowBtn.visibility = View.GONE
                viewModel.followUser()
            }
        })
        setUpSuggestionRecyclerview()
        binding.followingLayout.setOnClickListener(DebounceClickHandler { openFollowing() })
        binding.fansLayout.setOnClickListener(DebounceClickHandler { openFollowers() })
    }
    private fun openShop() {
        val intent = Intent(binding.root.context, ShopA::class.java)
        intent.putExtra("name",viewModel.userModel.value!!.username)
        intent.putExtra("profile",viewModel.userModel.value!!.getProfilePic())
        intent.putExtra("id",viewModel.userModel.value!!.id)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    fun SetTabs() {
        adapter = ViewPagerAdapter(this)
        binding.bottomTabs.pager.setOffscreenPageLimit(3)
        registerFragmentWithPager()
        binding.bottomTabs.pager.setAdapter(adapter)
        addTabs()
        setupTabIcons()
        binding.bottomTabs.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomTabs.tabs.getTabAt(position)!!.select()
            }
        })
    }

    private fun addTabs() {
        val tabLayoutMediator = TabLayoutMediator(
            binding.bottomTabs.tabs, binding.bottomTabs.pager
        ) { tab, position ->
            if (position == 0) {
                tab.setText(getString(R.string.my_videos))
            } else if (position == 1) {
                tab.setText(getString(R.string.liked_videos))
            }
        }
        tabLayoutMediator.attach()
    }

    private fun registerFragmentWithPager() {
        fragmentUserVides = UserVideoFragment.newInstance(false, viewModel.userId.toString(), viewModel.userName.toString(), userDetailModel?.block.toString())
        adapter!!.addFrag(fragmentUserVides, getString(R.string.my_videos))

        fragmentLikesVides = LikedVideoFragment.newInstance(false, viewModel.userId.toString(),
            viewModel.userName.toString(), isLikeVideoShow,  userDetailModel?.block.toString())
        adapter!!.addFrag(fragmentLikesVides, getString(R.string.liked_videos))
    }

    private fun setUpSuggestionRecyclerview() {
        val layoutManager = LinearLayoutManager(this@ProfileActivity)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        binding.rvSugesstion.layoutManager = layoutManager
        adapterSuggestion = SuggestionAdapter(suggestionList) { view, postion, item ->
            if (view.id == R.id.tvFollowBtn) {
                if (checkLoginUser(this@ProfileActivity))
                    item.id?.let { viewModel.followSuggestionUser(it) }

            } else if (view.id == R.id.user_image) {
                if (checkProfileOpenValidation(item.id)) {
                    val intent = Intent(this@ProfileActivity, ProfileActivity::class.java)
                    intent.putExtra("user_id", item.id)
                    intent.putExtra("user_name", item.username)
                    intent.putExtra("user_pic", item.getProfilePic())
                    startActivity(intent)
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
                }
            } else if (view.id == R.id.ivCross) {
                suggestionList.removeAt(postion)
                adapterSuggestion!!.notifyDataSetChanged()
            }
        }
        binding.rvSugesstion.adapter = adapterSuggestion
    }


    private fun setupTabIcons() {
        val view1 = LayoutInflater.from(this@ProfileActivity).inflate(R.layout.item_tabs_profile_menu, null)
        val imageView1 = view1.findViewById<ImageView>(R.id.image)
        imageView1.setImageDrawable(
            ContextCompat.getDrawable(
               this@ProfileActivity,
                R.drawable.ic_my_video_select
            )
        )
        imageView1.setColorFilter(
            ContextCompat.getColor(this@ProfileActivity, R.color.black),
            PorterDuff.Mode.SRC_IN
        )
        binding.bottomTabs.tabs.getTabAt(0)!!.setCustomView(view1)

        val view3 = LayoutInflater.from(this@ProfileActivity).inflate(R.layout.item_tabs_profile_menu, null)
        val imageView3 = view3.findViewById<ImageView>(R.id.image)
        imageView3.setImageDrawable(ContextCompat.getDrawable(this@ProfileActivity!!, R.drawable.ic_liked_video_gray))
        imageView3.setColorFilter(
            ContextCompat.getColor(this@ProfileActivity!!, R.color.darkgray),
            PorterDuff.Mode.SRC_IN
        )
        binding.bottomTabs.tabs.getTabAt(1)!!.setCustomView(view3)

        binding.bottomTabs.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.bottomTabs.pager.setCurrentItem(tab.position, true)
                val v = tab.customView
                val image = v!!.findViewById<ImageView>(R.id.image)
                image.setColorFilter(
                    ContextCompat.getColor(this@ProfileActivity, R.color.black),
                    PorterDuff.Mode.SRC_IN
                )
                tab.setCustomView(v)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val v = tab.customView
                val image = v!!.findViewById<ImageView>(R.id.image)
                image.setColorFilter(
                    ContextCompat.getColor(this@ProfileActivity, R.color.darkgray),
                    PorterDuff.Mode.SRC_IN
                )
                tab.setCustomView(v)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    fun setData(){

        viewModel.userId = userDetailModel?.id
        userDetailModel?.let { viewModel.setData(it) }

        if (userDetailModel?.storyModel!=null) {
            binding.circleStatusBar.visibility = View.VISIBLE
            binding.circleStatusBar.counts=userDetailModel?.storyModel?.videoList?.size!!
        }
        else {
            binding.circleStatusBar.visibility = View.GONE
        }

        viewModel.userPic = userDetailModel?.getProfilePic()
        if (userDetailModel?.getProfileGif()!!.isEmpty()) {
            binding.userImage.controller =
                frescoImageLoad(userDetailModel?.getProfilePic(), binding.userImage, false)
        }
        else {
            binding.userImage.controller = frescoImageLoad(
                userDetailModel?.getProfileGif(),
                R.drawable.ic_user_icon,
                binding.userImage,
                true
            )
        }


        notificationType = userDetailModel?.notification
        setUpNotificationIcon(notificationType)

        val follow_status = userDetailModel?.button!!.lowercase(Locale.getDefault())
        if (follow_status.equals("following", ignoreCase = true)) {
            binding.unFriendBtn.visibility = View.VISIBLE
            binding.shopBtn.visibility = View.VISIBLE
            binding.tvFollowBtn.visibility = View.GONE
        }
        else if (follow_status.equals("friends", ignoreCase = true)) {
            binding.unFriendBtn.visibility = View.VISIBLE
            binding.shopBtn.visibility = View.VISIBLE
            binding.tvFollowBtn.visibility = View.GONE
        }
        else if (follow_status.equals("follow back", ignoreCase = true)) {
            binding.unFriendBtn.visibility = View.GONE
            binding.shopBtn.visibility = View.GONE
            binding.tvFollowBtn.visibility = View.VISIBLE
        }
        else {
            binding.unFriendBtn.visibility = View.GONE
            binding.shopBtn.visibility = View.GONE
            binding.tvFollowBtn.visibility = View.VISIBLE
        }


        if (userDetailModel?.block == "1") {
            binding.notificationBtn.visibility = View.GONE
            binding.tabFollowOtherUser.visibility = View.GONE
        } else {
            binding.notificationBtn.visibility = View.GONE
            binding.tabFollowOtherUser.visibility = View.VISIBLE
        }

    }


    fun openStoryDetail() {
        val list=ArrayList<StoryModel>().apply {
            viewModel.userModel?.value?.storyModel?.let { add(it) }
        }
        if(list.isNotEmpty()) {
            val myIntent = Intent(this@ProfileActivity, ViewStoryA::class.java)
            myIntent.putParcelableArrayListExtra("storyList", list) //Optional parameters
            myIntent.putExtra("position", 0) //Optional parameters
            startActivity(myIntent)
            overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
        }
    }

    private fun shareProfile() {
        var fromSetting = false
        fromSetting = viewModel.userId.equals(
            getSharedPreference(this@ProfileActivity).getString(Variables.U_ID, ""),
            ignoreCase = true
        )
        val fragment =
            ShareUserProfileFragment(
                viewModel.userId,
                viewModel.userName,
                userDetailModel?.first_name + userDetailModel?.last_name,
                viewModel.userPic,
                userDetailModel?.button!!.lowercase(Locale.getDefault()),
                viewModel.isDirectMessage,
                fromSetting
            ) { bundle ->
                if (bundle.getBoolean("isShow", false)) {
                    viewModel.getUserDetails()
                }
            }
        fragment.show(supportFragmentManager, "")
    }


    fun openWebUrl(title: String?, url: String?) {
        val intent = Intent(this@ProfileActivity, WebviewActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openFollowing() {
        val intent = Intent(this@ProfileActivity, FollowsMainTabActivity::class.java)
        intent.putExtra("id", viewModel.userId)
        intent.putExtra("from_where", "following")
        intent.putExtra("userName", viewModel.userName)
        intent.putExtra("followingCount", userDetailModel?.following_count)
        intent.putExtra("followerCount", userDetailModel?.followers_count)
        resultFollowCallback.launch(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    // open the followers screen
    private fun openFollowers() {
        val intent = Intent(this@ProfileActivity, FollowsMainTabActivity::class.java)
        intent.putExtra("id", viewModel.userId)
        intent.putExtra("from_where", "fan")
        intent.putExtra("userName", viewModel.userName)
        intent.putExtra("followingCount", userDetailModel?.following_count)
        intent.putExtra("followerCount", userDetailModel?.followers_count)
        resultFollowCallback.launch(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }


    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("isShow", true)
        setResult(RESULT_OK, intent)
       finish()
    }
}
