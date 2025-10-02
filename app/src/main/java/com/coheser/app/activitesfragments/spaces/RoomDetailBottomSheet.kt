package com.coheser.app.activitesfragments.spaces

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.chat.ChatActivity
import com.coheser.app.activitesfragments.profile.ReportTypeActivity
import com.coheser.app.activitesfragments.spaces.adapters.CurrentSpeakerRoomAdapter
import com.coheser.app.activitesfragments.spaces.models.HomeUserModel
import com.coheser.app.activitesfragments.spaces.services.RoomStreamService
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.MainStreamingModel
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.RoomFirebaseListener
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.RoomFirebaseManager
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.RoomManager
import com.coheser.app.databinding.CurrentRoomLayoutSheetBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.InviteForSpeakModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Dialogs.showSuccess
import com.coheser.app.simpleclasses.Functions.getShareRoomLink
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.getSuffix
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Functions.shareData
import com.coheser.app.simpleclasses.Variables
import com.realpacific.clickshrinkeffect.applyClickShrink

class RoomDetailBottomSheet : Fragment, View.OnClickListener {
    lateinit var binding: CurrentRoomLayoutSheetBinding
    var mainStreamingModel: MainStreamingModel? = null
    var reference: DatabaseReference? = null
    var myUserModel: HomeUserModel? = null

    var messageCount: Long = 0

    constructor(mainStreamingModel: MainStreamingModel?) {
        this.mainStreamingModel = mainStreamingModel
    }

    constructor()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.current_room_layout_sheet, container, false)

        reference = FirebaseDatabase.getInstance().reference


        initRoomSheet()
        connectWithRoom()
        return binding.getRoot()
    }


    var roomManager: RoomManager? = null
    var firebaseRoomManager: RoomFirebaseManager? = null

    fun addManagerListeners() {
        firebaseRoomManager = RoomFirebaseManager.getInstance(requireActivity())
        firebaseRoomManager?.mainStreamingModel = mainStreamingModel
        firebaseRoomManager?.updateListerner3(object : RoomFirebaseListener {
            override fun createRoom(bundle: Bundle?) {
            }

            override fun JoinedRoom(bundle: Bundle?) {
            }

            override fun onRoomLeave(bundle: Bundle?) {
                closeRoomScreen()
            }

            override fun onRoomDelete(bundle: Bundle?) {
                closeRoomScreen()
            }

            override fun onRoomUpdate(bundle: Bundle?) {
                setRoomData()
            }

            override fun onRoomUsersUpdate(bundle: Bundle?) {
                setRoomUserData()
            }

            override fun onMyUserUpdate(bundle: Bundle?) {
                setMyUserModelData()
            }

            override fun onSpeakInvitationReceived(bundle: Bundle?) {
            }

            override fun onWaveUserUpdate(bundle: Bundle?) {
            }
        })

        roomManager = RoomManager.getInstance(requireActivity())
    }


    private fun connectWithRoom() {
        addManagerListeners()

        setupSpeakerRoomAdapter()
        setupAudienceRoomAdapter()

        setRoomData()
        setRoomUserData()
        setMyUserModelData()
    }

    private fun initRoomSheet() {
       binding.ivRoomShare.setOnClickListener(this)
       binding.ivRoomShare.applyClickShrink()

       binding.ivRoomClose.setOnClickListener(this)
       binding.ivRoomClose.applyClickShrink()

       binding.ivOption.setOnClickListener(this)
       binding.ivOption.applyClickShrink()

       binding.tabRoomChat.setOnClickListener(this)
       binding.tabRoomChat.applyClickShrink()


       binding.tabLeaveQueitly.setOnClickListener(this)
       binding.tabLeaveQueitly.applyClickShrink()

       binding.tabQueitly.setOnClickListener(this)
       binding.tabQueitly.applyClickShrink()

       binding.tabRaiseHand.setOnClickListener(this)
       binding.tabRaiseHand.applyClickShrink()

       binding.tabRiseHandUser.setOnClickListener(this)
       binding.tabRiseHandUser.applyClickShrink()

       binding.tabMice.setOnClickListener(this)
       binding.tabMice.applyClickShrink()
    }


    var speakerAdapter: CurrentSpeakerRoomAdapter? = null

    private fun setupSpeakerRoomAdapter() {
        val layoutManager = GridLayoutManager(binding!!.root.context, 3)
        layoutManager.orientation = RecyclerView.VERTICAL
       binding.recylerviewSpeaker.layoutManager = layoutManager
        speakerAdapter =
            CurrentSpeakerRoomAdapter(firebaseRoomManager?.speakersUserList!!) { view, pos, `object` ->
                val itemUpdate = firebaseRoomManager!!.speakersUserList[pos]
                when (view.id) {
                    R.id.tabMain -> {
                        openUserProfile(itemUpdate)
                    }
                }
            }
       binding.recylerviewSpeaker.adapter = speakerAdapter
    }

    var audienceAdapter: CurrentSpeakerRoomAdapter? = null

    private fun setupAudienceRoomAdapter() {
        val layoutManager = GridLayoutManager(binding!!.root.context, 3)
        layoutManager.orientation = RecyclerView.VERTICAL
       binding.recylerviewOtherUser.layoutManager = layoutManager
        audienceAdapter =
            CurrentSpeakerRoomAdapter(firebaseRoomManager!!.audienceUserList) { view, pos, `object` ->
                val itemUpdate = firebaseRoomManager!!.audienceUserList[pos]
                when (view.id) {
                    R.id.tabMain -> {
                        openUserProfile(itemUpdate)
                    }
                }
            }
       binding.recylerviewOtherUser.adapter = audienceAdapter
    }


    fun setRoomData() {
        mainStreamingModel = firebaseRoomManager!!.mainStreamingModel
        myUserModel = firebaseRoomManager!!.myUserModel

        if (!(TextUtils.isEmpty(mainStreamingModel?.model?.title))) {
           binding.roomTitle.text = "" + mainStreamingModel?.model?.title
        }

        if (myUserModel != null && myUserModel!!.userRoleType == "0") {
            if (mainStreamingModel?.model?.riseHandRule == "1") {
               binding.tabRaiseHand.visibility = View.VISIBLE
            } else {
               binding.tabRaiseHand.visibility = View.GONE
            }
        }
    }

    fun setMyUserModelData() {
        mainStreamingModel = firebaseRoomManager!!.mainStreamingModel
        myUserModel = firebaseRoomManager!!.myUserModel
        ListenerChatCountNode()

        if (myUserModel != null) {
            if (myUserModel!!.userRoleType == "1" || myUserModel!!.userRoleType == "2") {
                if (myUserModel!!.mice == "1") {
                   binding.ivMice.setImageDrawable(
                        ContextCompat.getDrawable(
                           binding.root.context,
                            R.drawable.ic_mice
                        )
                    )

                    if (RoomStreamService.streamingInstance != null && RoomStreamService.streamingInstance!!.ismAudioMuted()) RoomStreamService.streamingInstance!!.enableVoiceCall()
                } else {
                   binding.ivMice.setImageDrawable(
                        ContextCompat.getDrawable(
                           binding.root.context,
                            R.drawable.ic_mice_mute
                        )
                    )

                    if (RoomStreamService.streamingInstance != null && !RoomStreamService.streamingInstance!!.ismAudioMuted()) RoomStreamService.streamingInstance!!.muteVoiceCall()
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

                if (RoomStreamService.streamingInstance != null && !RoomStreamService.streamingInstance!!.ismAudioMuted()) RoomStreamService.streamingInstance!!.muteVoiceCall()


               binding.tabMice.visibility = View.GONE
               binding.tabRiseHandUser.visibility = View.GONE
            }

            if (myUserModel!!.userRoleType == "1") {
               binding.tabRiseHandUser.visibility = View.VISIBLE
            }
        }
    }

    fun setRoomUserData() {
        mainStreamingModel = firebaseRoomManager!!.mainStreamingModel
        myUserModel = firebaseRoomManager!!.myUserModel

        speakerAdapter!!.notifyDataSetChanged()
        audienceAdapter!!.notifyDataSetChanged()

        if (mainStreamingModel?.model?.riseHandCount!!.toInt() > 0) {
           binding.tvRiseHandCount.text =
                "" + getSuffix("" + mainStreamingModel?.model?.riseHandCount)
           binding.tvRiseHandCount.visibility = View.VISIBLE
        } else {
           binding.tvRiseHandCount.text = "0"
           binding.tvRiseHandCount.visibility = View.GONE
        }

        if (firebaseRoomManager!!.speakersUserList.size > 0) {
            checkRoomOwnerOnline()
        }
    }


    fun checkRoomOwnerOnline() {
        var online = "0"
        for (i in firebaseRoomManager!!.speakersUserList.indices) {
            if (firebaseRoomManager!!.speakersUserList[i].online != null && firebaseRoomManager!!.speakersUserList[i].online == "1") {
                online = "1"
                printLog(
                    Constants.tag,
                    "Online:" + firebaseRoomManager!!.speakersUserList[i].online
                )
            }
        }

        printLog(Constants.tag, "Online2:$online")
        if (online == "0") {
            roomManager!!.deleteRoom(mainStreamingModel?.model?.id!!)
        }
    }

    private fun openUserProfile(itemUpdate: HomeUserModel) {
        Log.d(
            Constants.tag,
            "AdminUser: " + itemUpdate.userModel!!.id + "    " + mainStreamingModel?.model?.id!!
        )
        Log.d(
            Constants.tag,
            "AdminUserName: " + itemUpdate.userModel!!.username + "    " + mainStreamingModel?.model?.title
        )

        val fragment = OtherUserProfileF.newInstance(
            itemUpdate.userModel!!,
            mainStreamingModel?.model?.id!!,
            itemUpdate.userRoleType,
            firebaseRoomManager?.speakersUserList
        ) { bundle ->
            if (bundle.getBoolean("isShow")) {
                handleProfileClick(bundle, itemUpdate.userModel)
            }
        }
        fragment.show(requireActivity().supportFragmentManager, "UserProfileF")
    }

    private fun handleProfileClick(bundle: Bundle, userModel: UserModel?) {
        if (bundle.getString("action") == "openChat") {
            openChat(userModel)
        } else if (bundle.getString("action") == "moveToAudience") {
            moveToRoomAudiance(userModel)
        } else if (bundle.getString("action") == "inviteToSpeaker") {
            sendInvitationForSpeak(userModel)
        } else if (bundle.getString("action") == "acceptInviteToSpeaker") {
            roomManager!!.speakerJoinRoomHitApi(
                getSharedPreference(
                    context
                ).getString(Variables.U_ID, ""), mainStreamingModel?.model?.id!!, "2"
            )
        } else if (bundle.getString("action") == "makeToModerator") {
            makeRoomModerator(userModel)
        } else if (bundle.getString("action") == "makeModeratorToSpeakerAndLeave") {
            makeModeratorToSpeakerAndLeave(
                userModel,
                bundle.getSerializable("speakerModel") as HomeUserModel?
            )
        }
    }

    fun openChat(userModel: UserModel?) {
        val intent1 = Intent(activity, ChatActivity::class.java)
        intent1.putExtra("user_id", userModel!!.id)
        intent1.putExtra("user_name", userModel.username)
        intent1.putExtra("user_pic", userModel.getProfilePic())
        startActivity(intent1)
    }

    fun openRoomReport() {
        val intent = Intent(activity, ReportTypeActivity::class.java)
        intent.putExtra("id", mainStreamingModel?.model?.id!!)
        intent.putExtra("type", "room")
        intent.putExtra("isFrom", "room")
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }


    private fun moveToRoomAudiance(userModel: UserModel?) {
        if (mainStreamingModel != null) {
            reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!).child(Variables.roomUsers).child(userModel?.id!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val dataItem = snapshot.getValue(
                                HomeUserModel::class.java
                            )
                            dataItem!!.userRoleType = "0"

                            reference!!.child(Variables.roomKey)
                                .child(mainStreamingModel?.model?.id!!).child(Variables.roomUsers).child(userModel?.id!!)
                                .setValue(dataItem)
                                .addOnCompleteListener(object : OnCompleteListener<Void> {
                                    override fun onComplete(task: Task<Void>) {
                                        if (task.isSuccessful) {
                                            reference!!.child(Variables.roomKey)
                                                .child(mainStreamingModel?.model?.id!!).child(Variables.roomInvitation)
                                                .child(userModel?.id!!)
                                                .removeValue()
                                        }
                                    }
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }


    private fun makeRoomModerator(userModel: UserModel?) {
        if (mainStreamingModel != null) {
            reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!).child(Variables.roomUsers).child(userModel?.id!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val dataItem = snapshot.getValue(
                                HomeUserModel::class.java
                            )
                            dataItem!!.userRoleType = "1"

                            reference!!.child(Variables.roomKey)
                                .child(mainStreamingModel?.model?.id!!).child(Variables.roomUsers)
                                .child(userModel?.id!!)
                                .setValue(dataItem)
                                .addOnCompleteListener(object : OnCompleteListener<Void> {
                                    override fun onComplete(task: Task<Void>) {
                                        if (task.isSuccessful) {
                                            showSuccess(
                                                activity,
                                               binding.root.context.getString(R.string.great_they_are_now_moderator)
                                            )
                                        }
                                    }
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }

    private fun makeModeratorToSpeakerAndLeave(
        userModel: UserModel?,
        speakerModel: HomeUserModel?
    ) {
        makeRoomModerator(userModel, speakerModel)
    }

    private fun makeRoomModerator(userModel: UserModel?, speakerModel: HomeUserModel?) {
        if (mainStreamingModel != null) {
            reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!).child(Variables.roomUsers)
                .child(speakerModel?.userModel?.id!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val dataItem = snapshot.getValue(
                                HomeUserModel::class.java
                            )
                            dataItem!!.userRoleType = "1"

                            reference!!.child(Variables.roomKey)
                                .child(mainStreamingModel?.model?.id!!).child(Variables.roomUsers)
                                .child(speakerModel?.userModel?.id!!)
                                .setValue(dataItem)
                                .addOnCompleteListener(object : OnCompleteListener<Void> {
                                    override fun onComplete(task: Task<Void>) {
                                        if (task.isSuccessful) {
                                            roomManager!!.speakerJoinRoomHitApi(
                                                userModel!!.id,
                                                mainStreamingModel?.model?.id!!,
                                                "0"
                                            )
                                        }
                                    }
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }


    private fun closeRoomScreen() {
        requireActivity().onBackPressed()
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivOption -> {
                openRoomSettingOption()
            }

            R.id.tabRoomChat -> {
                openRoomChat()
            }

            R.id.ivRoomClose -> {
                closeRoomScreen()
            }

            R.id.ivRoomShare -> {
                shareData(
                    requireActivity(), getShareRoomLink(
                        requireContext(), mainStreamingModel?.model?.id!!
                    )
                )
            }

            R.id.tabRiseHandUser -> {
                openRiseHandList()
            }

            R.id.tabRaiseHand -> {
                openRiseHandToSpeak()
            }

            R.id.tabLeaveQueitly -> {
                run {
                    removeRoom()
                }
                run {
                    updateMyMiceStatus()
                }
            }

            R.id.tabMice -> {
                updateMyMiceStatus()
            }
        }
    }


    private fun updateMyMiceStatus() {
        if (RoomStreamService.streamingInstance != null) {
            val updateMice = HashMap<String, Any>()
            if (RoomStreamService.streamingInstance!!.ismAudioMuted()) {
                updateMice["mice"] = "1"
            } else {
                updateMice["mice"] = "0"
            }
            reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!)
                .child(Variables.roomUsers).child(
                    getSharedPreference(
                        context
                    ).getString(Variables.U_ID, "")!!
                )
                .updateChildren(updateMice)
                .addOnCompleteListener(object : OnCompleteListener<Void> {
                    override fun onComplete(task: Task<Void>) {
                        if (task.isSuccessful) {
                        }
                    }
                })
        }
    }


    private fun openRoomSettingOption() {
        val fragment = RoomStreamingSettingF(firebaseRoomManager!!.speakersUserList) { bundle ->
            if (bundle.getBoolean("isShow")) {
                val actionType = bundle.getString("action")
                if (actionType == "ShareRoom") {
                    shareData(
                        requireActivity(), getShareRoomLink(
                            requireContext(), mainStreamingModel?.model?.id!!
                        )
                    )
                } else if (actionType == "EndRoom") {
                    roomManager!!.deleteRoom(mainStreamingModel?.model?.id!!)
                } else if (actionType == "UserShareRoom") {
                } else if (actionType == "UserReportRoomTitle") {
                    openRoomReport()
                }
            }
        }
        fragment.show(requireActivity().supportFragmentManager, "RoomStreamingSettingF")
    }


    private fun openRoomChat() {
       binding.ivMessageCount.visibility = View.GONE

        val roomChatF = RoomChatF.newInstance(mainStreamingModel) { bundle ->
            if (bundle.getBoolean("isShow")) {
               binding.ivMessageCount.visibility = View.GONE
            }
        }
        val ft = childFragmentManager.beginTransaction()
        ft.setCustomAnimations(
            R.anim.in_from_bottom, R.anim.out_to_top,
            R.anim.in_from_top, R.anim.out_from_bottom
        )
        ft.replace(R.id.mainRoomContainer, roomChatF, "RoomChatF")
            .addToBackStack("RoomChatF").commit()
    }


    private fun removeRoom() {
        val bundle = roomManager!!.checkRoomCanDeleteOrLeave(firebaseRoomManager!!.speakersUserList)
        printLog(Constants.tag, bundle.getString("action"))
        if (bundle.getString("action") == "removeRoom") {
            roomManager!!.deleteRoom(mainStreamingModel?.model?.id!!)
        } else if (bundle.getString("action") == "leaveRoom") {
            roomManager!!.leaveRoom(mainStreamingModel?.model?.id!!)
        } else {
            val speakerAsModeratorModel = bundle.getParcelable<HomeUserModel>("model")
            makeRoomModeratorAndLeave(speakerAsModeratorModel)
        }
    }


    private fun openRiseHandToSpeak() {
        val riseHandForSpeakF = RiseHandForSpeakF { bundle ->
            if (bundle.getBoolean("isShow")) {
                if (bundle.getString("action") == "riseHandForSpeak") {
                    val riseHandMap = HashMap<String, Any>()
                    riseHandMap["riseHand"] = "1"

                    reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!)
                        .child(Variables.roomUsers).child(
                            getSharedPreference(
                                context
                            ).getString(Variables.U_ID, "")!!
                        )
                        .updateChildren(riseHandMap)
                } else if (bundle.getString("action") == "neverMind") {
                    val riseHandMap = HashMap<String, Any>()
                    riseHandMap["riseHand"] = "0"

                    reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!)
                        .child(Variables.roomUsers).child(
                            getSharedPreference(
                                context
                            ).getString(Variables.U_ID, "")!!
                        )
                        .updateChildren(riseHandMap)
                }
            }
        }
        riseHandForSpeakF.show(requireActivity().supportFragmentManager, "RiseHandForSpeakF")
    }

    private fun openRiseHandList() {
        val fragment = RiseHandUsersF(
            mainStreamingModel?.model?.id!!,
            mainStreamingModel?.model?.riseHandRule
        ) { bundle ->
            if (bundle.getBoolean("isShow")) {
                if (bundle.getString("action") == "invite") {
                    val itemUpdate = bundle.getSerializable("itemModel") as HomeUserModel?
                    sendInvitationForSpeak(itemUpdate?.userModel)
                }
            }
        }
        fragment.show(requireActivity().supportFragmentManager, "RiseHandUsersF")
    }


    private fun sendInvitationForSpeak(userModel: UserModel?) {
        if (mainStreamingModel != null && userModel != null) {
            val invitation = InviteForSpeakModel()
            invitation.setInvite("1")
            invitation.setUserId(getSharedPreference(context).getString(Variables.U_ID, ""))
            invitation.setUserName(getSharedPreference(context).getString(Variables.U_NAME, ""))

            reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!).child(Variables.roomInvitation)
                .child(userModel?.id!!)
                .setValue(invitation).addOnCompleteListener(object : OnCompleteListener<Void> {
                    override fun onComplete(task: Task<Void>) {
                        if (task.isSuccessful) {
                            showSuccess(
                                activity,
                               binding.root.context.getString(R.string.great_we_are_sent_them_an_invite)
                            )
                        }
                    }
                })
        }
    }


    private fun makeRoomModeratorAndLeave(itemUpdate: HomeUserModel?) {
        if (mainStreamingModel!!.model != null) {
            reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!).child(Variables.roomUsers)
                .child(itemUpdate?.userModel?.id!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val dataItem = snapshot.getValue(
                                HomeUserModel::class.java
                            )
                            dataItem!!.userRoleType = "1"

                            reference!!.child(Variables.roomKey)
                                .child(mainStreamingModel?.model?.id!!).child(Variables.roomUsers)
                                .child(itemUpdate?.userModel?.id!!)
                                .setValue(dataItem)
                                .addOnCompleteListener(object : OnCompleteListener<Void> {
                                    override fun onComplete(task: Task<Void>) {
                                        if (task.isSuccessful) {
                                            roomManager!!.leaveRoom(mainStreamingModel?.model?.id)
                                        }
                                    }
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }


    var chatCountEventListener: ValueEventListener? = null

    private fun ListenerChatCountNode() {
        if (chatCountEventListener == null && mainStreamingModel != null) {
            chatCountEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (messageCount != dataSnapshot.childrenCount) {
                            messageCount = dataSnapshot.childrenCount
                           binding.ivMessageCount.visibility = View.VISIBLE
                        } else {
                           binding.ivMessageCount.visibility = View.GONE
                        }

                        Log.d(Constants.tag, "Chat message Count: " + dataSnapshot.childrenCount)
                    } else {
                       binding.ivMessageCount.visibility = View.GONE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                   binding.ivMessageCount.visibility = View.GONE
                }
            }
            reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!)
                .child(Variables.roomchat)
                .addValueEventListener(chatCountEventListener!!)
        }
    }

    fun removeChatCountListener() {
        if (reference != null && chatCountEventListener != null && mainStreamingModel != null) {
            reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!)
                .child(Variables.roomchat).removeEventListener(
                chatCountEventListener!!
            )
            chatCountEventListener = null
        }
    }


    override fun onDetach() {
        if (firebaseRoomManager != null) firebaseRoomManager!!.updateListerner3(null)
        removeChatCountListener()
        super.onDetach()
    }

    companion object {
        fun newInstance(
            mainStreamingModel: MainStreamingModel?,
            fragmentCallBack: FragmentCallBack?
        ): RoomDetailBottomSheet {
            val fragment = RoomDetailBottomSheet(mainStreamingModel)
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}