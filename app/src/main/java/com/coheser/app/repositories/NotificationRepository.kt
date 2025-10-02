package com.coheser.app.repositories

import androidx.lifecycle.MutableLiveData
import com.coheser.app.Constants
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.NotificationModel
import com.coheser.app.models.StoryModel
import com.coheser.app.models.StoryVideoModel
import com.coheser.app.simpleclasses.DataParsing
import com.coheser.app.simpleclasses.DataParsing.getVideoDataModel
import com.coheser.app.simpleclasses.Functions
import org.json.JSONObject

class NotificationRepository : BaseRepository<NotificationModel>(){

   suspend fun showAllNotifications(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<NotificationModel>>>,
    ) {

       try {


       val endpoint = { requestBody: String ->
           apiInterface.showAllNotifications(requestBody)
       }
       val parseData = { apiResponseData: ApiResponseData ->
           when (apiResponseData) {
               is ApiResponseData.JsonArray -> {
                   val list = ArrayList<NotificationModel>()
                   for (i in 0 until apiResponseData.data.length()) {
                       val data = apiResponseData.data.getJSONObject(i)
                       val notification = data.optJSONObject("Notification")
                       val video = data.optJSONObject("Video")
                       val senderUserDetailModel =
                           DataParsing.getUserDataModel(data.optJSONObject("Sender"))
                       val receiverUserDetailModel =
                           DataParsing.getUserDataModel(data.optJSONObject("Receiver"))
                       val item = NotificationModel()
                       item.senderModel=senderUserDetailModel
                       item.id = notification.optString("id")
                       item.status = notification.optString("status", "0")
                       item.live_streaming_id = notification.optString("live_streaming_id", "")
                       item.effected_fb_id = receiverUserDetailModel.id
                       item.type = notification.optString("type")
                       item.order_id = notification.optString("order_id")
                       if (item.type.equals(
                               "video_comment",
                               ignoreCase = true
                           ) || item.type.equals(
                               "comment_like",
                               ignoreCase = true
                           ) || item.type.equals(
                               "video_like",
                               ignoreCase = true
                           ) || item.type.equals("video_updates", ignoreCase = true)
                       ) {
                           item.video_id = video.optString("id")
                           item.video = video.optString("video")
                           item.thum = video.optString("thum")
                           item.gif = video.optString("gif")
                       }
                       item.string = notification.optString("string")
                       item.created = notification.optString("created")
                       list.add(item)
                   }
                   list

               }

               else -> throw IllegalArgumentException("Unsupported response type")
           }
       }

       makeApiCall(params, endpoint, liveData, parseData)
       }catch (e:Exception){}
    }


    suspend fun callApiAllStory(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<StoryModel>>>,
    ) {

        try {
            val endpoint = { requestBody: String ->
                apiInterface.showFriendsStories(requestBody)
            }
            val parseData = { apiResponseData: ApiResponseData ->
                when (apiResponseData) {
                    is ApiResponseData.JsonObject -> {
                        val list = ArrayList<StoryModel>()
                        val msgObj = apiResponseData.data
                        val myUserArray = msgObj.getJSONArray("User")
                        for (i in 0 until myUserArray.length()) {
                            val data = myUserArray.getJSONObject(i)

                            val userVideoList = ArrayList<StoryVideoModel>()
                            val storyItem = StoryModel()
                            storyItem.id=data.optString("id")
                            storyItem.username=data.optString("username")
                            storyItem.setProfilePic(data.optString("profile_pic"))
                            val storyArray = data.getJSONArray("Video")
                            for (j in 0 until storyArray.length()) {
                                val itemObj = storyArray.getJSONObject(j)
                                val videoItem = getVideoDataModel(itemObj.optJSONObject("Video"))
                                userVideoList.add(videoItem)
                            }
                            storyItem.videoList = userVideoList
                            if (userVideoList.size > 0) {
                                list.add(storyItem)
                            }
                        }
                        Functions.printLog(Constants.tag,"story size"+list.size)
                        list

                    }

                    else -> throw IllegalArgumentException("Unsupported response type")
                }
            }

            makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }



    suspend fun readNotification(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.readNotification(requestBody)
        }
        val parseData = { apiResponseData: ApiResponseData ->
            when (apiResponseData) {

                is ApiResponseData.JsonArray -> {
                    apiResponseData.data.toString()
                }
                is ApiResponseData.JsonObject -> {
                    apiResponseData.data.toString()
                }
                is ApiResponseData.JsonString -> {
                    apiResponseData.data.toString()
                }
                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }



}