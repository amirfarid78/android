package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.HomeModel
import com.coheser.app.repositories.VideosRepository
import com.coheser.app.simpleclasses.Functions
import kotlinx.coroutines.launch
import org.json.JSONObject

class NearByVideoViewModel(
    private val context: Context,
    private val videoRepository: VideosRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }



    lateinit var placeId: String
    lateinit var lat: String
    lateinit var lng: String
    var locImage = ""

    var ispostFinsh = false
    var pageCount: ObservableInt = ObservableInt(0)





    val _videosLiveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>> = MutableLiveData()
    val videosLiveData: LiveData<ApiResponce<ArrayList<HomeModel>>> get() = _videosLiveData

    fun showVideosAgainstLocation(){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put("location_id", placeId)
                put("starting_point", pageCount.get().toString())

            }
            videoRepository.showVideosAgainstLocation(params,_videosLiveData)
        }
    }


    var noDataLayoutVisibility = ObservableBoolean(false)
    var loadMoreProgressVisibility = ObservableBoolean(false)

    fun showNoDataView(){
        noDataLayoutVisibility.set(true)
    }

    fun showDataView(){
        noDataLayoutVisibility.set(false)
    }

}
