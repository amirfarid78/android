package com.coheser.app.repositories

import androidx.lifecycle.MutableLiveData
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.ReportTypeModel
import com.coheser.app.models.UserModel
import org.json.JSONObject

class ReportRepository : BaseRepository<UserModel>(){

   suspend fun showReportReasons(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<ArrayList<ReportTypeModel>>>,
    ) {

       try {


       val endpoint = { requestBody: String ->
           apiInterface.showReportReasons(requestBody)
       }
       val parseData = { apiResponseData: ApiResponseData ->
           when (apiResponseData) {
               is ApiResponseData.JsonArray -> {
                   val list = ArrayList<ReportTypeModel>()
                   for (i in 0 until apiResponseData.data.length()) {
                       val itemdata = apiResponseData.data.optJSONObject(i)
                       val reportreason = itemdata.optJSONObject("ReportReason")
                       val item = ReportTypeModel()
                       item.id = reportreason.optString("id")
                       item.title = reportreason.optString("title")
                       list.add(item)
                   }

                   list

               }

               else -> throw IllegalArgumentException("Unsupported response type")
           }
       }

       makeApiCall(params, endpoint, liveData, parseData)
       }catch (e:Exception){}
    }


    suspend fun reportVideo(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.reportVideo(requestBody)
        }
        val parseData = { apiResponseData: ApiResponseData ->
            when (apiResponseData) {

                is ApiResponseData.JsonArray -> {
                    apiResponseData.data.toString()
                }
                is ApiResponseData.JsonObject -> {
                    apiResponseData.data.toString()
                }
                is ApiResponseData.JsonString -> {
                    apiResponseData.data.toString()
                }
                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }

    suspend fun report(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {

        try {


            val endpoint = { requestBody: String ->
                apiInterface.report(requestBody)
            }
            val parseData = { apiResponseData: ApiResponseData ->
                when (apiResponseData) {

                    is ApiResponseData.JsonArray -> {
                        apiResponseData.data.toString()
                    }
                    is ApiResponseData.JsonObject -> {
                        apiResponseData.data.toString()
                    }
                    is ApiResponseData.JsonString -> {
                        apiResponseData.data.toString()
                    }
                    else -> throw IllegalArgumentException("Unsupported response type")
                }
            }

            makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }


    suspend fun reportUser(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {

        try {


        val endpoint = { requestBody: String ->
            apiInterface.reportUser(requestBody)
        }
        val parseData = { apiResponseData: ApiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray -> {
                    apiResponseData.data.toString()
                }
                is ApiResponseData.JsonObject -> {
                    apiResponseData.data.toString()
                }
                is ApiResponseData.JsonString -> {
                    apiResponseData.data.toString()
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }


    suspend fun reportRoom(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {

        try {


        val endpoint = { requestBody: String -> apiInterface.reportRoom(requestBody) }

        val parseData = { apiResponseData: ApiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray -> {
                    apiResponseData.data.toString()
                }
                is ApiResponseData.JsonObject -> {
                    apiResponseData.data.toString()
                }
                is ApiResponseData.JsonString -> {
                    apiResponseData.data.toString()
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }

    suspend fun reportProduct(
        params: JSONObject,
        liveData: MutableLiveData<ApiResponce<String>>,
    ) {

        try {


        val endpoint = { requestBody: String -> apiInterface.reportProduct(requestBody) }

        val parseData = { apiResponseData: ApiResponseData ->
            when (apiResponseData) {
                is ApiResponseData.JsonArray -> {
                    apiResponseData.data.toString()
                }
                is ApiResponseData.JsonObject -> {
                    apiResponseData.data.toString()
                }
                is ApiResponseData.JsonString -> {
                    apiResponseData.data.toString()
                }

                else -> throw IllegalArgumentException("Unsupported response type")
            }
        }

        makeApiCall(params, endpoint, liveData, parseData)
        }catch (e:Exception){}
    }


}