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
import com.coheser.app.models.HashTagModel
import com.coheser.app.models.HomeModel
import com.coheser.app.repositories.VideosRepository
import com.coheser.app.simpleclasses.Functions
import kotlinx.coroutines.launch
import org.json.JSONObject

class TaggedVideoViewModel(
    private val context: Context,
    private val videoRepository: VideosRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }


    lateinit var tagId: String
    lateinit var tagTxt: String
    var favourite: String? = "0"
    var ispostFinsh = false

    var pageCount: ObservableInt = ObservableInt(0)



    val _hashtagModelLiveData: MutableLiveData<ApiResponce<HashTagModel>> = MutableLiveData()
    val hashtagModelLiveData: LiveData<ApiResponce<HashTagModel>> get() = _hashtagModelLiveData



    val _videosLiveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>> = MutableLiveData()
    val videosLiveData: LiveData<ApiResponce<ArrayList<HomeModel>>> get() = _videosLiveData

    fun getTaggedVideo(){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put("hashtag", tagTxt)
                put("starting_point", pageCount.get().toString())

            }
            videoRepository.showVideosAgainstHashtag(params,_videosLiveData,_hashtagModelLiveData)
        }
    }


    val _favoriteLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val favoriteLiveData: LiveData<ApiResponce<String>> get() = _favoriteLiveData

    fun addHashtagFavourite(){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put("hashtag_id", tagId)

            }
            videoRepository.addHashtagFavourite(params,_favoriteLiveData)
        }
    }


    var dataLayoutVisibility = ObservableBoolean(false)
    var noDataLayoutVisibility = ObservableBoolean(false)
    var loadMoreProgressVisibility = ObservableBoolean(false)

    fun showNoDataView(){
        dataLayoutVisibility.set(false)
        noDataLayoutVisibility.set(true)
    }


    fun showDataView(){
        dataLayoutVisibility.set(true)
        noDataLayoutVisibility.set(false)
    }



}

