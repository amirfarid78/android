package com.coheser.app.activitesfragments.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.coheser.app.Constants
import com.coheser.app.databinding.ActivityMapBinding
import com.coheser.app.simpleclasses.Functions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.util.Locale


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var binding : ActivityMapBinding
    private lateinit var mMap: GoogleMap
    var lat = "0.0"
    var lng = "0.0"
    var type = ""
    var geocoder : Geocoder? = null
    var model = DeliveryAddress()
    var nearModel = AddressPlacesModel()
    private lateinit var onCameraIdleListener: GoogleMap.OnCameraIdleListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.map.onCreate(savedInstanceState)

        geocoder = Geocoder(this)

        if (intent.hasExtra("lat") && intent.hasExtra("lng")){
            lat = intent.getStringExtra("lat")!!
            lng = intent.getStringExtra("lng")!!
            type = intent.getStringExtra("type")!!
            configureCameraIdle()
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.saveAddress.setOnClickListener {
                try {
                    val geocoder: Geocoder
                    geocoder = Geocoder(this, Locale.getDefault())
                    val addresses: MutableList<Address> = geocoder.getFromLocation(lat.toDouble(), lng.toDouble(), 1)!!
                    if (addresses != null && addresses.size > 0) {

                        val address = addresses[0]

                        val streetNum = address.subThoroughfare ?: "N/A"
                        val street = address.thoroughfare ?: "N/A"
                        val state = address.adminArea ?: "N/A"
                        val country = address.countryName ?: "N/A"
                        val cityName = address.locality ?: "N/A"
                        val zipCode = address.postalCode ?: "N/A"
                        val countryCode = address.countryCode ?: "N/A"
                        val title = address.featureName ?: "N/A"


                        val locString = address.getAddressLine(0) ?: "N/A"

                        Log.d(Constants.tag," title: $title ")
                        if (type.equals("new")){
                            nearModel.lat = lat.toDouble()
                            nearModel.lng = lng.toDouble()
                            nearModel.street = street
                            nearModel.streetNumber = streetNum
                            nearModel.state = state
                            nearModel.cityName = cityName
                            nearModel.zipCode = zipCode
                            nearModel.address = locString

                            val intent = Intent()
                            intent.putExtra("data",nearModel)
                            setResult(RESULT_OK,intent)
                            finish()
                        }else if(type.equals("update")){
                            model.street = street
                            model.street_num = streetNum
                            model.state = state
                            model.city = cityName
                            model.zip = zipCode
                            model.location_string = locString
                            model.lat = lat
                            model.lng = lng

                            val intent = Intent()
                            intent.putExtra("data",model)
                            setResult(RESULT_OK,intent)
                            finish()
                        }else{
                            val intent = Intent()
                            intent.putExtra("lat",lat)
                            intent.putExtra("lng",lng)
                            setResult(RESULT_OK,intent)
                            finish()
                        }

                    }
                }catch (e: IOException) {
                    e.printStackTrace()
                }


        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap!!

        val latitude = lat.toDouble()
        val longitude = lng.toDouble()
        val location = LatLng(latitude, longitude)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.setOnCameraIdleListener(onCameraIdleListener)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))

        configureCameraIdle()


    }


    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()

        Functions.printLog(Constants.tag,"onEnterAnimationComplete")

        binding.map.getMapAsync(this)

    }
    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.map.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.map.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.map.onSaveInstanceState(outState)
    }

    private fun configureCameraIdle() {
        onCameraIdleListener = GoogleMap.OnCameraIdleListener {
            val latLng: LatLng = mMap.cameraPosition.target
            lat = latLng.latitude.toString()
            lng = latLng.longitude.toString()

            Log.d(Constants.tag, "lat :$lat  lng:$lng")
        }
    }

}