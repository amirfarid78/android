package com.coheser.app.repositories

import androidx.lifecycle.MutableLiveData
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.PayoutModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.Functions
import com.example.example.WithDrawalModel
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

class WalletRepository : BaseRepository<R>() {

    suspend fun addPayout(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>>
    ) {
        try {

        Functions.printLog(Constants.tag, "addPayout")
        makeApiCall(
            params,
            endpoint = { body -> apiInterface.addPayout(body) },
            liveData,
            parseData = { apiResponseData ->
                parseAddPayout((apiResponseData as ApiResponseData.JsonObject).data)
            }
        )

        }catch (e:Exception){}
    }

    private fun parseAddPayout(msgObject: JSONObject): ArrayList<UserModel> {
        val tempList = ArrayList<UserModel>()
        val userDetailModel = getUserDataModel(msgObject.optJSONObject("User"))
        val value = msgObject.optJSONObject("Payout")?.optString("value")
        userDetailModel.paypal = value
        tempList.add(userDetailModel)
        return tempList
    }

    suspend fun showPayout(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<PayoutModel>>>
    ) {
        try {


        Functions.printLog(Constants.tag, "showPayout")
        makeApiCall(
            params,
            endpoint = { body -> apiInterface.showPayout(body) },
            liveData,
            parseData = { apiResponseData ->
                parseShowPayout((apiResponseData as ApiResponseData.JsonObject).data)
            }
        )

        }catch (e:Exception){}
    }

    private fun parseShowPayout(msgJson: JSONObject): ArrayList<PayoutModel> {
        val tempList = ArrayList<PayoutModel>()
        val payoutModel = Gson().fromJson(
            msgJson.optJSONObject("Payout").toString(),
            PayoutModel::class.java
        )
        tempList.add(payoutModel)
        return tempList
    }

    suspend fun withdrawRequest(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>>
    ) {

        try {


        Functions.printLog(Constants.tag, "withdrawRequest")
        makeApiCall(
            params,
            endpoint = { body -> apiInterface.withdrawRequest(body) },
            liveData,
            parseData = { apiResponseData ->
                parserWithdraw((apiResponseData as ApiResponseData.JsonObject).data)
            }
        )

        }catch (e:Exception){}

    }

    private fun parserWithdraw(jsonObject: JSONObject): ArrayList<UserModel> {
        var templist = ArrayList<UserModel>()
        val userDetailModel = getUserDataModel(jsonObject.optJSONObject("User"))
        templist.add(userDetailModel)
        return templist
    }

    suspend fun showWithdrawalHistory(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<WithDrawalModel>>>
    ) {
        try {


        Functions.printLog(Constants.tag, "showWithdrawalHistory")
        makeApiCall(
            params,
            endpoint = { body -> apiInterface.showWithdrawalHistory(body)},
            liveData,
            parseData = { apiResponseData ->
                parseShowWithdrawHistory((apiResponseData as ApiResponseData.JsonArray).data)
            }
        )

        }catch (e:Exception){}
    }

    private fun parseShowWithdrawHistory(jsonArray: JSONArray): ArrayList<WithDrawalModel> {
        var templist = ArrayList<WithDrawalModel>()
        for (i in 0 until jsonArray.length()) {
            val order = jsonArray.getJSONObject(i)
            val item =
                Gson().fromJson(order.toString(), WithDrawalModel::class.java)
            templist.add(item)
        }
        return templist
    }


    suspend fun purchaseCoins(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>
    ) {

        try {
            val endpoint = { requestBody : String ->
                apiInterface.purchaseCoin(requestBody)
            }
            val parseData={ apiResponseData:ApiResponseData ->
                (apiResponseData as ApiResponseData.JsonObject).data.toString()
             }

            makeApiCall(params,
                endpoint,
                liveData,
                parseData
            )

        }catch (e:Exception){}

    }

}