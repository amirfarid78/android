package com.coheser.app.activitesfragments.accounts

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
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
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentEmailRegBinding
import com.coheser.app.models.UserRegisterModel
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeadersWithAuthTokon
import com.coheser.app.simpleclasses.Functions.getHeadersWithOutLogin
import com.coheser.app.simpleclasses.Functions.isValidEmail
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Functions.setUpMultipleAccount
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Variables
import com.google.firebase.auth.FirebaseAuth
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.LinkBuilder
import com.coheser.app.activitesfragments.SplashActivity
import com.coheser.app.activitesfragments.WebviewActivity
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.Callback
import org.json.JSONObject

/**
 * Signs up/in Email
 */
class EmailFragment : Fragment() {

    lateinit var binding: FragmentEmailRegBinding
    var fromWhere: String = ""
    var userRegisterModel: UserRegisterModel? = null
    var links: MutableList<Link> = ArrayList()
    private var passwordCheck = true

    companion object {
        fun newInstance(fromWhere: String, userRegisterModel: UserRegisterModel?): EmailFragment {
            val fragment = EmailFragment()
            val args = Bundle()
            args.putSerializable("user_model", userRegisterModel)
            args.putString("fromWhere", fromWhere)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_email_reg, container, false)
        initControl()
        actionControl()
        return binding.root
    }

    private fun actionControl() {
        binding.emailEdit.doAfterTextChanged {
            checkValidation(false)

            binding.emailFieldLayout.error = null
            binding.emailFieldLayout.isErrorEnabled = false
            userRegisterModel?.email = binding.emailEdit.text.toString()
        }



        binding.passwordEt.doAfterTextChanged {
            checkValidation(false)
            binding.passwordFieldLayout.error = null
            binding.passwordFieldLayout.isErrorEnabled = false
        }




        binding.btnNext.setOnClickListener(DebounceClickHandler {
            moveToNextStep()

        })
        binding.forgotPassBtn.setOnClickListener(DebounceClickHandler {
            moveToForgotScreen()
        })
    }

    fun enableBtn() {
        binding.btnNext.isEnabled = true
        binding.btnNext.isClickable = true
    }

    fun disableBtn() {
        binding.btnNext.isEnabled = false
        binding.btnNext.isClickable = false
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

    private fun moveToForgotScreen() {
        val movingIntent = Intent(binding.root.context, ForgotPassActivity::class.java)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            binding.root.context, R.anim.in_from_right, R.anim.out_to_left
        )
        startActivity(movingIntent, options.toBundle())
    }

    private fun moveToNextStep() {
        if (checkValidation(true)) {
            hideError()
            if (fromWhere.equals(AccountUtils.typeLogin)) {
                printLog(Constants.tag, "next button Login")
                createOrLoginInFirebase()
            } else {
                Log.d(Constants.tag, "move to next")
                checkIsEmailAlreadySigned()
            }
        }
    }

    private fun initControl() {
        arguments?.let {
            fromWhere = it.getString("fromWhere", "")
            userRegisterModel = it.getSerializable("user_model") as UserRegisterModel?
        }
        if (fromWhere == AccountUtils.typeLogin) {
            binding.forgotPassBtn.visibility = View.VISIBLE
            binding.passwordFieldLayout.visibility = View.VISIBLE
            binding.btnNext.text = binding.root.context.getString(R.string.login)
        } else {
            if (userRegisterModel != null) {
                userRegisterModel?.let { model ->
                    binding.emailEdit.setText(model.email)
                }
            }
        }

        binding.emailEdit.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        })
        binding.passwordEt.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        SetupScreenData()
    }

    private fun SetupScreenData() {
        val link = Link(binding.root.context.getString(R.string.terms_of_use))
        link.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
        link.setTextColorOfHighlightedLink(
            ContextCompat.getColor(
                binding.root.context, R.color.appColor
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
        val link2 = Link(binding.root.context.getString(R.string.privacy_policy))
        link2.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        link2.setTextColorOfHighlightedLink(
            ContextCompat.getColor(
                requireContext(), R.color.appColor
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
        val sequence =
            LinkBuilder.from(requireContext(), binding.loginTermsConditionTxt.text.toString())
                .addLinks(links).build()
        binding.loginTermsConditionTxt.text = sequence
        binding.loginTermsConditionTxt.movementMethod = LinkMovementMethod.getInstance()
    }

    fun openWebUrl(title: String, url: String) {
        val intent = Intent(binding.root.context, WebviewActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            binding.root.context, R.anim.in_from_right, R.anim.out_to_left
        )
        startActivity(intent, options.toBundle())
    }

    private fun checkIsEmailAlreadySigned() {
        val parameters = JSONObject()
        try {
            parameters.put("email", binding.emailEdit.text.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(activity,
            ApiLinks.checkEmail,
            parameters,
            getHeadersWithOutLogin(
                activity
            ),
            Callback { resp ->
                checkStatus(activity, resp)
                cancelLoader()
                parseCheckEmailData(resp)
            })
    }

    fun parseCheckEmailData(loginData: String?) {
        try {
            val jsonObject = JSONObject(loginData)
            val code = jsonObject.optString("code")
            if ((code == "200")) {

                    openCreatePasswordF()


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

    private fun openCreatePasswordF() {
        userRegisterModel?.email = binding.emailEdit.text.toString()
        userRegisterModel?.firebaseUID=""

        val nextF = CreatePasswordFragment.newInstance("fromEmail", userRegisterModel)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right
        )
        transaction.addToBackStack(null)
        transaction.replace(R.id.sign_up_fragment, nextF).commit()
    }

    fun callApiUserDetails(authTokon: String) {
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
            getHeadersWithAuthTokon(authTokon)
        ) { resp ->
            checkStatus(activity, resp)
            cancelLoader()
            parseLoginData(resp)
        }
    }

    fun parseLoginData(loginData: String?) {
        try {
            val jsonObject = JSONObject(loginData)
            val code = jsonObject.optString("code")
            if ((code == "200")) {
                val jsonObj = jsonObject.getJSONObject("msg")
                val userDetailModel = getUserDataModel(jsonObj.optJSONObject("User"))

                setUpMultipleAccount(requireContext(), userDetailModel)
                Variables.reloadMyVideos = true
                Variables.reloadMyVideosInner = true
                Variables.reloadMyLikesInner = true
                Variables.reloadMyNotification = true


                val intent = Intent(context, SplashActivity::class.java)
                intent.putExtra("openMain",true)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                binding.root.context.startActivity(intent)

            } else {
                showError("The account do not exist")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // t
    // his will check the validations like none of the field can be the empty
    fun checkValidation(showError: Boolean): Boolean {
        val st_email = binding.emailEdit.text.toString()
        val password = binding.passwordEt.text.toString()
        printLog(Constants.tag, "next button st_email")
        if (TextUtils.isEmpty(st_email)) {
            disableBtn()
            if (showError) {
                binding.emailFieldLayout.error =
                    Constants.alertUniCode + binding.root.context.getString(R.string.please_enter_email)
            }
            return false
        }
        if (!isValidEmail(st_email)) {
            disableBtn()

            if (showError) {
                binding.emailFieldLayout.error =
                    Constants.alertUniCode + binding.root.context.getString(R.string.please_enter_valid_email)
            }
            return false
        }

        if ((fromWhere == AccountUtils.typeLogin)) {
            if (TextUtils.isEmpty(password)) {
                disableBtn()
                if (showError) {
                    binding.passwordFieldLayout.error =
                        binding.root.context.getString(R.string.please_enter_password)
                }
                return false
            }
        }

        enableBtn()

        return true
    }

    fun createOrLoginInFirebase() {

        showLoader(requireActivity(), false, false)
        val email = binding.emailEdit.text.toString()
        val password = binding.passwordEt.text.toString()
        printLog(Constants.tag, "$email $password")
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user?.isEmailVerified == true) {

                        callApiUserDetails(user.uid)
                    } else {
                        cancelLoader()
                        showError("We've already sent you an email. Click the link to verify your account.")
                    }

                } else {
                    cancelLoader()
                    showError("The email or password you entered is incorrect. Please try again.")
                }
            }
    }

}