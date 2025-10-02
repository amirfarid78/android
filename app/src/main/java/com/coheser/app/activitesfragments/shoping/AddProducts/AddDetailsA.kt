package com.coheser.app.activitesfragments.shoping.AddProducts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.GalleryActivity
import com.coheser.app.activitesfragments.shoping.Utils.ItemMoveCallback
import com.coheser.app.activitesfragments.shoping.Utils.PhotoViewHolder
import com.coheser.app.activitesfragments.shoping.adapter.SelectedImagesAdapter
import com.coheser.app.activitesfragments.shoping.models.AddProductModel
import com.coheser.app.activitesfragments.shoping.models.CategoryModel
import com.coheser.app.activitesfragments.shoping.models.GalleryModel
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.activitesfragments.shoping.services.ProductImagesService
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.ActivityAddDetailsBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.mainmenu.MainMenuActivity
import com.coheser.app.simpleclasses.DataParsing
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import java.util.Collections

class AddDetailsA : AppCompatActivity(), View.OnClickListener, ItemMoveCallback.ItemTouchHelperContract{
    var binding:ActivityAddDetailsBinding?=null
    var dataModel: AddProductModel?=null
    var from  = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_add_details)
        setContentView(binding!!.root)

        if(intent.hasExtra("dataModel")) {
            dataModel = intent.getParcelableExtra<AddProductModel>("dataModel")!!
            setUpData()
            setSelectedImagesAdapter()
        }else if (intent.hasExtra("from")){
            val productModel = intent.getParcelableExtra<ProductModel>("productModel")
            from = intent.getStringExtra("from")!!
            dataModel = AddProductModel(
                id = productModel?.product?.id ?: "",
                condition = productModel?.product?.condition ?: "",
                title = productModel?.product?.title ?: "",
                description = productModel?.product?.description ?: "",
                dealMethod = productModel?.product?.delivery_method ?: "",
                locationString = productModel?.product?.meetup_location_string ?: "",
                lat = productModel?.product?.meetup_location_lat ?: "",
                lng = productModel?.product?.meetup_location_long ?: "",
                price = productModel?.product?.price ?: "",
                imagesList = ArrayList<Uri?>(),
                categoryModel = productModel?.category
            )
            Log.d(Constants.tag,"category :${productModel!!.category.title}")

            setUpData()
        }



        binding!!.selectCategoryLayout.setOnClickListener(this)
        binding!!.conditionLayout.setOnClickListener(this)
        binding!!.detailLayout.setOnClickListener(this)
        binding!!.dealMethodLayout.setOnClickListener(this)
        binding!!.priceLayout.setOnClickListener(this)
        binding!!.addBtn.setOnClickListener(this)
        binding!!.ivBack.setOnClickListener(this)

    }



    var productImagesAdapter:SelectedImagesAdapter?=null
    fun setSelectedImagesAdapter() {


        productImagesAdapter = SelectedImagesAdapter(
            this, dataModel!!.imagesList
        ) { view, pos, `object` ->
            when (view.id) {
                R.id.mainLayout -> {
                    if(`object`==null){
                        selectImage()
                    }
                }
                R.id.deleteImageBtn -> {
                    dataModel!!.imagesList.removeAt(pos)
                    productImagesAdapter!!.notifyDataSetChanged()
                }
            }
        }
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding!!.imagesRecylerView.setLayoutManager(linearLayoutManager)
        binding!!.imagesRecylerView.setAdapter(productImagesAdapter)

        val callback: ItemTouchHelper.Callback = ItemMoveCallback(this)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding!!.imagesRecylerView)
    }

    fun selectImage(){
        val intent=Intent(this, GalleryActivity::class.java)
        intent.putExtra("dataModel",dataModel)
        resultCallback.launch(intent)
    }



    var resultCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data

            val seletedList=data!!.getSerializableExtra("data") as ArrayList<GalleryModel>

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

            dataModel!!.imagesList.clear()
            dataModel!!.imagesList.addAll(selectedImages)
            productImagesAdapter!!.notifyDataSetChanged()

        }
    }



    override fun onClick(v: View?) {


        if(v!!.id==R.id.conditionLayout){
            val intent=Intent(this@AddDetailsA,AddProductConditionA::class.java)
            intent.putExtra("dataModel",dataModel)
            luncher.launch(intent)
        }

        else if(v!!.id==R.id.detailLayout){
            val intent=Intent(this@AddDetailsA,AddProductDetailsA::class.java)
            intent.putExtra("dataModel",dataModel)
            luncher.launch(intent)

        }

        else if(v!!.id==R.id.dealMethodLayout){

            val intent=Intent(this@AddDetailsA,DealMethodA::class.java)
            intent.putExtra("dataModel",dataModel)
            luncher.launch(intent)

        }

        else if(v!!.id==R.id.priceLayout){
            val intent=Intent(this@AddDetailsA,AddPriceA::class.java)
            intent.putExtra("dataModel",dataModel)
            luncher.launch(intent)
        }

        else if(v!!.id==R.id.selectCategoryLayout){
            getAllCategory()
        }


        else if(v!!.id==R.id.addBtn){
            if(checkValidation()){
                callApiAddProduct()
            }
        }
        else if(v!!.id==R.id.iv_back){
            finish()
        }



    }


    var luncher = registerForActivityResult<Intent, ActivityResult>(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            data.let {
                dataModel = it?.getParcelableExtra<AddProductModel>("dataModel")

                setUpData()
            }
        }
    }


    fun setUpData(){
        if (from == "edit"){
            binding!!.imagesLay.visibility = View.GONE
            binding!!.addBtnText.text = "Edit Product"
        }

        binding!!.categoryTxt.setText(dataModel!!.categoryModel!!.title)

        if(!TextUtils.isEmpty(dataModel!!.condition)) {
            binding!!.conditionTxt.text = dataModel!!.condition
        }

        if(!TextUtils.isEmpty(dataModel!!.title)) {
            binding!!.detailTxt.text = dataModel!!.title
        }

        if(!TextUtils.isEmpty(dataModel!!.dealMethod)) {
            var dealMethod = ""
            if (dataModel!!.dealMethod.equals("1")){
                dealMethod = "pickUp"

            }else if (dataModel!!.dealMethod.equals("2")){
                dealMethod = "meetup"
            }
            binding!!.dealMethodTxt.text = dealMethod
        }

        if(!TextUtils.isEmpty(dataModel!!.price)) {
            binding!!.PriceTxt.text = "${Constants.CURRENCY} ${dataModel!!.price}"
        }

    }




    fun getAllCategory() {

        val parameters = JSONObject()
        try {
            parameters.put("parent_id", "0")

        } catch (e: Exception) {
            e.printStackTrace()
        }
        Functions.showLoader(this, false, false)
        VolleyRequest.JsonPostRequest(
            this,
            ApiLinks.showProductCategories,
            parameters,
            Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this, resp)
            Functions.cancelLoader()

            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if (code == "200") {
                    val msgArray = response.getJSONArray("msg")

                    val temp_list = java.util.ArrayList<CategoryModel>()
                    for (i in 0 until msgArray.length()) {
                        val itemdata = msgArray.optJSONObject(i)
                        val Category = itemdata.optJSONObject("Category")

                        var model= DataParsing.getCategoryDataModel(Category)

                        val Children = itemdata.optJSONArray("Children")
                        val childList = java.util.ArrayList<CategoryModel>()
                        if (Children!=null && Children.length()>0) {
                            for (j in 0 until Children.length()) {
                                val jsonObject = Children.getJSONObject(j)
                                var model= DataParsing.getCategoryDataModel(jsonObject)
                                childList.add(model)
                            }
                        }

                        model.list=childList
                        temp_list.add(model)

                    }

                  openSubCat(temp_list, getString(R.string.select_category))
                }

            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception: comment$e")
            }
        }
    }


    private fun openSubCat(dataList: ArrayList<CategoryModel>,title:String) {
        val fragment = SubCategoryF.newInstance(dataList, title,object: FragmentCallBack {
            override fun onResponce(bundle: Bundle?) {
                val categoryModel=bundle!!.getParcelable<CategoryModel>("data")
                if(categoryModel!!.list!=null && categoryModel!!.list.size>0){
                    openSubCat(categoryModel.list,categoryModel?.title!!.toString())
                }
                else {
                    dataModel!!.categoryModel = bundle!!.getParcelable<CategoryModel>("data")
                    setUpData()
                }

            }
        })
        fragment.show(getSupportFragmentManager(), "SubCategoryF")
    }



    fun checkValidation():Boolean{
        if(TextUtils.isEmpty(dataModel!!.condition)) {
            Functions.showToast(this, getString(R.string.please_select_the_condition_of_product))
            return false
        }

        if(TextUtils.isEmpty(dataModel!!.title)) {
            Functions.showToast(this, getString(R.string.please_enter_product_details))
            return false
        }

        if(TextUtils.isEmpty(dataModel!!.dealMethod)) {
            Functions.showToast(this, getString(R.string.please_select_where_you_deal_this_product))
            return false
        }

        if(TextUtils.isEmpty(dataModel!!.price)) {
            Functions.showToast(this, getString(R.string.please_enter_product_price))
            return false
        }
        if(TextUtils.isEmpty(dataModel!!.categoryModel!!.title)) {
            Functions.showToast(this, getString(R.string.please_select_category))
            return false
        }

        return true
    }

    fun callApiAddProduct() {

        val parameters = JSONObject()
        try {
            if (from == "edit"){
                parameters.put("id", dataModel!!.id)
            }

            parameters.put("user_id", Functions.getSharedPreference(this).getString(Variables.U_ID, ""))
            parameters.put("category_id", dataModel!!.categoryModel!!.id)
            parameters.put("title", dataModel!!.title)
            parameters.put("description", dataModel!!.description)
            parameters.put("price", dataModel!!.price)
//            parameters.put("condition", Functions.replaceSpecialCharactersWithUnderscore(dataModel!!.condition))
            parameters.put("condition", dataModel!!.condition)
            parameters.put("delivery_method", dataModel!!.dealMethod)
            if(dataModel!!.dealMethod.equals("meetup")){
                parameters.put("meetup_location_string",dataModel!!.locationString)
                parameters.put("meetup_location_lat", dataModel!!.lat)
                parameters.put("meetup_location_long", dataModel!!.lng)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

         Functions.showLoader(this, false, false)
        VolleyRequest.JsonPostRequest(
            this,
            ApiLinks.addProduct,
            parameters,
            Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this, resp)
            Functions.cancelLoader()
            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if (code == "200") {
                    val msg = response.optJSONObject("msg")
                    val Product=msg.optJSONObject("Product");
                    dataModel!!.id=Product.optString("id")
                    if(dataModel!!.imagesList.size>0){
                        startService()
                    }

                    callbackToShop()

                }

            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception: comment$e")
                Functions.cancelLoader()
            }
        }
    }

    fun callbackToShop(){
        val intent = Intent()
        intent.putExtra("isUpdate", true)
        setResult(RESULT_OK, intent)
        finish()
    }

    fun startService() {
        val mService = ProductImagesService()
        if (!Functions.isMyServiceRunning(this, mService.javaClass)) {

            val mServiceIntent = Intent(this.applicationContext, mService.javaClass)
            mServiceIntent.setAction("startservice")
            mServiceIntent.putExtra("dataModel", dataModel)
            startService(mServiceIntent)


            this@AddDetailsA.runOnUiThread(Runnable {
                val intent = Intent(this@AddDetailsA, MainMenuActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
               })
        }
        else {
            Toast.makeText(
                this@AddDetailsA,
                getString(R.string.please_wait_product_uploading_is_already_in_progress),
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        Functions.printLog(Constants.tag,"fromPosition: $fromPosition ToPosition: $toPosition")
        if (dataModel!!.imagesList.get(fromPosition)==null) {
        } else {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(dataModel!!.imagesList, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(dataModel!!.imagesList, i, i - 1)
                }
            }
            productImagesAdapter!!.notifyItemMoved(fromPosition, toPosition)
        }
    }

    override fun onRowSelected(myViewHolder: PhotoViewHolder?) {
        Functions.printLog(Constants.tag,"onRowSelected")
    }

    override fun onRowClear(myViewHolder: PhotoViewHolder?) {
        Functions.printLog(Constants.tag,"onRowClear")
    }



}