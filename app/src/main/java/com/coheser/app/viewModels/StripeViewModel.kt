package com.coheser.app.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.StripeModel
import com.coheser.app.repositories.StripeRepository
import com.coheser.app.repositories.WalletRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class StripeViewModel(
    private val context : Context,
    private val stripeRepository: StripeRepository,
    private val walletRepository: WalletRepository
) :ViewModel() {
    val sharedPreferences=Functions.getSharedPreference(context)
    val _stripeDetailLivedata : MutableLiveData<ApiResponce<StripeModel>> = MutableLiveData()
    val stripeDetailLivedata : LiveData<ApiResponce<StripeModel>> get() = _stripeDetailLivedata

    fun purchaseFromCard(name : String,amount :String){
        viewModelScope.launch {
            val params = JSONObject()
            params.put("name",name)
            params.put("amount",amount)
            stripeRepository.purchaseFromCard(params,_stripeDetailLivedata)
        }
    }


    val _purchaseProductLivedata : MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val purchaseProductLivedata : LiveData<ApiResponce<String>> get() = _purchaseProductLivedata
    fun purchaseProduct(product_id : String,
                        delivery_address_id :String,
                        total :String,
                        device :String,
                        stripe_session_id :String
                        ){
        viewModelScope.launch {
            val params = JSONObject()
            params.put("product_id",product_id)
            params.put("delivery_address_id",delivery_address_id)
            params.put("total",total)
            params.put("device",device)
            params.put("stripe_session_id",stripe_session_id)
            stripeRepository.purchaseProduct(params,_purchaseProductLivedata)
        }
    }




    val _purchaseCoinsLivedata : MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val purchaseCoinLivedata : LiveData<ApiResponce<String>> get() = _purchaseCoinsLivedata
    fun purchaseCoin(coins:String,title:String,price:String,
                        stripe_session_id :String
    ){
        viewModelScope.launch {
            val params = JSONObject()
            params.put("user_id",sharedPreferences.getString(Variables.U_ID,""))
            params.put("coin",coins)
            params.put("title",title)
            params.put("price",price)
            params.put("transaction_id",stripe_session_id)
            params.put("stripe_session_id",stripe_session_id)
            params.put("device","android")
            walletRepository.purchaseCoins(params,_purchaseCoinsLivedata)
        }
    }


}
