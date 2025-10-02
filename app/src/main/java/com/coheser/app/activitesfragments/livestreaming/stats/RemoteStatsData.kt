package com.coheser.app.activitesfragments.livestreaming.stats

import java.util.Locale

class RemoteStatsData : StatsData() {
    var videoDelay: Int = 0
    var audioNetDelay: Int = 0
    var audioNetJitter: Int = 0
    var audioLoss: Int = 0
    var audioQuality: String? = null

    override fun toString(): String {
        return String.format(
            Locale.getDefault(), fORMAT,
            uid,
            width, height, framerate,
            sendQuality, recvQuality,
            videoDelay,
            audioNetDelay, audioNetJitter,
            audioLoss, audioQuality
        )
    }

    companion object {
        const val fORMAT: String = "Remote(%d)\n\n" +
                "%dx%d %dfps\n" +
                "Quality tx/rx: %s/%s\n" +
                "Video delay: %d ms\n" +
                "Audio net delay/jitter: %dms/%dms\n" +
                "Audio loss/quality: %d%%/%s"
    }
}
