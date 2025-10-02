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
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.coheser.app.BuildConfig
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.location.adapter.AddressAdapter
import com.coheser.app.activitesfragments.location.adapter.LableAdapter
import com.coheser.app.activitesfragments.location.adapter.NearPlacesAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityAddAddressBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.PermissionUtils
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.AddressViewModel
import com.google.android.gms.maps.model.LatLng
import com.volley.plus.VPackages.VolleyRequest
import io.paperdb.Paper
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddAddressActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddAddressBinding
    var adapter: AddressAdapter? = null
    var addressList : MutableList<DeliveryAddress> = mutableListOf()

    var adapterNear: NearPlacesAdapter? = null
    var nearbyList : MutableList<AddressPlacesModel> = mutableListOf()

    var labelList : MutableList<DeliveryAddress> = mutableListOf()
    var labelAdapter: LableAdapter? = null


    var showCurrentLocation: Boolean = true

    private val viewModel : AddressViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_address)
        binding.lifecycleOwner = this


        binding.addLabelBtn.visibility = View.GONE
        if (intent.hasExtra("from")) {
            enablePermission()
        }

        if (intent.hasExtra("showCurrentLocation")) {
            showCurrentLocation = intent.getBooleanExtra("showCurrentLocation", true)
        }
        Functions.showLoader(this@AddAddressActivity, false, false)


        actionControl()
        setlableData()
        viewModel.showDeliveryAddress()
        initObserver()


    }
    fun initObserver(){
        viewModel.addressLiveData.observe(this){ response ->
            when(response){
                is ApiResponce.Success -> {
                    val addressModel = Functions.getAddressModel(this@AddAddressActivity)
                    addressList.clear()
                    Functions.cancelLoader()
                    response.data?.let { list ->
                        addressList.addAll(list)
                        list.forEach { model ->
                            if (model.label.lowercase() == "work"){
                                labelList = labelList.filter { it.label.lowercase() != "work" } as ArrayList<DeliveryAddress>
                            }
                            if (model.label.lowercase() == "home"){
                                labelList = labelList.filter { it.label.lowercase() != "home" } as ArrayList<DeliveryAddress>
                            }
                            labelList.add(model)
                            Log.d(Constants.tag,"label list size : ${labelList.size}")
                            if (addressModel != null && addressModel.id == model.id) {
                                Functions.saveAddressModel(
                                    model,
                                    this@AddAddressActivity
                                )
                            }
                        }
                    }
                    setupAdapter()
                }
                is ApiResponce.Loading ->{
                    Functions.showLoader(this@AddAddressActivity,false,false)
                }
                is ApiResponce.Error ->{
                    Functions.cancelLoader()
                    setupAdapter()
                }
                else -> {}
            }
        }
    }


    fun setlableData() {
        labelList = ArrayList()
        binding.addLabelBtn.visibility = View.VISIBLE

        for (i in 1..2) {
            val model = DeliveryAddress().apply {
                label = if (i == 1) "Home" else "Work"
                location_string = "Set address"
            }
            labelList.add(model)
        }

    }

    private fun actionControl() {

        if (!showCurrentLocation) {
            binding.currentLocLay.visibility = View.GONE
        } else {
            binding.currentLocLay.setOnClickListener {
                if (Functions.checkLoginUser(this)) {
                    enablePermission()
                }
            }
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (Functions.checkLoginUser(this@AddAddressActivity)) {

                    if (s.length > 0) {
                        binding.datalyout.visibility = View.GONE
                        binding.nearByLayout.visibility = View.VISIBLE
                    } else {
                        binding.datalyout.visibility = View.VISIBLE
                        binding.nearByLayout.visibility = View.GONE
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (Functions.checkLoginUser(this@AddAddressActivity)) {
                    searchPlaces()
                }
            }
        })

        binding.addLabelBtn.setOnClickListener {
            if (Functions.checkLoginUser(this@AddAddressActivity)) {

                val intent = Intent(this@AddAddressActivity, SearchAddressActivity::class.java)
                intent.putParcelableArrayListExtra("list", ArrayList(addressList))
                resultCallbackLabel.launch(intent)
            }

        }

        // nearby locations adapter
        binding.nearRecyclerView.setHasFixedSize(true)
        binding.nearRecyclerView.layoutManager = LinearLayoutManager(this)
        adapterNear = NearPlacesAdapter(this, nearbyList) { view, pos, `object` ->
            var model = `object` as AddressPlacesModel
            val nearbyModel = getGeoCodeing1(model)

            val intent = Intent(this, SaveAddressActivity::class.java)
            intent.putExtra("modelNear", nearbyModel)
            intent.putExtra("type", "new")
            try {
                resultCallback.launch(intent)
            } catch (e: Exception) {
                startActivity(intent)
            }

        }
        binding.nearRecyclerView.adapter = adapterNear


    }

    var handler: Handler? = null
    var runnable: Runnable? = null
    fun searchPlaces() {
        stopHandler()
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            if (binding.searchEdit.text.toString() == "") {
                enablePermission()
            } else {
                callApiSearchPlace(binding.searchEdit.text.toString())
            }
        }
        requireNotNull(handler).postDelayed(requireNotNull(runnable), 1000)
    }

    fun stopHandler() {
        if (handler != null && runnable != null) {
            handler!!.removeCallbacks(runnable!!)
        }
    }


    var takePermissionUtils: PermissionUtils? = null
    private fun enablePermission() {
        takePermissionUtils = PermissionUtils(this, mPermissionLocationResult)

        val locationManager =
            getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        val GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!GpsStatus) {
            GpsUtils(this).turnGPSOn(null)
        } else if (!takePermissionUtils!!.isLocationPermissionGranted) {
            takePermissionUtils!!.takeLocationPermission()
        } else {
            getCurrentLocation()
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
                enablePermission()
            }
        }
    )

    private fun getCurrentLocation() {

        val lat = Functions.getSettingsPreference(this).getString(Variables.DEVICE_LAT, "0.0")!!
            .toDouble()
        val lng = Functions.getSettingsPreference(this).getString(Variables.DEVICE_LNG, "0.0")!!
            .toDouble()

        val deliveryAddress = Functions.getGeoCodeing(this, LatLng(lat, lng))
        deliveryAddress?.let {
            Paper.book().write(Variables.AdressModel, it)
        }
        setResult(RESULT_OK)
        onBackPressed()


    }

    private fun getGeoCodeing1(nearbyPlace: AddressPlacesModel): AddressPlacesModel {

        val geocoder = Geocoder(this@AddAddressActivity, Locale.getDefault())
        try {
            val addressList = geocoder.getFromLocation(nearbyPlace.lat, nearbyPlace.lng, 1)
            if (addressList != null && addressList.size > 0) {

                val address = addressList[0]

                val streetNum = address.subThoroughfare ?: "N/A"
                val street = address.thoroughfare ?: "N/A"
                val state = address.adminArea ?: "N/A"
                val country = address.countryName ?: "N/A"
                val cityName = address.locality ?: "N/A"
                val zipCode = address.postalCode ?: "N/A"
                val countryCode = address.countryCode ?: "N/A"
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

    private fun setupAdapter() {
        val layoutManager = LinearLayoutManager(binding.root.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recyclerview.layoutManager = layoutManager
        adapter = AddressAdapter(
            this@AddAddressActivity, "address", addressList
        ) { view, pos, `object` ->
            val model = `object` as DeliveryAddress
            when (view?.id) {
                R.id.dataLay -> {
                    Functions.getSettingsPreference(this@AddAddressActivity).edit()
                        .putString(Variables.selectedId, model?.id).apply()
                    adapter!!.notifyDataSetChanged()
                    Paper.book().write(Variables.AdressModel, model)
                    setResult(RESULT_OK)
                    finish()
                }

                R.id.editBtn -> {
                    val intent = Intent(this@AddAddressActivity, SaveAddressActivity::class.java)
                    intent.putExtra("model", model)
                    intent.putExtra("type", "update")
                    try {
                        resultCallback.launch(intent)
                    } catch (e: Exception) {
                        startActivity(intent)
                    }
                }
            }
        }
        binding.recyclerview.adapter = adapter
        binding.recyclerview.itemAnimator = DefaultItemAnimator()

        labelAdapter =
            LableAdapter(this@AddAddressActivity, labelList!!, object : AdapterClickListener {
                override fun onItemClick(view: View?, pos: Int, `object`: Any?) {
                    if (Functions.checkLoginUser(this@AddAddressActivity)) {
                        var model = `object` as DeliveryAddress
                        if (model?.location_string?.lowercase().equals("set address")) {
                            val intent = Intent(this@AddAddressActivity, SearchAddressActivity::class.java)
                            intent.putExtra("label", model?.label)
                            intent.putExtra("from", "add")
                            intent.putExtra("list", ArrayList(addressList))
                            resultCallbackLabel.launch(intent)
                        } else {
                            Paper.book().write(Variables.AdressModel, model)
                            Functions.getSettingsPreference(this@AddAddressActivity).edit()
                                .putString(Variables.selectedId, model?.id).apply()
                            val resultIntent = Intent()
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    }

                }
            })
        binding.labelRecyclerView.adapter = labelAdapter
    }

    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            onBackPressed()
        }
    }

    var resultCallbackLabel = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val action = data?.getStringExtra("action")
            Functions.printLog(Constants.tag, "action Add Address:" + action)
            if (action != null && action.equals("LabelChanged", true)) {
                viewModel.showDeliveryAddress()
            } else {
                setResult(RESULT_OK)
                onBackPressed()
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

        val headers = HashMap<String, String>().apply {
            put("X-Goog-Api-Key", Functions.decodeKey(BuildConfig.encodedKey))
            put(
                "X-Goog-FieldMask",
                "places.id,places.displayName,places.formattedAddress,places.location"
            )
        }
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
                        val nearbyPlace = AddressPlacesModel().apply {
                            title = displayName.getString("text")
                            address = place.getString("formattedAddress")
                            placeId = place.getString("id")
                            latLng = LatLng(
                                location.getDouble("latitude"),
                                location.getDouble("longitude")
                            )
                            lat = location.getDouble("latitude")
                            lng = location.getDouble("longitude")
                        }
                        tempList.add(nearbyPlace)
                    }
                }
                nearbyList.clear()
                nearbyList.addAll(tempList)
            } catch (e: Exception) {
                Log.e(Constants.tag, e.toString())
            } finally {
                binding.pbar.visibility = View.GONE
                Log.d(Constants.tag, nearbyList.size.toString())
                adapterNear!!.notifyDataSetChanged()
            }
        }
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

    override fun onBackPressed() {
        var model = Paper.book().read<DeliveryAddress>(Variables.AdressModel)
        if (model != null) {
            super.onBackPressed()
        }
    }


}