package com.coheser.app.repositories

import androidx.lifecycle.MutableLiveData
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.DiscoverModel
import com.coheser.app.models.HashTagModel
import com.coheser.app.models.HomeModel
import com.coheser.app.simpleclasses.DataParsing
import org.json.JSONArray
import org.json.JSONObject

class VideosRepository : BaseRepository<HomeModel>(){

    suspend fun showNearbyVideos(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>>,
    ) {

        try {


        makeApiCall(
            params,
            endpoint = { body -> apiInterface.showNearbyVideos(body) },
            liveData,
            parseData = { apiResponseData ->
                parseVideoData((apiResponseData as ApiResponseData.JsonArray).data)
            }
        )

        }catch (e:Exception){}

    }

    suspend fun showFollowingVideos(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>>,
    ) {
        try {


        makeApiCall(
            params,
            endpoint = { body -> apiInterface.showFollowingVideos(body) },
            liveData,
            parseData = { apiResponseData ->
                parseVideoData((apiResponseData as ApiResponseData.JsonArray).data)
            }
        )

        }catch (e:Exception){}
    }

    suspend fun showRelatedVideos(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>>,
    ) {

        try {


        makeApiCall(
            params,
            endpoint = { body -> apiInterface.showRelatedVideos(body) },
            liveData,
            parseData = { apiResponseData ->
                parseVideoData((apiResponseData as ApiResponseData.JsonArray).data)
            }
        )
        }catch (e:Exception){}
    }


    suspend fun showVideosAgainstUserID(
        params: JSONObject,
        type:String,
        liveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.showVideosAgainstUserID(requestBody)
        }
        val parseData = { apiResponseData: ApiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonObject -> {
                    parseVideoData(apiResponseData.data.optJSONArray(type))
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }


    suspend fun showUserLikedVideos(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>>,
    ) {
        try {



        makeApiCall(
            params,
            endpoint = { body -> apiInterface.showUserLikedVideos(body) },
            liveData,
            parseData = { apiResponseData ->
                parseVideoData((apiResponseData as ApiResponseData.JsonArray).data)
            }
        )

        }catch (e:Exception){}


    }


    suspend fun showUserRepostedVideos(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>>,
    ) {

        try {


        makeApiCall(
            params,
            endpoint = { body -> apiInterface.showUserRepostedVideos(body) },
            liveData,
            parseData = { apiResponseData ->
                parseVideoData((apiResponseData as ApiResponseData.JsonArray).data)
            }
        )

        }catch (e:Exception){}

    }



    suspend fun showStoreTaggedVideos(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>>,
    ) {

        try {


        makeApiCall(
            params,
            endpoint = { body -> apiInterface.showStoreTaggedVideos(body) },
            liveData,
            parseData = { apiResponseData ->
                parseVideoData((apiResponseData as ApiResponseData.JsonArray).data)
            }
        )

        }catch (e:Exception){}
    }


    suspend fun showTaggedVideosAgainstUserID(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>>,
    ) {

        try {


        makeApiCall(
            params,
            endpoint = { body -> apiInterface.showTaggedVideosAgainstUserID(body) },
            liveData,
            parseData = { apiResponseData ->
                parseVideoData((apiResponseData as ApiResponseData.JsonArray).data)
            }
        )

        }catch (e:Exception){}
    }


    suspend fun showFavouriteVideos(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>>,
    ) {

        try {

        makeApiCall(
            params,
            endpoint = { body -> apiInterface.showFavouriteVideos(body) },
            liveData,
            parseData = { apiResponseData ->
                parseVideoData((apiResponseData as ApiResponseData.JsonArray).data)
            }
        )
        }catch (e:Exception){}
    }

    suspend fun showVideosAgainstHashtag(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>>,
        hashtagModelLiveData: MutableLiveData<ApiResponce<HashTagModel>>
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.showVideosAgainstHashtag(requestBody)
        }
        val parseData =
            { apiResponseData: ApiResponseData ->
                when (apiResponseData) {
                    is ApiResponseData.JsonObject -> {

                        val hashtag = apiResponseData.data.optJSONObject("Hashtag")
                        val model= HashTagModel()
                        model.id=hashtag.optString("id")
                        model.fav=hashtag.optString("favourite")
                        model.videos_count=hashtag.optString("videos_count")
                        hashtagModelLiveData.postValue(ApiResponce.Success(model))

                        parseVideoData(hashtag.optJSONArray("videos"))

                    }

                    else -> throw IllegalArgumentException("Unsupported response type")
                }
            }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }


    suspend fun addHashtagFavourite(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.addHashtagFavourite(requestBody)
        }
        val parseData =
            { apiResponseData: ApiResponseData ->
                when (apiResponseData) {
                    is ApiResponseData.JsonObject -> {
                        apiResponseData.data.toString()
                    }
                    is ApiResponseData.JsonString -> {
                        apiResponseData.data
                    }

                    else -> throw IllegalArgumentException("Unsupported response type")
                }
            }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }



    suspend fun showDiscoverySections(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<DiscoverModel>>>
    ) {

        try {



        val endpoint = { requestBody: String ->
            apiInterface.showDiscoverySections(requestBody)
        }
        val parseData =
            { apiResponseData: ApiResponseData ->
                when (apiResponseData) {
                    is ApiResponseData.JsonArray -> {


                        val temp_list = ArrayList<DiscoverModel>()

                        for (d in 0 until apiResponseData.data.length()) {
                            val discover_object = apiResponseData.data.optJSONObject(d)
                            val hashtag = discover_object.optJSONObject("Hashtag")
                            val discover_model = DiscoverModel()
                            discover_model.id = hashtag.optString("id")
                            discover_model.title = hashtag.optString("name")
                            discover_model.views = hashtag.optString("views")
                            discover_model.videos_count = hashtag.optString("videos_count")
                            discover_model.fav = hashtag.optString("favourite", "0")
                            val video_array = hashtag.optJSONArray("Videos")

                            discover_model.arrayList = parseVideoData(video_array)
                            temp_list.add(discover_model)
                        }

                       temp_list
                    }

                    else -> throw IllegalArgumentException("Unsupported response type")
                }
            }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }


    suspend fun showVideosAgainstLocation(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>>,
    ) {

        try {


        makeApiCall(
            params,
            endpoint = { body -> apiInterface.showVideosAgainstLocation(body) },
            liveData,
            parseData = { apiResponseData ->
                parseVideoData((apiResponseData as ApiResponseData.JsonArray).data)
            }
        )

        }catch (e:Exception){}
    }


    suspend fun showVideoDetailAd(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<HomeModel>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.showVideoDetailAd(requestBody)
        }
        val parseData =
            { apiResponseData: ApiResponseData ->
                when (apiResponseData) {
                    is ApiResponseData.JsonObject -> {
                        val video = apiResponseData.data.optJSONObject("Video")
                        val user = apiResponseData.data.optJSONObject("User")
                        val sound = apiResponseData.data.optJSONObject("Sound")
                        val location = apiResponseData.data.optJSONObject("Location")
                        val videoProduct = apiResponseData.data.optJSONArray("VideoProduct")
                        val userprivacy = user.optJSONObject("PrivacySetting")
                        val userPushNotification = user.optJSONObject("PushNotification")
                        val item = DataParsing.parseVideoDetailData(
                            HomeModel(),
                            user,
                            sound,
                            video,
                            location,
                            videoProduct,
                            userprivacy,
                            userPushNotification
                        )

                        item


                    }

                    else -> throw IllegalArgumentException("Unsupported response type")
                }
            }

        makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }


    suspend fun showVideoDetail(
        params: JSONObject,
        homeModel: HomeModel,
        liveData: MutableLiveData<ApiResponce<HomeModel>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.showVideoDetail(requestBody)
        }
        val parseData =
            { apiResponseData: ApiResponseData ->
                when (apiResponseData) {
                    is ApiResponseData.JsonObject -> {


                        val video = apiResponseData.data.optJSONObject("Video")
                        val user = apiResponseData.data.optJSONObject("User")
                        val sound = apiResponseData.data.optJSONObject("Sound")
                        val location = apiResponseData.data.optJSONObject("Location")
                        val videoProduct = apiResponseData.data.optJSONArray("VideoProduct")
                        val userprivacy = user.optJSONObject("PrivacySetting")
                        val userPushNotification = user.optJSONObject("PushNotification")
                        val item = DataParsing.parseVideoDetailData(
                                homeModel,
                                user,
                                sound,
                                video,
                                location,
                                videoProduct,
                                userprivacy,
                                userPushNotification
                            )

                        item


                    }

                    else -> throw IllegalArgumentException("Unsupported response type")
                }
            }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }



    suspend fun destinationTap(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.destinationTap(requestBody)
        }
        val parseData: (ApiResponseData) -> String = { apiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray->apiResponseData.data.toString()
                is ApiResponseData.JsonObject -> apiResponseData.data.toString()
                is ApiResponseData.JsonString -> apiResponseData.data
                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }


    suspend fun pinVideo(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {
        try {


        val endpoint = { requestBody: String ->
            apiInterface.pinVideo(requestBody)
        }
        val parseData: (ApiResponseData) -> String = { apiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray->apiResponseData.data.toString()
                is ApiResponseData.JsonObject -> apiResponseData.data.toString()
                is ApiResponseData.JsonString -> apiResponseData.data
                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }

    suspend fun NotInterestedVideo(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {
        try {


        val endpoint = { requestBody: String ->
            apiInterface.NotInterestedVideo(requestBody)
        }
        val parseData: (ApiResponseData) -> String = { apiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray->apiResponseData.data.toString()
                is ApiResponseData.JsonObject -> apiResponseData.data.toString()
                is ApiResponseData.JsonString -> apiResponseData.data
                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }

    suspend fun downloadVideo(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {
        try {


        val endpoint = { requestBody: String ->
            apiInterface.downloadVideo(requestBody)
        }
        val parseData: (ApiResponseData) -> String = { apiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray->apiResponseData.data.toString()
                is ApiResponseData.JsonObject -> apiResponseData.data.toString()
                is ApiResponseData.JsonString -> apiResponseData.data
                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }

    suspend fun deleteWaterMarkVideo(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {
        try {


        val endpoint = { requestBody: String ->
            apiInterface.deleteWaterMarkVideo(requestBody)
        }
        val parseData: (ApiResponseData) -> String = { apiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray->apiResponseData.data.toString()
                is ApiResponseData.JsonObject -> apiResponseData.data.toString()
                is ApiResponseData.JsonString -> apiResponseData.data
                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }


    suspend fun repostVideo(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.repostVideo(requestBody)
        }
        val parseData: (ApiResponseData) -> String = { apiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray->apiResponseData.data.toString()
                is ApiResponseData.JsonObject -> apiResponseData.data.toString()
                is ApiResponseData.JsonString -> apiResponseData.data
                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }


    suspend fun shareVideo(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {
        try {


        val endpoint = { requestBody: String ->
            apiInterface.shareVideo(requestBody)
        }
        val parseData: (ApiResponseData) -> String = { apiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray->apiResponseData.data.toString()
                is ApiResponseData.JsonObject -> apiResponseData.data.toString()
                is ApiResponseData.JsonString -> apiResponseData.data
                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }









    fun parseVideoData(msgArray: JSONArray): ArrayList<HomeModel> {

        val temp_list = ArrayList<HomeModel>()

        for (i in 0 until msgArray.length()) {
            val itemdata = msgArray.optJSONObject(i)
            val video = itemdata.optJSONObject("Video")
            val sound = itemdata.optJSONObject("Sound")
            val user = itemdata.optJSONObject("User")
            val location = itemdata.optJSONObject("Location")
            val store = itemdata.optJSONObject("Store")
            val videoProduct = itemdata.optJSONObject("Product")
            val userPrivacy = user.optJSONObject("PrivacySetting")
            val pushNotification = user.optJSONObject("PushNotification")
            val item = DataParsing.parseVideoData(
                user,
                sound,
                video,
                location,
                store,
                videoProduct,
                userPrivacy,
                pushNotification
            )
            temp_list.add(item)
        }

        return temp_list

    }

    suspend fun getSearchVideos(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>>,
    ) {

        try {

        makeApiCall(
            params,
            endpoint = { body -> apiInterface.search(body) },
            liveData,
            parseData = { apiResponseData ->
                parseVideoData((apiResponseData as ApiResponseData.JsonArray).data)
            }
        )

    }catch (e:Exception){}


    }







}