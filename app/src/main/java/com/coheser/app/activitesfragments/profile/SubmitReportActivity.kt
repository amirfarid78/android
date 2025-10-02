package com.coheser.app.activitesfragments.profile

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivitySubmitReportBinding
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.isStringHasValue
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.ReportViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SubmitReportActivity : AppCompatLocaleActivity(), View.OnClickListener {

    lateinit var reportId: String
    lateinit var txtReportType: String
    var type = ""
    lateinit var id: String

    lateinit var binding:ActivitySubmitReportBinding
    private val viewModel: ReportViewModel by viewModel()


    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode == RESULT_OK) {
                    val data = result.data
                    if (data!!.getBooleanExtra("isShow", false)) {
                        val report_reason = data.getStringExtra("reason")
                        binding.reportType.text = report_reason
                    }
                }
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@SubmitReportActivity).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding=DataBindingUtil.setContentView(this,R.layout.activity_submit_report)


        binding.lifecycleOwner = this
        reportId = intent.getStringExtra("report_id")?:""
        txtReportType = intent.getStringExtra("report_type")?:""
        type = intent.getStringExtra("type")?:""
        id = intent.getStringExtra("id")?:""


        binding.reportType.setText(txtReportType)
        binding.backBtn.setOnClickListener(this)
        binding.reportReasonLayout.setOnClickListener(this)
        binding.submitBtn.setOnClickListener(this)

        setObserveAble()
    }


    fun setObserveAble(){

        viewModel.reportLiveData.observe(this,{
            when(it){
                is ApiResponce.Loading ->{
                    showLoader(this@SubmitReportActivity, false, false)
                }
                is ApiResponce.Success ->{
                    showToast(
                        this@SubmitReportActivity,
                        getString(R.string.report_submitted_successfully)
                    )
                    moveBack()
                }

                is ApiResponce.Error ->{
                    cancelLoader()
                    showToast(this@SubmitReportActivity,it.message)

                }

                else -> {}
            }
        })


    }


    fun checkValidation(): Boolean {
        if (TextUtils.isEmpty(binding.reportType!!.text)) {
            showToast(this@SubmitReportActivity, getString(R.string.please_give_some_reason))
            return false
        }
        return true
    }


    private fun moveBack() {
        val intent = Intent()
        intent.putExtra("isShow", true)
        setResult(RESULT_OK, intent)
        finish()
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.submit_btn ->{
//                if (isStringHasValue(videoId) && checkValidation()) {
//                    viewModel.reportVideo(
//                        videoId,
//                        reportId,
//                        binding.reportDescriptionTxt.text.toString()
//                    )
//                }
//
//                else if (isStringHasValue(productId) && checkValidation()){
//                    viewModel.reportProduct(productId,reportId,binding.reportDescriptionTxt.text.toString())
//                }
//                else if (isStringHasValue(roomId) && checkValidation()) {
//                    viewModel.reportRoom(roomId,reportId,binding.reportDescriptionTxt.text.toString())
//                }
//                else if (isStringHasValue(userId) && checkValidation()) {
//                    viewModel.reportUser(userId,reportId,binding.reportDescriptionTxt.text.toString())
//                }
                if (isStringHasValue(id) && checkValidation()){
                    viewModel.report(id,type,reportId,binding.reportDescriptionTxt.text.toString())
                }
            }


            R.id.report_reason_layout -> {
                val intent = Intent(this@SubmitReportActivity, ReportTypeActivity::class.java)
                intent.putExtra("type", type)
                intent.putExtra("id", id)
                intent.putExtra("isFrom", true)
                resultCallback.launch(intent)
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
            }

            R.id.back_btn -> super@SubmitReportActivity.onBackPressed()
        }
    }
}