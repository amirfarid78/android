package com.coheser.app.activitesfragments.profile.settings

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.exifinterface.media.ExifInterface
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.OptionSelectionSheetFragment
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityProfileVarificationBinding
import com.coheser.app.enumClasses.MediaOptions
import com.coheser.app.models.OptionSelectionModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.FileUtils.bitmapToBase64
import com.coheser.app.simpleclasses.FileUtils.getFileFromUri
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.getPermissionStatus
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Functions.showPermissionSetting
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.PermissionUtils
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.ProfileVerificationViewModel
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileVarificationActivity : AppCompatLocaleActivity(), View.OnClickListener {

    lateinit var binding: ActivityProfileVarificationBinding
    var base64: String? = null
    var image_file: File? = null
    var takePermissionUtils: PermissionUtils? = null

    var resultCallbackForGallery = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode == RESULT_OK) {
                    val data = result.data
                    val selectedImage = data!!.data
                    try {
                        image_file =
                            getFileFromUri(this@ProfileVarificationActivity, selectedImage!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    var imageStream: InputStream? = null
                    try {
                        imageStream =
                            this@ProfileVarificationActivity.contentResolver.openInputStream(
                                selectedImage!!
                            )
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                    val imagebitmap = BitmapFactory.decodeStream(imageStream)
                    val path = getPath(selectedImage)
                    val matrix = Matrix()
                    var exif: ExifInterface? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try {
                            exif = ExifInterface(path)
                            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
                            when (orientation) {
                                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        imagebitmap,
                        0,
                        0,
                        imagebitmap.width,
                        imagebitmap.height,
                        matrix,
                        true
                    )
                    val resized = Bitmap.createScaledBitmap(
                        rotatedBitmap,
                        (rotatedBitmap.width * 0.5).toInt(),
                        (rotatedBitmap.height * 0.5).toInt(),
                        true
                    )
                    val baos = ByteArrayOutputStream()
                    resized.compress(Bitmap.CompressFormat.JPEG, 20, baos)
                    base64 = bitmapToBase64(resized)
                    if (image_file != null) binding!!.fileNameTxt.text = image_file!!.name
                }
            }
        })

    var resultCallbackForCamera = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode == RESULT_OK) {
                    val imageFilePath = getSharedPreference(
                        applicationContext
                    ).getString(Variables.captureImage, "")
                    val matrix = Matrix()
                    try {
                        val exif = ExifInterface(
                            imageFilePath!!
                        )
                        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
                        when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    image_file = File(imageFilePath)
                    val selectedImage = Uri.fromFile(image_file)
                    var imageStream: InputStream? = null
                    try {
                        imageStream =
                            this@ProfileVarificationActivity.contentResolver.openInputStream(
                                selectedImage
                            )
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                    val imagebitmap = BitmapFactory.decodeStream(imageStream)
                    val rotatedBitmap = Bitmap.createBitmap(
                        imagebitmap,
                        0,
                        0,
                        imagebitmap.width,
                        imagebitmap.height,
                        matrix,
                        true
                    )
                    val resized = Bitmap.createScaledBitmap(
                        rotatedBitmap,
                        (rotatedBitmap.width * 0.7).toInt(),
                        (rotatedBitmap.height * 0.7).toInt(),
                        true
                    )
                    val baos = ByteArrayOutputStream()
                    resized.compress(Bitmap.CompressFormat.JPEG, 20, baos)
                    base64 = bitmapToBase64(resized)
                    if (image_file != null) binding!!.fileNameTxt.text = image_file!!.name
                }
            }
        })

    private val mPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),  { result ->

                var allPermissionClear = true
                val blockPermissionCheck: MutableList<String> = ArrayList()
                for (key in result.keys) {
                    if (!result[key]!!) {
                        allPermissionClear = false
                        blockPermissionCheck.add(
                            getPermissionStatus(
                                this@ProfileVarificationActivity,
                                key
                            )
                        )
                    }
                }
                if (blockPermissionCheck.contains("blocked")) {
                    showPermissionSetting(
                        this@ProfileVarificationActivity,
                        getString(R.string.we_need_storage_and_camera_permission_for_upload_profile_pic)
                    )
                } else if (allPermissionClear) {
                    selectImage()
                }

        })

    private val viewModel: ProfileVerificationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@ProfileVarificationActivity).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile_varification)

        binding.viewModel=viewModel
        binding.lifecycleOwner = this

        InitControl()
        setObserver()
    }


    fun setObserver(){
        viewModel.verificationLiveData.observe(this,{
            when(it){
                is ApiResponce.Error ->{
                    showLoader(this@ProfileVarificationActivity, false, false)
                }
                is ApiResponce.Success ->{
                    cancelLoader()
                    it.data?.let {

                        getSharedPreference(this@ProfileVarificationActivity).edit()
                                .putString(Variables.IS_VERIFICATION_APPLY, "1").commit()
                            showToast(
                                this@ProfileVarificationActivity,
                                getString(R.string.your_application_has_been_received_we_will_notify_you_via_email_if_it_gets_approved)
                            )
                            super@ProfileVarificationActivity.onBackPressed()

                    }
                }
                is ApiResponce.Error ->{
                    cancelLoader()
                    showToast(this@ProfileVarificationActivity, it.message)

                }
                else -> {

                }
            }
        })

    }



    private fun InitControl() {
        takePermissionUtils = PermissionUtils(this@ProfileVarificationActivity, mPermissionResult)
        binding!!.goBack.setOnClickListener(this)
        binding!!.chooseFileBtn.setOnClickListener(this)
        binding!!.sendBtn.setOnClickListener(this)
        setUpScreendata()
    }

    private fun setUpScreendata() {
        val applyForverification =
            getString(R.string.apply_for) + " " + getString(R.string.app_name) + " " + getString(R.string.verification)
        binding!!.tvTitle.text = applyForverification
        val instruction =
            getString(R.string.verification_instruction_one) + getString(R.string.app_name) + " " + getString(
                R.string.verification_instruction_two
            )
        binding!!.tvInstruction.text = instruction
        if (getSharedPreference(this@ProfileVarificationActivity).getString(
                Variables.IS_VERIFICATION_APPLY,
                "0"
            ).equals("1", ignoreCase = true)
        ) {
            binding!!.fileNameTxt.setTextColor(
                ContextCompat.getColor(
                    this@ProfileVarificationActivity,
                    R.color.greenColor
                )
            )
            binding!!.fileNameTxt.text = getString(R.string.verification_request_already_apply)
            binding!!.sendBtn.visibility = View.GONE
            binding!!.chooseFileBtn.visibility = View.GONE
        } else {
            binding!!.sendBtn.visibility = View.VISIBLE
            binding!!.chooseFileBtn.visibility = View.VISIBLE
        }
        binding!!.fullnameEdit.setText(
            getSharedPreference(this@ProfileVarificationActivity).getString(
                Variables.F_NAME,
                ""
            ) + " " + getSharedPreference(this@ProfileVarificationActivity).getString(
                Variables.L_NAME,
                ""
            )
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.goBack -> super@ProfileVarificationActivity.onBackPressed()
            R.id.choose_file_btn -> if (takePermissionUtils!!.isStorageCameraPermissionGranted) {
                selectImage()
            } else {
                takePermissionUtils!!.showStorageCameraPermissionDailog(getString(R.string.we_need_storage_and_camera_permission_for_upload_verification_pic))
            }

            R.id.send_btn -> if (checkValidation()) {

                callApi()

            }
        }
    }

    private fun selectImage() {
        val optionalList=ArrayList<OptionSelectionModel>().apply {
            add(
                OptionSelectionModel(
                    MediaOptions.TakePhoto.value,
                    MediaOptions.TakePhoto.getValue(binding.root.context)
                )
            )

            add(
                OptionSelectionModel(
                    MediaOptions.SelectGallery.value,
                    MediaOptions.SelectGallery.getValue(binding.root.context)
                )
            )
        }

        val fragment = OptionSelectionSheetFragment(optionalList) { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                val item = optionalList[bundle.getInt("position", 0)]
                if (item.id == MediaOptions.TakePhoto.value) {
                    openCameraIntent()
                } else if (item.id == MediaOptions.SelectGallery.value) {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    resultCallbackForGallery.launch(intent)
                }
            }
        }
        fragment.show(supportFragmentManager, "OptionSelectionSheetF")
    }


    // below three method is related with taking the picture from camera
    private fun openCameraIntent() {
        val pictureIntent = Intent(
            MediaStore.ACTION_IMAGE_CAPTURE
        )
        if (pictureIntent.resolveActivity(this@ProfileVarificationActivity.packageManager) != null) {
            //Create a file to store the image
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: Exception) {
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this@ProfileVarificationActivity.applicationContext,
                    this@ProfileVarificationActivity.packageName + ".fileprovider",
                    photoFile
                )
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                resultCallbackForCamera.launch(pictureIntent)
            }
        }
    }

    @Throws(Exception::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.ENGLISH
        ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        getSharedPreference(this).edit().putString(Variables.captureImage, image.absolutePath)
            .commit()
        return image
    }

    fun getPath(uri: Uri?): String {
        var result: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = this@ProfileVarificationActivity.contentResolver.query(
            uri!!, proj, null, null, null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndex(proj[0])
                result = cursor.getString(column_index)
            }
            cursor.close()
        }
        if (result == null) {
            result = "Not found"
        }
        return result
    }

    // this will check the validations like none of the field can be the empty
    fun checkValidation(): Boolean {
        val fullname = binding!!.fullnameEdit.text.toString()
        if (TextUtils.isEmpty(fullname)) {
            showToast(this@ProfileVarificationActivity, getString(R.string.enter_full_name))
            return false
        } else if (base64 == null) {
            showToast(this@ProfileVarificationActivity, getString(R.string.select_image))
            return false
        }
        return true
    }

    fun callApi() {
        val params = JSONObject()
        try {
            params.put(
                "user_id",
                getSharedPreference(this@ProfileVarificationActivity).getString(Variables.U_ID, "")
            )
            params.put("name", binding!!.fullnameEdit.text.toString())
            params.put("ssn", binding!!.ssnEdit.text.toString())
            val file_data = JSONObject()
            file_data.put("file_data", base64)
            params.put("attachment", file_data)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        viewModel.callApiRequestVerification(params)

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

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroy() {
        mPermissionResult.unregister()
        super.onDestroy()
    }
}
