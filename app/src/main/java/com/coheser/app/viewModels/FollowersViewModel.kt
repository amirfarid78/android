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
import com.coheser.app.models.UserModel
import com.coheser.app.repositories.UserRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class FollowersViewModel(
    private val context: Context,
    private val userRepository: UserRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }



    var fromWhere = "fan"
    var ispostFinsh = false
    var isMyProfile = true
    var isFromTab = false
    var isApiRun = false


    var pageCount: ObservableInt = ObservableInt(0)

    val _listLiveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>> = MutableLiveData()
    val listLiveData: LiveData<ApiResponce<ArrayList<UserModel>>> get() = _listLiveData

    fun getFollowersList(userId:String){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put("user_id", sharedPreferences.getString(Variables.U_ID, ""));
                if(!isMyProfile) {
                    put("other_user_id", userId)
                }
                put("starting_point", pageCount.get().toString())

            }
            userRepository.showFollowers(params,_listLiveData)

        }
    }

    fun getFollowingList(userId:String){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put("user_id", sharedPreferences.getString(Variables.U_ID, ""));
                if(!isMyProfile) {
                    put("other_user_id", userId)
                }
                put("starting_point", pageCount.get().toString())

            }
            userRepository.showFollowing(params,_listLiveData)

        }
    }

    fun getFollowingSearch(userId:String,keyword:String){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put("user_id", userId)
                put("type", "following")
                put("keyword", keyword)
                put("starting_point", pageCount.get().toString())
            }
            userRepository.getSearchUserList(params,_listLiveData)

        }
    }


    fun getSuggesstionList(userId:String){
        viewModelScope.launch {
            val param = JSONObject().apply {
                put("user_id", userId);
                put("starting_point", pageCount.get().toString())
            }
            userRepository.getSuggestionUserList(param, _listLiveData)
        }
    }


    val _followLiveData: MutableLiveData<ApiResponce<UserModel>> = MutableLiveData()
    val followLiveData: LiveData<ApiResponce<UserModel>> get() = _followLiveData

    fun followUser(userId:String){
        viewModelScope.launch {
            val param = JSONObject().apply {
                put("sender_id", sharedPreferences.getString(Variables.U_ID, "0"))
                put("receiver_id", userId)
            }
            userRepository.callApiFollowUser(param, _followLiveData)
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
