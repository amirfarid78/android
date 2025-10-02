package com.coheser.app.activitesfragments.spaces

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.FollowsMainTabActivity
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.activitesfragments.profile.ReportTypeActivity
import com.coheser.app.activitesfragments.profile.ShareUserProfileFragment
import com.coheser.app.activitesfragments.spaces.adapters.ProfileSuggestionAdapter
import com.coheser.app.activitesfragments.spaces.models.HomeUserModel
import com.coheser.app.activitesfragments.spaces.models.UserSuggestionModel
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentOtherUserProfileBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.InviteForSpeakModel
import com.coheser.app.models.PrivacySettingModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.ApiRepository.callApiForFollowUnFollow
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.DateOprations.changeDateFormat
import com.coheser.app.simpleclasses.Dialogs.showError
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkLoginUser
import com.coheser.app.simpleclasses.Functions.checkProfileOpenValidation
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.isShowContentPrivacy
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.Functions.showUsername
import com.coheser.app.simpleclasses.Variables
import com.realpacific.clickshrinkeffect.applyClickShrink
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.APICallBack
import org.json.JSONObject
import java.util.Locale

class OtherUserProfileF : BottomSheetDialogFragment, View.OnClickListener {
    lateinit var binding: FragmentOtherUserProfileBinding
    var myUserModel: HomeUserModel? = null
    var selectedModel: UserModel? = null
    var callBack: FragmentCallBack? = null
    var reference: DatabaseReference? = null
    var dataList: ArrayList<UserSuggestionModel> = ArrayList()
    var adapter: ProfileSuggestionAdapter? = null
    var isDirectMessage: Boolean = false

    var isInvitedAsSpeaker: Boolean = false


    var roomId: String? = null
    var currentUserList: ArrayList<HomeUserModel>? = null
    var roleType: String? = null
    var userId: String? = null

    var isUserAlreadyBlock: String = "0"
    var blockByUserId: String? = "0"

    constructor(
        userModel: UserModel,
        roomId: String?,
        roleType: String?,
        currentUserList: ArrayList<HomeUserModel>?,
        callBack: FragmentCallBack?
    ) {
        this.userId = userModel.id
        this.selectedModel = userModel
        this.callBack = callBack
        this.roomId = roomId
        this.roleType = roleType
        this.currentUserList = currentUserList
    }


    constructor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_other_user_profile,
            container,
            false
        )
        initControl()
        return binding.getRoot()
    }

    private fun initControl() {
        binding.tabProfile.setOnClickListener(this)
        binding.tabProfile.applyClickShrink()
        binding.tabFollow.setOnClickListener(this)
        binding.tabFollow.applyClickShrink()
        binding.tabSuggestion.setOnClickListener(this)
        binding.tabSuggestion.applyClickShrink()
        binding.ivMenu.setOnClickListener(this)
        binding.ivMenu.applyClickShrink()
        binding.ivClose.setOnClickListener(this)
        binding.ivClose.applyClickShrink()
        binding.tabChat.setOnClickListener(this)
        binding.tabChat.applyClickShrink()
        binding.tabViewProfile.setOnClickListener(this)
        binding.tabViewProfile.applyClickShrink()
        binding.tabFollowers.setOnClickListener(this)
        binding.tabFollowers.applyClickShrink()
        binding.tabFollowerings.setOnClickListener(this)
        binding.tabFollowerings.applyClickShrink()
        binding.tabMoveToAduiance.setOnClickListener(this)
        binding.tabMoveToAduiance.applyClickShrink()
        binding.tabInviteToSpeak.setOnClickListener(this)
        binding.tabInviteToSpeak.applyClickShrink()
        binding.tabMakeAModerator.setOnClickListener(this)
        binding.tabMakeAModerator.applyClickShrink()
        binding.tabWave.setOnClickListener(this)
        binding.tabWave.applyClickShrink()

        reference = FirebaseDatabase.getInstance().reference


        setupSuggestionList()
        suggestedFollowers

        if (selectedModel != null) {
            setUpScreenData()
        }

        hitUserprofileDetail()
    }


    private fun setupSuggestionList() {
        val layoutManager = LinearLayoutManager(binding.root.context)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        binding.recyclerview.layoutManager = layoutManager
        adapter = ProfileSuggestionAdapter(dataList) { view, pos, `object` ->
            val itemUpdate = dataList[pos]
            when (view.id) {
                R.id.tabFollow -> {
                    followUnFollowUser()
                }

                R.id.tabProfile -> {
                    openProfile(itemUpdate.userModel!!.id)
                }

                R.id.tabRemove -> {
                    dataList.removeAt(pos)
                    adapter!!.notifyDataSetChanged()
                }
            }
        }
        binding.recyclerview.adapter = adapter
    }


    fun followUnFollowUser() {
        callApiForFollowUnFollow(
            activity,
            getSharedPreference(activity).getString(Variables.U_ID, ""),
            userId,
            object : APICallBack {
                override fun arrayData(arrayList: ArrayList<*>?) {
                }

                override fun onSuccess(responce: String) {
                    hitUserprofileDetail()
                }

                override fun onFail(responce: String) {
                }
            })
    }


    private fun openProfile(id: String?) {
        dismiss()
        if (checkProfileOpenValidation(selectedModel!!.id)) {
            val intent = Intent(activity, ProfileActivity::class.java)
            intent.putExtra("user_id", selectedModel!!.id)
            intent.putExtra("user_name", selectedModel!!.username)
            intent.putExtra("user_pic", selectedModel!!.getProfilePic())
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        }
    }


    private val suggestedFollowers: Unit
        get() {
            val parameters = JSONObject()
            try {
                parameters.put(
                    "user_id", getSharedPreference(
                        context
                    ).getString(Variables.U_ID, "")
                )
                parameters.put("starting_point", "0")
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }

            VolleyRequest.JsonPostRequest(
                activity, ApiLinks.showSuggestedUsers, parameters, getHeaders(
                    activity
                )
            ) { resp -> parseResponseData(resp) }
        }

    private fun parseResponseData(resp: String) {
        try {
            val jsonObject = JSONObject(resp)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val jsonObj = jsonObject.getJSONArray("msg")
                dataList.clear()

                for (i in 0 until jsonObj.length()) {
                    val innerObject = jsonObj.getJSONObject(i)
                    val userDetailModel = getUserDataModel(innerObject.getJSONObject("User"))
                    val model = UserSuggestionModel()
                    model.userModel = userDetailModel
                    dataList.add(model)
                }
                adapter!!.notifyDataSetChanged()
            }

            if (dataList.isEmpty()) {
                binding.tabNoData.visibility = View.VISIBLE
                binding.tvNoData.text =
                    binding.root.context.getString(R.string.no_user_suggestion_available)
            } else {
                binding.tabNoData.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }
    }


    private fun setUpScreenData() {
        if (roomId != null) {
            setupButtonLogic()

            if (roleType == "1") {
                binding.tabModerator.visibility = View.VISIBLE
            } else {
                binding.tabModerator.visibility = View.GONE
            }
        }

        binding.ivProfile.controller = frescoImageLoad(
            binding.root.context,
            selectedModel!!.username!!, selectedModel!!.getProfilePic(), binding.ivProfile
        )

        if (getSharedPreference(context).getString(Variables.U_ID, "") == selectedModel!!.id) {
            binding.ivMenu.visibility = View.GONE
            binding.tabFollowSuggestion.visibility = View.INVISIBLE
            binding.tabChat.visibility = View.GONE
            binding.tabViewProfile.visibility = View.GONE
        } else {
            binding.ivMenu.visibility = View.VISIBLE
            binding.tabFollowSuggestion.visibility = View.VISIBLE

            if (isDirectMessage) {
                binding.tabChat.visibility = View.VISIBLE
            } else {
                binding.tabChat.visibility = View.GONE
            }
        }

        binding.tvFullName.text = selectedModel!!.first_name + " " + selectedModel!!.last_name
        binding.tvUsername.text = showUsername(
            selectedModel!!.username
        )
        binding.tvBio.text = selectedModel!!.bio


        binding.tvFollowersCount.text = "" + selectedModel!!.followers_count
        binding.tvFolloweringsCount.text = "" + selectedModel!!.following_count

        binding.tvJoinDate.text =
            binding.root.context.getString(R.string.joined) + " " + changeDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                "MMMM dd, yyyy",
                selectedModel!!.created!!
            )


        isUserAlreadyBlock = selectedModel!!.block
        blockByUserId = selectedModel!!.blockByUser



        updateFollowButtonStatus()
    }

    private fun setupButtonLogic() {
        for (myModel in currentUserList!!) {
            if (myModel.userModel!!.id == getSharedPreference(
                    context
                ).getString(Variables.U_ID, "")
            ) {
                myUserModel = myModel
            }
        }

        //moderator
        if (myUserModel != null && myUserModel!!.userRoleType == "1") {
            if (selectedModel!!.id == getSharedPreference(
                    context
                ).getString(Variables.U_ID, "")
            ) {
                binding.tabMakeAModerator.visibility = View.GONE
                binding.tabMoveToAduiance.visibility = View.VISIBLE
                binding.tabInviteToSpeak.visibility = View.GONE
            } else if (roleType == "1") {
                binding.tabMakeAModerator.visibility = View.GONE
                binding.tabMoveToAduiance.visibility = View.VISIBLE
                binding.tabInviteToSpeak.visibility = View.GONE
            } else if (roleType == "2") {
                binding.tabMakeAModerator.visibility = View.VISIBLE
                binding.tabMoveToAduiance.visibility = View.VISIBLE
                binding.tabInviteToSpeak.visibility = View.GONE
                binding.tabViewProfile.visibility = View.VISIBLE
            } else {
                binding.tabMakeAModerator.visibility = View.GONE
                binding.tabMoveToAduiance.visibility = View.GONE
                binding.tabInviteToSpeak.visibility = View.VISIBLE
            }
        } else  //speaker
            if (myUserModel != null && myUserModel!!.userRoleType == "2") {
                if (selectedModel!!.id == getSharedPreference(
                        context
                    ).getString(Variables.U_ID, "")
                ) {
                    binding.tabMakeAModerator.visibility = View.GONE
                    binding.tabMoveToAduiance.visibility = View.VISIBLE
                    binding.tabInviteToSpeak.visibility = View.GONE
                } else if (roleType == "1") {
                    binding.tabMakeAModerator.visibility = View.GONE
                    binding.tabMoveToAduiance.visibility = View.GONE
                    binding.tabInviteToSpeak.visibility = View.GONE
                } else if (roleType == "2") {
                    binding.tabMakeAModerator.visibility = View.GONE
                    binding.tabMoveToAduiance.visibility = View.GONE
                    binding.tabInviteToSpeak.visibility = View.GONE
                } else {
                    binding.tabMakeAModerator.visibility = View.GONE
                    binding.tabMoveToAduiance.visibility = View.GONE
                    binding.tabInviteToSpeak.visibility = View.GONE
                }
            } else  //user
            {
                if (selectedModel!!.id == getSharedPreference(
                        context
                    ).getString(Variables.U_ID, "")
                ) {
                    binding.tabMakeAModerator.visibility = View.GONE
                    binding.tabMoveToAduiance.visibility = View.GONE
                    binding.tabInviteToSpeak.visibility = View.GONE
                    binding.tabViewProfile.visibility = View.VISIBLE
                    checkModeratorInvitation()
                } else if (roleType == "1") {
                    binding.tabMakeAModerator.visibility = View.GONE
                    binding.tabMoveToAduiance.visibility = View.GONE
                    binding.tabInviteToSpeak.visibility = View.GONE
                } else if (roleType == "2") {
                    binding.tabMakeAModerator.visibility = View.GONE
                    binding.tabMoveToAduiance.visibility = View.GONE
                    binding.tabInviteToSpeak.visibility = View.GONE
                } else {
                    binding.tabMakeAModerator.visibility = View.GONE
                    binding.tabMoveToAduiance.visibility = View.GONE
                    binding.tabInviteToSpeak.visibility = View.GONE
                }
            }
    }

    private fun checkModeratorInvitation() {
        reference!!.child(Variables.roomKey).child(roomId!!).child(Variables.roomInvitation).child(getSharedPreference(context).getString(Variables.U_ID, "")!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val invitation = snapshot.getValue(
                            InviteForSpeakModel::class.java
                        )
                        if (invitation!!.getInvite() == "1") {
                            registerSpeakInvitationListener()
                            isInvitedAsSpeaker = true
                            binding.tabInviteToSpeak.visibility = View.VISIBLE
                            binding.tvInviteToSpeak.text =
                                binding.root.context.getString(R.string.accept_speaking_invitation)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivMenu -> {
                showSettingMenu()
            }

            R.id.ivClose -> {
                dismiss()
            }

            R.id.tabChat -> {
                val bundle = Bundle()
                bundle.putBoolean("isShow", true)
                bundle.putString("action", "openChat")
                bundle.putParcelable("data", selectedModel)
                callBack!!.onResponce(bundle)
                dismiss()
            }

            R.id.tabMoveToAduiance -> {
                if (checkIAmTheSingleModerator()) {
                    val bundle = Bundle()
                    bundle.putBoolean("isShow", true)
                    bundle.putString("action", "moveToAudience")
                    bundle.putParcelable("data", selectedModel)
                    callBack!!.onResponce(bundle)
                    dismiss()
                }
            }

            R.id.tabInviteToSpeak -> {
                val bundle = Bundle()
                bundle.putBoolean("isShow", true)
                if (isInvitedAsSpeaker) {
                    bundle.putString("action", "acceptInviteToSpeaker")
                } else {
                    bundle.putString("action", "inviteToSpeaker")
                }
                bundle.putParcelable("data", selectedModel)
                callBack!!.onResponce(bundle)

                dismiss()
            }

            R.id.tabMakeAModerator -> {
                val bundle = Bundle()
                bundle.putBoolean("isShow", true)
                bundle.putString("action", "makeToModerator")
                bundle.putParcelable("data", selectedModel)
                callBack!!.onResponce(bundle)
                dismiss()
            }

            R.id.tabWave -> {
                val bundle = Bundle()
                bundle.putBoolean("isShow", true)
                bundle.putString("action", "wave")
                bundle.putParcelable("data", selectedModel)
                callBack!!.onResponce(bundle)
                dismiss()
            }

            R.id.tabViewProfile -> {
                openProfile(selectedModel!!.id)
            }

            R.id.tabSuggestion -> {
                if (binding.tabSuggestionUser.visibility == View.VISIBLE) {
                    binding.tabSuggestionUser.visibility = View.GONE
                    binding.tabSuggestion.background = ContextCompat.getDrawable(
                        binding.root.context, R.drawable.button_rounded_background
                    )
                    binding.ivSuggestion.setColorFilter(
                        ContextCompat.getColor(
                            binding.root.context, R.color.whiteColor
                        ), PorterDuff.Mode.SRC_IN
                    )
                    binding.ivSuggestion.rotation = 0f
                } else {
                    binding.tabSuggestionUser.visibility = View.VISIBLE
                    binding.tabSuggestion.background = ContextCompat.getDrawable(
                        binding.root.context, R.drawable.ractengle_gray_on_black
                    )
                    binding.ivSuggestion.setColorFilter(
                        ContextCompat.getColor(
                            binding.root.context, R.color.white
                        ), PorterDuff.Mode.SRC_IN
                    )
                    binding.ivSuggestion.rotation = 180f
                }
            }

            R.id.tabFollowers -> {
                openFollowers()
                dismiss()
            }

            R.id.tabFollowerings -> {
                openFollowings()
                dismiss()
            }

            R.id.tabFollow -> {
                followUnFollowUser()
            }

            else -> {}
        }
    }


    private fun updateFollowButtonStatus() {
        if (selectedModel!!.button!!.lowercase(Locale.getDefault()) == "follow" || selectedModel!!.button!!.lowercase(
                Locale.getDefault()
            ) == "follow back"
        ) {
            binding.tabFollow.background = ContextCompat.getDrawable(
                binding.root.context, R.drawable.button_rounded_solid_primary
            )
            binding.tvFollow.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.whiteColor
                )
            )
            binding.tvFollow.text = "Follow"
        } else {
            binding.tabFollow.background = ContextCompat.getDrawable(
                binding.root.context, R.drawable.button_rounded_gray_strok_background
            )
            binding.tvFollow.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.appColor
                )
            )
            binding.tvFollow.text = "Following"
        }


        if (selectedModel!!.button!!.lowercase(Locale.getDefault()) == "friends" || selectedModel!!.button!!.lowercase(
                Locale.getDefault()
            ) == "follow back"
        ) {
            binding.tabWave.visibility = View.VISIBLE
        } else {
            binding.tabWave.visibility = View.GONE
        }
    }


    private fun increateFollowingCount() {
        selectedModel!!.following_count = selectedModel!!.following_count + 1
        binding.tvFolloweringsCount.text = "" + selectedModel!!.following_count
    }


    private fun decreateFollowingCount() {
        selectedModel!!.following_count = selectedModel!!.following_count - 1
        binding.tvFolloweringsCount.text = "" + selectedModel!!.following_count
    }


    private fun checkIAmTheSingleModerator(): Boolean {
        var countModerator = 0
        var countSpeaker = 0

        var speakerModel: HomeUserModel? = null

        for (moderatorModel in currentUserList!!) {
            if (moderatorModel.userRoleType == "1") {
                countModerator = countModerator + 1
            }
            if (moderatorModel.userRoleType == "2") {
                countSpeaker = countSpeaker + 1

                if (countSpeaker == 1) {
                    speakerModel = moderatorModel
                }
            }
        }

        if (countModerator < 2) {
            if (countSpeaker < 1) {
                showError(
                    activity,
                    binding.root.context.getString(R.string.you_are_the_only_speaker_so_you_cant_go_to_the_audience)
                )
                dismiss()
                return false
            }

            //            else
//            {
////                moderation assign to top speaker
//                if (speakerModel!=null)
//                {
//                    Bundle bundle=new Bundle();
//                    bundle.putBoolean("isShow",true);
//                    bundle.putString("action","makeModeratorToSpeakerAndLeave");
//                    bundle.putParcelable("speakerModel",speakerModel);
//                    callBack.onResponce(bundle);
//                    dismiss();
//                    return false;
//                }
//
//            }
        }


        return true
    }


    var tvBlockUser: TextView? = null
    private fun showSettingMenu() {
        val alertDialog = Dialog(requireActivity())
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setContentView(R.layout.item_report_user_dialog)

        val tabReportUser = alertDialog.findViewById<RelativeLayout>(R.id.tabReportUser)
        val tabBlockUser = alertDialog.findViewById<RelativeLayout>(R.id.tabBlockUser)
        val tabShareProfile = alertDialog.findViewById<RelativeLayout>(R.id.tabShareProfile)
        tvBlockUser = alertDialog.findViewById(R.id.tvBlockUser)

        Log.d(Constants.tag, "blockObj: $blockByUserId")
        Log.d(Constants.tag, "isUserAlreadyBlock: $isUserAlreadyBlock")

        if (blockByUserId == getSharedPreference(activity).getString(Variables.U_ID, "")) {
            tabBlockUser.visibility = View.VISIBLE
        } else {
            if (isUserAlreadyBlock == "1") {
                tabBlockUser.visibility = View.GONE
            } else {
                tabBlockUser.visibility = View.VISIBLE
            }
        }

        if (isUserAlreadyBlock == "1") tvBlockUser?.setText(requireContext().getString(R.string.unblock_user))
        else tvBlockUser?.setText(requireContext().getString(R.string.block_user))

        tabShareProfile.setOnClickListener { v: View? ->
            alertDialog.dismiss()
            if (checkLoginUser(activity)) {
                shareProfile()
            }
        }
        tabReportUser.setOnClickListener { v: View? ->
            alertDialog.dismiss()
            if (checkLoginUser(activity)) {
                openUserReport()
            }
        }


        tabBlockUser.setOnClickListener { v: View? ->
            alertDialog.dismiss()
            if (checkLoginUser(activity)) {
                openBlockUserDialog()
            }
        }

        alertDialog.show()
    }


    private fun openBlockUserDialog() {
        val params = JSONObject()
        try {
            params.put(
                "user_id",
                getSharedPreference(activity).getString(Variables.U_ID, "")
            )
            params.put("block_user_id", userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }


        showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.blockUser, params, getHeaders(
                activity
            )
        ) { resp ->
            checkStatus(activity, resp)
            cancelLoader()
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code == "200") {
                    val msgObj = jsonObject.getJSONObject("msg")
                    if (msgObj.has("BlockUser")) {
                        showToast(activity, getString(R.string.user_blocked))
                        tvBlockUser!!.setText(R.string.unblock_user)
                        isUserAlreadyBlock = "1"
                    } else {
                        isUserAlreadyBlock = "0"
                    }
                } else {
                    isUserAlreadyBlock = "0"
                }
                hitUserprofileDetail()
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception: $e")
            }
        }
    }


    private fun shareProfile() {
        var fromSetting = false
        fromSetting = if (userId.equals(
                getSharedPreference(activity).getString(Variables.U_ID, ""),
                ignoreCase = true
            )
        ) {
            true
        } else {
            false
        }

        val fragment = ShareUserProfileFragment(
            userId,
            selectedModel!!.username,
            selectedModel!!.first_name + " " + selectedModel!!.last_name,
            selectedModel!!.getProfilePic(),
            selectedModel!!.button,
            isDirectMessage,
            fromSetting
        ) { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                hitUserprofileDetail()
            }
        }
        fragment.show(childFragmentManager, "")
    }

 

    fun openUserReport() {
        val intent = Intent(activity, ReportTypeActivity::class.java)
        intent.putExtra("id", userId)
        intent.putExtra("type", "user")
        intent.putExtra("isFrom", false)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }


    private fun openFollowings() {
        val intent = Intent(activity, FollowsMainTabActivity::class.java)
        intent.putExtra("id", userId)
        intent.putExtra("from_where", "following")
        intent.putExtra("userName", selectedModel!!.username)
        intent.putExtra("followingCount", "" + selectedModel!!.following_count)
        intent.putExtra("followerCount", "" + selectedModel!!.followers_count)
        resultFollowCallback.launch(intent)
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    // open the followers screen
    private fun openFollowers() {
        val intent = Intent(activity, FollowsMainTabActivity::class.java)
        intent.putExtra("id", userId)
        intent.putExtra("from_where", "fan")
        intent.putExtra("userName", selectedModel!!.username)
        intent.putExtra("followingCount", "" + selectedModel!!.following_count)
        intent.putExtra("followerCount", "" + selectedModel!!.followers_count)
        resultFollowCallback.launch(intent)
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    var resultFollowCallback: ActivityResultLauncher<Intent> =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    if (result.resultCode == Activity.RESULT_OK) {
                        val data = result.data
                        if (data!!.getBooleanExtra("isShow", false)) {
                            hitUserprofileDetail()
                        }
                    }
                }
            })


    private fun hitUserprofileDetail() {
        val parameters = JSONObject()
        try {
            if (userId == getSharedPreference(context).getString(Variables.U_ID, "")) {
                parameters.put(
                    "user_id", getSharedPreference(
                        context
                    ).getString(Variables.U_ID, "")
                )
            } else {
                parameters.put(
                    "user_id", getSharedPreference(
                        context
                    ).getString(Variables.U_ID, "")
                )
                parameters.put("other_user_id", userId)
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: hitUserprofileDetail $e")
        }

        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.showUserDetail, parameters, getHeaders(
                activity
            )
        ) { resp -> parseUserDetailRes(resp) }
    }

    private fun parseUserDetailRes(resp: String) {
        try {
            val jsonObject = JSONObject(resp)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val jsonObj = jsonObject.getJSONObject("msg")
                val userDetailModel = getUserDataModel(jsonObj.getJSONObject("User"))

                selectedModel = userDetailModel

                val push_notification_setting = jsonObj.optJSONObject("PushNotification")
                val privacy_policy_setting = jsonObj.optJSONObject("PrivacySetting")


                val privacyPolicySetting_model = Gson().fromJson(
                    privacy_policy_setting.toString(),
                    PrivacySettingModel::class.java
                )


                isDirectMessage = if (isShowContentPrivacy(
                        requireActivity(), privacyPolicySetting_model.directMessage,
                        selectedModel!!.button!!.lowercase(Locale.getDefault())
                            .equals("friends", ignoreCase = true)
                    )
                ) {
                    true
                } else {
                    false
                }

                setUpScreenData()
            } else {
                showError(activity, jsonObject.optString("msg"))
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }
    }


    var speakInvitationListener: ValueEventListener? = null
    private fun registerSpeakInvitationListener() {
        if (speakInvitationListener == null) {
            speakInvitationListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(Constants.tag, "roomUpdateListener : $dataSnapshot")
                    if (dataSnapshot.exists()) {
                        val invitation = dataSnapshot.getValue(
                            InviteForSpeakModel::class.java
                        )
                        if (invitation!!.getInvite() == "1") {
                            isInvitedAsSpeaker = true
                            binding.tabInviteToSpeak.visibility = View.VISIBLE
                            binding.tvInviteToSpeak.text =
                                binding.root.context.getString(R.string.accept_speaking_invitation)
                        } else {
                            isInvitedAsSpeaker = false
                            binding.tabInviteToSpeak.visibility = View.GONE
                            binding.tvInviteToSpeak.text =
                                binding.root.context.getString(R.string.invite_to_speak)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            reference!!.child(Variables.roomKey).child(roomId!!).child(Variables.roomInvitation).child(getSharedPreference(context).getString(Variables.U_ID, "")!!)
                .addValueEventListener(speakInvitationListener!!)
        }
    }

    fun removeSpeakInvitationListener() {
        if (reference != null && speakInvitationListener != null) {
            reference!!.child(Variables.roomKey).child(roomId!!).child(
            Variables.roomInvitation).child(getSharedPreference(context).getString(Variables.U_ID, "")!!)
                .removeEventListener(speakInvitationListener!!)
            speakInvitationListener = null
        }
    }


    override fun onDetach() {
        removeSpeakInvitationListener()
        super.onDetach()
    }

    companion object {
        fun newInstance(
            userModel: UserModel,
            roomId: String?,
            roleType: String?,
            currentUserList: ArrayList<HomeUserModel>?,
            callBack: FragmentCallBack?
        ): OtherUserProfileF {
            val fragment = OtherUserProfileF(userModel, roomId, roleType, currentUserList, callBack)
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}