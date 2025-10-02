package com.coheser.app.activitesfragments.spaces.voicecallmodule.openacall

import android.util.Log
import com.coheser.app.Constants
import com.coheser.app.activitesfragments.livestreaming.rtc.EventHandler
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.TicTicApp

class VoiceStreamingNonUiChat(override var application: TicTicApp) :
    VoiceStreamingNonUiBase(application), EventHandler {
    @Volatile
    private var mAudioMuted = true

    @Volatile
    private var mAudioRouting = -1

    var channelName: String? = null
    var uid: String? = null
    var isCallStart: Boolean = false


    fun setChannelNameAndUid(channelName: String?, userId: String?) {
        this.channelName = channelName
        this.uid = userId
        printLog(Constants.tag, "channelName:" + this.channelName + " UserID:" + this.uid)
        config().uid = userId
    }

    fun startStream(voiceControler: FragmentCallBack?) {
        initConfiguration()
    }

    protected fun initConfiguration() {
        isCallStart = true
        event(this)


        rtcEngine()!!.disableVideo()

        rtcEngine()!!.setDefaultAudioRoutetoSpeakerphone(true)
        rtcEngine()!!.adjustRecordingSignalVolume(100)
        rtcEngine()!!.adjustPlaybackSignalVolume(100)
        rtcEngine()!!.adjustAudioMixingVolume(100)

        rtcEngine()!!.joinChannel(null, channelName, "OpenVCall", config().uid!!.toInt())
        Log.d(Constants.tag, "Connected Channel ID: $channelName")

        onEnableSpeakerSwitch()
    }

    protected fun removeConfiguration() {
        isCallStart = false
        doLeaveChannel()
        removeRtcEventHandler(this)
    }


    private fun doLeaveChannel() {
        rtcEngine()!!.leaveChannel()
    }

    fun quitCall() {
        printLog(Constants.tag, "quitCall ")
        removeConfiguration()
    }

    fun muteVoiceCall() {
        printLog(Constants.tag, "muteVoiceCall")
        mAudioMuted = true
        rtcEngine()!!.setClientRole(io.agora.rtc2.Constants.CLIENT_ROLE_AUDIENCE)
        rtcEngine()!!.muteLocalAudioStream(mAudioMuted)

        if (mAudioRouting == 0) {
            onDisableSpeakerSwitch()
        } else {
            onEnableSpeakerSwitch()
        }
    }

    fun enableVoiceCall() {
        printLog(Constants.tag, "enableVoiceCall")
        mAudioMuted = false
        rtcEngine()!!.setClientRole(io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER)
        rtcEngine()!!.muteLocalAudioStream(mAudioMuted)

        if (mAudioRouting == 0) {
            onDisableSpeakerSwitch()
        } else {
            onEnableSpeakerSwitch()
        }
    }


    fun ismAudioMuted(): Boolean {
        return mAudioMuted
    }

    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
        val msg = "onJoinChannelSuccess $channel=>  UserId:$uid => $elapsed"
        printLog(Constants.tag, msg)
        rtcEngine()!!.muteLocalAudioStream(mAudioMuted)
    }


    override fun onUserOffline(uid: Int, reason: Int) {
        val msg = "onUserOffline $uid $reason"
        printLog(Constants.tag, msg)
    }


    fun notifyHeadsetPlugged(routing: Int) {
        printLog(Constants.tag, "notifyHeadsetPlugged $routing")
        mAudioRouting = routing
        if (mAudioRouting == 0) {
            onDisableSpeakerSwitch()
        } else {
            onEnableSpeakerSwitch()
        }
    }

    fun onEnableSpeakerSwitch() {
        rtcEngine()!!.setEnableSpeakerphone(true)
    }

    fun onDisableSpeakerSwitch() {
        rtcEngine()!!.setEnableSpeakerphone(false)
    }
}
