package com.coheser.app.activitesfragments.accounts

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentVerifySignupEmailBinding
import com.coheser.app.models.UserRegisterModel
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.DelayedTextWatcher
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeadersWithOutLogin
import com.coheser.app.simpleclasses.Functions.showLoader
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.Callback
import org.json.JSONObject

class VerifySignupEmailFragment : Fragment() {

    lateinit var binding:FragmentVerifySignupEmailBinding
    var userRegisterModel: UserRegisterModel? = null

    companion object {
        fun newInstance(userRegisterModel: UserRegisterModel?): VerifySignupEmailFragment {
            val fragment = VerifySignupEmailFragment()
            val args = Bundle()
            args.putSerializable("user_model",userRegisterModel)
            fragment.arguments = args
            return fragment
        }
    }
    public override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =DataBindingUtil.inflate(inflater,R.layout.fragment_verify_signup_email, container, false)
        initControl()
        actionControl()
        callApiCodeVerification(false)

        return binding.root
    }

    private fun initControl() {
        arguments?.let {
            userRegisterModel = it.getSerializable("user_model") as UserRegisterModel?
        }

        userRegisterModel?.let {
            binding.edtEmail.setText(it.email)
        }
    }

    // initlize all the click lister
    private fun actionControl() {
        binding.goBack.setOnClickListener(DebounceClickHandler{
            activity?.onBackPressed()
        })
        binding.resendCode.setOnClickListener(DebounceClickHandler{
            binding.resendCode.setVisibility(View.GONE)
            binding.etCode.setText("")
            callApiCodeVerification(false)
        })
        binding.sendOtpBtn.setOnClickListener(DebounceClickHandler{
            callApiCodeVerification(true)
        })

        binding.etCode.addTextChangedListener(
            DelayedTextWatcher(delayMillis = 200) { text ->
                if (text.length == 4) {
                    binding.sendOtpBtn.setEnabled(true)
                    binding.sendOtpBtn.setClickable(true)
                } else {
                    binding.sendOtpBtn.setEnabled(false)
                    binding.sendOtpBtn.setClickable(false)
                }
            }
        )
    }

    // run the one minute countdown timer
    private fun oneMinuteTimer() {
        binding.rl1Id.setVisibility(View.VISIBLE)
        object : CountDownTimer(60000, 1000) {
            public override fun onTick(l: Long) {
                binding.tv1Id.setText("${binding.root.context.getString(R.string.resend_code)} 00:${(l / 1000)}")
            }

            public override fun onFinish() {
                binding.rl1Id.setVisibility(View.GONE)
                binding.resendCode.setVisibility(View.VISIBLE)
            }
        }.start()
    }

    // this method will call the api for code varification
    private fun callApiCodeVerification(isVerify: Boolean) {
        val parameters: JSONObject = JSONObject()
        try {
            if (isVerify) {
                userRegisterModel?.let {
                    parameters.put("email", it.email)
                }
                parameters.put("code", binding.etCode.getText().toString())
            } else {
                userRegisterModel?.let {
                    parameters.put("email", it.email)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.verifyRegisterEmailCode,
            parameters,
            getHeadersWithOutLogin(binding.root.context),
            object : Callback {
                public override fun onResponce(resp: String) {
                    checkStatus(activity, resp)
                    cancelLoader()
                    parseOptData(resp, isVerify)
                }
            })
    }

    // this method will parse the api responce
    fun parseOptData(loginData: String, isVerify: Boolean) {
        try {
            val jsonObject: JSONObject = JSONObject(loginData)
            val code: String = jsonObject.optString("code")
            if ((code == "200")) {
                if (isVerify) {
                    openCreatePasswordF()
                } else {
                    oneMinuteTimer()
                }
            } else {
                Toast.makeText(getContext(), jsonObject.optString("msg"), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openCreatePasswordF() {
        val nextF = CreatePasswordFragment.newInstance("fromEmail",userRegisterModel)
        val transaction: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.in_from_right,
            R.anim.out_to_left,
            R.anim.in_from_left,
            R.anim.out_to_right)
        transaction.addToBackStack(null)
        transaction.replace(R.id.email_verify_container, nextF).commit()
    }
}