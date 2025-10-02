package com.coheser.app.activitesfragments.spaces.utils.RoomManager

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.coheser.app.Constants
import com.coheser.app.activitesfragments.spaces.models.HomeUserModel
import com.coheser.app.models.InviteForSpeakModel
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Variables
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RoomFirebaseManager(var activity: Activity) : RoomFirebaseListener {
    var reference: DatabaseReference?
    var roomManager: RoomManager

    var mainStreamingModel: MainStreamingModel? = null
    var speakersUserList: ArrayList<HomeUserModel> = ArrayList()
    var audienceUserList: ArrayList<HomeUserModel> = ArrayList()
    var myUserModel: HomeUserModel? = null

    var listerner1: RoomFirebaseListener? = null
    var listerner2: RoomFirebaseListener? = null
    var listerner3: RoomFirebaseListener? = null


    fun addAllRoomListerner() {
        registerMyRoomListener()
        registerRoomUserListener()
        registerMyJoinRoomListener()
        registerSpeakInvitationListener()
    }


    fun updateListerner1(responseListener: RoomFirebaseListener?) {
        this.listerner1 = responseListener
    }

    fun updateListerner2(responseListener: RoomFirebaseListener?) {
        this.listerner2 = responseListener
    }

    fun updateListerner3(responseListener: RoomFirebaseListener?) {
        this.listerner3 = responseListener
    }


    fun createRoomNode(model: MainStreamingModel?) {
        reference!!.child(Variables.roomKey).child(model?.model?.id!!).setValue(model.model)
            .addOnCompleteListener(
                OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (model != null) {
                            Log.d(Constants.tag, "MainStreamingModel: " + model.model!!.id)

                            for (userModel: HomeUserModel in model.userList!!) {
                                if ((userModel.userModel!!.id == getSharedPreference(
                                        activity
                                    ).getString(Variables.U_ID, ""))
                                ) {
                                    joinRoom(model?.model?.id!!, userModel)
                                    break
                                }
                            }
                        }
                    }
                })
    }

    fun joinRoom(roomId: String, userModel: HomeUserModel) {
        reference!!.child(Variables.roomKey).child(roomId).child(Variables.roomUsers)
            .child((userModel.userModel!!.id)!!).setValue(userModel)
            .addOnCompleteListener(object : OnCompleteListener<Void?> {
                override fun onComplete(task: Task<Void?>) {
                    if (task.isSuccessful) {
                        val map = HashMap<String, String>()
                        map["roomId"] = roomId
                        reference!!.child(Variables.joinedKey).child((userModel.userModel!!.id)!!)
                            .setValue(map)
                    }
                }
            })
    }

    fun updateMemberModel(myUserModel: HomeUserModel) {
        reference!!.child(Variables.roomKey)
            .child(mainStreamingModel?.model?.id!!)
            .child(Variables.roomUsers)
            .child((myUserModel.userModel!!.id)!!)
            .setValue(myUserModel)
    }


    fun removeRoomNode(roomID: String?) {
        removeInvitation()
        removeJoindNode()
        reference!!.child(Variables.roomKey)
            .child((roomID)!!)
            .removeValue()
            .addOnCompleteListener(object : OnCompleteListener<Void?> {
                override fun onComplete(task: Task<Void?>) {
                    if (task.isSuccessful) {
                        removeAllListener()
                        onRoomDelete(null)
                    }
                }
            })
    }

    fun removeUserLeaveNode(roomId: String?) {
        removeInvitation()
        removeJoindNode()
        reference!!.child(Variables.roomKey)
            .child((roomId)!!)
            .child(Variables.roomUsers)
            .child((getSharedPreference(this.activity).getString(Variables.U_ID, ""))!!)
            .removeValue()
            .addOnCompleteListener(object : OnCompleteListener<Void?> {
                override fun onComplete(task: Task<Void?>) {
                    if (task.isSuccessful) {
                        removeAllListener()
                        onRoomLeave(Bundle())
                    }
                }
            })
    }


    var myjoinListener: ValueEventListener? = null
    private fun registerJoinListener() {
        if (myjoinListener == null) {
            printLog(Constants.tag, "registerJoinListener call")
            myjoinListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        printLog(Constants.tag, "myjoinListener$dataSnapshot")
                        val roomId = dataSnapshot.child("roomId").getValue(
                            String::class.java
                        )
                        printLog(Constants.tag, "joined User roomId$roomId")

                        val bundle = Bundle()
                        bundle.putString("action", "roomJoin")
                        bundle.putString("roomId", roomId)
                        JoinedRoom(bundle)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            reference!!.child(Variables.joinedKey).child(
                (getSharedPreference(
                    this.activity
                ).getString(Variables.U_ID, ""))!!
            ).addValueEventListener(myjoinListener!!)
        }
    }

    private fun removeJoindNode() {
        reference!!.child(Variables.joinedKey).child(
            (getSharedPreference(
                this.activity
            ).getString(Variables.U_ID, ""))!!
        ).removeValue()
    }

    fun removeInvitation() {
        reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!).child(Variables.roomInvitation)
            .child(getSharedPreference(this.activity).getString(Variables.U_ID, "")!!).setValue(null)
    }


    var myRoomListener: ValueEventListener? = null
    private fun registerMyRoomListener() {
        if (myRoomListener == null) {
            myRoomListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val riseHandRule = "" + dataSnapshot.child("riseHandRule").getValue(
                            String::class.java
                        )

                        if (mainStreamingModel == null || mainStreamingModel!!.model == null) {
                            return
                        }
                        mainStreamingModel!!.model!!.riseHandRule = riseHandRule

                        val bundle = Bundle()
                        bundle.putParcelable("data", mainStreamingModel)
                        onRoomUpdate(bundle)
                    } else {
                        onRoomDelete(null)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!)
                .addValueEventListener(myRoomListener!!)
        } else {
            Log.d(Constants.tag, "myRoomListener not null")
        }
    }

    var roomUserUpdateListener: ChildEventListener? = null
    private fun registerRoomUserListener() {
        if (roomUserUpdateListener == null) {
            roomUserUpdateListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    try {
                        Log.d(Constants.tag, "onChildAdded ::")

                        if (!(TextUtils.isEmpty(snapshot.value.toString()))) {
                            val dataItem = snapshot.getValue(
                                HomeUserModel::class.java
                            )
                            if ((dataItem!!.userRoleType == "0")) {
                                audienceUserList.add(dataItem)
                            } else {
                                speakersUserList.add(dataItem)
                            }

                            if ((dataItem.userModel!!.id == getSharedPreference(
                                    activity
                                ).getString(Variables.U_ID, ""))
                            ) {
                                myUserModel = dataItem
                            }

                            Log.d(
                                Constants.tag,
                                "speakersUserList size onChildAdded" + speakersUserList.size
                            )
                            Log.d(
                                Constants.tag,
                                "audienceUserList size onChildAdded" + audienceUserList.size
                            )

                            onRoomUsersUpdate(null)
                        }
                    } catch (e: Exception) {
                        Log.d(Constants.tag, "onChildAdded: checkPoint:  " + e.message)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    try {
                        Log.d(Constants.tag, "onChildChanged ::")

                        if (!(TextUtils.isEmpty(snapshot.value.toString()))) {
                            val dataItem = snapshot.getValue(HomeUserModel::class.java)
                            val speakerPostion = getlistPostion(speakersUserList, dataItem)
                            val audiencePosition = getlistPostion(audienceUserList, dataItem)
                            if ((dataItem!!.userRoleType == "0")) {
                                if (speakerPostion >= 0) {
                                    speakersUserList.removeAt(speakerPostion)
                                }
                                if (audiencePosition >= 0) {
                                    audienceUserList[audiencePosition] = dataItem
                                } else {
                                    audienceUserList.add(dataItem)
                                }
                            } else {
                                if (audiencePosition >= 0) {
                                    audienceUserList.removeAt(audiencePosition)
                                }
                                if (speakerPostion >= 0) {
                                    speakersUserList[speakerPostion] = dataItem
                                } else {
                                    speakersUserList.add(dataItem)
                                }
                            }

                            onRoomUsersUpdate(null)


                            if ((dataItem.userModel!!.id == getSharedPreference(
                                    activity
                                ).getString(Variables.U_ID, ""))
                            ) {
                                if ((dataItem.userRoleType == "1")) {
                                    riseHandCounts
                                }
                            }

                            Log.d(
                                Constants.tag,
                                "speakersUserList size onChildChanged" + speakersUserList.size
                            )
                            Log.d(
                                Constants.tag,
                                "audienceUserList size onChildChanged" + audienceUserList.size
                            )
                        }
                    } catch (e: Exception) {
                        Log.d(Constants.tag, "onChildChanged: $e")
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    try {
                        Log.d(Constants.tag, "onChildRemoved ::")

                        val dataItem = snapshot.getValue(
                            HomeUserModel::class.java
                        )
                        val speakerPostion = getlistPostion(speakersUserList, dataItem)
                        val audiencePosition = getlistPostion(audienceUserList, dataItem)

                        if (speakerPostion >= 0) {
                            speakersUserList.removeAt(speakerPostion)
                        }
                        if (audiencePosition >= 0) {
                            audienceUserList.removeAt(audiencePosition)
                        }

                        Log.d(
                            Constants.tag,
                            "speakersUserList size onChildRemoved" + speakersUserList.size
                        )
                        Log.d(
                            Constants.tag,
                            "audienceUserList size onChildRemoved" + audienceUserList.size
                        )

                        onRoomUsersUpdate(null)
                    } catch (e: Exception) {
                        Log.d(Constants.tag, "onChildRemoved: $e")
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(Constants.tag, "onChildMoved ::")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(Constants.tag, "onCancelled ::")
                }
            }
            reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!)
                .child(Variables.roomUsers).addChildEventListener(roomUserUpdateListener!!)
        }
    }

    private fun getlistPostion(
        currentUserList: ArrayList<HomeUserModel>,
        dataItem: HomeUserModel?
    ): Int {
        for (i in currentUserList.indices) {
            if ((currentUserList[i]!!.userModel!!.id == dataItem!!.userModel!!.id)) {
                return i
            }
        }
        return -1
    }

    private val riseHandCounts: Unit
        get() {
            var riseHandCount = 0
            for (riseHandUser: HomeUserModel? in speakersUserList) {
                if ((riseHandUser!!.riseHand == "1")) {
                    riseHandCount = riseHandCount + 1
                }
            }
            mainStreamingModel!!.model!!.riseHandCount = "" + riseHandCount
        }


    var currentJoinRoomListener: ValueEventListener? = null
    private fun registerMyJoinRoomListener() {
        if (currentJoinRoomListener == null) {
            currentJoinRoomListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        if (snapshot.exists()) {
                            val dataItem = snapshot.getValue(
                                HomeUserModel::class.java
                            )

                            if ((dataItem != null) && (dataItem.userModel != null) && (
                                        dataItem.userModel!!.id != null) && (myUserModel != null)
                            ) {
                                if (myUserModel!!.userRoleType != dataItem.userRoleType) {
                                    if ((dataItem.userRoleType == "1")) {
                                        roomManager.speakerJoinRoomHitApi(
                                            getSharedPreference(
                                                activity
                                            ).getString(Variables.U_ID, ""),
                                            mainStreamingModel!!.model!!.id,
                                            "1"
                                        )
                                    } else if ((dataItem.userRoleType == "0")) {
                                        roomManager.speakerJoinRoomHitApi(
                                            getSharedPreference(
                                                activity
                                            ).getString(Variables.U_ID, ""),
                                            mainStreamingModel!!.model!!.id,
                                            "0"
                                        )
                                    }
                                }
                            }
                            myUserModel = dataItem
                            onMyUserUpdate(Bundle())
                        } else {
                            onRoomLeave(Bundle())
                        }
                    } catch (e: Exception) {
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
        }

        reference!!.child(Variables.roomKey)
            .child(mainStreamingModel?.model?.id!!)
            .child(Variables.roomUsers).child(
                (getSharedPreference(
                    this.activity
                ).getString(Variables.U_ID, ""))!!
            )
            .addValueEventListener(currentJoinRoomListener!!)
    }


    var speakInvitationListener: ValueEventListener? = null

    init {
        roomManager = RoomManager(activity)
        reference = FirebaseDatabase.getInstance().reference

        registerJoinListener()
    }

    private fun registerSpeakInvitationListener() {
        if (speakInvitationListener == null) {
            speakInvitationListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(Constants.tag, "roomUpdateListener : $dataSnapshot")
                    if (dataSnapshot.exists()) {
                        val invitation = dataSnapshot.getValue(
                            InviteForSpeakModel::class.java
                        )
                        val bundle = Bundle()
                        bundle.putSerializable("data", invitation)
                        onSpeakInvitationReceived(bundle)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            reference!!.child(Variables.roomKey)
                .child(mainStreamingModel?.model?.id!!)
                .child(Variables.roomInvitation)
                .child((getSharedPreference(this.activity).getString(Variables.U_ID, ""))!!)
                .addValueEventListener(speakInvitationListener!!)
        }
    }


    fun removeAllListener() {
        if (reference != null && myRoomListener != null) {
            reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!)
                .removeEventListener(
                    myRoomListener!!
                )
        }
        if (reference != null && roomUserUpdateListener != null) {
            reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!)
                .child(Variables.roomUsers).removeEventListener(
                roomUserUpdateListener!!
            )
        }
        if (reference != null && currentJoinRoomListener != null) {
            reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!)
                .child(Variables.roomUsers).child(
                (getSharedPreference(
                    activity
                ).getString(Variables.U_ID, ""))!!
            ).removeEventListener(currentJoinRoomListener!!)
        }
        if (reference != null && speakInvitationListener != null) {
            reference!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!).child(Variables.roomInvitation)
                .child(getSharedPreference(this.activity).getString(Variables.U_ID, "")!!)
                .removeEventListener(speakInvitationListener!!)
        }


        myRoomListener = null
        roomUserUpdateListener = null
        currentJoinRoomListener = null
        speakInvitationListener = null


        speakersUserList.clear()
        audienceUserList.clear()


        mainStreamingModel = null
        myUserModel = null
    }


    fun removeMainListener() {
        if (reference != null && myjoinListener != null) {
            reference!!.child(Variables.joinedKey).child(
                (getSharedPreference(
                    activity
                ).getString(Variables.U_ID, ""))!!
            ).removeEventListener(myjoinListener!!)
        }


        myjoinListener = null

        INSTANCE = null
    }


    override fun createRoom(bundle: Bundle?) {
        if (listerner1 != null) listerner1!!.createRoom(bundle)

        if (listerner2 != null) listerner2!!.createRoom(bundle)

        if (listerner3 != null) listerner3!!.createRoom(bundle)
    }

    override fun JoinedRoom(bundle: Bundle?) {
        if (listerner1 != null) listerner1!!.JoinedRoom(bundle)

        if (listerner2 != null) listerner2!!.JoinedRoom(bundle)

        if (listerner3 != null) listerner3!!.JoinedRoom(bundle)
    }

    override fun onRoomLeave(bundle: Bundle?) {
        if (listerner1 != null) listerner1!!.onRoomLeave(bundle)

        if (listerner2 != null) listerner2!!.onRoomLeave(bundle)

        if (listerner3 != null) listerner3!!.onRoomLeave(bundle)
    }

    override fun onRoomDelete(bundle: Bundle?) {
        if (listerner1 != null) listerner1!!.onRoomDelete(bundle)

        if (listerner2 != null) listerner2!!.onRoomDelete(bundle)

        if (listerner3 != null) listerner3!!.onRoomDelete(bundle)
    }

    override fun onRoomUpdate(bundle: Bundle?) {
        if (listerner1 != null) listerner1!!.onRoomUpdate(bundle)

        if (listerner2 != null) listerner2!!.onRoomUpdate(bundle)

        if (listerner3 != null) listerner3!!.onRoomUpdate(bundle)
    }

    override fun onRoomUsersUpdate(bundle: Bundle?) {
        if (listerner1 != null) listerner1!!.onRoomUsersUpdate(bundle)

        if (listerner2 != null) listerner2!!.onRoomUsersUpdate(bundle)

        if (listerner3 != null) listerner3!!.onRoomUsersUpdate(bundle)
    }

    override fun onMyUserUpdate(bundle: Bundle?) {
        if (listerner1 != null) listerner1!!.onMyUserUpdate(bundle)

        if (listerner2 != null) listerner2!!.onMyUserUpdate(bundle)

        if (listerner3 != null) listerner3!!.onMyUserUpdate(bundle)
    }

    override fun onSpeakInvitationReceived(bundle: Bundle?) {
        if (listerner1 != null) listerner1!!.onSpeakInvitationReceived(bundle)

        if (listerner2 != null) listerner2!!.onSpeakInvitationReceived(bundle)

        if (listerner3 != null) listerner3!!.onSpeakInvitationReceived(bundle)
    }

    override fun onWaveUserUpdate(bundle: Bundle?) {
        if (listerner1 != null) listerner1!!.onWaveUserUpdate(bundle)

        if (listerner2 != null) listerner2!!.onWaveUserUpdate(bundle)

        if (listerner3 != null) listerner3!!.onWaveUserUpdate(bundle)
    }


    companion object {
        @Volatile
        private var INSTANCE: RoomFirebaseManager? = null

        fun getInstance(activity: Activity): RoomFirebaseManager? {
            if (INSTANCE == null) {
                synchronized(RoomFirebaseManager::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = RoomFirebaseManager(activity)
                    }
                }
            }
            return INSTANCE
        }
    }
}
