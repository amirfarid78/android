package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.activitesfragments.location.DeliveryAddress
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.repositories.AddressRepository
import com.coheser.app.simpleclasses.Functions
import kotlinx.coroutines.launch
import org.json.JSONObject

class AddressViewModel (
    private val context: Context,
    private val addressRepository: AddressRepository
) : ViewModel(){
    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences= Functions.getSharedPreference(context)
    }


    val _addressLiveData: MutableLiveData<ApiResponce<ArrayList<DeliveryAddress>>> = MutableLiveData()
    val addressLiveData: LiveData<ApiResponce<ArrayList<DeliveryAddress>>> get() = _addressLiveData

    fun showDeliveryAddress(){
        viewModelScope.launch {
            val params = JSONObject()
            addressRepository.showDeliveryAddresses(params,_addressLiveData)
        }
    }

    fun saveDeliveryAddress(params: JSONObject){
        viewModelScope.launch {
            addressRepository.addDeliveryAddress(params,_addressLiveData)
        }
    }
    val _deleteAddressLiveData: MutableLiveData<ApiResponce<ArrayList<String>>> = MutableLiveData()
    val deleteAddressLiveData: LiveData<ApiResponce<ArrayList<String>>> get() = _deleteAddressLiveData
    fun deleteDeliveryAddress(addressId: String){
        viewModelScope.launch {
            val params = JSONObject()
            try {
               params.put("id", addressId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            addressRepository.deleteDeliveryAddresses(params,_deleteAddressLiveData)
        }
    }


}
