package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.HomeModel
import com.coheser.app.models.UserModel
import com.coheser.app.repositories.UserRepository
import com.coheser.app.repositories.VideosRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class VideoPlayViewModel(
    private val context: Context,
    private val userRepository: UserRepository,
    private val videoRepository: VideosRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }

    val _videoDetailLiveData: MutableLiveData<ApiResponce<HomeModel>> = MutableLiveData()
    val videoDetailLiveData: LiveData<ApiResponce<HomeModel>> get() = _videoDetailLiveData
    fun getVideoDetails(homeModel: HomeModel){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put("video_id", homeModel.video_id)
                if (homeModel.promotionModel != null){
                    put("promotion_id",homeModel.promotionModel!!.id)
                }

            }
            videoRepository.showVideoDetail(params,homeModel,_videoDetailLiveData)
        }
    }



    val _destinationTapLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val destinationTapLiveData: LiveData<ApiResponce<String>> get() = _destinationTapLiveData
    fun destinationTap(id:String){
        viewModelScope.launch {

            val params = JSONObject()
            params.put("promotion_id", id)
            videoRepository.destinationTap(params,_destinationTapLiveData)
        }
    }



    val _pinVideoLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val pinVideoLiveData: LiveData<ApiResponce<String>> get() = _pinVideoLiveData
    fun pinVideo(videoID:String,pin: String){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put("video_id", videoID)
                put("pin", pin)

            }
            videoRepository.pinVideo(params,_pinVideoLiveData)
        }
    }



    val _notInterestedLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val notInterestedLiveData: LiveData<ApiResponce<String>> get() = _notInterestedLiveData
    fun notInterestedVideo(videoID:String){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put("video_id", videoID)

            }
            videoRepository.NotInterestedVideo(params,_notInterestedLiveData)
        }
    }


    val _downloadVideoLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val downloadVideoLiveData: LiveData<ApiResponce<String>> get() = _downloadVideoLiveData
    fun downloadVideo(videoID:String){
        viewModelScope.launch {
            val params = JSONObject()
            params.put("video_id", videoID)

            videoRepository.downloadVideo(params,_downloadVideoLiveData)
        }
    }


    val _deleteWaterMarkLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val deleteWaterMarkLiveData: LiveData<ApiResponce<String>> get() = _deleteWaterMarkLiveData
    fun deleteWaterMarkVideo(url:String){
        viewModelScope.launch {

            val params = JSONObject()
            params.put("video_url", url)
            videoRepository.deleteWaterMarkVideo(params,_deleteWaterMarkLiveData)
        }
    }


    val _repostLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val repostLiveData: LiveData<ApiResponce<String>> get() = _repostLiveData
    fun repostVideo(videoID:String){
        viewModelScope.launch {
            val params = JSONObject().apply {
                put("repost_user_id", sharedPreferences.getString(Variables.U_ID, ""))
                put("video_id", videoID)
                put("repost_comment", "")
            }
            videoRepository.repostVideo(params,_repostLiveData)
        }
    }




    val _followLiveData: MutableLiveData<ApiResponce<UserModel>> = MutableLiveData()
    val followLiveData: LiveData<ApiResponce<UserModel>> get() = _followLiveData

    fun followUser(item:HomeModel){
        viewModelScope.launch {
            val param = JSONObject()
            param.put("sender_id", sharedPreferences.getString(Variables.U_ID, "0"))
            param.put("receiver_id", item.user_id)
            if (item.promotionModel != null && item.promotionModel!!.destination == "follower"){
                param.put("promotion_id", item.promotionModel!!.id)
            }
            userRepository.callApiFollowUser(param, _followLiveData)
        }
    }


}

