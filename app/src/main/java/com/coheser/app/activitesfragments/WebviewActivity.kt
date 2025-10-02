package com.coheser.app.activitesfragments

import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.location.LocationPermissionActivity
import com.coheser.app.composeScreens.WebViewScreen
import com.coheser.app.mainmenu.MainMenuActivity
import com.coheser.app.models.StripeModel
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.PermissionUtils
import com.coheser.app.simpleclasses.Variables

class WebviewActivity : ComponentActivity(){

    private var url: String = "https://www.google.com"
    private var title: String = "Privacy"
    private var from: String? = null
    var purchase = "purchase"
    var isShowAcceptBtn = false
    var stripeModel : StripeModel ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        url = intent.getStringExtra("url") ?: "https://www.google.com"
        title = intent.getStringExtra("title") ?: "WebView"
        from = intent.getStringExtra("from")
        if (intent.hasExtra("modelStripe")){
            stripeModel = intent.getParcelableExtra("modelStripe")
            Log.d(Constants.tag,"success url : ${stripeModel!!.success_url}")
            Log.d(Constants.tag,"cancel url : ${stripeModel!!.cancel_url}")
        }
        if (from == "splash"){
            isShowAcceptBtn = true
        }

        setContent {
            WebViewScreen(
                url = url,
                title = title,
                showAcceptButton = isShowAcceptBtn,
                onBackPress = { finish() },
                onAcceptClick = {
                    acceptPrivacyPolicy()
                },
                onUrlChange = {
                    if (from.equals(purchase)){
                        Log.d(Constants.tag,"new url :$it")
                        if (stripeModel!!.success_url.equals(it)){
                            Log.d(Constants.tag,"sucess equal :)")
                            callBackToShop(true)

                        }else if (stripeModel!!.cancel_url.equals(it) || it.contains("fail")){
                            Log.d(Constants.tag,"cancel url :(")
                            callBackToShop(false)
                        }
                    }
                }
            )
        }
    }
    private fun callBackToShop(isSuccess : Boolean) {
        val intent = Intent()
        intent.putExtra("isSuccess", isSuccess)
        setResult(RESULT_OK, intent)
        finish()
    }

    @Preview
    @Composable
    fun previewScreen(){
        WebViewScreen(
            url = url,
            title = title,
            showAcceptButton = from == "splash",
            onBackPress = { finish() },
            onAcceptClick = {
                acceptPrivacyPolicy()
            },
            onUrlChange = {

            }
        )
    }

    private fun acceptPrivacyPolicy() {
        val sharedPrefs = Functions.getSettingsPreference(this).edit()
        sharedPrefs.putBoolean(Variables.IsPrivacyPolicyAccept, true).apply()
        val nextActivity = if (checkCurrentLocationUpdates()) {
            MainMenuActivity::class.java
        } else {
            LocationPermissionActivity::class.java
        }
        startActivity(Intent(this, nextActivity))
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        finish()
    }

    private fun checkCurrentLocationUpdates(): Boolean {
        val takePermissionUtils = PermissionUtils(this, null)
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                takePermissionUtils.isLocationPermissionGranted
    }

}
