package com.coheser.app.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.repositories.UserRepository
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class ShopPViewModel(
    private val context: Context,
    private val userRepository: UserRepository
) : ViewModel() {

    val _userShopLiveData: MutableLiveData<ApiResponce<ArrayList<ProductModel>>> = MutableLiveData()
    val userShopLiveData: LiveData<ApiResponce<ArrayList<ProductModel>>> get() = _userShopLiveData

    fun showProducts(pageCount : Int,id:String) {
        viewModelScope.launch {
            val param = JSONObject()
            if (id == getSharedPreference(context).getString(Variables.U_ID, "")) {
                param.put("starting_point", "" + pageCount)
            } else {
                param.put("user_id", id)
                param.put("starting_point", "" + pageCount)
            }
            userRepository.showProducts(param, _userShopLiveData)

        }
    }

    class MyShopFactory(
        private val context: Context,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShopPViewModel::class.java)) {
                return ShopPViewModel(context, userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}