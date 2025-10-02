package com.coheser.app.activitesfragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.chat.ChatActivity
import com.coheser.app.activitesfragments.livestreaming.StreamingConstants
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.activitesfragments.profile.usersstory.ViewStoryA
import com.coheser.app.adapters.NotificationAdapter
import com.coheser.app.adapters.StoryAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityNotificationBinding
import com.coheser.app.mainmenu.MainMenuActivity.Companion.tabLayout
import com.coheser.app.models.NotificationModel
import com.coheser.app.models.StoryModel
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.PermissionUtils
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.NotificationViewModel
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel // Changed import


class NotificationFragment : Fragment(), View.OnClickListener {
    lateinit var binding: ActivityNotificationBinding
    var takePermissionUtils: PermissionUtils? = null
    var adapter: NotificationAdapter? = null
    var datalist: ArrayList<NotificationModel?>? = null
    var mReceiver: NotificationBroadCast? = null
    var selectedNotificationModel: NotificationModel? = null
    var streamingType = ""
    var selectedPosition = 0
    var rootRef: DatabaseReference? = null
    var linearLayoutManager: LinearLayoutManager? = null

    private val viewModel: NotificationViewModel by viewModel() // Changed ViewModel initialization


    private val mPermissionRejectResult = registerForActivityResult(
        RequestMultiplePermissions(),  { result ->
            var allPermissionClear = true
            val blockPermissionCheck: MutableList<String> = ArrayList()
            for (key in result.keys) {
                if (!result[key]!!) {
                    allPermissionClear = false
                    blockPermissionCheck.add(Functions.getPermissionStatus(requireActivity(), key))
                }
            }
            if (blockPermissionCheck.contains("blocked")) {
                Functions.showPermissionSetting(
                    requireActivity(),
                    getString(R.string.we_need_camera_and_recording_permission_for_live_streaming)
                )
            } else if (allPermissionClear) {
                inviteRequestStatusUpdate(
                    selectedNotificationModel!!.id,
                    selectedNotificationModel!!.live_streaming_id,
                    "2",
                    selectedPosition
                )
            }
        })

    private val mPermissionAcceptResult = registerForActivityResult(
        RequestMultiplePermissions(),  { result ->
            var allPermissionClear = true
            val blockPermissionCheck: MutableList<String> = ArrayList()
            for (key in result.keys) {
                if (!result[key]!!) {
                    allPermissionClear = false
                    blockPermissionCheck.add(Functions.getPermissionStatus(requireActivity(), key))
                }
            }
            if (blockPermissionCheck.contains("blocked")) {
                Functions.showPermissionSetting(
                    requireActivity(),
                    getString(R.string.we_need_camera_and_recording_permission_for_live_streaming)
                )
            } else if (allPermissionClear) {
                inviteRequestStatusUpdate(
                    selectedNotificationModel!!.id,
                    selectedNotificationModel!!.live_streaming_id,
                    "1",
                    selectedPosition
                )
            }
        })



    companion object {
        fun newInstance(): NotificationFragment {
            val fragment = NotificationFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = ActivityNotificationBinding.inflate(inflater, container, false)

     
        binding.viewModel=viewModel
        binding.lifecycleOwner = viewLifecycleOwner // Changed to viewLifecycleOwner

        rootRef = FirebaseDatabase.getInstance().reference
        datalist = ArrayList()
        linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recylerview.layoutManager = linearLayoutManager
        binding.recylerview.setHasFixedSize(true)
        adapter = NotificationAdapter(
            requireContext(),
            datalist
        ) { view, postion, item ->
            selectedPosition = postion
            selectedNotificationModel = datalist!![selectedPosition]
            when (view.id) {
                R.id.watch_btn -> if (item.type == "live") {
                    openLivedUser()
                } else if (item.type.equals("video_comment", ignoreCase = true) || item.type.equals(
                        "video_like",
                        ignoreCase = true
                    )
                ) {
                    openWatchVideoWithComment(item)
                } else {
                    openWatchVideo(item)
                }

                R.id.btnAcceptRequest -> {
                    streamingType = item.type
                    if (item.type == "single" || item.type == "multiple") {
                        takePermissionUtils =
                           PermissionUtils(
                                requireActivity(),
                                mPermissionAcceptResult
                            )
                        if (takePermissionUtils!!.isCameraRecordingPermissionGranted) {
                            inviteRequestStatusUpdate(item.id, item.live_streaming_id, "1", postion)
                        } else {
                            takePermissionUtils!!.showCameraRecordingPermissionDailog(
                                getString(
                                    R.string.we_need_camera_and_recording_permission_for_live_streaming
                                )
                            )
                        }
                    }
                }

                R.id.btnDeleteRequest -> {
                    streamingType = item.type
                    if (item.type == "single" || item.type == "multiple") {
                        takePermissionUtils =
                            PermissionUtils(
                                requireActivity(),
                                mPermissionRejectResult
                            )
                        if (takePermissionUtils!!.isCameraRecordingPermissionGranted) {
                            inviteRequestStatusUpdate(item.id, item.live_streaming_id, "2", postion)
                        } else {
                            takePermissionUtils!!.showCameraRecordingPermissionDailog(
                                getString(R.string.we_need_camera_and_recording_permission_for_live_streaming)
                            )
                        }
                    }
                }

                R.id.follow_btn -> {
                    if (selectedNotificationModel?.senderModel?.button.equals("Friends")) {
                        val intent = Intent(
                            requireActivity(),
                            ChatActivity::class.java
                        )
                        intent.putExtra("user_id", selectedNotificationModel?.senderModel?.id)
                        intent.putExtra(
                            "user_name",
                            selectedNotificationModel?.senderModel?.username
                        )
                        intent.putExtra(
                            "user_pic",
                            selectedNotificationModel?.senderModel?.getProfilePic()
                        )
                        startActivity(intent)
                        requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
                    } else {
                        if (Functions.checkLoginUser(requireActivity())) {
                            selectedNotificationModel?.senderModel?.id?.let {
                                viewModel.followUser(
                                    it
                                )
                            }

                        }
                    }

                }

                else -> {
                    streamingType = item.type
                    if (item.type == "live") {
                        openLivedUser()
                    } else if (item.type == "single" || item.type == "multiple") {
                        openSingleStream()
                    } else {
                        openProfile(item)
                    }
                }
            }
        }
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
                Functions.printLog("resp", "" + scrollOutitems)
                if (userScrolled && scrollOutitems == datalist!!.size - 1) {
                    userScrolled = false
                    if (viewModel.loadMoreLoaderVisibility.get()== false && !viewModel.ispostFinsh) {
                        viewModel.loadMoreLoaderVisibility.set(true)
                        viewModel.pageCount.set(viewModel.pageCount.get()+1)
                        viewModel.showAllNotifications()

                    }
                }
            }
        })
        binding.swiperefresh.setOnRefreshListener {
            if (datalist!!.size < 1) {
                binding.dataContainer.visibility = View.GONE
                binding.shimmerMainLayout.shimmerViewContainer.visibility = View.VISIBLE
                binding.shimmerMainLayout.shimmerViewContainer.startShimmer()
            }
            viewModel.pageCount.set(0)
            viewModel.showAllNotifications()

        }

        mReceiver = NotificationBroadCast()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().registerReceiver(mReceiver, IntentFilter("NotificationHit"), Context.RECEIVER_NOT_EXPORTED)
        }
        else{
            requireContext().registerReceiver(mReceiver, IntentFilter("NotificationHit"))
        }


        setupStoryRecyclerview()
        setObserveAble() // Call this after view is created and viewLifecycleOwner is available
        viewModel.showAllStories()

        return binding.root
    }

    // It's good practice to unregister receiver in onDestroyView
    override fun onDestroyView() {
        super.onDestroyView()
        if (mReceiver != null) {
            try {
                requireContext().unregisterReceiver(mReceiver)
            } catch (e: IllegalArgumentException) {
                Log.w(Constants.tag, "BroadcastReceiver was not registered or already unregistered.", e)
            }
            mReceiver = null
        }
    }

    fun setObserveAble() {

        viewModel.listLiveData.observe(viewLifecycleOwner, { // Changed to viewLifecycleOwner
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let {
                        if(viewModel.pageCount.get()==0) {
                            datalist?.clear()
                        }
                        datalist?.addAll(it)
                    }
                    changeUi()
                }

                is ApiResponce.Error -> {
                    changeUi()
                }
                is ApiResponce.Loading -> {
                }

            }
        })

        viewModel.listStoryData.observe(viewLifecycleOwner, { // Changed to viewLifecycleOwner
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let {
                        if(viewModel.pageCount.get()==0) {
                            storyDataList?.clear()
                        }
                        storyDataList?.addAll(it)
                    }
                    storyAdapter.notifyDataSetChanged()

                }

                is ApiResponce.Error -> {
                    storyAdapter.notifyDataSetChanged()

                }
                is ApiResponce.Loading -> {
                }

            }
        })


        viewModel.followLiveData.observe(viewLifecycleOwner, { // Changed to viewLifecycleOwner
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let {
                        selectedNotificationModel?.senderModel?.button = it.button
                        datalist!![selectedPosition] = selectedNotificationModel
                        adapter!!.notifyDataSetChanged()
                    }
                }

                is ApiResponce.Error -> {
                }
                is ApiResponce.Loading -> {
                }

            }
        })


        viewModel.userNotificationLiveData.observe(viewLifecycleOwner,{ // Changed to viewLifecycleOwner
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        Functions.printLog(Constants.tag,"Count:"+it)
                        showNotificationCount(it)
                    }

                }

                else -> {}
            }
        })


    }

    fun changeUi(){
        if (datalist!!.isEmpty()) {
           viewModel.showNoData()
        } else {
            viewModel.hideNoData()
        }
        adapter?.notifyDataSetChanged()
        binding.shimmerMainLayout.shimmerViewContainer.stopShimmer()
        binding.shimmerMainLayout.shimmerViewContainer.visibility = View.GONE
        binding.dataContainer.visibility = View.VISIBLE
        binding.swiperefresh.isRefreshing = false
        viewModel.loadMoreLoaderVisibility.set(false)

    }


     var storyDataList: ArrayList<StoryModel> = ArrayList()
    lateinit var storyAdapter: StoryAdapter
    var selectedStoryItem: StoryModel? = null

    private fun setupStoryRecyclerview() {
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.storyRecyclerview.setLayoutManager(layoutManager)
        storyAdapter = StoryAdapter(
            storyDataList
        ) { view, pos, `object` ->
            selectedStoryItem = storyDataList.get(pos)
            if (view.id == R.id.tabUserPic) {

                val myIntent = Intent(requireActivity(), ViewStoryA::class.java)
                myIntent.putParcelableArrayListExtra("storyList", storyDataList) //Optional parameters
                myIntent.putExtra("position", pos) //Optional parameters
                startActivity(myIntent)
            }
        }
        binding.storyRecyclerview.setAdapter(storyAdapter)
    }




    private fun openSingleStream() {
        goLive()
    }

    private fun inviteRequestStatusUpdate(
        id: String,
        streamingId: String,
        status: String,
        postion: Int
    ) {
        val parameters = JSONObject()
        try {
            parameters.put("id", id)
            parameters.put("status", status)
            parameters.put("user_id", Functions.getSharedPreference(requireActivity()).getString(Variables.U_ID, ""))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Functions.showLoader(requireActivity(), false, false)
        VolleyRequest.JsonPostRequest(
            requireActivity(),
            com.coheser.app.apiclasses.ApiLinks.acceptStreamingInvite,
            parameters,
            Functions.getHeaders(requireActivity())
        ) { resp ->
            Functions.checkStatus(requireActivity(), resp)
            Functions.cancelLoader()
            try {
                val jsonObj = JSONObject(resp)
                if (jsonObj.optString("code").equals("200", ignoreCase = true)) {
                    val itemUpdate = datalist!![postion]
                    itemUpdate!!.status = status
                    datalist!![postion] = itemUpdate
                    adapter!!.notifyDataSetChanged()
                    if (status.equals("1", ignoreCase = true)) {
                        acceptStreamInvitation(streamingId)
                    } else {
                        deleteStreamInvitation(streamingId)
                    }
                }
            } catch (e: Exception) {
                Log.d(com.coheser.app.Constants.tag, "Exception : $e")
            }
        }
    }

    private fun deleteStreamInvitation(streamingId: String) {
        rootRef!!.child(StreamingConstants.liveStreamingUsers)
            .child(streamingId)
            .child("StreamInvite")
            .child(Functions.getSharedPreference(requireActivity()).getString(Variables.U_ID, "")!!)
            .removeValue()
    }

    private fun acceptStreamInvitation(streamingId: String) {
        val itemUpdate = com.coheser.app.models.StreamInviteModel()
        itemUpdate.setAccept(true)
        rootRef!!.child(StreamingConstants.liveStreamingUsers)
            .child(streamingId)
            .child("StreamInvite")
            .child(Functions.getSharedPreference(requireActivity()).getString(Variables.U_ID, "")!!)
            .setValue(itemUpdate).addOnCompleteListener { task ->
                if (task.isComplete) {
                    requireActivity().runOnUiThread { goLive() }
                }
            }
    }

    private fun goLive() {
        rootRef!!.child(StreamingConstants.liveStreamingUsers)
            .child(selectedNotificationModel!!.live_streaming_id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val selectLiveModel = snapshot.getValue(
                            LiveUserModel::class.java
                        )
                        requireActivity().runOnUiThread { joinStream(selectLiveModel) }

                    } else {
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireActivity(),
                                selectedNotificationModel?.senderModel?.username + " " + getString(R.string.is_offline_now),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }


    private fun joinStream(selectLiveModel: LiveUserModel?) {
            val dataList = ArrayList<LiveUserModel?>()
            dataList.add(selectLiveModel)
            val intent = Intent()
            intent.putExtra("user_id", selectLiveModel?.getUserId())
            intent.putExtra("user_name", selectLiveModel?.getUserName())
            intent.putExtra("user_picture", selectLiveModel?.getUserPicture())
            intent.putExtra("user_role", io.agora.rtc2.Constants.CLIENT_ROLE_AUDIENCE)
            intent.putExtra("onlineType", "multicast")
            intent.putExtra("description", selectLiveModel?.getDescription())
            intent.putExtra("secureCode", "")
            intent.putExtra("dataList", dataList)
            intent.putExtra("position", 0)
            intent.setClass(requireActivity(), com.coheser.app.activitesfragments.livestreaming.activities.MultiViewLiveActivity::class.java)
            startActivity(intent)

    }


    var isMenuvisible=false
    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        isMenuvisible=menuVisible
        // It's safer to check if view is created before accessing viewModel or UI components
        if (!isAdded || view == null) {
            return
        }
        if (Variables.reloadMyNotification || isMenuvisible) {
            if (viewModel.pageCount.get() == 0) { // Accessing viewModel here
                    Variables.reloadMyNotification = false
                    if (datalist!!.size < 1) {
                        binding.dataContainer.visibility = View.GONE
                        binding.shimmerMainLayout.shimmerViewContainer.visibility = View.VISIBLE
                        binding.shimmerMainLayout.shimmerViewContainer.startShimmer()
                    }
                    viewModel.pageCount.set(0)
                    viewModel.showAllNotifications()
                }
        }
    }


    override fun onClick(v: View) {
        when (v.id) {

        }
    }

    // open the broad cast live user streaming on notification receive
    private fun openLivedUser() {
        val intent = Intent(requireActivity(), com.coheser.app.activitesfragments.livestreaming.activities.LiveUsersActivity::class.java)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openWatchVideo(item: NotificationModel) {
        val intent = Intent(requireActivity(), WatchVideosActivity::class.java)
        intent.putExtra("video_id", item.video_id)
        intent.putExtra("position", 0)
        intent.putExtra("pageCount", 0)
        intent.putExtra(
            "userId",
            Functions.getSharedPreference(requireActivity()).getString(Variables.U_ID, "")
        )
        intent.putExtra("whereFrom", "IdVideo")
        startActivity(intent)
    }

    private fun openWatchVideoWithComment(item: NotificationModel) {
        val intent = Intent(requireActivity(), WatchVideosActivity::class.java)
        intent.putExtra("video_id", item.video_id)
        intent.putExtra("position", 0)
        intent.putExtra("pageCount", 0)
        intent.putExtra(
            "userId",
            Functions.getSharedPreference(requireActivity()).getString(Variables.U_ID, "")
        )
        intent.putExtra("whereFrom", "IdVideo")
        if (item.type == "video_comment") {
            intent.putExtra("video_comment", true)
        }
        startActivity(intent)
    }

    // open the profile of the user which notification we have receive
    fun openProfile(item: NotificationModel) {
        if (Functions.checkProfileOpenValidation(item?.senderModel?.id)) {
            val intent = Intent(requireActivity(), ProfileActivity::class.java)
            intent.putExtra("user_id", item?.senderModel?.id)
            intent.putExtra("user_name", item?.senderModel?.username)
            intent.putExtra("user_pic", item?.senderModel?.getProfilePic())
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        }
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

    // onDestroy is called after onDestroyView.
    // Unregistration of broadcast receiver moved to onDestroyView.
    override fun onDestroy() {
        super.onDestroy()
        // If mReceiver was not nulled out in onDestroyView due to some conditional logic error (it shouldn't be),
        // this would be a last resort, but ideally, it's handled in onDestroyView.
        // if (mReceiver != null) {
        //     requireContext().unregisterReceiver(mReceiver)
        //     mReceiver = null
        // }
    }

    inner class NotificationBroadCast : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Add safety check: Fragment might receive broadcast even when not in a good state.
            if (!isAdded || view == null) {
                return
            }

            if(isMenuvisible) { // isMenuvisible check implies UI context
                viewModel.pageCount.set(0)
                viewModel.showAllNotifications()
            }
            else {
                viewModel.getUnReadNotification()
            }
        }
    }

}
