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

class ProfileVerificationViewModel(
    private val context: Context,
    private val userRepository: UserRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }

    val _verificationLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val verificationLiveData: LiveData<ApiResponce<String>> get() = _verificationLiveData
    fun callApiRequestVerification(params: JSONObject){
        viewModelScope.launch {
            userRepository.callApiProfileVerification(params, _verificationLiveData)
        }
    }


}


