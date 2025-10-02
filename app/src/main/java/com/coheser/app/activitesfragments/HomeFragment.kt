package com.coheser.app.activitesfragments

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.work.WorkManager
import com.adcolony.sdk.AdColony
import com.adcolony.sdk.AdColonyAdOptions
import com.adcolony.sdk.AdColonyInterstitial
import com.adcolony.sdk.AdColonyInterstitialListener
import com.adcolony.sdk.AdColonyZone
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.tabs.TabLayout
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.accounts.UserInterestActivity
import com.coheser.app.activitesfragments.livestreaming.activities.LiveUsersActivity
import com.coheser.app.activitesfragments.location.AddAddressActivity
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.activitesfragments.search.SearchMainActivity
import com.coheser.app.adapters.HomeSuggestionAdapter
import com.coheser.app.adapters.ViewPagerStatAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentHomeBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.mainmenu.MainMenuActivity
import com.coheser.app.models.HomeModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.ApiRepository.callApiForFollowUnFollow
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.FileUtils
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.checkLoginUser
import com.coheser.app.simpleclasses.Functions.checkProfileOpenValidation
import com.coheser.app.simpleclasses.Functions.getSettingsPreference
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.HomeViewModel
import com.volley.plus.interfaces.APICallBack
import com.yarolegovich.discretescrollview.DSVOrientation
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import io.paperdb.Paper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Collections
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeFragment : Fragment(), FragmentCallBack {
    var dataList: ArrayList<HomeModel?> = ArrayList()
    var type = "typeforYou"
    var adapterSuggestion: HomeSuggestionAdapter? = null
    lateinit var binding: FragmentHomeBinding


    var handler: Handler? = null

    var mReceiver: HomeBroadCast? = null
    private val viewModel: HomeViewModel by viewModel()

    override fun onResponce(bundle: Bundle) {
        bundle?.getString("action")?.let { action ->
            when (action) {
                "showad" -> showCustomAd()
                "hidead" -> hideCustomad()
                "removeList" -> {
                    pagerSatetAdapter?.removeFragment(binding.viewpager?.currentItem ?: 0)
                    dataList.removeAt(binding.viewpager?.currentItem ?: 0)
                }
                else -> {}
            }
        }
    }


    inner class HomeBroadCast : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (intent.hasExtra("type")) {
                val actionType = intent.getStringExtra("type")
                if (actionType.equals("interest")) {
                   // refreshData()
                }

            } else {

                val workInfos =
                    WorkManager.getInstance(context).getWorkInfosByTag("videoUpload").get()
                val isRunningLiveData =
                    workInfos.any { it.state == androidx.work.WorkInfo.State.RUNNING }
                if (isRunningLiveData) {
                    if (::binding.isInitialized) {

                        binding.uploadVideoLayout.visibility = View.VISIBLE
                        if (fragment != null && fragment!!::binding.isInitialized) {
                            fragment?.binding?.progressBar?.progress = 0
                            fragment?.binding?.tvProgressCount?.text = "0%"
                        }
                        val bitmap = FileUtils.base64ToBitmap(
                            getSharedPreference(context)
                                .getString(Variables.default_video_thumb, "")
                        )

                        if (bitmap != null) binding.uploadingThumb.setImageBitmap(bitmap)

                    }
                }
                else {
                    if (::binding.isInitialized) {
                        binding.uploadVideoLayout.visibility = View.GONE
                    }
                }
            }

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        showAddValue = getSettingsPreference(context).getInt(
            Variables.ShowAdvertAfter,
            Constants.SHOW_AD_ON_EVERY
        )

        handler = Handler(Looper.getMainLooper())

        binding.liveUsers.setOnClickListener(DebounceClickHandler { view ->
            onPause()
            val intent = Intent(view.context, LiveUsersActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        })

        binding.discoverBtn.setOnClickListener {
            val intent = Intent(binding.root.context, SearchMainActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        }




        binding.tabPlaylist.setOnClickListener(DebounceClickHandler { openPlaylist() })

        binding.swiperefresh.setProgressViewOffset(false, 0, 200)
        binding.swiperefresh.setColorSchemeResources(R.color.black)

        binding.swiperefresh.setOnRefreshListener {

            swipeCount=0
            dataList.clear()
            viewModel.refreshCurrentTab(type)

        }



        if (!getSettingsPreference(context).getString(Variables.AddType, "none")
                .equals("none", ignoreCase = true)
        ) {
            if (getSettingsPreference(context).getString(Variables.AddType, "")
                    .equals("adcolony", ignoreCase = true)
            ) {
                initInterstitialAdColonyAd()
            }
        }


        mReceiver = HomeBroadCast()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().registerReceiver(
                mReceiver,
                IntentFilter(Variables.homeBroadCastAction),
                Context.RECEIVER_NOT_EXPORTED
            )
        }
        else {
            requireActivity().registerReceiver(
                mReceiver, IntentFilter(Variables.homeBroadCastAction)
            )

        }


        if (Functions.isWorkManagerRunning(binding.root.context, "videoUpload")) {
            binding.uploadVideoLayout.visibility = View.VISIBLE
            val bitmap = FileUtils.base64ToBitmap(
                Functions.getSharedPreference(binding.root.context)
                    .getString(Variables.default_video_thumb, "")
            )
            if (bitmap != null) binding.uploadingThumb.setImageBitmap(bitmap)
        }
        else {
            binding.uploadVideoLayout.visibility = View.GONE
        }


        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

          binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.loginLayoutVisibility.set(false)
        viewModel.recylerViewVisiblity.set(true)


        if (Functions.checkLogin(activity)) {
            viewModel.getUserDetails()
        }

        setObserveAble()

        dataList.clear()
        dataList.addAll(viewModel.getArrayList(type))



        viewModel.getAddressLabel()

        setTabs(true)

        viewModel.callVideoApi(type)

    }

    fun setObserveAble() {

        viewModel.nearByLiveData.observe(viewLifecycleOwner, {
            viewModel.isApiRunning.set(false)
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let {

                        if (type.equals(viewModel.typeNearBy)) {
                            Functions.printLog(
                                Constants.tag, "page" + viewModel.nearByPageCount.get()
                            )

                            if (viewModel.nearByPageCount.get() == 0) {
                                Functions.printLog(
                                    Constants.tag, "clear" + viewModel.nearByPageCount.get()
                                )
                                viewModel.nearbyList.clear()

                                dataList.clear()
                                viewModel.saveArrayList(type, it)
                            }
                            viewModel.nearbyList.addAll(it)
                            parseData(it)
                        }

                    }
                }

                is ApiResponce.Error -> {
                    binding.swiperefresh.isRefreshing=false
                    if (!it.isRequestError) {
                        if (viewModel.nearByPageCount.get() == 0) {
                            dataList.clear()
                            viewModel.saveArrayList(type, ArrayList())
                        }
                    }
                    viewModel.decreasePageCount(type, !it.isRequestError)

                }

                is ApiResponce.Loading -> {

                }

                else -> {}
            }
        })

        viewModel.followingLiveData.observe(viewLifecycleOwner, {
            viewModel.isApiRunning.set(false)
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let {

                        if (type.equals(viewModel.typeFollowing)) {

                            if (viewModel.followingPageCount.get() == 0) {
                                viewModel.followingList.clear()
                                dataList.clear()
                                viewModel.saveArrayList(type, it)
                            }

                            viewModel.followingList.addAll(it)
                            parseData(it)
                        }
                    }
                }

                is ApiResponce.Error -> {
                    binding.swiperefresh.isRefreshing=false
                    if (!it.isRequestError) {
                        if (viewModel.followingPageCount.get() == 0) {
                            dataList.clear()
                            viewModel.saveArrayList(type, ArrayList())

                            showToast(activity, getString(R.string.follow_an_account_to_see_there_video_here))

                            binding.tabNoFollower.visibility = View.VISIBLE
                            binding.viewpager.visibility = View.GONE
                            onPause()
                            binding.swiperefresh.isEnabled = false
                            setUpSuggestionRecyclerview()

                        }
                    }
                    viewModel.decreasePageCount(type, !it.isRequestError)
                }

                is ApiResponce.Loading -> {
                }

                else -> {}
            }
        })

        viewModel.forYouLiveData.observe(viewLifecycleOwner, {
            viewModel.isApiRunning.set(false)
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let {
                        if (type.equals(viewModel.typeforYou)) {
                            if (viewModel.forYouPageCount.get() == 0) {
                                viewModel.forYouList.clear()
                                if(dataList.size>0 && dataList.get(0)?.promote.equals("1")){
                                }
                                else{
                                    dataList.clear()
                                }

                                viewModel.saveArrayList(type, it)
                            }
                            viewModel.forYouList.addAll(it)
                            parseData(it)
                        }
                    }
                }

                is ApiResponce.Error -> {
                    binding.swiperefresh.isRefreshing=false
                    if (it.isRequestError) {
                        if (viewModel.forYouPageCount.get() == 0) {
                            dataList.clear()
                            viewModel.saveArrayList(type, ArrayList())
                        }
                    }
                    viewModel.decreasePageCount(type, !it.isRequestError)

                }

                is ApiResponce.Loading -> {
                }

                else -> {}
            }
        })

        viewModel.userDetailLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let {
                        if (it != null) {

                            Functions.getSharedPreference(binding.root.context).edit().putInt(
                                Variables.notificationCount, it.unread_notification.toInt()
                            ).commit()

                            if (it.intrestsCount < 1) {
                                startActivity(
                                    Intent(
                                        requireActivity(), UserInterestActivity::class.java
                                    )
                                )
                            }

                        }
                    }

                }


                else -> {}
            }
        })

        viewModel.suggesstionLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let {

                        suggestionList.clear()
                        suggestionList.addAll(it)
                        adapterSuggestion?.notifyDataSetChanged()

                        if (suggestionList.isEmpty()) {
                            binding.tvNoSuggestionFound.visibility = View.VISIBLE
                        } else {
                            binding.tvNoSuggestionFound.visibility = View.GONE
                        }

                    }
                }
                is ApiResponce.Error -> {
                    binding.tvNoSuggestionFound.visibility = View.VISIBLE
                }

                else -> {}
            }
        })

        viewModel.followLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let { userModel ->
                        if (userModel != null) {
                            for (item in suggestionList) {
                                if (item.id.equals(userModel.id)) {
                                    suggestionList.remove(item)
                                    adapterSuggestion?.notifyDataSetChanged()
                                    break
                                }
                            }
                            viewModel.callVideoApi(type)
                        }
                    }

                }

                else -> {}
            }
        })

        viewModel.addressTxt.observe(viewLifecycleOwner,{
            setTabs(it)
        })

    }


    fun setTabs(location:String){
        if(binding.tabLayout.getTabAt(0)==null) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(location))
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.following)))
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.for_you)))
            binding.tabLayout.getTabAt(2)?.select()
            binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    Functions.printLog(Constants.tag, "tab Position:" + tab.position)
                    viewModel.recylerViewVisiblity.set(true)
                    when (tab.position) {

                        0 -> {
                            swipeCount=0
                            dataList.clear()
                            binding.swiperefresh.isRefreshing = true
                            type = viewModel.typeNearBy
                            viewModel.callVideoApi(type)

                        }

                        1 -> {
                            swipeCount=0
                            dataList.clear()
                            binding.swiperefresh?.isRefreshing = true
                            type = viewModel.typeFollowing
                            viewModel.callVideoApi(type)

                        }

                        2 -> {
                            swipeCount=0
                            dataList.clear()
                            binding.swiperefresh?.isRefreshing = true
                            type = viewModel.typeforYou
                            viewModel.callVideoApi(type)
                        }


                    }

                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}

                override fun onTabReselected(tab: TabLayout.Tab) {
                    when (tab.position) {

                        0 -> {
                            if (type.equals(viewModel.typeNearBy, true)) {
                                if(checkLoginUser(requireActivity())) {


                                    val intent = Intent(context, AddAddressActivity::class.java)
                                    try {
                                        resultCallbackAddress.launch(intent)
                                    }catch (e:Exception){
                                        startActivity(intent)
                                    }

                                }
                            }
                        }

                        2 -> {
                            if (type.equals(viewModel.typeforYou, ignoreCase = true)) {
                                if (checkLoginUser(activity)) {
                                    val intent =
                                        Intent(requireActivity(), UserInterestActivity::class.java)
                                    intent.putExtra("from", "foryou")

                                    try {
                                        resultCallbackAddress.launch(intent)
                                    }catch (e:Exception){
                                        startActivity(intent)
                                    }
                                    activity?.overridePendingTransition(
                                        R.anim.in_from_bottom, R.anim.out_to_top
                                    )
                                }
                            }
                        }

                    }
                }
            })
        }
        else{
            binding.tabLayout.removeTab(binding.tabLayout.getTabAt(0)!!)
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(location),0)
            binding.tabLayout.getTabAt(0)?.select()
        }
    }
    var resultCallbackAddress = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            viewModel.getAddressLabel()
            refreshData()
        }
    }


    fun refreshData() {
        dataList.clear()
        viewModel.refreshAllData(type)
    }

    // set the fragments for all the videos list
    var showAddValue: Int = 0
    var swipeCount: Int = 0
    var pagerSatetAdapter: ViewPagerStatAdapter? = null
    fun setTabs(isFirstTime: Boolean) {
        dataList.clear()
        if (isFirstTime) {
            try {
                if (Paper.book(Variables.PromoAds).contains(Variables.PromoAdsModel)) {
                    val item =
                        Paper.book(Variables.PromoAds).read<HomeModel>(Variables.PromoAdsModel)
                    dataList.add(item)
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception: $e")
            }
        }


        pagerSatetAdapter = ViewPagerStatAdapter(
            childFragmentManager,
            binding.viewpager,
            isFirstTime
        ) { bundle: Bundle -> this.onResponce(bundle) }
        binding.viewpager.adapter = pagerSatetAdapter
        binding.viewpager.offscreenPageLimit = 1
        binding.viewpager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {

                if (position == 0) {
                    binding.swiperefresh?.isEnabled = true
                }
                else {
                    binding.swiperefresh?.isEnabled = false
                }

                if (position == 0 && (pagerSatetAdapter != null && pagerSatetAdapter?.count!! > 0)) {
                    val fragment =
                        pagerSatetAdapter?.getItem(binding.viewpager.currentItem) as VideosPlayFragment
                    fragment.setData()
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(200)
                        fragment.setPlayer(is_visible_to_user)
                    }
                }

                updatePlaylistView()


                if (dataList.size > 5 && (dataList.size - 5) == (position + 1)) {
                    if (viewModel.isApiRunning.get()==false) {
                        viewModel.increasePageCount(type)
                        viewModel.callVideoApi(type)
                    }
                }

                swipeCount++
                Functions.printLog(Constants.tag,"swipeCount:"+swipeCount)

                if (swipeCount == showAddValue) {

                    swipeCount=0

                    if (!getSettingsPreference(context).getString(Variables.AddType, "none")
                            .equals("none", ignoreCase = true)
                    ) {
                        if (getSettingsPreference(context).getString(Variables.AddType, "")
                                .equals("admob", ignoreCase = true)
                        ) {
                            initGoogleAdd()
                        } else if (getSettingsPreference(context).getString(Variables.AddType, "")
                                .equals("adcolony", ignoreCase = true)
                        ) {
                            showAdColonyAdd()
                        }
                    }
                }

            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    private fun updatePlaylistView() {
        try {
            if (dataList[binding.viewpager.currentItem]?.playlistId == "0") {
                binding.tabPlaylist.visibility = View.GONE
                binding.tabSneekbarView.visibility = View.GONE
            } else {
                binding.tvPlaylistTitle.text = getString(R.string.playlist) + " . " + dataList[binding.viewpager.currentItem]?.playlistName
                binding.tabPlaylist.visibility = View.VISIBLE
                binding.tabSneekbarView.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            binding.tabPlaylist?.visibility = View.GONE
            binding.tabSneekbarView?.visibility = View.GONE
        }
    }


    private fun openPlaylist() {
        if (dataList.size >= binding.viewpager.currentItem) {
            val itemUpdate = dataList[binding.viewpager.currentItem]
            val fragment = ShowHomePlaylistF(
                itemUpdate?.video_id,
                itemUpdate?.playlistId,
                itemUpdate?.user_id,
                itemUpdate?.playlistName
            ) { bundle ->
                if (bundle.getBoolean("isShow", false)) {
                    if (bundle.getString("type").equals("videoPlay", ignoreCase = true)) {
                        val playlistVideoPosition = bundle.getInt("position", 0)
                        openPlaylistVideo(
                            itemUpdate?.playlistId,
                            itemUpdate?.playlistName,
                            itemUpdate?.user_id,
                            playlistVideoPosition
                        )
                    }
                }
            }
            fragment.show(requireActivity().supportFragmentManager, "ShowHomePlaylistF")
        }
    }

    // open the videos in full screen on click
    private fun openPlaylistVideo(
        id: String?,
        playlistName: String?,
        userId: String?,
        position: Int
    ) {
        val intent = Intent(activity, WatchVideosActivity::class.java)
        intent.putExtra("playlist_id", id)
        intent.putExtra("position", position)
        intent.putExtra("pageCount", "0")
        intent.putExtra("userId", userId)
        intent.putExtra("playlistName", playlistName)
        intent.putExtra("whereFrom", Variables.playlistVideo)
        resultInfoAgainCallback.launch(intent)
    }

    var resultInfoAgainCallback: ActivityResultLauncher<Intent> =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
                override fun onActivityResult(result: ActivityResult?) {
                    if (result?.resultCode == Activity.RESULT_OK) {
                        val data = result.data
                        data.let {
                            if (data!!.getBooleanExtra("isShow", false)) {
                            }  
                        }
                       
                    }
                }
            })


    var suggestionList: ArrayList<UserModel> = ArrayList()
    private var infiniteAdapter: InfiniteScrollAdapter<*>? = null
    private fun setUpSuggestionRecyclerview() {
        binding.rvSugesstion.setOrientation(DSVOrientation.HORIZONTAL)
        adapterSuggestion = HomeSuggestionAdapter(suggestionList) { view, postion, `object` ->
            val item = `object` as UserModel
            if (view.id == R.id.tvFollowBtn) {
                if (checkLoginUser(activity)) {
                    followSuggestedUser(item.id, postion)
                }
            } else if (view.id == R.id.user_image) {
                if (checkProfileOpenValidation(item.id)) {
                    val intent = Intent(view.context, ProfileActivity::class.java)
                    intent.putExtra("user_id", item.id)
                    intent.putExtra("user_name", item.username)
                    intent.putExtra("user_pic", item.getProfilePic())
                    startActivity(intent)
                   requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
                }
            } else if (view.id == R.id.ivCross) {
                suggestionList.removeAt(postion)
                adapterSuggestion?.notifyDataSetChanged()
            }
        }
        infiniteAdapter = InfiniteScrollAdapter.wrap(
            adapterSuggestion!!
        )
        binding.rvSugesstion.adapter = infiniteAdapter
        binding.rvSugesstion.setItemTransitionTimeMillis(150)
        binding.rvSugesstion.setItemTransformer(
            ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build()
        )

        if (suggestionList.isEmpty()) {
            viewModel.getSuggesstionList()
        }

    }

    private fun followSuggestedUser(userId: String?, position: Int) {
        callApiForFollowUnFollow(
            activity,
            getSharedPreference(context).getString(Variables.U_ID, ""),
            userId,
            object : APICallBack {
                override fun arrayData(arrayList: ArrayList<*>?) {
                }

                override fun onSuccess(responce: String) {
                    suggestionList.removeAt(position)
                    adapterSuggestion?.notifyDataSetChanged()
                    viewModel.callVideoApi(type)
                }

                override fun onFail(responce: String) {
                }
            })
    }


    override fun onPause() {
        super.onPause()
        if (pagerSatetAdapter != null && pagerSatetAdapter?.count!! > 0) {
            val fragment =
                pagerSatetAdapter?.getItem(binding.viewpager.currentItem) as VideosPlayFragment
            fragment.mainMenuVisibility(false)
        }
    }


    // parse the list of the videos
    fun parseData(list: ArrayList<HomeModel>) {
        binding.swiperefresh.isRefreshing = false

        if(list.isNotEmpty()){
                Collections.shuffle(list)

                if (dataList.isEmpty()) {
                    setTabs(false)
                }

                dataList.addAll(list)

                for (item in list) {
                    pagerSatetAdapter?.addFragment(VideosPlayFragment(false, item, binding.viewpager, this, R.id.mainMenuFragment))
                }
                pagerSatetAdapter?.refreshStateSet(false)
                pagerSatetAdapter?.notifyDataSetChanged()

                if (!(binding.swiperefresh?.isEnabled)!!) {
                    binding.swiperefresh?.isEnabled = false
                }

                binding.tabNoFollower?.visibility = View.GONE
                binding.viewpager?.visibility = View.VISIBLE

            updatePlaylistView()
            }
        else {
                hideCustomad()

                if (dataList.isEmpty() && type.equals("following", ignoreCase = true)) {
                    showToast(
                        activity,
                        getString(R.string.follow_an_account_to_see_there_video_here)
                    )
                    binding.tabNoFollower.visibility = View.VISIBLE
                    binding.viewpager.visibility = View.GONE
                    onPause()
                    binding.swiperefresh.isEnabled = false
                    setUpSuggestionRecyclerview()
                }
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == callbackVideoLisCode) {
            val bundle = Bundle()
            bundle.putBoolean("isShow", true)
            VideosPlayFragment.videoListCallback?.onResponce(bundle)
        }
    }

    fun showCustomAd() {
        if (is_visible_to_user && (type != null && type.equals(viewModel.typeforYou, ignoreCase = true))) {
            binding.topBtnLayout.visibility = View.GONE

            if (MainMenuActivity.tabLayout != null)
                MainMenuActivity.tabLayout?.visibility = View.GONE
        }
    }

    fun hideCustomad() {
        if (MainMenuActivity.tabLayout != null) {
            MainMenuActivity.tabLayout?.visibility = View.VISIBLE
        }
        binding.topBtnLayout.visibility = View.VISIBLE

      if(MainMenuActivity.mainMenuActivity!=null)
            MainMenuActivity.mainMenuActivity?.setRoomListerner();

    }


    var mInterstitialAd: InterstitialAd? = null
    fun initGoogleAdd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            requireContext(), getString(R.string.my_Interstitial_Add), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    mInterstitialAd = interstitialAd
                    Log.d(Constants.tag, "onAdLoaded")
                    mInterstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d(Constants.tag, "Ad was clicked.")
                            }

                            override fun onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(Constants.tag, "Ad dismissed fullscreen content.")
                                mInterstitialAd = null
                                if (activity != null) {
                                    activity?.runOnUiThread {
                                        val fragment = pagerSatetAdapter?.getItem(
                                            binding.viewpager.currentItem
                                        ) as VideosPlayFragment
                                        fragment.exoplayer?.playWhenReady = true
                                    }
                                }
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Log.e(Constants.tag, "Ad failed to show fullscreen content.")
                                mInterstitialAd = null
                            }

                            override fun onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(Constants.tag, "Ad recorded an impression.")
                            }

                            override fun onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(Constants.tag, "Ad showed fullscreen content.")
                                Handler(Looper.getMainLooper()).postDelayed({
                                    if (activity != null) {
                                        activity?.runOnUiThread {
                                            try {
                                                val fragment = pagerSatetAdapter?.getItem(
                                                    binding.viewpager.currentItem
                                                ) as VideosPlayFragment
                                                fragment.exoplayer?.playWhenReady = false
                                            } catch (e: Exception) {
                                                Log.d(Constants.tag, "Exception: $e")
                                            }
                                        }
                                    }
                                }, 1000)
                            }
                        }

                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(activity!!)
                    }
                }


                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    Log.d(Constants.tag, loadAdError.toString())
                    mInterstitialAd = null
                }
            })
    }


    private var interstitialAdColony: AdColonyInterstitial? = null
    private var interstitialListener: AdColonyInterstitialListener? = null
    private var interstitialAdOptions: AdColonyAdOptions? = null
    private fun initInterstitialAdColonyAd() {
       interstitialListener = object : AdColonyInterstitialListener() {
            // Code to be executed when an ad request is filled.
            // get AdColonyInterstitial object from adcolony Ad Server.
            override fun onRequestFilled(adIn: AdColonyInterstitial) {
                // Ad passed back in request filled callback, ad can now be shown
                interstitialAdColony = adIn
                isInterstitialLoaded = true
            }

            // Code to be executed when an ad request is not filled
            override fun onRequestNotFilled(zone: AdColonyZone) {
                super.onRequestNotFilled(zone)
            }

            //Code to be executed when an ad opens
            override fun onOpened(ad: AdColonyInterstitial) {
                super.onOpened(ad)
            }

            //Code to be executed when user closed an ad
            override fun onClosed(ad: AdColonyInterstitial) {
                super.onClosed(ad)
                Toast.makeText(context, "Ad is closed!", Toast.LENGTH_SHORT).show()

                //request new Interstitial Ad on close
                AdColony.requestInterstitial(
                    Constants.AD_COLONY_INTERSTITIAL_ID,
                    interstitialListener!!,
                    interstitialAdOptions
                )
            }

            // Code to be executed when the user clicks on an ad.
            override fun onClicked(ad: AdColonyInterstitial) {
                super.onClicked(ad)
            }

            // called after onAdOpened(), when a user click opens another app
            // (such as the Google Play), backgrounding the current app
            override fun onLeftApplication(ad: AdColonyInterstitial) {
                super.onLeftApplication(ad)
            }

            // Code to be executed when an ad expires.
            override fun onExpiring(ad: AdColonyInterstitial) {
                super.onExpiring(ad)
            }
        }
        interstitialAdOptions = AdColonyAdOptions()
        AdColony.requestInterstitial(
            Constants.AD_COLONY_INTERSTITIAL_ID,
            interstitialListener!!,
            interstitialAdOptions
        )
    }


    fun showAdColonyAdd() {
        if (interstitialAdColony != null && isInterstitialLoaded) {
            interstitialAdColony?.show()
        }
    }

    // this will call when go to the home tab From other tab.
    // this is very importent when for video play and pause when the focus is changes
    var is_visible_to_user: Boolean = false
    override fun setMenuVisibility(visible: Boolean) {
        super.setMenuVisibility(visible)
        is_visible_to_user = visible

        if (is_visible_to_user && pagerSatetAdapter != null && pagerSatetAdapter?.count!! > 0) {

            CoroutineScope(Dispatchers.Main).launch {
                delay(200)
                if (binding.tabNoFollower.visibility == View.VISIBLE) {
                    onPause()
                } else {
                    val fragment =
                        pagerSatetAdapter?.getItem(binding.viewpager.currentItem) as VideosPlayFragment
                    fragment.mainMenuVisibility(is_visible_to_user)
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mReceiver != null) {
            requireActivity().unregisterReceiver(mReceiver)
            mReceiver = null
        }
    }

    companion object {
        var fragment: HomeFragment? = null
        fun newInstance(): HomeFragment? {
            if (fragment == null) {
                fragment = HomeFragment()
                val args = Bundle()
                fragment?.arguments = args
            }
            return fragment
        }

        var uploadingCallback: FragmentCallBack = FragmentCallBack { bundle ->
            if (bundle.getBoolean("isShow")) {
                if (fragment != null && fragment?.binding != null) {
                    val currentProgress = bundle.getInt("currentpercent", 0)
                    if (fragment?.binding?.progressBar != null && newInstance()?.binding?.tvProgressCount != null) {
                        newInstance()?.binding?.progressBar?.progress = currentProgress
                        newInstance()?.binding?.tvProgressCount?.text = "$currentProgress%"
                    }
                }
            }
        }

        private const val callbackVideoLisCode = 3292
        private var isInterstitialLoaded = false
    }

}
