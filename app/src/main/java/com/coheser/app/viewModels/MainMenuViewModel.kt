package com.coheser.app.viewModels

import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coheser.app.activitesfragments.DiscoverFragment
import com.coheser.app.activitesfragments.HomeFragment
import com.coheser.app.activitesfragments.NotificationFragment
import com.coheser.app.activitesfragments.location.DeliveryAddress
import com.coheser.app.activitesfragments.profile.ProfileTabFragment
import com.coheser.app.repositories.AddressRepository
import com.coheser.app.repositories.UserRepository
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.mainmenu.BlankFragment
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainMenuViewModel(
    private val sharedPreferences: SharedPreferences,
    private val addressRepository: AddressRepository,
    private val userRepository: UserRepository
) : ViewModel(){

    val _deliveryAddressLiveData: MutableLiveData<ApiResponce<ArrayList<DeliveryAddress>>> = MutableLiveData()
    val deliveryAddressLiveData: LiveData<ApiResponce<ArrayList<DeliveryAddress>>> get() = _deliveryAddressLiveData


    val _userDetailLiveData: MutableLiveData<ApiResponce<UserModel>> = MutableLiveData()
    val userDetailLiveData: LiveData<ApiResponce<UserModel>> get() = _userDetailLiveData



    private val _tabFragments = MutableLiveData<List<Fragment>>()
    val tabFragments: LiveData<List<Fragment>> get() = _tabFragments

    // LiveData to observe tab position
    private val _currentTabPosition = MutableLiveData<Int>()
    val currentTabPosition: LiveData<Int> get() = _currentTabPosition
    init {
        registerFragmentWithPager()
    }


    // Method to initialize fragments and tabs
    private fun registerFragmentWithPager() {
        val fragments = mutableListOf<Fragment>()
        fragments.add(HomeFragment.newInstance()!!)
        fragments.add(DiscoverFragment.newInstance())
        fragments.add(BlankFragment.newInstance())
        fragments.add(NotificationFragment.newInstance())
        fragments.add(ProfileTabFragment.newInstance())


        _tabFragments.value = fragments
    }


    fun getAddressList(){
        viewModelScope.launch {
            val param = JSONObject()
            param.put("user_id",
                sharedPreferences.getString(Variables.U_ID, "")
            )

            addressRepository.showDeliveryAddresses(param, _deliveryAddressLiveData)
        }
    }


    fun getUserDetails(userName:String){
        viewModelScope.launch {
            val param = JSONObject().apply {
//                put(
//                    "auth_token", Functions.getSharedPreference(context)
//                        .getString(Variables.AUTH_TOKEN, "0")
//                )
                put("username", userName)
            }
            userRepository.showUserDetail(param, _userDetailLiveData)
        }
    }



    val _userNotificationLiveData: MutableLiveData<ApiResponce<String>> = MutableLiveData()
    val userNotificationLiveData: LiveData<ApiResponce<String>> get() = _userNotificationLiveData
    fun getUnReadNotification(){
        viewModelScope.launch {
            val param = JSONObject()
            userRepository.showUnReadNotifications(param, _userNotificationLiveData)
        }
    }

}
