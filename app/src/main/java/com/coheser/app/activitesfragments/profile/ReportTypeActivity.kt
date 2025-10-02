package com.coheser.app.activitesfragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.adapters.ReportTypeAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityReportTypeBinding
import com.coheser.app.models.ReportTypeModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.ReportViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReportTypeActivity : AppCompatLocaleActivity(), View.OnClickListener {

    var adapter: ReportTypeAdapter? = null
     var isFromRegister=false
    lateinit var id: String
    var type = ""

    var dataList = mutableListOf<ReportTypeModel>()

    lateinit var binding:ActivityReportTypeBinding

    private val viewModel: ReportViewModel by viewModel()


    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode == RESULT_OK) {
                    val data = result.data
                    if (data!!.getBooleanExtra("isShow", false)) {
                        super@ReportTypeActivity.onBackPressed()
                    }
                }
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@ReportTypeActivity).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding=DataBindingUtil.setContentView(this,R.layout.activity_report_type)


        binding.lifecycleOwner = this
        binding.backBtn.setOnClickListener(this)

        isFromRegister = intent.getBooleanExtra("isFrom", false)
        type = intent.getStringExtra("type") ?: ""
        id = intent.getStringExtra("id") ?: ""


        setAdapter()
        setObserveAble()

        viewModel.showReportReasons()

    }

    fun setObserveAble(){

        viewModel.reportTypeLiveData.observe(this,{
            when(it){
                is ApiResponce.Loading ->{
                    showLoader(this@ReportTypeActivity, false, false)
                }
                is ApiResponce.Success ->{
                    cancelLoader()
                    it.data?.let {
                        dataList.clear()
                        dataList.addAll(it)
                        adapter!!.notifyDataSetChanged()
                    }

                }

                is ApiResponce.Error ->{
                    cancelLoader()
                    showToast(this@ReportTypeActivity,it.message)

                }

                else -> {}
            }
        })


    }

    private fun setAdapter() {
        adapter = ReportTypeAdapter(this@ReportTypeActivity, dataList, {view, positon, objectModel ->
            val item=objectModel as ReportTypeModel
            when (view.id) {
                R.id.rlt_report -> if (isFromRegister!!) {
                    printLog(Constants.tag, item.title)
                    sendDataBack(item.title)
                } else {
                    val intent = Intent(this@ReportTypeActivity, SubmitReportActivity::class.java)
                    intent.putExtra("report_id", item.id)
                    intent.putExtra("report_type", item.title)
                    intent.putExtra("type", type)
                    intent.putExtra("id", id)
                    try {
                        resultCallback.launch(intent)
                    } catch (e: Exception) {
                        startActivity(intent)
                    }
                    overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
                }
            }
        });
        adapter!!.setHasStableIds(true)
        binding.recylerview.setHasFixedSize(true)
        binding.recylerview.layoutManager = LinearLayoutManager(this@ReportTypeActivity, LinearLayoutManager.VERTICAL, false)
        binding.recylerview.adapter = adapter
        adapter!!.notifyDataSetChanged()
    }

    private fun sendDataBack(reason: String) {
        val intent = Intent()
        intent.putExtra("isShow", true)
        intent.putExtra("reason", reason)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.back_btn -> super@ReportTypeActivity.onBackPressed()
        }
    }


}