package com.coheser.app.activitesfragments.shoping

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.adapter.GalleryAdapter
import com.coheser.app.activitesfragments.shoping.models.AddProductModel
import com.coheser.app.activitesfragments.shoping.models.GalleryModel
import com.coheser.app.databinding.ActivityGalleryBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GalleryActivity : AppCompatActivity() {

    var dataModel: AddProductModel? = null
    private var dataList: MutableList<GalleryModel> = ArrayList()
    var adapter: GalleryAdapter?= null
    var isSingleSelection:Boolean = false
    var alreadyAddedCount = 0
    lateinit var binding: ActivityGalleryBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_gallery)
        alreadyAddedCount = intent.getIntExtra("alreadyAddedCount",0)
        isSingleSelection = intent.getBooleanExtra("isSingleSelection",false)

        if (intent != null && intent.hasExtra("dataModel")) {
            dataModel = intent.getParcelableExtra<AddProductModel>("dataModel")!!
        }

        Log.d(Constants.tag,"cout in galery : $alreadyAddedCount")
        if (isSingleSelection)
        {
            binding.selectButton.visibility = View.GONE
        } else {
            binding.selectButton.visibility = View.VISIBLE
        }

        adapter = GalleryAdapter(dataList, object : AdapterClickListener {
            override fun onItemClick(view: View?, pos: Int, `object`: Any?) {
                if (pos==0)
                {
                    if(getSelectedImageCount() ==0)
                    {
                            openCameraIntent()
                    }
                } else
                {
                    val model = dataList.get(pos)
                    if (isSingleSelection)
                    {
                        val intent = Intent()
                        intent.putExtra("isCamera",false)
                        intent.putExtra("data", model)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                    else {

                        if (getSelectedImageCount() == 20 && !model.isSelected)
                        {
                            Functions.showToast(
                                this@GalleryActivity,
                                getString(R.string.you_can_only_choose_upto_20_photos)
                            )
                        }
                        else {

                            if (model.isSelected) {
                                model.isSelected = false
                            }
                            else {
                                model.isSelected = true
                                model.selectionCount = getSelectedList().size
                            }

                            dataList.removeAt(pos)
                            dataList.add(pos, model)
                            adapter?.notifyItemChanged(pos)

                            if (model.isSelected == false) {
                                var totalCount = getSelectedList().size
                                if (totalCount>0) {

                                    for( (index,item) in dataList.withIndex()) {

                                        if (item.isSelected) {

                                            if(item.selectionCount>model.selectionCount) {
                                                val result = item.selectionCount - 1
                                                item.selectionCount = result
                                                dataList.set(index, item)
                                            }

                                            adapter?.notifyItemChanged(index)
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        })

        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter

        binding.selectButton.setOnClickListener(DebounceClickHandler{
            if (getSelectedImageCount()==0) {
                Functions.showToast(this, getString(R.string.please_select_the_image))
            } else {
                val intent = Intent()
                intent.putExtra("isCamera",false)
                intent.putExtra("data", getSelectedList())
                setResult(RESULT_OK, intent)
                finish()
            }
        })

        binding.backBtn.setOnClickListener(DebounceClickHandler{
            finish()
        })

        loadGalleryMedia()
    }


    private val resultCallbackForCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val image = Functions.getSharedPreference(binding.root.context).getString(Variables.captureImage, "")
            Functions.printLog(Constants.tag, "imageFilePath$image")

            val matrix = Matrix()
            try {
                val exif = ExifInterface(image ?: "")
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

            val model = GalleryModel().apply {
                actualUri = selectedImage.toString()
                thumbnailUri = selectedImage.toString()
                isSelected = false
            }

            val intent = Intent()
            intent.putExtra("isCamera",true)
            intent.putExtra("data", model)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun openCameraIntent() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (pictureIntent.resolveActivity(binding.root.context.packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: Exception) {
                Functions.printLog(Constants.tag, "error: ${ex}")
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(binding.root.context.applicationContext, "${binding.root.context.packageName}.fileprovider", photoFile)
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                resultCallbackForCamera.launch(pictureIntent)
            }
        }
    }


    @Throws(Exception::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "IMG_${timeStamp}_"
        val storageDir = binding.root.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        Functions.getSharedPreference(binding.root.context).edit().putString(Variables.captureImage, image.absolutePath).commit()
        return image
    }


    private fun getSelectedImageCount(): Int {
        val selectedList: ArrayList<GalleryModel> = ArrayList()
        for (model in dataList)
        {
            if(model.isSelected)
            {
                selectedList.add(model)
            }
        }
        return (selectedList.size + alreadyAddedCount)
    }

    private fun getSelectedList(): ArrayList<GalleryModel> {
        val selectedList: ArrayList<GalleryModel> = ArrayList()
        for (model in dataList)
        {
            if(model.isSelected)
            {
                selectedList.add(model)
            }
        }
        return selectedList
    }

    private fun loadGalleryMedia() {
        var uri = Uri.parse("android.resource://${this.packageName}/${R.drawable.ic_take_photo}")
        dataList.add(0, GalleryModel().apply {
            actualUri = uri.toString()
            thumbnailUri = uri.toString()
            isSelected = false
        })
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC"
        val cursor: Cursor? = this.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )
        cursor?.use {
            val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            cursor.moveToLast()
            while (cursor.moveToPrevious()) {
                val model = GalleryModel()
                val imageId = cursor.getLong(columnIndex)
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageId
                )
                model.actualUri = contentUri.toString()
                val isSeleted=dataModel?.imagesList?.any { it.toString() == contentUri.toString() } ?: false
                if(isSeleted) {
                    model.isSelected =isSeleted
                    model.selectionCount = (getSelectedList().size+1)
                }
                dataList.add(model)
            }
        }

        adapter?.notifyDataSetChanged()
    }



}
