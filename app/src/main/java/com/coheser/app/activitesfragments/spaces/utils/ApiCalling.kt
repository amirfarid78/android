package com.coheser.app.activitesfragments.spaces.utils

import android.app.Activity
import android.util.Log
import com.coheser.app.Constants
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.APICallBack
import org.json.JSONObject

object ApiCalling {
    fun createRoomBYUserId(activity: Activity?, params: JSONObject?, apiCallBack: APICallBack) {
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.addRoom,
            params,
            getHeaders(activity)
        ) { resp ->
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")

                if (code == "200") {
                    apiCallBack.onSuccess(resp)
                } else {
                    apiCallBack.onFail(jsonObject.optString("msg"))
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }
        }
    }


    fun inviteMembersIntoRoom(activity: Activity?, params: JSONObject?, apiCallBack: APICallBack) {
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.inviteUserToRoom,
            params,
            getHeaders(activity)
        ) { resp ->
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")

                if (code == "200") {
                    apiCallBack.onSuccess(resp)
                } else {
                    apiCallBack.onFail(jsonObject.optString("msg"))
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }
        }
    }


    fun leaveRoom(activity: Activity?, params: JSONObject?, apiCallBack: APICallBack) {
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.leaveRoom,
            params,
            getHeaders(activity)
        ) { resp ->
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")

                if (code == "200") {
                    apiCallBack.onSuccess(resp)
                } else {
                    apiCallBack.onFail(jsonObject.optString("msg"))
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }
        }
    }

    fun deleteRoom(activity: Activity?, params: JSONObject?, apiCallBack: APICallBack) {
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.deleteRoom,
            params,
            getHeaders(activity)
        ) { resp ->
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")

                if (code == "200") {
                    apiCallBack.onSuccess(resp)
                } else {
                    apiCallBack.onFail(jsonObject.optString("msg"))
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }
        }
    }


    fun checkMyRoomJoinStatus(activity: Activity?, params: JSONObject?, apiCallBack: APICallBack) {
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.showUserJoinedRooms,
            params,
            getHeaders(activity)
        ) { resp ->
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")

                if (code == "200") {
                    apiCallBack.onSuccess(resp)
                } else {
                    apiCallBack.onFail(jsonObject.optString("msg"))
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }
        }
    }


    fun showRoomDetail(activity: Activity?, params: JSONObject?, apiCallBack: APICallBack) {
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.showRoomDetail,
            params,
            getHeaders(activity)
        ) { resp ->
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")

                if (code == "200") {
                    apiCallBack.onSuccess(resp)
                } else {
                    apiCallBack.onFail(jsonObject.optString("msg"))
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }
        }
    }
}
