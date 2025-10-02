package com.coheser.app.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.activitesfragments.location.DeliveryAddress
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.HomeModel
import com.coheser.app.models.SettingsModel
import com.coheser.app.repositories.AddressRepository
import com.coheser.app.repositories.SplashRepository
import com.coheser.app.repositories.VideosRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class SplashViewModel(
    private val context: Context,
    private val settingRepository: SplashRepository,
    private val addressRepository: AddressRepository,
    private val videoRepository: VideosRepository
): ViewModel() {

    val _settingsLiveData: MutableLiveData<ApiResponce<ArrayList<SettingsModel>>> = MutableLiveData()
    val settingsLiveData: LiveData<ApiResponce<ArrayList<SettingsModel>>> get() = _settingsLiveData

    val _deliveryAddressLiveData: MutableLiveData<ApiResponce<ArrayList<DeliveryAddress>>> = MutableLiveData()
    val deliveryAddressLiveData: LiveData<ApiResponce<ArrayList<DeliveryAddress>>> get() = _deliveryAddressLiveData


    val _adLiveData: MutableLiveData<ApiResponce<HomeModel>> = MutableLiveData()
    val adLiveData: LiveData<ApiResponce<HomeModel>> get() = _adLiveData


    fun showSettings(){
        viewModelScope.launch {
            val params = JSONObject()
            settingRepository.showSettings(params,_settingsLiveData)
        }

    }

    fun getAddressList(){
        viewModelScope.launch {
            val param = JSONObject()
            param.put("user_id",
                Functions.getSharedPreference(context)
                    .getString(Variables.U_ID, "")
            )

            addressRepository.showDeliveryAddresses(param, _deliveryAddressLiveData)
        }
    }

    fun getAdsVideo(){
        viewModelScope.launch {
            val param = JSONObject()
            if(Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN,false)) {
                param.put(
                    "user_id",
                    Functions.getSharedPreference(context)
                        .getString(Variables.U_ID, "")
                )
            }

            videoRepository.showVideoDetailAd(param, _adLiveData)
        }
    }

}

