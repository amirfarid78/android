package com.coheser.app.activitesfragments.accounts

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.databinding.ActivityForgotPassBinding
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.Variables
import com.google.firebase.auth.FirebaseAuth


class ForgotPassActivity : AppCompatLocaleActivity() {

    lateinit var binding:ActivityForgotPassBinding
    var userId: String? = null
    var mAuth:FirebaseAuth?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@ForgotPassActivity).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding=DataBindingUtil.setContentView(this,R.layout.activity_forgot_pass)
        mAuth=FirebaseAuth.getInstance()

        actionControl()
    }

    private fun actionControl() {


        binding.btnNext.setOnClickListener(DebounceClickHandler{
            if (validateEmail()) {
                hideError()
                showLoader(this@ForgotPassActivity, false, false)
                sendPasswordResetEmail(binding.recoverEmail.text.toString())

            }
        })

        binding.goBack1.setOnClickListener(DebounceClickHandler{
           finish()
        })


        binding.recoverEmail.doAfterTextChanged {
            if (binding.recoverEmail.text?.length!! > 0) {
                binding.btnNext.setEnabled(true)
                binding.btnNext.setClickable(true)
            } else {
                binding.btnNext.setEnabled(false)
                binding.btnNext.setClickable(false)
            }
            hideError()
        }

    }



    private fun sendPasswordResetEmail(email: String) {
        mAuth!!.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            Functions.cancelLoader()
            if (task.isSuccessful) {
                showToast(this,"A password reset link has been sent to your email. Follow the instructions to reset your password.")
                finish()
            } else {
                showError("We're currently unable to send the password reset email. Please try again later.")
            }
        }
    }



    // check the email validations
    fun validateEmail(): Boolean {
        val email = binding.recoverEmail.text.toString().trim { it <= ' ' }
        return if (email.isEmpty()) {
            showError(binding.root.context.getString(R.string.please_enter_valid_email))
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(binding.root.context.getString(R.string.please_enter_valid_email))
            false
        } else {
            true
        }
    }

    fun showError(error:String){
        binding.errorMsgTxt.text= Constants.alertUniCode+ error
        binding.errorMsgTxt.visibility= View.VISIBLE

    }
    fun hideError(){
        binding.errorMsgTxt.visibility= View.GONE

    }

    override fun onDestroy() {
        super.onDestroy()
    }


}