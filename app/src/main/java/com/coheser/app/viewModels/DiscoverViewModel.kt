package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.activitesfragments.spaces.models.RoomModel
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.DiscoverModel
import com.coheser.app.models.UserModel
import com.coheser.app.repositories.RoomRepository
import com.coheser.app.repositories.ShopRepository
import com.coheser.app.repositories.VideosRepository
import com.coheser.app.simpleclasses.Functions
import kotlinx.coroutines.launch
import org.json.JSONObject

class DiscoverViewModel(
    private val context: Context,
    private val videoRepository: VideosRepository,
    private val shopRepository: ShopRepository,
    private val roomRepository: RoomRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }


    var ispostFinsh = false
    var pageCount: ObservableInt = ObservableInt(0)



    val _videosLiveData: MutableLiveData<ApiResponce<ArrayList<DiscoverModel>>> = MutableLiveData()
    val videosLiveData: LiveData<ApiResponce<ArrayList<DiscoverModel>>> get() = _videosLiveData

    fun showDiscoverySections(){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put("starting_point", pageCount.get().toString())

            }
            videoRepository.showDiscoverySections(params,_videosLiveData)
        }
    }


    val _shopsLiveData:MutableLiveData<ApiResponce<ArrayList<UserModel>>> = MutableLiveData()
    val shopsLiveData:LiveData<ApiResponce<ArrayList<UserModel>>> get() = _shopsLiveData
    fun showShops(){
        viewModelScope.launch {
            val params = JSONObject().apply {
                put("starting_point", pageCount.get().toString())
            }

            shopRepository.getShopsList(params,_shopsLiveData)
        }
    }


    val _RoomLiveData:MutableLiveData<ApiResponce<ArrayList<RoomModel>>> = MutableLiveData()
    val roomLiveData:LiveData<ApiResponce<ArrayList<RoomModel>>> get() = _RoomLiveData
    fun showRoom(){
        viewModelScope.launch {
            val params = JSONObject().apply {
                put("starting_point", pageCount.get().toString())
            }

            roomRepository.getRoomList(params,_RoomLiveData)
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
