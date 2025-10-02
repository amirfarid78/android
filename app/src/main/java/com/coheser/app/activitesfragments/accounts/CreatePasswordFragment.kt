package com.coheser.app.activitesfragments.accounts

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentCreatePasswordBinding
import com.coheser.app.models.UserRegisterModel
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.google.firebase.auth.FirebaseAuth
import com.coheser.app.activitesfragments.SplashActivity
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject

/**
 * Create password once user has entered the email
 */
class CreatePasswordFragment : Fragment() {

    lateinit var binding: FragmentCreatePasswordBinding
    var userRegisterModel: UserRegisterModel? = null

    var fromWhere: String = ""
    var stEmail: String? = null

    lateinit var mAuth: FirebaseAuth

    companion object {
        fun newInstance(
            fromWhere: String, userRegisterModel: UserRegisterModel?
        ): CreatePasswordFragment {
            val fragment = CreatePasswordFragment()
            val args = Bundle()
            args.putString("fromWhere", fromWhere)
            args.putSerializable("user_model", userRegisterModel)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_create_password, container, false)
        mAuth = FirebaseAuth.getInstance()

        initControl()
        actionControl()
        return binding.root
    }

    private fun actionControl() {
        binding.edtPassword.doAfterTextChanged {
            if (binding.edtPassword.text?.length!! > 0) {
                binding.btnPass.setEnabled(true)
                binding.btnPass.setClickable(true)
            } else {
                binding.btnPass.setEnabled(false)
                binding.btnPass.setClickable(false)
            }
            binding.passwordFieldLayout.error = null
            binding.passwordFieldLayout.isErrorEnabled = false
        }

        binding.goBack.setOnClickListener(DebounceClickHandler {
            activity?.onBackPressed()
        })

        binding.crossBtn.setOnClickListener {


            val intent = Intent(context, SplashActivity::class.java)
            intent.putExtra("openMain",true)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

        }
        binding.btnPass.setOnClickListener(DebounceClickHandler {
            createPassword()
        })


        setUpPasswordListeners()

    }

    private fun setUpPasswordListeners() {
        val passwordEditText = binding.edtPassword
        val confirmPasswordEditText = binding.edtConfirmPassword
        val ruleMinLength = binding.ruleMinLength
        val ruleOneNumber = binding.ruleOneNumber
        val ruleSpecialChar = binding.ruleSpecialChar

        var passwordValid = false
        var passwordsMatch = false

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(editable: Editable?) {
                val password = editable.toString()
                var rulesMatched = 0

                // Check minimum length
                if (password.length >= 8) {
                    rulesMatched++
                    ruleMinLength.isSelected = true
                } else {
                    ruleMinLength.isSelected = false
                }

                // Check letter and number
                if (password.any { it.isDigit() } && password.any { it.isLetter() }) {
                    rulesMatched++
                    ruleOneNumber.isSelected = true
                } else {
                    ruleOneNumber.isSelected = false
                }

                // Check special character
                if (password.any { !it.isLetterOrDigit() }) {
                    rulesMatched++
                    ruleSpecialChar.isSelected = true
                } else {
                    ruleSpecialChar.isSelected = false
                }

                // Update password validity
                passwordValid = rulesMatched == 3

                // Update password strength UI (your custom function)
                updatePasswordStrengthUI(rulesMatched)

                // Compare passwords if confirmation field is not empty
                val confirmPassword = confirmPasswordEditText.text.toString()
                passwordsMatch = password == confirmPassword
                updateConfirmPasswordUI(passwordsMatch, passwordValid)
            }
        })

// TextWatcher for the confirm password field
        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(editable: Editable) {
                val confirmPassword = editable.toString()
                val password = passwordEditText.text.toString()

                // Compare passwords
                passwordsMatch = password == confirmPassword
                updateConfirmPasswordUI(passwordsMatch, passwordValid)
            }
        })
    }

    private fun updatePasswordStrengthUI(rulesMatched: Int) {
        binding.passwordStrengthBar.progress = if (rulesMatched == 3) {
            100
        } else {
            rulesMatched * 33
        }
        binding.passwordStrengthLabel.text = when (rulesMatched) {
            0 -> "Weak"
            1 -> "Fair"
            2 -> "Good"
            3 -> "Strong"
            else -> ""
        }
    }

    // Function to update the UI when passwords are compared
    fun updateConfirmPasswordUI(passwordsMatch: Boolean, passwordValid: Boolean) {
        val confirmPasswordLayout = binding.confirmPasswordFieldLayout
        if (passwordsMatch) {
            confirmPasswordLayout.error = null  // Clear the error
        } else {
            confirmPasswordLayout.error = "Passwords do not match"  // Set an error message
        }

        // Optionally enable/disable the 'Next' button based on password validity and match
        binding.btnPass.isEnabled = passwordValid && passwordsMatch
    }

    private fun createPassword() {
        if (checkValidation()) {
            userRegisterModel?.password = binding.edtPassword.text.toString()
            userRegisterModel?.let {
                createFireBaseUser(it)
            }

        }
    }


    fun createFireBaseUser(model: UserRegisterModel) {
        Functions.showLoader(activity, false, false)
        if (!TextUtils.isEmpty(model.firebaseUID)) {
            callApiForSignup(model)
        } else if (fromWhere == "fromEmail") {
            Functions.printLog(Constants.tag,"email:"+model.email+": Password:"+model.password)
            mAuth.createUserWithEmailAndPassword(
                model.email, model.password
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    model.firebaseUID = "${FirebaseAuth.getInstance().uid}"
                    callApiForSignup(model)
                } else {
                    Functions.cancelLoader()
                    showError("" + task.exception?.localizedMessage)
                }
            }
        }

    }

    private fun verifyEmail() {
        val nextF = VerifyEmailFragment.newInstance("fromEmail", userRegisterModel)
        val transaction: FragmentTransaction =
            requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right
        )
        transaction.addToBackStack(null)
        transaction.replace(R.id.sign_up_fragment, nextF).commit()
    }

    private fun callApiForSignup(model: UserRegisterModel) {
        val parameters = JSONObject()
        try {

            parameters.put("auth_token", model.firebaseUID)
            parameters.put("device_token", Variables.DEVICE)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.registerUser, parameters, Functions.getHeadersWithOutLogin(
                activity
            )
        ) { resp ->
            Functions.checkStatus(activity, resp)

            parseSignupData(resp)
        }
    }

    // if the signup successfull then this method will call and it store the user info in local
    fun parseSignupData(loginData: String?) {
        Functions.cancelLoader()
        try {
            val jsonObject = JSONObject(loginData)
            val code = jsonObject.optString("code")
            if (code == "200") {
                sendEmailVerification()

            } else {
                showError(jsonObject.optString("msg"))

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendEmailVerification() {
        val user = mAuth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
            if (verificationTask.isSuccessful) {

                showAlert("We've sent you an email. Click the link to verify your account.")

                binding.goBack.visibility = View.GONE
                binding.crossBtn.visibility = View.VISIBLE
                binding.btnPass.visibility = View.GONE
                verifyEmail()

            } else {
                showError("" + verificationTask.exception?.localizedMessage)
            }
        }
    }

    private fun initControl() {
        arguments?.let {
            fromWhere = it.getString("fromWhere", "")
            userRegisterModel = it.getSerializable("user_model") as UserRegisterModel?
            stEmail = it.getString("email")
        }


        binding.edtPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
    }

    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data = result.data
            data?.let {
                userRegisterModel = it.getSerializableExtra("model") as UserRegisterModel
                Log.d(Constants.tag, "user call back:  ${userRegisterModel!!.company_name}")
            }
        }
    }

    // this will check the validations like none of the field can be the empty
    fun checkValidation(): Boolean {
        val password = binding.edtPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.passwordFieldLayout.error =
                Constants.alertUniCode + binding.root.context.getString(R.string.enter_valid_new_password)
            return false
        }
        else if (password.length <= 5) {
            binding.passwordFieldLayout.error =
                Constants.alertUniCode + binding.root.context.getString(R.string.valid_password_length)

            return false
        } else {
            hideError()
            return true
        }
    }

    fun showError(error: String) {
        binding.errorMsgTxt.text = Constants.alertUniCode + error
        binding.errorMsgTxt.visibility = View.VISIBLE

    }

    fun showAlert(error: String) {
        binding.errorMsgTxt.text = error
        binding.errorMsgTxt.visibility = View.VISIBLE
        binding.errorMsgTxt.setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.greenColor
            )
        )

    }

    fun hideError() {
        binding.errorMsgTxt.visibility = View.GONE

    }

}