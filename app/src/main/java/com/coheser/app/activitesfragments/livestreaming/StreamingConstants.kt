package com.coheser.app.activitesfragments.livestreaming

import io.agora.rtc2.video.BeautyOptions
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.rtc2.video.VideoEncoderConfiguration.VideoDimensions

object StreamingConstants {

    const val streamTypeMulticast: String = "multicast"

    const val liveStreamingUsers="LiveStreamingUsers"
    const val coinsStream="CoinsStream"

    val DEFAULT_BEAUTY_OPTIONS: BeautyOptions = BeautyOptions()
    const val PREF_NAME: String = "io.agora.openlive"
    const val DEFAULT_PROFILE_IDX: Int = 5
    const val PREF_RESOLUTION_IDX: String = "pref_profile_index"
    const val PREF_ENABLE_STATS: String = "pref_enable_stats"

    const val PREF_MIRROR_LOCAL: String = "pref_mirror_local"
    const val PREF_MIRROR_REMOTE: String = "pref_mirror_remote"
    const val PREF_MIRROR_ENCODE: String = "pref_mirror_encode"
    var VIDEO_DIMENSIONS: Array<VideoDimensions> = arrayOf(
        VideoEncoderConfiguration.VD_320x240,
        VideoEncoderConfiguration.VD_480x360,
        VideoEncoderConfiguration.VD_640x360,
        VideoEncoderConfiguration.VD_640x480,
        VideoEncoderConfiguration.VD_960x540,
        VideoEncoderConfiguration.VD_1280x720,
        VideoEncoderConfiguration.VD_1920x1080,
        VideoEncoderConfiguration.VD_2540x1440,
        VideoEncoderConfiguration.VD_3840x2160
    )

}
