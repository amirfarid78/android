package com.coheser.app.simpleclasses

import android.app.Activity
import android.util.Log
import com.coheser.app.BuildConfig
import com.coheser.app.Constants
import com.coheser.activitiesfragments.accounts.model.Interest
import com.coheser.activitiesfragments.accounts.model.InterestModel
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.models.CommentModel
import com.coheser.app.models.UserModel
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.APICallBack
import com.volley.plus.interfaces.Callback
import io.paperdb.Paper
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object ApiRepository {
    fun callShowInterest(context:Activity,apiCallBack:APICallBack) {
        val parameters = JSONObject()
        val auth=Functions.getSharedPreference(context).getString(Variables.AUTH_TOKEN, "")
        if(Functions.isStringHasValue(auth)) {
            parameters.put("auth_token", auth)
        }

        VolleyRequest.JsonPostRequest(context,
            ApiLinks.showInterestSection,
            parameters,
            Functions.getHeaders(context),
            Callback { resp ->

                val arrayList:ArrayList<InterestModel> = ArrayList()

                try {
                    val jsonObj = JSONObject(resp)
                    val code = jsonObj.optString("code")
                    if (code.equals("200")) {
                        val msgArray = jsonObj.getJSONArray("msg")
                        for (i in 0 until msgArray.length()) {
                            val itemdata = msgArray.optJSONObject(i)
                            val interestSectionObj = itemdata.optJSONObject("InterestSection")
                            val secTitle = interestSectionObj.optString("title")

                            val interestArray = itemdata.optJSONArray("Interest")
                            val interestList = ArrayList<Interest>()
                            for (j in 0 until interestArray.length()) {
                                val interestJson = interestArray.optJSONObject(j)
                                val interest = DataParsing.getInterestDataModel(interestJson)
                                interestList.add(interest)
                            }

                            val interestModel = InterestModel().apply {
                                this.secTitle= secTitle
                                this.userIntrest = interestList
                            }

                            arrayList.add(interestModel)

                        }
                        Paper.book().write(Variables.Interests,arrayList)

                    }
                } catch (e: Exception) {
                    Log.d(Constants.tag, "Exception : $e")
                }
                finally {
                    apiCallBack.arrayData(arrayList)
                }


            })
    }


    fun callApiForLikeVideo(
        activity: Activity?,
        video_id: String?, action: String?,
        api_callBack: APICallBack?
    ) {
        val parameters = JSONObject()
        try {
            parameters.put("video_id", video_id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.likeVideo,
            parameters,
            Functions.getHeaders(activity)
        ) { resp ->
            Functions.checkStatus(activity, resp)
            api_callBack?.onSuccess(resp)
        }
    }

    fun callApiForFavouriteVideo(
        activity: Activity?,
        video_id: String?, action: String?,
        api_callBack: APICallBack?
    ) {
        val parameters = JSONObject()
        try {
            parameters.put("video_id", video_id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.addVideoFavourite,
            parameters,
            Functions.getHeaders(activity)
        ) { resp ->
            Functions.checkStatus(activity, resp)
            api_callBack?.onSuccess(resp)
        }
    }

    // this method will like the comment
    fun callApiForLikeComment(
        activity: Activity?,
        video_id: String?,
        api_callBack: APICallBack?
    ) {
        val parameters = JSONObject()
        try {

            parameters.put("comment_id", video_id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.likeComment,
            parameters,
            Functions.getHeaders(activity)
        ) { resp ->
            Functions.checkStatus(activity, resp)
            api_callBack?.onSuccess(resp)
        }
    }

    // this method will like the reply comment
    fun callApiForLikeCommentReply(
        activity: Activity?,
        comment_reply_id: String?,
        video_id: String?,
        api_callBack: APICallBack?
    ) {
        val parameters = JSONObject()
        try {
            parameters.put("comment_id", comment_reply_id)
            parameters.put("video_id", video_id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.likeComment,
            parameters,
            Functions.getHeaders(activity)
        ) { resp ->
            Functions.checkStatus(activity, resp)
            Functions.printLog(Constants.tag, "resp at like comment reply : $resp")
            api_callBack?.onSuccess(resp)
        }
    }

    fun callApiForSendComment(
        activity: Activity,
        videoId: String,
        comment: String,
        taggedUserList: ArrayList<UserModel>,
        api_callBack: APICallBack
    ) {
        val parameters = JSONObject()
        try {
            parameters.put("video_id", videoId)
            parameters.put("comment", comment)
            val tagUserArray = JSONArray()
            for (item in taggedUserList) {
                if (comment!!.contains("@" + item.username)) {
                    val tagUser = JSONObject()
                    tagUser.put("user_id", item.id)
                    tagUserArray.put(tagUser)
                }
            }
            parameters.put("users_json", tagUserArray)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.postCommentOnVideo,
            parameters,
            Functions.getHeaders(activity)
        ) { resp ->
            Functions.checkStatus(activity, resp)
            val arrayList = ArrayList<CommentModel>()
            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if (code == "200") {
                    val msg = response.optJSONObject("msg")
                    val videoComment = msg?.optJSONObject("VideoComment")
                    val videoObj = msg?.optJSONObject("Video")
                    val userDetailModel = DataParsing.getUserDataModel(msg?.optJSONObject("User"))
                    val item = CommentModel()
                    item.isLikedByOwner = videoComment?.optString("owner_like")
                    item.videoOwnerId = videoObj?.optString("user_id")
                    item.pin_comment_id = videoObj?.optString("pin_comment_id")
                    item.userId = userDetailModel.id
                    item.isVerified = userDetailModel.verified
                    item.user_name = userDetailModel.username
                    item.first_name = userDetailModel.first_name
                    item.last_name = userDetailModel.last_name
                    item.profile_pic = userDetailModel.getProfilePic()
                    item.arrayList = ArrayList()
                    item.arraylist_size = "0"
                    item.video_id = videoComment?.optString("video_id")
                    item.comments = videoComment?.optString("comment")
                    item.liked = videoComment?.optString("like")
                    item.like_count = videoComment?.optString("like_count")
                    item.comment_id = videoComment?.optString("id")
                    item.created = videoComment?.optString("created")
                    arrayList.add(item)
                    api_callBack.arrayData(arrayList)
                }
                else {
                    Functions.showToast(activity, "" + response.optString("msg"))
                }
            } catch (e: Exception) {
                api_callBack.onFail(e.toString())
                e.printStackTrace()
            }
        }
    }

    // this method will send the reply to the comment of the video
    // this method will send the reply to the comment of the video
    fun callApiForSendCommentReply(
        activity: Activity?,
        commentId: String,
        comment: String,
        videoId: String?,
        videoOwnerId: String?,
        taggedUserList: ArrayList<UserModel>,
        api_callBack: APICallBack
    ) {
        val parameters = JSONObject()
        try {
            parameters.put("parent_id", "" + commentId)
            parameters.put(
                "user_id",
                "" + Functions.getSharedPreference(activity).getString(Variables.U_ID, "0")
            )
            parameters.put("comment", "" + comment)
            parameters.put("video_id", "" + videoId)
            val tagUserArray = JSONArray()
            for (item in taggedUserList) {
                if (comment.contains("@" + item.username)) {
                    val tagUser = JSONObject()
                    tagUser.put("user_id", item.id)
                    tagUserArray.put(tagUser)
                }
            }
            parameters.put("users_json", tagUserArray)
            Functions.printLog(Constants.tag, "parameters at reply : $parameters")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.postCommentOnVideo,
            parameters,
            Functions.getHeaders(activity)
        ) { resp ->
            Functions.checkStatus(activity, resp)
            val arrayList = ArrayList<CommentModel>()
            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if (code == "200") {
                    val msg = response.optJSONObject("msg")
                    val videoComment = msg?.optJSONObject("VideoComment")
                    val userDetailModel = DataParsing.getUserDataModel(msg?.optJSONObject("User"))
                    val item = CommentModel()
                    item.userId = userDetailModel.id
                    item.isVerified = userDetailModel.verified
                    item.isLikedByOwner = videoComment?.optString("owner_like")
                    item.videoOwnerId = videoOwnerId
                    item.pin_comment_id = "0"
                    item.first_name = userDetailModel.first_name
                    item.last_name = userDetailModel.last_name
                    item.replay_user_name = userDetailModel.username
                    item.replay_user_url = userDetailModel.getProfilePic()
                    item.video_id = videoComment?.optString("video_id")
                    item.comments = videoComment?.optString("comment")
                    item.created = videoComment?.optString("created")
                    item.comment_reply_id = videoComment?.optString("id")
                    item.comment_reply = videoComment?.optString("comment")
                    item.parent_comment_id = videoComment?.optString("parent_id")
                    item.reply_create_date = videoComment?.optString("created")
                    item.reply_liked_count = "0"
                    item.comment_reply_liked = "0"
                    arrayList.add(item)
                    item.item_count_replies = "1"
                    api_callBack.arrayData(arrayList)
                } else {
                    Functions.showToast(activity, "" + response.optString("msg"))
                }
            } catch (e: Exception) {
                api_callBack.onFail(e.toString())
                e.printStackTrace()
            }
        }
    }

    fun callApiForUpdateView(
        activity: Activity,
        video_id: String, percentage: Int
    ) {
        val jsonArray = Paper.book().read(Variables.watchVideoList, JSONArray())
        val jsonObject = JSONObject()
        try {
            jsonObject.put("video_id", video_id)
            jsonObject.put("duration", percentage)
            jsonArray?.put(jsonObject)
        } catch (e: JSONException) {
            Functions.printLog(Constants.tag,e.toString())
        }
        if (jsonArray == null) {
            return
        }

        if (jsonArray.length() < 3) {
            Paper.book().write(Variables.watchVideoList, jsonArray)
        } else {
            Paper.book().write(Variables.watchVideoList, JSONArray())
            val parameters = JSONObject()
            try {

                parameters.put(
                    "user_id",
                    Functions.getSharedPreference(activity).getString(Variables.U_ID, "")
                )
                parameters.put("watch_videos", jsonArray)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            VolleyRequest.JsonPostRequest(
                activity,
                ApiLinks.watchVideo,
                parameters,
                Functions.getHeaders(activity),
                null
            )
        }
    }



    @JvmStatic
    fun callApiForFollowUnFollow(
        activity: Activity?,
        fbId: String?,
        followedFbId: String?,
        api_callBack: APICallBack
    ) {
        val parameters = JSONObject()
        try {
            parameters.put("sender_id", fbId)
            parameters.put("receiver_id", followedFbId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.followUser,
            parameters,
            Functions.getHeaders(activity)
        ) { resp ->
            Functions.checkStatus(activity, resp)
            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if (code == "200") {
                    api_callBack.onSuccess(response.toString())
                    val msg = response.optJSONObject("msg")
                    val receiver = msg?.optJSONObject("User")
                    val receiverDetailModel = DataParsing.getUserDataModel(receiver)
                    if (Variables.followMapList.containsKey(receiverDetailModel.id)) {
                        val status = receiverDetailModel.button
                        if (status.equals("following", ignoreCase = true)) {
                            Variables.followMapList[receiverDetailModel.id] = status
                        } else if (status.equals("friends", ignoreCase = true)) {
                            Variables.followMapList[receiverDetailModel.id] = status
                        } else if (status.equals("follow back", ignoreCase = true)) {
                            Variables.followMapList.remove(receiverDetailModel.id)
                        } else {
                            Variables.followMapList.remove(receiverDetailModel.id)
                        }
                    } else {
                        Variables.followMapList[receiverDetailModel.id] = receiverDetailModel.button
                    }
                } else {
                    Functions.showToast(activity, "" + response.optString("msg"))
                }
            } catch (e: Exception) {
                api_callBack.onFail(e.toString())
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun callApiForGetUserData(
        activity: Activity,
        fbId: String,
        api_callBack: APICallBack
    ) {
        val parameters = JSONObject()
        try {
            if (Functions.getSharedPreference(activity).getBoolean(Variables.IS_LOGIN, false) && fbId != null) {
                parameters.put(
                    "user_id",
                    Functions.getSharedPreference(activity).getString(Variables.U_ID, "")
                )
                parameters.put("other_user_id", fbId)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        Functions.printLog("resp", parameters.toString())
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.showUserDetail,
            parameters,
            Functions.getHeaders(activity)
        ) { resp ->
            Functions.checkStatus(activity, resp)
            Functions.cancelLoader()
            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if (code == "200") {
                    api_callBack.onSuccess(response.toString())
                } else {
                    Functions.showToast(activity, "" + response.optString("msg"))
                }
            } catch (e: Exception) {
                api_callBack.onFail(e.toString())
                e.printStackTrace()
            }
        }
    }


    fun callApiForDeleteVideo(
        activity: Activity?,
        videoId: String?,
        api_callBack: APICallBack?
    ) {
        val parameters = JSONObject()
        try {
            parameters.put("video_id", videoId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.deleteVideo,
            parameters,
            Functions.getHeaders(activity)
        ) { resp ->
            Functions.checkStatus(activity, resp)
            Functions.cancelLoader()
            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if (code == "200") {
                    api_callBack?.onSuccess(response.toString())
                } else {
                    Functions.showToast(activity, "" + response.optString("msg"))
                }
            } catch (e: Exception) {
                api_callBack?.onFail(e.toString())
                e.printStackTrace()
            }
        }
    }



    fun addDeviceData(context: Activity) {
        val headers = JSONObject()
        try {
            headers.put("user_id", Functions.getSharedPreference(context)
                .getString(Variables.U_ID, null))
            headers.put("device", "android")
            headers.put("lat", Functions.getSettingsPreference(context)
                .getString(Variables.DEVICE_LAT, "0.0"))
            headers.put("long", Functions.getSettingsPreference(context)
                .getString(Variables.DEVICE_LNG, "0.0"))
            headers.put("version", BuildConfig.VERSION_NAME)
            headers.put("ip", Functions.getSharedPreference(context)
                .getString(Variables.DEVICE_IP, null))
            headers.put(
                "device_token",
                Functions.getSharedPreference(context).getString(Variables.DEVICE_TOKEN, null)
            )
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
        }
        VolleyRequest.JsonPostRequest(
            context,
            ApiLinks.addDeviceData,
            headers,
            Functions.getHeaders(context)
        ) { resp -> Functions.checkStatus(context, resp) }
    }




}