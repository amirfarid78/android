package com.coheser.app.activitesfragments.accounts

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentFirstLastNameBinding
import com.coheser.app.models.UserRegisterModel
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setUpMultipleAccount
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.Functions.updateUserModel
import com.coheser.app.simpleclasses.Variables
import com.google.firebase.auth.FirebaseAuth
import com.coheser.app.activitesfragments.SplashActivity
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject

// This fragment will get the first last name from the users
class CreateFristLastNameFragment : Fragment() {

    lateinit var binding: FragmentFirstLastNameBinding
    var userRegisterModel: UserRegisterModel? = null
    var fromWhere = ""
    lateinit var mAuth: FirebaseAuth

    companion object {
        fun newInstance(
            fromWhere: String, userRegisterModel: UserRegisterModel?
        ): CreateFristLastNameFragment {
            val fragment = CreateFristLastNameFragment()
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
            DataBindingUtil.inflate(inflater, R.layout.fragment_first_last_name, container, false)
        mAuth = FirebaseAuth.getInstance()

        initControl()
        actionControl()
        return binding.root
    }

    private fun actionControl() {

        binding.goBack.setOnClickListener(DebounceClickHandler {
            activity?.onBackPressed()
        })
        binding.btnSignUp.setOnClickListener(DebounceClickHandler {
            if (checkValidation(true)) {
                userRegisterModel?.let {
                    callApiForEditProfile()
                }
            }
        })
    }

    // check the username validation here
    private fun checkValidation(showError: Boolean): Boolean {
        var allOk = true
        if (TextUtils.isEmpty(binding.firstnameEdit.text.toString())) {
            if (showError) {
                binding.firstnameEdit.error = getString(R.string.please_enter_first_name)
                binding.firstnameEdit.isFocusable = true
            }
            allOk = false
        }
        if (TextUtils.isEmpty(binding.lastnameEdit.text.toString())) {
            if (showError) {
                binding.lastnameEdit.error = getString(R.string.please_enter_last_name)
                binding.lastnameEdit.isFocusable = true
            }
            allOk = false
        }
        if (allOk) {
            hideError()
        }

        return allOk
    }

    private fun showError(error: String) {
        binding.errorMsgTxt.text = Constants.alertUniCode + error
        binding.errorMsgTxt.visibility = View.VISIBLE

    }

    private fun hideError() {
        binding.errorMsgTxt.visibility = View.GONE

    }

    private fun initControl() {
        arguments?.let {
            fromWhere = it.getString("fromWhere", "")
            userRegisterModel = it.getSerializable("user_model") as UserRegisterModel?
        }

        val firstnameFilters = arrayOfNulls<InputFilter>(1)
        firstnameFilters[0] = LengthFilter(Constants.USERNAME_CHAR_LIMIT)
        binding.firstnameEdit.filters = firstnameFilters

        val lastnameFilters = arrayOfNulls<InputFilter>(1)
        lastnameFilters[0] = LengthFilter(Constants.USERNAME_CHAR_LIMIT)
        binding.lastnameEdit.filters = lastnameFilters

        userRegisterModel?.let { model ->
            if (model.referalCode.isNotEmpty()) {
                binding.referalEdit.setText(model.referalCode)
            }
        }
    }

    // this will update the latest info of user in database
    private fun callApiForEditProfile() {
        showLoader(activity, false, false)

        val uname: String =
            userRegisterModel?.username ?: getSharedPreference(binding.root.context).getString(
                Variables.U_NAME, ""
            )!!
        val parameters = JSONObject()
        try {
            getSharedPreference(context).edit()
                .putString(Variables.AUTH_TOKEN, userRegisterModel?.firebaseUID ?: "0").apply()

            parameters.put("username", uname)
            parameters.put("dob", userRegisterModel!!.dateOfBirth)
            parameters.put("first_name", binding.firstnameEdit.text.toString())
            parameters.put("last_name", binding.lastnameEdit.text.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val header = getHeaders(activity)
        if (!header.containsKey("Auth-Token")) {
            Log.d("mufasa", "callApiForEditProfile: Auth-Token not found")
            header["Auth-Token"] = userRegisterModel?.firebaseUID ?: "null"
        }
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.editProfile, parameters, header
        ) { resp ->
            checkStatus(activity, resp)
            cancelLoader()
            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                val msg = response.optJSONObject("msg")
                if (code == "200") {
                    val userDetailModel = getUserDataModel(msg.optJSONObject("User"))
                    updateUserModel(userDetailModel)

                    setUpMultipleAccount(binding.root.context,userDetailModel)
                    Variables.reloadMyVideos = true
                    Variables.reloadMyVideosInner = true
                    Variables.reloadMyLikesInner = true
                    Variables.reloadMyNotification = true

                    val editor = getSharedPreference(binding.root.context).edit()

                    var u_name = userDetailModel.username
                    if (!u_name!!.contains("@")) u_name = "@$u_name"

                    editor.putString(Variables.U_NAME, u_name)
                    editor.putString(Variables.F_NAME, userDetailModel.first_name)
                    editor.putString(Variables.L_NAME, userDetailModel.last_name)
                    editor.commit()
                    navigateMainScreen()
                } else {
                    showToast(
                        binding.root.context, response.optString("msg")
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateMainScreen() {
        val intent = Intent(context, SplashActivity::class.java)
        intent.putExtra("openMain",true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

    }
}