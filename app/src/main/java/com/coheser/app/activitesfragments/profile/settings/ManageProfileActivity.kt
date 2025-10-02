package com.coheser.app.activitesfragments.profile.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.activitesfragments.accounts.AccountUtils
import com.coheser.app.activitesfragments.accounts.PhoneVarificationActivity
import com.coheser.app.databinding.ActivityManageProfileBinding
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Variables

class ManageProfileActivity : AppCompatLocaleActivity(), View.OnClickListener {

    var binding:ActivityManageProfileBinding?=null
    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {

            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode == RESULT_OK) {
                    val data = result.data
                    if (data!!.getBooleanExtra("isShow", false)) {
                        setUpScreenData()
                    }
                }
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@ManageProfileActivity).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding=DataBindingUtil.setContentView(this,R.layout.activity_manage_profile)

        InitControl()
    }

    private fun InitControl() {
        binding?.tabDeleteAccount?.setOnClickListener(this)
        binding?.backBtn?.setOnClickListener(this)
        binding?.tabChangePhoneNo?.setOnClickListener(this)
        binding?.tabChangePassword?.setOnClickListener(this)
        setUpScreenData()
    }

    private fun setUpScreenData() {

        var phone=getSharedPreference(this).getString(Variables.U_PHONE_NO, "")
        var email=getSharedPreference(this).getString(Variables.U_EMAIL, "")

        if (email!!.contains("@")){
            binding?.tabEmail!!.visibility=View.VISIBLE
            binding?.tvemail!!.text=email

            if (!getSharedPreference(this).getString(Variables.U_SOCIAL,"").equals(AccountUtils.typeSocial)){
                binding?.tabChangePassword!!.visibility = View.VISIBLE
            }
        }else{
            binding?.tabEmail!!.visibility=View.GONE
        }
        if (phone!!.isNotEmpty() && phone != "null"){
            binding?.tabChangePhoneNo!!.visibility = View.VISIBLE
            binding?.tvPhoneNo!!.text = phone
        }else{
            binding?.tabChangePhoneNo!!.visibility = View.GONE
        }
//        if (getSharedPreference(this).getString(Variables.U_SOCIAL, "") == "email") {
//
//            binding?.tabEmail!!.visibility=View.VISIBLE
//            binding?.tvemail!!.text=email
//
//            if(Functions.isStringHasValue(phone)){
//                binding?.tabChangePhoneNo!!.visibility = View.VISIBLE
//                binding?.tvPhoneNo!!.text = phone
//            }
//
//            binding?.tabChangePassword!!.visibility = View.VISIBLE
//
//        }
//
//        else if(getSharedPreference(this).getString(Variables.U_SOCIAL, "") == "google"){
//            binding?.tabEmail!!.visibility=View.VISIBLE
//            binding?.tvemail!!.text= getSharedPreference(this).getString(Variables.U_EMAIL,"")
//
//            if(Functions.isStringHasValue(phone)){
//                binding?.tabChangePhoneNo!!.visibility = View.VISIBLE
//                binding?.tvPhoneNo!!.text = phone
//            }
//        }
//
//        else if (getSharedPreference(this).getString(Variables.U_SOCIAL, "") == "phone") {
//
//            binding?.tabChangePhoneNo!!.visibility = View.VISIBLE
//            binding?.tvPhoneNo!!.text = phone
//
//            if(Functions.isStringHasValue(email)){
//                binding?.tabEmail!!.visibility = View.VISIBLE
//                binding?.tvemail!!.text = email
//            }
//        }

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tabDeleteAccount -> {
                startActivity(Intent(this@ManageProfileActivity, DeleteAccountActivity::class.java))
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
            }

            R.id.tabChangePhoneNo -> {
                val intent = Intent(this@ManageProfileActivity, PhoneVarificationActivity::class.java)
                intent.putExtra("type", "change")
                resultCallback.launch(intent)
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
            }

            R.id.tabChangePassword -> {
                startActivity(Intent(this@ManageProfileActivity, ChangePasswordActivity::class.java))
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
            }

            R.id.back_btn -> {
                onBackPressed()
            }
        }
    }
}