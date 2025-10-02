package com.coheser.app.repositories

import androidx.lifecycle.MutableLiveData
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.StripeModel
import org.json.JSONObject

class StripeRepository : BaseRepository<StripeModel>(){
    suspend fun purchaseFromCard(
        param : JSONObject,
        liveData: MutableLiveData<ApiResponce<StripeModel>>
    ){
        try {
            val endpoint = { requestBody : String ->
                apiInterface.purchaseFromCard(requestBody)
            }
            val parsedata = { apiResponseData : ApiResponseData ->

                val stripeModel = when(apiResponseData){
                    is ApiResponseData.JsonObject ->{
                        val msg = apiResponseData.data
                        val stripeModel = StripeModel(
                            id = msg.optString("id"),
                            url = msg.optString("url"),
                            success_url = msg.optString("success_url"),
                            cancel_url = msg.optString("cancel_url")
                        )
                        stripeModel
                    }
                    else -> throw IllegalArgumentException("Unsupported response type")
                }
                stripeModel
            }
            makeApiCall(param,endpoint,liveData,parsedata)
        }catch (e:Exception){}
    }


    suspend fun purchaseProduct(
        param : JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>
    ){
        try {
            val endpoint = { requestBody : String ->
                apiInterface.purchaseProduct(requestBody)
            }
            val parseData = { apiResponseData: ApiResponseData ->
                val msg = when (apiResponseData) {
                    is ApiResponseData.JsonObject ->{
                        apiResponseData.data.toString()
                    }
                    is ApiResponseData.JsonString -> {
                        apiResponseData.data.toString()
                    }
                    else -> throw IllegalArgumentException("Unsupported response type")
                }
                msg
            }
            makeApiCall(param,endpoint,liveData,parseData)
        }catch (e:Exception){}
    }
}