package com.coheser.app.activitesfragments.shoping.AddProducts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.GalleryActivity
import com.coheser.app.activitesfragments.shoping.models.AddProductModel
import com.coheser.app.activitesfragments.shoping.models.GalleryModel
import com.coheser.app.databinding.ActivityListProductsBinding
import com.coheser.app.simpleclasses.Functions

class ListProducts : AppCompatActivity() , View.OnClickListener {
    var bining: ActivityListProductsBinding?=null

    var dataModel:AddProductModel= AddProductModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bining=DataBindingUtil.setContentView(this,R.layout.activity_list_products)
        setContentView(bining!!.root)

        bining!!.selectImagesLayout.setOnClickListener(this)
        bining!!.mobileLayout.setOnClickListener(this)
        bining!!.fashionLayout.setOnClickListener(this)
        bining!!.carsLayout.setOnClickListener(this)

        bining!!.ivBack.setOnClickListener{
            finish()
        }


    }

    override fun onClick(p0: View?) {

        if(p0!!.id==R.id.selectImagesLayout){
            selectImage()
        }
        else if(p0!!.id==R.id.mobileLayout){
            selectImage()
        }
        else if(p0!!.id==R.id.fashionLayout){
            selectImage()
        }
        else if(p0!!.id==R.id.carsLayout){
            selectImage()
        }

    }

    fun selectImage(){
        val intent=Intent(this, GalleryActivity::class.java)
        intent.putExtra("dataModel",dataModel)
        resultCallback.launch(intent)
    }



    var resultCallback = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data

            if (!data!!.hasExtra("data")) {
                Toast.makeText(this, "No data received", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            val seletedList= data!!.getSerializableExtra("data") as ArrayList<GalleryModel>

            val selectedImages = ArrayList<Uri?>()

            if (seletedList != null) {
                for (model in seletedList) {
                    selectedImages.add(Uri.parse(model.actualUri))
                    Functions.printLog(Constants.tag, "Uri: $model.actualUri")
                }
            }

            if(selectedImages.size<10){
                selectedImages.add(null)
            }

            dataModel.imagesList.clear()
            dataModel.imagesList.addAll(selectedImages)

            openSelectCategory()
        }
    }


    fun openSelectCategory() {
        val intent = Intent(this@ListProducts, ChooseCategory::class.java)
        intent.putExtra("dataModel",dataModel)
        resultCallbackChooseCategory.launch(intent)
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }


    var resultCallbackChooseCategory = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            dataModel= AddProductModel()
        }
    }


}