package com.coheser.app.activitesfragments.livestreaming.activities

import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import com.coheser.app.Constants
import com.coheser.app.activitesfragments.livestreaming.StreamingConstants
import com.coheser.app.activitesfragments.livestreaming.rtc.EventHandler
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Variables
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration


abstract class RtcBaseMultiviewActivity : BaseActivity(), EventHandler {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun refreshStreamingConnection(channelName: String?) {
        config().uid = getSharedPreference(this)
            .getString(Variables.U_ID, "")
        config().channelName = channelName
        registerRtcEventHandler(this)
        configVideo()
        joinChannel()
    }

    fun removeStreamingConnection() {
        removeRtcEventHandler(this)
        rtcEngine()?.leaveChannel()
    }

    val channelName: String
        get() = config().channelName!!

    private fun configVideo() {
        val configuration = VideoEncoderConfiguration(
            StreamingConstants.VIDEO_DIMENSIONS[config().videoDimenIndex],
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
            VideoEncoderConfiguration.STANDARD_BITRATE,
            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        )
        rtcEngine()?.setVideoEncoderConfiguration(configuration)
    }

    private fun joinChannel() {
        rtcEngine()?.joinChannel(null, config().channelName, "", config().uid!!.toInt())
    }

    protected fun prepareRtcVideo(uid: Int, local: Boolean): SurfaceView {
        val surface = SurfaceView(applicationContext)
        surface.setZOrderMediaOverlay(true)

        if (local) {
            rtcEngine()?.setupLocalVideo(
                VideoCanvas(
                    surface,
                    VideoCanvas.RENDER_MODE_HIDDEN,
                    0
                )
            )
        } else {
            rtcEngine()?.setupRemoteVideo(
                VideoCanvas(
                    surface,
                    VideoCanvas.RENDER_MODE_HIDDEN,
                    uid
                )
            )
        }
        return surface
    }

    protected fun removeRtcVideo(uid: Int, local: Boolean) {
        if (local) {
            Log.d(Constants.tag, "local True: ")
            rtcEngine()?.setupLocalVideo(null)
        } else {
            Log.d(Constants.tag, "local false: ")
            rtcEngine()?.setupRemoteVideo(VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeRtcEventHandler(this)
        rtcEngine()?.leaveChannel()
    }
}
