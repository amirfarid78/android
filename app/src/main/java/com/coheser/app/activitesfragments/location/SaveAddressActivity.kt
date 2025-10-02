package com.coheser.app.activitesfragments.location

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivitySaveAddressBinding
import com.coheser.app.simpleclasses.Dialogs
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.AddressViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.paperdb.Paper
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SaveAddressActivity : AppCompatActivity() , OnMapReadyCallback {

    lateinit var binding : ActivitySaveAddressBinding
    var model : DeliveryAddress? = null
    var nearModel : AddressPlacesModel? = null
    private lateinit var mMap: GoogleMap
    var lat = "0.0"
    var lng  = "0.0"
    var dropOff="0"
    var type = ""


    private val viewModel : AddressViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (intent.hasExtra("model")){

            model  = intent.getParcelableExtra("model")
            type = intent.getStringExtra("type")!!
            if (model != null){
                setData()
            }

        }
        if (intent.hasExtra("modelNear")){

            nearModel = intent.getParcelableExtra("modelNear")
            type = intent.getStringExtra("type")!!
            Log.d(Constants.tag," postalCode : ${nearModel!!.zipCode} streetNum: ${nearModel!!.streetNumber} street: ${nearModel!!.street} state:${nearModel!!.state} city name:${nearModel!!.cityName}")
            setData()

        }

        binding.saveAddress.setOnClickListener {
            viewModel.saveDeliveryAddress(getRequiredParams())
        }
        binding.adjustPin.setOnClickListener {
            val intent = Intent(this,MapActivity::class.java)
            intent.putExtra("lat",lat)
            intent.putExtra("lng",lng)
            intent.putExtra("type",type)
            try {
            resultCallback.launch(intent)
        }catch (e:Exception){
            startActivity(intent)
        }
        }
        binding.handoverBtn.setOnClickListener {
            dropOff = "0"
            binding.handoverBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.appColor)
            binding.handoverBtn.setTextColor(resources.getColor(R.color.whiteColor))
            binding.leaveDropBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.lightgraycolor)
            binding.leaveDropBtn.setTextColor(resources.getColor(R.color.blackColor))
        }
        binding.leaveDropBtn.setOnClickListener {
            dropOff = "1"
            binding.leaveDropBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.appColor)
            binding.leaveDropBtn.setTextColor(resources.getColor(R.color.whiteColor))
            binding.handoverBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.lightgraycolor)
            binding.handoverBtn.setTextColor(resources.getColor(R.color.blackColor))
        }

        binding.deleteBtn.setOnClickListener {
            Dialogs.showAlert(
               this, "Delete Address", "Are You Sure. You Want to delete ", "Yes", "No"
            ) { response ->
                if (response.equals(getString(R.string.yes), ignoreCase = true)) {
                    viewModel.deleteDeliveryAddress(model?.id!!)
                }
            }
        }
        binding.backBtn.setOnClickListener {
            finish()
        }

        initObserver()

    }
    private fun initObserver(){
        viewModel.addressLiveData.observe(this){ response ->
            when(response){
                is ApiResponce.Success -> {
                    Functions.cancelLoader()
                    response.data?.let { list ->
                        list.forEach { model ->
                            Paper.book().write(Variables.AdressModel,model)
                            Functions.getSettingsPreference(this@SaveAddressActivity).edit()
                                .putString(Variables.selectedId, model?.id).apply()
                            setResult(RESULT_OK)
                            finish()
                        }
                    }

                }
                is ApiResponce.Loading -> {
                    Functions.showLoader(this@SaveAddressActivity,false,false)
                }
                is ApiResponce.Error -> {
                    Functions.cancelLoader()
                    Functions.showToast(this,response.message)
                }
            }
        }
        viewModel.deleteAddressLiveData.observe(this){ response ->
            when(response){
                is ApiResponce.Success -> {
                    Functions.cancelLoader()
                    val resultIntent = Intent()
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                is ApiResponce.Loading ->{
                    Functions.showLoader(this,false,false)
                }
                is ApiResponce.Error -> {
                    Functions.cancelLoader()
                    Functions.showToast(this,response.message)
                }
            }
        }
    }

    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            data?.let {
                if (type.equals("new")){
                    val  model1 = it.getParcelableExtra<AddressPlacesModel>("data")
                    nearModel!!.lat = model1!!.lat
                    nearModel!!.lng = model1.lng
                    nearModel!!.street = model1.street!!
                    nearModel!!.streetNumber = model1.streetNumber!!
                    nearModel!!.state = model1.state!!
                    nearModel!!.cityName = model1.cityName!!
                    nearModel!!.zipCode = model1.zipCode!!
                    Log.d(Constants.tag,"location stfing : ${model1.address}")
                    binding.locTitle.text = model1.address
                    binding.locAddress.text = model1.address

                    lat = model1.lat.toString()
                    lng = model1.lng.toString()
                }else{
                    val  model1 = it.getParcelableExtra<DeliveryAddress>("data")
                    model!!.location_string = model1!!.location_string
                    model!!.lat = model1.lat
                    model!!.lng = model1.lng
                    model!!.street = model1.street
                    model!!.street_num = model1.street_num
                    model!!.state = model1.state
                    model!!.city = model1.city
                    model!!.zip = model1.zip
                    binding.locTitle.text = model!!.location_string.substringBefore(",")
                    binding.locAddress.text = model!!.location_string.substringAfter(",")

                    lat = model1.lat
                    lng = model1.lng
                }

            }
        }
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()

        Functions.printLog(Constants.tag,"onEnterAnimationComplete")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@SaveAddressActivity)

    }


    fun setData(){
        if (type.equals("new")){
            binding.locTitle.text = nearModel!!.title
            binding.locAddress.text = nearModel!!.address
            binding.toolTitle.text ="Save Address"
            binding.deleteBtn.visibility = View.GONE
            lat = nearModel!!.lat.toString()
            lng = nearModel!!.lng.toString()
            binding.labelEdt.setText(nearModel!!.label)
        }else{

            binding.toolTitle.text ="Edit"
            binding.deleteBtn.visibility = View.GONE

            binding.locTitle.text = model?.location_string?.substringBefore(",")
            binding.locAddress.text = model?.location_string?.substringAfter(",")

            dropOff = model?.dropoff_option!!
            if (dropOff == "0"){
                binding.handoverBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.appColor)
                binding.handoverBtn.setTextColor(resources.getColor(R.color.whiteColor))
                binding.leaveDropBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.lightgraycolor)
                binding.leaveDropBtn.setTextColor(resources.getColor(R.color.blackColor))
            }else{
                binding.leaveDropBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.appColor)
                binding.leaveDropBtn.setTextColor(resources.getColor(R.color.whiteColor))
                binding.handoverBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.lightgraycolor)
                binding.handoverBtn.setTextColor(resources.getColor(R.color.blackColor))
            }

            lat = model?.lat?:"0.0"
            lng = model?.lng?:"0.0"
            if (Functions.isStringHasValue(model?.apartment!!)){
                binding.apartmentEdt.setText(model?.apartment)
            }
            if (Functions.isStringHasValue(model?.entry_code!!)){
                binding.enrtyCodeEdt.setText(model?.entry_code)
            }
            if (Functions.isStringHasValue(model?.building_name!!)){
                binding.buildingEdt.setText(model?.building_name)
            }
            if (Functions.isStringHasValue(model?.instructions!!)){
                binding.dropOffNoteEdt.setText(model?.instructions)
            }
            if (Functions.isStringHasValue(model?.label!!)){
                binding.labelEdt.setText(model?.label)
            }
        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap!!

        val latitude = lat.toDouble()
        val longitude = lng.toDouble()
        var icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_png)

        if (icon != null) {
            val location = LatLng(latitude, longitude)
            mMap.addMarker(MarkerOptions().position(location).icon(icon))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
        } else {
            Log.e(Constants.tag, "Error: Marker icon bitmap is null.")
        }
    }
    private fun getRequiredParams() : JSONObject{
        var params = JSONObject()
        if (type == "update"){
            params = JSONObject().apply {
                put("user_id", Functions.getSharedPreference(this@SaveAddressActivity).getString(Variables.U_ID,""))
                put("default",model?.defaultValue)
                put("id", model?.id)
                put("street_num", model?.street_num)
                put("street", model?.street)
                put("apartment", binding.apartmentEdt.text.toString())
                put("city", model?.city)
                put("state", model?.state)
                put("country", model)
                put("zip",model?.zip)
                put("lat", model?.lat)
                put("long", model?.lng)
                put("country_id", "")
                put("street_addr", model?.street_num)
//                put("location_string", model?.location_string)
                put("building_name", binding.buildingEdt.text.toString())
                put("entry_code", binding.enrtyCodeEdt.text.toString())
                put("dropoff_option", dropOff)
                put("instructions", binding.dropOffNoteEdt.text.toString())

                if (TextUtils.isEmpty(binding.labelEdt.text.toString())) {
                    val title = model?.location_string?.substringBefore(",")
                    put("label", title)
                } else {
                    put("label", binding.labelEdt.text.toString())
                }
            }
        }else{
            params = JSONObject().apply {
                put("user_id", Functions.getSharedPreference(this@SaveAddressActivity).getString(Variables.U_ID,""))
                put("default", "1")
                put("street_num", nearModel!!.streetNumber)
                put("street", nearModel!!.street)
                put("apartment", binding.apartmentEdt.text.toString())
                put("city", nearModel!!.cityName)
                put("state", nearModel!!.state)
                put("country", nearModel!!.country)
                put("zip",nearModel!!.zipCode)
                put("lat", nearModel!!.lat)
                put("long", nearModel!!.lng)
                put("country_id", "")
                put("street_addr", nearModel!!.streetNumber)
                put("location_string", "${nearModel!!.title} , ${nearModel!!.address}")
                put("building_name", binding.buildingEdt.text.toString())
                put("entry_code", binding.enrtyCodeEdt.text.toString())
                put("dropoff_option", dropOff)
                put("instructions", binding.dropOffNoteEdt.text.toString())
                if (TextUtils.isEmpty(binding.labelEdt.text.toString())) {
                    put("label", nearModel!!.title)
                } else {
                    put("label", binding.labelEdt.text.toString())
                }
            }
        }
        return params
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