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

class OthersProfileViewModel(
    private val context: Context,
    private val userRepository: UserRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }



    var userId: String? = null
    var userName: String? = null
    var userPic: String? = null


    var isDirectMessage = false


    val _userDetailLiveData: MutableLiveData<ApiResponce<UserModel>> = MutableLiveData()
    val userDetailLiveData: LiveData<ApiResponce<UserModel>> get() = _userDetailLiveData

    fun getUserDetails(){
        viewModelScope.launch {

                val param = JSONObject()
            if(sharedPreferences.getBoolean(Variables.IS_LOGIN,false)) {
                param.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""))
                (if (userId != null)
                    param.put("other_user_id", userId)
                else
                   param.put("username", userName))
            }else{
                (if (userId != null)
                    param.put("user_id", userId)
                else
                    param.put("username", userName))
            }
            userRepository.showUserDetail(param, _userDetailLiveData)
            }
        }



    val _userModel: MutableLiveData<UserModel> = MutableLiveData()
    val userModel: LiveData<UserModel> get() = _userModel

    fun setData(userModel: UserModel){
        _userModel.value=userModel
    }



    val _blockUserLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val blockUserLiveData: LiveData<ApiResponce<String>> get() = _blockUserLiveData

    fun blockUser(){
        viewModelScope.launch {

            val param = JSONObject()
            if(sharedPreferences.getBoolean(Variables.IS_LOGIN,false)) {
                param.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""))
                param.put("block_user_id", userId)
            }
            userRepository.callApiBlockUser(param, _blockUserLiveData)
        }
    }






    val _suggesstionLiveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>> = MutableLiveData()
    val suggesstionLiveData: LiveData<ApiResponce<ArrayList<UserModel>>> get() = _suggesstionLiveData
    fun getSuggesstionList(){
        viewModelScope.launch {
            val param = JSONObject().apply {
                put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, "0"))
                put("other_user_id", userId)
                put("starting_point", "0")
            }
            userRepository.getSuggestionUserList(param, _suggesstionLiveData)
        }
    }



    val _followLiveData: MutableLiveData<ApiResponce<UserModel>> = MutableLiveData()
    val followLiveData: LiveData<ApiResponce<UserModel>> get() = _followLiveData

    fun followUser(){
        viewModelScope.launch {
            val param = JSONObject()
            param.put("sender_id", sharedPreferences.getString(Variables.U_ID, "0"))
            param.put("receiver_id", userId)
            userRepository.callApiFollowUser(param, _followLiveData)
        }
    }


    val _followSuggesstionLiveData: MutableLiveData<ApiResponce<UserModel>> = MutableLiveData()
    val followSuggesstionLiveData: LiveData<ApiResponce<UserModel>> get() = _followSuggesstionLiveData
    fun followSuggestionUser(userId:String){
        viewModelScope.launch {
            val param = JSONObject()
            param.put("sender_id", sharedPreferences.getString(Variables.U_ID, "0"))
            param.put("receiver_id", userId)
            userRepository.callApiFollowUser(param, _followSuggesstionLiveData)
        }
    }




}
