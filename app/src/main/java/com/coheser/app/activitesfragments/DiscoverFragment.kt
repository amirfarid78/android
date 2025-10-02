package com.coheser.app.activitesfragments

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.activities.MultiViewLiveActivity
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.activitesfragments.livestreaming.utils.StreamingFirebaseManager
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.activitesfragments.spaces.adapters.MainHomeAdapter
import com.coheser.app.activitesfragments.spaces.models.RoomModel
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.RoomFirebaseListener
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.RoomFirebaseManager
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.RoomManager
import com.coheser.app.adapters.DiscoverAdapter
import com.coheser.app.adapters.ShopDiscoverAdapter
import com.coheser.app.adapters.SlidingAdapter
import com.coheser.app.adapters.StreamingDiscoverAdapter
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentDiscoverBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.mainmenu.MainMenuActivity
import com.coheser.app.models.DiscoverModel
import com.coheser.app.models.HomeModel
import com.coheser.app.models.SliderModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.DataHolder
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.checkLoginUser
import com.coheser.app.simpleclasses.Functions.checkProfileOpenValidation
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getPermissionStatus
import com.coheser.app.simpleclasses.Functions.showPermissionSetting
import com.coheser.app.simpleclasses.PermissionUtils
import com.coheser.app.viewModels.DiscoverViewModel
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel


class DiscoverFragment : Fragment(), View.OnClickListener {

    var datalist = mutableListOf<DiscoverModel>()
    var adapter: DiscoverAdapter? = null
    var linearLayoutManager: LinearLayoutManager? = null
    var parentPostion = 0
    lateinit var binding: FragmentDiscoverBinding
    var isSliderApiCall: Boolean = false

    private val viewModel: DiscoverViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_discover, container, false)

        takePermissionUtils = PermissionUtils(requireActivity(), mPermissionResult)

        linearLayoutManager = LinearLayoutManager(binding.root.context)
        linearLayoutManager!!.orientation = RecyclerView.VERTICAL
        binding.recylerview.layoutManager = linearLayoutManager
        binding.recylerview.setHasFixedSize(true)
        (binding.recylerview.itemAnimator as SimpleItemAnimator?)!!.supportsChangeAnimations = false

        val json = Functions.getSharedPreference(binding.root.context).getString("showDiscoverySections", "")
        if (!TextUtils.isEmpty(json)) {
            val type = object : TypeToken<ArrayList<DiscoverModel>?>() {}.type
            val gson = Gson()
            datalist= gson.fromJson(json, type)
        }

        adapter = DiscoverAdapter(
            binding.root.context,
            datalist, object: DiscoverAdapter.OnItemClickListener{
                override fun onItemClick(
                    view: View?,
                    video_list: ArrayList<HomeModel?>,
                    main_position: Int,
                    child_position: Int
                ) {
                    parentPostion = main_position
                    if (view?.id == R.id.hashtag_layout || video_list[child_position] == null) {
                        openHashtag(datalist[main_position].title)
                    } else {
                        openWatchVideo(child_position, video_list, datalist[main_position].title)
                    }
                }
            })
        binding.recylerview.adapter = adapter

        binding.recylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var userScrolled = false
            var scrollOutitems = 0
            var scrollInItem = 0
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollInItem = linearLayoutManager!!.findFirstVisibleItemPosition()
                scrollOutitems = linearLayoutManager!!.findLastVisibleItemPosition()

                if (userScrolled && scrollOutitems == datalist!!.size - 1) {
                    userScrolled = false
                    if (viewModel.loadMoreProgressVisibility.get() ==false && !viewModel.ispostFinsh) {
                        viewModel.loadMoreProgressVisibility.set(true)
                        viewModel.pageCount.set(viewModel.pageCount.get()+1)
                        viewModel.showDiscoverySections()
                    }
                }
            }
        })
        binding.searchLayout.setOnClickListener(this)
        binding.searchEdit.setOnClickListener(this)
        binding.shopBtn.setOnClickListener(this)

        setTabs()
        getStreamingUser()
        startRoomListener()
        setupRoomAdapter()
        setUpShopsAdapter()

        return binding.getRoot()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setObserveAble()
        viewModel.showRoom()

    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if(::binding.isInitialized && menuVisible){

            val postion= binding.tabLayout.selectedTabPosition
            if(roomDatalist.isEmpty() || postion==2)
                viewModel.showRoom()

            viewModel.showShops()

            if(sliderList.isEmpty())
                callApiSlider()

            viewModel.showDiscoverySections()
        }
    }

    fun setObserveAble(){

        viewModel.videosLiveData.observe(viewLifecycleOwner,{
            when(it){
                is ApiResponce.Loading ->{

                    if(viewModel.pageCount.get()==0 && datalist?.isEmpty() == true) {
                        binding.shimmerLayout.shimmerViewContainer.startShimmer()
                        binding.shimmerLayout.shimmerViewContainer.visibility = View.VISIBLE
                    }else{
                        binding.shimmerLayout.shimmerViewContainer.stopShimmer()
                        binding.shimmerLayout.shimmerViewContainer.visibility = View.GONE
                    }
                }

                is ApiResponce.Success ->{
                    it.data?.let {
                        if (viewModel.pageCount.get() == 0) {
                            datalist!!.clear()
                            saveHashTagArrayList(it)
                        }
                        datalist!!.addAll(it)
                        changeUi()
                    }

                }

                is ApiResponce.Error ->{
                    changeUi()
                }

            }
        })

        viewModel.shopsLiveData.observe(viewLifecycleOwner,{
            when(it){
                is ApiResponce.Loading ->{

                }
                is ApiResponce.Error -> {

                }
                is ApiResponce.Success -> {
                    shopsDataList.clear()
                    it.data?.let {
                        it1 -> shopsDataList.addAll(it1)
                    shopDiscoverAdapter?.notifyDataSetChanged()}
                }
            }
        })


        viewModel.roomLiveData.observe(viewLifecycleOwner,{
            when(it){
                is ApiResponce.Loading ->{

                }
                is ApiResponce.Error -> {

                }
                is ApiResponce.Success -> {
                    roomDatalist.clear()
                    it.data?.let { it1 ->
                        roomDatalist.addAll(it1)
                        roomAdapter.notifyDataSetChanged()
                    }
                }
            }
        })

    }


    fun changeUi(){

        if (datalist!!.isEmpty()) {
            viewModel.showNoDataView()
        }
        else {
            viewModel.showDataView()
        }
        adapter?.notifyDataSetChanged()
        binding.shimmerLayout.shimmerViewContainer.stopShimmer()
        binding.shimmerLayout.shimmerViewContainer.visibility = View.GONE
        viewModel.loadMoreProgressVisibility.set(false)

    }




    // get the image of the upper slider in the discover screen
    private fun callApiSlider() {
        if (isSliderApiCall) {
            return
        }
        isSliderApiCall = true
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.showAppSlider, JSONObject(), getHeaders(
                activity
            )
        ) { resp ->
            checkStatus(activity, resp)
            isSliderApiCall = false
            parseSliderData(resp)
        }
    }

    var sliderList: ArrayList<SliderModel> = ArrayList()
    fun parseSliderData(resp: String?) {
        try {
            val jsonObject = JSONObject(resp)

            val code = jsonObject.optString("code")
            if (code == "200") {
                sliderList.clear()

                val msg = jsonObject.optJSONArray("msg")
                for (i in 0 until msg.length()) {
                    val `object` = msg.optJSONObject(i)
                    val AppSlider = `object`.optJSONObject("AppSlider")

                    val sliderModel = SliderModel()
                    sliderModel.id = AppSlider.optString("id")
                    sliderModel.image = AppSlider.optString("image")
                    sliderModel.url = AppSlider.optString("url")

                    sliderList.add(sliderModel)
                }

                if(sliderList.isNotEmpty()) {
                    saveSliderList(sliderList)
                }
                else{
                    val json = Functions.getSharedPreference(binding.root.context).getString("showAppSlider", "")
                    if (!TextUtils.isEmpty(json)) {
                        val type = object : TypeToken<ArrayList<SliderModel>?>() {}.type
                        val gson = Gson()
                        sliderList= gson.fromJson(json, type)
                    }
                }

                setSliderAdapter()

            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun setSliderAdapter() {
        binding.pageIndicatorView.setCount(sliderList.size)
        binding.pageIndicatorView.setSelection(0)

        binding.viewPager.setAdapter(SlidingAdapter(
            activity, sliderList
        ) { view, pos, `object` ->
            val slider_url = sliderList[pos].url
            if (slider_url != null && slider_url != "") {
                val intent = Intent(view.context, WebviewActivity::class.java)
                intent.putExtra("url", slider_url)
                intent.putExtra("title", "Link")
                startActivity(intent)
                requireActivity().overridePendingTransition(
                    R.anim.in_from_right,
                    R.anim.out_to_left
                )
            }
        })

        binding.pageIndicatorView.setViewPager(binding.viewPager)
    }

    fun saveSliderList(list: ArrayList<SliderModel>?) {
        val gson = Gson()
        val json = gson.toJson(list)
        Functions.getSharedPreference(getContext()).edit().putString("showAppSlider", json)
            .apply()
    }


    fun setTabs(){
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.all_lives)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.shop)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.spaces)))
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                Functions.printLog(Constants.tag, "tab Position:" + tab.position)
                when (tab.position) {

                    0 -> {
                        binding.streamingRecylerView.adapter=liveUserAdapter
                    }

                    1 -> {
                        binding.streamingRecylerView.adapter=shopDiscoverAdapter

                    }

                    2 -> {
                        binding.streamingRecylerView.adapter=roomAdapter

                    }

                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
    }




    var dataList: ArrayList<LiveUserModel> = ArrayList()
    var selectLiveModel: LiveUserModel? = null
    var position: Int = 0
    var takePermissionUtils: PermissionUtils? = null
    var liveUserAdapter: StreamingDiscoverAdapter? = null
    fun getStreamingUser(){

        liveUserAdapter = StreamingDiscoverAdapter(requireContext(), dataList) { view, pos, `object` ->
            if (!(dataList.isEmpty())) {
                position = pos
                val itemUpdate = dataList[pos]
                selectLiveModel = itemUpdate
                if (checkLoginUser(requireActivity())) {
                    if (takePermissionUtils!!.isCameraRecordingPermissionGranted) {
                        joinStream()
                    } else {
                        takePermissionUtils!!.showCameraRecordingPermissionDailog(getString(R.string.we_need_camera_and_recording_permission_for_live_streaming))
                    }
                }
            }
        }
        binding.streamingRecylerView.setAdapter(liveUserAdapter)

        val userList= StreamingFirebaseManager.getInstance(requireContext())?.userList
        userList?.let { dataList.addAll(it) }

        userList?.onAdd={it,index->
            dataList.add(it)
            liveUserAdapter?.notifyItemInserted((dataList.size-1))
        }
        userList?.onRemove={it,index->
            dataList.removeAt(index)
            liveUserAdapter?.notifyItemRemoved(index)
        }

        userList?.onUpdate={old,new,index->
            dataList.set(index,new)
        }


    }

    private fun joinStream() {
        val intent = Intent()
        intent.putParcelableArrayListExtra("dataList", dataList)
        intent.putExtra("position", position)
        intent.setClass(requireActivity(), MultiViewLiveActivity::class.java)
        startActivity(intent)
    }



    var shopsDataList: ArrayList<UserModel> = ArrayList()
    var shopDiscoverAdapter: ShopDiscoverAdapter? = null
    fun setUpShopsAdapter(){
        shopDiscoverAdapter =ShopDiscoverAdapter(requireContext(), shopsDataList) { view, pos, `object` ->
            val item = `object` as UserModel
            if (checkProfileOpenValidation(item.id)) {
                val intent = Intent(activity, ProfileActivity::class.java)
                intent.putExtra("user_id", item.id)
                intent.putExtra("user_name", item.username)
                intent.putExtra("user_pic", item.getProfilePic())
                intent.putExtra("userModel", item)
                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
            }
        }
    }


    lateinit var roomAdapter: MainHomeAdapter
    var roomDatalist=ArrayList<RoomModel>()
    private fun setupRoomAdapter() {
        roomAdapter = MainHomeAdapter(
            requireContext(),
            roomDatalist,
            AdapterClickListener { view: View, pos: Int, `object`: Any? ->
                val itemUpdate = roomDatalist[pos] as RoomModel
                when (view.id) {
                    R.id.tabView -> if (takePermissionUtils!!.isStorageRecordingPermissionGranted) {

                        val mainMenuActivity = activity as MainMenuActivity?
                        if (mainMenuActivity != null) {
                            val roomManager = mainMenuActivity.roomManager!!
                            roomManager.checkMyRoomJoinStatus("join", itemUpdate?.id!!)
                        }
                    } else {
                        takePermissionUtils!!.showStorageRecordingPermissionDailog(
                            binding.root.context
                                .getString(R.string.we_need_voice_and_read_write_storage_permission)
                        )
                    }

                }
            })
    }


    var roomFirebaseListener: RoomFirebaseListener? = null
    var roomManager: RoomManager? = null
    var firebaseRoomManager: RoomFirebaseManager? = null

    private fun startRoomListener() {
        val mainMenuActivity = activity as MainMenuActivity?

        if (mainMenuActivity!!.roomManager == null) {
            mainMenuActivity!!.setRoomListerner()
        }

        roomManager = mainMenuActivity!!.roomManager
        firebaseRoomManager = mainMenuActivity!!.roomFirebaseManager

        roomFirebaseListener = object : RoomFirebaseListener {
            override fun createRoom(bundle: Bundle?) {
            }

            override fun JoinedRoom(bundle: Bundle?) {
                viewModel.showRoom()
            }

            override fun onRoomLeave(bundle: Bundle?) {
                viewModel.showRoom()
            }

            override fun onRoomDelete(bundle: Bundle?) {
                viewModel.showRoom()
            }

            override fun onRoomUpdate(bundle: Bundle?) {
            }

            override fun onRoomUsersUpdate(bundle: Bundle?) {
            }

            override fun onMyUserUpdate(bundle: Bundle?) {
            }

            override fun onSpeakInvitationReceived(bundle: Bundle?) {
            }

            override fun onWaveUserUpdate(bundle: Bundle?) {
            }
        }
        if (firebaseRoomManager != null) {
            firebaseRoomManager?.listerner2 = roomFirebaseListener
        }
    }


    private val mPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(), object :
            ActivityResultCallback<Map<String, Boolean>> {
            @RequiresApi(api = Build.VERSION_CODES.M)
            override fun onActivityResult(result: Map<String, Boolean>) {
                var allPermissionClear = true
                val blockPermissionCheck: MutableList<String> = ArrayList()
                for (key in result.keys) {
                    if (!result[key]!!) {
                        allPermissionClear = false
                        blockPermissionCheck.add(
                            getPermissionStatus(
                                requireActivity(), key
                            )
                        )
                    }
                }
                if (blockPermissionCheck.contains("blocked")) {
                    showPermissionSetting(requireActivity(),
                        getString(R.string.we_need_camera_and_recording_permission_for_live_streaming)
                    )
                } else if (allPermissionClear) {
                    joinStream()
                }
            }
        })



    fun saveHashTagArrayList(list: ArrayList<DiscoverModel>?) {
        val gson = Gson()
        val json = gson.toJson(list)
        Functions.getSharedPreference(getContext()).edit().putString("showDiscoverySections", json)
            .apply()
    }

    // When you click on any Video a new activity is open which will play the Clicked video
    private fun openWatchVideo(postion: Int, data_list: ArrayList<HomeModel?>, hashtag: String) {
        if (data_list.size > 5) data_list.removeAt(data_list.size - 1)
        val intent = Intent(binding.root.context, WatchVideosActivity::class.java)

        val args = Bundle()
        args.putSerializable("arraylist", data_list)
        DataHolder.instance?.data = args

        intent.putExtra("position", postion)
        intent.putExtra("pageCount", 0)
        intent.putExtra("hashtag", hashtag)
        intent.putExtra(
            "userId",
            Functions.getSharedPreference(binding.root.context).getString(com.coheser.app.simpleclasses.Variables.U_ID, "")
        )
        intent.putExtra("whereFrom", "discoverTagedVideo")
        try {
            resultCallback.launch(intent)
        }catch (e:Exception){
            startActivity(intent)
        }
    }

    var resultCallback = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
                try {
                    val itemUpdate = datalist!![parentPostion]
                    val datalist1 = itemUpdate.arrayList
                    if (datalist1.size >= 5) datalist1.add(null)
                    itemUpdate.arrayList = datalist1
                    datalist!![parentPostion] = itemUpdate
                    adapter?.notifyItemChanged(parentPostion)
                    viewModel.pageCount.set(data.getIntExtra("pageCount", 0))
                } catch (e: Exception) {
                    Functions.printLog(com.coheser.app.Constants.tag, "Exception: $e")
                }
            }
        }
    }

    fun openSearch() {
        val intent = Intent(binding.root.context, com.coheser.app.activitesfragments.search.SearchMainActivity::class.java)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun openHashtag(tag: String) {
        val intent = Intent(binding.root.context, TagedVideosActivity::class.java)
        intent.putExtra("tag", tag)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.search_layout -> openSearch()
            R.id.search_edit -> openSearch()
            R.id.back_btn -> requireActivity().onBackPressed()

            R.id.shopBtn ->openShop()

            else -> return
        }
    }

    private fun openShop() {
        val shopF = com.coheser.app.activitesfragments.shoping.ShopF.newInstance()
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(
            R.anim.in_from_right,
            R.anim.out_to_left,
            R.anim.in_from_left,
            R.anim.out_to_right
        )
        ft.replace(R.id.mainMenuFragment, shopF, "ShopF").addToBackStack("ShopF").commit()
    }

    companion object {
        fun newInstance(): DiscoverFragment {
            val fragment = DiscoverFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }


}
