package com.coheser.app.activitesfragments.accounts

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.ActivityPhoneVarificationBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.DelayedTextWatcher
import com.coheser.app.simpleclasses.FirebaseFunction
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.coheser.app.activitesfragments.WebviewActivity
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PhoneVarificationActivity : AppCompatActivity() {

    lateinit var binding: ActivityPhoneVarificationBinding
    var phoneNo = ""
    var type="add"
    val typeAdd="add"
    val typeChange="change"

    var firebaseUser:FirebaseUser?=null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_varification)

        firebaseUser=FirebaseAuth.getInstance().currentUser

        if(intent.hasExtra("type")){
            type= intent.getStringExtra("type").toString()
        }

        if(type.equals(typeChange)){
            binding.titleTxt.setText("Update Phone")
        }

        initControl()
        actionControl()
    }

    private fun actionControl() {
        binding.resendCode.setOnClickListener(DebounceClickHandler{
            if(type.equals(typeChange)){
                resendCode()
            }
            else{
                callFuntionSendNumber()
            }

        })
        binding.editNumId.setOnClickListener(DebounceClickHandler{})

        binding.sendOtpBtn.setOnClickListener(DebounceClickHandler{
            if(type.equals(typeChange)){
                sendCode()
            }
            else{
                callFuntionVarifyCode()
            }

        })

        binding.btnSendCode.setOnClickListener(DebounceClickHandler{
            sendCodeActionPerform()
        })


        binding.goBack.setOnClickListener(DebounceClickHandler{
            if (binding.viewFlipper.displayedChild == 1) {
                binding.viewFlipper.displayedChild = 0
            } else {
                onBackPressed()
            }
        })


        binding.phoneEdit.addTextChangedListener(
            DelayedTextWatcher(delayMillis = 200) { text ->
                if (text.length > 0) {
                    binding.btnSendCode.setEnabled(true)
                    binding.btnSendCode.isClickable = true
                } else {
                    binding.btnSendCode.setEnabled(false)
                    binding.btnSendCode.isClickable = false
                }
            }
        )

        binding.etCode.addTextChangedListener(
            DelayedTextWatcher(delayMillis = 200) { text ->
                if (text.length == 6) {
                    binding.sendOtpBtn.setEnabled(true)
                    binding.sendOtpBtn.isClickable = true
                } else {
                    binding.sendOtpBtn.setEnabled(false)
                    binding.sendOtpBtn.isClickable = false
                }
            }
        )
    }

    private fun sendCodeActionPerform() {
        if (checkValidation()) {
            if (binding.phoneEdit.text.toString().length<7) {
                binding.phoneEdit.error = getString(R.string.invalid_phone_number)
                binding.phoneEdit.isFocusable = true
                return
            }
            phoneNo = binding.phoneEdit.getText().toString()
            phoneNo = Functions.applyPhoneNoValidation(phoneNo, binding.ccp.selectedCountryCodeWithPlus)


            if(type.equals(typeChange)){
                sendNumberTofirebase()
            }
            else{
                callFuntionSendNumber()
            }

        }
    }

    private fun initControl() {
        if(type.equals(typeAdd)){
            binding.ccp.setCountryForNameCode("us")
            binding.ccp.setClickable(false)
        }else {
            binding.ccp.setCountryForNameCode(binding.ccp.defaultCountryNameCode)
        }

        binding.ccp.registerPhoneNumberTextView(binding.phoneEdit)

    }


    fun openWebUrl(title: String?, url: String?) {
        val intent = Intent(this, WebviewActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }


    fun checkValidation(): Boolean {
        val st_phone: String = binding.phoneEdit.getText().toString()
        if (TextUtils.isEmpty(st_phone)) {
            Toast.makeText(
                this,
                getString(R.string.please_enter_phone_number),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }


    val verificationCallbacks = object : OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {

        }

        override fun onVerificationFailed(e: FirebaseException) {
            Functions.cancelLoader()
            Functions.printLog(Constants.tag,"FirebaseException"+e.toString())
            Functions.showToast(this@PhoneVarificationActivity, "Error in phone number Verification")
            onBackPressed()
        }

        override fun onCodeSent(verificationId: String, token: ForceResendingToken) {
            Functions.cancelLoader()
            phoneVerificationId = verificationId
            resendToken = token
            resetTimerAction()
        }
    }




    private var phoneVerificationId = ""
    private var resendToken: ForceResendingToken? = null
    fun sendNumberTofirebase() {
        setUpVerificatonCallbacks()
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNo) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this@PhoneVarificationActivity) // (optional) Activity for callback binding
            .setCallbacks(verificationCallbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun setUpVerificatonCallbacks() {
        Functions.showLoader(this, false, true)
        FirebaseAuth.getInstance().setLanguageCode("en")

    }



    private fun resetTimerAction() {
        binding.viewFlipper.displayedChild = 1
        binding.resendCode.visibility = View.GONE
        binding.msgTxt.text = getString(R.string.your_code_was_sent_to) + " " + phoneNo
        binding.etCode.setText("")
        oneMinuteTimer()
    }

    fun sendCode() {
        Functions.showLoader(this,false,false)
        if(type.equals(typeChange)){
           PhoneAuthProvider.getCredential(phoneVerificationId, binding.etCode.text.toString())
            updatePhoneFirebase()
        }
        else{
            callApiUpdatePhone()
        }
    }

    fun resendCode() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNo,
            60,
            TimeUnit.SECONDS,
            this@PhoneVarificationActivity,
            verificationCallbacks,
            resendToken
        )
    }


    // run the one minute countdown timer
    var countDownTimer: CountDownTimer? = null
    private fun oneMinuteTimer() {
        binding.rl1Id.visibility = View.VISIBLE
        resetCountDownTimer()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(l: Long) {
                binding.tv1Id.text = "${binding.root.context.getString(R.string.resend_code)} 00:${(l / 1000)}"
            }

            override fun onFinish() {
                binding.tv1Id.visibility = View.GONE
                binding.resendCode.visibility = View.VISIBLE
            }
        }
        countDownTimer?.start()
    }


    fun resetCountDownTimer(){
        countDownTimer?.cancel()
        countDownTimer=null
    }


    override fun onDestroy() {
        resetCountDownTimer()
        super.onDestroy()
    }


    fun callFuntionSendNumber() {
        Functions.showLoader(this,false,false)
        FirebaseFunction.sendVerificationCode(phoneNo,object :FragmentCallBack{
            override fun onResponce(bundle: Bundle?) {
                Functions.cancelLoader()
                if(bundle!=null){
                    val result=bundle.getString("result").toString()
                    Functions.printLog(Constants.tag,result)
                    if(result?.contains("success=true",true) == true){
                        resetTimerAction()
                    }
                }
            }
        })
    }

    fun callFuntionVarifyCode() {
        Functions.showLoader(this,false,false)
        FirebaseFunction.verifyCode(phoneNo,binding.etCode.text.toString(),object :FragmentCallBack{
            override fun onResponce(bundle: Bundle?) {
                Functions.cancelLoader()
                if(bundle!=null){
                    val result=bundle.getString("result").toString()
                    Functions.printLog(Constants.tag,result)
                    if(result?.contains("success=true",true) == true){
                        sendCode()
                    }
                }
            }
        })
    }


    fun updatePhoneFirebase() {
        val credential = PhoneAuthProvider.getCredential(phoneVerificationId, binding.etCode.text.toString())
        firebaseUser?.updatePhoneNumber(credential)
            ?.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    callApiUpdatePhone()
                } else {
                    Functions.cancelLoader()
                    Functions.showToast(this@PhoneVarificationActivity,task.exception?.localizedMessage)
                }
            }


    }


    // call api for phone register code
    private fun callApiUpdatePhone() {
        val parameters = JSONObject()
        try {
            parameters.put("auth_token", Functions.getSharedPreference(binding.root.context).getString(Variables.AUTH_TOKEN, "0"))
            parameters.put("phone", phoneNo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Functions.showLoader(this, false, false)
        VolleyRequest.JsonPostRequest(
            this,
            ApiLinks.editProfile,
            parameters,
            Functions.getHeadersWithOutLogin(this)
        ) { resp ->
            Functions.checkStatus(this@PhoneVarificationActivity, resp)
            Functions.cancelLoader()
            parseLoginData(resp)
        }
    }


    private fun parseLoginData(loginData: String) {
        try {
            val jsonObject = JSONObject(loginData)
            val code = jsonObject.optInt("code",0)
            if (code==200) {

                Functions.getSharedPreference(this@PhoneVarificationActivity).edit()
                    .putString(Variables.U_PHONE_NO, phoneNo).commit()

                val intent = Intent()
                intent.putExtra("isShow",true)
                setResult(RESULT_OK, intent)
                finish()


            } else {
                Toast.makeText(this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}