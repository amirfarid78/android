package com.coheser.app.activitesfragments.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.accounts.AccountUtils
import com.coheser.app.databinding.ActivityLocationPermissionBinding
import com.coheser.app.mainmenu.MainMenuActivity
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.PermissionUtils
import com.coheser.app.simpleclasses.Variables
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import io.paperdb.Paper

class LocationPermissionActivity : AppCompatActivity() {

    lateinit var binding: ActivityLocationPermissionBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_permission)

        binding.locationPermissionLayout.mainlayout.visibility = View.VISIBLE
        Paper.init(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        checkCurrentLocationUpdates()
        binding.locationPermissionLayout.locationPermissionBtn.setOnClickListener {
            checkCurrentLocationUpdates()
        }

    }


    var takePermissionUtils: PermissionUtils? = null
    private fun checkCurrentLocationUpdates() {
        takePermissionUtils = PermissionUtils(this, mPermissionLocationResult)

        val locationManager =
            this?.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        val GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!GpsStatus) {

            GpsUtils(this).turnGPSOn(null)

        }
        else if (!takePermissionUtils!!.isLocationPermissionGranted) {

            takePermissionUtils!!.takeLocationPermission()

        } else {
            getLocation()
        }
    }


    private fun getLocation() {

        Functions.cancelLoader()
        Functions.showLoader(this,false,false)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                location?.let {
                    Functions.cancelLoader()
                    val latitude = it.latitude
                    val longitude = it.longitude

                    Log.d(Constants.tag, "lat :$latitude  lng:  $longitude")

                    if (Paper.book().read<DeliveryAddress>(Variables.AdressModel) == null) {
                        val deliveryAddress = Functions.getGeoCodeing(this, LatLng(latitude, longitude))
                        deliveryAddress?.let {
                            Paper.book().write(Variables.AdressModel, it)
                            val editor = Functions.getSettingsPreference(binding.root.context).edit()
                            editor.putString(Variables.DEVICE_LAT, latitude.toString())
                            editor.putString(Variables.DEVICE_LNG, longitude.toString())
                            editor.putString(Variables.currentLocation,deliveryAddress!!.label)
                            editor.commit()
                        }
                    }
                    openMainActivity()

                } ?: run {
                    setlocationCallback()
                }
            }

    }

    override fun onStop() {
        super.onStop()
        removeCallBack()
    }

    private  var locationRequest: LocationRequest?=null
    private  var locationCallback: LocationCallback?=null
    fun setlocationCallback(){

        removeCallBack()

        locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    Functions.cancelLoader()
                    // Update UI with location data
                    val latitude = location.latitude
                    val longitude = location.longitude

                    if (Paper.book().read<DeliveryAddress>(Variables.AdressModel) == null) {
                        val deliveryAddress = Functions.getGeoCodeing(this@LocationPermissionActivity, LatLng(latitude, longitude))
                        deliveryAddress?.let {
                            Paper.book().write(Variables.AdressModel, it)
                        }
                    }

                    val editor = Functions.getSettingsPreference(binding.root.context).edit()
                    editor.putString(Variables.DEVICE_LAT, latitude.toString())
                    editor.putString(Variables.DEVICE_LNG, longitude.toString())
                    editor.commit()


                    openMainActivity()
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest!!, locationCallback!!, null)

    }


    fun removeCallBack(){
        if(locationCallback!=null) {
            fusedLocationClient.removeLocationUpdates(locationCallback!!)
        }
    }

    private val mPermissionLocationResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
         { result: Map<String, Boolean> ->
            var allPermissionClear = true
            val blockPermissionCheck: MutableList<String> = ArrayList()
            for (key in result.keys) {
                if (!result[key]!!) {
                    allPermissionClear = false
                    blockPermissionCheck.add(
                        Functions.getPermissionStatus(
                            this, key
                        )
                    )
                }
            }
            if (blockPermissionCheck.contains("blocked")) {
                Functions.showPermissionSetting(
                    binding.root.context,
                    getString(R.string.we_need_location_permission_to_show_you_nearby_contents)
                )
            } else if (allPermissionClear) {
                getLocation()
            }
        }
    )

    fun openMainActivity() {
        val intent = Intent(this@LocationPermissionActivity, MainMenuActivity::class.java)
        if (getIntent().extras != null) {
            try {
                // its for multiple account notification handling
                val userId = getIntent().getStringExtra("receiver_id")
                AccountUtils.setUpSwitchOtherAccount(this@LocationPermissionActivity, userId)
            } catch (e: Exception) {
            }
            intent.putExtras(getIntent().extras!!)
            setIntent(null)
        }
        startActivity(intent)
        finish()
    }

}

