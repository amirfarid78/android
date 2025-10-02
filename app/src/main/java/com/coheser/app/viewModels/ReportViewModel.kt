package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.ReportTypeModel
import com.coheser.app.repositories.ReportRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class ReportViewModel(
    private val context: Context,
    private val reportRepository: ReportRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }



    val _reportLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val reportLiveData: LiveData<ApiResponce<String>> get() = _reportLiveData

    fun reportVideo(videoId:String,reportID:String,description:String){
        viewModelScope.launch {
            val param = JSONObject().apply {
                put("user_id",sharedPreferences.getString(Variables.U_ID, ""))
                put("video_id", videoId)
                put("report_reason_id", reportID)
                put("description", description)
            }
            reportRepository.reportVideo(param, _reportLiveData)
        }
    }
    fun report(id : String,type :String,reasonId : String,description : String){
        viewModelScope.launch {
            val param = JSONObject().apply {
                put("type",type)
                put("value", id)
                put("report_reason_id", reasonId)
                put("description", description)
            }
            reportRepository.report(param, _reportLiveData)
        }
    }

    fun reportUser(userId: String,reportID:String,description:String){
        viewModelScope.launch {
            val param = JSONObject().apply {
                put("report_user_id", userId)
                put("report_reason_id", reportID)
                put("description", description)
            }

            reportRepository.reportUser(param, _reportLiveData)
        }
    }

    fun reportRoom(roomId:String,reportID:String,description:String){
        viewModelScope.launch {
            val param = JSONObject().apply {
                put("user_id", sharedPreferences.getString(Variables.U_ID, ""))
                put("room_id", roomId)
                put("report_reason_id", reportID)
                put("description", description)
            }
            reportRepository.reportRoom(param, _reportLiveData)
        }
    }

    fun reportProduct(productId:String,reportID:String,description:String){
       viewModelScope.launch {
            val param = JSONObject().apply {
                put("user_id", sharedPreferences.getString(Variables.U_ID, ""))
                put("product_id", productId)
                put("report_reason_id", reportID)
                put("description", description)
            }
            reportRepository.reportProduct(param, _reportLiveData)
        }
    }




    val _reportTypeLiveData: MutableLiveData<ApiResponce<ArrayList<ReportTypeModel>>> = MutableLiveData()
    val reportTypeLiveData: LiveData<ApiResponce<ArrayList<ReportTypeModel>>> get() = _reportTypeLiveData
    fun showReportReasons(){
        viewModelScope.launch {
            val param = JSONObject()
            reportRepository.showReportReasons(param, _reportTypeLiveData)
        }
    }



}
