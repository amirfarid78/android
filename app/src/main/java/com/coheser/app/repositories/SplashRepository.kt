package com.coheser.app.repositories

import androidx.lifecycle.MutableLiveData
import com.coheser.app.Constants
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.apiclasses.BaseApi
import com.coheser.app.apiclasses.RetrofitApi
import com.coheser.app.models.SettingsModel
import com.coheser.app.simpleclasses.Functions
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SplashRepository {
    lateinit var apiInterface: BaseApi

    suspend fun showSettings(
        params : JSONObject,
        liveData : MutableLiveData<ApiResponce<ArrayList<SettingsModel>>>
    ){
        apiInterface = RetrofitApi.getRetrofitInstance().create(BaseApi::class.java)
        liveData.value = ApiResponce.Loading()
        Functions.printLog(Constants.tag,"showSettings :"+params.toString())

        withContext(Dispatchers.IO){
            try {

                val responce = apiInterface.showSettings(params.toString()).execute()
                if (responce.isSuccessful && responce.body() != null) {
                    Functions.printLog(Constants.tag, "showSettings: ${responce.body()}")
                    try {
                        val respObjects = JSONObject(responce.body()!!)
                        val code = respObjects.optInt("code")
                        if (code == 200) {
                            val tempList = ArrayList<SettingsModel>()
                            val msgArray = respObjects.getJSONArray("msg")
                            if (msgArray.length() > 0) {
                                for (i in 0 until msgArray.length()) {
                                    val dataObj = msgArray.getJSONObject(i)
                                    val settings = Gson().fromJson(
                                        dataObj.optJSONObject("Setting").toString(),
                                        SettingsModel::class.java
                                    )
                                    tempList.add(settings)
                                }
                            }
                            liveData.postValue(ApiResponce.Success(tempList))
                        } else {
                            liveData.postValue(
                                ApiResponce.Error(
                                    respObjects.optString("msg").toString(), false
                                )
                            )
                        }
                    } catch (e: Exception) {
                    }

                }

            }catch (e:Exception){}
        }
    }

}