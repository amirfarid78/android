package com.coheser.app.activitesfragments.shoping.AddProducts

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.models.AddProductModel
import com.coheser.app.databinding.ActivityAddPriceBinding

class AddPriceA : AppCompatActivity() {
    lateinit var binding : ActivityAddPriceBinding
    var dataModel: AddProductModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPriceBinding.inflate(layoutInflater)
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

        if(!TextUtils.isEmpty(dataModel!!.price)){
            binding.rootD.priceEdt.setText(dataModel!!.price)
        }


        binding.saveBtn.setOnClickListener {
            if (TextUtils.isEmpty(binding.rootD.priceEdt.text.toString())){
                binding.rootD.priceEdt.error = getString(R.string.enter_price)
                binding.rootD.priceEdt.requestFocus()
            }else{
                dataModel!!.price=binding.rootD.priceEdt.text.toString()

                val data  = Intent()
                data.putExtra("dataModel",dataModel)
                setResult(Activity.RESULT_OK,data)
                finish()
            }
        }
    }
}