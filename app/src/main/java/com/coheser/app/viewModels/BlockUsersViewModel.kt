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
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class BlockUsersViewModel(
    private val context: Context,
    private val userRepository: UserRepository
) : ViewModel(){

    private var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }

    var unblockPosition=0
    var noDataLayoutVisibility = ObservableBoolean(false)


    val _blockUserLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val blockUserLiveData: LiveData<ApiResponce<String>> get() = _blockUserLiveData

    fun blockUser(userId:String,unblockPosition:Int){
        this.unblockPosition=unblockPosition
        viewModelScope.launch {

            val param = JSONObject()
            if(sharedPreferences.getBoolean(Variables.IS_LOGIN,false)) {
                param.put("block_user_id", userId)
            }
            userRepository.callApiBlockUser(param, _blockUserLiveData)
        }
    }


    val _blockUsersLiveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>> = MutableLiveData()
    val blockUsersLiveData: LiveData<ApiResponce<ArrayList<UserModel>>> get() = _blockUsersLiveData
    fun getblockUsersList(){
        viewModelScope.launch {
            val param = JSONObject()
            userRepository.getBlockUserList(param, _blockUsersLiveData)
        }
    }



    fun showNoDataView(){
        noDataLayoutVisibility.set(true)

    }

    fun showDataView(){
        noDataLayoutVisibility.set(false)
    }





}
