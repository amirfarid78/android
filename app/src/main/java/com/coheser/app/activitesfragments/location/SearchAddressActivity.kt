package com.coheser.app.activitesfragments.location

import android.content.Intent
import android.graphics.Rect
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.coheser.app.BuildConfig
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.location.adapter.AddressAdapter
import com.coheser.app.activitesfragments.location.adapter.NearPlacesAdapter
import com.coheser.app.databinding.ActivitySearchAddressBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.LocationTracker
import com.coheser.app.simpleclasses.PermissionUtils
import com.coheser.app.simpleclasses.Variables
import com.google.android.gms.maps.model.LatLng
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

class SearchAddressActivity : AppCompatActivity() {
    lateinit var binding: ActivitySearchAddressBinding
    var adapter: NearPlacesAdapter? = null
    var nearbyList: MutableList<AddressPlacesModel> = mutableListOf()
    var addressList: MutableList<DeliveryAddress> = mutableListOf()
    var adapterAddress: AddressAdapter? = null
    var model: DeliveryAddress? = null
    var labelString = ""
    var from = "add"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length > 0) {
                    binding.saveAddressLay.visibility = View.GONE
                    binding.recycerViewLayout.visibility = View.VISIBLE
                } else {
                    binding.saveAddressLay.visibility = View.VISIBLE
                    binding.recycerViewLayout.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable) {
                searchPlaces()
            }
        })



        from = intent.getStringExtra("from").toString()
        if (intent.hasExtra("list")) {

            addressList = intent.getParcelableArrayListExtra<DeliveryAddress>("list") as ArrayList<DeliveryAddress>
            if (intent.hasExtra("label")) {
                labelString = intent.getStringExtra("label")!!
            }

            if (addressList.isNotEmpty()) {
                binding.saveAddressLay.visibility = View.VISIBLE
                binding.recycerViewLayout.visibility = View.GONE

            } else {
                binding.saveAddressLay.visibility = View.GONE
                binding.recycerViewLayout.visibility = View.VISIBLE

            }

        }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NearPlacesAdapter(
            this@SearchAddressActivity,
            nearbyList,
            object : AdapterClickListener {
                override fun onItemClick(view: View?, pos: Int, `object`: Any?) {
                    var model = `object` as AddressPlacesModel
                    val nearbyModel = getGeoCodeing1(model)

                    if (from.equals("select")) {

                        val result = Intent()
                        result.putExtra("data", nearbyModel)
                        setResult(RESULT_OK, result)
                        finish()

                    } else {

                        if (!labelString.equals("")) {
                            model.label = labelString
                        }
                        var intent =
                            Intent(this@SearchAddressActivity, SetLabelActivity::class.java)
                        intent.putExtra("model", nearbyModel)
                        try {
                            resultCallback.launch(intent)
                        } catch (e: Exception) {
                            startActivity(intent)
                        }

                    }

                }

            })
        binding.recyclerView.adapter = adapter

        binding.backBtn.setOnClickListener {
            finish()
        }

        val layoutManager = LinearLayoutManager(binding.root.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recyclerviewSave.layoutManager = layoutManager
        adapterAddress = AddressAdapter(
            this@SearchAddressActivity,
            "search",
            addressList,
            object : AdapterClickListener {
                override fun onItemClick(view: View?, pos: Int, `object`: Any?) {
                    model = `object` as DeliveryAddress

                    val intent = Intent(this@SearchAddressActivity, SetLabelActivity::class.java)
                    intent.putExtra("modelAddress", model)
                    intent.putExtra("from", "saved")
                    try {
                        resultCallback.launch(intent)
                    } catch (e: Exception) {
                        startActivity(intent)
                    }
                }

            })
        binding.recyclerviewSave.adapter = adapterAddress
    }

    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val action = data?.getStringExtra("action")
            Functions.printLog(Constants.tag, "action:" + action)
            val intent = Intent()
            if (action != null) {
                intent.putExtras(data?.extras!!)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    var handler: Handler? = null
    var runnable: Runnable? = null
    fun searchPlaces() {
        stopHandler()
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            if (binding.searchEdit.text.toString() == "") {
                enablePermission(false)
            } else {
                callApiSearchPlace(binding.searchEdit.text.toString())
            }
        }
        requireNotNull(handler).postDelayed(requireNotNull(runnable), 1000)
    }

    override fun onResume() {
        super.onResume()
        enablePermission(false)
    }

    fun stopHandler() {
        if (handler != null && runnable != null) {
            handler!!.removeCallbacks(runnable!!)
        }
    }

    var takePermissionUtils: PermissionUtils? = null
    private fun enablePermission(selectCurrent: Boolean) {
        takePermissionUtils = PermissionUtils(this, mPermissionLocationResult)

        val locationManager =
            this?.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        val GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!GpsStatus) {
            GpsUtils(this).turnGPSOn(null)
        } else if (!takePermissionUtils!!.isLocationPermissionGranted) {
            takePermissionUtils!!.takeLocationPermission()
        } else {

            getCurrentLocation(selectCurrent)
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
                        Functions.getPermissionStatus(this, key)
                    )
                }
            }
            if (blockPermissionCheck.contains("blocked")) {
                Functions.showPermissionSetting(
                    binding.root.context,
                    getString(R.string.we_need_location_permission_to_show_you_nearby_contents)
                )
            } else if (allPermissionClear) {
                enablePermission(false)
            }
        }
    )

    private fun getCurrentLocation(selectCurrent: Boolean) {

        val locationTracker = LocationTracker(this)
        if (locationTracker.isGooglePlayServicesAvailable && locationTracker.isGPSEnabled) {

            Log.d(
                Constants.tag,
                locationTracker.latitude.toString() + " , " + locationTracker.longitude
            )
            if (selectCurrent) {
                locationTracker.stopUsingGPS()
            } else {
                if (nearbyList.isEmpty()) {
                    binding.pbar.visibility = View.VISIBLE
                } else {
                    binding.pbar.visibility = View.GONE
                }

                Functions.getSettingsPreference(this@SearchAddressActivity).edit()
                    .putString(Variables.DEVICE_LAT, "" + locationTracker.latitude).commit()
                Functions.getSettingsPreference(this@SearchAddressActivity).edit()
                    .putString(Variables.DEVICE_LNG, "" + locationTracker.longitude).commit()

                locationTracker.stopUsingGPS()
            }
        }

    }

    fun callApiSearchPlace(search: String?) {
        val parameters = JSONObject()
        try {
            parameters.put("textQuery", search)
            parameters.put("pageSize", 15)
            val locationBias = JSONObject()
            val circle = JSONObject()
            circle.put("radius", 10000)
            val center = JSONObject()
            center.put(
                "latitude",
                Functions.getSettingsPreference(this).getString(Variables.DEVICE_LAT, "0.0")!!
                    .toDouble()
            )
            center.put(
                "longitude",
                Functions.getSettingsPreference(this).getString(Variables.DEVICE_LNG, "0.0")!!
                    .toDouble()
            )
            circle.put("center", center)
            locationBias.put("circle", circle)
            parameters.put("locationBias", locationBias)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val headers = HashMap<String, String>()
        headers["X-Goog-Api-Key"] = Functions.decodeKey(BuildConfig.encodedKey)
        headers["X-Goog-FieldMask"] =
            "places.id,places.displayName,places.formattedAddress,places.location,places.addressComponents"
        VolleyRequest.JsonPostRequest(
            this, "https://places.googleapis.com/v1/places:searchText", parameters, headers
        ) { resp ->
            try {
                val jsonObject = JSONObject(resp)
                val results = jsonObject.optJSONArray("places")
                val tempList = ArrayList<AddressPlacesModel>()

                if (results != null) {
                    for (i in 0 until results.length()) {
                        val place = results.getJSONObject(i)
                        val location = place.optJSONObject("location")
                        val displayName = place.optJSONObject("displayName")
                        val nearbyPlace = AddressPlacesModel()
                        nearbyPlace.title = displayName.getString("text")
                        nearbyPlace.address = place.getString("formattedAddress")
                        nearbyPlace.placeId = place.getString("id")
                        val lat = location.getDouble("latitude")
                        val lng = location.getDouble("longitude")

                        nearbyPlace.latLng = LatLng(lat, lng)
                        nearbyPlace.lat = lat
                        nearbyPlace.lng = lng

                        tempList.add(nearbyPlace)
                    }
                }
                nearbyList.clear()
                nearbyList.addAll(tempList)
            } catch (e: Exception) {
                Functions.printLog(Constants.tag, e.toString())
            } finally {
                binding.pbar.visibility = View.GONE
                Log.d(Constants.tag, nearbyList.size.toString())
                adapter!!.notifyDataSetChanged()
            }
        }
    }


    private fun getGeoCodeing1(nearbyPlace: AddressPlacesModel): AddressPlacesModel {

        val geocoder = Geocoder(this@SearchAddressActivity, Locale.getDefault())
        try {
            val addressList = geocoder.getFromLocation(nearbyPlace.lat, nearbyPlace.lng, 1)
            if (addressList != null && addressList.size > 0) {

                val address = addressList[0]

                val streetNum = address.subThoroughfare ?: ""
                val street = address.thoroughfare ?: ""
                val state = address.adminArea ?: ""
                val country = address.countryName ?: ""
                val cityName = address.locality ?: ""
                val zipCode = address.postalCode ?: ""
                val countryCode = address.countryCode ?: ""
                Log.d(Constants.tag, " code : $countryCode streetNum: $streetNum street: $street")


                nearbyPlace.street = street
                nearbyPlace.streetNumber = streetNum
                nearbyPlace.state = state
                nearbyPlace.country = country
                nearbyPlace.cityName = cityName
                nearbyPlace.zipCode = zipCode

            }
        } catch (e: IOException) {
            Log.d(Constants.tag, "Exception GetGeoCoding: $e")
        }
        return nearbyPlace
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    hideKeyboard(v)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}