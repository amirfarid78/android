package com.coheser.app.activitesfragments.shoping.AddProducts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.CompoundButton
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.coheser.app.R
import com.coheser.app.activitesfragments.location.AddAddressActivity
import com.coheser.app.activitesfragments.location.DeliveryAddress
import com.coheser.app.activitesfragments.shoping.models.AddProductModel
import com.coheser.app.databinding.ActivityDealMethodBinding
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import io.paperdb.Paper

class DealMethodA : AppCompatActivity() {
    lateinit var binding : ActivityDealMethodBinding
    var dataModel: AddProductModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDealMethodBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(intent!=null) {
            dataModel = intent.getParcelableExtra<AddProductModel>("dataModel")!!
        }

        binding!!.ivBack.setOnClickListener{
            finish()
        }

        inits()
    }

    fun inits(){

        if(!TextUtils.isEmpty(dataModel!!.locationString)){
            binding.rootD.locationTxt.text=dataModel!!.locationString
        }

        if("pickup".equals(dataModel!!.dealMethod)){
            binding.rootD.arrangeMyself.isChecked=true
            binding.rootD.meetUp.isChecked=false
        }

        else if("meetup".equals(dataModel!!.dealMethod)){
            binding.rootD.meetUp.isChecked=true
            binding.rootD.arrangeMyself.isChecked=false
        }


        binding.saveBtn.setOnClickListener {

            if (binding.rootD.arrangeMyself.isChecked){
                dataModel!!.dealMethod="1"
                val data  = Intent()
                data.putExtra("dataModel",dataModel)
                setResult(Activity.RESULT_OK,data)
                finish()
            }
            else if(binding.rootD.meetUp.isChecked){
                if(TextUtils.isEmpty(dataModel!!.locationString)){
                    Functions.showToast(this, getString(R.string.please_pick_the_location))
                }
                else {
                    dataModel!!.dealMethod = "2"
                    val data = Intent()
                    data.putExtra("dataModel", dataModel)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
            }
            else{
                Functions.showToast(this,getString(R.string.please_select_the_deal_method))
            }

        }

        binding.rootD.selectLocationLayout.setOnClickListener{
            openMapActivity()
        }

        binding.rootD.arrangeMyself.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                if(p1){
                    binding.rootD.meetUp.isChecked=false
                }
            }
        })

        binding.rootD.meetUp.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                if(p1){
                    binding.rootD.arrangeMyself.isChecked=false
                }
            }
        })

    }


    fun openMapActivity() {

        val intent = Intent(this, AddAddressActivity::class.java)
        intent.putExtra("showCurrentLocation", true)
        try {
            resultCallback.launch(intent)
        } catch (e: Exception) {
            startActivity(intent)
        }

    }


    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {

            val deliveryAddress =  Paper.book().read<DeliveryAddress>(Variables.AdressModel)

            dataModel!!.locationString = deliveryAddress!!.location_string
            dataModel!!.lat = deliveryAddress!!.lat
            dataModel!!.lng = deliveryAddress!!.lng

            if(!TextUtils.isEmpty(dataModel!!.locationString)){
                binding.rootD.locationTxt.text=dataModel!!.locationString
            }

        }
    }

}