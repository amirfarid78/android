package com.coheser.app.activitesfragments.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.WebviewActivity
import com.coheser.app.activitesfragments.accounts.AccountUtils
import com.coheser.app.activitesfragments.accounts.LoginActivity
import com.coheser.app.activitesfragments.accounts.ManageAccountsFragment
import com.coheser.app.activitesfragments.location.AddAddressActivity
import com.coheser.app.activitesfragments.payment.PaymentFragment
import com.coheser.app.activitesfragments.profile.settings.AppLanguageChangeActivity
import com.coheser.app.activitesfragments.profile.settings.AppSpaceClearActivity
import com.coheser.app.activitesfragments.profile.settings.AppThemActivity
import com.coheser.app.activitesfragments.profile.settings.BlockUserListActivity
import com.coheser.app.activitesfragments.profile.settings.CreatorToolsActivity
import com.coheser.app.activitesfragments.profile.settings.ManageProfileActivity
import com.coheser.app.activitesfragments.profile.settings.PrivacyPolicySettingActivity
import com.coheser.app.activitesfragments.profile.settings.ProfileVarificationActivity
import com.coheser.app.activitesfragments.profile.settings.PushNotificationSettingActivity
import com.coheser.app.activitesfragments.profile.settings.QrCodeProfileActivity
import com.coheser.app.activitesfragments.profile.settings.WalletPaymentA
import com.coheser.app.activitesfragments.shoping.HistoryA
import com.coheser.app.activitesfragments.videorecording.DraftVideosActivity
import com.coheser.app.activitesfragments.walletandwithdraw.MyWallet
import com.coheser.app.databinding.ActivitySettingAndPrivacyBinding
import com.coheser.app.mainmenu.MainMenuActivity
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Dialogs
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.hideSoftKeyboard
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Variables
import io.paperdb.Paper

class SettingAndPrivacyActivity : AppCompatLocaleActivity(), View.OnClickListener {

    lateinit var binding:ActivitySettingAndPrivacyBinding

    var resultDarkModeCallback = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
                val intent = Intent(this@SettingAndPrivacyActivity, MainMenuActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@SettingAndPrivacyActivity).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting_and_privacy)
        InitControl()
    }

    private fun InitControl() {
        binding.backBtn.setOnClickListener(this)
        binding.tabManageAccount.setOnClickListener(this)
        binding.tabPrivacy.setOnClickListener(this)
        binding.tabCreatorTools.setOnClickListener(this)
        binding.taborders.setOnClickListener(this)
        binding.tabFavourite.setOnClickListener(this)
        binding.tabBalance.setOnClickListener(this)
        binding.tabQr.setOnClickListener(this)
        binding.tabShareProfile.setOnClickListener(this)
        binding.tabPushNotificaiton.setOnClickListener(this)
        binding.tabApplanguage.setOnClickListener(this)
        binding.tabFreeSpace.setOnClickListener(this)
        binding.tabTermsOfService.setOnClickListener(this)
        binding.tabPrivacyPolicy.setOnClickListener(this)
        binding.tabSwitchAccount.setOnClickListener(this)
        binding.tabLogout.setOnClickListener(this)
        binding.tabPayoutSetting.setOnClickListener(this)
        binding.tabBlockUser.setOnClickListener(this)
        binding.tabDraftVideo.setOnClickListener(this)
        binding.tabDarkmode.setOnClickListener(this)
        binding.referalLayout.setOnClickListener(this)
        binding.tabaddress.setOnClickListener(this)
        binding.tabdeliveryPay.setOnClickListener(this)
        binding.tabVerifyProfile.setOnClickListener(this)
        setUpScreenData()
    }

    private fun setUpScreenData() {
        try {
            if (getSharedPreference(this@SettingAndPrivacyActivity).getInt(Variables.IS_VERIFIED, 0) == 1) {
                binding.tabVerifyProfile!!.visibility = View.GONE
            } else {
                binding.tabVerifyProfile!!.visibility = View.VISIBLE
            }

        }catch (e:Exception){

        }

        binding.tvreferal!!.text = getSharedPreference(this)
            .getString(Variables.REFERAL_CODE, "")
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.back_btn -> {
                onBackPressed()
            }

            R.id.tabVerifyProfile -> {
                openRequestVerification()
            }

            R.id.tabManageAccount -> {
                openManageAccount()
            }

            R.id.tabPrivacy -> {
                openPrivacySetting()
            }

            R.id.tabCreatorTools -> {
                openCreatorTools()
            }

            R.id.taborders -> {
                openOrders()
            }


            R.id.tabFavourite -> {
                openFavurite()
            }

            R.id.tabPayoutSetting -> {
                openPayoutSetting()
            }

            R.id.tabBlockUser -> {
                openBlockUserList()
            }

            R.id.tabDraftVideo -> {
                openDraftVideo()
            }

            R.id.tabDarkmode -> {
                openDarkMode()
            }

            R.id.tabBalance -> {
                openMyWallet2()
            }


            R.id.tabQr->{
                openQrCode()
            }

            R.id.tabShareProfile -> {
                shareProfile()
            }

            R.id.tabaddress -> {
                startActivity(Intent(this, AddAddressActivity::class.java))
            }

            R.id.tabdeliveryPay -> {
                openpayment()
            }

            R.id.tabPushNotificaiton -> {
                openPushNotificationSetting()
            }

            R.id.referalLayout -> {
                if (binding.tvreferal!!.text.toString().isEmpty()) {
                    Toast.makeText(
                        this,
                        getString(R.string.contact_with_support_to_enable_this_feature),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showRefferalPopup(view, this@SettingAndPrivacyActivity)
                }
            }

            R.id.tabApplanguage -> {
                openAppLanguage()
            }

            R.id.tabFreeSpace -> {
                openAppSpace()
            }

            R.id.tabTermsOfService -> {
                openWebUrl(getString(R.string.terms_amp_conditions), Constants.terms_conditions)
            }

            R.id.tabPrivacyPolicy -> {
                openWebUrl(getString(R.string.privacy_policy), Constants.privacy_policy)
            }

            R.id.tabSwitchAccount -> {
                openManageMultipleAccounts()
            }

            R.id.tabLogout -> {
                logoutProceed()
            }
        }
    }


    private fun showRefferalPopup(view: View, context: Context) {
        val wrapper: Context = ContextThemeWrapper(context, R.style.AlertDialogCustom)
        val popup = PopupMenu(wrapper, view)
        popup.menuInflater.inflate(R.menu.menu_refferal, popup.menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.gravity = Gravity.TOP or Gravity.RIGHT
        }
        popup.show()
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuCopy -> {
                    copyRefferalLink()
                }

                R.id.menuShare -> {
                    shareCode()
                }
            }
            true
        }
    }

    private fun copyRefferalLink() {
//        String refferallink=Constants.REFERRAL_LINK+Functions.getSharedPreference(SettingAndPrivacyA.this).getString(Variables.REFERAL_CODE,"");
//        try {
//            ClipboardManager clipboard = (ClipboardManager) SettingAndPrivacyA.this.getSystemService(Context.CLIPBOARD_SERVICE);
//            ClipData clip = ClipData.newPlainText("Copied Text", refferallink);
//            clipboard.setPrimaryClip(clip);
//
//            Toast.makeText(SettingAndPrivacyA.this, SettingAndPrivacyA.this.getString(R.string.link_copy_in_clipboard), Toast.LENGTH_SHORT).show();
//        } catch(Exception e) {
//            Log.d(Constants.tag,"Exception : "+e);
//        }
    }

    fun shareCode() {
//        String refferallink=Constants.REFERRAL_LINK+Functions.getSharedPreference(SettingAndPrivacyA.this).getString(Variables.REFERAL_CODE,"");
//        try {
//            Intent sendIntent = new Intent("android.intent.action.MAIN");
//            sendIntent.setAction(Intent.ACTION_SEND);
//            sendIntent.setType("text/plain");
//            sendIntent.putExtra(Intent.EXTRA_TEXT, refferallink);
//            startActivity(sendIntent);
//        } catch(Exception e) {
//            Log.d(Constants.tag,"Exception : "+e);
//        }
    }

    private fun openCreatorTools() {
        val intent = Intent(this@SettingAndPrivacyActivity, CreatorToolsActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openManageAccount() {
        val intent = Intent(this@SettingAndPrivacyActivity, ManageProfileActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openDarkMode() {
        val intent = Intent(this@SettingAndPrivacyActivity, AppThemActivity::class.java)
        resultDarkModeCallback.launch(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openMyWallet() {
        val intent = Intent(this@SettingAndPrivacyActivity, MyWallet::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openMyWallet2() {
        val intent = Intent(this@SettingAndPrivacyActivity, MyWallet::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    override fun onResume() {
        super.onResume()
        binding.tvLanguage!!.text = getSharedPreference(this@SettingAndPrivacyActivity)
            .getString(Variables.APP_LANGUAGE, Variables.DEFAULT_LANGUAGE)
    }

    private fun openPayoutSetting() {
        val intent = Intent(this@SettingAndPrivacyActivity, WalletPaymentA::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openOrders() {
        val intent = Intent(this@SettingAndPrivacyActivity, HistoryA::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    val resultRefreshCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result -> if (result.resultCode == Activity.RESULT_OK) {
        val data = result.data
        data?.let {
            if (it.getBooleanExtra("isShow",false))
            {
                isRefrehsCallback = true
            }
        }
    }
    }



    private fun openFavurite() {
        val intent = Intent(this@SettingAndPrivacyActivity, FavouriteMainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun openBlockUserList() {
        val intent = Intent(this@SettingAndPrivacyActivity, BlockUserListActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    fun openRequestVerification() {
        val intent = Intent(this@SettingAndPrivacyActivity, ProfileVarificationActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    fun openDraftVideo() {
        val intent = Intent(this@SettingAndPrivacyActivity, DraftVideosActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun shareProfile() {
        val userId = getSharedPreference(this@SettingAndPrivacyActivity).getString(Variables.U_ID, "")
        val userName = getSharedPreference(this@SettingAndPrivacyActivity).getString(Variables.U_NAME, "")
        val fullName = getSharedPreference(this@SettingAndPrivacyActivity).getString(
            Variables.F_NAME,
            ""
        ) + " " + getSharedPreference(this@SettingAndPrivacyActivity).getString(Variables.L_NAME, "")
        val userPic = getSharedPreference(this@SettingAndPrivacyActivity).getString(Variables.U_PIC, "")
        val fragment =
            ShareUserProfileFragment(
                userId,
                userName,
                fullName,
                userPic,
                "",
                false,
                true
            ) { bundle ->
                if (bundle.getBoolean("isShow", false)) {
                }
            }
        fragment.show(supportFragmentManager, "ShareUserProfileF")
    }

    private fun openpayment() {
        val fragment =
            PaymentFragment({ bundle ->
                if (bundle != null) {
                }
            }, false)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(
            R.anim.in_from_right,
            R.anim.out_to_left,
            R.anim.in_from_left,
            R.anim.out_to_right
        )
        fragmentTransaction.replace(android.R.id.content, fragment).addToBackStack(null).commit()
    }

    private fun openPrivacySetting() {
        val intent = Intent(this@SettingAndPrivacyActivity, PrivacyPolicySettingActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openPushNotificationSetting() {
        val intent = Intent(this@SettingAndPrivacyActivity, PushNotificationSettingActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openAppLanguage() {
        val intent = Intent(this@SettingAndPrivacyActivity, AppLanguageChangeActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }


    private fun openAppSpace() {
        val intent = Intent(this@SettingAndPrivacyActivity, AppSpaceClearActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun logoutProceed() {
        if (Paper.book(Variables.MultiAccountKey).allKeys.size > 1) {
            Dialogs.showDoubleButtonAlert(
                this@SettingAndPrivacyActivity,
                getString(R.string.are_you_sure_to_logout),
                "",
                getString(R.string.logout),
                getString(R.string.switch_account), true
            ) { bundle ->
                if (bundle.getBoolean("isShow", false)) {
                    openManageMultipleAccounts()
                } else {
                    removePreferenceData()
                }
            }
        } else {
            removePreferenceData()
        }
    }
    private fun openQrCode() {
        val intent: Intent = Intent(this@SettingAndPrivacyActivity, QrCodeProfileActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    fun openWebUrl(title: String?, url: String?) {
        val intent = Intent(this@SettingAndPrivacyActivity, WebviewActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        intent.putExtra("from", "setting")
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun removePreferenceData() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(this@SettingAndPrivacyActivity, gso)
        googleSignInClient.signOut()
        AccountUtils.removePreferenceData(this@SettingAndPrivacyActivity)
        val intent = Intent(this@SettingAndPrivacyActivity, MainMenuActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun openManageMultipleAccounts() {
        val f = ManageAccountsFragment { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                hideSoftKeyboard(this@SettingAndPrivacyActivity)
                val intent = Intent(this@SettingAndPrivacyActivity, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
            }
        }
        f.show(supportFragmentManager, "ManageAccountsF")
    }


    var isRefrehsCallback =false
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (isRefrehsCallback) {
            val intent = Intent()
            intent.putExtra("isShow", true)
            setResult(RESULT_OK, intent)
        }

        finish()
    }
}