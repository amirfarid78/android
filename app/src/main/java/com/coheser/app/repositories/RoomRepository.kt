package com.coheser.app.repositories

import androidx.lifecycle.MutableLiveData
import com.coheser.app.activitesfragments.spaces.models.HomeUserModel
import com.coheser.app.activitesfragments.spaces.models.RoomModel
import com.coheser.app.activitesfragments.spaces.models.TopicModel
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.DataParsing.getTopicDataModel
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import org.json.JSONObject

class RoomRepository : BaseRepository<UserModel>() {

    suspend fun getRoomList(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<RoomModel>>>,
    ) {

        try {

        val endpoint = { requestBody: String ->
            apiInterface.showRooms(requestBody)
        }
        val parseData = { apiResponseData: ApiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray -> {
                    val list = ArrayList<RoomModel>()
                    for (i in 0 until apiResponseData.data.length()) {
                        val `object` = apiResponseData.data.optJSONObject(i)

                        val roomObj = `object`.optJSONObject("Room")
                        val topicobject = `object`.optJSONObject("Topic")
                        val roomMemberArray = `object`.optJSONArray("RoomMember")

                        val model = RoomModel()
                        model.id = roomObj.optString("id")
                        model.adminId = roomObj.optString("user_id")
                        model.title = roomObj.optString("title")
                        model.privacyType = roomObj.optString("privacy")

                        val topicList = java.util.ArrayList<TopicModel>()
                        val topicModel = getTopicDataModel(topicobject)
                        topicList.add(topicModel)
                        model.topicModels = topicList

                        val userList = java.util.ArrayList<HomeUserModel>()
                        for (j in 0 until roomMemberArray.length()) {
                            val innerObj = roomMemberArray.getJSONObject(j)
                            val userModel = getUserDataModel(innerObj.optJSONObject("User"))

                            val userItemModel = HomeUserModel()
                            userItemModel.userModel = userModel
                            userItemModel.userRoleType = innerObj.optString("moderator")
                            userList.add(userItemModel)
                        }
                        model.userList = userList

                        list.add(model)
                    }
                    list
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }

}