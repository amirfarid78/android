package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.NotificationModel
import com.coheser.app.models.StoryModel
import com.coheser.app.models.UserModel
import com.coheser.app.repositories.NotificationRepository
import com.coheser.app.repositories.UserRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class NotificationViewModel(
    private val context: Context,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }

    var ispostFinsh = false
    var isApiRun = false
    var pageCount: ObservableInt = ObservableInt(0)

    var noDataViewVisibility = ObservableBoolean(false)
    var loadMoreLoaderVisibility = ObservableBoolean(false)

    val _listLiveData: MutableLiveData<ApiResponce<ArrayList<NotificationModel>>> = MutableLiveData()
    val listLiveData: LiveData<ApiResponce<ArrayList<NotificationModel>>> get() = _listLiveData
    fun showAllNotifications(){
        viewModelScope.launch {
            val params = JSONObject().apply {
                 put("starting_point",  pageCount.get().toString())
            }
            notificationRepository.showAllNotifications(params,_listLiveData)

        }
    }



    val _listStoryData: MutableLiveData<ApiResponce<ArrayList<StoryModel>>> = MutableLiveData()
    val listStoryData: LiveData<ApiResponce<ArrayList<StoryModel>>> get() = _listStoryData
    fun showAllStories(){
        viewModelScope.launch {
            val params = JSONObject().apply {
                put("user_id", sharedPreferences.getString(Variables.U_ID, "0"))
                put("starting_point",  pageCount.get().toString())
            }
            notificationRepository.callApiAllStory(params,_listStoryData)

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


    val _userNotificationLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val userNotificationLiveData: LiveData<ApiResponce<String>> get() = _userNotificationLiveData
    fun getUnReadNotification(){
        viewModelScope.launch {
            val param = JSONObject()
            userRepository.showUnReadNotifications(param, _userNotificationLiveData)
        }
    }

    fun showNoData(){
        noDataViewVisibility.set(true)
    }

    fun hideNoData(){
        noDataViewVisibility.set(false)
    }

}
