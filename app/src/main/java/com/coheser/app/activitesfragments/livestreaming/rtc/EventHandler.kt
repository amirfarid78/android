package com.coheser.app.activitesfragments.livestreaming.rtc

import io.agora.rtc2.IRtcEngineEventHandler.LastmileProbeResult
import io.agora.rtc2.IRtcEngineEventHandler.LocalVideoStats
import io.agora.rtc2.IRtcEngineEventHandler.RemoteAudioStats
import io.agora.rtc2.IRtcEngineEventHandler.RemoteVideoStats
import io.agora.rtc2.IRtcEngineEventHandler.RtcStats

interface EventHandler {
    fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int)

    fun onLeaveChannel(stats: RtcStats)

    fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int)

    fun onUserOffline(uid: Int, reason: Int)

    fun onUserJoined(uid: Int, elapsed: Int)

    fun onLastmileQuality(quality: Int)

    fun onLastmileProbeResult(result: LastmileProbeResult)

    fun onLocalVideoStats(stats: LocalVideoStats)

    fun onRtcStats(stats: RtcStats)

    fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int)

    fun onRemoteVideoStats(stats: RemoteVideoStats)

    fun onRemoteAudioStats(stats: RemoteAudioStats)


    companion object {
        const val EVENT_TYPE_ON_USER_AUDIO_MUTED: Int = 7

        const val EVENT_TYPE_ON_SPEAKER_STATS: Int = 8

        const val EVENT_TYPE_ON_AGORA_MEDIA_ERROR: Int = 9

        const val EVENT_TYPE_ON_AUDIO_QUALITY: Int = 10

        const val EVENT_TYPE_ON_APP_ERROR: Int = 13

        const val EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED: Int = 18
    }
}
