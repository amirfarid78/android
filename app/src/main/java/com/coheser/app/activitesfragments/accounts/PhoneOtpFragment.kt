package com.coheser.app.activitesfragments.accounts

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentOtpBinding
import com.coheser.app.models.UserModel
import com.coheser.app.models.UserRegisterModel
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.DelayedTextWatcher
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeadersWithOutLogin
import com.coheser.app.simpleclasses.Functions.setUpMultipleAccount
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.Variables
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.coheser.app.activitesfragments.SplashActivity
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.Callback
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Send OTP toPhon the phone number and verify the OTP on sign in/ sign up
 */
class PhoneOtpFragment : Fragment() {

    lateinit var binding: FragmentOtpBinding
    var phoneNo = ""
    var userRegisterModel: UserRegisterModel? = null

    // run the one minute countdown timer
    private var countDownTimer: CountDownTimer? = null
    private var phoneVerificationId = ""
    private var resendToken: ForceResendingToken? = null

    companion object {
        fun newInstance(phoneNo: String, userRegisterModel: UserRegisterModel?): PhoneOtpFragment {
            val fragment = PhoneOtpFragment()
            val args = Bundle()
            args.putString("phoneNo", phoneNo)
            args.putSerializable("user_model", userRegisterModel)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_otp, container, false)
        initControl()
        actionControl()
        return binding.root
    }

    private fun initControl() {
        arguments?.let {
            phoneNo = it.getString("phoneNo", "")
            userRegisterModel = it.getSerializable("user_model") as UserRegisterModel?
        }
        binding.msgTxt.text =
            "${binding.root.context.getString(R.string.your_code_was_sent_to)} $phoneNo"
        sendNumberToFirebase(phoneNo)
    }

    private fun actionControl() {
        binding.etCode.addTextChangedListener(
            DelayedTextWatcher(delayMillis = 200) { text ->
                if (text.length == 6) {
                    binding.sendOtpBtn.isEnabled = true
                    binding.sendOtpBtn.isClickable = true
                } else {
                    binding.sendOtpBtn.isEnabled = false
                    binding.sendOtpBtn.isClickable = false
                }
            }
        )

        binding.goBack.setOnClickListener(DebounceClickHandler {
            activity?.onBackPressed()
        })
        binding.resendCode.setOnClickListener(DebounceClickHandler {
            hideError()
            resendCode()
        })
        binding.editNumId.setOnClickListener(DebounceClickHandler {
            activity?.onBackPressed()
        })
        binding.sendOtpBtn.setOnClickListener(DebounceClickHandler {
            hideError()
            sendCode()
        })
    }

    val verificationCallbacks = object : OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            cancelLoader()
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            cancelLoader()
            Functions.printLog(Constants.tag,"FirebaseException"+e.toString())
            showToast(
                binding.root.context,
                binding.root.context.getString(R.string.error_in_phone_number_verification)
            )
            activity?.onBackPressed()
        }

        override fun onCodeSent(verificationId: String, token: ForceResendingToken) {
            cancelLoader()
            phoneVerificationId = verificationId
            resendToken = token
            resetTimerAction()
        }
    }

    private fun sendNumberToFirebase(phoneNumber: String) {
        showLoader(activity, false, true)
        FirebaseAuth.getInstance().setLanguageCode("en")
        val options: PhoneAuthOptions = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(verificationCallbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(
                requireActivity()
            ) { task ->
                cancelLoader()
                if (task.isSuccessful) {
                    userRegisterModel?.firebaseUID = FirebaseAuth.getInstance().uid.toString()
                    userRegisterModel?.firebaseUID?.let { callApiUserDetails(it) }
                } else {
                    showError(binding.root.context.getString(R.string.otp_code_not_matched_please_recheck_your_code_))

                }
            }
    }

    fun sendCode() {
        val credential: PhoneAuthCredential =
            PhoneAuthProvider.getCredential(phoneVerificationId, binding.etCode.text.toString())
        showLoader(activity, false, false)
        signInWithPhoneAuthCredential(credential)
    }

    fun resendCode() {
        showLoader(activity, false, false)
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNo,
            60,
            TimeUnit.SECONDS,
            requireActivity(),
            verificationCallbacks,
            resendToken
        )
    }

    private fun oneMinuteTimer() {
        binding.rl1Id.visibility = View.VISIBLE
        resetCountDownTimer()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(l: Long) {
                binding.tv1Id.text =
                    "${binding.root.context.getString(R.string.resend_code)} 00:${(l / 1000)}"
            }

            override fun onFinish() {
                binding.rl1Id.visibility = View.GONE
                binding.resendCode.visibility = View.VISIBLE
            }
        }
        countDownTimer?.start()
    }

    fun resetCountDownTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    override fun onDetach() {
        resetCountDownTimer()
        super.onDetach()
    }

    // call api for phone register code
    private fun callApiUserDetails(authTokon: String) {
        val parameters = JSONObject()
        try {
            parameters.put("auth_token", authTokon)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.showUserDetail,
            parameters,
            Functions.getHeadersWithAuthTokon(authTokon),
            object : Callback {
                override fun onResponce(resp: String) {
                    checkStatus(activity, resp)
                    cancelLoader()
                    parseLoginData(resp)
                }
            })

    }

    private fun parseLoginData(loginData: String) {
        try {
            val jsonObject = JSONObject(loginData)
            val code: String = jsonObject.optString("code")
            // if code is 200 then login
            if ((code == "200")) {
                val jsonObj: JSONObject = jsonObject.getJSONObject("msg")
                val userDetailModel: UserModel = getUserDataModel(jsonObj.optJSONObject("User"))

                setUpMultipleAccount(binding.root.context, userDetailModel)
                Variables.reloadMyVideos = true
                Variables.reloadMyVideosInner = true
                Variables.reloadMyLikesInner = true
                Variables.reloadMyNotification = true

                val intent = Intent(binding.root.context, SplashActivity::class.java)
                intent.putExtra("openMain",true)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                activity?.finish()

            } else if ((code == "201") && !jsonObject.optString("msg")
                    .contains("have been blocked", true)
            ) {
                // register
                callApiForSignup(userRegisterModel!!)
            } else {
                showError(jsonObject.optString("msg"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resetTimerAction() {
        binding.resendCode.visibility = View.GONE
        binding.etCode.setText("")
        oneMinuteTimer()
    }

    private fun callApiForSignup(model: UserRegisterModel) {
        val parameters = JSONObject()
        try {
            parameters.put("auth_token", model.firebaseUID)
            parameters.put("phone", model.phoneNo)
            parameters.put("device_token", Variables.DEVICE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.registerUser, parameters, getHeadersWithOutLogin(
                activity
            )
        ) { resp ->
            checkStatus(activity, resp)

            parseSignupData(resp)
        }
    }

    // if the signup successfull then this method will call and it store the user info in local
    fun parseSignupData(loginData: String?) {
        cancelLoader()
        try {
            val jsonObject = JSONObject(loginData)
            val code = jsonObject.optString("code")
            if (code == "200") {

                val jsonObj = jsonObject.getJSONObject("msg")
                val userDetailModel = getUserDataModel(jsonObj.optJSONObject("User"))

                setUpMultipleAccount(binding.root.context, userDetailModel)

                Variables.reloadMyVideos = true
                Variables.reloadMyVideosInner = true
                Variables.reloadMyLikesInner = true
                Variables.reloadMyNotification = true

                openGetStartActivity("fromPhone")
            } else {
                showError(jsonObject.optString("msg"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openGetStartActivity(type: String) {
        val DOBF = DateOfBirthFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.in_from_right,
            R.anim.out_to_left,
            R.anim.in_from_left,
            R.anim.out_to_right
        )
        val bundle = Bundle()
        bundle.putSerializable("user_model", userRegisterModel)
        if (type.equals(AccountUtils.typeSocial)) {
            bundle.putString("fromWhere", AccountUtils.typeSocial)
        } else {
            bundle.putString("fromWhere", AccountUtils.typeSignUp)
        }
        DOBF.arguments = bundle
        transaction.addToBackStack(null)
        transaction.replace(R.id.login_f, DOBF).commit()
    }

    fun showError(error: String) {
        binding.errorMsgTxt.text = Constants.alertUniCode + error
        binding.errorMsgTxt.visibility = View.VISIBLE

    }

    fun hideError() {
        binding.errorMsgTxt.visibility = View.GONE

    }
}