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

class SearchAllUsersViewModel(
    private val context: Context,
    private val userRepository: UserRepository

) : ViewModel(){

    lateinit var sharedPreferences: SharedPreferences
    init {
        sharedPreferences=Functions.getSharedPreference(context)
    }



    var ispostFinsh = false
    var isApiRun = false


    var pageCount: ObservableInt = ObservableInt(0)

    val _listLiveData: MutableLiveData<ApiResponce<ArrayList<UserModel>>> = MutableLiveData()
    val listLiveData: LiveData<ApiResponce<ArrayList<UserModel>>> get() = _listLiveData

    fun searchUsersList(searchTxt:String){
        viewModelScope.launch {

            val params = JSONObject().apply {
                put("type", "user")
                put("keyword", searchTxt)
                put("starting_point", pageCount.get().toString())

            }
            userRepository.getSearchUserList(params,_listLiveData)

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




    var dataLayoutVisibility = ObservableBoolean(false)
    var recentLayoutVisibility = ObservableBoolean(false)
    var noDataLayoutVisibility = ObservableBoolean(false)
    var loadMoreProgressVisibility = ObservableBoolean(false)

    fun showNoDataView(){
        noDataLayoutVisibility.set(true)
    }

    fun showDataView(){
        noDataLayoutVisibility.set(false)
    }

}
