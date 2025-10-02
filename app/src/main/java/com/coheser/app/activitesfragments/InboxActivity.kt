package com.coheser.app.activitesfragments

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.coheser.app.R
import com.coheser.app.activitesfragments.chat.ChatActivity
import com.coheser.app.models.InboxModel
import com.coheser.app.simpleclasses.FirebaseChatUtil
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.composeScreens.InboxScreen
import com.coheser.app.viewModels.InboxViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class InboxActivity : ComponentActivity() {

    var isActivityCallback = false

    private val viewModel: InboxViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE,
            Variables.DEFAULT_LANGUAGE_CODE), this, javaClass, false)

        setContent {
                InboxScreen(
                    this,
                    viewModel,
                    onBackPressed = { onBackPressed() },
                    onChatSelected = { model ->
                        chatFragment(model)
                    }
                )

        }
    }


    var resultCallback = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let {data->
                if (data.getBooleanExtra("isShow", false)) {

                }
            }
        }
    }
    fun chatFragment(model: InboxModel) {
        isActivityCallback = true
        val intent = Intent(this@InboxActivity, ChatActivity::class.java)
        intent.putExtra("user_id", model.id)
        intent.putExtra("user_name", model.name)
        intent.putExtra("user_pic", model.pic)
        try {
            resultCallback.launch(intent)
        }catch (e:Exception){
            startActivity(intent)
        }
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    override fun onBackPressed() {
        if (isActivityCallback) {
            val intent = Intent()
            intent.putExtra("isShow", true)
            setResult(RESULT_OK, intent)
        }
        finish()
    }


    override fun onDestroy() {
        FirebaseChatUtil.unregisterUserInbox()
        super.onDestroy()
    }
}
