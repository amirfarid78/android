package com.coheser.app.repositories

import androidx.lifecycle.MutableLiveData
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.DataParsing
import org.json.JSONObject

class ShopRepository : BaseRepository<UserModel>() {


    suspend fun getShopsList(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.showShops(requestBody)
        }
        val parseData = { apiResponseData: ApiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray -> {
                    val list = ArrayList<UserModel>()
                    for (i in 0 until apiResponseData.data.length()) {
                        val `object` = apiResponseData.data.optJSONObject(i)
                        val userDetailModel =
                            DataParsing.getUserDataModel(`object`.optJSONObject("User"))

                        list.add(userDetailModel)
                    }
                    list
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)

        }catch (e:Exception){}
    }

}