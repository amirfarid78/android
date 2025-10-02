package com.coheser.app.activitesfragments.livestreaming.activities

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceView
import com.coheser.app.Constants
import com.coheser.app.activitesfragments.livestreaming.rtc.EventHandler
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.rtc2.video.VideoEncoderConfiguration.AdvanceOptions

abstract class RtcBaseActivity : BaseActivity(), EventHandler {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerRtcEventHandler(this)
        configVideo()
        joinChannel()
    }


    private fun configVideo() {
        val configuration = VideoEncoderConfiguration(
            VideoEncoderConfiguration.VD_1920x1080,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_60,
            VideoEncoderConfiguration.STANDARD_BITRATE,
            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
        )
        configuration.advanceOptions = AdvanceOptions(
            VideoEncoderConfiguration.ENCODING_PREFERENCE.PREFER_AUTO,
            VideoEncoderConfiguration.COMPRESSION_PREFERENCE.PREFER_QUALITY,false)

        val cameraCapturerConfiguration =
            CameraCapturerConfiguration(CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT)
        cameraCapturerConfiguration.cameraFocalLengthType =
            CameraCapturerConfiguration.CAMERA_FOCAL_LENGTH_TYPE.CAMERA_FOCAL_LENGTH_ULTRA_WIDE
        rtcEngine()?.setCameraCapturerConfiguration(cameraCapturerConfiguration)
        rtcEngine()?.setCameraAutoFocusFaceModeEnabled(false)
        rtcEngine()?.setVideoEncoderConfiguration(configuration)
    }

    fun joinChannel() {
        Log.d(Constants.tag, "Check channel name : " + config().channelName)
        if (!TextUtils.isEmpty(config().channelName)) {
            rtcEngine()?.joinChannel(null, config().channelName, "", config().uid!!.toInt())
        }
    }

    protected fun prepareRtcVideo(uid: Int, local: Boolean): SurfaceView {
        val surface = SurfaceView(applicationContext)
        surface.setZOrderMediaOverlay(true)

        if (local) {
            rtcEngine()?.setupLocalVideo(
                VideoCanvas(
                    surface,
                    VideoCanvas.RENDER_MODE_HIDDEN,
                    uid
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
            rtcEngine()?.setupLocalVideo(null)
        } else {
            rtcEngine()?.setupRemoteVideo(VideoCanvas(null, VideoCanvas.RENDER_MODE_FIT, uid))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeRtcEventHandler(this)
        rtcEngine()?.leaveChannel()
    }
}
