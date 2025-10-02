package com.coheser.app.repositories

import androidx.lifecycle.MutableLiveData
import com.coheser.app.Constants
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.apiclasses.BaseApi
import com.coheser.app.apiclasses.RetrofitApi
import com.coheser.app.simpleclasses.Functions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call

/**
 * Base repository class to make API calls to the server from the viewRoot model.
 */
abstract class BaseRepository<T> {

    protected lateinit var apiInterface: BaseApi

    /**
     * Make an API call to the server
     *
     * @param params The parameters to be sent to the server
     * @param endpoint The endpoint to be called
     * @param liveData The live data to be updated
     * @param parseData The function to parse the response data
     */
    protected suspend fun <R> makeApiCall(
        params: JSONObject,
        endpoint: (String) -> Call<String>,
        liveData: MutableLiveData<ApiResponce<R>>,
        parseData: (ApiResponseData) -> R
    ) {
        apiInterface = RetrofitApi.getRetrofitInstance().create(BaseApi::class.java)

        liveData.value = ApiResponce.Loading()
        Functions.printLog(Constants.tag, endpoint.javaClass.name+"-"+params.toString())

        withContext(Dispatchers.IO) {

            try {

                val response = endpoint(params.toString()).execute()

                if (response.isSuccessful && response.body() != null) {
                    val rawResponse = response.body()
                    Functions.printLog(Constants.tag, endpoint.javaClass.name + "-" + rawResponse)

                    try {
                        val respObject = JSONObject(rawResponse.toString())
                        val code = respObject.optInt("code")
                        if (code == 200) {
                            val apiResponseData = when (val msg = respObject.opt("msg")) {
                                is JSONArray -> ApiResponseData.JsonArray(msg)
                                is JSONObject -> ApiResponseData.JsonObject(msg)
                                is String -> ApiResponseData.JsonString(msg)
                                is Int -> ApiResponseData.JsonString(msg.toString())
                                else -> throw IllegalArgumentException("Unsupported response type")
                            }
                            liveData.postValue(ApiResponce.Success(parseData(apiResponseData)))
                        } else {
                            liveData.postValue(
                                ApiResponce.Error(
                                    respObject.optString("msg"),
                                    false
                                )
                            )
                        }
                    } catch (e: JSONException) {
                        Functions.printLog(Constants.tag, "JSON Parsing Error: ${e.message}")
                        liveData.postValue(
                            ApiResponce.Error(
                                "Invalid JSON format in response",
                                false
                            )
                        )
                    }
                } else {
                    liveData.postValue(ApiResponce.Error("Error", false))
                }

            }catch (e:Exception){

            }

        }
    }

}

/**
 * Sealed class to represent the different types of API response data.
 */
sealed class ApiResponseData {
    data class JsonObject(val data: JSONObject) : ApiResponseData()
    data class JsonArray(val data: JSONArray) : ApiResponseData()
    data class JsonString(val data: String) : ApiResponseData()
}