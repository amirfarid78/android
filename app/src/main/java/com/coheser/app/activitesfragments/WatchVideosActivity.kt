package com.coheser.app.activitesfragments

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.creatorplaylist.CreatePlaylistStep2Fragment
import com.coheser.app.activitesfragments.profile.creatorplaylist.ShowPlaylistFragment
import com.coheser.app.adapters.ViewPagerStatAdapter
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.ActivityWatchVideosBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.CreatePlaylistModel
import com.coheser.app.models.HomeModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.DataHolder
import com.coheser.app.simpleclasses.DataParsing
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.Dialogs
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import io.paperdb.Paper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject


class WatchVideosActivity : AppCompatLocaleActivity(), FragmentCallBack {
    lateinit var myContext: Context
    lateinit var binding: ActivityWatchVideosBinding
    var dataList = ArrayList<HomeModel>()
    var playlistName: String? = ""
    var pageCount = 0
    var isApiRuning = false
    var handler: Handler? = null
    var whereFrom: String? = ""
    var currentPositon = 0
    var userId: String? = ""
    var playlistMapList = HashMap<String?, HomeModel>()
    var fragmentConainerId = 0

   // var tvComment: TextView? = null
    override fun onResponce(bundle: Bundle) {
        if (bundle.getString("action").equals("deleteVideo", ignoreCase = true)) {
            dataList.removeAt(bundle.getInt("position"))
            Log.d(Constants.tag, "notify data : " + dataList.size)
            if (dataList.size == 0) {
                onBackPressed()
            }
        } else if (bundle.getString("action").equals("pinned", ignoreCase = true)) {
            Paper.book("pinnedRefresh").write("refresh", true)
            val itemUpdate = dataList[bundle.getInt("position")]
            itemUpdate.pin = bundle.getString("pin", "0")
            dataList[bundle.getInt("position")] = itemUpdate
            pagerSatetAdapter!!.refreshStateSet(false)
            pagerSatetAdapter!!.notifyDataSetChanged()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (e: Exception) {
        }
        Functions.setLocale(
            Functions.getSharedPreference(this)
                .getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this,
            javaClass,
            false
        )
        binding = ActivityWatchVideosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fragmentConainerId = R.id.watchVideo_F
        myContext = this@WatchVideosActivity
        binding.tabPlaylist.setOnClickListener(DebounceClickHandler { openPlaylist() })
        binding.ivEditPlaylist.setOnClickListener(DebounceClickHandler { showPlaylistSetting() })
        whereFrom = intent.getStringExtra("whereFrom")

        userId = intent.getStringExtra("userId")
        pageCount = intent.getIntExtra("pageCount", 0)
        currentPositon = intent.getIntExtra("position", 0)

        if (whereFrom.equals(Variables.IdVideo, ignoreCase = true)) {
            dataList.clear()
            callApiForSinglevideos(intent.getStringExtra("video_id"), true, whereFrom)
            binding.tabPlaylist.visibility = View.GONE
            binding.tabSneekbarView.visibility = View.GONE
            binding.ivEditPlaylist.visibility = View.GONE
        }

        else if (whereFrom.equals(Variables.playlistVideo, ignoreCase = true)) {
            dataList.clear()
            playlistName = intent.getStringExtra("playlistName")
            callApiForPlaylistVideos(intent.getStringExtra("playlist_id"), true)
            binding.tabPlaylist.visibility = View.VISIBLE
            binding.tabSneekbarView.visibility = View.VISIBLE
            binding.tvPlaylistTitle.text = getString(R.string.playlist) + " . " + playlistName
        }

        else {
            val bundle = DataHolder.instance?.data
            if (bundle != null) {
                val arrayList = bundle.getSerializable("arraylist") as ArrayList<HomeModel>?
                dataList.clear()
                dataList.addAll(arrayList!!)
            }

            if(dataList.isNotEmpty()) {
                if (dataList.size < currentPositon) {
                    currentPositon = 0
                }
                if (dataList[currentPositon].playlistId == "0") {
                    binding.tabPlaylist.visibility = View.GONE
                    binding.tabSneekbarView.visibility = View.GONE
                } else {
                    binding.tabPlaylist.visibility = View.VISIBLE
                    binding.tabSneekbarView.visibility = View.VISIBLE
                    binding.tvPlaylistTitle.text =
                        myContext.getString(R.string.playlist) + " . " + dataList[currentPositon].playlistName
                }

                if (dataList[currentPositon].video_user_id == Functions.getSharedPreference(
                        myContext
                    )
                        .getString(Variables.U_ID, "")
                ) {
                    if (dataList[currentPositon].playlistId == "0") {
                        binding.ivEditPlaylist.visibility = View.GONE
                    } else {
                        binding.ivEditPlaylist.visibility = View.VISIBLE
                    }
                } else {
                    binding.ivEditPlaylist.visibility = View.GONE
                }
            }
            else{
                currentPositon = 0
                pageCount = 0
                callVideoApi()
            }
        }

        handler = Handler(Looper.getMainLooper())
        binding.goBack.setOnClickListener(DebounceClickHandler {onBackPressed() })
        binding.swiperefresh.setProgressViewOffset(false, 0, 200)
        binding.swiperefresh.setColorSchemeResources(R.color.black)
        binding.swiperefresh.setOnRefreshListener {
            currentPositon = 0
            pageCount = 0
            dataList.clear()
            callVideoApi()
        }

        setTabs(false)
        setUpPreviousScreenData()

        binding.tvComment.setOnClickListener(View.OnClickListener { openSheetForComment() })
    }

    var taggedUserList: ArrayList<UserModel> = ArrayList()
    private fun openSheetForComment() {
        taggedUserList!!.clear()
        val fragment = EditTextSheetFragment("OwnComment", taggedUserList, FragmentCallBack { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                if (bundle.getString("action") == "sendComment") {
                    taggedUserList = bundle.getSerializable("taggedUserList") as ArrayList<UserModel>
                    val message = bundle.getString("message")
                    binding.tvComment!!.text = message
                    findViewById<View>(R.id.send_progress).visibility = View.VISIBLE
                    val fragment =
                        pagerSatetAdapter!!.getItem( binding.viewpager.currentItem) as VideosPlayFragment
                    message?.let {
                        fragment.sendComment(it, taggedUserList) { bundle ->
                            findViewById<View>(R.id.send_progress).visibility = View.GONE
                            if (bundle.getString("type").equals("sended", ignoreCase = true)) {
                                binding.tvComment!!.text = this@WatchVideosActivity.getString(R.string.add_a_comment)
                            } else if (bundle.getString("type").equals("failed", ignoreCase = true)) {
                                binding.tvComment!!.text = this@WatchVideosActivity.getString(R.string.add_a_comment)
                            }
                        }
                    }
                }
            }
        })
        val bundle = Bundle()
        bundle.putString("replyStr", "")
        fragment.arguments = bundle
        fragment.show(supportFragmentManager, "EditTextSheetF")
    }


    private fun setUpPreviousScreenData() {
        for (item in dataList) {
            pagerSatetAdapter.addFragment(
                VideosPlayFragment(
                    false,
                    item,
                    binding.viewpager,
                    this,
                    fragmentConainerId
                )
            )
        }
        pagerSatetAdapter.refreshStateSet(false)
        pagerSatetAdapter.notifyDataSetChanged()
        binding.viewpager.setCurrentItem(currentPositon, true)
    }

    // set the fragments for all the videos list
    lateinit var pagerSatetAdapter: ViewPagerStatAdapter
    fun setTabs(isListSet: Boolean) {
        if (isListSet) {
            dataList.clear()
        }
        Log.d(Constants.tag, "settabs")
        pagerSatetAdapter = ViewPagerStatAdapter(supportFragmentManager,  binding.viewpager, false, this)

        binding.viewpager.setAdapter(pagerSatetAdapter)
        binding.viewpager.setOffscreenPageLimit(1)
        binding.viewpager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                Log.d(Constants.tag, "on page selected")
                if (position == 0) {
                    if (pagerSatetAdapter != null && pagerSatetAdapter!!.count > 0) {
                        Log.d(Constants.tag, "on page selected call videolistf")
                        val fragment =
                            pagerSatetAdapter!!.getItem( binding.viewpager.getCurrentItem()) as VideosPlayFragment
                        fragment.setData()
                        CoroutineScope(Dispatchers.Main).launch{
                            fragment.setPlayer(true)
                        }
                    }
                    binding.swiperefresh.isEnabled = true
                } else {
                    val fragment = pagerSatetAdapter!!.getItem(binding.viewpager.currentItem - 1) as VideosPlayFragment
                    fragment.updateVideoView()
                    binding.swiperefresh.isEnabled = false
                }
                currentPositon =  binding.viewpager.getCurrentItem()
                Log.d(Constants.tag, "$currentPositon on page selected")
                setupPlaylist()

                if (dataList.size > 2 && dataList.size - 1 == position) {
                    if (!isApiRuning) {
                        pageCount++
                        callVideoApi()
                        Log.d(Constants.tag, "$currentPositon on page selected call api")
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun setupPlaylist() {
        Log.d(Constants.tag, "in setup playlist")
        if (dataList.size > 0) {
            if (dataList[currentPositon].playlistId == "0") {
                binding.tabPlaylist.visibility = View.GONE
                binding.tabSneekbarView.visibility = View.GONE
            } else {
                binding.tabSneekbarView.visibility = View.VISIBLE
                binding.tabPlaylist.visibility = View.VISIBLE
                binding.tvPlaylistTitle.text =
                    myContext!!.getString(R.string.playlist) + " . " + dataList[currentPositon].playlistName
            }
            if (dataList[currentPositon].video_user_id == Functions.getSharedPreference(myContext)
                    .getString(Variables.U_ID, "")
            ) {
                if (dataList[currentPositon].playlistId == "0") {
                    binding.ivEditPlaylist.visibility = View.GONE
                } else {
                    binding.ivEditPlaylist.visibility = View.VISIBLE
                }
            } else {
                binding.ivEditPlaylist.visibility = View.GONE
            }
        }
    }

    private fun callApiForSinglevideos(videoId: String?, isFirstTime: Boolean, fro: String?) {
        Log.d(Constants.tag, fro!!)
        try {
            val parameters = JSONObject()
            parameters.put("user_id", userId)
            parameters.put("video_id", videoId)
            VolleyRequest.JsonPostRequest(
                this,
                ApiLinks.showVideoDetail,
                parameters,
                Functions.getHeaders(this)
            ) { resp ->
                binding.swiperefresh.isRefreshing = false
                singalVideoParseData(resp, isFirstTime)
            }
        } catch (e: Exception) {
            Functions.printLog(Constants.tag, e.toString())
        }
    }

    fun singalVideoParseData(responce: String?, isFirstTime: Boolean) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msg = jsonObject.optJSONObject("msg")
                val temp_list = ArrayList<HomeModel>()
                val video = msg.optJSONObject("Video")
                val user = msg.optJSONObject("User")
                val sound = msg.optJSONObject("Sound")
                val location = msg.optJSONObject("Location")
                val videoProduct = msg.optJSONArray("VideoProduct")
                val userPrivacy = user.optJSONObject("PrivacySetting")
                val pushNotification = user.optJSONObject("PushNotification")
                run {
                    val item = DataParsing.parseVideoDetailData(
                        HomeModel(),
                        user,
                        sound,
                        video,
                        location,
                        videoProduct,
                        userPrivacy,
                        pushNotification
                    )
                    if (item.user_id != null && item.user_id != "null" && item.user_id != "0") {
                        temp_list.add(item)
                    }
                    if (dataList.isEmpty()) {
                        setTabs(true)
                    }
                    dataList.addAll(temp_list)
                }
                for (item in temp_list) {
                    pagerSatetAdapter!!.addFragment(
                        VideosPlayFragment(
                            false,
                            item,
                            binding.viewpager,
                            this,
                            fragmentConainerId
                        )
                    )
                }
                pagerSatetAdapter!!.refreshStateSet(false)
                pagerSatetAdapter!!.notifyDataSetChanged()
                if (isFirstTime) {
                    if (dataList.size > 0) {
                        if (dataList[currentPositon].playlistId == "0") {
                            binding.tabPlaylist.visibility = View.GONE
                            binding.tabSneekbarView.visibility = View.GONE
                        } else {
                            binding.tabSneekbarView.visibility = View.VISIBLE
                            binding.tabPlaylist.visibility = View.VISIBLE
                            binding.tvPlaylistTitle.text =
                                myContext!!.getString(R.string.playlist) + " . " + dataList[ binding.viewpager.currentItem].playlistName
                        }
                        if (dataList[currentPositon].video_user_id == Functions.getSharedPreference(
                                myContext
                            ).getString(Variables.U_ID, "")
                        ) {
                            if (dataList[currentPositon].playlistId == "0") {
                                binding.ivEditPlaylist.visibility = View.GONE
                            } else {
                                binding.ivEditPlaylist.visibility = View.VISIBLE
                            }
                        } else {
                            binding.ivEditPlaylist.visibility = View.GONE
                        }
                    }
                } else {
                    pagerSatetAdapter!!.notifyDataSetChanged()
                }
                setupPlaylist()
                if (dataList.size > 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        if (intent.hasExtra("video_comment")) {
                            if (intent.getBooleanExtra("video_comment", false)) {
                                val fragment = pagerSatetAdapter!!.getItem(
                                    binding.viewpager.currentItem
                                ) as VideosPlayFragment
                                if (Functions.checkLoginUser(this@WatchVideosActivity)) {
                                    fragment.openComment(fragment.item)
                                }
                            }
                        }
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (pageCount > 0) pageCount--
        } finally {
            isApiRuning = false
        }
    }

    private fun callApiForSinglevideos1(videoId: String?, model: HomeModel?) {
        try {
            val parameters = JSONObject()
            parameters.put("user_id", userId)
            parameters.put("video_id", videoId)
            VolleyRequest.JsonPostRequest(
                this,
                ApiLinks.showVideoDetail,
                parameters,
                Functions.getHeaders(this)
            ) { resp ->
                binding.swiperefresh.isRefreshing = false
                singalVideoParseData1(resp, model, true)
            }
        }
        catch (e: Exception) {
            Functions.printLog(Constants.tag, e.toString())
        }
    }

    fun singalVideoParseData1(responce: String?, model: HomeModel?, isFirstTime: Boolean) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msg = jsonObject.optJSONObject("msg")
                val temp_list = ArrayList<HomeModel>()
                val video = msg.optJSONObject("Video")
                val user = msg.optJSONObject("User")
                val sound = msg.optJSONObject("Sound")
                val location = msg.optJSONObject("Location")
                val videoProduct = msg.optJSONArray("VideoProduct")
                val userPrivacy = user.optJSONObject("PrivacySetting")
                val pushNotification = user.optJSONObject("PushNotification")
                run {
                    val item = DataParsing.parseVideoDetailData(HomeModel(),
                        user,
                        sound,
                        video,
                        location,
                        videoProduct,
                        userPrivacy,
                        pushNotification)
                    if (item.user_id != null && item.user_id != "null" && item.user_id != "0") {
                        temp_list.add(item)
                    }
                    Log.d(
                        Constants.tag,
                        item.tagProductList!!.size.toString() + " tagproductlistsize"
                    )
                    if (dataList.isEmpty()) {
                        setTabs(true)
                    }
                    dataList.addAll(temp_list)
                }
                for (item in temp_list) {
                    pagerSatetAdapter!!.addFragment(
                        VideosPlayFragment(
                            false,
                            item,
                            binding.viewpager,
                            this,
                            fragmentConainerId
                        )
                    )
                }
                pagerSatetAdapter!!.refreshStateSet(false)
                pagerSatetAdapter!!.notifyDataSetChanged()
                if (isFirstTime) {
                    if (dataList.size > 0) {
                        if (dataList[currentPositon].playlistId == "0") {
                            binding.tabPlaylist.visibility = View.GONE
                            binding.tabSneekbarView.visibility = View.GONE
                        } else {
                            binding.tabSneekbarView.visibility = View.VISIBLE
                            binding.tabPlaylist.visibility = View.VISIBLE
                            binding.tvPlaylistTitle.text =
                                myContext!!.getString(R.string.playlist) + " . " + dataList[ binding.viewpager!!.currentItem].playlistName
                        }
                        if (dataList[currentPositon].video_user_id == Functions.getSharedPreference(
                                myContext
                            ).getString(Variables.U_ID, "")
                        ) {
                            if (dataList[currentPositon].playlistId == "0") {
                                binding.ivEditPlaylist.visibility = View.GONE
                            } else {
                                binding.ivEditPlaylist.visibility = View.VISIBLE
                            }
                        } else {
                            binding.ivEditPlaylist.visibility = View.GONE
                        }
                    }
                } else {
                    pagerSatetAdapter!!.notifyDataSetChanged()
                }
                setupPlaylist()
                if (dataList.size > 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(200)
                        if (intent.hasExtra("video_comment")) {
                            if (intent.getBooleanExtra("video_comment", false)) {
                                val fragment = pagerSatetAdapter!!.getItem(
                                    binding.viewpager.currentItem
                                ) as VideosPlayFragment
                                if (Functions.checkLoginUser(this@WatchVideosActivity)) {
                                    fragment.openComment(fragment.item)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (pageCount > 0) pageCount--
        } finally {
            isApiRuning = false
        }
    }

    private fun showPlaylistSetting() {
        val wrapper: Context = ContextThemeWrapper(myContext, R.style.AlertDialogCustom)
        val popup = PopupMenu(wrapper, binding.ivEditPlaylist)
        popup.menuInflater.inflate(R.menu.menu_playlist_setting, popup.menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.gravity = Gravity.TOP or Gravity.RIGHT
        }
        popup.show()
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuEdit -> {
                    editUserPlaylist()
                }

                R.id.menuDelete -> {
                    Dialogs.showDoubleButtonAlert(
                        myContext,
                        myContext!!.getString(R.string.playlist_setting),
                        myContext!!.getString(R.string.are_you_sure_to_delete_this_playlist),
                        myContext!!.getString(R.string.cancel_),
                        myContext!!.getString(R.string.delete),
                        false
                    ) { bundle ->
                        if (bundle.getBoolean("isShow", false)) {
                            deletePlaylist()
                        }
                    }
                }
            }
            true
        }
    }

    private fun editUserPlaylist() {
        val playlistModel = CreatePlaylistModel()
        playlistModel.name = playlistName
        val f =
            CreatePlaylistStep2Fragment(
                false
            ) { bundle ->
                if (!bundle.getBoolean("isShow")) {
                    currentPositon = 0
                    Log.d(Constants.tag, "Update List")
                    pageCount = 0
                    dataList.clear()
                    callVideoApi()
                }
            }
        val bundle = Bundle()
        bundle.putSerializable("model", playlistModel)
        bundle.putSerializable("playlistMapList", playlistMapList)
        bundle.putString("playlist_id", intent.getStringExtra("playlist_id"))
        f.arguments = bundle
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(
            R.anim.in_from_right,
            R.anim.out_to_left,
            R.anim.in_from_left,
            R.anim.out_to_right
        )
        ft.replace(R.id.watchVideo_F, f, "EditPlaylistFromStepTwoF")
            .addToBackStack("EditPlaylistFromStepTwoF").commit()
    }

    private fun deletePlaylist() {
        val parameters = JSONObject()
        try {
            parameters.put("id", intent.getStringExtra("playlist_id"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Functions.showLoader(this@WatchVideosActivity, false, false)
        VolleyRequest.JsonPostRequest(
            this@WatchVideosActivity,
            ApiLinks.deletePlaylist,
            parameters,
            Functions.getHeaders(this@WatchVideosActivity)
        ) { resp ->
            Functions.checkStatus(this@WatchVideosActivity, resp)
            Functions.cancelLoader()
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code == "200") {
                    moveBack()
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception: $e")
            }
        }
    }

    private fun openPlaylist() {
        if (dataList.size <= 0) {
            Toast.makeText(
                myContext,
                myContext!!.getString(R.string.refresh_playlist_to_open_list_detail),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val fragment =
            ShowPlaylistFragment(
                dataList,
                "" + dataList[currentPositon].video_id,
                "" + dataList[currentPositon].playlistId,
                "" + dataList[currentPositon].video_user_id,
                "" + dataList[currentPositon].playlistName
            ) { bundle ->
                if (bundle.getBoolean("isShow", false)) {
                    if (bundle.getString("type").equals("videoPlay", ignoreCase = true)) {
                        currentPositon = bundle.getInt("position", 0)
                        binding.viewpager.setCurrentItem(currentPositon, true)
                    } else if (bundle.getString("type")
                            .equals("deletePlaylist", ignoreCase = true)
                    ) {
                        moveBack()
                    } else if (bundle.getString("type")
                            .equals("deletePlaylistVideo", ignoreCase = true)
                    ) {
                        currentPositon = bundle.getInt("position", 0)
                        pagerSatetAdapter!!.refreshStateSet(true)
                        pagerSatetAdapter!!.removeFragment(currentPositon)
                        pagerSatetAdapter!!.refreshStateSet(false)
                        dataList.removeAt(currentPositon)
                        if (dataList.size == 0) {
                            onBackPressed()
                        } else {
                            currentPositon = currentPositon - 1
                            binding.viewpager.setCurrentItem(currentPositon, true)
                        }
                    }
                }
            }
        fragment.show(supportFragmentManager, "")
    }

    fun callVideoApi() {
        isApiRuning = true
        if (whereFrom.equals(Variables.playlistVideo, ignoreCase = true)) {
            callApiForPlaylistVideos(intent.getStringExtra("playlist_id"), false)
        } else if (whereFrom.equals(Variables.userVideo, ignoreCase = true)) {
            callApiForUserVideos()
        } else if (whereFrom.equals(Variables.likedVideo, ignoreCase = true)) {
            callApiForLikedVideos()
        } else if (whereFrom.equals(Variables.repostVideo, ignoreCase = true)) {
            callApiForRepostVideos()
        } else if (whereFrom.equals(Variables.privateVideo, ignoreCase = true)) {
            callApiForPrivateVideos()
        } else if (whereFrom.equals(Variables.tagedVideo, ignoreCase = true) ||
            whereFrom.equals(Variables.discoverTagedVideo, ignoreCase = true)
        ) {
            callApiForTagedVideos()
        }

        else if (whereFrom.equals(Variables.videoSound, ignoreCase = true)) {
            callApiForSoundVideos()
        }

        else if (whereFrom.equals(Variables.location, ignoreCase = true)) {
            callApishowVideosAgainstLocation()
        }

        else if (whereFrom.equals(Variables.IdVideo, ignoreCase = true)) {
            callApiForSinglevideos(intent.getStringExtra("video_id"), false, whereFrom)
        }

        else if (whereFrom.equals(Variables.tagProduct, ignoreCase = true)) {
            callApiForSinglevideos1(
                intent.getStringExtra("video_id"),
                intent.getParcelableExtra("model")
            )
        }
        else{
            binding.swiperefresh.isRefreshing=false
        }
    }

    private fun callApishowVideosAgainstLocation() {
        var placeID = ""
        if (intent.hasExtra("locationID")){
            placeID = intent.getStringExtra("locationID")!!
        }
        if (dataList == null) dataList = ArrayList()
        val parameters = JSONObject()
        try {
            parameters.put(
                "user_id",
                Functions.getSharedPreference(myContext).getString(Variables.U_ID, "")
            )
            parameters.put("location_id",placeID)
            parameters.put("starting_point", "0")
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        VolleyRequest.JsonPostRequest(
            this@WatchVideosActivity,
            ApiLinks.showVideosAgainstLocation,
            parameters,
            Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this@WatchVideosActivity, resp)
            binding.swiperefresh.isRefreshing = false
            parseData(resp)
        }

    }

    // parse the data of video list
    private fun parseData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgArray = jsonObject.getJSONArray("msg")
                val temp_list = ArrayList<HomeModel>()
                for (i in 0 until msgArray.length()) {
                    val itemdata = msgArray.optJSONObject(i)
                    val video = itemdata.optJSONObject("Video")
                    val sound = itemdata.optJSONObject("Sound")
                    val user = itemdata.optJSONObject("User")
                    val location = itemdata.optJSONObject("Location")
                    val store = itemdata.optJSONObject("Store")
                    val videoProduct = itemdata.optJSONObject("Product")
                    val userPrivacy = user.optJSONObject("PrivacySetting")
                    val pushNotification = user.optJSONObject("PushNotification")
                    val item = DataParsing.parseVideoData(
                        user,
                        sound,
                        video,
                        location,
                        store,
                        videoProduct,
                        userPrivacy,
                        pushNotification
                    )
                    temp_list.add(item)
                }
                if (pageCount == 0) {
                    dataList.clear()
                    dataList.addAll(temp_list)
                } else {
                    dataList.addAll(temp_list)
                }
            }
            pagerSatetAdapter!!.refreshStateSet(false)
            pagerSatetAdapter!!.notifyDataSetChanged()
            if (dataList.isEmpty()) {
                findViewById<View>(R.id.no_data_layout).visibility = View.VISIBLE
            } else {
                findViewById<View>(R.id.no_data_layout).visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
        }
    }

    // api for get the videos list from server
    private fun callApiForSoundVideos() {
        val parameters = JSONObject()
        try {
            parameters.put("sound_id", intent.getStringExtra("soundId"))
            parameters.put("device_id", intent.getStringExtra("deviceId"))
            parameters.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this@WatchVideosActivity,
            ApiLinks.showVideosAgainstSound,
            parameters,
            Functions.getHeaders(this)
        ) { resp ->
            binding.swiperefresh.isRefreshing = false
            parseSoundVideoData(resp)
        }
    }

    fun parseSoundVideoData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgArray = jsonObject.getJSONArray("msg")
                val temp_list = ArrayList<HomeModel>()
                for (i in 0 until msgArray.length()) {
                    val itemdata = msgArray.optJSONObject(i)
                    val video = itemdata.optJSONObject("Video")
                    val user = itemdata.optJSONObject("User")
                    val sound = itemdata.optJSONObject("Sound")
                    val location = itemdata.optJSONObject("Location")
                    val store = itemdata.optJSONObject("Store")
                    val videoProduct = itemdata.optJSONObject("Product")
                    val userPrivacy = user.optJSONObject("PrivacySetting")
                    val userPushNotification = user.optJSONObject("PushNotification")
                    val item = DataParsing.parseVideoData(
                        user,
                        sound,
                        video,
                        location,
                        store,
                        videoProduct,
                        userPrivacy,
                        userPushNotification
                    )
                    if (item.user_id != null && item.user_id != "null" && item.user_id != "0") {
                        temp_list.add(item)
                    }
                }
                if (dataList.isEmpty()) {
                    setTabs(true)
                }
                dataList.addAll(temp_list)
                for (item in temp_list) {
                    pagerSatetAdapter!!.addFragment(
                        VideosPlayFragment(
                            false,
                            item,
                            binding.viewpager,
                            this,
                            fragmentConainerId
                        )
                    )
                }
                pagerSatetAdapter!!.refreshStateSet(false)
                pagerSatetAdapter!!.notifyDataSetChanged()
                setupPlaylist()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (pageCount > 0) pageCount--
        } finally {
            isApiRuning = false
        }
    }

    // api for get the videos list from server
    private fun callApiForTagedVideos() {
        val parameters = JSONObject()
        try {
            parameters.put("user_id", userId)
            parameters.put("hashtag", intent.getStringExtra("hashtag"))
            parameters.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this@WatchVideosActivity,
            ApiLinks.showVideosAgainstHashtag,
            parameters,
            Functions.getHeaders(this)
        ) { resp ->
            binding.swiperefresh.isRefreshing = false
            parseHashtagVideoData(resp)
        }
    }

    fun parseHashtagVideoData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msg = jsonObject.getJSONObject("msg")
                val hashtag = msg.optJSONObject("Hashtag")
                val videos = hashtag.optJSONArray("videos")
                val temp_list = ArrayList<HomeModel>()

                for (i in 0 until videos.length()) {
                    val itemdata = videos.optJSONObject(i)
                    val video = itemdata.optJSONObject("Video")
                    val sound = itemdata.optJSONObject("Sound")
                    val user = itemdata.optJSONObject("User")
                    val location = itemdata.optJSONObject("Location")
                    val store = itemdata.optJSONObject("Store")
                    val videoProduct = itemdata.optJSONObject("Product")
                    val userPrivacy = user.optJSONObject("PrivacySetting")
                    val pushNotification = user.optJSONObject("PushNotification")
                    val item = DataParsing.parseVideoData(
                        user,
                        sound,
                        video,
                        location,
                        store,
                        videoProduct,
                        userPrivacy,
                        pushNotification
                    )
                    temp_list.add(item)
                }

                if (dataList.isEmpty()) {
                    setTabs(true)
                }
                dataList.addAll(temp_list)
                for (item in temp_list) {
                    pagerSatetAdapter!!.addFragment(
                        VideosPlayFragment(
                            false,
                            item,
                            binding.viewpager,
                            this,
                            fragmentConainerId
                        )
                    )
                }
                pagerSatetAdapter!!.refreshStateSet(false)
                pagerSatetAdapter!!.notifyDataSetChanged()
                binding.viewpager.setCurrentItem(currentPositon, true)
                setupPlaylist()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (pageCount > 0) pageCount--
        } finally {
            isApiRuning = false
        }
    }

    fun parsePrivateVideoData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msg = jsonObject.optJSONObject("msg")
                val public_array = msg.optJSONArray("private")
                val temp_list = ArrayList<HomeModel>()
                for (i in 0 until public_array.length()) {
                    val itemdata = public_array.optJSONObject(i)
                    val video = itemdata.optJSONObject("Video")
                    val user = itemdata.optJSONObject("User")
                    val sound = itemdata.optJSONObject("Sound")
                    val location = itemdata.optJSONObject("Location")
                    val store = itemdata.optJSONObject("Store")
                    val videoProduct = itemdata.optJSONObject("Product")
                    val userPrivacy = user.optJSONObject("PrivacySetting")
                    val userPushNotification = user.optJSONObject("PushNotification")
                    val item = DataParsing.parseVideoData(
                        user,
                        sound,
                        video,
                        location,
                        store,
                        videoProduct,
                        userPrivacy,
                        userPushNotification
                    )
                    if (item.user_id != null && item.user_id != "null" && item.user_id != "0") {
                        temp_list.add(item)
                    }
                }
                if (dataList.isEmpty()) {
                    setTabs(true)
                }
                dataList.addAll(temp_list)
                for (item in temp_list) {
                    pagerSatetAdapter!!.addFragment(
                        VideosPlayFragment(
                            false,
                            item,
                            binding.viewpager,
                            this,
                            fragmentConainerId
                        )
                    )
                }
                pagerSatetAdapter!!.refreshStateSet(false)
                pagerSatetAdapter!!.notifyDataSetChanged()
                setupPlaylist()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (pageCount > 0) pageCount--
        } finally {
            isApiRuning = false
        }
    }

    fun parseRepostVideoData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgArray = jsonObject.getJSONArray("msg")
                val temp_list = ArrayList<HomeModel>()
                for (i in 0 until msgArray.length()) {
                    val itemdata = msgArray.optJSONObject(i)
                    val video = itemdata.optJSONObject("Video")
                    val user = itemdata.optJSONObject("User")
                    val sound = itemdata.optJSONObject("Sound")
                    val location = itemdata.optJSONObject("Location")
                    val store = itemdata.optJSONObject("Store")
                    val videoProduct = itemdata.optJSONObject("Product")
                    val userPrivacy = user.optJSONObject("PrivacySetting")
                    val userPushNotification = user.optJSONObject("PushNotification")
                    val item = DataParsing.parseVideoData(
                        user,
                        sound,
                        video,
                        location,
                        store,
                        videoProduct,
                        userPrivacy,
                        userPushNotification
                    )
                    if (item.user_id != null && item.user_id != "null" && item.user_id != "0") {
                        temp_list.add(item)
                    }
                }
                if (dataList.isEmpty()) {
                    setTabs(true)
                }
                dataList.addAll(temp_list)
                for (item in temp_list) {
                    pagerSatetAdapter!!.addFragment(
                        VideosPlayFragment(
                            false,
                            item,
                            binding.viewpager,
                            this,
                            fragmentConainerId
                        )
                    )
                }
                pagerSatetAdapter!!.refreshStateSet(false)
                pagerSatetAdapter!!.notifyDataSetChanged()
                setupPlaylist()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (pageCount > 0) pageCount--
        } finally {
            isApiRuning = false
        }
    }

    fun parseVideoData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgArray = jsonObject.getJSONArray("msg")
                val temp_list = ArrayList<HomeModel>()
                for (i in 0 until msgArray.length()) {
                    val itemdata = msgArray.optJSONObject(i)
                    val video = itemdata.optJSONObject("Video")
                    val user = itemdata.optJSONObject("User")
                    val sound = itemdata.optJSONObject("Sound")
                    val location = itemdata.optJSONObject("Location")
                    val store = itemdata.optJSONObject("Store")
                    val videoProduct = itemdata.optJSONObject("Product")
                    val userPrivacy = user.optJSONObject("PrivacySetting")
                    val userPushNotification = user.optJSONObject("PushNotification")
                    val item = DataParsing.parseVideoData(
                        user,
                        sound,
                        video,
                        location,
                        store,
                        videoProduct,
                        userPrivacy,
                        userPushNotification
                    )
                    if (item.user_id != null && item.user_id != "null" && item.user_id != "0") {
                        temp_list.add(item)
                    }
                }
                if (dataList.isEmpty()) {
                    setTabs(true)
                }
                dataList.addAll(temp_list)
                for (item in temp_list) {
                    pagerSatetAdapter!!.addFragment(
                        VideosPlayFragment(
                            false,
                            item,
                            binding.viewpager,
                            this,
                            fragmentConainerId
                        )
                    )
                }
                pagerSatetAdapter!!.refreshStateSet(false)
                pagerSatetAdapter!!.notifyDataSetChanged()
                setupPlaylist()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (pageCount > 0) pageCount--
        } finally {
            isApiRuning = false
        }
    }

    fun parsePlalistVideoData(responce: String?, isFirstTime: Boolean) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msg = jsonObject.optJSONObject("msg")
                val temp_list = ArrayList<HomeModel>()
                val public_array = msg.optJSONArray("PlaylistVideo")
                for (i in 0 until public_array.length()) {
                    val itemdata = public_array.optJSONObject(i)
                    val video = itemdata.optJSONObject("Video")
                    val store = video.optJSONObject("Store")
                    val videoProduct = video.optJSONObject("Product")
                    val location = video.optJSONObject("Location")
                    val sound = video.optJSONObject("Sound")
                    val user = video.optJSONObject("User")
                    val userPrivacy = user.optJSONObject("PrivacySetting")
                    val userPushNotification = user.optJSONObject("PushNotification")
                    val item = DataParsing.parseVideoData(
                        user,
                        sound,
                        video,
                        location,
                        store,
                        videoProduct,
                        userPrivacy,
                        userPushNotification
                    )
                    item.playlistVideoId = itemdata.optString("id")
                    item.playlistId = msg.getJSONObject("Playlist").optString("id")
                    item.playlistName = msg.getJSONObject("Playlist").optString("name")
                    if (item.user_id != null && item.user_id != "null" && item.user_id != "0") {
                        playlistMapList[item.video_id] = item
                        temp_list.add(item)
                    }
                }
                if (dataList.isEmpty()) {
                    setTabs(true)
                }
                dataList.addAll(temp_list)
                for (item in temp_list) {
                    pagerSatetAdapter!!.addFragment(
                        VideosPlayFragment(
                            false,
                            item,
                            binding.viewpager,
                            this,
                            fragmentConainerId
                        )
                    )
                }
                pagerSatetAdapter!!.refreshStateSet(false)
                pagerSatetAdapter!!.notifyDataSetChanged()
                binding.viewpager.setCurrentItem(currentPositon, true)
                if (isFirstTime) {
                    setupPlaylist()
                }
            }
        } catch (e: JSONException) {
            Log.d(Constants.tag, "Error: Exception: $e")
            if (pageCount > 0) pageCount--
        } finally {
            isApiRuning = false
        }
    }


    fun parseMyVideoData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msg = jsonObject.optJSONObject("msg")
                val temp_list = ArrayList<HomeModel>()
                val public_array = msg.optJSONArray("public")
                val pinnedVideo = HashMap<String?, HomeModel>()
                for (i in 0 until public_array.length()) {
                    val itemdata = public_array.optJSONObject(i)
                    val video = itemdata.optJSONObject("Video")
                    val user = itemdata.optJSONObject("User")
                    val sound = itemdata.optJSONObject("Sound")
                    val location = itemdata.optJSONObject("Location")
                    val store = itemdata.optJSONObject("Store")
                    val videoProduct = itemdata.optJSONObject("Product")
                    val userPrivacy = user.optJSONObject("PrivacySetting")
                    val userPushNotification = user.optJSONObject("PushNotification")
                    val item = DataParsing.parseVideoData(
                        user,
                        sound,
                        video,
                        location,
                        store,
                        videoProduct,
                        userPrivacy,
                        userPushNotification
                    )
                    if (item.user_id != null && item.user_id != "null" && item.user_id != "0") {
                        if (item.pin == "1") {
                            pinnedVideo[item.video_id] = item
                        }
                        temp_list.add(item)
                    }
                    Log.d(Constants.tag, item.tagProductList!!.size.toString() + " sizee")
                }
                if (pinnedVideo != null) {
                    Paper.book("PinnedVideo").write("pinnedVideo", pinnedVideo)
                }
                if (dataList.isEmpty()) {
                    setTabs(true)
                }
                dataList.addAll(temp_list)
                for (item in temp_list) {
                    pagerSatetAdapter!!.addFragment(
                        VideosPlayFragment(
                            false,
                            item,
                            binding.viewpager,
                            this,
                            fragmentConainerId
                        )
                    )
                }
                pagerSatetAdapter!!.refreshStateSet(false)
                pagerSatetAdapter!!.notifyDataSetChanged()
                setupPlaylist()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (pageCount > 0) pageCount--
        } finally {
            isApiRuning = false
        }
    }

    // api for get the videos list from server
    private fun callApiForPlaylistVideos(platlistId: String?, iSFirstTime: Boolean) {
        val parameters = JSONObject()
        try {
            parameters.put("id", platlistId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this@WatchVideosActivity,
            ApiLinks.showPlaylists,
            parameters,
            Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this@WatchVideosActivity, resp)
            binding.swiperefresh.isRefreshing = false
            parsePlalistVideoData(resp, iSFirstTime)
        }
    }

    // api for get the videos list from server
    private fun callApiForUserVideos() {
        val parameters = JSONObject()
        try {
            parameters.put(
                "user_id",
                Functions.getSharedPreference(myContext).getString(Variables.U_ID, "")
            )
            if (!userId.equals(
                    Functions.getSharedPreference(myContext).getString(Variables.U_ID, ""),
                    ignoreCase = true
                )
            ) {
                parameters.put("other_user_id", userId)
            }
            parameters.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this@WatchVideosActivity,
            ApiLinks.showVideosAgainstUserID,
            parameters,
            Functions.getHeaders(this)
        ) { resp ->
            binding.swiperefresh.isRefreshing = false
            parseMyVideoData(resp)
        }
    }

    // api for get the videos list from server
    private fun callApiForRepostVideos() {
        val parameters = JSONObject()
        try {
            parameters.put("user_id", userId)
            parameters.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this@WatchVideosActivity,
            ApiLinks.showUserRepostedVideos,
            parameters,
            Functions.getHeaders(this)
        ) { resp ->
            binding.swiperefresh.isRefreshing = false
            parseRepostVideoData(resp)
        }
    }

    // api for get the videos list from server
    private fun callApiForLikedVideos() {
        val parameters = JSONObject()
        try {
            parameters.put("user_id", userId)
            parameters.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this@WatchVideosActivity,
            ApiLinks.showUserLikedVideos,
            parameters,
            Functions.getHeaders(this)
        ) { resp ->
            binding.swiperefresh.isRefreshing = false
            parseVideoData(resp)
        }
    }

    // api for get the videos list from server
    private fun callApiForPrivateVideos() {
        val parameters = JSONObject()
        try {
            parameters.put("user_id", userId)
            parameters.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this@WatchVideosActivity,
            ApiLinks.showVideosAgainstUserID,
            parameters,
            Functions.getHeaders(this)
        ) { resp ->
            binding.swiperefresh.isRefreshing = false
            parsePrivateVideoData(resp)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == callbackVideoLisCode) {
            val bundle = Bundle()
            bundle.putBoolean("isShow", true)
            VideosPlayFragment.videoListCallback!!.onResponce(bundle)
        }
    }

    override fun onStart() {
        super.onStart()
        if (pagerSatetAdapter != null && pagerSatetAdapter!!.count > 0) {
            val fragment = pagerSatetAdapter!!.getItem( binding.viewpager.currentItem) as VideosPlayFragment
            fragment.mainMenuVisibility(true)
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("isShow", true)

        val args = Bundle()
        args.putSerializable("arraylist", dataList)
        DataHolder.instance?.data = args

        intent.putExtra("pageCount", pageCount)
        setResult(RESULT_OK, intent)
        finish()
    }


    override fun onPause() {
        super.onPause()
        if (pagerSatetAdapter != null && pagerSatetAdapter!!.count > 0) {
            val fragment = pagerSatetAdapter!!.getItem( binding.viewpager.currentItem) as VideosPlayFragment
            fragment.mainMenuVisibility(false)
        }
    }

    fun moveBack() {
        val intent = Intent()
        intent.putExtra("isShow", true)
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        private const val callbackVideoLisCode = 3292
    }
}
