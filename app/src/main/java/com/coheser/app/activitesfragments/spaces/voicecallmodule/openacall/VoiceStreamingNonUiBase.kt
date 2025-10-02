package com.coheser.app.activitesfragments.spaces.voicecallmodule.openacall

import com.coheser.app.activitesfragments.livestreaming.rtc.EngineConfig
import com.coheser.app.activitesfragments.livestreaming.rtc.EventHandler
import com.coheser.app.activitesfragments.livestreaming.stats.StatsManager
import com.coheser.app.simpleclasses.TicTicApp
import io.agora.rtc2.IRtcEngineEventHandler.LastmileProbeResult
import io.agora.rtc2.IRtcEngineEventHandler.LocalVideoStats
import io.agora.rtc2.IRtcEngineEventHandler.RemoteAudioStats
import io.agora.rtc2.IRtcEngineEventHandler.RemoteVideoStats
import io.agora.rtc2.IRtcEngineEventHandler.RtcStats
import io.agora.rtc2.RtcEngine


open class VoiceStreamingNonUiBase(open var application: TicTicApp) : EventHandler {
    protected fun rtcEngine(): RtcEngine? {
        return application.rtcEngine()
    }


    protected fun config(): EngineConfig {
        return application.engineConfig()
    }

    protected fun statsManager(): StatsManager {
        return application.statsManager()
    }

    protected fun event(handler: EventHandler?) {
        application.registerEventHandler(handler)
    }

    protected fun removeRtcEventHandler(handler: EventHandler?) {
        if (handler != null) {
            application.removeEventHandler(handler)
        }
    }

    override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
    }

    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
    }

    override fun onLeaveChannel(stats: RtcStats) {
    }

    override fun onUserOffline(uid: Int, reason: Int) {
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
    }

    override fun onLastmileQuality(quality: Int) {
    }

    override fun onLastmileProbeResult(result: LastmileProbeResult) {
    }

    override fun onLocalVideoStats(stats: LocalVideoStats) {
    }

    override fun onRtcStats(stats: RtcStats) {
    }

    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
    }

    override fun onRemoteVideoStats(stats: RemoteVideoStats) {
    }

    override fun onRemoteAudioStats(stats: RemoteAudioStats) {
    }
}
