package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.UserModel
import com.coheser.app.repositories.WalletRepository
import com.coheser.app.simpleclasses.Functions
import kotlinx.coroutines.launch
import org.json.JSONObject

class AddPayoutViewModel(
    private val context: Context,
    private val walletRepository: WalletRepository
):ViewModel() {
    private var sharedPreferences: SharedPreferences
    init {
        sharedPreferences= Functions.getSharedPreference(context)
    }
    val _addPayoutLiveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>> = MutableLiveData()
    val payoutLiveData: LiveData<ApiResponce<ArrayList<UserModel>>> get() = _addPayoutLiveData

    fun addPayout(type: String, email: String){
        viewModelScope.launch {
            val params = JSONObject()
            params.put("value", email)
            params.put("type", type)

            walletRepository.addPayout(params,_addPayoutLiveData)
        }
    }
}