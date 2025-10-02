package com.coheser.app.activitesfragments.livestreaming.rtc

import com.coheser.app.activitesfragments.livestreaming.StreamingConstants

class EngineConfig {
    var channelName: String? = null
    @JvmField
    var uid: String? = null
    private var mShowVideoStats = false
    var videoDimenIndex: Int = StreamingConstants.DEFAULT_PROFILE_IDX
    var mirrorLocalIndex: Int = 0
    var mirrorRemoteIndex: Int = 0
    var mirrorEncodeIndex: Int = 0


    fun ifShowVideoStats(): Boolean {
        return mShowVideoStats
    }

    fun setIfShowVideoStats(show: Boolean) {
        mShowVideoStats = show
    }
}
