package com.coheser.app.repositories

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.coheser.app.Constants
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.PrivacySettingModel
import com.coheser.app.models.PushNotificationModel
import com.coheser.app.models.SoundsModel
import com.coheser.app.models.StoryModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.DataParsing
import com.coheser.app.simpleclasses.DataParsing.getVideoDataModel
import com.coheser.app.simpleclasses.Functions
import org.json.JSONArray
import org.json.JSONObject

class UserRepository : BaseRepository<UserModel>() {

    suspend fun showUserDetail(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<UserModel>>,
    ) {
        try {


        val endpoint = { requestBody: String ->
            apiInterface.showUserDetail(requestBody)
        }

        val parseData = { apiResponseData: ApiResponseData ->
            val userDetailModel = when (apiResponseData) {
                is ApiResponseData.JsonObject -> {
                    val msg = apiResponseData.data
                    val user = msg.optJSONObject("User")
                    val userDetailModel = DataParsing.getUserDataModel(user)

                    val pushNotificationSetting = msg.optJSONObject("PushNotification")
                    val privacyPolicySetting = msg.optJSONObject("PrivacySetting")

                    if (pushNotificationSetting != null) {
                        userDetailModel.pushNotificationModel = Gson().fromJson(
                            pushNotificationSetting.toString(), PushNotificationModel::class.java
                        )
                    }
                    if (privacyPolicySetting != null) {
                        userDetailModel.privacySettingModel = Gson().fromJson(
                            privacyPolicySetting.toString(), PrivacySettingModel::class.java
                        )
                    }

                    val storyArray = user.optJSONArray("story")
                    if(storyArray!=null && storyArray.length()>0) {
                        val storyItem = StoryModel()
                        storyItem.id=userDetailModel.id
                        storyItem.username=userDetailModel.username
                        storyItem.setProfilePic(userDetailModel.getProfilePic())
                        storyItem.videoList=ArrayList()
                        for (i in 0 until storyArray.length()) {
                            val itemObj = storyArray.getJSONObject(i)
                            val storyVideoItem = getVideoDataModel(itemObj.optJSONObject("Video"))

                            storyItem.videoList.add(storyVideoItem)
                        }
                        userDetailModel.storyModel = storyItem
                    }

                    userDetailModel
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
            userDetailModel
        }

        makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }


    suspend fun showUnReadNotifications(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {
        try {


            val endpoint = { requestBody: String ->
                apiInterface.showUnReadNotifications(requestBody)
            }

            val parseData = { apiResponseData: ApiResponseData ->
               when (apiResponseData) {
                    is ApiResponseData.JsonString -> {
                        Functions.printLog(Constants.tag,"Notification Count : "+apiResponseData.data)
                        apiResponseData.data

                       }
                    else -> throw IllegalArgumentException("Unsupported response type")
                }

            }

            makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }


    suspend fun showProducts(
        params: JSONObject,
        livedata : MutableLiveData<ApiResponce<ArrayList<ProductModel>>>
    ){
        try {
            val endpoint = { requestBody: String ->
                apiInterface.showProducts(requestBody)
            }
            val parseData = { apiResponseData: ApiResponseData ->
                when (apiResponseData) {
                    is ApiResponseData.JsonArray -> {
                        val list = ArrayList<ProductModel>()
                        for (i in 0 until apiResponseData.data.length()) {
                            val `object` = apiResponseData.data.optJSONObject(i)

                            val model = Gson().fromJson(`object`.toString(),ProductModel::class.java)

                            list.add(model)
                        }
                        list
                    }

                    else -> throw IllegalArgumentException("Unsupported response type")
                }
            }

            makeApiCall(params, endpoint, livedata, parseData)

        }catch (e : Exception){

        }

    }

    suspend fun showUserDetail(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<UserModel>>,
        playListData: MutableLiveData<ApiResponce<JSONArray>>
    ) {
        try {


        val endpoint = { requestBody: String ->
            apiInterface.showUserDetail(requestBody)
        }

        val parseData = { apiResponseData: ApiResponseData ->
            val userDetailModel = when (apiResponseData) {
                is ApiResponseData.JsonObject -> {
                    val msg = apiResponseData.data
                    val user = msg.optJSONObject("User")
                    val userDetailModel = DataParsing.getUserDataModel(user)

                    val pushNotificationSetting = msg.optJSONObject("PushNotification")
                    val privacyPolicySetting = msg.optJSONObject("PrivacySetting")

                    if (pushNotificationSetting != null) {
                        userDetailModel.pushNotificationModel = Gson().fromJson(
                            pushNotificationSetting.toString(), PushNotificationModel::class.java
                        )
                    }

                    if (privacyPolicySetting != null) {
                        userDetailModel.privacySettingModel = Gson().fromJson(
                            privacyPolicySetting.toString(), PrivacySettingModel::class.java
                        )
                    }


                    val storyArray = user.optJSONArray("story")
                    if(storyArray!=null && storyArray.length()>0) {
                        val storyItem = StoryModel()
                        storyItem.id=userDetailModel.id
                        storyItem.username=userDetailModel.username
                        storyItem.setProfilePic(userDetailModel.getProfilePic())
                        storyItem.videoList=ArrayList()
                        for (i in 0 until storyArray.length()) {
                            val itemObj = storyArray.getJSONObject(i)
                            val storyVideoItem = getVideoDataModel(itemObj.optJSONObject("Video"))

                            storyItem.videoList.add(storyVideoItem)
                        }
                        userDetailModel.storyModel = storyItem
                    }


                    if (user.has("Playlist")) {
                        playListData.postValue(ApiResponce.Success(user.getJSONArray("Playlist")))
                    }


                    userDetailModel
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
            userDetailModel
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }

    suspend fun getSuggestionUserList(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.showSuggestedUsers(requestBody)
        }
        val parseData = { apiResponseData: ApiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray -> {
                    val list = ArrayList<UserModel>()
                    for (i in 0 until apiResponseData.data.length()) {
                        val `object` = apiResponseData.data.optJSONObject(i)
                        val userDetailModel =
                            DataParsing.getUserDataModel(`object`.optJSONObject("User"))

                        list.add(userDetailModel)
                    }
                    list
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }

    suspend fun callApiFollowUser(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<UserModel>>,
    ) {
        try {


        val endpoint = { requestBody: String ->
            apiInterface.followUser(requestBody)
        }

        val parseData = { apiResponseData: ApiResponseData ->
            val userDetailModel = when (apiResponseData) {
                is ApiResponseData.JsonObject -> {
                    val msg = apiResponseData.data
                    val user = msg.optJSONObject("User")
                    DataParsing.getUserDataModel(user)
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
            userDetailModel
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }



    suspend fun callApiBlockUser(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.blockUser(requestBody)
        }

        val parseData = { apiResponseData: ApiResponseData ->
            val msg = when (apiResponseData) {
                is ApiResponseData.JsonObject ->{
                    "1"
                }
                is ApiResponseData.JsonString -> {
                   "0"
                }
                else -> throw IllegalArgumentException("Unsupported response type")
            }
            msg
        }

        makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}

    }



    suspend fun showFollowers(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>>,
    ) {
        try {


        val endpoint = { requestBody: String ->
            apiInterface.showFollowers(requestBody)
        }
        val parseData = { apiResponseData: ApiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray -> {
                    val list = ArrayList<UserModel>()
                    for (i in 0 until apiResponseData.data.length()) {
                        val `object` = apiResponseData.data.optJSONObject(i)
                        val userDetailModel =
                            DataParsing.getUserDataModel(`object`.optJSONObject("User"))


                        list.add(userDetailModel)
                    }
                    list
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }


    suspend fun showFollowing(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.showFollowing(requestBody)
        }
        val parseData = { apiResponseData: ApiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray -> {
                    val list = ArrayList<UserModel>()
                    for (i in 0 until apiResponseData.data.length()) {
                        val `object` = apiResponseData.data.optJSONObject(i)
                        val userDetailModel =
                            DataParsing.getUserDataModel(`object`.optJSONObject("User"))

                        list.add(userDetailModel)
                    }
                    list
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }





    suspend fun getSearchUserList(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>>,
    ) {
        try {


        val endpoint = { requestBody: String ->
            apiInterface.search(requestBody)
        }
        val parseData = { apiResponseData: ApiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray -> {
                    val list = ArrayList<UserModel>()
                    for (i in 0 until apiResponseData.data.length()) {
                        val `object` = apiResponseData.data.optJSONObject(i)
                        val userDetailModel =
                            DataParsing.getUserDataModel(`object`.optJSONObject("User"))

                        list.add(userDetailModel)
                    }
                    list
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }



    suspend fun getProfileVisitors(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>>,
    ) {
        try {


        val endpoint = { requestBody: String ->
            apiInterface.showProfileVisitors(requestBody)
        }
        val parseData = { apiResponseData: ApiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray -> {
                    val list = ArrayList<UserModel>()
                    for (i in 0 until apiResponseData.data.length()) {
                        val `object` = apiResponseData.data.optJSONObject(i)
                        val userDetailModel =
                            DataParsing.getUserDataModel(`object`.optJSONObject("User"))

                        list.add(userDetailModel)
                    }
                    list
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }


    suspend fun callApiEditProfile(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<UserModel>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.editProfile(requestBody)
        }

        val parseData = { apiResponseData: ApiResponseData ->
            val userDetailModel = when (apiResponseData) {
                is ApiResponseData.JsonObject -> {
                    val msg = apiResponseData.data
                    val user = msg.optJSONObject("User")
                    val userDetailModel = DataParsing.getUserDataModel(user)

                    val pushNotificationSetting = msg.optJSONObject("PushNotification")
                    val privacyPolicySetting = msg.optJSONObject("PrivacySetting")

                    if (pushNotificationSetting != null) {
                        userDetailModel.pushNotificationModel = Gson().fromJson(
                            pushNotificationSetting.toString(), PushNotificationModel::class.java
                        )
                    }

                    if (privacyPolicySetting != null) {
                        userDetailModel.privacySettingModel = Gson().fromJson(
                            privacyPolicySetting.toString(), PrivacySettingModel::class.java
                        )
                    }
                    userDetailModel
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
            userDetailModel
        }

        makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }



    suspend fun addPolicySettings(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<PrivacySettingModel>>,
    ) {
        try {


        val endpoint = { requestBody: String ->
            apiInterface.addPrivacySetting(requestBody)
        }

        val parseData = { apiResponseData: ApiResponseData ->
            val privacySettingModel = when (apiResponseData) {
                is ApiResponseData.JsonObject -> {
                    val msg = apiResponseData.data
                    val privacy_policy_setting = msg.optJSONObject("PrivacySetting")
                    val privacySettingModel = Gson().fromJson(
                        privacy_policy_setting.toString(),
                        PrivacySettingModel::class.java
                    )
                    privacySettingModel
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
            privacySettingModel
        }

        makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }


    suspend fun updatePushNotificationSetting(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<PushNotificationModel>>,
    ) {
        try {


        val endpoint = { requestBody: String ->
            apiInterface.updatePushNotificationSettings(requestBody)
        }

        val parseData = { apiResponseData: ApiResponseData ->
            val model = when (apiResponseData) {
                is ApiResponseData.JsonObject -> {
                    val msg = apiResponseData.data
                    val push_notification_setting = msg.optJSONObject("PushNotification")
                    val pushNotificationModel = Gson().fromJson(
                        push_notification_setting.toString(),
                        PushNotificationModel::class.java
                    )
                    pushNotificationModel
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
            model
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }


    suspend fun getBlockUserList(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>>,
    ) {
        try {


        val endpoint = { requestBody: String ->
            apiInterface.showBlockedUsers(requestBody)
        }
        val parseData = { apiResponseData: ApiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray -> {
                    val list = ArrayList<UserModel>()
                    for (i in 0 until apiResponseData.data.length()) {
                        val `object` = apiResponseData.data.optJSONObject(i)
                        val userDetailModel =
                            DataParsing.getUserDataModel(`object`.optJSONObject("BlockedUser"))

                        list.add(userDetailModel)
                    }
                    list
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }


    suspend fun callApiDeleteUser(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.deleteUserAccount(requestBody)
        }

        val parseData = { apiResponseData: ApiResponseData ->
            val msg = when (apiResponseData) {
                is ApiResponseData.JsonObject ->{
                    apiResponseData.data.toString()
                }
                is ApiResponseData.JsonString -> {
                    apiResponseData.data.toString()
                }
                else -> throw IllegalArgumentException("Unsupported response type")
            }
            msg
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}

    }


    suspend fun callApiProfileVerification(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.userVerificationRequest(requestBody)
        }

        val parseData = { apiResponseData: ApiResponseData ->
            val msg = when (apiResponseData) {
                is ApiResponseData.JsonObject ->{
                    apiResponseData.data.toString()
                }
                else -> throw IllegalArgumentException("Unsupported response type")
            }
            msg
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}

    }

    suspend fun callApiSoundSearch(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<SoundsModel>>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.search(requestBody)
        }

        makeApiCall(params, endpoint, liveData,
            parseData = { apiResponseData ->
                parseSoundData((apiResponseData as ApiResponseData.JsonArray).data)
            })

        }catch (e:Exception){}

    }

    fun parseSoundData (jsonArray: JSONArray) : ArrayList<SoundsModel> {
        val templist = ArrayList<SoundsModel> ()
        for (i in 0 until  jsonArray.length()){
            val soundData = jsonArray.optJSONObject(i).getJSONObject("Sound")
            if (soundData != null) {
                val soundModel = Gson().fromJson(soundData.toString(), SoundsModel::class.java)
                templist.add(soundModel)
            }
        }
        return templist
    }

    suspend fun callApiAddFavSound(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {

        try {



        val endpoint = { requestBody: String ->
            apiInterface.addSoundFavourite(requestBody)
        }

        val parseData = { apiResponseData: ApiResponseData ->
            val msg = when (apiResponseData) {
                is ApiResponseData.JsonObject ->{
                    apiResponseData.data.toString()
                }
                is ApiResponseData.JsonString -> {
                    apiResponseData.data.toString()
                }
                else -> throw IllegalArgumentException("Unsupported response type")
            }
            msg
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }



}