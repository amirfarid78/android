package com.coheser.app.mainmenu

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.WatchVideosActivity
import com.coheser.app.activitesfragments.accounts.LoginActivity
import com.coheser.app.activitesfragments.chat.ChatActivity
import com.coheser.app.activitesfragments.livestreaming.activities.MultiViewLiveActivity
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.viewModels.MainMenuViewModel
import com.coheser.app.adapters.ViewPagerAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityMainMenuBinding
import com.coheser.app.firebasenotification.NotificationActionHandler
import com.coheser.app.simpleclasses.ApiRepository
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Dialogs
import com.coheser.app.simpleclasses.FileUtils
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.PermissionUtils
import com.coheser.app.simpleclasses.Variables
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.coheser.app.activitesfragments.spaces.CreateRoomFragment
import com.coheser.app.activitesfragments.spaces.RiseHandForSpeakF
import com.coheser.app.activitesfragments.spaces.RiseHandUsersF
import com.coheser.app.activitesfragments.spaces.RoomDetailBottomSheet
import com.coheser.app.activitesfragments.spaces.models.HomeUserModel
import com.coheser.app.activitesfragments.spaces.models.TopicModel
import com.coheser.app.activitesfragments.spaces.services.RoomStreamService
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.MainStreamingModel
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.RoomApisListener
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.RoomFirebaseListener
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.RoomFirebaseManager
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.RoomManager
import com.coheser.app.models.InviteForSpeakModel
import com.coheser.app.models.UserModel
import com.volley.plus.VPackages.VolleyRequest
import io.paperdb.Paper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainMenuActivity : AppCompatLocaleActivity() {
    var mBackPressed: Long = 0
    var takePermissionUtils: PermissionUtils? = null
    private var adapter: ViewPagerAdapter? = null
    var rootRef: DatabaseReference? = null
    lateinit var binding: ActivityMainMenuBinding
    private val mainMenuViewModel: MainMenuViewModel by viewModel()

    lateinit var sharePreference:SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (e: Exception) {
        }
        Functions.setLocale(
            Functions.getSharedPreference(this@MainMenuActivity)
                .getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this,
            javaClass,
            false
        )
        binding = DataBindingUtil.setContentView(
            this@MainMenuActivity,
            R.layout.activity_main_menu
        )
        mainMenuActivity = this

        sharePreference=Functions.getSharedPreference(this)
        binding.lifecycleOwner = this

        SetTabs()
        initObservers()
        rootRef = FirebaseDatabase.getInstance().reference
        val intent = intent
        chechDeepLink(intent)
        if (intent != null && intent.hasExtra("type")) {
            val actionIntent = Intent(this, NotificationActionHandler::class.java).apply {
                putExtra("title", "" + intent.getStringExtra("title"))
                putExtra("body", "" + intent.getStringExtra("body"))
                putExtra("image", "" + intent.getStringExtra("image"))
                putExtra("receiver_id", "" + intent.getStringExtra("receiver_id"))
                putExtra("sender_id", "" + intent.getStringExtra("sender_id"))
                putExtra("user_id", "" + intent.getStringExtra("user_id"))
                putExtra("video_id", "" + intent.getStringExtra("video_id"))
                putExtra("type", "" + intent.getStringExtra("type"))
                putExtra("order_id", "" + intent.getStringExtra("order_id"))
                putExtra("tracking_link", "" + intent.getStringExtra("tracking_link"))
            }

            sendBroadcast(actionIntent)
        }

        publicIP


        CoroutineScope(Dispatchers.Default).launch {
            FileUtils.makeDirectry(FileUtils.getAppFolder(this@MainMenuActivity) + Variables.APP_HIDED_FOLDER)
            FileUtils.makeDirectry(FileUtils.getAppFolder(this@MainMenuActivity) + Variables.DRAFT_APP_FOLDER)
            FileUtils.makeDirectry(FileUtils.getAppFolder(this@MainMenuActivity) + Variables.APP_Gifts_Folder)
        }

        setIntent(null)

        if (Functions.getSharedPreference(this).getString(Variables.countryRegion, "null")
                .equals("null", ignoreCase = true)
        ) {
            val region = Functions.getCountryCode(this)
            Functions.getSharedPreference(this).edit().putString(
                Variables.countryRegion, region
            )
                .commit()
        }

        checkPostNotificationPermission()


        if(Functions.checkLogin(this)) {
            var checkData = Functions.getSettingsPreference(this).getString(Variables.selectedId, "")
            if (checkData.equals("")) {
                mainMenuViewModel.getAddressList()
            }
        }

        registerReceiver()

        mainMenuViewModel.getUnReadNotification()


    }


    fun initObservers(){
        mainMenuViewModel.deliveryAddressLiveData.observe(this){
            when (it) {
                is ApiResponce.Success ->{
                    it.data?.let { list ->
                        list.forEach{

                            if (it.defaultValue.equals("1")) {
                                Functions.printLog(Constants.tag,"deliveryAddressLiveData"+it.location_string)
                                Functions.getSettingsPreference(this@MainMenuActivity).edit()
                                    .putString(
                                        Variables.selectedId,
                                        it.id
                                    ).apply()
                                Paper.book().write(Variables.AdressModel, it)
                            }
                    }
                }}
                else -> {

                }
            }
        }

        mainMenuViewModel.userDetailLiveData.observe(this){
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        moveToProfile(
                            it.id!!, it.username!!, it.getProfilePic()!!
                        )
                    }

                }

                else -> {}
            }
        }

        mainMenuViewModel.userNotificationLiveData.observe(this){
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        Functions.printLog(Constants.tag,"Count:"+it)
                        showNotificationCount(it)
                    }

                }

                else -> {}
            }
        }

        mainMenuViewModel.tabFragments.observe(this) { fragments ->
            fragments?.let {
                adapter?.clearFragments()
                adapter?.setFragments(it)  // Update adapter with fragments
                addTabs()  // Attach tabs
                setupTabIcons()  // Setup icons for tabs
            }
        }

        // Observing tab position changes
        mainMenuViewModel.currentTabPosition.observe(this) { position ->
            tabLayout?.getTabAt(position)?.select()
        }
    }

    private fun checkPostNotificationPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            takePermissionUtils =
                PermissionUtils(
                    this@MainMenuActivity,
                    mPermissionPostNotificationResult
                )
            if (!takePermissionUtils!!.isPostNotificationPermissionGranted) {
                takePermissionUtils!!.takePostNotificationPermission()

            }
        }
    }


    fun SetTabs() {
        adapter = ViewPagerAdapter(this)
        tabLayout = findViewById(R.id.tabs)
        binding.viewpager.setOffscreenPageLimit(4)
        binding.viewpager.setAdapter(adapter)
        binding.viewpager.setUserInputEnabled(false)
    }

    private fun setupTabIcons() {
        val view1 = LayoutInflater.from(this@MainMenuActivity).inflate(R.layout.item_tablayout, null)
        val imageView1 = view1.findViewById<ImageView>(R.id.image)
        imageView1.setImageDrawable(
            ContextCompat.getDrawable(
                this@MainMenuActivity,
                R.drawable.ic_home_fill
            )
        )
        imageView1.setColorFilter(
            ContextCompat.getColor(this, R.color.colorwhite_50),
            PorterDuff.Mode.SRC_IN
        )
        tabLayout!!.getTabAt(0)!!.customView = view1


        val view2 =
            LayoutInflater.from(this@MainMenuActivity).inflate(R.layout.item_tablayout, null)
        val imageView2 = view2.findViewById<ImageView>(R.id.image)
        imageView2.setImageDrawable(
            ContextCompat.getDrawable(
                this@MainMenuActivity,
                R.drawable.ic_discover
            )
        )
        imageView2.setColorFilter(
            ContextCompat.getColor(this, R.color.darkgray),
            PorterDuff.Mode.SRC_IN
        )
        tabLayout!!.getTabAt(1)!!.customView = view2


        val view3 = LayoutInflater.from(this@MainMenuActivity).inflate(R.layout.item_add_tab_layout, null)
        tabLayout!!.getTabAt(2)!!.customView = view3


        val view4 = LayoutInflater.from(this@MainMenuActivity).inflate(R.layout.item_tablayout, null)
        val imageView4 = view4.findViewById<ImageView>(R.id.image)
        imageView4.setImageDrawable(
            ContextCompat.getDrawable(
                this@MainMenuActivity,
                R.drawable.ic_normal_notification
            )
        )
        imageView4.setColorFilter(
            ContextCompat.getColor(this, R.color.darkgray),
            PorterDuff.Mode.SRC_IN
        )
        tabLayout!!.getTabAt(3)!!.customView = view4


        val view5 = LayoutInflater.from(this@MainMenuActivity)
            .inflate(R.layout.item_tablayout_profile, null)
        val imageView5 = view5.findViewById<SimpleDraweeView>(R.id.image)
        if (sharePreference.getBoolean(Variables.IS_LOGIN, false)) {
            val pic_url = sharePreference.getString(Variables.U_PIC, "null")
            imageView5.controller = Functions.frescoImageLoad(pic_url, imageView5, false)
        }
        tabLayout!!.getTabAt(4)!!.customView = view5

        tabLayout!!.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {

                tab.position?.let { binding.viewpager.setCurrentItem(it, false) }

                val v = tab.customView
                val image = v!!.findViewById<ImageView>(R.id.image)

                when (tab.position) {
                    0 -> {
                        image.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@MainMenuActivity,
                                R.drawable.ic_home_fill
                            )
                        )
                        onHomeClick()
                    }

                    1 -> {

                        onotherTabClick()
                        image.setColorFilter(
                            ContextCompat.getColor(this@MainMenuActivity, R.color.black),
                            PorterDuff.Mode.SRC_IN
                        )

                    }

                    3 -> {

                        onotherTabClick()
                        image.setColorFilter(
                            ContextCompat.getColor(this@MainMenuActivity, R.color.black),
                            PorterDuff.Mode.SRC_IN
                        )
                        showNotificationCount("0")
                    }

                    4 -> {

                        onotherTabClick()

                    }
                }


                tab.customView = v
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val v = tab.customView
                val image = v!!.findViewById<ImageView>(R.id.image)
                when (tab.position) {
                    0 -> {

                        image.setColorFilter(
                            ContextCompat.getColor(this@MainMenuActivity, R.color.darkgray),
                            PorterDuff.Mode.SRC_IN
                        )
                    }

                    1 -> {


                        image.setColorFilter(
                            ContextCompat.getColor(this@MainMenuActivity, R.color.darkgray),
                            PorterDuff.Mode.SRC_IN
                        )

                    }

                    3 -> {
                        image.setColorFilter(
                            ContextCompat.getColor(this@MainMenuActivity, R.color.darkgray),
                            PorterDuff.Mode.SRC_IN
                        )
                    }

                    4 -> {
                        if(sharePreference.getBoolean(Variables.ISBusinessProfile,false)) {
                            window.statusBarColor = ContextCompat.getColor(
                                this@MainMenuActivity,
                                R.color.whiteColor
                            )
                        }

                    }

                }


                tab.customView = v
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        window.navigationBarColor = ContextCompat.getColor(this, R.color.blackColor)


        val tabStrip = tabLayout!!.getChildAt(0) as LinearLayout
        tabStrip.isEnabled = false

        tabStrip.getChildAt(2).isClickable = false
        view3.setOnClickListener { v: View? ->
            takePermissionUtils =
                PermissionUtils(
                    this@MainMenuActivity,
                    mPermissionResult
                )
            if (takePermissionUtils!!.isStorageCameraRecordingPermissionGranted) {
                uploadNewVideo()
            } else {
                takePermissionUtils!!.showStorageCameraRecordingPermissionDailog(getString(R.string.we_need_storage_camera_recording_permission_for_make_new_video))
            }
        }


        tabStrip.getChildAt(3).isClickable = false
        view4.setOnClickListener { v: View? ->
            if (Functions.checkLoginUser(this@MainMenuActivity)) {
                val tab = tabLayout!!.getTabAt(3)
                tab!!.select()
            }
        }


        tabStrip.getChildAt(4).isClickable = false
        view5.setOnClickListener { v: View? ->
            if (Functions.checkLoginUser(this@MainMenuActivity)) {
                val tab = tabLayout!!.getTabAt(4)
                tab!!.select()
            }
        }


        if (intent != null) {
            if (intent.hasExtra("action_type")) {
                if (Functions.getSharedPreference(this@MainMenuActivity).getBoolean(
                        Variables.IS_LOGIN, false
                    )
                ) {
                    val action_type = intent.extras!!.getString("action_type")
                    if (action_type == "message") {
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1500)
                            val tab = tabLayout!!.getTabAt(3)
                            tab!!.select()
                        }
                        val id = intent.extras!!.getString("senderid")
                        val name = intent.extras!!.getString("title")
                        val icon = intent.extras!!.getString("icon")
                        chatFragment(id, name, icon)
                    }
                }
            }
        }

    }

    private fun addTabs() {
        val tabLayoutMediator =
            TabLayoutMediator(tabLayout!!, binding.viewpager) { tab, position ->
                if (position == 0) {
                    tab.text = getString(R.string.home)
                } else if (position == 1) {
                    tab.text = getString(R.string.discover)
                } else if (position == 2) {
                    tab.text = getString(R.string.upload)
                } else if (position == 3) {
                    tab.text = getString(R.string.notifications)
                } else if (position == 4) {
                    tab.text = getString(R.string.profile)
                }
            }
        tabLayoutMediator.attach()
    }


    // add the listener of home bth which will open the recording screen
    fun onHomeClick() {

        Functions.blackStatusBar(this)

        val tab0 = tabLayout!!.getTabAt(0)
        val view0 = tab0!!.customView
        val imageView0 = view0!!.findViewById<ImageView>(R.id.image)
        imageView0.setColorFilter(
            ContextCompat.getColor(this, R.color.colorwhite_50),
            PorterDuff.Mode.SRC_IN
        )
        tab0.customView = view0


        val tab1 = tabLayout!!.getTabAt(1)
        val view1 = tab1!!.customView
        val imageView1 = view1!!.findViewById<ImageView>(R.id.image)
        imageView1.setColorFilter(
            ContextCompat.getColor(this, R.color.darkgray),
            PorterDuff.Mode.SRC_IN
        )
        tab1.customView = view1


        val tab2 = tabLayout!!.getTabAt(2)
        val view2 = tab2!!.customView
        tab2.customView = view2


        val tab3 = tabLayout!!.getTabAt(3)
        val view3 = tab3!!.customView
        val imageView3 = view3!!.findViewById<ImageView>(R.id.image)
        imageView3.setColorFilter(
            ContextCompat.getColor(this, R.color.darkgray),
            PorterDuff.Mode.SRC_IN
        )
        tab3.customView = view3


        val tab4 = tabLayout!!.getTabAt(4)
        val view4 = tab4!!.customView
        val image4 = view4!!.findViewById<SimpleDraweeView>(R.id.image)

        if (sharePreference.getBoolean(Variables.IS_LOGIN, false)) {
            val pic_url = sharePreference.getString(Variables.U_PIC, "null")
            image4.controller = Functions.frescoImageLoad(pic_url, image4, false)
        }
        tab4.customView = view4

        tabLayout!!.setBackgroundColor(ContextCompat.getColor(this, R.color.blackColor))
        window.navigationBarColor = ContextCompat.getColor(this, R.color.blackColor)
    }

    // profile and notification tab click listener handler when user is not login into com
    fun onotherTabClick() {
        Functions.whiteStatusBar(this, tabLayout!!.selectedTabPosition)


        val tab0 = tabLayout!!.getTabAt(0)
        val view0 = tab0!!.customView
        val imageView0 = view0!!.findViewById<ImageView>(R.id.image)
        imageView0.setColorFilter(
            ContextCompat.getColor(this, R.color.darkgray),
            PorterDuff.Mode.SRC_IN
        )
        tab0.customView = view0

        val tab1 = tabLayout!!.getTabAt(1)
        val view1 = tab1!!.customView
        val imageView1 = view1!!.findViewById<ImageView>(R.id.image)
        imageView1.setColorFilter(
            ContextCompat.getColor(this, R.color.darkgray),
            PorterDuff.Mode.SRC_IN
        )
        tab1.customView = view1


        val tab2 = tabLayout!!.getTabAt(2)
        val view2 = tab2!!.customView
        tab2.customView = view2


        val tab3 = tabLayout!!.getTabAt(3)
        val view3 = tab3!!.customView
        val imageView3 = view3!!.findViewById<ImageView>(R.id.image)
        imageView3.setColorFilter(
            ContextCompat.getColor(this, R.color.darkgray),
            PorterDuff.Mode.SRC_IN
        )
        tab3.customView = view3


        val tab4 = tabLayout!!.getTabAt(4)
        val view4 = tab4!!.customView
        val image4 = view4!!.findViewById<SimpleDraweeView>(R.id.image)
        if (sharePreference.getBoolean(Variables.IS_LOGIN, false)
        ) {
            val pic_url = sharePreference.getString(Variables.U_PIC, "null")
            image4.controller = Functions.frescoImageLoad(pic_url, image4, false)
        }
        tab4.customView = view4


        tabLayout!!.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
    }


    fun showNotificationCount(count:String){
        if (tabLayout != null) {
            val tabview = tabLayout!!.getTabAt(3)!!.customView
            val tabNotificationCount=tabview!!.findViewById<RelativeLayout>(R.id.tabNotificationCount)
            val count_txt = tabview!!.findViewById<TextView>(R.id.tvNotificationCount)
            if (count.toInt() > 0) {
                tabNotificationCount.visibility = View.VISIBLE
                count_txt.text = "" + count
            } else {
                tabNotificationCount.visibility = View.GONE
            }
            tabLayout!!.getTabAt(3)!!.customView = tabview
        }
    }


//    var flowJob: Job?=null
//    fun getFlowValues() {
//        flowJob = lifecycleScope.launch {
//            flow.collect {
//            }
//        }
//    }
//
//
//    lateinit var flow: Flow<Int>
//    fun startFlowToGetUnreadNotification() {
//        flow = flow {
//            while (true){
//                if(TicTicApp.appInForeground) {
//                    mainMenuViewModel.getUnReadNotification()
//                }
//                delay(30000)
//            }
//        }
//    }
//



    // open the chat fragment when click on notification of message
    fun chatFragment(receiverid: String?, name: String?, picture: String?) {
        val intent = Intent(this@MainMenuActivity, ChatActivity::class.java).apply {
            putExtra("user_id", receiverid)
            putExtra("user_name", name)
            putExtra("user_pic", picture)
        }
        resultChatCallback.launch(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun uploadNewVideo() {
        FileUtils.makeDirectry(
            FileUtils.getAppFolder(this@MainMenuActivity) + Variables.APP_HIDED_FOLDER
        )
        FileUtils.makeDirectry(
            FileUtils.getAppFolder(this@MainMenuActivity) + Variables.DRAFT_APP_FOLDER
        )
        if (Functions.checkLoginUser(this@MainMenuActivity)) {
            val giftFragment =
                com.coheser.app.activitesfragments.videorecording.CreateContentFragment(
                    object : com.coheser.app.interfaces.FragmentCallBack {
                        override fun onResponce(bundle: Bundle?) {
                            if(bundle!=null){
                                createSpace()
                            }
                        }
                    })
            giftFragment.show(supportFragmentManager, "")

        }
    }



    fun createSpace(){

        val fragment = CreateRoomFragment { bundle ->
            if (bundle.getString("action", "") == "genrateRoom") {
                roomManager?.selectedInviteFriends = bundle.getSerializable("selectedFriends") as ArrayList<UserModel>
                roomManager?.selectedTopics = bundle.getSerializable("topics") as ArrayList<TopicModel>
                roomManager?.roomName = bundle.getString("roomName")
                roomManager?.privacyType = bundle.getString("privacyType")
                roomManager?.checkMyRoomJoinStatus("create", "")
            }
        }

        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(
            R.anim.in_from_bottom, R.anim.out_to_top,
            R.anim.in_from_top, R.anim.out_from_bottom
        )
        ft.replace(R.id.mainMenuFragment, fragment, "CreateRoomF")
            .addToBackStack("CreateRoomF").commit()

    }


    private val mPermissionResult: ActivityResultLauncher<Array<String>> =
        registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allPermissionClear = true
            val blockPermissionCheck: MutableList<String> = ArrayList()
            for (key in result.keys) {
                if (!result[key]!!) {
                    allPermissionClear = false
                    blockPermissionCheck.add(
                        Functions.getPermissionStatus(
                            this@MainMenuActivity,
                            key
                        )
                    )
                }
            }
            if (blockPermissionCheck.contains("blocked")) {
                Functions.showPermissionSetting(
                    this,
                    getString(R.string.we_need_storage_camera_recording_permission_for_make_new_video)
                )
            } else if (allPermissionClear) {
                uploadNewVideo()
            }
        }


    private val mPermissionPostNotificationResult =
        registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allPermissionClear = true
            val blockPermissionCheck: MutableList<String> = ArrayList()
            for (key in result.keys) {
                if (!result[key]!!) {
                    allPermissionClear = false
                    blockPermissionCheck.add(
                        Functions.getPermissionStatus(
                            this@MainMenuActivity,
                            key
                        )
                    )
                }
            }
            if (blockPermissionCheck.contains("blocked")) {
                Functions.showPermissionSetting(
                    this,
                    getString(R.string.we_need_location_permission_to_show_you_nearby_contents)
                )
            } else if (allPermissionClear) {
            }
        }


    var streamId = ""
    var spaceId = ""
    private fun chechDeepLink(intent: Intent?) {
        try {
            val uri = intent!!.data
            val linkUri = "" + uri
            var userId = ""
            var videoId = ""
            val profileURL =
                Variables.https + "://" + getString(R.string.domain) + getString(
                    R.string.share_profile_endpoint_second
                )
            val videoURL =
                Variables.https + "://" + getString(R.string.domain) + getString(
                    R.string.share_video_endpoint_second
                )
            val streamURL =
                Variables.https + "://" + getString(R.string.domain) + getString(
                    R.string.share_stream_endpoint_second
                )

            val spaceURL =
                Variables.https + "://" + getString(R.string.domain) + getString(
                    R.string.share_space_endpoint_second
                )


            if (linkUri.contains(streamURL) && linkUri.contains("&")) {
                val extractedPart = linkUri.replaceFirst(getString(R.string.share_stream_endpoint_second), "")

                val regex = Regex("[A-Za-z]+(\\d+)[A-Za-z]+") // Extracts numbers between letters
                val matchResult = regex.find(extractedPart)
                if (matchResult != null) {
                    streamId = matchResult.groupValues[1]
                    streamingOpen()
                }
            } else if (linkUri.contains(spaceURL)) {
                val parts =
                    linkUri.split(spaceURL.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                spaceId = parts[1]
            } else if (linkUri.contains(profileURL)) {
                val parts = linkUri.split(profileURL.toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                var userName = parts[1]
                mainMenuViewModel.getUserDetails(userName)

            } else if (linkUri.contains(getString(R.string.share_referal_code))) {
                Log.d(Constants.tag, "Link : $linkUri")
                val parts =
                    linkUri.split("code=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                userId = parts[1]
                OpenRegisterationScreen(userId)
            } else if (linkUri.contains(videoURL)) {
                val extractedPart = linkUri.replaceFirst(getString(R.string.share_profile_endpoint_second), "")

                val regex = Regex("[A-Za-z]+(\\d+)[A-Za-z]+") // Extracts numbers between letters
                val matchResult = regex.find(extractedPart)
                if (matchResult != null) {
                    videoId = matchResult.groupValues[1]
                    openWatchVideo(videoId)
                }
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception Link : $e")
        }
    }


    private fun OpenRegisterationScreen(referalCode: String) {
        Functions.hideSoftKeyboard(this@MainMenuActivity)

        val intent = Intent(this@MainMenuActivity, LoginActivity::class.java).apply {
            putExtra("referalCode", referalCode)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)

    }

    private fun streamingOpen() {
        takePermissionUtils = PermissionUtils(
            this@MainMenuActivity,
            mPermissionStreamResult
        )
        if (takePermissionUtils!!.isCameraRecordingPermissionGranted) {
            goLive(streamId)
        } else {
            takePermissionUtils!!.showCameraRecordingPermissionDailog(getString(R.string.we_need_camera_and_recording_permission_for_live_streaming))
        }
    }

    private val mPermissionStreamResult =
        registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allPermissionClear = true
            val blockPermissionCheck: MutableList<String> = ArrayList()
            for (key in result.keys) {
                if (!result[key]!!) {
                    allPermissionClear = false
                    blockPermissionCheck.add(
                        Functions.getPermissionStatus(
                            this@MainMenuActivity,
                            key
                        )
                    )
                }
            }
            if (blockPermissionCheck.contains("blocked")) {
                Functions.showPermissionSetting(
                    this,
                    getString(R.string.we_need_camera_and_recording_permission_for_live_streaming)
                )
            } else if (allPermissionClear) {
                goLive(streamId)
            }
        }


    private fun goLive(streamId: String) {
        Log.d(Constants.tag, "StreamingID: $streamId")
        rootRef!!.child("LiveStreamingUsers")
            .child(streamId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val selectLiveModel = snapshot.getValue(
                            com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel::class.java
                        )
                        runOnUiThread { joinStream(selectLiveModel) }

                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@MainMenuActivity,
                                getString(R.string.user) + " " + getString(R.string.is_offline_now),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }


    private fun joinStream(selectLiveModel: com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel?) {
        Log.d(Constants.tag, "getOnlineType: " + selectLiveModel!!.getOnlineType())

            val dataList =
                ArrayList<com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel?>()
            dataList.add(selectLiveModel)
            val intent = Intent().apply {
                putExtra("user_id", selectLiveModel.getUserId())
                putExtra("user_name", selectLiveModel.getUserName())
                putExtra("user_picture", selectLiveModel.getUserPicture())
                putExtra("user_role", io.agora.rtc2.Constants.CLIENT_ROLE_AUDIENCE)
                putExtra("onlineType", "multicast")
                putExtra("description", selectLiveModel.getDescription())
                putExtra("secureCode", "")
                putExtra("dataList", dataList)
                putExtra("position", 0)
               setClass(this@MainMenuActivity,
                    MultiViewLiveActivity::class.java
                )
            }

            startActivity(intent)

    }


    private fun openWatchVideo(videoId: String) {
        val intent = Intent(this@MainMenuActivity,
            WatchVideosActivity::class.java
        ).apply {
            putExtra("video_id", videoId)
            putExtra("position", 0)
            putExtra("pageCount", 0)
            putExtra("userId",
                Functions.getSharedPreference(this@MainMenuActivity).getString(
                    Variables.U_ID, ""
                )
            )
            putExtra("whereFrom", Variables.IdVideo)
        }
        startActivity(intent)

    }


    private fun moveToProfile(id: String, username: String, pic: String) {
        if (Functions.checkProfileOpenValidation(id)) {
            val intent = Intent(
                this@MainMenuActivity,
                ProfileActivity::class.java
            )
            intent.putExtra("user_id", id)
            intent.putExtra("user_name", username)
            intent.putExtra("user_pic", pic)
            startActivity(intent)
            overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        chechDeepLink(intent)
        if (intent != null) {
            val type = intent.getStringExtra("type")
            if (type != null && type.equals("message", ignoreCase = true)) {

                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    val chatIntent = Intent(
                        this@MainMenuActivity,
                        ChatActivity::class.java
                    )
                    chatIntent.putExtra("user_id", intent.getStringExtra("user_id"))
                    chatIntent.putExtra("user_name", intent.getStringExtra("user_name"))
                    chatIntent.putExtra("user_pic", intent.getStringExtra("user_pic"))
                    resultChatCallback.launch(chatIntent)
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)

                }
            }
        }
    }

    var resultChatCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
            }
        }
    }

    val publicIP: Unit
        get() {
            VolleyRequest.JsonGetRequest(this, "https://api.ipify.org/?format=json") { s ->
                try {
                    val responce = JSONObject(s)
                    val ip = responce.optString("ip")
                    Functions.getSharedPreference(this@MainMenuActivity).edit()
                        .putString(Variables.DEVICE_IP, ip).commit()
                    if (Functions.getSharedPreference(this@MainMenuActivity).getString(Variables.DEVICE_TOKEN, "").equals("", ignoreCase = true)) {
                        addFirebaseToken()
                    } else {
                        if (Functions.getSharedPreference(this).getBoolean(Variables.IS_LOGIN, false)) {
                            ApiRepository.addDeviceData(this@MainMenuActivity)
                        }
                    }
                } catch (e: Exception) {
                    Log.d(Constants.tag, "Exception : $e")
                }
            }
        }

    fun addFirebaseToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                Functions.getSharedPreference(this@MainMenuActivity).edit()
                    .putString(Variables.DEVICE_TOKEN, token).commit()


                if (Functions.getSharedPreference(this).getBoolean(Variables.IS_LOGIN, false)) {
                    ApiRepository.addDeviceData(this@MainMenuActivity)
                }

            })
    }


    override fun onBackPressed() {
        val count = this.supportFragmentManager.backStackEntryCount
        if (count == 0) {
            if (binding.viewpager.currentItem != 0) {
                tabLayout!!.getTabAt(0)!!.select()
                return
            }
            mBackPressed = if (mBackPressed + 2000 > System.currentTimeMillis()) {
                super.onBackPressed()
                return
            } else {
                Functions.showToast(baseContext, getString(R.string.tap_to_exist))
                System.currentTimeMillis()
            }
        } else {
            val frag = supportFragmentManager.fragments[supportFragmentManager.fragments.size - 1]
            if (frag != null) {
                val childCount = frag.childFragmentManager.backStackEntryCount
                if (childCount == 0) {

                    super.onBackPressed()
                } else {
                    frag.childFragmentManager.popBackStack()
                }
            } else {

                super.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        mPermissionResult.unregister()
        unRegisterReceiver()
        removeListener()
        super.onDestroy()
    }


    companion object {
        @JvmField
        var mainMenuActivity: MainMenuActivity? = null

        @JvmField
        var tabLayout: TabLayout? = null
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

    var mReceiver: NotificationBroadCast? = null
    fun registerReceiver(){
        mReceiver = NotificationBroadCast()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mReceiver, IntentFilter("NotificationHit"), Context.RECEIVER_NOT_EXPORTED)
        }
        else{
           registerReceiver(mReceiver, IntentFilter("NotificationHit"))
        }

    }

    fun unRegisterReceiver(){
        if (mReceiver != null) {
            unregisterReceiver(mReceiver)
            mReceiver = null
        }
    }

    inner class NotificationBroadCast : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateNotificationCount()

        }
    }


    fun updateNotificationCount(){
        var count= Functions.getSharedPreference(binding?.root?.context).getInt(Variables.notificationCount,0)
        count++
        Functions.getSharedPreference(binding?.root?.context).edit().putInt(Variables.notificationCount,count).commit()
    }



    @JvmField
    var roomManager: RoomManager? = null
    @JvmField
    var roomFirebaseManager:RoomFirebaseManager? = null
    var model:MainStreamingModel? = null
    var myUserModel: HomeUserModel? = null
    var reference: DatabaseReference? = null
    fun setRoomListerner() {
        reference = FirebaseDatabase.getInstance().reference
        roomManager =RoomManager.getInstance(this)
        roomFirebaseManager = RoomFirebaseManager.getInstance(this)
        roomFirebaseManager!!.updateListerner1(object : RoomFirebaseListener {
            override fun createRoom(bundle: Bundle?) {}
            override fun JoinedRoom(bundle: Bundle?) {
                if (bundle != null) {
                    val roomId = bundle.getString("roomId")
                    Functions.printLog(Constants.tag, "JoinedRoom roomId$roomId")
                    if (!TextUtils.isEmpty(bundle.getString("roomId"))) {
                        roomManager!!.showRoomDetailAfterJoin(roomId)
                    }
                }
            }

            override fun onRoomLeave(bundle: Bundle?) {
                stopRoomService()
                Dialogs.closeInvitationCookieBar(this@MainMenuActivity)
                roomFirebaseManager!!.removeAllListener()
                binding.sheetBottomBar.visibility = View.GONE
            }

            override fun onRoomDelete(bundle: Bundle?) {
                try {

                    stopRoomService()
                    roomFirebaseManager!!.removeAllListener()
                    binding.sheetBottomBar.visibility = View.GONE

                }catch (e:Exception){}
            }

            override fun onRoomUpdate(bundle: Bundle?) {
                model = roomFirebaseManager!!.mainStreamingModel
                myUserModel = roomFirebaseManager!!.myUserModel
            }

            override fun onRoomUsersUpdate(bundle: Bundle?) {
                model = roomFirebaseManager!!.mainStreamingModel
                myUserModel = roomFirebaseManager!!.myUserModel
                if (roomFirebaseManager!!.speakersUserList.size > 0) {
                    val userModel = roomFirebaseManager!!.speakersUserList[0]
                    binding.ivJoinProfileOne.controller = userModel.userModel?.username?.let {
                        Functions.frescoImageLoad(
                            this@MainMenuActivity,
                            it,
                            userModel.userModel?.getProfilePic(),
                            binding.ivJoinProfileOne
                        )
                    }
                }
                if (roomFirebaseManager!!.speakersUserList.size > 1) {
                    binding.ivJoinProfileTwo.visibility = View.VISIBLE
                    val userModel = roomFirebaseManager!!.speakersUserList[1]
                    binding.ivJoinProfileTwo.controller = userModel.userModel?.username?.let {
                        Functions.frescoImageLoad(
                            this@MainMenuActivity,
                            it,
                            userModel.userModel?.getProfilePic(),
                            binding.ivJoinProfileTwo
                        )
                    }
                }
                else if (roomFirebaseManager!!.audienceUserList.size > 0) {
                    binding.ivJoinProfileTwo.visibility = View.VISIBLE
                    val userModel = roomFirebaseManager!!.audienceUserList[0]
                    binding.ivJoinProfileTwo.controller = userModel.userModel?.username?.let {
                        Functions.frescoImageLoad(
                            this@MainMenuActivity,
                            it,
                            userModel.userModel?.getProfilePic(),
                            binding.ivJoinProfileTwo
                        )
                    }
                } else {
                    binding.ivJoinProfileTwo.visibility = View.GONE
                }
                val totalCount =
                    roomFirebaseManager!!.speakersUserList.size + roomFirebaseManager!!.audienceUserList.size
                if (totalCount > 2) {
                    binding.tabJoinCount.visibility = View.VISIBLE
                    binding.tvJoinCount.text = "+" + (totalCount - 2)
                } else {
                    binding.tabJoinCount.visibility = View.GONE
                }
            }

            override fun onMyUserUpdate(bundle: Bundle?) {
                model = roomFirebaseManager!!.mainStreamingModel
                myUserModel = roomFirebaseManager!!.myUserModel
                if (myUserModel!!.userRoleType == null) {
                    myUserModel!!.userRoleType = "0"
                }
                if (myUserModel!!.userRoleType == "1" || myUserModel!!.userRoleType == "2") {
                    if (myUserModel!!.mice == "1") {
                        binding.ivMice.setImageDrawable(
                            ContextCompat.getDrawable(
                                binding.root.context,
                                R.drawable.ic_mice
                            )
                        )
                        if (RoomStreamService.streamingInstance != null && RoomStreamService.streamingInstance?.ismAudioMuted()==true) RoomStreamService.streamingInstance?.enableVoiceCall()
                    }
                    else {
                        binding.ivMice.setImageDrawable(ContextCompat.getDrawable(
                            binding.root.context,
                            R.drawable.ic_mice_mute
                        )
                        )
                        if (RoomStreamService.streamingInstance != null && RoomStreamService.streamingInstance?.ismAudioMuted()==false) RoomStreamService.streamingInstance?.muteVoiceCall()
                    }
                    binding.tabMice.visibility = View.VISIBLE
                    binding.tabRaiseHand.visibility = View.GONE
                    binding.tabRiseHandUser.visibility = View.VISIBLE
                } else {
                    if (myUserModel!!.riseHand == "1") {
                        binding.ivRaiseHand.setImageDrawable(
                            ContextCompat.getDrawable(
                                binding.root.context, R.drawable.ic_hand
                            )
                        )
                    } else {
                        binding.ivRaiseHand.setImageDrawable(
                            ContextCompat.getDrawable(
                                binding.root.context, R.drawable.ic_hand_black
                            )
                        )
                    }
                    if (RoomStreamService.streamingInstance != null && RoomStreamService.streamingInstance?.ismAudioMuted()==false)
                        RoomStreamService.streamingInstance?.muteVoiceCall()
                    binding.tabMice.visibility = View.GONE
                    binding.tabRiseHandUser.visibility = View.GONE
                }
                if (myUserModel!!.userRoleType == "1") {
                    binding.tabRiseHandUser.visibility = View.VISIBLE
                }
            }

            override fun onSpeakInvitationReceived(bundle: Bundle?) {
                if (bundle != null) {
                    val invitation = bundle.getSerializable("data") as com.coheser.app.models.InviteForSpeakModel?
                    if (invitation!!.getInvite() == "1") {
                        Dialogs.showInvitationDialog(
                            this@MainMenuActivity,
                            invitation.getUserName()
                        ) { bundle ->
                            if (bundle != null) {
                                roomFirebaseManager!!.removeInvitation()
                                val updateRise = HashMap<String, Any>()
                                updateRise["riseHand"] = "0"
                                reference!!.child(Variables.roomKey)
                                    .child(model?.model?.id!!).child(Variables.roomUsers)
                                    .child(
                                        Functions.getSharedPreference(this@MainMenuActivity)
                                            .getString(Variables.U_ID, "")!!
                                    )
                                    .updateChildren(updateRise)
                                if (bundle.getBoolean("isShow")) {
                                    if (RoomStreamService.streamingInstance != null && RoomStreamService?.streamingInstance?.ismAudioMuted() == true) {
                                        RoomStreamService.streamingInstance?.enableVoiceCall()
                                    }
                                    roomManager!!.speakerJoinRoomHitApi(
                                        Functions.getSharedPreference(
                                            this@MainMenuActivity
                                        ).getString(Variables.U_ID, ""), model?.model?.id, "2"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            override fun onWaveUserUpdate(bundle: Bundle?) {}
        })
        roomManager!!.addResponseListener(object :
            RoomApisListener {
            override fun roomInvitationsSended(bundle: Bundle?) {
                Functions.printLog(Constants.tag, "roomInvitationsSended")
                if (bundle!!.getString("action") == "roomInvitationSended") {
                    Dialogs.showSuccess(
                        this@MainMenuActivity,
                        binding.root.context.getString(R.string.room_invitation_send_successfully)
                    )
                    roomManager!!.selectedInviteFriends = null
                }
            }

            override fun goAheadForRoomGenrate(bundle: Bundle?) {
                if (bundle!!.getString("action") == "goAheadForRoomGenrate") {
                    if (roomManager!!.roomName != null && roomManager!!.privacyType != null) {
                        Log.d(Constants.tag, "roomName: " + roomManager!!.roomName)
                        roomManager!!.createRoomBYUserId()
                    } else {
                        Dialogs.showError(
                            this@MainMenuActivity,
                            binding.root.context.getString(R.string.something_went_wrong)
                        )
                    }
                }
            }

            override fun onRoomJoined(bundle: Bundle?) {
                Functions.printLog(Constants.tag, "onRoomJoined")
                val myUserModel = bundle!!.getParcelable("model") as HomeUserModel?
                val roomID = bundle!!.getString("roomId")
                roomFirebaseManager!!.joinRoom(roomID!!, myUserModel!!)
            }

            override fun onRoomReJoin(bundle: Bundle?) {
                Functions.printLog(Constants.tag, "onRoomReJoin")
                val myUserModel = bundle!!.getParcelable("model") as HomeUserModel?
                val roomID = bundle!!.getString("roomId")
                roomFirebaseManager!!.joinRoom(roomID!!, myUserModel!!)
                if (!TextUtils.isEmpty(bundle!!.getString("roomId"))) {
                    roomManager!!.showRoomDetailAfterJoin(roomID)
                }
            }

            override fun onRoomMemberUpdate(bundle: Bundle?) {
                if (bundle != null) {
                    val homeUserModel = bundle!!.getParcelable("model") as HomeUserModel?
                    roomFirebaseManager!!.updateMemberModel(homeUserModel!!)
                }
            }

            override fun doRoomLeave(bundle: Bundle?) {
                Functions.printLog(Constants.tag, "doRoomLeave")
                if (bundle!!.getString("action") == "leaveRoom") {
                    val roomId = bundle!!.getString("roomId")
                    roomManager!!.leaveRoom(roomId)
                }
            }

            override fun doRoomDelete(bundle: Bundle?) {
                Functions.printLog(Constants.tag, "doRoomDelete")
                if (bundle!!.getString("action") == "deleteRoom") {
                    val roomId = bundle!!.getString("roomId")
                    roomManager!!.deleteRoom(roomId)
                }
            }

            override fun onRoomLeave(bundle: Bundle?) {
                roomFirebaseManager!!.removeUserLeaveNode(model!!.model?.id)
            }

            override fun onRoomDelete(bundle: Bundle?) {
                try {
                    roomFirebaseManager!!.removeRoomNode(model!!.model?.id)
                }catch (e:Exception){}
            }

            override fun goAheadForRoomJoin(bundle: Bundle?) {
                Functions.printLog(Constants.tag, "goAheadForRoomJoin")
                if (bundle!!.getString("action") == "goAheadForJoinRoom") {
                    val roomId = bundle!!.getString("roomId")
                    Log.d(Constants.tag, "roomId: $roomId")
                    roomManager!!.joinRoom(
                        Functions.getSharedPreference(this@MainMenuActivity).getString(
                            Variables.U_ID, ""),
                        roomId,
                        "0"
                    )
                }
            }

            override fun roomCreated(bundle: Bundle?) {
                Functions.printLog(Constants.tag, "roomCreated")
                if (bundle!!.getString("action") == "roomCreated") {
                    val model = bundle!!.getParcelable("model") as com.coheser.app.activitesfragments.spaces.utils.RoomManager.MainStreamingModel?
                    if (roomManager!!.selectedInviteFriends != null && roomManager!!.selectedInviteFriends!!.size > 0) {
                        roomManager!!.inviteMembersIntoRoom(
                            Functions.getSharedPreference(this@MainMenuActivity).getString(
                                Variables.U_ID, ""),
                            roomManager?.selectedInviteFriends!!
                        )
                    }
                    roomManager!!.roomName = null
                    roomManager!!.privacyType = null
                    roomManager!!.selectedInviteFriends = null
                    roomFirebaseManager!!.createRoomNode(model)
                }
            }

            @SuppressLint("SuspiciousIndentation")
            override fun showRoomDetailAfterJoin(bundle: Bundle?) {
                Functions.printLog(Constants.tag, "showRoomDetailAfterJoin()")
                if (bundle != null) {
                    model = bundle["model"] as MainStreamingModel?
                    roomFirebaseManager!!.mainStreamingModel = model
                    roomFirebaseManager!!.addAllRoomListerner()
                    startRoomService()
                    binding.sheetBottomBar.visibility = View.VISIBLE
                    Functions.printLog(Constants.tag, "showRoomDetailAfterJoin()")
                    if (tabLayout!!.selectedTabPosition == 3) openRoomScreen()
                }
            }
        })
        binding.tabRaiseHand.setOnClickListener { openRiseHandToSpeak() }
        binding.tabMice.setOnClickListener { updateMyMiceStatus() }
        binding.tabRiseHandUser.setOnClickListener { openRiseHandList() }
        binding.tabQueitly.setOnClickListener { removeRoom() }
        binding.sheetBottomBar.setOnClickListener { openRoomScreen() }
    }

    fun removeListener() {
        if (roomFirebaseManager != null) {
            roomFirebaseManager!!.removeMainListener()
        }
    }

    fun startRoomService() {
        val mService = RoomStreamService()
        if (!Functions.isMyServiceRunning(this@MainMenuActivity, mService.javaClass)) {
            val intent = Intent(applicationContext, mService.javaClass)
            var userModel: HomeUserModel? = null
            for (homeUserModel in roomFirebaseManager?.mainStreamingModel?.userList!!) {
                if (homeUserModel.userModel?.id == Functions.getSharedPreference(this)
                        .getString(Variables.U_ID, "")
                ) {
                    userModel = homeUserModel
                }
            }
            if (userModel != null) {
                intent.putExtra(
                    "title",
                    "" + userModel.userModel?.first_name + " " + userModel.userModel?.last_name
                )
            } else {
                intent.putExtra("title", "")
            }
            intent.putExtra(
                "message",
                getString(R.string.connected_with_space) + " " + roomFirebaseManager!!.mainStreamingModel?.model?.title
            )
            intent.putExtra("roomId", roomFirebaseManager!!.mainStreamingModel?.model?.id)
            intent.putExtra(
                "userId",
                Functions.getSharedPreference(this).getString(Variables.U_ID, "")
            )
            intent.action = "start"
            ContextCompat.startForegroundService(applicationContext, intent)
        }
    }

    fun stopRoomService() {
        val mService = RoomStreamService()
        if (Functions.isMyServiceRunning(applicationContext, mService.javaClass)) {
            val intent = Intent(applicationContext, mService.javaClass)
            intent.action = "stop"
            ContextCompat.startForegroundService(applicationContext, intent)
        }
    }

    private fun openRoomScreen() {
        if (model != null) {
            val f = RoomDetailBottomSheet.newInstance(model) { }
            val transaction = supportFragmentManager.beginTransaction()
            val bundle = Bundle()
            f.arguments = bundle
            transaction.setCustomAnimations(
                R.anim.in_from_bottom,
                R.anim.out_to_top,
                R.anim.in_from_top,
                R.anim.out_from_bottom
            )
            transaction.addToBackStack("RoomDetailBottomSheet")
            transaction.replace(R.id.mainMenuFragment, f, "RoomDetailBottomSheet").commit()
        }
    }

    private fun removeRoom() {
        val bundle = roomManager!!.checkRoomCanDeleteOrLeave(roomFirebaseManager!!.speakersUserList)
        if (bundle.getString("action") == "removeRoom") {
            roomManager!!.deleteRoom(model!!.model?.id)
        } else if (bundle.getString("action") == "leaveRoom") {
            roomManager!!.leaveRoom(model!!.model?.id)
        } else {
            val speakerAsModeratorModel = bundle.getParcelable("model") as HomeUserModel?
            makeRoomModeratorAndLeave(speakerAsModeratorModel)
        }
    }

    private fun makeRoomModeratorAndLeave(itemUpdate: HomeUserModel?) {
        if (model!!.model != null) {
            reference!!.child(Variables.roomKey).child(model?.model?.id!!).child(
                Variables.roomUsers)
                .child(itemUpdate!!.userModel?.id!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val dataItem = snapshot.getValue(HomeUserModel::class.java)
                            dataItem!!.userRoleType = "1"
                            reference!!.child(Variables.roomKey).child(model?.model?.id!!)
                                .child(Variables.roomUsers)
                                .child(itemUpdate.userModel?.id!!)
                                .setValue(dataItem).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        roomManager!!.leaveRoom(model!!.model?.id)
                                    }
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun openRiseHandToSpeak() {
        val riseHandForSpeakF = RiseHandForSpeakF { bundle ->
                if (bundle.getBoolean("isShow")) {
                    if (bundle.getString("action") == "riseHandForSpeak") {
                        val riseHandMap = HashMap<String, Any>()
                        riseHandMap["riseHand"] = "1"
                        reference!!.child(Variables.roomKey).child(model?.model?.id!!)
                            .child(Variables.roomUsers).child(
                                Functions.getSharedPreference(this).getString(Variables.U_ID, "")!!
                            )
                            .updateChildren(riseHandMap)
                    } else if (bundle.getString("action") == "neverMind") {
                        val riseHandMap = HashMap<String, Any>()
                        riseHandMap["riseHand"] = "0"
                        reference!!.child(Variables.roomKey).child(model?.model?.id!!)
                            .child(Variables.roomUsers).child(
                                Functions.getSharedPreference(this).getString(Variables.U_ID, "")!!
                            )
                            .updateChildren(riseHandMap)
                    }
                }
            }
        riseHandForSpeakF.show(supportFragmentManager, "RiseHandForSpeakF")
    }

    private fun updateMyMiceStatus() {
        if (RoomStreamService.streamingInstance != null) {
            val updateMice = HashMap<String, Any>()
            if (RoomStreamService.streamingInstance?.ismAudioMuted()==true) {
                updateMice["mice"] = "1"
            } else {
                updateMice["mice"] = "0"
            }
            reference!!.child(Variables.roomKey).child(model?.model?.id!!)
                .child(Variables.roomUsers)
                .child(Functions.getSharedPreference(this).getString(Variables.U_ID, "")!!)
                .updateChildren(updateMice).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                    }
                }
        }
    }

    private fun openRiseHandList() {
        val fragment = RiseHandUsersF(
                model!!.model?.id,
                roomFirebaseManager!!.mainStreamingModel?.model?.riseHandRule
            ) { bundle ->
                if (bundle.getBoolean("isShow")) {
                    if (bundle.getString("action") == "invite") {
                        val itemUpdate =
                            bundle.getParcelable("itemModel") as com.coheser.app.activitesfragments.spaces.models.HomeUserModel?
                        sendInvitationForSpeak(itemUpdate?.userModel!!)
                    }
                }
            }
        fragment.show(supportFragmentManager, "RiseHandUsersF")
    }

    private fun sendInvitationForSpeak(userModel: UserModel) {
        if (model != null) {
            val invitation =InviteForSpeakModel()
            invitation.setInvite("1")
            invitation.setUserId(
                Functions.getSharedPreference(this).getString(Variables.U_ID, "")
            )
            invitation.setUserName(
                Functions.getSharedPreference(this).getString(Variables.U_NAME, "")
            )
            reference!!.child(Variables.roomKey).child(model?.model?.id!!)
                .child(Variables.roomInvitation)
                .child(userModel.id!!)
                .setValue(invitation).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Dialogs.showSuccess(
                            this@MainMenuActivity,
                            binding.root.context.getString(R.string.great_we_are_sent_them_an_invite)
                        )
                    }
                }
        }
    }


}