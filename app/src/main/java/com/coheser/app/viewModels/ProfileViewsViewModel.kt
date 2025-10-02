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
import com.coheser.app.repositories.UserRepository
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class ProfileViewsViewModel(
    private val context: Context,
    private val userRepository: UserRepository
) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=getSharedPreference(context)
    }

    var isActivityCallback=false

    var openSettingFragment = MutableLiveData(false)
    var isShowProfileHistory = MutableLiveData(getSharedPreference(context).getString(Variables.U_PROFILE_VIEW, "0"))

    val _listLiveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>> = MutableLiveData()
    val listLiveData: LiveData<ApiResponce<ArrayList<UserModel>>> get() = _listLiveData

    fun getProfileViewsList(){
        viewModelScope.launch {
            val params = JSONObject()
            userRepository.getProfileVisitors(params,_listLiveData)
        }
    }


    val _followLiveData: MutableLiveData<ApiResponce<UserModel>> = MutableLiveData()
    val followLiveData: LiveData<ApiResponce<UserModel>> get() = _followLiveData
    fun followUser(userId:String){
        viewModelScope.launch {
            val param = JSONObject().apply {
                put("sender_id", sharedPreferences.getString(Variables.U_ID, "0"))
                put("receiver_id", userId)
            }
            userRepository.callApiFollowUser(param, _followLiveData)
        }
    }




    val _editProfileLiveData: MutableLiveData<ApiResponce<UserModel>> = MutableLiveData()
    val editProfileLiveData: LiveData<ApiResponce<UserModel>> get() = _editProfileLiveData

    fun updateProfileViewStatus(){
        viewModelScope.launch {
            val param = JSONObject().apply {
                put("auth_token", sharedPreferences.getString(Variables.AUTH_TOKEN, "0"))
                put("profile_view", isShowProfileHistory)
            }
            userRepository.callApiEditProfile(param, _editProfileLiveData)
        }
    }



    var noDataLayoutVisibility = ObservableBoolean(false)
    var loadMoreProgressVisibility = ObservableBoolean(false)

    fun showNoDataView(){
        noDataLayoutVisibility.set(true)
    }

    fun showDataView(){
        noDataLayoutVisibility.set(false)
    }

}
