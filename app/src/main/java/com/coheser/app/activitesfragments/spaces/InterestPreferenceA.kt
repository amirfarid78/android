package com.coheser.app.activitesfragments.spaces

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.spaces.models.TopicModel
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.ActivityInterestBinding
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.DataParsing.getTopicDataModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Variables
import com.facebook.drawee.view.SimpleDraweeView
import com.realpacific.clickshrinkeffect.applyClickShrink
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject

class InterestPreferenceA : AppCompatLocaleActivity(), View.OnClickListener {
    lateinit var binding: ActivityInterestBinding
    var selectedTopic: ArrayList<TopicModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(
                this
            ).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this, javaClass, false
        )
        binding = DataBindingUtil.setContentView(
            this@InterestPreferenceA,
            R.layout.activity_interest
        )
        InitControl()
    }


    private fun InitControl() {
        binding.ivBack.setOnClickListener(this)
        binding.ivBack.applyClickShrink()

        binding.saveBtn.setOnClickListener(this)
        binding.saveBtn.applyClickShrink()

        topicsCategoryLists
    }


    var topicModels: ArrayList<TopicModel> = ArrayList()
    private val topicsCategoryLists: Unit
        get() {
            val parameters = JSONObject()
            try {
                parameters.put("user_id", getSharedPreference(this).getString(Variables.U_ID, ""))
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }

            VolleyRequest.JsonPostRequest(
                this, ApiLinks.showTopics, parameters, getHeaders(
                    this
                )
            ) { resp -> parseResponseData(resp) }
        }

    private fun parseResponseData(resp: String) {
        try {
            val jsonObject = JSONObject(resp)
            val code = jsonObject.optString("code")
            if (code == "200") {
                topicModels.clear()

                val msgArray = jsonObject.getJSONArray("msg")
                for (i in 0 until msgArray.length()) {
                    val innerObject = msgArray.getJSONObject(i)

                    val topic = innerObject.optJSONObject("Topic")
                    val model = getTopicDataModel(topic)
                    topicModels.add(model)
                }
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : parseResponseData $e")
        } finally {
            binding.progressBar.visibility = View.GONE

            if (topicModels.isEmpty()) {
                binding.tabNoData.visibility = View.VISIBLE
                binding.tvNoData.text = binding.root.context.getString(R.string.no_topic_found)
            } else {
                binding.tabNoData.visibility = View.GONE
                populateDataList(topicModels)
            }
        }
    }

    private fun populateDataList(listData: ArrayList<TopicModel>) {
        for (i in listData.indices) {
            val itemModel = listData[i]

            val tabTag = LayoutInflater.from(binding.root.context)
                .inflate(R.layout.item_topic, null) as RelativeLayout
            val innerView = tabTag.findViewById<LinearLayout>(R.id.innerView)
            val ivTag = innerView.findViewById<SimpleDraweeView>(R.id.ivTag)
            val ivFrameTag = innerView.findViewById<View>(R.id.ivFrameTag)
            val tvTag = innerView.findViewById<TextView>(R.id.tvTag)
            tvTag.text = "" + itemModel.title

            tabTag.tag = i
            ivTag.controller = frescoImageLoad(
                binding.root.context,
                "" + itemModel.title,
                binding.root.context.resources.getDimension(R.dimen._9sdp).toInt(),
                itemModel.image, ivTag
            )


            tvTag.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.black
                )
            )
            tabTag.isActivated = false
            ivFrameTag.backgroundTintList = ContextCompat.getColorStateList(
                binding.root.context, R.color.lightgraycolor
            )


            tabTag.setOnClickListener { v: View? ->
                if (selectedTopic.contains(itemModel)) {
                    selectedTopic.remove(itemModel)
                    tvTag.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.black
                        )
                    )
                    tabTag.isActivated = false
                    ivFrameTag.backgroundTintList = ContextCompat.getColorStateList(
                        binding.root.context, R.color.lightgraycolor
                    )
                } else if (selectedTopic.size < 1) {
                    selectedTopic.add(itemModel)
                    tvTag.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.white
                        )
                    )
                    tabTag.isActivated = true
                    ivFrameTag.backgroundTintList = ContextCompat.getColorStateList(
                        binding.root.context, R.color.appColor
                    )
                }
                binding.countTxt.text = "" + selectedTopic.size
            }
            binding.chipGroup.addView(tabTag)
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivBack -> {
                finish()
            }

            R.id.saveBtn -> {
                val bundle = Intent()
                bundle.putExtra("isShow", true)
                bundle.putExtra("dataList", selectedTopic)
                setResult(RESULT_OK, bundle)
                finish()
            }
        }
    }
}