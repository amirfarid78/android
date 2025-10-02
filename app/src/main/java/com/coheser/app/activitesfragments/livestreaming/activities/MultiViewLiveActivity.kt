package com.coheser.app.activitesfragments.livestreaming.activities

import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.StreamingConstants
import com.coheser.app.activitesfragments.livestreaming.adapter.MultiCastStatAdapter
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.activitesfragments.livestreaming.stats.LocalStatsData
import com.coheser.app.activitesfragments.livestreaming.stats.RemoteStatsData
import com.coheser.app.activitesfragments.livestreaming.stats.StatsManager
import com.coheser.app.activitesfragments.livestreaming.ui.VideoGridContainer
import com.coheser.app.databinding.ActivityMultiViewLiveBinding
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.TicTicApp
import com.coheser.app.simpleclasses.Variables
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.agora.rtc2.IRtcEngineEventHandler.LocalVideoStats
import io.agora.rtc2.IRtcEngineEventHandler.RemoteAudioStats
import io.agora.rtc2.IRtcEngineEventHandler.RemoteVideoStats
import io.agora.rtc2.IRtcEngineEventHandler.RtcStats
import io.agora.rtc2.video.VideoEncoderConfiguration.VideoDimensions


class MultiViewLiveActivity : RtcBaseMultiviewActivity(), View.OnClickListener {
    @JvmField
    var mVideoGridContainer: VideoGridContainer? = null
    @JvmField
    var videoGridMainLayout: RelativeLayout? = null
    @JvmField
    var mVideoDimension: VideoDimensions? = null
    var pagerSatetAdapter: MultiCastStatAdapter? = null
    var dataList: ArrayList<LiveUserModel> = ArrayList()
    var rootref: DatabaseReference? = null
    var binding: ActivityMultiViewLiveBinding? = null
    var currentModel: LiveUserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(
                this
            ).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this, javaClass, false
        )
        binding = DataBindingUtil.setContentView(
            this@MultiViewLiveActivity,
            R.layout.activity_multi_view_live
        )

        InitControl()
        ActionControl()
    }

    private fun ActionControl() {
        binding!!.swiperefresh.setProgressViewOffset(false, 0, 200)
        binding!!.swiperefresh.setColorSchemeResources(R.color.black)
        binding!!.swiperefresh.setOnRefreshListener { refreshRelated() }
    }

    private fun refreshRelated() {
        binding!!.swiperefresh.isRefreshing = true
        binding!!.swiperefresh.isEnabled = true
        dataList.clear()
        callStreamerList()
    }

    private fun callStreamerList() {
        rootref!!.child(StreamingConstants.liveStreamingUsers)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding!!.swiperefresh.isRefreshing = false
                    if (snapshot.exists()) {
                        val tempList = ArrayList<LiveUserModel>()
                        for (postData in snapshot.children) {
                            val model = postData.getValue(LiveUserModel::class.java)
                            if (model!!.getOnlineType() != null && model.getOnlineType() == "multicast") {
                                tempList.add(model)
                            }
                        }
                        if (dataList.isEmpty()) {
                            setTabs()
                        }
                        dataList.addAll(tempList)
                        pagerSatetAdapter!!.refreshStateSet(false)
                        pagerSatetAdapter!!.notifyDataSetChanged()
                        if (!(binding!!.swiperefresh.isEnabled)) {
                            binding!!.swiperefresh.isEnabled = false
                        }

                        if (dataList.isEmpty()) {
                            binding!!.tabNoUser.visibility = View.VISIBLE
                            binding!!.viewpager.visibility = View.GONE
                        } else {
                            binding!!.tabNoUser.visibility = View.GONE
                            binding!!.viewpager.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (dataList.isEmpty()) {
                        binding!!.tabNoUser.visibility = View.VISIBLE
                        binding!!.viewpager.visibility = View.GONE
                    } else {
                        binding!!.tabNoUser.visibility = View.GONE
                        binding!!.viewpager.visibility = View.VISIBLE
                    }
                }
            })
    }

    private fun InitControl() {
        rootref = FirebaseDatabase.getInstance().reference
        setTabs()
        previousList
    }

    private val previousList: Unit
        get() {
            var tempList: ArrayList<LiveUserModel>? = ArrayList()
            tempList = intent.getParcelableArrayListExtra("dataList")
            dataList.addAll(tempList!!)
            pagerSatetAdapter!!.refreshStateSet(false)
            pagerSatetAdapter!!.notifyDataSetChanged()
            if (!(binding!!.swiperefresh.isEnabled)) {
                binding!!.swiperefresh.isEnabled = false
            }
            binding!!.viewpager.setCurrentItem(intent.getIntExtra("position", 0), true)
            currentModel = dataList[intent.getIntExtra("position", 0)]
        }

    fun updateLiveModel(liveUserModel: LiveUserModel) {
        for (i in dataList.indices) {
            if (dataList[i]!!.userId.equals(liveUserModel.userId, ignoreCase = true)) {
                dataList.removeAt(i)
                dataList.add(i, liveUserModel)
            }
        }
        currentModel = dataList[binding!!.viewpager.currentItem]
    }

    fun setTabs() {
        pagerSatetAdapter =
            MultiCastStatAdapter(supportFragmentManager, dataList, this@MultiViewLiveActivity)
        binding!!.viewpager.adapter = pagerSatetAdapter
        binding!!.viewpager.offscreenPageLimit = 1
        binding!!.viewpager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                currentModel = dataList[position]
                binding!!.swiperefresh.isEnabled = position == 0
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }


    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
        // Do nothing at the moment
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        // Do nothing at the moment
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        runOnUiThread { removeRemoteUser(uid) }
    }

    override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
        runOnUiThread {
            printLog(Constants.tag, "onFirstRemoteVideoDecoded")
            renderRemoteUser(uid)
        }
    }

    private fun renderRemoteUser(uid: Int) {
        printLog(Constants.tag, "renderRemoteUser $uid")
        val surface = prepareRtcVideo(uid, false)
        if (currentModel!!.pkInvitation != null && currentModel!!.pkInvitation!!.pkStreamingId != null) {
            mVideoGridContainer!!.addUserVideoSurface(
                uid,
                surface,
                currentModel!!.streamUid != -1 && uid == currentModel!!.streamUid
            )
        } else {
            mVideoGridContainer!!.addUserVideoSurface(uid, surface, false)
        }
    }

    private fun removeRemoteUser(uid: Int) {
        removeRtcVideo(uid, false)
        mVideoGridContainer!!.removeUserVideo(uid, false)
    }

    override fun onLocalVideoStats(stats: LocalVideoStats) {
        if (!statsManager().isEnabled) return

        val data = statsManager().getStatsData(config().uid!!.toInt()) as LocalStatsData
            ?: return

        data.width = mVideoDimension!!.width
        data.height = mVideoDimension!!.height
        data.framerate = stats.sentFrameRate
    }

    override fun onRtcStats(stats: RtcStats) {
        if (channelName != "" && (channelName != null)) {
        }

        if (!statsManager().isEnabled) return

        val data = statsManager().getStatsData(config().uid!!.toInt()) as LocalStatsData
            ?: return

        data.lastMileDelay = stats.lastmileDelay
        data.videoSendBitrate = stats.txVideoKBitRate
        data.videoRecvBitrate = stats.rxVideoKBitRate
        data.audioSendBitrate = stats.txAudioKBitRate
        data.audioRecvBitrate = stats.rxAudioKBitRate
        data.cpuApp = stats.cpuAppUsage
        data.cpuTotal = stats.cpuAppUsage
        data.sendLoss = stats.txPacketLossRate
        data.recvLoss = stats.rxPacketLossRate
    }


    // check the network quality
    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
        if (!statsManager().isEnabled) return

        val data = statsManager().getStatsData(uid) ?: return

        data.sendQuality = statsManager().qualityToString(txQuality)
        data.recvQuality = statsManager().qualityToString(rxQuality)
    }

    override fun onRemoteVideoStats(stats: RemoteVideoStats) {
        if (!statsManager().isEnabled) return

        val data = statsManager().getStatsData(stats.uid) as RemoteStatsData ?: return

        data.width = stats.width
        data.height = stats.height
        data.framerate = stats.rendererOutputFrameRate
        data.videoDelay = stats.delay
    }

    override fun onRemoteAudioStats(stats: RemoteAudioStats) {
        if (!statsManager().isEnabled) return

        val data = statsManager().getStatsData(stats.uid) as RemoteStatsData ?: return

        data.audioNetDelay = stats.networkTransportDelay
        data.audioNetJitter = stats.jitterBufferDelay
        data.audioLoss = stats.audioLossRate
        data.audioQuality = statsManager().qualityToString(stats.quality)
    }


    fun switchCamera() {
        rtcEngine()?.switchCamera()
    }

    fun muteLocalAudioStream(isAudioActivated: Boolean) {
        rtcEngine()?.muteLocalAudioStream(isAudioActivated)
    }

    fun setBeautyEffectOptions(isbeautyActivated: Boolean) {
        rtcEngine()?.setBeautyEffectOptions(
            isbeautyActivated,
            StreamingConstants.DEFAULT_BEAUTY_OPTIONS
        )
    }

    fun stopBroadcast(role: Int) {
        rtcEngine()?.setClientRole(role)
        removeRtcVideo(0, true)
        statsManager().clearAllData()
    }

    fun startBroadcast(userId: String?, role: Int): SurfaceView {
        val ticTicApp = application as TicTicApp
        config().uid = getSharedPreference(this)
            .getString(Variables.U_ID, "")
        ticTicApp.engineConfig().channelName = userId
        rtcEngine()?.setClientRole(role)
        return prepareRtcVideo(0, true)
    }


    fun getconfigDimenIndex(): VideoDimensions {
        return StreamingConstants.VIDEO_DIMENSIONS[config().videoDimenIndex]
    }

    fun setStatsManager(): StatsManager {
        return statsManager()
    }

    fun setClientRole(userRole: Int) {
        rtcEngine()?.setClientRole(userRole)
    }


    override fun finish() {
        super.finish()
        statsManager().clearAllData()
    }


    override fun onClick(v: View) {
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}