package com.coheser.app.activitesfragments

import android.annotation.SuppressLint
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.accounts.AccountUtils
import com.coheser.app.activitesfragments.location.LocationPermissionActivity
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.SplashScreen1Binding
import com.coheser.app.mainmenu.MainMenuActivity
import com.coheser.app.models.HomeModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.FirebaseFunction
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.PermissionUtils
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.MyProfileViewModel
import com.coheser.app.viewModels.SplashViewModel
import io.paperdb.Paper
import org.koin.androidx.viewmodel.ext.android.viewModel


class SplashActivity : AppCompatLocaleActivity() {
    var countDownTimer: CountDownTimer? = null
    lateinit var binding: SplashScreen1Binding


    private val splashViewModel: SplashViewModel by viewModel()
    private val viewModel: MyProfileViewModel by viewModel()

    var time:Long=3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Functions.setLocale(
            Functions.getSharedPreference(this@SplashActivity)
                .getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this,
            javaClass,
            false
        )
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = DataBindingUtil.setContentView(this, R.layout.splash_screen1)

         binding.lifecycleOwner = this

        if(intent.hasExtra("openMain")){
            time=1000
            setTimer()
        }
        else{

            FirebaseFunction.isLoginToFirebase(this)

            viewModel.getUserDetails()
            apiCallHit()
            initObservers()
        }

    }

    fun initObservers() {
        splashViewModel.deliveryAddressLiveData.observe(this) {
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let { list ->
                        list.forEach {

                            if (it.defaultValue.equals("1")) {
                                Functions.printLog(
                                    Constants.tag,
                                    "deliveryAddressLiveData" + it.location_string
                                )
                                Functions.getSettingsPreference(this@SplashActivity).edit()
                                    .putString(
                                        Variables.selectedId,
                                        it.id
                                    ).apply()
                                Paper.book().write(Variables.AdressModel, it)
                            }
                        }
                    }
                }

                else -> {

                }
            }
        }
        viewModel.userDetailLiveData.observe(this,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {

                        }
                    }

                }
                is ApiResponce.Error ->{
                    if (it.message.equals("EMPTY: NO RECORD IN THE DATABASE")){
                        removePreferenceData()
                    }
                }
                else -> {}
            }
        })
        splashViewModel.settingsLiveData.observe(this) {
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let { list ->
                        val sharedPreferencesEditor =
                            Functions.getSettingsPreference(this@SplashActivity).edit()

                        list.forEach { setting ->
                            when (setting.type) {
                                "show_advert_after" -> {
                                    sharedPreferencesEditor.putInt(
                                        Variables.ShowAdvertAfter,
                                        setting.value.toInt()
                                    )
                                }

                                "coin_worth" -> {
                                    sharedPreferencesEditor.putString(
                                        Variables.CoinWorth,
                                        setting.value
                                    )
                                }

                                "add_type" -> {
                                    sharedPreferencesEditor.putString(
                                        Variables.AddType,
                                        setting.value
                                    )
                                }

                                "foodtok_comission" -> {
                                    sharedPreferencesEditor.putString(
                                        Variables.FoodtokComission,
                                        setting.value
                                    )
                                }

                                "marked_price" -> {
                                    sharedPreferencesEditor.putString(
                                        Variables.MarkedPrice,
                                        setting.value
                                    )
                                }
                            }
                        }
                        sharedPreferencesEditor.apply()
                    }
                }

                else -> {

                }
            }


        }
        splashViewModel.adLiveData.observe(this) {
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let { item ->
                        item.promote = "1"
                        Paper.book(Variables.PromoAds)
                            .write<HomeModel>(Variables.PromoAdsModel, item)
                    }
                }
                is ApiResponce.Error ->{
                    if(!it.isRequestError){
                        Paper.book(Variables.PromoAds).destroy()
                    }
                }
                else -> {

                }
            }


        }
    }
    private fun removePreferenceData() {
        Paper.book(Variables.PrivacySetting).destroy()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(this@SplashActivity, gso)
        googleSignInClient.signOut()
        AccountUtils.removeMultipleAccount(this@SplashActivity)
        val editor = Functions.getSharedPreference(this@SplashActivity).edit()
        editor.clear()
        editor.apply()
        AccountUtils.setUpExistingAccountLogin(this@SplashActivity)

    }

    private fun apiCallHit() {
        splashViewModel.showSettings()
        splashViewModel.getAdsVideo()

        if(Functions.checkLogin(this)) {
            var checkData =
                Functions.getSettingsPreference(this).getString(Variables.selectedId, "")
            if (checkData.equals("")) {
                splashViewModel.getAddressList()
            }
        }

        setTimer()
    }

    fun setTimer() {
        if (countDownTimer != null) {
            countDownTimer?.cancel()
        }
        countDownTimer = object : CountDownTimer(time, 500) {
            override fun onTick(millisUntilFinished: Long) {
                // this will call on every 500 ms
            }

            override fun onFinish() {
                   moveNext()
            }
        }
        countDownTimer?.start()
    }

    fun moveNext() {

        if (Functions.getSettingsPreference(this@SplashActivity)
                .getBoolean(Variables.IsPrivacyPolicyAccept, false)
        ) {

            if (checkCurrentLocationUpdates()) {
                val intent = Intent(this@SplashActivity, MainMenuActivity::class.java)
                if (getIntent().extras != null && !intent.hasExtra("openMain")) {
                    try {
                        val userId = getIntent().getStringExtra("receiver_id")
                        AccountUtils.setUpSwitchOtherAccount(this@SplashActivity, userId)
                    } catch (e: Exception) {
                    }
                    intent.putExtras(getIntent().extras!!)
                    setIntent(null)
                }
                startActivity(intent)
                finish()
            }
            else {
                val intent = Intent(this@SplashActivity, LocationPermissionActivity::class.java)
                startActivity(intent)
                finish()
            }

        } else {
            openWebUrl(getString(R.string.terms_of_use), Constants.terms_conditions)
        }
    }

    fun openWebUrl(title: String?, url: String?) {
        val intent = Intent(this@SplashActivity, WebviewActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        intent.putExtra("from", "splash")
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onDestroy() {
        super.onDestroy()
        if (countDownTimer != null) {
            countDownTimer?.cancel()
        }
    }


    private fun checkCurrentLocationUpdates(): Boolean {
        val takePermissionUtils = PermissionUtils(this, null)

        val locationManager =
            this?.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        val GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!GpsStatus) {

            return false

        } else if (!takePermissionUtils!!.isLocationPermissionGranted) {

            return false

        } else {

            return true
        }
    }

}
