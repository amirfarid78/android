package com.coheser.app.activitesfragments.videorecording.videothum

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.coheser.app.R
import com.coheser.app.activitesfragments.videorecording.videothum.listener.SeekListener
import com.coheser.app.databinding.ViewTimelineBinding

class ThumbnailTimeline @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs) {

    private var frameDimension: Int = 0
    var currentProgress = 0.0
    var currentSeekPosition = 0f
    var seekListener: SeekListener? = null
    var binding:ViewTimelineBinding?=null

    var uri: Uri? = null
        set(value) {
            field = value
            field?.let {
                loadThumbnails(it)
                invalidate()
                binding?.viewSeekBar!!.setDataSource(context, it, 4)
                binding?.viewSeekBar!!.seekTo(currentSeekPosition.toInt())
            }
        }

    init {
        binding = ViewTimelineBinding.inflate(LayoutInflater.from(context), this, true)

        View.inflate(getContext(), R.layout.view_timeline, this)
        frameDimension = context.resources.getDimensionPixelOffset(R.dimen.frames_video_height)
        isFocusable = true
        isFocusableInTouchMode = true
        setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) elevation = 8f

        val margin = DisplayMetricsUtil.convertDpToPixel(16f, context).toInt()
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(margin, 0, margin, 0)
        layoutParams = params
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> handleTouchEvent(event)
            MotionEvent.ACTION_MOVE -> handleTouchEvent(event)
        }
        return true
    }

    private fun handleTouchEvent(event: MotionEvent) {
        val seekViewWidth = context.resources.getDimensionPixelSize(R.dimen.frames_video_height)

        currentSeekPosition = (Math.round(event.x) - (seekViewWidth / 2)).toFloat()

        val availableWidth = binding?.containerThumbnails?.width!! -
                (layoutParams as LinearLayout.LayoutParams).marginEnd -
                (layoutParams as LinearLayout.LayoutParams).marginStart
        if (currentSeekPosition + seekViewWidth > binding?.containerThumbnails!!.right) {
            currentSeekPosition = (binding?.containerThumbnails?.right!! - seekViewWidth).toFloat()
        } else if (currentSeekPosition < binding?.containerThumbnails!!.left) {
            currentSeekPosition = paddingStart.toFloat()
        }

        currentProgress = (currentSeekPosition.toDouble() / availableWidth.toDouble()) * 100
        binding?.containerSeekBar!!.translationX = currentSeekPosition

        binding?.viewSeekBar?.seekTo(((currentProgress * binding?.viewSeekBar!!.getDuration()) / 100).toInt())

        seekListener?.onVideoSeeked(currentProgress)
    }

    private fun loadThumbnails(uri: Uri) {
        val metaDataSource = MediaMetadataRetriever()
        metaDataSource.setDataSource(context, uri)

        val videoLength = (metaDataSource.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt() * 1000).toLong()

        var thumbnailCount = 15

        val interval = videoLength / thumbnailCount

        for (i in 0 until thumbnailCount - 1) {
            val frameTime = i * interval
            var bitmap = metaDataSource.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            try {
                val targetWidth: Int
                val targetHeight: Int
                if (bitmap!!.height > bitmap!!.width) {
                    targetHeight = frameDimension
                    val percentage = frameDimension.toFloat() / bitmap.height
                    targetWidth = (bitmap.width * percentage).toInt()
                } else {
                    targetWidth = frameDimension
                    val percentage = frameDimension.toFloat() / bitmap.width
                    targetHeight = (bitmap.height * percentage).toInt()
                }
                bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            binding?.containerThumbnails?.addView(ThumbnailView(context).apply { setImageBitmap(bitmap) })
        }
        metaDataSource.release()
    }
}