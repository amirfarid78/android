package com.coheser.app.activitesfragments.livestreaming.stats

open class StatsData {
    @JvmField
    var uid: Long = 0
    var width: Int = 0
    var height: Int = 0
    var framerate: Int = 0
    var recvQuality: String? = null
    var sendQuality: String? = null
}
