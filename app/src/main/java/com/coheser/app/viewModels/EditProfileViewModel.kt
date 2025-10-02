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
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class EditProfileViewModel(
    private val context: Context,
    private val userRepository: UserRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }



    val _userDetailLiveData: MutableLiveData<ApiResponce<UserModel>> = MutableLiveData()
    val userDetailLiveData: LiveData<ApiResponce<UserModel>> get() = _userDetailLiveData

    fun getUserDetails(){
        viewModelScope.launch {

            val param = JSONObject()
            if(sharedPreferences.getBoolean(Variables.IS_LOGIN,false)) {
                param.put("auth_token", Functions.getSharedPreference(context).getString(Variables.AUTH_TOKEN, ""))
            }
            userRepository.showUserDetail(param, _userDetailLiveData)
        }
    }




    val _editProfileLiveData: MutableLiveData<ApiResponce<UserModel>> = MutableLiveData()
    val editProfileLiveData: LiveData<ApiResponce<UserModel>> get() = _editProfileLiveData

    fun editProfile(params:JSONObject){
        viewModelScope.launch {
            userRepository.callApiEditProfile(params, _editProfileLiveData)
        }
    }



}
