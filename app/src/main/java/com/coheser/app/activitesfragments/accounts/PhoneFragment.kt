package com.coheser.app.activitesfragments.accounts

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentAddPhoneBinding
import com.coheser.app.models.UserRegisterModel
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.applyPhoneNoValidation
import com.google.firebase.functions.FirebaseFunctions
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.LinkBuilder
import com.coheser.app.activitesfragments.WebviewActivity
import com.coheser.app.simpleclasses.Dialogs
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject

/**
 * Sign in with Phone number
 */
class PhoneFragment : Fragment() {

    lateinit var binding: FragmentAddPhoneBinding
    var fromWhere = ""
    var userRegisterModel: UserRegisterModel? = null
    var phoneNo = ""
    var links: MutableList<Link> = ArrayList()
    private lateinit var functions: FirebaseFunctions

    companion object {
        fun newInstance(fromWhere: String, userRegisterModel: UserRegisterModel?): PhoneFragment {
            val fragment = PhoneFragment()
            val args = Bundle()
            args.putSerializable("user_model", userRegisterModel)
            args.putString("fromWhere", fromWhere)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_phone, container, false)
        initControl()
        actionControl()
        return binding.root
    }

    private fun actionControl() {
        binding.phoneEdit.doAfterTextChanged {
            if (binding.phoneEdit.text?.length!! > 0) {
                hideError()
                binding.btnSendCode.isEnabled = true
                binding.btnSendCode.isClickable = true

            } else {
                binding.btnSendCode.isEnabled = false
                binding.btnSendCode.isClickable = false
            }
        }

        binding.btnSendCode.setOnClickListener(DebounceClickHandler {
            if(Constants.IS_DEMO_APP){
                Dialogs.showAlert(requireActivity(),"Alert","Registered with phone no is disabled in demo")
            }
            else{
                sendCodeOnPhoneNumber()
            }

        })

    }

    private fun SetupScreenData() {
        val link: Link = Link(
            binding.root.context.getString(R.string.terms_of_use)
        )
        link.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        link.setTextColorOfHighlightedLink(
            ContextCompat.getColor(
                binding.root.context,
                R.color.appColor
            )
        )
        link.setUnderlined(true)
        link.setBold(false)
        link.setHighlightAlpha(.20f)
        link.setOnClickListener(object : Link.OnClickListener {
            override fun onClick(clickedText: String) {
                openWebUrl(
                    binding.root.context.getString(R.string.terms_of_use),
                    Constants.terms_conditions
                )
            }
        })
        val link2: Link = Link(
            binding.root.context.getString(R.string.privacy_policy)
        )
        link2.setTextColor(ContextCompat.getColor((context)!!, R.color.black))
        link2.setTextColorOfHighlightedLink(
            ContextCompat.getColor(
                binding.root.context,
                R.color.appColor
            )
        )
        link2.setUnderlined(true)
        link2.setBold(false)
        link2.setHighlightAlpha(.20f)
        link2.setOnClickListener(object : Link.OnClickListener {
            override fun onClick(clickedText: String) {
                openWebUrl(
                    binding.root.context.getString(R.string.privacy_policy),
                    Constants.privacy_policy
                )
            }
        })
        links.add(link)
        links.add(link2)
        val sequence: CharSequence? = LinkBuilder.from(
            binding.root.context,
            binding.loginTermsConditionTxt.text.toString()
        )
            .addLinks(links)
            .build()
        binding.loginTermsConditionTxt.text = sequence
        binding.loginTermsConditionTxt.movementMethod = LinkMovementMethod.getInstance()
    }

    fun openWebUrl(title: String, url: String) {
        val intent = Intent(binding.root.context, WebviewActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            binding.root.context,
            R.anim.in_from_right,
            R.anim.out_to_left
        )
        startActivity(intent, options.toBundle())
    }

    // this will initialize all the views
    private fun initControl() {
        functions = FirebaseFunctions.getInstance()
        arguments?.let {
            fromWhere = it.getString("fromWhere", "")
            userRegisterModel = it.getSerializable("user_model") as UserRegisterModel?
        }

        binding.ccp.setCountryForNameCode(binding.ccp.defaultCountryNameCode)
        binding.ccp.registerPhoneNumberTextView(binding.phoneEdit)
        binding.ccp.phoneNumber
        SetupScreenData()
    }

    private fun sendCodeOnPhoneNumber() {
        if (checkValidation()) {

            if (binding.phoneEdit.text.toString().length < 7) {
                showError(binding.root.context.getString(R.string.invalid_phone_number))
                return
            }
            phoneNo = binding.phoneEdit.text.toString()
            phoneNo = applyPhoneNoValidation(phoneNo, binding.ccp.selectedCountryCodeWithPlus)

            callApiOtp()
        }
    }

    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data = result.data
            data?.let {
                userRegisterModel = it.getSerializableExtra("model") as UserRegisterModel
                Log.d(Constants.tag, "user call back:  ${userRegisterModel!!.company_name}")
                val nextF = PhoneOtpFragment.newInstance(phoneNo, userRegisterModel)
                val transaction: FragmentTransaction =
                    requireActivity().supportFragmentManager.beginTransaction()
                transaction.setCustomAnimations(
                    R.anim.in_from_right,
                    R.anim.out_to_left,
                    R.anim.in_from_left,
                    R.anim.out_to_right
                )
                transaction.addToBackStack(null)
                transaction.replace(R.id.sign_up_fragment, nextF).commit()
            }
        }
    }

    fun checkValidation(): Boolean {

        if (binding.phoneEdit.text.toString().isEmpty()) {
            showError(binding.root.context.getString(R.string.please_enter_phone_number))
            return false
        } else {
            hideError()
            return true
        }

    }

    /**
     *  Register the phone number
     */
    private fun callApiOtp() {
        val parameters = JSONObject()
        try {
            parameters.put("phone", phoneNo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Functions.showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.checkPhoneNo, parameters, Functions.getHeadersWithOutLogin(
                activity
            )
        ) { resp ->
            Functions.checkStatus(activity, resp)
            Functions.cancelLoader()
            parseLoginData(resp)
        }
    }

    // if api return the ok responce then open the get opt screen
    fun parseLoginData(loginData: String?) {
        try {
            val jsonObject = JSONObject(loginData)
            val code = jsonObject.optString("code")
            if (code == "200") {
                // not registered on system
                userRegisterModel?.phoneNo = phoneNo
                if (fromWhere == AccountUtils.typeLogin) {
                    showError(jsonObject.optString("msg"))
                    println("mufasa phone not registered")
                } else {
                    val nextF = PhoneOtpFragment.newInstance(phoneNo, userRegisterModel)
                    val transaction: FragmentTransaction =
                        requireActivity().supportFragmentManager.beginTransaction()
                    transaction.setCustomAnimations(
                        R.anim.in_from_right,
                        R.anim.out_to_left,
                        R.anim.in_from_left,
                        R.anim.out_to_right
                    )
                    transaction.addToBackStack(null)
                    transaction.replace(R.id.sign_up_fragment, nextF).commit()
                }
            } else if (code == "201") {
                // aldready registered on the system
                if (fromWhere == AccountUtils.typeLogin) {
                    val nextF = PhoneOtpFragment.newInstance(phoneNo, userRegisterModel)
                    val transaction: FragmentTransaction =
                        requireActivity().supportFragmentManager.beginTransaction()
                    transaction.setCustomAnimations(
                        R.anim.in_from_right,
                        R.anim.out_to_left,
                        R.anim.in_from_left,
                        R.anim.out_to_right
                    )
                    transaction.addToBackStack(null)
                    transaction.replace(R.id.sign_up_fragment, nextF).commit()
                } else {
                    showError(jsonObject.optString("msg"))
                    println("mufasa phone already registered")
                }
            } else {
                showError(jsonObject.optString("msg"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showError(error: String) {
        binding.errorMsgTxt.text = Constants.alertUniCode + error
        binding.errorMsgTxt.visibility = View.VISIBLE
    }

    fun hideError() {
        binding.errorMsgTxt.visibility = View.GONE

    }
}