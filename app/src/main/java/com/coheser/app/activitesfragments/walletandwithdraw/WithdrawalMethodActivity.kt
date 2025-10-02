package com.coheser.app.activitesfragments.walletandwithdraw

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.settings.AddPayoutMethodActivity
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityWithdrawalMethodBinding
import com.coheser.app.viewModels.ShowPayoutViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class WithdrawalMethodActivity : AppCompatActivity() {
    var binding: ActivityWithdrawalMethodBinding? = null
    private val viewModel: ShowPayoutViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_withdrawal_method)

        binding!!.lifecycleOwner = this
        binding!!.backBtn.setOnClickListener { finish() }
        binding!!.paypalBtn.setOnClickListener {
            openAddPayout("Paypal", binding!!.emailPaypal.text.toString())
        }
        binding!!.venmoBtn.setOnClickListener {
            openAddPayout("Venmo", binding!!.emailVenmo.text.toString())
        }

        initObserver()
    }

    fun initObserver() {
        viewModel.payoutLiveData.observe(this) { response ->
            if (response is ApiResponce.Success) {
                response.data?.let { list ->
                    binding?.apply {
                        list.forEach { payout ->
                            when (payout.type) {
                                "Venmo" -> {
                                    emailVenmo.visibility = View.VISIBLE
                                    emailVenmo.text = payout.value
                                }

                                "Paypal" -> {
                                    emailPaypal.visibility = View.VISIBLE
                                    emailPaypal.text = payout.value
                                }

                                "bank" -> {
                                    emailBank.visibility = View.VISIBLE
                                    emailBank.text = payout.value
                                }
                            }
                        }
                    }
                }
            }
        }

    }


    fun openAddPayout(type: String, email: String) {
        val intent = Intent(this@WithdrawalMethodActivity, AddPayoutMethodActivity::class.java)
        if (!TextUtils.isEmpty(email)) {
            intent.putExtra("email", email)
            intent.putExtra("isEdit", true)
        }
        intent.putExtra("type", type)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }
    override fun onResume() {
        super.onResume()
        viewModel.showPayout()
    }

}