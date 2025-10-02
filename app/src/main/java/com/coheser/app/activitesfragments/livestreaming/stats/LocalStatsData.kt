package com.coheser.app.activitesfragments.livestreaming.stats

import java.util.Locale

class LocalStatsData : StatsData() {
    var lastMileDelay: Int = 0
    var videoSendBitrate: Int = 0
    var videoRecvBitrate: Int = 0
    var audioSendBitrate: Int = 0
    var audioRecvBitrate: Int = 0
    var cpuApp: Double = 0.0
    var cpuTotal: Double = 0.0
    var sendLoss: Int = 0
    var recvLoss: Int = 0

    override fun toString(): String {
        return String.format(
            Locale.getDefault(), FORMAT,
            uid,
            width, height, framerate,
            lastMileDelay,
            videoSendBitrate, videoRecvBitrate,
            audioSendBitrate, audioRecvBitrate,
            cpuApp, cpuTotal,
            sendQuality, recvQuality,
            sendLoss, recvLoss
        )
    }

    companion object {
        private const val FORMAT = "Local(%d)\n\n" +
                "%dx%d %dfps\n" +
                "LastMile delay: %d ms\n" +
                "Video tx/rx (kbps): %d/%d\n" +
                "Audio tx/rx (kbps): %d/%d\n" +
                "CPU: com/total %.1f%%/%.1f%%\n" +
                "Quality tx/rx: %s/%s\n" +
                "Loss tx/rx: %d%%/%d%%"
    }
}
