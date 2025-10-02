package com.coheser.app.activitesfragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.coheser.app.R
import com.coheser.app.simpleclasses.AppCompatLocaleActivity

class CustomErrorActivity : AppCompatLocaleActivity(), View.OnClickListener {
    var pacakgeName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_error)
        pacakgeName = applicationContext.packageName
        val restartButton = findViewById<Button>(R.id.restart_button)
        val config = CustomActivityOnCrash.getConfigFromIntent(intent)
        if (config == null) {
            finish()
            return
        }
        if (config.isShowRestartButton && config.restartActivityClass != null) {
            restartButton.setText(R.string.restart_app)
            restartButton.setOnClickListener {
                startActivity(Intent(this@CustomErrorActivity, SplashActivity::class.java))
                finish()
            }
        } else {
            restartButton.setOnClickListener {
                CustomActivityOnCrash.closeApplication(
                    this@CustomErrorActivity,
                    config
                )
            }
        }
        findViewById<View>(R.id.detail_button).setOnClickListener(this)
        showSendReport()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.detail_button -> showAlert()
        }
    }

    fun showAlert() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.customactivityoncrash_error_activity_error_details_title)
            .setMessage(
                CustomActivityOnCrash.getAllErrorDetailsFromIntent(
                    this@CustomErrorActivity,
                    intent
                )
            )
            .setPositiveButton(
                R.string.customactivityoncrash_error_activity_error_details_close,
                null
            )
            .setNeutralButton(
                R.string.customactivityoncrash_error_activity_error_details_copy
            ) { dialog, which -> copyErrorToClipboard() }
            .show()
    }

    private fun copyErrorToClipboard() {
        val errorInformation =
            CustomActivityOnCrash.getAllErrorDetailsFromIntent(this@CustomErrorActivity, intent)
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        //Are there any devices without clipboard...?
        if (clipboard != null) {
            val clip = ClipData.newPlainText(
                getString(R.string.customactivityoncrash_error_activity_error_details_clipboard_label),
                errorInformation
            )
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                this@CustomErrorActivity,
                R.string.customactivityoncrash_error_activity_error_details_copied,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun showSendReport() {
        val sendReposrt = findViewById<Button>(R.id.send_reposrt)
        if (pacakgeName!!.contains("qboxus")) {
            sendReposrt.visibility = View.VISIBLE
            sendReposrt.setOnClickListener(this)
        } else {
            sendReposrt.visibility = View.GONE
        }
    }
}