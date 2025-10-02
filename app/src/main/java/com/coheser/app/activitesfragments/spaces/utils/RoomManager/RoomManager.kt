package com.coheser.app.activitesfragments.spaces.utils.RoomManager

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.spaces.models.HomeUserModel
import com.coheser.app.activitesfragments.spaces.models.TopicModel
import com.coheser.app.activitesfragments.spaces.utils.ApiCalling
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.Dialogs.showError
import com.coheser.app.simpleclasses.Dialogs.showSuccess
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.APICallBack
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class RoomManager(var activity: Activity) {
    var roomApisListener: RoomApisListener? = null
    var model: MainStreamingModel? = null

    @JvmField
    var roomName: String? = null
    @JvmField
    var privacyType: String? = null
    @JvmField
    var selectedInviteFriends: ArrayList<UserModel>? = null

    @JvmField
    var selectedTopics: ArrayList<TopicModel>? = null


    fun addResponseListener(responseListener: RoomApisListener?) {
        this.roomApisListener = responseListener
    }


    fun createRoomBYUserId() {
        val parameters = JSONObject()
        try {
            parameters.put(
                "user_id", getSharedPreference(
                    activity
                ).getString(Variables.U_ID, "")
            )
            parameters.put("title", roomName)
            parameters.put("privacy", privacyType)
            if (selectedTopics != null && !selectedTopics!!.isEmpty()) parameters.put(
                "topic_id",
                selectedTopics!![0].id
            )
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }

        showLoader(activity, false, false)
        ApiCalling.createRoomBYUserId(activity, parameters, object : APICallBack {
            override fun arrayData(arrayList: ArrayList<*>?) {
            }

            override fun onSuccess(responce: String) {
                cancelLoader()
                try {
                    val jsonObject = JSONObject(responce)
                    val msgObj = jsonObject.getJSONObject("msg")

                    val roomObj = msgObj.getJSONObject("Room")
                    val roomMemberArray = msgObj.getJSONArray("RoomMember")

                    model = MainStreamingModel()
                    val streamModel = StreamModel()

                    streamModel.id = roomObj.optString("id")
                    streamModel.adminId = roomObj.optString("user_id")
                    streamModel.title = roomObj.optString("title")
                    streamModel.privacyType = roomObj.optString("privacy")
                    streamModel.created = roomObj.optString("created")

                    val userList = ArrayList<HomeUserModel>()
                    for (j in 0 until roomMemberArray.length()) {
                        val innerObj = roomMemberArray.getJSONObject(j)
                        val userModel = getUserDataModel(innerObj.getJSONObject("User"))

                        val userItemModel = HomeUserModel()
                        userItemModel.userModel = userModel
                        userItemModel.mice = "1"
                        userItemModel.online = "1"
                        userItemModel.userRoleType = innerObj.optString("moderator")
                        userList.add(userItemModel)
                    }
                    model!!.userList = userList
                    model!!.model = streamModel

                    if (roomApisListener != null) {
                        val bundle = Bundle()
                        bundle.putString("action", "roomCreated")
                        bundle.putParcelable("model", model)
                        roomApisListener!!.roomCreated(bundle)
                    }
                } catch (e: Exception) {
                    Log.d(Constants.tag, "Exception : $e")
                }
            }

            override fun onFail(responce: String) {
                cancelLoader()
                showError(activity, responce)
            }
        })
    }


    fun inviteMembersIntoRoom(userId: String?, selectedInviteFriends: ArrayList<UserModel>) {
        val parameters = JSONObject()
        try {
            parameters.put("sender_id", userId)
            parameters.put("room_id", model!!.model!!.id)
            val friendsArray = JSONArray()
            for (user in selectedInviteFriends) {
                val userObj = JSONObject()
                userObj.put("receiver_id", user.id)
                friendsArray.put(userObj)
            }
            parameters.put("receivers", friendsArray)
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }


        ApiCalling.inviteMembersIntoRoom(activity, parameters, object : APICallBack {
            override fun arrayData(arrayList: ArrayList<*>?) {
            }

            override fun onSuccess(responce: String) {
                if (roomApisListener != null) {
                    val bundle = Bundle()
                    bundle.putString("action", "roomInvitationSended")
                    roomApisListener!!.roomInvitationsSended(bundle)
                }
            }

            override fun onFail(responce: String) {
                cancelLoader()
                showError(activity, responce)
            }
        })
    }


    fun joinRoom(userid: String?, roomId: String?, moderator: String?) {
        val parameters = JSONObject()
        try {
            parameters.put("user_id", userid)
            parameters.put("room_id", roomId)
            parameters.put("moderator", moderator)
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }

        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.joinRoom, parameters, getHeaders(
                activity
            )
        ) { resp ->
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code == "200") {
                    val msgObj = jsonObject.getJSONObject("msg")
                    val roomObj = msgObj.getJSONObject("RoomMember")
                    val userModel = getUserDataModel(msgObj.getJSONObject("User"))
                    val myUserModel = HomeUserModel()
                    myUserModel.online = "1"
                    myUserModel.userModel = userModel
                    myUserModel.userRoleType = roomObj.optString("moderator")

                    val bundle = Bundle()
                    bundle.putParcelable("model", myUserModel)
                    bundle.putString("roomId", roomId)
                    roomApisListener!!.onRoomJoined(bundle)
                } else {
                    showError(activity, jsonObject.optString("msg"))
                }
            } catch (e: JSONException) {
                Log.d(Constants.tag, "Exception : $e")
            }
        }
    }


    fun leaveRoom(roomId: String?) {
        val parameters = JSONObject()
        try {
            parameters.put(
                "user_id", getSharedPreference(
                    activity
                ).getString(Variables.U_ID, "")
            )
            parameters.put("room_id", roomId)
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }


        ApiCalling.leaveRoom(activity, parameters, object : APICallBack {
            override fun arrayData(arrayList: ArrayList<*>?) {
            }

            override fun onSuccess(responce: String) {
                if (roomApisListener != null) {
                    val bundle = Bundle()
                    bundle.putString("roomId", roomId)
                    roomApisListener!!.onRoomLeave(bundle)
                }
            }

            override fun onFail(responce: String) {
                cancelLoader()
                showError(activity, responce)
            }
        })
    }

    fun deleteRoom(roomId: String?) {
        val parameters = JSONObject()
        try {
            parameters.put(
                "user_id", getSharedPreference(
                    activity
                ).getString(Variables.U_ID, "")
            )
            parameters.put("id", roomId)
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }


        ApiCalling.deleteRoom(activity, parameters, object : APICallBack {
            override fun arrayData(arrayList: ArrayList<*>?) {
            }

            override fun onSuccess(responce: String) {
                if (roomApisListener != null) {
                    val bundle = Bundle()
                    bundle.putString("roomId", roomId)
                    roomApisListener!!.onRoomDelete(bundle)
                }
            }

            override fun onFail(responce: String) {
                cancelLoader()
                showError(activity, responce)
            }
        })
    }


    fun showRoomDetailAfterJoin(roomId: String?) {
        val parameters = JSONObject()
        try {
            parameters.put(
                "user_id", getSharedPreference(
                    activity
                ).getString(Variables.U_ID, "")
            )
            parameters.put("room_id", roomId)
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }
        ApiCalling.showRoomDetail(activity, parameters, object : APICallBack {
            override fun arrayData(arrayList: ArrayList<*>?) {
            }

            override fun onSuccess(responce: String) {
                try {
                    val jsonObject = JSONObject(responce)
                    val msgObj = jsonObject.optJSONObject("msg")
                    val roomObj = msgObj.optJSONObject("Room")
                    val roomMemberArray = msgObj.optJSONArray("RoomMember")

                    model = MainStreamingModel()
                    val streamModel = StreamModel()

                    streamModel.id = roomObj.optString("id")
                    streamModel.adminId = roomObj.optString("user_id")
                    streamModel.title = roomObj.optString("title")
                    streamModel.privacyType = roomObj.optString("privacy")
                    streamModel.created = roomObj.optString("created")

                    val userList = ArrayList<HomeUserModel>()
                    for (j in 0 until roomMemberArray.length()) {
                        val innerObj = roomMemberArray.optJSONObject(j)
                        val userModel = getUserDataModel(innerObj.optJSONObject("User"))

                        val userItemModel = HomeUserModel()
                        userItemModel.userModel = userModel
                        userItemModel.userRoleType = innerObj.optString("moderator")
                        userList.add(userItemModel)
                    }
                    model!!.userList = userList
                    model!!.model = streamModel

                    if (roomApisListener != null) {
                        val bundle = Bundle()
                        bundle.putString("action", "showRoomDetailAfterJoin")
                        bundle.putParcelable("model", model)
                        roomApisListener!!.showRoomDetailAfterJoin(bundle)
                    }
                } catch (e: JSONException) {
                    Log.d(Constants.tag, "Exception : $e")
                }
            }

            override fun onFail(responce: String) {
                showError(activity, responce)
            }
        })
    }


    //need this function before create room or join room
    var roomJoinStatusList: ArrayList<RoomJoinStatusModel> = ArrayList()

    fun checkMyRoomJoinStatus(action: String, roomId: String) {
        val parameters = JSONObject()
        try {
            parameters.put(
                "user_id", getSharedPreference(
                    activity
                ).getString(Variables.U_ID, "")
            )
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }

        ApiCalling.checkMyRoomJoinStatus(activity, parameters, object : APICallBack {
            override fun arrayData(arrayList: ArrayList<*>?) {
            }

            override fun onSuccess(responce: String) {
                try {
                    val resObj = JSONObject(responce)
                    val msgArray = resObj.getJSONArray("msg")

                    roomJoinStatusList.clear()
                    for (m in 0 until msgArray.length()) {
                        val mainObj = msgArray.getJSONObject(m)
                        val joinStatusModel = RoomJoinStatusModel()

                        val roomObj = mainObj.getJSONObject("Room")
                        val moderatorsArray = roomObj.getJSONArray("Moderators")
                        val moderatorList = ArrayList<HomeUserModel>()
                        for (i in 0 until moderatorsArray.length()) {
                            val innerObj = moderatorsArray.getJSONObject(i)
                            val user = getUserDataModel(innerObj.getJSONObject("User"))

                            val userModel = HomeUserModel()
                            userModel.online = "1"
                            userModel.userModel = user
                            userModel.userRoleType =
                                innerObj.getJSONObject("RoomMember").optString("moderator")
                            userModel.mice = ""
                            userModel.riseHand = ""

                            moderatorList.add(userModel)
                        }

                        val myModel = HomeUserModel()
                        myModel.userModel = getUserDataModel(mainObj.getJSONObject("User"))
                        myModel.userRoleType =
                            mainObj.getJSONObject("RoomMember").optString("moderator")
                        myModel.mice = ""
                        myModel.riseHand = ""
                        myModel.online = "1"

                        joinStatusModel.myModel = myModel
                        joinStatusModel.roomId = roomObj.optString("id")
                        joinStatusModel.userList = moderatorList
                        roomJoinStatusList.add(joinStatusModel)
                    }

                    if (action.equals("join", ignoreCase = true)) {
                        performActionAgainstRoomJoin(roomId)
                    } else if (action.equals("create", ignoreCase = true)) {
                        performActionAgainstRoomGenrate()
                    }
                } catch (e: Exception) {
                    Log.d(Constants.tag, "Exception : $e")
                }
            }

            override fun onFail(responce: String) {
                if (action.equals("join", ignoreCase = true)) {
                    joinRoomResponce(roomId)
                } else if (action.equals("create", ignoreCase = true)) {
                    genrateRoomResponce()
                }
            }
        })
    }


    private fun performActionAgainstRoomJoin(roomId: String) {
        var matchedRoom: RoomJoinStatusModel? = null
        if (roomJoinStatusList.size > 0) {
            for (i in roomJoinStatusList.indices) {
                val model = roomJoinStatusList[i]

                if (model.roomId == "" + roomId) {
                    matchedRoom = model
                } else {
                    val myRole = model.myModel!!.userRoleType
                    if (myRole == "1") {
                        if (model.userList!!.size > 1) {
                            leaveRoomResponce(model.roomId)
                            roomJoinStatusList.removeAt(i)
                        } else {
                            deleteRoomResponce(model.roomId)
                            roomJoinStatusList.removeAt(i)
                        }
                    } else if (myRole == "2") {
                        leaveRoomResponce(model.roomId)
                        roomJoinStatusList.removeAt(i)
                    } else {
                        leaveRoomResponce(model.roomId)
                        roomJoinStatusList.removeAt(i)
                    }
                }
            }

            roomJoinStatusList.clear()
            if (matchedRoom?.roomId != null && matchedRoom.roomId == "" + roomId) {
                val bundle = Bundle()
                bundle.putParcelable("model", matchedRoom.myModel)
                bundle.putString("roomId", roomId)
                roomApisListener!!.onRoomReJoin(bundle)
            }
            else {
                joinRoomResponce(roomId)
            }

        } else {
            joinRoomResponce(roomId)
        }
    }

    private fun performActionAgainstRoomGenrate() {
        if (roomJoinStatusList.size > 0) {
            for (i in roomJoinStatusList.indices) {
                val model = roomJoinStatusList[i]
                val myRole = model.myModel!!.userRoleType
                if (myRole == "1") {
                    if (model.userList!!.size > 1) {
                        leaveRoomResponce(model.roomId)
                        roomJoinStatusList.removeAt(i)
                    } else {
                        deleteRoomResponce(model.roomId)
                        roomJoinStatusList.removeAt(i)
                    }
                } else if (myRole == "2") {
                    leaveRoomResponce(model.roomId)
                    roomJoinStatusList.removeAt(i)
                } else {
                    leaveRoomResponce(model.roomId)
                    roomJoinStatusList.removeAt(i)
                }
            }

            if (roomJoinStatusList.isEmpty()) {
                genrateRoomResponce()
            } else {
                performActionAgainstRoomGenrate()
            }
        } else {
            genrateRoomResponce()
        }
    }

    private fun leaveRoomResponce(roomId: String?) {
        val bundle = Bundle()
        bundle.putString("action", "leaveRoom")
        bundle.putString("roomId", roomId)
        roomApisListener!!.doRoomLeave(bundle)
    }

    private fun deleteRoomResponce(roomId: String?) {
        val bundle = Bundle()
        bundle.putString("action", "deleteRoom")
        bundle.putString("roomId", roomId)
        roomApisListener!!.doRoomDelete(bundle)
    }

    private fun genrateRoomResponce() {
        val bundle = Bundle()
        bundle.putString("action", "goAheadForRoomGenrate")
        bundle.putString("resp", "remove all and create new")
        roomApisListener!!.goAheadForRoomGenrate(bundle)
    }

    private fun joinRoomResponce(roomId: String) {
        val bundle = Bundle()
        bundle.putString("action", "goAheadForJoinRoom")
        bundle.putString("roomId", roomId)
        roomApisListener!!.goAheadForRoomJoin(bundle)
    }

    fun speakerJoinRoomHitApi(userId: String?, roomID: String?, userType: String) {
        val parameters = JSONObject()
        try {
            parameters.put("user_id", userId)
            parameters.put("room_id", roomID)
            parameters.put("moderator", userType)
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }


        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.assignModerator, parameters, getHeaders(
                activity
            )
        ) { resp ->
            try {
                if (userType == "1") {
                    showSuccess(
                        activity,
                        activity.getString(R.string.you_are_now_moderator_you_can_now_invite_other_speakers)
                    )
                } else if (userType == "0") {
                    showSuccess(
                        activity,
                        activity.getString(R.string.you_have_been_move_back_into_the_audience)
                    )
                }
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code == "200") {
                    val msgObj = jsonObject.getJSONObject("msg")
                    val roomObj = msgObj.getJSONObject("RoomMember")
                    val userModel = getUserDataModel(msgObj.getJSONObject("User"))
                    val myUserModel = HomeUserModel()
                    myUserModel.userModel = userModel
                    myUserModel.mice = "1"
                    myUserModel.online = "1"
                    myUserModel.userRoleType = roomObj.optString("moderator")

                    if (roomApisListener != null) {
                        val bundle = Bundle()
                        bundle.putString("action", "updateRoomMember")
                        bundle.putParcelable("model", myUserModel)
                        roomApisListener!!.onRoomMemberUpdate(bundle)
                    }
                } else {
                    showError(activity, jsonObject.optString("msg"))
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }
        }
    }

    fun checkRoomCanDeleteOrLeave(speakersUserList: ArrayList<HomeUserModel>): Bundle {
        var speakerAsModeratorModel: HomeUserModel? = null
        var myModel: HomeUserModel? = null
        var countModerator = 0
        var countSpeaker = 0
        val bundle = Bundle()

        try {
            for (moderatorModel in speakersUserList) {
                if (moderatorModel.userModel!!.id == getSharedPreference(
                        activity
                    ).getString(Variables.U_ID, "")
                ) {
                    myModel = moderatorModel
                }

                if (moderatorModel.userRoleType == "1") {
                    countModerator = countModerator + 1
                } else if (moderatorModel.userRoleType == "2") {
                    countSpeaker = countSpeaker + 1

                    if (countSpeaker == 1) {
                        speakerAsModeratorModel = moderatorModel
                    }
                }
            }
        }catch (e:Exception){

        }
        if (myModel == null) {
            bundle.putString("action", "leaveRoom")
            return bundle
        } else if (countModerator < 2) {
            if (countSpeaker < 1) {
                bundle.putString("action", "removeRoom")
                return bundle
            } else if (speakerAsModeratorModel != null) {
                bundle.putString("action", "leaveRoomAndAssign")
                bundle.putParcelable("model", speakerAsModeratorModel)
                return bundle
            } else {
                bundle.putString("action", "leaveRoom")
                return bundle
            }
        } else {
            bundle.putString("action", "leaveRoom")
            return bundle
        }
    }


    companion object {
        @Volatile
        private var INSTANCE: RoomManager? = null

        fun getInstance(activity: Activity): RoomManager? {
            if (INSTANCE == null) {
                synchronized(RoomManager::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = RoomManager(activity)
                    }
                }
            }
            return INSTANCE
        }
    }
}
