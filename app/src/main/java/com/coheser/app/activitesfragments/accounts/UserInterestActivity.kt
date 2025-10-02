package com.coheser.app.activitesfragments.accounts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.accounts.adapter.InterestAdapter
import com.coheser.activitiesfragments.accounts.model.Interest
import com.coheser.activitiesfragments.accounts.model.InterestModel
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.ActivityUserInterestBinding
import com.coheser.app.interfaces.AdapterClickListener3
import com.coheser.app.simpleclasses.ApiRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.google.android.material.chip.Chip
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.APICallBack
import io.paperdb.Paper
import org.json.JSONArray
import org.json.JSONObject


class UserInterestActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserInterestBinding
    var dataList: ArrayList<InterestModel> = ArrayList()
    var adapter: InterestAdapter? = null
    var selectedList: ArrayList<Interest> = ArrayList()

    var from="foryou"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInterestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.hasExtra("from")){
            from= intent.getStringExtra("from").toString()
        }



        binding.btnReset.setOnClickListener {
            selectedList.clear()

            for ((parentIndex,intrestModel) in dataList.withIndex()) {
                for ((childIndex,intest) in intrestModel.userIntrest.withIndex()){
                    if(intest.selected.equals("1")){
                        intest.selected="0"
                        intrestModel.userIntrest.set(childIndex,intest)
                    }
                }
                dataList.set(parentIndex,intrestModel)
            }
            setAdapter()
        }

        binding.btnNext.setOnClickListener {
            if(selectedList.size<3){
                Functions.showToast(this, getString(R.string.please_select_at_least_3_interests))
            }
            else {
                callApiAddInterest()
            }
        }


        val list=Paper.book().read<ArrayList<InterestModel>>(Variables.Interests)
        if(list!=null) {
            dataList.addAll(list)
            for (intrestModel in dataList) {
                for (intest in intrestModel.userIntrest) {
                    if (intest.selected.equals("1")) {
                        selectedList.add(intest)
                        Functions.printLog(Constants.tag,"intrestedName:"+intest.title)
                    }
                }
            }
        }

        setAdapter()


        callShowInteterest()

    }

    fun setAdapter(){
        adapter = InterestAdapter(this, dataList, object : AdapterClickListener3 {
            override fun onItemClick(view: View?, parentPos: Int,childPos: Int, `object`: Any?) {
                Functions.printLog(Constants.tag,"position:"+childPos)

                if (view is Chip) {
                    val chip = view
                    val interest = `object` as Interest
                    interest.let {
                        val exist = selectedList.any { it.id.equals(interest.id) }
                        if (exist) {
                            val index = selectedList.indexOfFirst { it.id.equals(interest.id)}
                            // Chip was already selected, so deselect it
                            chip.setTextColor(resources.getColor(R.color.black))
                            chip.setChipBackgroundColorResource(android.R.color.white)
                            chip.setChipStrokeColorResource(R.color.graycolor2)
                            chip.chipStrokeWidth = 1f
                            selectedList.removeAt(index)
                            interest.selected="0"

                        } else {
                            // Chip was not selected, so select it
                            selectedList.add(it)
                            chip.setTextColor(resources.getColor(R.color.white))
                            chip.setChipBackgroundColorResource(R.color.appColor)
                            chip.setChipStrokeColorResource(R.color.appColor)
                            interest.selected="1"
                        }

                        val interestModel=dataList.get(parentPos)
                        interestModel.userIntrest.set(childPos,interest)
                        dataList.removeAt(parentPos)
                        dataList.add(parentPos,interestModel)

                    }
                }
                Functions.printLog(Constants.tag,"selected list size:"+selectedList.size)
            }

        })
        binding.recylerview.adapter = adapter

    }

    private fun callShowInteterest() {
        if(dataList.isEmpty()){
            binding.shimmerRoot.shimmerViewContainer.startShimmer()
            binding.shimmerRoot.shimmerViewContainer.visibility=View.VISIBLE
            binding.dataLay.visibility=View.GONE
        }
        else{
            binding.shimmerRoot.shimmerViewContainer.stopShimmer()
            binding.shimmerRoot.shimmerViewContainer.visibility=View.GONE
            binding.dataLay.visibility=View.VISIBLE
        }

        ApiRepository.callShowInterest(this,object : APICallBack {
            override fun arrayData(list: java.util.ArrayList<*>?) {

                val arraylist=list as ArrayList<InterestModel>
                dataList.clear()
                dataList.addAll(arraylist)
                selectedList.clear()

                for (intrestModel in dataList) {
                    for (intest in intrestModel.userIntrest){
                        if(intest.selected.equals("1")){
                            selectedList.add(intest)
                        }
                    }
                }
                adapter?.notifyDataSetChanged()
                binding.shimmerRoot.shimmerViewContainer.visibility = View.GONE
                binding.dataLay.visibility = View.VISIBLE
            }

            override fun onSuccess(p0: String?) {
                binding.shimmerRoot.shimmerViewContainer.visibility = View.GONE
                binding.dataLay.visibility = View.VISIBLE
            }

            override fun onFail(p0: String?) {
                binding.shimmerRoot.shimmerViewContainer.visibility = View.GONE
                binding.dataLay.visibility = View.VISIBLE
            }
        })

    }



    fun saveArrayList(list: ArrayList<InterestModel>?) {
        list?.let {
            Paper.book().write(Variables.Interests, it)
        }
    }


    fun callApiAddInterest() {

        val parameters = JSONObject()
        try {
            parameters.put(
                "user_id",
                Functions.getSharedPreference(this).getString(Variables.U_ID, "")
            )
            var interestJson = JSONArray()
            for (interest in selectedList) {
                var json = JSONObject()
                json.put("interest_id", interest.id)
                json.put("name", interest.title)
                interestJson.put(json)
            }
            parameters.put("interests", interestJson)


        } catch (e: Exception) {
            e.printStackTrace()
        }

        Functions.showLoader(this, false, false)
        VolleyRequest.JsonPostRequest(
            this,
            ApiLinks.addUserInterest,
            parameters,
            Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this, resp)
            Functions.cancelLoader()
            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if (code == "200") {

                    saveArrayList(dataList)
                    sendBroadByName(Variables.homeBroadCastAction)
                    finish()
                    overridePendingTransition(
                        R.anim.in_from_top,
                        R.anim.out_from_bottom
                    )
                }

            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception: comment$e")
                Functions.cancelLoader()
            }
        }
    }


    private fun sendBroadByName(action: String) {
        val intent = Intent(action)
        intent.putExtra("type","interest")
        intent.setPackage(applicationContext.packageName)
        applicationContext.sendBroadcast(intent)
    }

    override fun onBackPressed() {
        if(from!=null && from.equals("foryou")) {
            finish()
            overridePendingTransition(
                R.anim.in_from_top,
                R.anim.out_from_bottom
            )
        }
    }
}