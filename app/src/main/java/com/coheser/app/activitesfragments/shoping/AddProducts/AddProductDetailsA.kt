package com.coheser.app.activitesfragments.shoping.AddProducts

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.models.AddProductModel
import com.coheser.app.databinding.ActivityAddProductDetailsBinding

class AddProductDetailsA : AppCompatActivity() {
    lateinit var binding : ActivityAddProductDetailsBinding
    var dataModel: AddProductModel?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent!=null) {
            dataModel = intent.getParcelableExtra<AddProductModel>("dataModel")!!
        }

        binding!!.ivBack.setOnClickListener{
            finish()
        }


        initis()
    }
    fun initis(){

        if(!TextUtils.isEmpty(dataModel!!.title)){
            binding.rootD.listingTitle.setText(dataModel!!.title)
        }

        if(!TextUtils.isEmpty(dataModel!!.description)){
            binding.rootD.listDescription.setText(dataModel!!.description)
        }

        binding.saveBtn.setOnClickListener {
            if (TextUtils.isEmpty(binding.rootD.listingTitle.text.toString())){
                binding.rootD.listingTitle.error =getString(R.string.listing_title)
                binding.rootD.listingTitle.requestFocus()
            }else{
                val data = Intent()
                dataModel!!.title= binding.rootD.listingTitle.text.toString()
                dataModel!!.description= binding.rootD.listDescription.text.toString()
                data.putExtra("dataModel",dataModel)
                setResult(Activity.RESULT_OK,data)
                finish()
            }
        }
    }
}