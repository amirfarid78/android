package com.coheser.app.activitesfragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject

class PrivacyVideoSettingActivity : AppCompatLocaleActivity(), View.OnClickListener {
    var viewVideo: TextView? = null
    var allowCommentSwitch: Switch? = null
    var allowDuetSwitch: Switch? = null
    var videoId: String? = null
    var commentValue: String? = null
    var duetValue: String? = null
    var privacyValue: String? = null
    var duetVideoId: String? = null
    var allowDuetLayout: RelativeLayout? = null
    var callApi = false
    var viewVideoType: String? = "Private"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Functions.setLocale(
            Functions.getSharedPreference(this@PrivacyVideoSettingActivity)
                .getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this,
            javaClass,
            false
        )
        setContentView(R.layout.activity_privacy_video_setting)
        viewVideo = findViewById(R.id.view_video)
        allowDuetLayout = findViewById(R.id.allow_duet_layout)
        allowCommentSwitch = findViewById(R.id.allow_comment_switch)
        allowCommentSwitch!!.setOnClickListener(this)
        allowDuetSwitch = findViewById(R.id.allow_duet_switch)
        allowDuetSwitch!!.setOnClickListener(this)
        findViewById<View>(R.id.view_video_layout).setOnClickListener(this)
        findViewById<View>(R.id.back_btn).setOnClickListener(this)
        videoId = intent.getStringExtra("video_id")
        privacyValue = intent.getStringExtra("privacy_value")
        duetValue = intent.getStringExtra("duet_value")
        commentValue = intent.getStringExtra("comment_value")
        duetVideoId = intent.getStringExtra("duet_video_id")
        viewVideo!!.setText(privacyValue)
        viewVideoType = privacyValue
        allowCommentSwitch!!.setChecked(commentValue(commentValue))
        allowDuetSwitch!!.setChecked(getTrueFalseCondition(duetValue))
        if (duetVideoId != null && duetVideoId.equals("0", ignoreCase = true)
        ) {
            allowDuetLayout!!.setVisibility(View.VISIBLE)
        }
    }

    private fun getTrueFalseCondition(str: String?): Boolean {
        return str.equals("1", ignoreCase = true)
    }

    private fun commentValue(str: String?): Boolean {
        return str.equals("true", ignoreCase = true)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.view_video_layout -> openDialogForPrivacy(this@PrivacyVideoSettingActivity)
            R.id.back_btn -> onBackPressed()
            R.id.allow_duet_switch -> {
                duetValue = if (allowDuetSwitch!!.isChecked) {
                    "1"
                } else {
                    "0"
                }
                callApi()
            }

            R.id.allow_comment_switch -> {
                commentValue = if (allowCommentSwitch!!.isChecked) {
                    "true"
                } else {
                    "false"
                }
                callApi()
            }

            else -> {}
        }
    }

    // call api for change the privacy setting of profile
    fun callApi() {
        val params = JSONObject()
        try {
            params.put("video_id", videoId)
            params.put("allow_comments", commentValue)
            params.put("allow_duet", duetValue)
            params.put("privacy_type", viewVideoType)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Functions.printLog(Constants.tag, "params at video_setting: $params")
        VolleyRequest.JsonPostRequest(
            this@PrivacyVideoSettingActivity,
            ApiLinks.updateVideoDetail,
            params,
            Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this@PrivacyVideoSettingActivity, resp)
            parseDate(resp)
        }
    }

    fun parseDate(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                Functions.showToast(
                    this@PrivacyVideoSettingActivity,
                    getString(R.string.setting_updated_successfully)
                )
                callApi = true
            } else {
                Functions.showToast(this@PrivacyVideoSettingActivity, jsonObject.optString("msg"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // open the dialog for privacy public or private options
    private fun openDialogForPrivacy(context: Context) {
        val options =
            arrayOf<CharSequence>(getString(R.string.public_), getString(R.string.private_))
        val builder = AlertDialog.Builder(context, R.style.AlertDialogCustom)
        builder.setTitle(null)
        builder.setItems(options) { dialog, item ->
            viewVideo!!.text = options[item]
            viewVideoType = if (item == 0) {
                "Public"
            } else {
                "Private"
            }
            callApi()
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("isShow", callApi)
        intent.putExtra("video_id", videoId)
        setResult(RESULT_OK, intent)
        finish()
    }
}