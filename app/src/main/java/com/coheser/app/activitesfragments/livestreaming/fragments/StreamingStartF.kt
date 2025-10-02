package com.coheser.app.activitesfragments.livestreaming.fragments

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.activitesfragments.profile.OptionSelectionSheetFragment
import com.coheser.app.activitesfragments.shoping.SelectProductA
import com.coheser.app.activitesfragments.shoping.adapter.StreamingProductsAdapter
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.databinding.FragmentStreamingStartBinding
import com.coheser.app.enumClasses.MediaOptions
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.OptionSelectionModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity.RESULT_OK
import com.coheser.app.simpleclasses.FileUtils
import com.coheser.app.simpleclasses.FirebaseFunction
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.TicTicApp
import com.coheser.app.simpleclasses.TimerDialog
import com.coheser.app.simpleclasses.Variables
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.volley.plus.interfaces.Callback
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StreamingStartF(val callBack: FragmentCallBack) : Fragment() {

    lateinit var model:LiveUserModel
    lateinit var binding:FragmentStreamingStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            model=it.getParcelable("data")!!

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_streaming_start, container, false)

        binding.userImage.controller = Functions.frescoImageLoad(model.userPicture, binding.userImage, false)

        binding.flipCameraBtn.setOnClickListener{
            val bundle=Bundle()
            bundle.putString("type","flipCamera")
            callBack.onResponce(bundle)
        }

        binding.enhanceBtn.setOnClickListener{
            val bundle=Bundle()
            bundle.putString("type","enhance")
            callBack.onResponce(bundle)
        }

        binding.tagBtn.setOnClickListener{
            val intent = Intent(requireActivity(), SelectProductA::class.java)
            intent.putExtra("isMultiple", true)
            resultCallback.launch(intent)
        }

        binding.settingBtn.setOnClickListener{
            val fragment=SettingBottonSheetFragment.newInstance(model,object :FragmentCallBack{
                override fun onResponce(bundle: Bundle?) {
                    if(bundle!=null){
                        model=bundle.getParcelable("data")!!
                    }
                }
            })
            fragment.show(childFragmentManager,"SettingBottonSheetFragment")
        }

        binding.GoLiveBtn.setOnClickListener{

            if(Functions.getSharedPreference(requireContext()).getBoolean(Variables.isLiveRuleShow,false)){
                startLiveTimer()
            }
            else {
                val f = LiveRulesBottomF.newInstance(object : FragmentCallBack {
                    override fun onResponce(bundle: Bundle?) {
                        if (bundle != null) {
                            Functions.getSharedPreference(requireContext()).edit().putBoolean(Variables.isLiveRuleShow, true).commit()
                            startLiveTimer()
                        }
                    }
                })
                f.show(childFragmentManager, "LiveRulesBottomF")
            }

        }

        binding.goalLayout.setOnClickListener{
            val f = StreamingGoalFragment.newInstance(object : FragmentCallBack{
                override fun onResponce(bundle: Bundle?) {
                    if(bundle!=null){
                        model.setGoalStream=bundle.getParcelable("data")!!
                        model.setGoalStream?.userId=model.userId
                        model.setGoalStream?.userName=model.userName
                        model.setGoalStream?.userPicture=model.userPicture
                    }
                }
            })
            f.show(childFragmentManager, "ShowOtherProfileBottomF")
        }

        binding.wishListLayout.setOnClickListener{
            val f = WishListBottomF.newInstance(model,WishListBottomF.fromSelection,object : FragmentCallBack{
                override fun onResponce(bundle: Bundle?) {
                    if(bundle!=null){
                        model=bundle.getParcelable("data")!!
                    }
                }
            })
            f.show(childFragmentManager, "ShowOtherProfileBottomF")
        }

        binding.changeBtn.setOnClickListener{
            openBottomSheetforImage()
        }

        return binding.root
    }


    fun goLive(){
        val bundle=Bundle()
        bundle.putString("type","goLive")
        callBack.onResponce(bundle)
        parentFragmentManager.popBackStack()
    }

    var productList: ArrayList<ProductModel> = ArrayList()
    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data

            val model = data!!.getParcelableExtra<ProductModel>("data")!!
            productList.add(0,model)
            initproductAdapter()
        }
    }


    var productAdapter: StreamingProductsAdapter?=null
    fun initproductAdapter() {
        if(productAdapter==null) {
            val linearLayoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.reyclerView.layoutManager = linearLayoutManager
            binding.reyclerView.setHasFixedSize(true)

            val snapHelper: SnapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(binding.reyclerView)

            productAdapter = StreamingProductsAdapter(
                requireContext(),
                productList!!,
                AdapterClickListener { view, pos, `object` ->
                    val model = `object` as ProductModel
                })
            binding.reyclerView.adapter = productAdapter
        }else{
            productAdapter?.notifyDataSetChanged()
        }
    }

    fun startLiveTimer() {
        binding.selectionLayout.visibility = View.GONE
        model.description = binding.descriptionTxt.text.toString()
        model.productsList=productList
        val timerDialog = TimerDialog(requireContext())
        timerDialog.setTimerCallback(object : TimerDialog.TimerCallback {
            override fun onTimerFinished() {
                goLive()
            }
        })
        timerDialog.show()
    }


    private fun openBottomSheetforImage() {
        val optionalList: java.util.ArrayList<OptionSelectionModel> = java.util.ArrayList<OptionSelectionModel>()
        optionalList.add(OptionSelectionModel(MediaOptions.TakePhoto.value,
            MediaOptions.TakePhoto.getValue(requireContext())))
        optionalList.add(OptionSelectionModel(MediaOptions.SelectGallery.value,
            MediaOptions.SelectGallery.getValue(requireContext())))

        val fragment = OptionSelectionSheetFragment(optionalList,
            FragmentCallBack { bundle ->
                if (bundle.getBoolean("isShow", false)) {
                    val item: OptionSelectionModel = optionalList[bundle.getInt("position", 0)]
                    if (item.getId().equals(MediaOptions.TakePhoto.value)) {
                        openCameraIntent()
                    } else if (item.getId().equals(MediaOptions.SelectGallery.value)) {
                        openGalleryIntent()
                    }
                }
            })
        fragment.show(childFragmentManager, "OptionSelectionSheetF")
    }

    private fun beginCrop(source: Uri) {
        val intent = CropImage.activity(source).setCropShape(CropImageView.CropShape.OVAL)
            .setAspectRatio(1, 1).getIntent(requireActivity())
        resultCallbackForCrop.launch(intent)
    }

    // get the image uri after the image crope
    private fun handleCrop(userimageuri: Uri) {
        var imageStream: InputStream? = null
        try {
            imageStream = requireActivity().getContentResolver().openInputStream(userimageuri)
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

        val fullImagePath: File = FileUtils.getBitmapToUri(requireActivity(), fullBitmap, "fullBitmap.png")!!

        FirebaseFunction.uploadImageToFirebase(
            requireActivity(), Uri.fromFile(fullImagePath),
            Callback { s ->
                model.userPicture=s
                binding.userImage.controller =
                    Functions.frescoImageLoad(model.userPicture, binding.userImage, false)
                binding.userImage.setController(
                    Functions.frescoImageLoad(
                        model.userPicture,
                        binding.userImage,
                        false
                    )
                )
            })
    }
//
//    // below three method is related with taking the picture from camera
    private fun openCameraIntent() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (pictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: Exception) {
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    binding.root.context.applicationContext,
                    requireActivity().getPackageName() + ".fileprovider",
                    photoFile
                )
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                resultCallbackForCamera.launch(pictureIntent)
            }
        }
    }

    @Throws(Exception::class)
    private fun createImageFile(): File {
        val timeStamp =
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.ENGLISH
            ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir: File = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )

        Functions.getSharedPreference(requireContext()).edit()
            .putString(Variables.captureImage, image.absolutePath).commit()

        return image
    }

    private fun openGalleryIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultCallbackForGallery.launch(intent)
    }

    var resultCallbackForCrop: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    val cropResult = CropImage.getActivityResult(data)
                    handleCrop(cropResult.uri)
                }
            }
        })

    var resultCallbackForGallery: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    val selectedImage = data!!.data
                    beginCrop(selectedImage!!)
                }
            }
        })

    // create a temp image file
    var resultCallbackForCamera: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if (result.resultCode == RESULT_OK) {
                    val imageFilePath = Functions.getSharedPreference(TicTicApp.appLevelContext)
                        .getString(Variables.captureImage, "")

                    val matrix = Matrix()
                    try {
                        val exif = ExifInterface(imageFilePath!!)
                        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
                        when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    val selectedImage = (Uri.fromFile(File(imageFilePath)))
                    beginCrop(selectedImage)
                }
            }
        })

    companion object {
        @JvmStatic
        fun newInstance(model: LiveUserModel,callBack: FragmentCallBack) =
            StreamingStartF(callBack).apply {
                arguments = Bundle().apply {
                    putParcelable("data", model)
                }
        }
    }

}