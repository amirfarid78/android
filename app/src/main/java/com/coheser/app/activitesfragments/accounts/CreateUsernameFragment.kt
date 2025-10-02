package com.coheser.app.activitesfragments.accounts

import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentUserNameBinding
import com.coheser.app.models.UserRegisterModel
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.DelayedTextWatcher
import com.coheser.app.simpleclasses.Functions
import com.google.firebase.auth.FirebaseAuth
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.Callback
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

// This fragment will get the username from the users
class CreateUsernameFragment : Fragment() {

    lateinit var binding: FragmentUserNameBinding
    var userRegisterModel: UserRegisterModel? = null
    var fromWhere = ""
    var mAuth: FirebaseAuth? = null

    companion object {
        // use this method for reference
        fun newInstance(
            fromWhere: String,
            userRegisterModel: UserRegisterModel?
        ): CreateUsernameFragment {
            val fragment = CreateUsernameFragment()
            val args = Bundle()
            args.putString("fromWhere", fromWhere)
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_name, container, false)
        mAuth = FirebaseAuth.getInstance()

        initControl()
        actionControl()
        return binding.root
    }

    private fun actionControl() {

        binding.goBack.setOnClickListener(DebounceClickHandler {
            activity?.onBackPressed()
        })
        binding.btnConfirm.setOnClickListener(DebounceClickHandler {
            if (checkValidation(true)) {
                userRegisterModel?.let {
                    it.username = binding.usernameEdit.text.toString()

                    moveToFirstLastNameScreen()

                }
            }
        })

        binding.usernameEdit.addTextChangedListener(
            DelayedTextWatcher(delayMillis = 200) { text ->
                binding.usernameCountTxt.text =
                    text.length.toString() + "/" + Constants.USERNAME_CHAR_LIMIT
                checkValidation(false)
                hideError()
                if (text.length > 0) {
                    checkUserName()
                }
            }
        )
    }

    private fun moveToFirstLastNameScreen() {
        val nextf = CreateFristLastNameFragment.newInstance(fromWhere, userRegisterModel)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.in_from_right,
            R.anim.out_to_left,
            R.anim.in_from_left,
            R.anim.out_to_right
        )
        transaction.addToBackStack(null)
        transaction.replace(R.id.username_fragment, nextf).commit()
    }

    private fun initControl() {
        arguments?.let {
            fromWhere = it.getString("fromWhere", "")
            userRegisterModel = it.getSerializable("user_model") as UserRegisterModel?
        }

        val usernameFilters = arrayOfNulls<InputFilter>(1)
        usernameFilters[0] = LengthFilter(Constants.USERNAME_CHAR_LIMIT)
        binding.usernameEdit.filters = usernameFilters

    }

    var isUserNameOk: Boolean = false
    private fun checkUserName() {

        val jsonObject = JSONObject().apply {
            put("username", binding.usernameEdit.text.toString())
        }

        isUserNameOk = false
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.checkUsername,
            jsonObject,
            Functions.getHeaders(binding.root.context),
            object : Callback {
                override fun onResponce(result: String?) {
                    result?.let {
                        try {
                            val jsonResponse = JSONObject(it)
                            val code = jsonResponse.optString("code").toInt()
                            if (code == 200) {
                                isUserNameOk = true
                                hideError()
                            } else {
                                isUserNameOk = false
                                showError("This username isn't available")
                                binding.usernameEdit.requestFocus()
                            }
                        } catch (e: JSONException) {
                            Functions.cancelLoader()
                            e.printStackTrace()
                        }
                    }
                }
            }
        )
    }

    // check the username validation here
    fun checkValidation(showError: Boolean): Boolean {
        var allOk = true
        val uname = binding.usernameEdit.text.toString()
        if (TextUtils.isEmpty(uname)) {
            if (showError) {
                binding.usernameEdit.error = getString(R.string.username_cant_empty)
                binding.usernameEdit.isFocusable = true
            }
            allOk = false
        }

        if (uname.length < 4 || uname.length > 14) {
            if (showError) {
                binding.usernameEdit.error = getString(R.string.username_length_between_valid)
                binding.usernameEdit.isFocusable = true
            }
            allOk = false
        }
        if (!isUserNameOk) {
            if (showError) {
                binding.usernameEdit.error = getString(R.string.username_length_between_valid)
                binding.usernameEdit.isFocusable = true
            }
            allOk = false
        }
        if (!UserNameTwoCaseValidate(uname)) {
            if (showError) {
                binding.usernameEdit.error = getString(R.string.username_must_contain_alphabet)
                binding.usernameEdit.isFocusable = true
            }
            allOk = false
        }
        if (allOk) {
            hideError()
            binding.btnConfirm.isEnabled = true
            binding.btnConfirm.isClickable = true
        } else {
            binding.btnConfirm.isEnabled = false
            binding.btnConfirm.isClickable = false
        }

        return allOk
    }

    private fun UserNameTwoCaseValidate(name: String): Boolean {
        val let_p =
            Pattern.compile("[a-z]", Pattern.CASE_INSENSITIVE)
        val let_m = let_p.matcher(name)
        return let_m.find()
    }

    fun showError(error: String) {
        binding.errorMsgTxt.text = Constants.alertUniCode + error
        binding.errorMsgTxt.visibility = View.VISIBLE

    }

    fun hideError() {
        binding.errorMsgTxt.visibility = View.GONE
    }

}