package com.coheser.app.activitesfragments.livestreaming.ui

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.compose.runtime.NoLiveLiterals
import com.facebook.drawee.view.SimpleDraweeView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.stats.StatsManager
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.printLog


class VideoGridContainer : RelativeLayout, Runnable {
    var mainParentlayout: RelativeLayout? = null
    private val mUserViewList = SparseArray<ViewGroup?>(MAX_USER)
    private val mUidList: MutableList<Int?> = ArrayList(MAX_USER)
    private var mStatsManager: StatsManager? = null
    private var mHandler: Handler? = null
    private var mStatMarginBottom = 0

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    lateinit var myview:View
    private fun init() {
        myview=  LayoutInflater.from(context).inflate(R.layout.item_streaming_video_placeholder, this, true)
        mStatMarginBottom = resources.getDimensionPixelSize(
            R.dimen.live_stat_margin_bottom
        )
        mHandler = Handler(Looper.getMainLooper())
    }

    fun setMainParentLayout(layout: RelativeLayout?,profileLink:String?) {
        this.mainParentlayout = layout
        if(profileLink?.isNotEmpty() == true) {
            val profileImage = myview.findViewById<SimpleDraweeView>(R.id.profileImage)
            if(profileImage!=null)
            profileImage.controller = frescoImageLoad(profileLink, profileImage, false)
        }

    }

    fun setStatsManager(manager: StatsManager?) {
        mStatsManager = manager
    }

    fun addUserVideoSurface(uid: Int, surface: SurfaceView?, isLocal: Boolean) {
        printLog(Constants.tag, "addUserVideoSurface:$uid")

        if (surface == null) {
            return
        }

        var id = -1
        if (isLocal) {
            if (mUidList.contains(uid)) {
                mUidList.remove(uid)
                mUserViewList.remove(uid)
            }

            if (mUidList.size == MAX_USER) {
                mUidList.remove(uid)
                mUserViewList.remove(uid)
            }
            id = 0
        }
        else {
            if (mUidList.contains(uid)) {
                mUidList.remove(uid)
                mUserViewList.remove(uid)
            }

            if (mUidList.size < MAX_USER) {
                id = uid
            }
        }

        if (id == 0) {
            mUidList.add(0, uid)
        } else mUidList.add(uid)

        if (id != -1) {
            mUserViewList.append(uid, createVideoView(surface))

            if (mStatsManager != null) {
                mStatsManager!!.addUserStats(uid, isLocal)
                if (mStatsManager!!.isEnabled) {
                    mHandler!!.removeCallbacks(this)
                    mHandler!!.postDelayed(this, STATS_REFRESH_INTERVAL.toLong())
                }
            }

            requestGridLayout()
        }
    }


    private fun createVideoView(surface: SurfaceView): ViewGroup {
        val layout = RelativeLayout(context)

        layout.id = surface.hashCode()

        val videoLayoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        layout.addView(surface, videoLayoutParams)

        val text = TextView(context)
        text.id = layout.hashCode()
        val textParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        textParams.addRule(ALIGN_PARENT_BOTTOM, TRUE)
        textParams.bottomMargin = mStatMarginBottom
        textParams.leftMargin = STAT_LEFT_MARGIN
        text.setTextColor(Color.WHITE)
        text.textSize = STAT_TEXT_SIZE.toFloat()

        layout.addView(text, textParams)
        return layout
    }

    fun removeUserVideo(uid: Int, isLocal: Boolean) {
        if (isLocal && mUidList.contains(0)) {
            mUidList.remove(0)
            mUserViewList.remove(0)
        } else if (mUidList.contains(uid)) {
            mUidList.remove(uid)
            mUserViewList.remove(uid)
        }

        mStatsManager!!.removeUserStats(uid)
        requestGridLayout()

        if (childCount == 0) {
            mHandler!!.removeCallbacks(this)
        }
    }

    private fun requestGridLayout() {
        removeAllViews()
        layout(mUidList.size)
    }

    private fun layout(size: Int) {
        val params = getParams(size)

        if (mainParentlayout != null) {
            if (size == 1) {
                val params1 = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
                params1.setMargins(
                    0, 0,
                    0, 0
                )
                params1.addRule(ALIGN_PARENT_TOP, TRUE)
                params1.addRule(ALIGN_PARENT_BOTTOM, TRUE)
                params1.addRule(ALIGN_PARENT_START, TRUE)
                params1.addRule(CENTER_VERTICAL, TRUE)
                mainParentlayout!!.layoutParams = params1
            }
            else if (size == 2) {
                val params1 = mainParentlayout!!.layoutParams as LayoutParams
                params1.setMargins(
                    0, resources.getDimensionPixelSize(R.dimen._80sdp),
                    0, measuredWidth
                )
                mainParentlayout!!.layoutParams = params1
            }
            else {
                val params1 = mainParentlayout!!.layoutParams as LayoutParams
                params1.setMargins(
                    0, resources.getDimensionPixelSize(R.dimen._80sdp),
                    0, resources.getDimensionPixelSize(R.dimen._80sdp)
                )
                mainParentlayout!!.layoutParams = params1
            }
        }

        for (i in 0 until size) {
            if (mUidList[i] != null) {
                if (mUserViewList[mUidList[i]!!] != null && mUserViewList[mUidList[i]!!]!!
                        .parent != null
                ) {
                    (mUserViewList[mUidList[i]!!]!!.parent as ViewGroup).removeView(
                        mUserViewList[mUidList[i]!!]
                    )
                }

                try {
                    addView(mUserViewList[mUidList[i]!!], params[i])
                } catch (e: Exception) {
                }
            }
        }
    }

    @NoLiveLiterals
    private fun getParams(size: Int): Array<LayoutParams?> {
        val width = measuredWidth
        val height = measuredHeight

        val array =
            arrayOfNulls<LayoutParams>(size)

        for (i in 0 until size) {
            if (i == 0) {
                array[0] = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
                array[0]!!.setMargins(
                    0, 0,
                    0, 0
                )
                array[0]!!.addRule(ALIGN_PARENT_TOP, TRUE)
                array[0]!!.addRule(ALIGN_PARENT_BOTTOM, TRUE)
                array[0]!!.addRule(ALIGN_PARENT_START, TRUE)
                array[0]!!.addRule(CENTER_VERTICAL, TRUE)
            }
            else if (i == 1) {
                array[1] = LayoutParams(width / 2, height)
                array[0]!!.width = array[1]!!.width
                array[1]!!.height = array[0]!!.height
                if (mainParentlayout == null) {
                    array[1]!!.setMargins(
                        0, resources.getDimensionPixelSize(R.dimen._80sdp),
                        0, resources.getDimensionPixelSize(R.dimen._80sdp)
                    )
                    array[0]!!.setMargins(
                        0, resources.getDimensionPixelSize(R.dimen._80sdp),
                        0, resources.getDimensionPixelSize(R.dimen._80sdp)
                    )
                }
                array[1]!!.addRule(ALIGN_PARENT_END, TRUE)
                array[1]!!.addRule(ALIGN_PARENT_TOP, TRUE)
                array[1]!!.addRule(ALIGN_PARENT_BOTTOM, TRUE)
                array[1]!!.addRule(CENTER_VERTICAL, TRUE)
            }
            else if (i == 2) {
                array[i] = LayoutParams(width / 2, height / 2)
                array[i - 1]!!.width = array[i]!!.width
                array[i]!!.addRule(
                    RIGHT_OF, mUserViewList[mUidList[i - 1]!!]!!
                        .id
                )
                array[i]!!.addRule(
                    ALIGN_TOP, mUserViewList[mUidList[i - 1]!!]!!
                        .id
                )
            }
            else if (i == 3) {
                array[i] = LayoutParams(width / 2, height / 2)
                array[0]!!.width = width / 2
                array[1]!!.addRule(BELOW, 0)
                array[1]!!.addRule(ALIGN_PARENT_LEFT, 0)
                array[1]!!
                    .addRule(RIGHT_OF, mUserViewList[mUidList[0]!!]!!.id)
                array[1]!!.addRule(ALIGN_PARENT_TOP, TRUE)
                array[2]!!.addRule(ALIGN_PARENT_LEFT, TRUE)
                array[2]!!.addRule(RIGHT_OF, 0)
                array[2]!!.addRule(ALIGN_TOP, 0)
                array[2]!!.addRule(BELOW, mUserViewList[mUidList[0]!!]!!.id)
                array[3]!!.addRule(BELOW, mUserViewList[mUidList[1]!!]!!.id)
                array[3]!!
                    .addRule(RIGHT_OF, mUserViewList[mUidList[2]!!]!!.id)
            }
        }

        return array
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearAllVideo()
    }

    private fun clearAllVideo() {
        removeAllViews()
        mUserViewList.clear()
        mUidList.clear()
        mHandler!!.removeCallbacks(this)
    }

    override fun run() {
        if (mStatsManager != null && mStatsManager!!.isEnabled) {
            val count = childCount
            for (i in 0 until count) {
                val layout = getChildAt(i) as RelativeLayout
                val text = layout.findViewById<TextView>(layout.hashCode())
                if (text != null) {
                    val data = mStatsManager!!.getStatsData(mUidList[i]!!)
                    val info = data?.toString()
                    if (info != null) text.text = info
                }
            }

            mHandler!!.postDelayed(this, STATS_REFRESH_INTERVAL.toLong())
        }
    }


    companion object {
        private const val MAX_USER = 2
        private const val STATS_REFRESH_INTERVAL = 30000
        private const val STAT_LEFT_MARGIN = 34
        private const val STAT_TEXT_SIZE = 10
    }
}
