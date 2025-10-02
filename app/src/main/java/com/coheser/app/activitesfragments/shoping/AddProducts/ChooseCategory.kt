package com.coheser.app.activitesfragments.shoping.AddProducts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.AddProducts.tabs.AddAllDetailsA
import com.coheser.app.activitesfragments.shoping.GalleryActivity
import com.coheser.app.activitesfragments.shoping.Utils.ItemMoveCallback
import com.coheser.app.activitesfragments.shoping.Utils.PhotoViewHolder
import com.coheser.app.activitesfragments.shoping.adapter.ProductCategoryAdapter
import com.coheser.app.activitesfragments.shoping.adapter.SelectedImagesAdapter
import com.coheser.app.activitesfragments.shoping.models.AddProductModel
import com.coheser.app.activitesfragments.shoping.models.CategoryModel
import com.coheser.app.activitesfragments.shoping.models.GalleryModel
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.ActivityChooseCategoryBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.DataParsing
import com.coheser.app.simpleclasses.Functions
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import java.util.Collections

class ChooseCategory : AppCompatActivity() , ItemMoveCallback.ItemTouchHelperContract {

    var dataModel:AddProductModel?=null

    var binding:ActivityChooseCategoryBinding?=null
    var adapter:ProductCategoryAdapter?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_choose_category)
        setContentView(binding!!.root)

        if(intent!=null) {
            dataModel = intent.getParcelableExtra<AddProductModel>("dataModel")!!
        }
        setSelectedImagesAdapter()
        setCategoryAdapter()

        binding!!.searchEdit.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun afterTextChanged(p0: Editable?) {
                adapter!!.filter.filter(p0.toString())
            }
        })

        binding!!.ivBack.setOnClickListener{
            onBackPressed()
        }

        getAllCategory()
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


    fun getAllCategory() {

        val parameters = JSONObject()
        try {
            parameters.put("parent_id", "0")

        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this,
            ApiLinks.showProductCategories,
            parameters,
            Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this, resp)


            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if (code == "200") {
                    val msgArray = response.getJSONArray("msg")

                    val temp_list = java.util.ArrayList<CategoryModel>()
                    for (i in 0 until msgArray.length()) {
                        val itemdata = msgArray.optJSONObject(i)
                        val Category = itemdata.optJSONObject("Category")

                        var model=DataParsing.getCategoryDataModel(Category)

                        val Children = itemdata.optJSONArray("Children")

                        val childList = java.util.ArrayList<CategoryModel>()
                        if (Children!=null && Children.length() > 0) {
                            for (j in 0 until Children.length()) {
                                val jsonObject = Children.getJSONObject(j)
                                var model=DataParsing.getCategoryDataModel(jsonObject)
                                childList.add(model)
                            }
                        }

                        model.list=childList
                        temp_list.add(model)

                    }

                    dataList.addAll(temp_list)
                }

            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception: comment$e")
            } finally {

                if (dataList.isEmpty()) {

                } else {
                }
                adapter!!.notifyDataSetChanged()
            }
        }
    }


    var dataList = ArrayList<CategoryModel>()
    fun setCategoryAdapter(){

       var linearLayoutManager = LinearLayoutManager(this)
       binding!!.recylerViewCategory.setLayoutManager(linearLayoutManager)
        adapter = ProductCategoryAdapter(
            this, dataList
        ) { view, pos, `object` ->
            val categoryModel = `object` as CategoryModel
            when (view.id) {
                R.id.mainlayout -> {
                    if(categoryModel.list!=null && categoryModel!!.list!!.size>0){

                        openSubCat(categoryModel)
                    }
                    else {
                        dataModel!!.categoryModel=categoryModel
                        openAddDetails()
                    }
                }
            }
        }
        binding!!.recylerViewCategory.setAdapter(adapter)

    }



    private fun openSubCat(model: CategoryModel) {
        val fragment = SubCategoryF.newInstance(model.list, model!!.title!!,object:FragmentCallBack{
            override fun onResponce(bundle: Bundle?) {

                dataModel!!.categoryModel=bundle!!.getParcelable<CategoryModel>("data")
                openAddDetails()
            }
        })
        fragment.show(getSupportFragmentManager(), "SubCategoryF")
    }

    fun openAddDetails(){
        val intent = Intent(this, AddAllDetailsA::class.java)
        intent.putExtra("dataModel",dataModel)
        startActivity(intent)
       overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
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

    override fun onBackPressed() {
        setResult(RESULT_OK, Intent())
        super.onBackPressed()
    }

}