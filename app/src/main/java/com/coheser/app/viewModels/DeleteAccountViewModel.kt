package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.repositories.UserRepository
import com.coheser.app.simpleclasses.Functions
import kotlinx.coroutines.launch
import org.json.JSONObject

class DeleteAccountViewModel(
    private val context: Context,
    private val userRepository: UserRepository

) : ViewModel(){


    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }


    val _deleteUserLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val deleteUserLiveData: LiveData<ApiResponce<String>> get() = _deleteUserLiveData

    fun deleteUserAccount(){
        viewModelScope.launch {

            val param = JSONObject()
            userRepository.callApiDeleteUser(param, _deleteUserLiveData)
        }
    }




}
