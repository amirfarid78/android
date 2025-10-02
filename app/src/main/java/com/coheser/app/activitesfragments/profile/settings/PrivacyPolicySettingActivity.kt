package com.coheser.app.activitesfragments.profile.settings

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityPrivacyPolicySettingBinding
import com.coheser.app.models.PrivacySettingModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.Functions.stringParseFromServerRestriction
import com.coheser.app.simpleclasses.Functions.stringParseIntoServerRestriction
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.PrivacyPolicyViewModel
import io.paperdb.Paper
import java.util.Locale
import org.koin.androidx.viewmodel.ext.android.viewModel

class PrivacyPolicySettingActivity : AppCompatLocaleActivity(), View.OnClickListener {

    lateinit var privacySettingModel: PrivacySettingModel
    lateinit var binding:ActivityPrivacyPolicySettingBinding
    private val viewModel: PrivacyPolicyViewModel  by viewModel()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@PrivacyPolicySettingActivity).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding=DataBindingUtil.setContentView(this@PrivacyPolicySettingActivity,R.layout.activity_privacy_policy_setting)

       binding.viewModel=viewModel
        binding.lifecycleOwner = this


        initControl()

        setObserver()
    }

    // initialize the all views
    private fun initControl() {

       privacySettingModel = requireNotNull(Paper.book(Variables.PrivacySetting).read(Variables.PrivacySettingModel))

        setUpScreenData()
        binding.backBtn.setOnClickListener(this)
        binding.allowDownloadLayout.setOnClickListener(this)
        binding.allowCommenetLayout.setOnClickListener(this)
        binding.allowDmesgesLayout.setOnClickListener(this)
        binding.allowDuetLayout.setOnClickListener(this)
        binding.allowViewLikevidLayout.setOnClickListener(this)
        binding.allowViewOrderedLayout.setOnClickListener(this)
    }

    private fun setUpScreenData() {
        try {
            if (privacySettingModel!!.videosDownload.toString() == "1") {
                binding.allowDownloadTxt!!.text = stringParseFromServerRestriction(getString(R.string.on))
            } else {
                binding.allowDownloadTxt!!.text = stringParseFromServerRestriction(getString(R.string.off))
            }

            binding.allowCommenetTxt!!.text = stringParseFromServerRestriction(
                privacySettingModel!!.videoComment!!
            )
            binding.allowDirectMesgTxt!!.text = stringParseFromServerRestriction(
                privacySettingModel!!.directMessage!!
            )
            binding.allowDuetTxt!!.text = stringParseFromServerRestriction(
                privacySettingModel!!.duet!!
            )
            binding.allowViewLikevidTxt!!.text = stringParseFromServerRestriction(
                privacySettingModel!!.likedVideos!!
            )
            binding.allowViewOrderedTxt!!.text = stringParseFromServerRestriction(
                privacySettingModel!!.orderHistory!!
            )
        } catch (e: Exception) {
            e.stackTrace
        }
    }



    fun setObserver(){
        viewModel.privacyPolicyLiveData.observe(this,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {
                            privacySettingModel=it
                            Paper.book(Variables.PrivacySetting)
                                .write(Variables.PrivacySettingModel, privacySettingModel!!)
                            Toast.makeText(
                                this,
                                getString(R.string.privacy_setting_updated),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                is ApiResponce.Error ->{
                    showToast(this@PrivacyPolicySettingActivity, it.message)
                }
                else -> {

                }
            }
        })


    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.backBtn -> super@PrivacyPolicySettingActivity.onBackPressed()
            R.id.allow_download_layout -> {
                val options = arrayOf<CharSequence>(
                    getString(R.string.on),
                    getString(R.string.off),
                    getString(R.string.cancel_)
                )
                selectimage(
                    getString(R.string.select_download_option),
                    options,
                    view.findViewById(R.id.allow_download_txt),
                    1
                )
            }

            R.id.allow_commenet_layout -> {
                val Commentoptions = arrayOf<CharSequence>(
                    getString(R.string.everyone),
                    getString(R.string.friend),
                    getString(R.string.no_one),
                    getString(R.string.cancel_)
                )
                selectimage(
                    getString(R.string.select_comment_option),
                    Commentoptions,
                    view.findViewById(R.id.allow_commenet_txt),
                    2
                )
            }

            R.id.allow_dmesges_layout -> {
                val messgeoptions = arrayOf<CharSequence>(
                    getString(R.string.everyone),
                    getString(R.string.friend),
                    getString(R.string.no_one),
                    getString(R.string.cancel_)
                )
                selectimage(
                    getString(R.string.select_message_option),
                    messgeoptions,
                    binding.allowDirectMesgTxt,
                    3
                )
            }

            R.id.allow_duet_layout -> {
                val duetoption = arrayOf<CharSequence>(
                    getString(R.string.everyone),
                    getString(R.string.friend),
                    getString(R.string.no_one),
                    getString(R.string.cancel_)
                )
                selectimage(getString(R.string.select_duet_option), duetoption, binding.allowDuetTxt, 4)
            }

            R.id.allow_view_likevid_layout -> {
                val likevidoption = arrayOf<CharSequence>(
                    getString(R.string.everyone),
                    getString(R.string.only_me),
                    getString(R.string.cancel_)
                )
                selectimage(
                    getString(R.string.select_like_video_option),
                    likevidoption,
                    binding.allowViewLikevidTxt,
                    5
                )
            }

            R.id.allow_view_ordered_layout -> {
                val orderOption = arrayOf<CharSequence>(
                    getString(R.string.everyone),
                    getString(R.string.only_me),
                    getString(R.string.cancel_)
                )
                selectimage(
                    getString(R.string.select_order_option),
                    orderOption,
                    binding.allowViewOrderedTxt,
                    6
                )
            }
        }
    }

    private fun selectimage(
        title: String,
        options: Array<CharSequence>,
        textView: TextView?,
        Selected_box: Int
    ) {
        val builder =
            AlertDialog.Builder(this@PrivacyPolicySettingActivity, R.style.AlertDialogCustom)
        builder.setTitle(title)
        builder.setItems(options) { dialog, item ->
            val op = options[item]
            if (op != getString(R.string.cancel_).toString()) {
                textView!!.text = ("" + options[item]).lowercase(Locale.getDefault())
                when (Selected_box) {
                    1 -> viewModel.strVideoDownload =
                        if (op.toString().equals(getString(R.string.on), ignoreCase = true)) {
                            "1"
                        } else {
                            "0"
                        }

                    2 -> viewModel.strVideoComment =
                        if (op.toString().equals(getString(R.string.everyone), ignoreCase = true)) {
                            stringParseIntoServerRestriction("Everyone")
                        } else if (op.toString()
                                .equals(getString(R.string.friend), ignoreCase = true)
                        ) {
                            stringParseIntoServerRestriction("Friend")
                        } else {
                            stringParseIntoServerRestriction("No One")
                        }

                    3 -> viewModel.strDirectMessage =
                        if (op.toString().equals(getString(R.string.everyone), ignoreCase = true)) {
                            stringParseIntoServerRestriction("Everyone")
                        } else if (op.toString()
                                .equals(getString(R.string.friend), ignoreCase = true)
                        ) {
                            stringParseIntoServerRestriction("Friend")
                        } else {
                            stringParseIntoServerRestriction("No One")
                        }

                    4 -> viewModel.strDuet =
                        if (op.toString().equals(getString(R.string.everyone), ignoreCase = true)) {
                            stringParseIntoServerRestriction("Everyone")
                        } else if (op.toString()
                                .equals(getString(R.string.friend), ignoreCase = true)
                        ) {
                            stringParseIntoServerRestriction("Friend")
                        } else {
                            stringParseIntoServerRestriction("No One")
                        }

                    5 -> viewModel.strLikedVideo =
                        if (op.toString().equals(getString(R.string.everyone), ignoreCase = true)) {
                            stringParseIntoServerRestriction("Everyone")
                        } else {
                            stringParseIntoServerRestriction("Only Me")
                        }

                    6 -> viewModel.orderView =
                        if (op.toString().equals(getString(R.string.everyone), ignoreCase = true)) {
                            stringParseIntoServerRestriction("Everyone")
                        } else {
                            stringParseIntoServerRestriction("Only Me")
                        }
                }
                viewModel.addPrivacySetting()

            }
        }
        builder.show()
    }



}