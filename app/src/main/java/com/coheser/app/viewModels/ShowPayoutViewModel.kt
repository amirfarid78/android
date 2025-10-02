package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.PayoutModel
import com.coheser.app.repositories.WalletRepository
import com.coheser.app.simpleclasses.Functions
import kotlinx.coroutines.launch
import org.json.JSONObject

class ShowPayoutViewModel (
    private val context: Context,
    private val walletRepository: WalletRepository
): ViewModel() {
    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences= Functions.getSharedPreference(context)
    }
    val _PayoutLiveData: MutableLiveData<ApiResponce<ArrayList<PayoutModel>>> = MutableLiveData()
    val payoutLiveData: LiveData<ApiResponce<ArrayList<PayoutModel>>> get() = _PayoutLiveData

    fun showPayout(){
        viewModelScope.launch {
            val params = JSONObject()

            walletRepository.showPayout(params,_PayoutLiveData)
        }
    }
}


