package com.coheser.app.activitesfragments.profile.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityPushNotificationSettingBinding
import com.coheser.app.models.PushNotificationModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.PushNotificationViewModel
import io.paperdb.Paper
import org.koin.androidx.viewmodel.ext.android.viewModel

class PushNotificationSettingActivity : AppCompatLocaleActivity(), View.OnClickListener {


    var pushNotificationModel: PushNotificationModel? = null
    lateinit var binding:ActivityPushNotificationSettingBinding
    private val viewModel: PushNotificationViewModel  by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@PushNotificationSettingActivity).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding=DataBindingUtil.setContentView(this,R.layout.activity_push_notification_setting)
        binding.viewModel=viewModel
        binding.lifecycleOwner = this

       initControl()

        setObserver()
    }


    fun setObserver(){
        viewModel.pushNotificationLiveData.observe(this,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {
                            pushNotificationModel=it
                            Paper.book(Variables.PrivacySetting)
                                .write(Variables.PushSettingModel, pushNotificationModel!!)
                            Toast.makeText(
                                this,
                                getString(R.string.push_notification_setting_updated),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                is ApiResponce.Error ->{
                    showToast(this@PushNotificationSettingActivity, it.message)
                }
                else -> {

                }
            }
        })


    }


    private fun initControl() {
        pushNotificationModel = Paper.book(Variables.PrivacySetting).read(Variables.PushSettingModel)

        binding.backBtn.setOnClickListener(this)
        binding.likesSwitch.setOnClickListener(this)
        binding.commentsSwitch.setOnClickListener(this)
        binding.newFollowerSwitch.setOnClickListener(this)
        binding.mentionSwitch.setOnClickListener(this)
        binding.directMessageSwitch.setOnClickListener(this)
        binding.videoUpdateSwitch.setOnClickListener(this)
        setUpScreenData()
    }


    private fun setUpScreenData() {
        try {
            viewModel.strLikes = pushNotificationModel!!.likes!!
            binding.likesSwitch!!.isChecked = getTrueFalseCondition("" + viewModel.strLikes)

            viewModel.str_video_update = pushNotificationModel!!.videoUpdates!!
            binding.videoUpdateSwitch!!.isChecked = getTrueFalseCondition("" + viewModel.str_video_update)

            viewModel.strDirectMessage = pushNotificationModel!!.directMessages!!
            binding.directMessageSwitch!!.isChecked = getTrueFalseCondition("" + viewModel.strDirectMessage)

            viewModel.strMention = pushNotificationModel!!.mentions!!
            binding.mentionSwitch!!.isChecked = getTrueFalseCondition("" + viewModel.strMention)

            viewModel.strNewFollow = pushNotificationModel!!.newFollowers!!
            binding.newFollowerSwitch!!.isChecked = getTrueFalseCondition("" + viewModel.strNewFollow)

            viewModel.strComment = pushNotificationModel!!.comments!!
            binding.commentsSwitch!!.isChecked = getTrueFalseCondition("" + viewModel.strComment)

        } catch (e: Exception) {
            e.stackTrace
        }
    }

    private fun getTrueFalseCondition(str: String): Boolean {
        return str.equals("1", ignoreCase = true)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.back_btn -> super@PushNotificationSettingActivity.onBackPressed()
            R.id.likesSwitch -> {
                viewModel.strLikes = if (binding.likesSwitch.isChecked) {
                    1
                } else {
                    0
                }
                viewModel.updatePushNotificationSetting()
            }

            R.id.commentsSwitch -> {
                viewModel.strComment = if (binding.commentsSwitch.isChecked) {
                    1
                } else {
                    0
                }
                viewModel.updatePushNotificationSetting()
            }

            R.id.newFollowerSwitch -> {
                viewModel.strNewFollow = if (binding.newFollowerSwitch.isChecked) {
                    1
                } else {
                    0
                }
                viewModel.updatePushNotificationSetting()
            }

            R.id.mentionSwitch -> {
                viewModel.strMention = if (binding.mentionSwitch.isChecked) {
                    1
                } else {
                    0
                }
                viewModel.updatePushNotificationSetting()
            }

            R.id.directMessageSwitch -> {
                viewModel.strDirectMessage = if (binding.directMessageSwitch.isChecked) {
                    1
                } else {
                    0
                }
                viewModel.updatePushNotificationSetting()
            }

            R.id.videoUpdateSwitch -> {
                viewModel.str_video_update = if (binding.videoUpdateSwitch.isChecked) {
                    1
                } else {
                    0
                }
                viewModel.updatePushNotificationSetting()

            }
        }
    }

}