package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.UserModel
import com.coheser.app.repositories.ChatRepository
import com.coheser.app.repositories.UserRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MyProfileViewModel(
    private val context: Context,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }



    private val _cartCount = MutableLiveData<Int>()
    val cartCount: LiveData<Int> = _cartCount

    private val _notificationCount = MutableLiveData<Int>()
    val notificationCount: LiveData<Int> = _notificationCount
    fun updateCounts() {
        val cart = sharedPreferences.getInt(Variables.cartCount, 0)
        _cartCount.value = cart

        val notifications = sharedPreferences.getInt(Variables.notificationCount, 0)
        _notificationCount.value = notifications
    }


    private val _inboxCount = MutableLiveData<Int>()
    val inboxCount: LiveData<Int> = _inboxCount
    fun getInboxCountData(){
        viewModelScope.launch {
            val userId =sharedPreferences.getString(Variables.U_ID, "0")
            userId?.let { chatRepository.updateInboxCount(it, _inboxCount) }
        }
    }


    var isRefreshTabs = ObservableBoolean(false)

    val _userDetailLiveData: MutableLiveData<ApiResponce<UserModel>> = MutableLiveData()
    val userDetailLiveData: LiveData<ApiResponce<UserModel>> get() = _userDetailLiveData

    val _playListLiveData: MutableLiveData<ApiResponce<JSONArray>> = MutableLiveData()
    val playListLiveData: LiveData<ApiResponce<JSONArray>> get() = _playListLiveData

    fun getUserDetails(){
        viewModelScope.launch {
            if(sharedPreferences.getBoolean(Variables.IS_LOGIN,false)) {
                val param = JSONObject()
                param.put("auth_token", sharedPreferences.getString(Variables.AUTH_TOKEN, "0"))
                userRepository.showUserDetail(param, _userDetailLiveData,_playListLiveData)
            }
        }
    }


    val _userModel: MutableLiveData<UserModel> = MutableLiveData()
    val userModel: LiveData<UserModel> get() = _userModel

    fun setData(userModel: UserModel){
        _userModel.value=userModel
    }


}
