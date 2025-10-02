package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.PushNotificationModel
import com.coheser.app.repositories.UserRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class PushNotificationViewModel(
    private val context: Context,
    private val userRepository: UserRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }

    var strLikes = 1
    var strComment = 1
    var strNewFollow = 1
    var strMention = 1
    var strDirectMessage = 1
    var str_video_update = 1


    val _pushNotificationLiveData: MutableLiveData<ApiResponce<PushNotificationModel>> = MutableLiveData()
    val pushNotificationLiveData: LiveData<ApiResponce<PushNotificationModel>> get() = _pushNotificationLiveData

    fun updatePushNotificationSetting(){
        viewModelScope.launch {

            val param = JSONObject().apply {
                put("likes", strLikes)
                put("comments", strComment)
                put("new_followers", strNewFollow)
                put("mentions", strMention)
                put("video_updates", str_video_update)
                put("direct_messages", strDirectMessage)
                put(
                    "user_id",
                    sharedPreferences.getString(
                        Variables.U_ID,
                        ""
                    )
                )
            }

            userRepository.updatePushNotificationSetting(param, _pushNotificationLiveData)
        }
    }



}