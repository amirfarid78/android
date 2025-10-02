package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.HomeModel
import com.coheser.app.models.UserModel
import com.coheser.app.repositories.AddressRepository
import com.coheser.app.repositories.UserRepository
import com.coheser.app.repositories.VideosRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomeViewModel(
    private val context: Context,
    private val videosRepository: VideosRepository,
    private val addressRepository: AddressRepository,
    private val userRepository: UserRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }



    private val _addressTxt = MutableLiveData<String>()
    val addressTxt: LiveData<String> get() = _addressTxt
    fun getAddressLabel() {
        viewModelScope.launch {
            val addressModel = addressRepository.getAddress()

            val label = if (addressModel != null) {
                if (Functions.isStringHasValue(addressModel.city)) {
                    addressModel.city
                } else {
                    addressModel.label
                }
            } else {
                "Location"
            }
            _addressTxt.value = label
        }
    }



    val _userDetailLiveData: MutableLiveData<ApiResponce<UserModel>> = MutableLiveData()
    val userDetailLiveData: LiveData<ApiResponce<UserModel>> get() = _userDetailLiveData

    fun getUserDetails(){
        viewModelScope.launch {
            val param = JSONObject()
            param.put("auth_token", sharedPreferences.getString(Variables.AUTH_TOKEN, "0"))
            userRepository.showUserDetail(param, _userDetailLiveData)
        }
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




    var noDataViewVisibility = ObservableBoolean(false)
    var recylerViewVisiblity = ObservableBoolean(false)
    var loadMoreLoaderVisibility = ObservableBoolean(false)
    var suggesstionVisibility = ObservableBoolean(false)
    var loginLayoutVisibility = ObservableBoolean(false)



    val _suggesstionLiveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>> = MutableLiveData()
    val suggesstionLiveData: LiveData<ApiResponce<ArrayList<UserModel>>> get() = _suggesstionLiveData
    fun getSuggesstionList(){
        viewModelScope.launch {
            val param = JSONObject()
            param.put("auth_token", sharedPreferences.getString(Variables.AUTH_TOKEN, "0"))
            param.put("starting_point", "0")
            userRepository.getSuggestionUserList(param, _suggesstionLiveData)
        }
    }


    val _forYouLiveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>> = MutableLiveData()
    val forYouLiveData: LiveData<ApiResponce<ArrayList<HomeModel>>> get() = _forYouLiveData

    val _followingLiveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>> = MutableLiveData()
    val followingLiveData: LiveData<ApiResponce<ArrayList<HomeModel>>> get() = _followingLiveData

    val _nearByLiveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>> = MutableLiveData()
    val nearByLiveData: LiveData<ApiResponce<ArrayList<HomeModel>>> get() = _nearByLiveData



    val typeforYou = "typeforYou"
    val typeFollowing = "typeFollowing"
    val typeNearBy = "typeNearBy"

    var forYouList = ArrayList<HomeModel>()
    var followingList = ArrayList<HomeModel>()
    var nearbyList = ArrayList<HomeModel>()

    var nearByPageCount: ObservableInt = ObservableInt(-1)
    var followingPageCount: ObservableInt = ObservableInt(-1)
    var forYouPageCount: ObservableInt = ObservableInt(-1)

    var forYouFinish = false
    var followingFinish = false
    var nearByFinish = false

    var isRefresh = ObservableBoolean()
    var isApiRunning = ObservableBoolean(false)

    fun callVideoApi(type: String) {
        isApiRunning=ObservableBoolean(true)

        if (type.equals(typeNearBy, ignoreCase = true)) {
            if (nearByPageCount.get() < 0)
                nearByPageCount.set(0)
            getNearByVideo()
        }

        else if (type.equals(typeFollowing, ignoreCase = true)) {
            if (followingPageCount.get() < 0)
                followingPageCount.set(0)
            getfollowingVideo()
        }

        else if (type.equals(typeforYou, ignoreCase = true)) {
            if (forYouPageCount.get() < 0)
                forYouPageCount.set(0)
            getForYouVideo()
        }


    }


    fun getNearByVideo(){
        viewModelScope.launch {


            val params = JSONObject().apply {
                val addressModel = addressRepository.getAddress()
                if (addressModel != null) {
                    put("lat", addressModel.lat)
                    put("long", addressModel.lng)
                }
                put("starting_point", nearByPageCount.get().toString())

            }
            videosRepository.showNearbyVideos(params,_nearByLiveData)
        }
    }


    fun getfollowingVideo(){
        viewModelScope.launch {
            val params = JSONObject().apply {
                val addressModel = addressRepository.getAddress()
                if (addressModel != null) {
                    put("lat", addressModel.lat)
                    put("long", addressModel.lng)
                }
                put("starting_point", followingPageCount.get().toString())

            }
            videosRepository.showFollowingVideos(params,_followingLiveData)

        }
    }

    fun getForYouVideo(){
        viewModelScope.launch {
            val params = JSONObject().apply {
                val addressModel = addressRepository.getAddress()
                if (addressModel != null) {
                    put("lat", addressModel.lat)
                    put("long", addressModel.lng)
                }
                put("starting_point", forYouPageCount.get().toString())

            }
            videosRepository.showRelatedVideos(params,_forYouLiveData)

        }
    }


    fun refreshCurrentTab(type: String){
        isRefresh.set(true)
        when(type){
            typeNearBy ->{
                nearByPageCount.set(0)
            }
            typeFollowing->{
                followingPageCount.set(0)
            }
            typeforYou ->{
                forYouPageCount.set(0)
            }
        }
        callVideoApi(type)
        isRefresh.set(false)
    }

    fun refreshAllData(type: String){
        nearByPageCount.set(-1)
        followingPageCount.set(-1)
        forYouPageCount.set(-1)
        callVideoApi(type)
    }

    fun decreasePageCount(type: String,isPostFinish: Boolean) {
        when{
            type.equals(typeNearBy, ignoreCase = true) && nearByPageCount.get() > 0 -> {
                nearByPageCount.set(nearByPageCount.get() - 1)
                nearByFinish = isPostFinish
            }
            type.equals(typeFollowing, ignoreCase = true) && followingPageCount.get() > 0 -> {
                followingPageCount.set(followingPageCount.get() - 1)
                followingFinish = isPostFinish
            }
            type.equals(typeforYou, ignoreCase = true) && forYouPageCount.get() > 0 -> {
                forYouPageCount.set(forYouPageCount.get() - 1)
                forYouFinish = isPostFinish
            }

        }

    }

    fun increasePageCount(type: String) {

        when{
            type.equals(typeNearBy, ignoreCase = true) -> {
                nearByPageCount.set(nearByPageCount.get() + 1)
            }
            type.equals(typeFollowing, ignoreCase = true)  -> {
                followingPageCount.set(followingPageCount.get() + 1)
            }
            type.equals(typeforYou, ignoreCase = true) -> {
                forYouPageCount.set(forYouPageCount.get() + 1)
            }

        }

    }

    fun loadMoreContent(type: String){
        if (!isApiRunning.get()) {
            when {
                type.equals(typeforYou, ignoreCase = true) && !forYouFinish -> {
                    forYouPageCount.set(forYouPageCount.get() + 1)
                    loadMoreLoaderVisibility.set(true)
                    callVideoApi(type)
                }

                type.equals(typeFollowing, ignoreCase = true) && !followingFinish -> {
                    followingPageCount.set(followingPageCount.get() + 1)
                    loadMoreLoaderVisibility.set(true)
                    callVideoApi(type)
                }
                type.equals(typeNearBy, ignoreCase = true) && !nearByFinish -> {
                    nearByPageCount.set(nearByPageCount.get() + 1)
                    loadMoreLoaderVisibility.set(true)
                    callVideoApi(type)
                }
            }
        }
    }


    fun getArrayList(type: String): ArrayList<HomeModel> {
        return when {
            type.equals(typeforYou, ignoreCase = true) &&  forYouList.isNotEmpty()-> {
                 forYouList
            }
            type.equals(typeFollowing, ignoreCase = true) && followingList.isNotEmpty() -> {
                followingList
            }
            type.equals(typeNearBy, ignoreCase = true) && nearbyList.isNotEmpty() -> {
               nearbyList
            }
            else -> getVideoListLocally(type)
        }
    }

    fun getVideoListLocally(type:String):ArrayList<HomeModel>{
        val json = Functions.getSharedPreference(context).getString(type, "")
        if (TextUtils.isEmpty(json)) {
           return ArrayList()
        } else {
            val type = object : TypeToken<ArrayList<HomeModel?>?>() {}.type
            val gson = Gson()
            return gson.fromJson(json, type)
        }
    }

    fun saveArrayList(type: String?, list: ArrayList<HomeModel>) {
        val gson = Gson()
        val json = gson.toJson(list)
        Functions.getSharedPreference(context).edit().putString(type, json)
            .apply()
    }

}
