package com.coheser.app.activitesfragments.profile

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.composeScreens.ProfileViewHistoryScreen
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.DarkModePrefManager
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.ProfileViewsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ViewProfileHistoryActivity2 : ComponentActivity() {

    private val viewModel: ProfileViewsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE), this, javaClass, false)

        if(DarkModePrefManager(this).isNightMode) {
            val newConfig = Configuration(resources.configuration)
            newConfig.uiMode = Configuration.UI_MODE_NIGHT_YES
            resources.updateConfiguration(newConfig, resources.displayMetrics)
        }

      setContent {
          ProfileViewHistoryScreen(viewModel,
              onbackpress = { onBackPressed() },
              onProfileClick = { item->
                  openProfile(item)
              },
              onFollowClick = { item->
                  if (Functions.checkLoginUser(this@ViewProfileHistoryActivity2)) {
                      if (item.id != getSharedPreference(this).getString(Variables.U_ID, "")) {
                          viewModel.followUser(""+item?.id)
                      }
                  }
              }
          )
      }

        setObserveAble()
        if(viewModel.isShowProfileHistory.equals("1")) {
            viewModel.getProfileViewsList()
        }

    }


    fun setObserveAble(){

        viewModel.followLiveData.observe(this,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let { userModel->
                        if (userModel != null) {
                            val list=viewModel.listLiveData.value?.data
                            for ((index,item) in list!!.withIndex()) {
                                if(item.id.equals(userModel.id)){

                                    list!![index] = userModel
                                    break
                                }
                            }
                            viewModel._listLiveData.value = ApiResponce.Success(list)

                        }
                    }

                }
                else -> {}
            }
        })

        viewModel.editProfileLiveData.observe(this,{
            when(it){
                is ApiResponce.Loading->{
                    Functions.showLoader(this, false, false)
                }

                is ApiResponce.Success ->{
                    Functions.cancelLoader()
                    viewModel.isActivityCallback = true
                }

                is ApiResponce.Error ->{
                    Functions.cancelLoader()
                    if (it.message != null) {
                        Functions.showToast(this,it.message)
                    }
                }
            }
        })

        viewModel.isShowProfileHistory.observe(this,{
            if(it.equals("1")) {
                viewModel.getProfileViewsList()
            }
        })
    }


    private fun openProfile(item: UserModel?) {
        if (Functions.checkProfileOpenValidation(item?.id)) {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user_id", "" + item?.id)
            intent.putExtra("user_name", "" + item?.username)
            intent.putExtra("user_pic", "" + item?.getProfilePic())
            startActivity(intent)
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        }
    }


    override fun onBackPressed() {
        if (viewModel.isActivityCallback) {
            val intent = Intent()
            intent.putExtra("isShow", true)
            setResult(RESULT_OK, intent)
        }
        finish()
    }

}