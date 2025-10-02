package com.coheser.app.activitesfragments.profile.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.ActivityQrCodeScannerBinding
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.Functions.checkProfileOpenValidation
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getPermissionStatus
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showPermissionSetting
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.PermissionUtils
import com.coheser.app.simpleclasses.Variables
import com.blikoon.qrcodescanner.decode.DecodeImageCallback
import com.blikoon.qrcodescanner.decode.DecodeImageThread
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.google.zxing.Result
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class QrCodeScannerActivity : AppCompatLocaleActivity(), View.OnClickListener {
    var userId: String? = null

    var takePermissionUtils: PermissionUtils? = null
    private var mCodeScanner: CodeScanner? = null
    private var mQrCodeExecutor: Executor? = null

    lateinit var binding:ActivityQrCodeScannerBinding


    private val mDecodeImageCallback: DecodeImageCallback = object : DecodeImageCallback {
        override fun decodeSucceed(result: Result) {
            Log.d(Constants.tag, "QR Code " + result.text)
            runOnUiThread {
                val profileURL =
                    "${Variables.https}://${getString(R.string.domain)}${getString(R.string.share_profile_endpoint_second)}"

                if (result.text.contains(profileURL)) {
                    try {
                        val parts = result.text.split(profileURL)
                        userId = parts[1]
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    hitgetUserProfile()
                } else {
                    Toast.makeText(
                        this@QrCodeScannerActivity,
                        getString(R.string.user_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        override fun decodeFail(type: Int, reason: String) {
            Log.d(Constants.tag, reason)
            runOnUiThread {
                Toast.makeText(
                    this@QrCodeScannerActivity,
                    getString(R.string.user_not_found),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    var resultCallbackForGallery = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode == RESULT_OK) {
                    val data = result.data
                    val uri = data!!.data
                    val imgPath = getPathFromUri(uri)
                    if (imgPath != null && !TextUtils.isEmpty(imgPath) && null != mQrCodeExecutor) {
                        mQrCodeExecutor!!.execute(DecodeImageThread(imgPath, mDecodeImageCallback))
                    }
                }
            }
        })


    private val mPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),  { result->

                var allPermissionClear = true
                val blockPermissionCheck: MutableList<String> = ArrayList()
                for (key in result.keys) {
                    if (!result[key]!!) {
                        allPermissionClear = false
                        blockPermissionCheck.add(
                            getPermissionStatus(
                                this@QrCodeScannerActivity,
                                key
                            )
                        )
                    }
                }
                if (blockPermissionCheck.contains("blocked")) {
                    showPermissionSetting(
                        this@QrCodeScannerActivity,
                        getString(R.string.we_need_storage_permission_for_upload_qr_pic)
                    )
                } else if (allPermissionClear) {
                    takePictureFromGallery()
                }


        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@QrCodeScannerActivity).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding=DataBindingUtil.setContentView(this,R.layout.activity_qr_code_scanner)
        InitControl()
    }

    private fun InitControl() {
        takePermissionUtils = PermissionUtils(this@QrCodeScannerActivity, mPermissionResult)
        mQrCodeExecutor = Executors.newSingleThreadExecutor()
        binding.ivBack.setOnClickListener(this)
        binding.tabQrCode.setOnClickListener(this)
        binding.tvAlbum.setOnClickListener(this)
        binding.ivFlash.setOnClickListener(this)
        setUpScannerView()
    }

    private fun setUpScannerView() {
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        mCodeScanner = CodeScanner(this, scannerView)
        mCodeScanner!!.decodeCallback = DecodeCallback { result ->
            runOnUiThread {
                Log.d(Constants.tag, "QR Code " + result.text)
                val profileURL = "${Variables.https}://${getString(R.string.domain)}/profile/"

                if (result.text.contains(profileURL)) {
                    try {
                        val parts = result.text.split(profileURL)
                        userId = parts[1]
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    hitgetUserProfile()
                } else {
                    Toast.makeText(this@QrCodeScannerActivity, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
                }
            }
        }
        scannerView.setOnClickListener { mCodeScanner!!.startPreview() }
        if (mCodeScanner!!.isFlashEnabled) {
            binding.ivFlash!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this@QrCodeScannerActivity,
                    R.drawable.ic_scan_flash_on
                )
            )
        } else {
            binding.ivFlash!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this@QrCodeScannerActivity,
                    R.drawable.ic_scan_flash_off
                )
            )
        }
    }

    private fun hitgetUserProfile() {
        if (intent == null) {
            userId = getSharedPreference(this@QrCodeScannerActivity).getString(Variables.U_ID, "0")
        }
        val parameters = JSONObject()
        try {
            if (getSharedPreference(this@QrCodeScannerActivity).getBoolean(
                    Variables.IS_LOGIN,
                    false
                ) && userId != null
            ) {
                parameters.put(
                    "user_id",
                    getSharedPreference(this@QrCodeScannerActivity).getString(Variables.U_ID, "")
                )
                parameters.put("other_user_id", userId)
            } else if (userId != null) {
                parameters.put("user_id", userId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.ivFlash!!.visibility = View.GONE
        binding.progressBar!!.visibility = View.VISIBLE
        VolleyRequest.JsonPostRequest(
            this@QrCodeScannerActivity,
            ApiLinks.showUserDetail,
            parameters,
            getHeaders(this)
        ) { resp ->
            checkStatus(this@QrCodeScannerActivity, resp)
            binding.progressBar!!.visibility = View.GONE
            binding.ivFlash!!.visibility = View.VISIBLE
            parseData(resp)
        }
    }

    fun parseData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msg = jsonObject.optJSONObject("msg")
                val userDetailModel = getUserDataModel(msg.optJSONObject("User"))
                moveToProfile(
                    userDetailModel.id, userDetailModel.username, userDetailModel.getProfilePic()
                )
            } else {
                showToast(this@QrCodeScannerActivity, getString(R.string.user_not_found))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun moveToProfile(id: String?, username: String?, pic: String?) {
        if (checkProfileOpenValidation(id)) {
            val intent = Intent(this@QrCodeScannerActivity, ProfileActivity::class.java)
            intent.putExtra("user_id", id)
            intent.putExtra("user_name", username)
            intent.putExtra("user_pic", pic)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
        }
    }

    override fun onResume() {
        super.onResume()
        mCodeScanner!!.startPreview()
    }

    override fun onPause() {
        mCodeScanner!!.releaseResources()
        super.onPause()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivBack -> {
                super@QrCodeScannerActivity.onBackPressed()
            }

            R.id.tabQrCode -> {
                super@QrCodeScannerActivity.onBackPressed()
            }

            R.id.tvAlbum -> {
                if (takePermissionUtils!!.isStoragePermissionGranted) {
                    takePictureFromGallery()
                } else {
                    takePermissionUtils!!.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_for_upload_qr_pic))
                }
            }

            R.id.ivFlash -> {
                if (mCodeScanner!!.isFlashEnabled) {
                    binding.ivFlash!!.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@QrCodeScannerActivity,
                            R.drawable.ic_scan_flash_off
                        )
                    )
                    mCodeScanner!!.isFlashEnabled = false
                } else {
                    binding.ivFlash!!.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@QrCodeScannerActivity,
                            R.drawable.ic_scan_flash_on
                        )
                    )
                    mCodeScanner!!.isFlashEnabled = true
                }
            }
        }
    }

    private fun takePictureFromGallery() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        resultCallbackForGallery.launch(intent)
    }

    fun getPathFromUri(uri: Uri?): String {
        return try {
            var cursor = contentResolver.query(uri!!, null, null, null, null)
            cursor!!.moveToFirst()
            var document_id = cursor.getString(0)
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1)
            cursor.close()
            cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", arrayOf(document_id), null
            )
            cursor!!.moveToFirst()
            @SuppressLint("Range") val path =
                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            cursor.close()
            path
        } catch (e: Exception) {
            ""
        }
    }

    override fun onDestroy() {
        mPermissionResult.unregister()
        super.onDestroy()
    }
}