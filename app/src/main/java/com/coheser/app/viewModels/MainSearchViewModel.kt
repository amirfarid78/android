package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.HomeModel
import com.coheser.app.models.SoundsModel
import com.coheser.app.models.UserModel
import com.coheser.app.repositories.UserRepository
import com.coheser.app.repositories.VideosRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainSearchViewModel(
    val context : Context,
    val userRepository: UserRepository,
    val videosRepository: VideosRepository
) :ViewModel(){
    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences= Functions.getSharedPreference(context)
    }


    var noDataViewVisibility = ObservableBoolean(false)
    var recylerViewVisiblity = ObservableBoolean(false)


    fun showDataView(){
        noDataViewVisibility.set(false)
        recylerViewVisiblity.set(true)

    }
    fun hideDataView(){
        noDataViewVisibility.set(true)
        recylerViewVisiblity.set(false)
    }


    val _followLiveData: MutableLiveData<ApiResponce<UserModel>> = MutableLiveData()
    val followLiveData: LiveData<ApiResponce<UserModel>> get() = _followLiveData

    fun followUser(userId:String){
        viewModelScope.launch {
            val param = JSONObject()
            param.put("sender_id", sharedPreferences.getString(Variables.U_ID, "0"))
            param.put("receiver_id", userId)
            userRepository.callApiFollowUser(param, _followLiveData)
        }
    }

    val _userLiveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>> = MutableLiveData()
    val userLiveData: LiveData<ApiResponce<ArrayList<UserModel>>> get() = _userLiveData

    val _soundLiveData: MutableLiveData<ApiResponce<ArrayList<SoundsModel>>> = MutableLiveData()
    val soundLiveData: LiveData<ApiResponce<ArrayList<SoundsModel>>> get() = _soundLiveData

    fun getSearchData(pageCount:Int,keyword:String,type:String){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put(
                    "user_id", sharedPreferences.getString(
                        Variables.U_ID, "0"
                    )
                )
                put("type", type)
                put("keyword", keyword)
                put("starting_point", "$pageCount")
            }
            if (type == "sound"){
                userRepository.callApiSoundSearch(params,_soundLiveData)
            }else{
                userRepository.getSearchUserList(params,_userLiveData)
            }

        }
    }

    val _videosLiveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>> = MutableLiveData()
    val videosLiveData: LiveData<ApiResponce<ArrayList<HomeModel>>> get() = _videosLiveData

    fun getSearchVideoData(pageCount:Int,keyword:String,type:String){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put("type", type)
                put("keyword", keyword)
                put("starting_point", "$pageCount")
            }
            videosRepository.getSearchVideos(params,_videosLiveData)

        }
    }

    val _addSoundLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val addSoundLiveData: LiveData<ApiResponce<String>> get() = _addSoundLiveData

    fun addFavSound(item: SoundsModel){
        viewModelScope.launch {

            val param = JSONObject().apply {
                put("sound_id",item.id)
            }
            userRepository.callApiAddFavSound(param, _addSoundLiveData)
        }
    }

}
