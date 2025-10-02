package com.coheser.app.viewModels

import android.content.Context
import android.content.SharedPreferences
import android.text.Html
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
import kotlinx.coroutines.launch
import org.json.JSONObject

class LikedVideosViewModel(
    private val context: Context,
    private val videoRepository: VideosRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }




    var ispostFinsh = false
    var isApiRun = false


    var isMyProfile = false
    var isLikeVideoShow = false
    var userName: String? = null
    var isUserAlreadyBlock: String? = null



    var pageCount: ObservableInt = ObservableInt(0)

    val _videosLiveData: MutableLiveData<ApiResponce<ArrayList<HomeModel>>> = MutableLiveData()
    val videosLiveData: LiveData<ApiResponce<ArrayList<HomeModel>>> get() = _videosLiveData

    fun getLikedVideo(userId:String){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put("other_user_id", userId);
                put("starting_point", pageCount.get().toString())
            }
            videoRepository.showUserLikedVideos(params,_videosLiveData)
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
        _noDataMsgTxt.value=context.getString(R.string.you_have_not_like_any_video_yet)
    }

    fun setBlockOrHideData() {
        noDataLayoutVisibility.set(true)
        if (isMyProfile) {
            _noDataTitleTxt.value = context.getString(R.string.only_you_can_see_which_video_you_liked)
            _noDataMsgTxt.value= Html.fromHtml(
                context.getString(R.string.you_can_change_this_in) + "  <font color='#B7332F'> " + context.getString(
                    R.string.privacy_setting
                ) + " </font>", Html.FROM_HTML_MODE_LEGACY
            ).toString()
        } else if (!isLikeVideoShow) {

            _noDataTitleTxt.value =context.getString(R.string.this_user_liked_video_are_private)
            _noDataMsgTxt.value=context.getString(R.string.videos_liked_by) + " " + userName + " " +context.getString(
                    R.string.are_currently_hidden
                )
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

