package com.coheser.app.activitesfragments.videorecording.videothum

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.videorecording.videothum.listener.SeekListener
import com.coheser.app.databinding.ActivityThumbyBinding
import com.coheser.app.simpleclasses.FileUtils
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables

class ThumbyActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_THUMBNAIL_POSITION = "EXTRA_THUMBNAIL_POSITION"
        const val EXTRA_URI = "EXTRA_URI"
    }

    private lateinit var videoUri: Uri

    var binding:ActivityThumbyBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_thumby)


        videoUri = intent.getParcelableExtra<Uri>(EXTRA_URI) as Uri


        binding?.saveBtn?.setOnClickListener{
            finishWithData()
        }

        binding?.cancelBtn?.setOnClickListener{
            finish()
        }



        setupVideoContent()
    }




    private fun setupVideoContent() {
        binding?.viewThumbnail!!.setDataSource(this, videoUri)
        binding?.thumbs?.seekListener = seekListener
        binding?.thumbs?.currentSeekPosition = intent.getLongExtra(EXTRA_THUMBNAIL_POSITION, 0).toFloat()
        binding?.thumbs?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding?.thumbs?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                binding?.thumbs?.uri = videoUri
            }
        })
    }

    private fun finishWithData() {
        val intent = Intent()
        intent.putExtra(EXTRA_THUMBNAIL_POSITION, (((binding?.thumbs?.currentProgress?.let {
                (binding?.viewThumbnail?.getDuration()?.div(100))?.times(
                    it
                )
            })?.toLong() ?: 1) * 1000))

        Functions.getSharedPreference(this).edit()
            .putString(Variables.selected_video_thumb, FileUtils.bitmapToBase64(binding?.viewThumbnail?.bitmap!!))
            .commit()
        setResult(RESULT_OK, intent)
        finish()
    }
    var bitmap:Bitmap?=null
    private val seekListener = object  : SeekListener {
        override fun onVideoSeeked(percentage: Double) {

           val position= ((percentage * binding?.viewThumbnail!!.getDuration()) / 100).toInt()
            Functions.printLog(Constants.tag,"Position:"+position)
            binding?.viewThumbnail?.seekTo(position!!.toInt())

        }
    }
}