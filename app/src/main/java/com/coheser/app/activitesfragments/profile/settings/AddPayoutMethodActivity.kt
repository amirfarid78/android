package com.coheser.app.activitesfragments.profile.settings

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityAddPayoutMethodBinding
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.isValidEmail
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.AddPayoutViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddPayoutMethodActivity : AppCompatLocaleActivity(), View.OnClickListener {
    var headingText: TextView? = null
    var type: String? = "Paypal"
    lateinit var binding: ActivityAddPayoutMethodBinding

    private val viewModel: AddPayoutViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@AddPayoutMethodActivity).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ),
            this, javaClass, false
        )
        binding = ActivityAddPayoutMethodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        InitControl()
        ActionContorl()
        initObserver()
    }
    fun initObserver(){
        viewModel.payoutLiveData.observe(this) {
            when (it) {
                is ApiResponce.Success -> {
                    it.data?.let { list ->
                        list.forEach {
                            val editor = getSharedPreference(this@AddPayoutMethodActivity).edit()
                            editor.putString(Variables.U_PAYOUT_ID, it.paypal)
                            editor.apply()
                            val intent = Intent()
                            intent.putExtra("isShow", true)
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                    }
                }
                else -> {

                }
            }
        }
    }

    private fun ActionContorl() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, count: Int) {
                // check the email validation during user typing
                val txtName = binding.etEmail.text.toString()
                if (txtName.length > 0) {
                    if (isValidEmail(binding.etEmail.text.toString())) {
                        binding.btnAdd.isEnabled = true
                        binding.btnAdd.isClickable = true
                    } else {
                        binding.btnAdd.isEnabled = false
                        binding.btnAdd.isClickable = false
                    }
                } else {
                    binding.btnAdd.isEnabled = false
                    binding.btnAdd.isClickable = false
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    private fun InitControl() {
        headingText = findViewById(R.id.headingtext)
        binding.btnAdd.setOnClickListener(this)
        findViewById<View>(R.id.goBack1).setOnClickListener(this)
        SetupScreenData()
    }

    private fun SetupScreenData() {
        if (intent.getBooleanExtra("isEdit", false)) {
            if (intent.hasExtra("type")) {
                type = intent.getStringExtra("type")
                headingText!!.text =
                    getString(R.string.enter_your) + " " + type + " " + getString(R.string.email_address)
            }
            binding.etEmail.setText(intent.getStringExtra("email"))
            binding.btnAdd.text = getString(R.string.update_payout)
        } else if (intent.hasExtra("type")) {
            type = intent.getStringExtra("type")
            headingText!!.text =
                getString(R.string.enter_your) + " " + type + " " + getString(R.string.email_address)
        } else {
            binding.btnAdd.text = getString(R.string.add_payout)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.goBack1 -> super@AddPayoutMethodActivity.onBackPressed()
            R.id.btnAdd -> {
                if (TextUtils.isEmpty(binding.etEmail.text.toString())) {
                    binding.etEmail.error = getString(R.string.email_cant_empty)
                    binding.etEmail.isFocusable = true
                    return
                }
                if (!isValidEmail(binding.etEmail.text.toString())) {
                    binding.etEmail.error = getString(R.string.enter_valid_email)
                    binding.etEmail.isFocusable = true
                    return
                }
                viewModel.addPayout(type!!,binding.etEmail.text.toString())
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    hideKeyboard(v)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}