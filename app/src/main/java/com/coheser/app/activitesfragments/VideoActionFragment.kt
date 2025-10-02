package com.coheser.app.activitesfragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.SendDirectMsg
import com.coheser.app.activitesfragments.profile.analytics.VideoAnalytics
import com.coheser.app.adapters.FollowingShareAdapter
import com.coheser.app.adapters.ProfileSharingAdapter
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentVideoActionBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.HomeModel
import com.coheser.app.models.ShareAppModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.PermissionUtils
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.VideoActionsViewModel
import com.volley.plus.VPackages.VolleyRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 */
class VideoActionFragment : BottomSheetDialogFragment, View.OnClickListener {
    lateinit var binding: FragmentVideoActionBinding
    lateinit var myContext: Context
    lateinit var fragmentCallback: FragmentCallBack
    var videoId: String? = null
    var userId: String? = null
    var userName: String? = null
    var userPic: String? = null
    var fullName: String? = null
    var item: HomeModel? = null
    var senderId: String? = ""
    var receiverId = ""
    var selectedUserList = ArrayList<UserModel>()
    var takePermissionUtils: PermissionUtils? = null
    var followingList: ArrayList<UserModel>? = null
    var followingShareAdapter: FollowingShareAdapter? = null
    var adapter: ProfileSharingAdapter? = null

    private val viewModel: VideoActionsViewModel by viewModel()

    var resultCallback = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
                shareVideoAction()
                dismiss()
            }
        }
    }
    private val mPermissionResult = registerForActivityResult(
        RequestMultiplePermissions(),  { result ->
            var allPermissionClear = true
            val blockPermissionCheck: MutableList<String> = ArrayList()
            for (key in result.keys) {
                if (!result[key]!!) {
                    allPermissionClear = false
                    blockPermissionCheck.add(
                        Functions.getPermissionStatus(
                            activity, key
                        )
                    )
                }
            }
            if (blockPermissionCheck.contains("blocked")) {
                Functions.showPermissionSetting(
                    myContext,
                    getString(R.string.we_need_storage_permission_for_save_video)
                )
            } else if (allPermissionClear) {
                saveVideoAction()
            }
        })
    private val mPermissionStorageCameraRecordingResult = registerForActivityResult(
        RequestMultiplePermissions(),  { result ->
            var allPermissionClear = true
            val blockPermissionCheck: MutableList<String> = ArrayList()
            for (key in result.keys) {
                if (!result[key]!!) {
                    allPermissionClear = false
                    blockPermissionCheck.add(
                        Functions.getPermissionStatus(
                            activity, key
                        )
                    )
                }
            }
            if (blockPermissionCheck.contains("blocked")) {
                Functions.showPermissionSetting(
                    myContext,
                    myContext!!.getString(R.string.we_need_storage_camera_recording_permission_for_make_new_duet_video)
                )
            } else if (allPermissionClear) {
                openDuetAction()
            }
        })

    constructor()

    @SuppressLint("ValidFragment")
    constructor(id: String?, fragmentCallback: FragmentCallBack?) {
        videoId = id
        this.fragmentCallback = fragmentCallback!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoActionBinding.inflate(inflater, container, false)
        myContext = requireContext()
        senderId = Functions.getSharedPreference(myContext).getString(Variables.U_ID, "")
        val bundle = arguments
        if (bundle != null) {
            item = bundle.getParcelable("data")
            videoId = bundle.getString("videoId")
            userId = bundle.getString("userId")
            userName = bundle.getString("userName")
            userPic = bundle.getString("userPic")
            fullName = bundle.getString("fullName")
        }
        binding.deleteLayout.setOnClickListener(this)
        binding.editlayout.setOnClickListener(this)
        binding.privacySettingLayout.setOnClickListener(this)
        binding.repostLayout.setOnClickListener(this)
        binding.analyticLayout.setOnClickListener(this)

        if (item!!.pin == "1") {

            binding.ivPinned.setImageDrawable(ContextCompat.getDrawable(myContext!!, R.drawable.ic_pinned_selected))
            binding.tvPinned.text = myContext!!.getString(R.string.unpin)

        }
        else {

            binding.ivPinned.setImageDrawable(ContextCompat.getDrawable(myContext!!, R.drawable.ic_pinned_unselected))
            binding.tvPinned.text = myContext!!.getString(R.string.pin)

        }
        if (userId != null && userId == Functions.getSharedPreference(myContext)
                .getString(Variables.U_ID, "")
        ) {
            binding.deleteLayout.visibility = View.VISIBLE
            binding.privacySettingLayout.visibility = View.VISIBLE
            binding.pinnedLayout.visibility = View.VISIBLE
            binding.analyticLayout.visibility = View.VISIBLE
            binding.promotionLayout.visibility = View.VISIBLE
            binding.editlayout.visibility = View.VISIBLE

        }

        else {
            binding.editlayout.visibility = View.GONE
            binding.deleteLayout.visibility = View.GONE
            binding.privacySettingLayout.visibility = View.GONE
            binding.pinnedLayout.visibility = View.GONE
            binding.analyticLayout.visibility = View.GONE
            binding.promotionLayout.visibility = View.GONE
        }
        if (isShowVideoDownloadPrivacy(item)) {
            binding.saveVideoLayout.visibility = View.VISIBLE
            binding.saveVideoLayout.setOnClickListener(this)
        }
        else binding.saveVideoLayout.visibility = View.GONE


        if (Constants.IS_DEMO_APP) {
            binding.progressBar.visibility = View.GONE
            binding.copyLayout.setVisibility(View.GONE)
        } else {
            sharedApp()
        }



        if (item!!.repost == "1") {
            binding.ivRepost.setImageDrawable(
                ContextCompat.getDrawable(
                    myContext!!, R.drawable.ic_repost_done
                )
            )
        }
        else {
            binding.ivRepost.setImageDrawable(
                ContextCompat.getDrawable(
                    myContext!!, R.drawable.ic_repost
                )
            )
        }
        if (item!!.allow_duet != null && item!!.allow_duet.equals("1", ignoreCase = true)
            && Functions.isShowContentPrivacy(
                myContext,
                item!!.apply_privacy_model!!.duet,
                item!!.userModel?.button.equals("friends", ignoreCase = true)
            )
        ) {
            binding.duetLayout.visibility = View.VISIBLE
            binding.duetLayout.setOnClickListener(this)
        } else {
            binding.duetLayout.visibility = View.GONE
        }

        binding.copyLayout.setOnClickListener(this)
        binding.pinnedLayout.setOnClickListener(this)
        binding.promotionLayout.setOnClickListener(this)
        binding.notIntrestedLayout.setOnClickListener(this)
        binding.reportLayout.setOnClickListener(this)
        if (userId != null && userId == Functions.getSharedPreference(myContext)
                .getString(Variables.U_ID, "")
        ) {
            binding.notIntrestedLayout.visibility = View.GONE
            binding.reportLayout.visibility = View.GONE
        }
        binding.bottomBtn.setOnClickListener(this)


        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObserveAble()

        if (Functions.getSharedPreference(myContext).getBoolean(Variables.IS_LOGIN, false)) {
            setFollowingAdapter()
            viewModel.getFollowingList()
        }
    }

    fun setObserveAble() {

        viewModel.listLiveData.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponce.Success -> {

                    followingList?.clear()
                    it.data?.let { it1 -> followingList!!.addAll(it1) }
                    binding.sendToTxt.visibility = View.VISIBLE
                    binding.recylerviewFollowing.visibility = View.VISIBLE
                }

                is ApiResponce.Error -> {

                }

                is ApiResponce.Loading -> {

                }

            }
        })


    }
        private fun isShowVideoDownloadPrivacy(home_item: HomeModel?): Boolean {
        return home_item!!.apply_privacy_model == null || !home_item.apply_privacy_model!!.videosDownload.toString().equals(
            "0",
            ignoreCase = true
        )
    }

    fun setFollowingAdapter() {
        followingList = ArrayList()
        val layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false)
        binding.recylerviewFollowing.layoutManager = layoutManager
        binding.recylerviewFollowing.setHasFixedSize(false)
        followingShareAdapter = FollowingShareAdapter(
            myContext,
            followingList!!
        ) { view, pos, `object` -> clickedUsers(pos) }
        binding.recylerviewFollowing.adapter = followingShareAdapter
    }

    fun clickedUsers(postion: Int) {
        val itemUpdate = followingList!![postion]
        selectedUserList = ArrayList()
        if (itemUpdate.isSelected) {
            itemUpdate.isSelected = false
            followingList!![postion] = itemUpdate
        } else {
            itemUpdate.isSelected = true
            followingList!![postion] = itemUpdate
        }
        followingShareAdapter!!.notifyDataSetChanged()
        for (i in followingList!!.indices) {
            if (followingList!![i].isSelected) {
                selectedUserList.add(followingList!![i])
            }
        }
        if (selectedUserList.size > 0) {
            binding.bottomBtn.text =
                selectedUserList.size.toString() + " " + myContext!!.getString(R.string.send)
            binding.bottomBtn.background = ContextCompat.getDrawable(myContext!!, R.color.appColor)
            binding.bottomBtn.setTextColor(ContextCompat.getColor(myContext!!, R.color.whiteColor))
        } else {
            binding.bottomBtn.setTextColor(ContextCompat.getColor(myContext!!, R.color.darkgray))
            binding.bottomBtn.background = ContextCompat.getDrawable(myContext!!, R.color.white)
            binding.bottomBtn.text = myContext!!.getString(R.string.cancel_)
        }
    }

    fun sendvideo(followerItem: UserModel) {
        val rootref = FirebaseDatabase.getInstance().reference
        val senderId = Functions.getSharedPreference(myContext).getString(Variables.U_ID, "0")
        val c = Calendar.getInstance().time
        val formattedDate = Variables.df.format(c)
        val dref = rootref.child("chat").child(senderId + "-" + followerItem.id).push()
        val key = dref.key
        val current_user_ref = "chat" + "/" + senderId + "-" + followerItem.id
        val chat_user_ref = "chat" + "/" + followerItem.id + "-" + senderId
        val message_user_map: HashMap<String, String> = HashMap()
        message_user_map["receiver_id"] = ""+followerItem.id
        message_user_map["sender_id"] = ""+senderId
        message_user_map["chat_id"] = ""+key
        message_user_map["text"] = ""
        message_user_map["type"] = "video"
        message_user_map["pic_url"] = ""+item!!.getThum()
        message_user_map["video_id"] = ""+item!!.video_id
        message_user_map["status"] = "0"
        message_user_map["time"] = ""
        message_user_map["sender_name"] = Functions.getSharedPreference(myContext).getString(Variables.U_NAME, "").toString()
        message_user_map["timestamp"] = formattedDate
        val user_map: HashMap<String, Any> = HashMap()
        user_map["$current_user_ref/$key"] = message_user_map
        user_map["$chat_user_ref/$key"] = message_user_map
        rootref.updateChildren(user_map,
            DatabaseReference.CompletionListener { databaseError, databaseReference ->
                val inbox_sender_ref = "Inbox" + "/" + senderId + "/" + followerItem.id
                val inbox_receiver_ref = "Inbox" + "/" + followerItem.id + "/" + senderId
                val sendermap: HashMap<String, Any> = HashMap()
                sendermap["rid"] = ""+senderId
                sendermap["name"] = ""+Functions.getSharedPreference(myContext).getString(Variables.U_NAME, "")
                sendermap["pic"] = ""+Functions.getSharedPreference(myContext).getString(Variables.U_PIC, "")
                sendermap["msg"] = "Send an video..."
                sendermap["status"] = "0"
                sendermap["timestamp"] = -1 * System.currentTimeMillis()
                sendermap["date"] = formattedDate
                val receivermap: HashMap<String, Any> = HashMap()
                receivermap["rid"] = followerItem.id!!
                receivermap["name"] = followerItem.username!!
                receivermap["pic"] = followerItem.getProfilePic()!!
                receivermap["msg"] = "Send an video..."
                receivermap["status"] = "1"
                receivermap["timestamp"] = -1 * System.currentTimeMillis()
                receivermap["date"] = formattedDate
                val both_user_map: HashMap<String, Any> = HashMap()
                both_user_map[inbox_sender_ref] = receivermap
                both_user_map[inbox_receiver_ref] = sendermap
                rootref.updateChildren(both_user_map).addOnCompleteListener {
                    val notimap = JSONObject()
                    try {
                        notimap.put(
                            "title",
                            Functions.getSharedPreference(myContext).getString(Variables.U_NAME, "")
                        )
                        notimap.put("message", "You have a new message")
                        notimap.put("sender_id", senderId)
                        val receiverArray = JSONArray()
                        receiverArray.put(JSONObject().put("receiver_id", receiverId))
                        notimap.put("receivers", receiverArray)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    VolleyRequest.JsonPostRequest(
                        activity, ApiLinks.sendPushNotification, notimap, Functions.getHeaders(
                            activity
                        )
                    ) { resp -> Functions.checkStatus(activity, resp) }
                }
            })
    }

    fun sharedApp() {
            val layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false)
            binding.recylerview.layoutManager = layoutManager
            binding.recylerview.setHasFixedSize(false)
            adapter = ProfileSharingAdapter(myContext, appShareDataList) { view, pos, `object` ->
                val item = `object` as ShareAppModel
                shareProfile(item)
            }
            requireActivity().runOnUiThread {
                binding.recylerview.adapter = adapter
                binding.progressBar.visibility = View.GONE
            }
        }


    fun shareProfile(item: ShareAppModel?) {
        val videoLink = Variables.https + "://" +getString(R.string.domain) + getString(R.string.share_video_endpoint_second) + Functions.getRandomString(5) + videoId + Functions.getRandomString(14)

        if (activity != null && item?.getName()?.equals(getString(R.string.copy_link), ignoreCase = true) == false) {
            activity?.runOnUiThread {
                sendShareVideo(videoId)
                shareVideoAction()
            }
        }

        when {
            item?.getName().equals(getString(R.string.messenge), ignoreCase = true) -> {
                moveToDirectMsg()
            }
            item?.getName().equals(getString(R.string.whatsapp), ignoreCase = true) -> {
                try {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, videoLink)
                        `package` = "com.whatsapp"
                    }
                    startActivity(sendIntent)
                } catch (e: Exception) {
                    Log.d(Constants.tag, "Exception: $e")
                }
            }
            item?.getName().equals(getString(R.string.facebook), ignoreCase = true) -> {
                try {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, videoLink)
                        `package` = "com.facebook.katana"
                    }
                    startActivity(sendIntent)
                } catch (e: Exception) {
                    Log.d(Constants.tag, "Exception: $e")
                }
            }
            item?.getName().equals(getString(R.string.messenger), ignoreCase = true) -> {
                try {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, videoLink)
                        `package` = "com.facebook.orca"
                    }
                    startActivity(sendIntent)
                } catch (e: Exception) {
                    Log.d(Constants.tag, "Exception: $e")
                }
            }
            item?.getName().equals(getString(R.string.sms), ignoreCase = true) -> {
                try {
                    val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                        type = "vnd.android-dir/mms-sms"
                        putExtra("sms_body", videoLink)
                    }
                    startActivity(smsIntent)
                } catch (e: Exception) {
                    Log.d(Constants.tag, "Exception: $e")
                }
            }
            item?.getName().equals(getString(R.string.copy_link), ignoreCase = true) -> {
                try {
                    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied Text", videoLink)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, getString(R.string.link_copy_in_clipboard), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.d(Constants.tag, "Exception: $e")
                }
            }
            item?.getName().equals(getString(R.string.email), ignoreCase = true) -> {
                try {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, videoLink)
                        `package` = "com.google.android.gm"
                    }
                    startActivity(sendIntent)
                } catch (e: Exception) {
                    Log.d(Constants.tag, "Exception: $e")
                }
            }
            item?.getName().equals(getString(R.string.other), ignoreCase = true) -> {
                try {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, videoLink)
                    }
                    startActivity(sendIntent)
                } catch (e: Exception) {
                    Log.d(Constants.tag, "Exception: $e")
                }
            }
        }
    }


    fun sendShareVideo(videoId: String?) {
        val parameters = JSONObject()
        try {
            parameters.put("user_id", getSharedPreference(context).getString(Variables.U_ID, ""))
            parameters.put("video_id", videoId)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.shareVideo, parameters, getHeaders(context)
        ) { resp ->
            checkStatus(activity, resp)
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code != null && code == "200") {
                }
            } catch (e: java.lang.Exception) {
                Log.d(Constants.tag, "Exception: $e")
            }
        }
    }


    private fun moveToDirectMsg() {
        videoShare()
    }

    private fun videoShare() {
        val intent = Intent(myContext, SendDirectMsg::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("userName", userName)
        intent.putExtra("userPic", userPic)
        intent.putExtra("fullName", fullName)
        intent.putExtra("thum", item!!.getThum())
        intent.putExtra("videoId", item!!.video_id)
        intent.putExtra("type", "videoShare")
        try {
            resultCallback.launch(intent)
        }catch (e:Exception){
            startActivity(intent)
        }
        requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private val appShareDataList: ArrayList<ShareAppModel>
        private get() {
            val dataList = ArrayList<ShareAppModel>()
            run {
                val item = ShareAppModel()
                item.name = getString(R.string.messenge)
                item.icon = R.drawable.ic_share_message
                dataList.add(item)
            }
            run {
                if (Functions.appInstalledOrNot(myContext, "com.whatsapp")) {
                    val item = ShareAppModel()
                    item.name = getString(R.string.whatsapp)
                    item.icon = R.drawable.ic_share_whatsapp
                    dataList.add(item)
                }
            }
            run {
                if (Functions.appInstalledOrNot(myContext, "com.facebook.katana")) {
                    val item = ShareAppModel()
                    item.name = getString(R.string.facebook)
                    item.icon = R.drawable.ic_share_facebook
                    dataList.add(item)
                }
            }
            run {
                if (Functions.appInstalledOrNot(myContext, "com.facebook.orca")) {
                    val item = ShareAppModel()
                    item.name = getString(R.string.messenger)
                    item.icon = R.drawable.ic_share_messenger
                    dataList.add(item)
                }
            }
            run {
                val item = ShareAppModel()
                item.name = getString(R.string.sms)
                item.icon = R.drawable.ic_share_sms
                dataList.add(item)
            }
            run {
                val item = ShareAppModel()
                item.name = getString(R.string.copy_link)
                item.icon = R.drawable.ic_share_copy_link
                dataList.add(item)
            }
            run {
                if (Functions.appInstalledOrNot(myContext, "com.whatsapp")) {
                    val item = ShareAppModel()
                    item.name = getString(R.string.email)
                    item.icon = R.drawable.ic_share_email
                    dataList.add(item)
                }
            }
            run {
                val item = ShareAppModel()
                item.name = getString(R.string.other)
                item.icon = R.drawable.ic_share_other
                dataList.add(item)
            }
            return dataList
        }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.save_video_layout -> {
                takePermissionUtils = PermissionUtils(activity, mPermissionResult)
                if (takePermissionUtils!!.isStoragePermissionGranted) {
                    saveVideoAction()
                } else {
                    takePermissionUtils!!.showStoragePermissionDailog(myContext!!.getString(R.string.we_need_storage_permission_for_save_video))
                }
            }

            R.id.duet_layout -> if (Functions.checkLoginUser(activity)) {
                takePermissionUtils =
                    PermissionUtils(activity, mPermissionStorageCameraRecordingResult)
                if (takePermissionUtils!!.isStorageCameraRecordingPermissionGranted) {
                    openDuetAction()
                } else {
                    takePermissionUtils!!.showStorageCameraRecordingPermissionDailog(
                        myContext!!.getString(
                            R.string.we_need_storage_camera_recording_permission_for_make_new_duet_video
                        )
                    )
                }
            }

            R.id.pinned_layout -> {
                val bundle = Bundle()
                bundle.putString("action", "pinned")
                dismiss()
                if (fragmentCallback != null) fragmentCallback!!.onResponce(bundle)
            }



            R.id.promotion_layout -> {
                val bundle = Bundle()
                bundle.putString("action", "promotion")
                dismiss()
                if (fragmentCallback != null) fragmentCallback!!.onResponce(bundle)
            }


         R.id.copy_layout ->{
                var clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                var clip: ClipData? = ClipData.newPlainText("Copied Text", Variables.https + "://" +getString(R.string.domain) + getString(R.string.share_video_endpoint_second) + Functions.getRandomString(5) + videoId + Functions.getRandomString(14))
               clip?.let { clipboard?.setPrimaryClip(it) }
               Toast.makeText(requireContext(), getString(R.string.link_copy_in_clipboard), Toast.LENGTH_SHORT).show()
         }

            R.id.editlayout ->{
                val bundle = Bundle()
                bundle.putString("action", "editVideo")
                dismiss()
                if (fragmentCallback != null) fragmentCallback!!.onResponce(bundle)
            }

            R.id.delete_layout -> {
                val bundle = Bundle()
                bundle.putString("action", "delete")
                dismiss()
                if (fragmentCallback != null) fragmentCallback!!.onResponce(bundle)
            }

            R.id.privacy_setting_layout -> {
                val bundleP = Bundle()
                bundleP.putString("action", "privacy")
                dismiss()
                if (fragmentCallback != null) fragmentCallback!!.onResponce(bundleP)
            }

            R.id.repost_layout -> {
                val bundleR = Bundle()
                bundleR.putString("action", "repost")
                dismiss()
                if (fragmentCallback != null) fragmentCallback!!.onResponce(bundleR)
            }

            R.id.analytic_layout -> {
                val intent = Intent(activity, VideoAnalytics::class.java)
                intent.putExtra("model", item)
                startActivity(intent)
            }

            R.id.not_intrested_layout -> {
                val not_interested_bundle = Bundle()
                not_interested_bundle.putString("action", "not_intrested")
                dismiss()
                if (fragmentCallback != null) fragmentCallback!!.onResponce(not_interested_bundle)
            }

            R.id.report_layout -> {
                val report_bundle = Bundle()
                report_bundle.putString("action", "report")
                dismiss()
                if (fragmentCallback != null) fragmentCallback!!.onResponce(report_bundle)
            }

            R.id.bottom_btn -> if (selectedUserList.size > 0) {
                for (item in selectedUserList) {
                    requireActivity().runOnUiThread { sendvideo(item) }
                }
                Functions.showLoader(activity, false, false)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1500)
                    Functions.cancelLoader()
                    item!!.video_id?.let { viewModel.shareVideo(it) }
                    shareVideoAction()
                    Toast.makeText(
                        myContext,
                        myContext!!.getString(R.string.profile_share_successfully_completed),
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
            } else {
                dismiss()
            }
        }
    }

    private fun shareVideoAction() {
        val duet_bundle = Bundle()
        duet_bundle.putString("action", "videoShare")
        dismiss()
        if (fragmentCallback != null) fragmentCallback!!.onResponce(duet_bundle)
    }

    private fun openDuetAction() {
        val duet_bundle = Bundle()
        duet_bundle.putString("action", "duet")
        dismiss()
        if (fragmentCallback != null) fragmentCallback!!.onResponce(duet_bundle)
    }

    private fun saveVideoAction() {
        val bundle = Bundle()
        bundle.putString("action", "save")
        dismiss()
        if (fragmentCallback != null) fragmentCallback!!.onResponce(bundle)
    }

    override fun onDetach() {
        super.onDetach()
        mPermissionResult.unregister()
        mPermissionStorageCameraRecordingResult.unregister()
    }
}
