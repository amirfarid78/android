package com.coheser.app.activitesfragments.accounts

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.ActivityLoginBinding
import com.coheser.app.models.UserRegisterModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.PrintHashKey
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeadersWithOutLogin
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Functions.removeSpecialChar
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.setUpMultipleAccount
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Variables
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.coheser.app.activitesfragments.SplashActivity
import com.coheser.app.activitesfragments.WebviewActivity
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject

class LoginActivity : AppCompatLocaleActivity() {

    var userRegisterModel: UserRegisterModel? = null
    lateinit var binding: ActivityLoginBinding

    //google Implimentation
    var mGoogleSignInClient: GoogleSignInClient? = null
    var mBackPressed: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@LoginActivity).getString(
                Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        setTheme(R.style.TransparentActivityTheme)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        initControl()
        actionControl()
    }

    private fun actionControl() {
        binding.phoneEmailLoginBtn.setOnClickListener(DebounceClickHandler {
            hideError()
            this.navigateToEmailPhoneLogin()

        })
        binding.signUp.setOnClickListener(DebounceClickHandler {
            hideError()
            setTheme(R.style.whiteStatus)
            navigateToRegisterEmailPhone()
        })

        binding.googleBtn.setOnClickListener(DebounceClickHandler {
            hideError()
            signInWithGmail()
        })

        binding.goBack.setOnClickListener(DebounceClickHandler {
            binding.topView.visibility = View.GONE
            finish()
        })
        binding.topView.setOnClickListener(DebounceClickHandler {
            finish()
        })
    }

    private fun navigateToEmailPhoneLogin() {
        val nextF = EmailPhoneFragment.newInstance(AccountUtils.typeLogin, false, userRegisterModel)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right
        )
        transaction.addToBackStack(null)
        transaction.replace(R.id.login_f, nextF, AccountUtils.typeLogin).addToBackStack(null)
            .commit()
    }

    private fun initControl() {
        userRegisterModel = UserRegisterModel()

        binding.loginTitleTxt.text =
            "${getString(R.string.log_in)} to ${getString(R.string.app_name)}"
        val ss =
            SpannableString(getString(R.string.by_signing_up_you_confirm_that_you_agree_to_our_n_terms_of_use_and_have_read_and_understood_n_our_privacy_policy))
        val color = ContextCompat.getColor(this, R.color.blueColor)

        setClickableSpan(ss, getString(R.string.privacy_policy), color) {
            openWebUrl(getString(R.string.privacy_policy), Constants.privacy_policy)
        }

        setClickableSpan(ss, getString(R.string.terms_of_use), color) {
            openWebUrl(getString(R.string.terms_of_use), Constants.terms_conditions)
        }

        binding.loginTermsConditionTxt.text = ss
        binding.loginTermsConditionTxt.isClickable = true
        binding.loginTermsConditionTxt.movementMethod = LinkMovementMethod.getInstance()
        PrintHashKey(this)
    }

    private fun setClickableSpan(
        spannableString: SpannableString, textToSpan: String, color: Int, onClickAction: () -> Unit
    ) {
        val startIndex = spannableString.toString().indexOf(textToSpan)
        val endIndex = startIndex + textToSpan.length
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                onClickAction()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        val colorSpan = ForegroundColorSpan(color)
        val underlineSpan = UnderlineSpan()
        spannableString.setSpan(
            clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(
            underlineSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    private fun openWebUrl(title: String, url: String) {
        val intent = Intent(binding.root.context, WebviewActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            binding.root.context, R.anim.in_from_right, R.anim.out_to_left
        )
        startActivity(intent, options.toBundle())
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 200
        binding.topView.startAnimation(anim)
        binding.topView.visibility = View.VISIBLE
    }


    fun signInWithGmail() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id)).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        try {
            mGoogleSignInClient?.signOut()
        } catch (e: Exception) {
        }
        val account = GoogleSignIn.getLastSignedInAccount(this)
        // if user is already signed in then we will get the account details and use it to sign in to firebase
        if (account != null) {
            val id = "" + account.id
            val fname = "" + account.givenName
            val lname = "" + account.familyName
            val email = "" + account.email
            val auth_tokon = "" + account.idToken
            val image = "" + account.photoUrl
            userRegisterModel = UserRegisterModel()
            userRegisterModel?.fname = removeSpecialChar(fname)
            userRegisterModel?.email = email
            userRegisterModel?.lname = removeSpecialChar(lname)
            userRegisterModel?.socailId = id
            userRegisterModel?.googleTokon = auth_tokon
            userRegisterModel?.picture = image
            userRegisterModel?.socailType = "google"
            if (intent.hasExtra("referalCode")) {
                userRegisterModel?.referalCode = intent?.getStringExtra("referalCode").toString()
            }
            loginWithGoogleFirebase(userRegisterModel!!)
        } else { // if user is not signed in then we will open the google sign in intent
            mGoogleSignInClient?.signInIntent?.let {
                resultCallbackForGoogle.launch(it)
            } ?: run {
                Log.e(TAG, "signInWithGmail: mGoogleSignInClient is null")
            }
        }
    }

    /**
     * Result callback for google sign in intent from existing google account.
     */
    private var resultCallbackForGoogle = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
    }

    /**
     * Handle the result of google sign in if the task is successful and we get the account details.
     * The google account is first used to sign in to firebase.
     */
    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(
                ApiException::class.java
            )
            if (account != null) {
                val id = "" + account.id
                val fname = "" + account.givenName
                val lname = "" + account.familyName
                val auth_token = "" + account.idToken
                val email = "" + account.email
                val image = "" + account.photoUrl
                printLog(Constants.tag, "GoogleToken: $auth_token")
                // if we do not get the picture of user then we will use default profile picture
                userRegisterModel = UserRegisterModel()
                userRegisterModel?.fname = fname
                userRegisterModel?.email = email
                userRegisterModel?.lname = lname
                userRegisterModel?.socailId = id
                userRegisterModel?.socailType = "google"
                userRegisterModel?.picture = image
                userRegisterModel?.googleTokon = account.idToken.toString()
                if (intent.hasExtra("referalCode")) {
                    userRegisterModel?.referalCode =
                        intent?.getStringExtra("referalCode").toString()
                }
                loginWithGoogleFirebase(userRegisterModel!!)
            }
        } catch (e: ApiException) {
            printLog(Constants.tag, "signInResult:failed code=" + e.statusCode)
        }
    }

    /**
     * Sign in with google account to firebase.
     */
    private fun loginWithGoogleFirebase(model: UserRegisterModel) {
        showLoader(this,false,false)
        val firebaseCredential = GoogleAuthProvider.getCredential(model.googleTokon, null)
        FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    userRegisterModel?.firebaseUID = FirebaseAuth.getInstance().uid.toString()
                    callApiUserDetails(userRegisterModel?.firebaseUID!!)
                } else {
                    cancelLoader()
                    // If sign in fails, navigates to the [GetStartedActivity] with the user model.
                    openGetStartActivity(AccountUtils.typeSocial)
                }
            }
    }

    private fun navigateToRegisterEmailPhone() {
        var nextF = EmailPhoneFragment.newInstance(AccountUtils.typeSignUp, true, userRegisterModel)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.in_from_right,
            R.anim.out_to_left,
            R.anim.in_from_left,
            R.anim.out_to_right
        )
        transaction.addToBackStack(null)
        transaction.replace(R.id.login_f, nextF).commit()
    }

    fun openGetStartActivity(type: String) {
        getSharedPreference(this).edit().putString(Variables.U_SOCIAL,type).apply()

        val DOBF = DateOfBirthFragment()
        val transaction = supportFragmentManager.beginTransaction()
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

    private fun callApiUserDetails(authTokon: String) {

        val parameters = JSONObject()
        try {
            parameters.put("auth_token", authTokon)

        } catch (e: Exception) {
            e.printStackTrace()
        }


         VolleyRequest.JsonPostRequest(
            this,
            ApiLinks.showUserDetail,
            parameters,
            Functions.getHeadersWithAuthTokon(authTokon)
        ) { resp ->
            checkStatus(this, resp)
            cancelLoader()
            parseLoginData(resp)
        }
    }

    private fun parseLoginData(loginData: String?) {
        try {
            val jsonObject = JSONObject(loginData)
            val code = jsonObject.optString("code")
            if (code == "200") {
                // registered user
                val jsonObj = jsonObject.getJSONObject("msg")
                val userDetailModel = getUserDataModel(jsonObj.optJSONObject("User"))
                AccountUtils.setUpMultipleAccount(this, userDetailModel)
                Variables.reloadMyVideos = true
                Variables.reloadMyVideosInner = true
                Variables.reloadMyLikesInner = true
                Variables.reloadMyNotification = true

                getSharedPreference(this).edit().putString(Variables.U_SOCIAL,AccountUtils.typeSocial).apply()


                val intent = Intent(this@LoginActivity, SplashActivity::class.java)
                intent.putExtra("openMain",true)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)

            } else if (code == "201" && !jsonObject.optString("msg")
                    .contains("have been blocked")
            ) {
                callApiForSignup(userRegisterModel!!)

            } else {
                Toast.makeText(this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            this, ApiLinks.registerUser, parameters, getHeadersWithOutLogin(
                this
            )
        ) { resp ->
            checkStatus(this, resp)

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

                openGetStartActivity(AccountUtils.typeSocial)

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


    override fun onBackPressed() {
        val count = this.supportFragmentManager.backStackEntryCount
        if (count == 0) {
            if (mBackPressed + 2000 > System.currentTimeMillis()) {
               finish()
            } else {
                binding.topView.visibility = View.GONE
                finish()
                overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom)
            }
        } else {
            finish()

        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}