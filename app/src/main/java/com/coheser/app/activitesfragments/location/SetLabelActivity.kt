package com.coheser.app.activitesfragments.location

import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivitySetLabelBinding
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.AddressViewModel
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SetLabelActivity : AppCompatActivity() {
    lateinit var binding: ActivitySetLabelBinding
    var model: AddressPlacesModel? = null
    var addressModel: DeliveryAddress? = null
    var from = ""
    private val viewModel: AddressViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetLabelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.labelEdt.requestFocus()

        if (intent.hasExtra("model")) {
            model = intent.getParcelableExtra("model")

            binding.labelEdt.setText(model!!.label)
        }
        if (intent.hasExtra("modelAddress")) {
            addressModel = intent.getParcelableExtra("modelAddress")
            from = intent.getStringExtra("from")!!
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.saveAddress.setOnClickListener {

            if (TextUtils.isEmpty(binding.labelEdt.text.toString())) {
                binding.labelEdt.error = "Enter Label!"
            } else {
                if (from.equals("saved")) {
                    viewModel.saveDeliveryAddress(getRequiredParams())
                } else {
                    model!!.label = binding.labelEdt.text.toString()
                    val intent = Intent(this@SetLabelActivity, SaveAddressActivity::class.java)
                    intent.putExtra("type", "new")
                    intent.putExtra("modelNear", model)
                    try {
                        resultCallback.launch(intent)
                    } catch (e: Exception) {
                        startActivity(intent)
                    }
                }

            }
        }

        initObserver()
    }

    fun initObserver(){
        viewModel.addressLiveData.observe(this){ response ->
            when(response){
                is ApiResponce.Success -> {
                    Functions.cancelLoader()
                    val resultIntent = Intent()
                    resultIntent.putExtra("action", "LabelChanged")
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                is ApiResponce.Loading -> {
                    Functions.showLoader(this@SetLabelActivity,false,false)
                }
                is ApiResponce.Error -> {
                    Functions.cancelLoader()
                    Functions.showToast(this,response.message)
                }
                else ->{}
            }
        }
    }

    private fun getRequiredParams() : JSONObject{
           val params = JSONObject().apply {
                put(
                    "user_id",
                    Functions.getSharedPreference(this@SetLabelActivity)
                        .getString(Variables.U_ID, "")
                )
                put("default", "1")
                put("id", addressModel?.id)
                put("street_num", addressModel?.street_num)
                put("street", addressModel?.street)
                put("apartment", addressModel?.apartment)
                put("city", addressModel?.city)
                put("state", addressModel?.state)
                put("country", addressModel)
                put("zip", addressModel?.zip)
                put("lat", addressModel?.lat)
                put("long", addressModel?.lng)
                put("country_id", "")
                put("street_addr", addressModel?.street_num)
                put("location_string", addressModel?.location_string)
                put("building_name", addressModel?.building_name)
                put("entry_code", addressModel?.entry_code)
                put("dropoff_option", addressModel?.dropoff_option)
                put("instructions", addressModel?.instructions)

                if (TextUtils.isEmpty(binding.labelEdt.text.toString())) {
                    val title = addressModel?.location_string?.substringBefore(",")
                    put("label", title)
                } else {
                    put("label", binding.labelEdt.text.toString())
                }
            }

        return params
    }

    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            setResult(RESULT_OK)
            finish()
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


}