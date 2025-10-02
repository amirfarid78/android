package com.coheser.app.activitesfragments.profile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.accounts.AccountUtils.updateUserModel
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.apiclasses.FileUploader
import com.coheser.app.databinding.ActivityEditProfileBinding
import com.coheser.app.enumClasses.MediaOptions
import com.coheser.app.models.OptionSelectionModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.Dialogs.showValidationMsg
import com.coheser.app.simpleclasses.FileUtils.getBitmapToUri
import com.coheser.app.simpleclasses.FirebaseFunction.uploadUserProfile
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.clearFilesCacheBeforeOperation
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getAppFolder
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getPermissionStatus
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.hideSoftKeyboard
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Functions.showPermissionSetting
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.KeyboardHeightProvider
import com.coheser.app.simpleclasses.PermissionUtils
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.trimmodule.TrimType
import com.coheser.app.trimmodule.TrimVideo
import com.coheser.app.trimmodule.TrimmerUtils
import com.coheser.app.viewModels.EditProfileViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProfileActivity : AppCompatLocaleActivity() {
    lateinit var binding: ActivityEditProfileBinding

    //for Permission taken
    var takePermissionUtils: PermissionUtils? = null
    var priviousHeight = 0
    var isActivityCallback = false

    var resultCallbackForCrop = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode == RESULT_OK) {
                    val data = result.data
                    val cropResult = CropImage.getActivityResult(data)
                    handleCrop(cropResult.uri)
                }
            }
        })

    var resultCallbackForGallery = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode == RESULT_OK) {
                    val data = result.data
                    val selectedImage = data!!.data
                    beginCrop(selectedImage)
                }
            }
        })

    var resultCallbackForCamera = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode == RESULT_OK) {
                    val image = getSharedPreference(
                        applicationContext
                    ).getString(Variables.captureImage, "")
                    printLog(Constants.tag, "imageFilePath$image")
                    val matrix = Matrix()
                    try {
                        val exif = ExifInterface(image!!)
                        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
                        when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val selectedImage = Uri.fromFile(File(image))
                    beginCrop(selectedImage)
                }
            }
        })


    private val mPermissionImageResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        { result: Map<String, Boolean> ->
            var allPermissionClear = true
            val blockPermissionCheck: MutableList<String> = ArrayList()
            for (key in result.keys) {
                if (!result[key]!!) {
                    allPermissionClear = false
                    blockPermissionCheck.add(
                        getPermissionStatus(
                            this@EditProfileActivity,
                            key
                        )
                    )
                }
            }
            if (blockPermissionCheck.contains("blocked")) {
                showPermissionSetting(
                    this@EditProfileActivity,
                    getString(R.string.we_need_storage_and_camera_permission_for_upload_profile_pic)
                )
            } else if (allPermissionClear) {
                openBottomSheetforImage()
            }
        })


    private val mPermissionVideoResult =
        registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions(),
            { result: Map<String, Boolean> ->
                    var allPermissionClear = true
                    val blockPermissionCheck: MutableList<String> = java.util.ArrayList()
                    for (key in result.keys) {
                        if (!result[key]!!) {
                            allPermissionClear = false
                            blockPermissionCheck.add(
                                getPermissionStatus(
                                    this@EditProfileActivity, key
                                )
                            )
                        }
                    }
                    if (blockPermissionCheck.contains("blocked")) {
                        showPermissionSetting(
                            this@EditProfileActivity,
                            getString(R.string.we_need_storage_and_camera_permission_for_upload_profile_pic)
                        )
                    } else if (allPermissionClear) {
                        openBottomSheetforGif()
                    }
                }
            )


    private val viewModel: EditProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)

        binding.viewModel=viewModel
        binding.lifecycleOwner = this

        setupScreenData()
        viewModel.getUserDetails()

        actionControl()
        setObserveAble()
    }


    fun setObserveAble(){

        viewModel.editProfileLiveData.observe(this,{
            when(it){
                is ApiResponce.Loading->{
                    showLoader(this@EditProfileActivity, false, false)

                }

                is ApiResponce.Success ->{
                    cancelLoader()
                    it.data.let {
                        it?.let { it1 -> updateUserModel(it1) }
                        val editor = getSharedPreference(
                            binding.root.context
                        ).edit()
                        var u_name = it?.username
                        if (!u_name!!.contains("@")) u_name = "@$u_name"
                        editor.putString(Variables.U_NAME, u_name)
                        editor.putString(Variables.F_NAME, it?.first_name)
                        editor.putString(Variables.L_NAME, it?.last_name)
                        editor.putString(Variables.U_BIO, it?.bio)
                        editor.putString(Variables.U_LINK, it?.website)
                        editor.putString(Variables.GENDER, it?.gender)
                        editor.putString(Variables.U_PIC, it?.getProfilePic())
                        editor.commit()
                        isActivityCallback = true
                        onBackPressed()
                    }
                }

                is ApiResponce.Error ->{
                    cancelLoader()
                    if (it.message != null) {
                        showToast(binding.root.context,it.message)
                    }
                }
            }
        })





        viewModel.editProfileLiveData.observe(this,{
            when(it){

                is ApiResponce.Success ->{
                    it.data.let {

                        it?.let { it1 -> updateUserModel(it1) }
                        val editor = getSharedPreference(
                            binding.root.context
                        ).edit()
                        var u_name = it?.username
                        if (!u_name!!.contains("@")) u_name = "@$u_name"
                        editor.putString(Variables.U_NAME, u_name)
                        editor.putString(Variables.F_NAME, it?.first_name)
                        editor.putString(Variables.L_NAME, it?.last_name)
                        editor.putString(Variables.U_BIO, it?.bio)
                        editor.putString(Variables.U_LINK, it?.website)
                        editor.putString(Variables.GENDER, it?.gender)
                        editor.commit()
                        setupScreenData()

                    }
                }

                else -> {}
            }
        })

    }


    private fun actionControl() {
        binding.ivProfile.setOnClickListener {
            takePermissionUtils = PermissionUtils(this@EditProfileActivity, mPermissionImageResult)
            if (takePermissionUtils!!.isStorageCameraPermissionGranted) {
                openBottomSheetforImage()
            } else {
                takePermissionUtils!!.showStorageCameraPermissionDailog(getString(R.string.we_need_storage_and_camera_permission_for_upload_profile_pic))
            }
        }

        binding.ivProfileVideo.setOnClickListener {
            takePermissionUtils =
                PermissionUtils(this@EditProfileActivity, mPermissionVideoResult)
            if (takePermissionUtils!!.isStoragePermissionGranted) {
                openBottomSheetforGif()
            } else {
                takePermissionUtils!!.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_for_upload_profile_video))
            }
        }

        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.tvSave.setOnClickListener {
            if (checkValidation()) {
                callApiForEditProfile(false, "")
            }
        }
        setKeyboardListener()


        // add the input filter to eidt text of username
        val username_filters = arrayOfNulls<InputFilter>(1)
        username_filters[0] = LengthFilter(Constants.USERNAME_CHAR_LIMIT)
        binding.etUsername.filters = username_filters
        binding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })


        // add the input filter to edittext of userbio
        val filters = arrayOfNulls<InputFilter>(1)
        filters[0] = LengthFilter(Constants.BIO_CHAR_LIMIT)
        binding.etUserBio.filters = filters
    }

    private fun openBottomSheetforImage() {
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

            add(
                OptionSelectionModel(
                    MediaOptions.SelectGallery.value,
                    MediaOptions.SelectGallery.getValue(binding.root.context)
                )
            )

            add(
                OptionSelectionModel(
                    MediaOptions.ViewPhoto.value,
                    MediaOptions.ViewPhoto.getValue(binding.root.context)
                )
            )

            add(OptionSelectionModel( MediaOptions.RemovePhoto.value,
                MediaOptions.RemovePhoto.getValue(binding.root.context)))
        }


        val fragment = OptionSelectionSheetFragment(optionalList) { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                val item = optionalList[bundle.getInt("position", 0)]
                if (item.id == MediaOptions.TakePhoto.value) {
                    openCameraIntent()
                } else if (item.id == MediaOptions.SelectGallery.value) {
                    openGalleryIntent()
                } else if (item.id == MediaOptions.ViewPhoto.value) {
                    openProfileFullview(false)
                } else if (item.id == MediaOptions.RemovePhoto.value) {
                    callApiForEditProfile(true, "")
                }
            }
        }
        fragment.show(supportFragmentManager, "OptionSelectionSheetF")
    }

    private fun openBottomSheetforGif() {

        val optionalList = ArrayList<OptionSelectionModel>().apply {
            add(OptionSelectionModel(MediaOptions.ChangeVideo.value,
                MediaOptions.ChangeVideo.getValue(binding.root.context)))

            add(OptionSelectionModel(MediaOptions.RemoveVideo.value,
                MediaOptions.RemoveVideo.getValue(binding.root.context)))

            add(OptionSelectionModel(MediaOptions.WatchVideo.value,
                MediaOptions.WatchVideo.getValue(binding.root.context)))
        }

        val fragment = OptionSelectionSheetFragment(optionalList, { bundle ->
                if (bundle.getBoolean("isShow", false)) {
                    val item = optionalList[bundle.getInt("position", 0)]
                    if (item.id == MediaOptions.ChangeVideo.value) {
                        pickVideoFromGallery()
                    } else if (item.id == MediaOptions.RemoveVideo.value) {
                        updateEmptyProfile()
                    } else if (item.id == MediaOptions.WatchVideo.value) {
                        openProfileFullview(true)
                    }
                }
            })
        fragment.show(supportFragmentManager, "OptionSelectionSheetF")
    }



    private fun openGalleryIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultCallbackForGallery.launch(intent)
    }

    private fun openProfileFullview(isGif: Boolean) {
        var mediaUrl: String? = ""
        mediaUrl = if (isGif) {
            getSharedPreference(binding.root.context)
                .getString(Variables.U_GIF, "")
        } else {
            getSharedPreference(binding.root.context)
                .getString(Variables.U_PIC, "")
        }
        val intent = Intent(binding.root.context, SeeFullImageActivity::class.java)
        intent.putExtra("image_url", mediaUrl)
        intent.putExtra("isGif", isGif)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun setupScreenData() {
        binding.etUsername.setText(
            getSharedPreference(binding.root.context).getString(
                Variables.U_NAME,
                ""
            )
        )
        binding.etFirstname.setText(
            getSharedPreference(binding.root.context).getString(
                Variables.F_NAME,
                ""
            )
        )
        binding.etLastname.setText(
            getSharedPreference(binding.root.context).getString(
                Variables.L_NAME,
                ""
            )
        )
        val pic = getSharedPreference(binding.root.context).getString(Variables.U_PIC, "")
        binding.ivProfile.controller =
            frescoImageLoad(pic, R.drawable.ic_picture_placeholder, binding.ivProfile, false)
        val videoGif = getSharedPreference(
            binding.root.context
        ).getString(Variables.U_GIF, "")
        binding.ivProfileVideo.controller = frescoImageLoad(
            videoGif,
            R.drawable.ic_video_placeholder,
            binding.ivProfileVideo,
            true
        )
        binding.etWebsite.setText(
            getSharedPreference(binding.root.context).getString(
                Variables.U_LINK,
                ""
            )
        )
        binding.etUserBio.setText(
            getSharedPreference(binding.root.context).getString(
                Variables.U_BIO,
                ""
            )
        )
    }

    // open the intent for get the video from gallery
    fun pickVideoFromGallery() {
        val fileTrim = File(getAppFolder(binding.root.context) + Variables.gallery_trimed_video)
        val fileFilter = File(getAppFolder(binding.root.context) + Variables.output_filter_file)
        clearFilesCacheBeforeOperation(fileTrim, fileFilter)

        Constants.RECORDING_DURATION = 10 * 1000
        val intent = Intent()
        intent.setType("video/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        takeOrSelectVideoResultLauncher.launch(Intent.createChooser(intent, "Select Video"))
    }

    // start trimming activity
    var takeOrSelectVideoResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
                override fun onActivityResult(result: ActivityResult?) {
                    if (result?.resultCode == RESULT_OK) {
                        val data = result.data
                        if (TrimmerUtils.getDuration(
                                this@EditProfileActivity,
                                data!!.data
                            ) < Constants.MIN_TRIM_TIME
                        ) {
                            Toast.makeText(
                                this@EditProfileActivity,
                                binding.root.context.getString(R.string.video_must_be_larger_then_second),
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                        if (data.data != null) {
                            openTrimActivity(data.data.toString())
                        }
                    }
                }
            })


    private fun openTrimActivity(data: String) {
        TrimVideo.activity(data)
            .setTrimType(TrimType.MIN_MAX_DURATION)
            .setMinToMax(
                Constants.MIN_TRIM_TIME.toLong(),
                (Constants.RECORDING_DURATION / 1000).toLong()
            )
            .setMinDuration(Constants.MAX_TRIM_TIME.toLong())
            .setTitle("") //seconds
            .setMaxTimeCheck(Constants.RECORDING_DURATION)
            .start(this, videoTrimResultLauncher)
    }

    var videoTrimResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
                override fun onActivityResult(result: ActivityResult?) {
                    if (result?.resultCode == RESULT_OK) {
                        val uri = Uri.parse(
                            TrimVideo.getTrimmedVideoPath(
                                result.data,
                                Variables.gallery_trimed_video
                            )
                        )
                        val filepath = uri.toString()
                        uploadProfileVideo(filepath)
                    } else Log.d(Constants.tag, "videoTrimResultLauncher data is null")
                }
            })

    private fun uploadProfileVideo(filepath: String) {
        showLoader(this@EditProfileActivity, false, false)
        val userId = getSharedPreference(binding.root.context).getString(Variables.U_ID, "")
        val fileUploader = FileUploader(
            File(filepath),
            applicationContext, userId
        )
        fileUploader.SetCallBack(object : FileUploader.FileUploaderCallback {
            override fun onError() {
                //send error broadcast
               cancelLoader()
            }

            override fun onFinish(responses: String) {
               cancelLoader()
                printLog(Constants.tag, responses)
                try {
                    val jsonObject = JSONObject(responses)
                    val code = jsonObject.optInt("code", 0)
                    val msg = jsonObject.getJSONObject("msg")
                    if (code == 200) {
                        val userDetailModel = getUserDataModel(msg.optJSONObject("User"))
                        val editor = getSharedPreference(binding.root.context).edit()
                        editor.putString(Variables.U_GIF, userDetailModel.getProfileGif())
                        editor.commit()
                        isActivityCallback = true
                        setupScreenData()
                    }
                } catch (e: java.lang.Exception) {
                    printLog(
                        Constants.tag,
                        "Exception: $e"
                    )
                }
            }

            override fun onProgressUpdate(currentpercent: Int, totalpercent: Int, msg: String) {
                //send progress broadcast
                if (currentpercent > 0) {
                }
            }
        })
    }

    private fun setKeyboardListener() {
        val keyboardHeightProvider = KeyboardHeightProvider(this@EditProfileActivity)
        keyboardHeightProvider.setKeyboardHeightObserver { height, orientation ->
            printLog(Constants.tag, "" + height)
            if (height < 0) {
                priviousHeight = Math.abs(height)
            }
            val main_layout = findViewById<LinearLayout>(R.id.main_layout)
            val params = FrameLayout.LayoutParams(main_layout.width, main_layout.height)
            params.bottomMargin = height + priviousHeight
            main_layout.layoutParams = params
        }
    }

    // below three method is related with taking the picture from camera
    private fun openCameraIntent() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (pictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: Exception) {
                printLog(Constants.tag, "error$ex")
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    binding.root.context.applicationContext,
                    "$packageName.fileprovider",
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
            Locale.US
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

    // this will check the validations like none of the field can be the empty
    fun checkValidation(): Boolean {
        val uname = binding.etUsername.text.toString()
        val firstname = binding.etFirstname.text.toString()
        val lastname = binding.etLastname.text.toString()
        if (TextUtils.isEmpty(uname)) {
            showValidationMsg(
                this@EditProfileActivity,
                binding.scrollContainer,
                binding.root.context.getString(R.string.please_correct_user_name)
            )
            return false
        } else if (uname.length < 4 || uname.length > 14) {
            showValidationMsg(
                this@EditProfileActivity,
                binding.scrollContainer,
                binding.root.context.getString(R.string.username_length_between_valid)
            )
            return false
        } else if (!UserNameTwoCaseValidate(uname)) {
            showValidationMsg(
                this@EditProfileActivity,
                binding.scrollContainer,
                binding.root.context.getString(R.string.username_must_contain_alphabet)
            )
            return false
        } else if (TextUtils.isEmpty(firstname)) {
            showValidationMsg(
                this@EditProfileActivity,
                binding.scrollContainer,
                binding.root.context.getString(R.string.please_enter_first_name)
            )
            return false
        } else if (TextUtils.isEmpty(lastname)) {
            showValidationMsg(
                this@EditProfileActivity,
                binding.scrollContainer,
                binding.root.context.getString(R.string.please_enter_last_name)
            )
            return false
        }
        return true
    }

    private fun UserNameTwoCaseValidate(name: String): Boolean {
        val let_p =
            Pattern.compile("[a-z]", Pattern.CASE_INSENSITIVE)
        val let_m = let_p.matcher(name)
        return let_m.find()
    }

    private fun beginCrop(source: Uri?) {
        val intent = CropImage.activity(source).setCropShape(CropImageView.CropShape.OVAL)
            .setAspectRatio(1, 1).getIntent(this@EditProfileActivity)
        resultCallbackForCrop.launch(intent)
    }

    // get the image uri after the image crope
    private fun handleCrop(userimageuri: Uri) {
        var imageStream: InputStream? = null
        try {
            imageStream = contentResolver.openInputStream(userimageuri)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        val imagebitmap = BitmapFactory.decodeStream(imageStream)
        val path = userimageuri.path
        val matrix = Matrix()
        var exif: ExifInterface? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                exif = ExifInterface(path!!)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    else -> {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val fullBitmap = Bitmap.createBitmap(
            imagebitmap,
            0,
            0,
            imagebitmap.width,
            imagebitmap.height,
            matrix,
            true
        )
        val out = ByteArrayOutputStream()
        fullBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        val fullImagePath = getBitmapToUri(this@EditProfileActivity, fullBitmap, "fullBitmap.png")
        uploadProfileImages(fullImagePath)
    }

    private fun uploadProfileImages(fullImagePath: File?) {
        showLoader(this@EditProfileActivity, false, false)

        uploadUserProfile(this@EditProfileActivity, Uri.fromFile(fullImagePath)) { s ->
            cancelLoader()
            callApiForEditProfile(true, s)
        }
    }


    // this will update the latest info of user in database
    fun updateEmptyProfile() {
        val parameters = JSONObject()
        try {
            parameters.put("user_id", getSharedPreference(binding.root.context).getString(Variables.U_ID, "0"))
            parameters.put("profile_gif", "")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        showLoader(this@EditProfileActivity, false, false)
        VolleyRequest.JsonPostRequest(
            this@EditProfileActivity, ApiLinks.editProfile, parameters, getHeaders(
                this
            )
        ) { resp ->
            checkStatus(this@EditProfileActivity, resp)
            cancelLoader()
            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                val msg = response.optJSONObject("msg")
                if (code == "200") {
                    val userDetailModel = getUserDataModel(msg.optJSONObject("User"))

                    val editor =
                        getSharedPreference(binding.root.context)
                            .edit()
                    editor.putString(Variables.U_GIF, "" + userDetailModel.getProfileGif())
                    editor.commit()
                    isActivityCallback = true

                    setupScreenData()
                } else {
                    showToast(
                        binding.root.context,
                        response.optString("msg")
                    )
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }


    // this will update the latest info of user in database
    fun callApiForEditProfile(isChangePic: Boolean, profilePic: String?) {
        showLoader(this@EditProfileActivity, false, false)
        val uname = binding.etUsername.text.toString().lowercase(Locale.getDefault())
            .replace("\\s".toRegex(), "")
        val parameters = JSONObject().apply {
            try {
                put("auth_token", getSharedPreference(binding.root.context).getString(Variables.AUTH_TOKEN, "0"))
                if (isChangePic) {
                    put("profile_pic", profilePic)
                } else {
                    put("username", uname.replace("@".toRegex(), ""))
                    put("first_name", binding.etFirstname.text.toString())
                    put("last_name", binding.etLastname.text.toString())
                    put("gender", "")
                    put("website", binding.etWebsite.text.toString())
                    put("bio", binding.etUserBio.text.toString())
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewModel.editProfile(parameters)



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
        mPermissionImageResult?.unregister()
        hideSoftKeyboard(this@EditProfileActivity)
        super.onDestroy()
    }
}
