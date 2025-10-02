package com.coheser.app.repositories

import androidx.lifecycle.MutableLiveData
import com.coheser.app.Constants
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.activitesfragments.location.DeliveryAddress
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.google.gson.Gson
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AddressRepository : BaseRepository<DeliveryAddress>() {

    suspend fun showDeliveryAddresses(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<DeliveryAddress>>>
    ) {
        try {

        Functions.printLog(Constants.tag, "showDeliveryAddresses")
        makeApiCall(
            params,
            endpoint = { body -> apiInterface.showDeliveryAddresses(body) },
            liveData,
            parseData = { apiResponseData ->
                parseAddressData((apiResponseData as ApiResponseData.JsonArray).data)
            }
        )

    }catch (e:Exception){}
    }

    suspend fun getAddress(): DeliveryAddress? {
        return withContext(Dispatchers.Default) {
            Paper.book().read<DeliveryAddress>(Variables.AdressModel)
        }
    }

    private fun parseAddressData(msgArray: JSONArray): ArrayList<DeliveryAddress> {
        val tempList = ArrayList<DeliveryAddress>()
        for (i in 0 until msgArray.length()) {
            val dataobj = msgArray.getJSONObject(i)
            val deliveryAddress = Gson().fromJson(
                dataobj.optJSONObject("DeliveryAddress")?.toString() ?: "{}",
                DeliveryAddress::class.java
            )
            tempList.add(deliveryAddress)
        }

        return tempList
    }

    suspend fun addDeliveryAddress(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<DeliveryAddress>>>
    ) {
        try {

        Functions.printLog(Constants.tag, "addDeliveryAddress")
        makeApiCall(
            params,
            endpoint = { body -> apiInterface.addDeliveryAddress(body) },
            liveData,
            parseData = { apiResponseData ->
                parseSaveAddress((apiResponseData as ApiResponseData.JsonObject).data)
            }
        )
        }catch (e:Exception){}
    }
    private fun parseSaveAddress(jsonObject: JSONObject) :ArrayList<DeliveryAddress>{
        val tempList = ArrayList<DeliveryAddress>()
        val model = Gson().fromJson(
            jsonObject.optJSONObject("DeliveryAddress")?.toString() ?: "{}",
            DeliveryAddress::class.java
        )
        tempList.add(model)
        return tempList
    }

    suspend fun deleteDeliveryAddresses(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<String>>>
    ) {
        try {


        Functions.printLog(Constants.tag, "deleteDeliveryAddress")
        val deleteParse: (String) -> ArrayList<String> = { jsonString ->
            val tempList = arrayListOf<String>()
            tempList.add(jsonString)
            tempList
        }
        makeApiCall(
            params,
            endpoint = { body -> apiInterface.deleteDeliveryAddress(body) },
            liveData,
            parseData = { apiResponseData ->
                deleteParse((apiResponseData as ApiResponseData.JsonString).data)
            }
        )
        }catch (e:Exception){}
    }
}
