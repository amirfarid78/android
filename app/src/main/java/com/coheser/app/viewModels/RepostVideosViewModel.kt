package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.HomeModel
import com.coheser.app.repositories.VideosRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class RepostVideosViewModel(
    private val context: Context,
    private val videoRepository: VideosRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }


    var ispostFinsh = false
    var isApiRun = false
    var isScrollToTop=true

    var isMyProfile = false
    var userName: String? = null
    var isUserAlreadyBlock: String? = null



    var pageCount: ObservableInt = ObservableInt(0)

    val _videosLiveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>> = MutableLiveData()
    val videosLiveData: LiveData<ApiResponce<ArrayList<HomeModel>>> get() = _videosLiveData

    fun getRepostVideo(userId:String){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put("user_id", sharedPreferences.getString(Variables.U_ID, ""));
                if(!isMyProfile) {
                    put("other_user_id", userId);
                }
                put("starting_point", pageCount.get().toString())

            }
            videoRepository.showUserRepostedVideos(params,_videosLiveData)
        }
    }


    var noDataLayoutVisibility = ObservableBoolean(false)
    var loadMoreProgressVisibility = ObservableBoolean(false)

    private val _noDataMsgTxt = MutableLiveData<String>()
    val noDataMsgTxt: LiveData<String> get() = _noDataMsgTxt

    private val _noDataTitleTxt = MutableLiveData<String>()
    val noDataTitleTxt: LiveData<String> get() = _noDataTitleTxt

    fun showNoDataView(){
        noDataLayoutVisibility.set(true)
        _noDataTitleTxt.value = context.getString(R.string.no_data_found)
        if(isMyProfile) {
            _noDataMsgTxt.value = context.getString(R.string.you_has_not_repost_any_video)
        }
        else{
            _noDataMsgTxt.value = context.getString(R.string.this_user_has_not_repost_any_video)
        }
    }

    fun showDataView(){
        noDataLayoutVisibility.set(false)
    }


    fun showBlockView(userName:String){
        noDataLayoutVisibility.set(true)
        _noDataTitleTxt.value = context.getString(R.string.alert)
        _noDataMsgTxt.value=context.getString(R.string.you_are_block_by)+ " " + userName

    }

}

