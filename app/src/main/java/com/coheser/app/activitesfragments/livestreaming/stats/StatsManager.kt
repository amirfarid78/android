package com.coheser.app.activitesfragments.livestreaming.stats

import io.agora.rtc2.Constants

class StatsManager {
    private val mUidList: MutableList<Int> = ArrayList()
    private val mDataMap: MutableMap<Int, StatsData> = HashMap()
    var isEnabled: Boolean = false
        private set

    fun addUserStats(uid: Int, ifLocal: Boolean) {
        if (mUidList.contains(uid) && mDataMap.containsKey(uid)) {
            return
        }

        val data = if (ifLocal
        ) LocalStatsData()
        else RemoteStatsData()
        // in case 32-bit unsigned integer uid is received
        data.uid = uid.toLong() and 0xFFFFFFFFL

        if (ifLocal) mUidList.add(0, uid)
        else mUidList.add(uid)

        mDataMap[uid] = data
    }

    fun removeUserStats(uid: Int) {
        if (mUidList.contains(uid) && mDataMap.containsKey(uid)) {
            mUidList.remove(uid)
            mDataMap.remove(uid)
        }
    }

    fun getStatsData(uid: Int): StatsData? {
        return if (mUidList.contains(uid) && mDataMap.containsKey(uid)) {
            mDataMap[uid]
        } else {
            null
        }
    }

    fun qualityToString(quality: Int): String {
        return when (quality) {
            Constants.QUALITY_EXCELLENT -> "Exc"
            Constants.QUALITY_GOOD -> "Good"
            Constants.QUALITY_POOR -> "Poor"
            Constants.QUALITY_BAD -> "Bad"
            Constants.QUALITY_VBAD -> "VBad"
            Constants.QUALITY_DOWN -> "Down"
            else -> "Unk"
        }
    }

    fun enableStats(enabled: Boolean) {
        isEnabled = enabled
    }

    fun clearAllData() {
        mUidList.clear()
        mDataMap.clear()
    }
}
