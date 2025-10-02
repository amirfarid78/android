package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.PrivacySettingModel
import com.coheser.app.repositories.UserRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class PrivacyPolicyViewModel(
    private val context: Context,
    private val userRepository: UserRepository

) : ViewModel(){


    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }



    var strVideoDownload: String? = null
    var strDirectMessage: String? = null
    var strDuet: String? = null
    var strLikedVideo: String? = null
    var strVideoComment: String? = null
    var orderView: String? = null


    val _privacyPolicyLiveData: MutableLiveData<ApiResponce<PrivacySettingModel>> = MutableLiveData()
    val privacyPolicyLiveData: LiveData<ApiResponce<PrivacySettingModel>> get() = _privacyPolicyLiveData

    fun addPrivacySetting(){
        viewModelScope.launch {

            val param = JSONObject().apply {
                put("videos_download", strVideoDownload)
                put("direct_message", strDirectMessage)
                put("duet", strDuet)
                put("liked_videos", strLikedVideo)
                put("video_comment", strVideoComment)
                put("order_history", orderView)
                put("user_id", sharedPreferences.getString(Variables.U_ID, ""))
            }

            userRepository.addPolicySettings(param, _privacyPolicyLiveData)
        }
    }




}
