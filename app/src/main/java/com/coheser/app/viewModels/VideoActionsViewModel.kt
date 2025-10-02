package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.UserModel
import com.coheser.app.repositories.UserRepository
import com.coheser.app.repositories.VideosRepository
import com.coheser.app.simpleclasses.Functions
import kotlinx.coroutines.launch
import org.json.JSONObject

class VideoActionsViewModel(
    private val context: Context,
    private val userRepository: UserRepository,
    private val videoRepository: VideosRepository
) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }


    val _listLiveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>> = MutableLiveData()
    val listLiveData: LiveData<ApiResponce<ArrayList<UserModel>>> get() = _listLiveData

    fun getFollowingList(){
        viewModelScope.launch {

            val params = JSONObject()
            userRepository.showFollowing(params,_listLiveData)

        }
    }


    val _shareVideoLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val shareVideoLiveData: LiveData<ApiResponce<String>> get() = _shareVideoLiveData

    fun shareVideo(videoId:String){
        viewModelScope.launch {

            val params = JSONObject().apply {
               put("video_id", videoId)
            }
            videoRepository.shareVideo(params,_shareVideoLiveData)

        }
    }

}
