package com.coheser.app.activitesfragments.shoping

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.ActivitySelectProductBinding
import com.coheser.app.simpleclasses.Functions
import com.google.gson.Gson
import com.coheser.app.activitesfragments.shoping.adapter.SelectProductsAdapter
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject

class SelectProductA : AppCompatActivity() ,View.OnClickListener{

    var binding:ActivitySelectProductBinding ?=null
    var dataList = ArrayList<ProductModel>()
    var adapter: SelectProductsAdapter? = null
    var context: Context? = null
    var pageCount = 0
    var ispostFinsh = false
    var linearLayoutManager: LinearLayoutManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_select_product)



        linearLayoutManager = LinearLayoutManager(context)
        binding!!.recylerview.setLayoutManager(linearLayoutManager)
        adapter = SelectProductsAdapter(
           this, dataList
        ) { view, pos, `object` ->
            val productModel = `object` as ProductModel
            when (view.id) {
                R.id.addBtnTxt -> {
                    val intent=Intent(this@SelectProductA,TagProductNameActivity::class.java)
                    intent.putExtra("data",productModel)
                    resultCallback.launch(intent)
                }
            }
        }
        binding!!.recylerview.setAdapter(adapter)

        binding!!.recylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var userScrolled = false
            var scrollOutitems = 0
            var scrollInItem = 0
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollInItem = linearLayoutManager!!.findFirstVisibleItemPosition()
                scrollOutitems = linearLayoutManager!!.findLastVisibleItemPosition()
                if (scrollInItem == 0) {
                    recyclerView.isNestedScrollingEnabled = true
                } else {
                    recyclerView.isNestedScrollingEnabled = false
                }
                if (userScrolled && scrollOutitems == dataList.size - 1) {
                    userScrolled = false
                    if (binding!!.loadMoreProgress.getVisibility() !== View.VISIBLE && !ispostFinsh) {
                        binding!!.loadMoreProgress.setVisibility(View.VISIBLE)
                        pageCount = pageCount + 1
                        callApiShowProducts()
                    }
                }
            }
        })

        binding!!.backbtn.setOnClickListener(this)

        callApiShowProducts()
    }


    var isApiRun = false
    //this will get the all videos data of user and then parse the data
    private fun callApiShowProducts() {
        if (dataList == null) dataList = java.util.ArrayList()
        isApiRun = true
        val parameters = JSONObject()
        try {
            parameters.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (pageCount == 0) {
            binding!!.pbar.setVisibility(View.VISIBLE)
            binding!!.noDataLayout.setVisibility(View.GONE)
        }
        VolleyRequest.JsonPostRequest(
            this, ApiLinks.showProducts, parameters, Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this, resp)
            isApiRun = false
            parseData(resp)
        }
    }

    fun parseData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msg = jsonObject.optJSONArray("msg")
                val temp_list = java.util.ArrayList<ProductModel>()
                for (i in 0 until msg.length()) {
                    val itemdata = msg.optJSONObject(i)
                    val model = Gson().fromJson(
                        itemdata.toString(),
                        ProductModel::class.java
                    )
                    temp_list.add(model)
                }
                if (pageCount == 0) {
                    dataList.clear()
                    dataList.addAll(temp_list)
                } else {
                    dataList.addAll(temp_list)
                }
            }
            adapter!!.notifyDataSetChanged()
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
        } finally {
            binding!!.pbar.setVisibility(View.GONE)
            binding!!.loadMoreProgress.setVisibility(View.GONE)
            if (dataList.isEmpty()) {
                binding!!.noDataLayout.setVisibility(View.VISIBLE)
            } else {
                binding!!.noDataLayout.setVisibility(View.GONE)
            }
        }
    }

    override fun onClick(v: View?) {
        if(v!!.id==R.id.backbtn){
            finish()
        }

    }



    val resultCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            setResult(RESULT_OK,data)
            finish()
        }
    }

}