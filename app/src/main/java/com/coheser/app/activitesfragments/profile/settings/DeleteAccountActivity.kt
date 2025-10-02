package com.coheser.app.activitesfragments.profile.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.activitesfragments.accounts.AccountUtils.removeMultipleAccount
import com.coheser.app.activitesfragments.accounts.AccountUtils.setUpExistingAccountLogin
import com.coheser.app.activitesfragments.accounts.LoginActivity
import com.coheser.app.activitesfragments.accounts.ManageAccountsFragment
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityDeleteAccountBinding
import com.coheser.app.mainmenu.MainMenuActivity
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Dialogs.showDoubleButtonAlert
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.hideSoftKeyboard
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.DeleteAccountViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import io.paperdb.Paper
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeleteAccountActivity : AppCompatLocaleActivity(), View.OnClickListener {

    lateinit var binding:ActivityDeleteAccountBinding

    private val viewModel: DeleteAccountViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@DeleteAccountActivity).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding=DataBindingUtil.setContentView(this,R.layout.activity_delete_account)

        binding.viewModel=viewModel
        binding.lifecycleOwner = this


        InitControl()
        setObserver()

    }



    private fun InitControl() {
        binding.tvDeleteTitle.setText("""${getString(R.string.delete_account)}
    ${getSharedPreference(this@DeleteAccountActivity).getString(Variables.U_NAME, "")}?
    """.trimIndent()
        )
        binding.goBack.setOnClickListener(this)
        binding.deleteAccount.setOnClickListener(this)
    }


    fun setObserver(){
        viewModel.deleteUserLiveData.observe(this,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {
                            removePreferenceData()

                        }
                    }
                }
                is ApiResponce.Error ->{
                    showToast(this@DeleteAccountActivity, it.message)

                }
                else -> {

                }
            }
        })

    }




    override fun onClick(view: View) {
        when (view.id) {
            R.id.goBack -> {
                super@DeleteAccountActivity.onBackPressed()
            }

            R.id.deleteAccount -> {
                logoutProceed()
            }
        }
    }

    private fun logoutProceed() {
        if (Paper.book(Variables.MultiAccountKey).allKeys.size > 1) {
            showDoubleButtonAlert(
                this@DeleteAccountActivity,
                getString(R.string.are_you_sure_to_delete_account),
                "",
                getString(R.string.delete_account),
                getString(R.string.switch_account), true
            ) { bundle ->
                if (bundle.getBoolean("isShow", false)) {
                    openManageMultipleAccounts()
                } else {
                    viewModel.deleteUserAccount()
                }
            }
        }
        else {
            showDoubleButtonAlert(
                this@DeleteAccountActivity,
                getString(R.string.are_you_sure_to_delete_account),
                "",
                getString(R.string.cancel_),
                getString(R.string.delete_account), true
            ) { bundle ->
                if (bundle.getBoolean("isShow", false)) {
                    viewModel.deleteUserAccount()
                }
            }
        }
    }

    private fun removePreferenceData() {
        Paper.book(Variables.PrivacySetting).destroy()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(this@DeleteAccountActivity, gso)
        googleSignInClient.signOut()
        removeMultipleAccount(this@DeleteAccountActivity)
        val editor = getSharedPreference(this@DeleteAccountActivity).edit()
        editor.clear()
        editor.commit()
        setUpExistingAccountLogin(this@DeleteAccountActivity)
        val intent = Intent(this@DeleteAccountActivity, MainMenuActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun openManageMultipleAccounts() {
        val f = ManageAccountsFragment { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                hideSoftKeyboard(this@DeleteAccountActivity)
                val intent = Intent(this@DeleteAccountActivity, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
            }
        }
        f.show(supportFragmentManager, "")
    }
}