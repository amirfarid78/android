package com.coheser.app.activitesfragments.videorecording

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.Image
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.argear.AppConfig
import com.coheser.app.activitesfragments.argear.BeautyFragment
import com.coheser.app.activitesfragments.argear.BulgeFragment
import com.coheser.app.activitesfragments.argear.GLView
import com.coheser.app.activitesfragments.argear.GLView.GLViewListener
import com.coheser.app.activitesfragments.argear.StickerFragment
import com.coheser.app.activitesfragments.argear.api.ContentsResponse
import com.coheser.app.activitesfragments.argear.camera.ReferenceCamera
import com.coheser.app.activitesfragments.argear.camera.ReferenceCamera.CameraListener
import com.coheser.app.activitesfragments.argear.data.BeautyItemData
import com.coheser.app.activitesfragments.argear.model.ItemModel
import com.coheser.app.activitesfragments.argear.network.DownloadAsyncTask
import com.coheser.app.activitesfragments.argear.rendering.CameraTexture
import com.coheser.app.activitesfragments.argear.rendering.ScreenRenderer
import com.coheser.app.activitesfragments.argear.util.FileDeleteAsyncTask
import com.coheser.app.activitesfragments.argear.util.PreferenceUtil
import com.coheser.app.activitesfragments.argear.viewmodel.ContentsViewModel
import com.coheser.app.activitesfragments.soundlists.SoundListMainActivity
import com.coheser.app.adapters.DurationTimeAdapter
import com.coheser.app.adapters.PhotoUploadAdapter
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.TimerDuration
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.DateOprations.getCurrentDate
import com.coheser.app.simpleclasses.DateOprations.millisecondsToMMSS
import com.coheser.app.simpleclasses.Dialogs.cancelDeterminentLoader
import com.coheser.app.simpleclasses.Dialogs.showAlert
import com.coheser.app.simpleclasses.Dialogs.showDeterminentLoader
import com.coheser.app.simpleclasses.Dialogs.showLoadingProgress
import com.coheser.app.simpleclasses.Dialogs.showToastOnTop
import com.coheser.app.simpleclasses.FFMPEGFunctions.CalculateFFMPEGTimeToPercentage
import com.coheser.app.simpleclasses.FFMPEGFunctions.ConcatenateMultipleVideos
import com.coheser.app.simpleclasses.FFMPEGFunctions.createImageVideo
import com.coheser.app.simpleclasses.FFMPEGFunctions.rotateVideoToPotrate
import com.coheser.app.simpleclasses.FFMPEGFunctions.videoSpeedProcess
import com.coheser.app.simpleclasses.FileUtils.clearFilesCacheBeforeOperation
import com.coheser.app.simpleclasses.FileUtils.convertImage
import com.coheser.app.simpleclasses.FileUtils.copyFile
import com.coheser.app.simpleclasses.FileUtils.getAppFolder
import com.coheser.app.simpleclasses.FileUtils.getBitmapToUri
import com.coheser.app.simpleclasses.FileUtils.getTrimVideoFrameRate
import com.coheser.app.simpleclasses.FileUtils.isFileSizeLessThan50KB
import com.coheser.app.simpleclasses.FileUtils.isWidthGreaterThanHeight
import com.coheser.app.simpleclasses.FileUtils.makeDirectryAndRefresh
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.parseInterger
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.SegmentedProgressBar
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.trimmodule.TrimType
import com.coheser.app.trimmodule.TrimVideo
import com.coheser.app.trimmodule.TrimmerUtils
import com.seerslab.argear.exceptions.InvalidContentsException
import com.seerslab.argear.exceptions.NetworkException
import com.seerslab.argear.exceptions.SignedUrlGenerationException
import com.seerslab.argear.session.ARGAuth
import com.seerslab.argear.session.ARGContents
import com.seerslab.argear.session.ARGContents.BulgeType
import com.seerslab.argear.session.ARGFrame
import com.seerslab.argear.session.ARGMedia
import com.seerslab.argear.session.ARGSession
import com.seerslab.argear.session.config.ARGCameraConfig
import com.seerslab.argear.session.config.ARGConfig
import com.seerslab.argear.session.config.ARGInferenceConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.EnumSet
import java.util.Locale
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class VideoRecoderActivity : AppCompatLocaleActivity(), View.OnClickListener {
    var number = 0
    var videopaths = ArrayList<String>()
    var recordImage: ImageButton? = null
    var doneBtn: ImageButton? = null
    var isRecording = false
    var isFlashOn = false
    var isSelected: String? = null
    var ivFlash: ImageView? = null
    var tabFlash: LinearLayout? = null
    var tabRotateCam: LinearLayout? = null
    var videoProgress: SegmentedProgressBar? = null
    var cameraOptions: LinearLayout? = null
    var photoSlideOptions: LinearLayout? = null
    var photosRecyclerview: RecyclerView? = null
    var photoUploadAdapter: PhotoUploadAdapter? = null
    var uploadPhotoPath = ArrayList<String>()
    var cutVideoBtn: ImageView? = null
    protected var speedSelectionTab: TabLayout? = null
    var speedTabPosition = 2
    var isSpeedMode = true
    var context: Context? = null
    var addSoundTxt: TextView? = null
    var secPassed = 0
    var timeInMilis: Long = 0
    var countdownTimerTxt: TextView? = null
    var isRecordingTimerEnable = false
    var recordingTime = 3
    var tvUploadStory: TextView? = null
    var tvUploadVideo: TextView? = null
    var videoType = "Video"
    var timerSelectedDuration = 30 * 1000
    var tabVideoLength: RelativeLayout? = null
    var recordingTimerTxt: TextView? = null
    var tabSpeed: LinearLayout? = null
    var tabTimer: LinearLayout? = null
    var tabFeature: LinearLayout? = null
    var tabFunny: LinearLayout? = null
    var tabFilter: LinearLayout? = null
    var progressBar: ProgressBar? = null
    var mCameraManager: CameraManager? = null
    var mCameraId: String? = null
    private var mCamera: ReferenceCamera? = null
    private var mGlView: GLView? = null
    private var mScreenRenderer: ScreenRenderer? = null
    private var mCameraTexture: CameraTexture? = null
    private val mScreenRatio = ARGFrame.Ratio.RATIO_FULL
    private var mItemDownloadPath: String? = null
    private var mIsShooting = false
    private var mFilterVignette = false
    private var mFilterBlur = false
    private var mFilterLevel = 100
    private var mCurrentStickeritem: ItemModel? = null
    var beautyItemData: BeautyItemData? = null
        private set
    private var mHasTrigger = false
    private var mUseARGSessionDestroy = false
    private var mDeviceWidth = 0
    private var mDeviceHeight = 0
    var gLViewWidth = 0
        private set
    var gLViewHeight = 0
        private set
    private var mTriggerToast: Toast? = null
    private var mARGSession: ARGSession? = null
    private var mARGMedia: ARGMedia? = null
    private var mContentsViewModel: ContentsViewModel? = null
    var cameraLayout: FrameLayout? = null
    var recyclerView : RecyclerView? = null

    val timerDurations = listOf(
        TimerDuration("15s", 15_000),
        TimerDuration("60s", 60_000),
        TimerDuration("10m", 600_000),
        TimerDuration("Photo", Constants.MAX_TIME_FOR_VIDEO_PICS.toLong() * 1000)
    )
    var durationAdapter : DurationTimeAdapter? = null

    fun setDurationAdapter() {
        recyclerView = findViewById(R.id.recyclerview)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView?.layoutManager = layoutManager
        recyclerView?.clipToPadding = false

        recyclerView?.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (recyclerView!!.childCount > 0) {
                val firstChild = recyclerView!!.getChildAt(0)
                val itemWidth = firstChild.measuredWidth
                val horizontalPadding = (recyclerView!!.width - itemWidth) / 2
                recyclerView!!.setPadding(horizontalPadding, recyclerView!!.paddingTop, horizontalPadding, recyclerView!!.paddingBottom)
            }
        }

        durationAdapter = DurationTimeAdapter(timerDurations, object : AdapterClickListener {
            override fun onItemClick(view: View?, pos: Int, `object`: Any?) {
                val model = `object` as TimerDuration
                handleDurationSelection(model)
                scrollToCenter(pos)
            }
        })
        recyclerView!!.adapter = durationAdapter

        recyclerView!!.post {
            addDynamicPadding()
            scrollToCenter(0) // Initially center the first item
        }

        // Add scroll listener for auto-centering
        recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    centerOnNearestItem()
                }
            }
        })
    }
    /**
     * Dynamically adds padding so that items can be centered even if they don’t fill the full width
     */
    private fun addDynamicPadding() {
        if (recyclerView!!.childCount > 0) {
            val firstChild = recyclerView!!.getChildAt(0)
            val lastChild = recyclerView!!.getChildAt(recyclerView!!.childCount - 1)

            val firstItemWidth = firstChild.measuredWidth
            val lastItemWidth = lastChild.measuredWidth
            val screenWidth = recyclerView!!.width

            // Calculate padding to center first and last items properly
            val startPadding = (screenWidth - firstItemWidth) / 2
            val endPadding = (screenWidth - lastItemWidth) / 2

            recyclerView!!.setPadding(startPadding, 0, endPadding, 0)
        }
    }
    /**
     * Adjusts the RecyclerView scroll to center the closest item when scrolling stops
     */
    private fun centerOnNearestItem() {
        val layoutManager = recyclerView!!.layoutManager as LinearLayoutManager
        val recyclerViewCenterX = recyclerView!!.width / 2

        var closestChild: View? = null
        var closestChildDistance = Int.MAX_VALUE

        for (i in 0 until recyclerView!!.childCount) {
            val child = recyclerView!!.getChildAt(i)
            val childCenterX = (child.left + child.right) / 2
            val distance = Math.abs(childCenterX - recyclerViewCenterX)

            if (distance < closestChildDistance) {
                closestChildDistance = distance
                closestChild = child
            }
        }

        closestChild?.let {
            val position = layoutManager.getPosition(it)

            durationAdapter?.setSelectedPosition(position)

            scrollToCenter(position)
            handleDurationSelection(timerDurations[position])
        }
    }

    /**
     * Scrolls the selected item to the center of the RecyclerView
     */
    private fun scrollToCenter(position: Int) {
        val layoutManager = recyclerView!!.layoutManager as LinearLayoutManager

        recyclerView!!.post {
            val selectedView = layoutManager.findViewByPosition(position)
            if (selectedView != null) {
                val parentWidth = recyclerView!!.width
                val itemWidth = selectedView.measuredWidth
                val itemStartX = selectedView.left

                val offset = itemStartX - (parentWidth / 2) + (itemWidth / 2)

                recyclerView!!.smoothScrollBy(offset, 0)
            }
        }
    }

    private fun handleDurationSelection(duration: TimerDuration) {
        timerSelectedDuration = duration.durationMs.toInt()
        videoType = if (duration.title == "Photo") "Photo" else "Video"
        clearCacheFiles()
        updateViewsAccordingToType()
        Constants.RECORDING_DURATION = timerSelectedDuration
        checkDoneBtnEnable()
        setupVideoProgress()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        hideNavigation()
        setContentView(R.layout.activity_video_recoder)
        context = this@VideoRecoderActivity

        setDurationAdapter()

        initNewControls()
        Variables.selectedSoundId = "null"
        Constants.RECORDING_DURATION = 15 * 1000
        clearCacheFiles()
        photoSlideOptions = findViewById(R.id.photoSlideOptions)
        cameraOptions = findViewById(R.id.cameraOptions)
        recordImage = findViewById(R.id.record_image)
        speedSelectionTab = findViewById<View>(R.id.speedSelectionTab) as TabLayout
        setupPhotoSlideAdapter()
        setupSpeedTab()
        findViewById<View>(R.id.upload_layout).setOnClickListener(this)
        cutVideoBtn = findViewById(R.id.cut_video_btn)
        cutVideoBtn!!.setVisibility(View.GONE)
        cutVideoBtn!!.setOnClickListener(this)
        doneBtn = findViewById(R.id.done)
        doneBtn!!.setEnabled(true)
        doneBtn!!.setOnClickListener(this)
        tabVideoLength = findViewById(R.id.tabVideoLength)
        recordingTimerTxt = findViewById(R.id.recordingTimerTxt)
        tvUploadStory = findViewById(R.id.tvUploadStory)
        tvUploadVideo = findViewById(R.id.tvUploadVideo)
        tvUploadStory!!.setOnClickListener(this)
        tvUploadVideo!!.setOnClickListener(this)
        tabRotateCam = findViewById(R.id.tabRotateCam)
        tabRotateCam!!.setOnClickListener(this)
        findViewById<View>(R.id.goBack).setOnClickListener(this)
        addSoundTxt = findViewById(R.id.add_sound_txt)
        addSoundTxt!!.setOnClickListener(this)
        tabTimer = findViewById(R.id.tabTimer)
        tabTimer!!.setOnClickListener(this)
        tabSpeed = findViewById(R.id.tabSpeed)
        tabSpeed!!.setOnClickListener(this)
        tabFeature = findViewById(R.id.tabFeature)
        tabFeature!!.setOnClickListener(this)
        tabFunny = findViewById(R.id.tabFunny)
        tabFunny!!.setOnClickListener(this)
        tabFilter = findViewById(R.id.tabFilter)
        tabFilter!!.setOnClickListener(this)
        initVideoProgress()
        val intent = intent
        if (intent.hasExtra("name")) {
            addSoundTxt!!.setText(intent.getStringExtra("name"))
            Variables.selectedSoundId = intent.getStringExtra("sound_id")
            isSelected = intent.getStringExtra("isSelected")
            tabVideoLength!!.setVisibility(View.INVISIBLE)
            findViewById<View>(R.id.tabVideoTypeSelection).visibility = View.INVISIBLE
            preparedAudio()
        }
        recordImage!!.setOnClickListener(View.OnClickListener { startOrStopRecording("") })
        countdownTimerTxt = findViewById(R.id.countdown_timer_txt)
    }

    private fun setupPhotoSlideAdapter() {
        photosRecyclerview = findViewById(R.id.photosRecyclerview)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        photosRecyclerview!!.setLayoutManager(layoutManager)
        val itemDecor = ItemTouchHelper((object : ItemTouchHelper.SimpleCallback(3, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition
                val fromItem = uploadPhotoPath[fromPos]
                val toItem = uploadPhotoPath[toPos]
                uploadPhotoPath[fromPos] = toItem
                uploadPhotoPath[toPos] = fromItem
                photoUploadAdapter!!.notifyItemMoved(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        } as ItemTouchHelper.Callback))
        itemDecor.attachToRecyclerView(photosRecyclerview)
        photoUploadAdapter = PhotoUploadAdapter(uploadPhotoPath) { view, pos, `object` ->
            val itemUpdated = uploadPhotoPath[pos]
            if (view.id == R.id.ivDeletePhoto) {
                uploadPhotoPath.remove(itemUpdated)
                photoUploadAdapter!!.notifyDataSetChanged()
                updatePhotoUploadStatus()
            }
        }
        photosRecyclerview!!.setAdapter(photoUploadAdapter)
    }

    private fun clearCacheFiles() {
        removeAllFilesIntoDir(getAppFolder(context!!) + Variables.APP_HIDED_FOLDER)
        removeAllFilesIntoDir(getAppFolder(context!!) + Variables.APP_STORY_EDITED_FOLDER)
        removeAllFilesIntoDir(getAppFolder(context!!) + Variables.APP_OUTPUT_FOLDER)
    }

    private fun initNewControls() {
        mContentsViewModel = ViewModelProvider(this).get(ContentsViewModel::class.java)
        mContentsViewModel!!.contents.observe(this, object : Observer<ContentsResponse?> {
            override fun onChanged(value: ContentsResponse?) {
                if (value == null) return
                setLastUpdateAt(this@VideoRecoderActivity, value.lastUpdatedAt)
            }
        })
        beautyItemData = BeautyItemData()
        val realSize = Point()
        val display = (this.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
        display.getRealSize(realSize)
        mDeviceWidth = realSize.x
        mDeviceHeight = realSize.y
        gLViewWidth = realSize.x
        gLViewHeight = realSize.y
        mItemDownloadPath = filesDir.absolutePath
        mCameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        progressBar = findViewById(R.id.progressBar)
        tabFlash = findViewById(R.id.tabFlash)
        tabFlash!!.setOnClickListener(this)
        ivFlash = findViewById(R.id.ivFlash)
        try {
            mCameraId = mCameraManager!!.cameraIdList[0]
            val isFlashAvailable = applicationContext.packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
            if (!isFlashAvailable) tabFlash!!.setVisibility(View.GONE)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            printLog(Constants.tag, e.toString())
        }
    }

    private fun setLastUpdateAt(context: Context, updateAt: Long) {
        PreferenceUtil.putLongValue(
            context,
            AppConfig.USER_PREF_NAME,
            "ContentLastUpdateAt",
            updateAt
        )
    }

    private fun initVideoProgress() {
        videoProgress = findViewById(R.id.video_progress)
        videoProgress!!.setDividerColor(Color.WHITE)
        videoProgress!!.setDividerEnabled(true)
        videoProgress!!.setDividerWidth(4f)
        videoProgress!!.setShader(
            intArrayOf(
                getColor(R.color.appColor),
                getColor(R.color.appColor),
                getColor(R.color.appColor)
            )
        )
        setupVideoProgress()
    }

    private fun removeAllFilesIntoDir(dirPath: String) {
        Log.d("Files__", "DirPath: $dirPath")
        val directory = File(dirPath)
        if (directory.exists()) {
            val files = directory.listFiles()
            Log.d("Files__", "Size: " + files.size)
            for (i in files.indices) {
                Log.d("Files__", "FileName:" + files[i].absolutePath)
                clearFilesCacheBeforeOperation(files[i])
            }
        }
    }

    private fun setupSpeedTab() {
        speedSelectionTab!!.addTab(
            speedSelectionTab!!.newTab().setText(context!!.getString(R.string.speed_scale_one))
        )
        speedSelectionTab!!.addTab(
            speedSelectionTab!!.newTab().setText(context!!.getString(R.string.speed_scale_two))
        )
        speedSelectionTab!!.addTab(
            speedSelectionTab!!.newTab().setText(context!!.getString(R.string.speed_scale_three))
        )
        speedSelectionTab!!.addTab(
            speedSelectionTab!!.newTab().setText(context!!.getString(R.string.speed_scale_four))
        )
        speedSelectionTab!!.addTab(
            speedSelectionTab!!.newTab().setText(context!!.getString(R.string.speed_scale_five))
        )
        speedSelectionTab!!.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val v = tab.customView
                val title = v!!.findViewById<TextView>(R.id.text)
                title.setTextColor(ContextCompat.getColor(context!!, R.color.blackColor))
                title.background = ContextCompat.getDrawable(
                    context!!,
                    R.drawable.ractengle_less_round_solid_white
                )
                tab.setCustomView(v)
                speedTabPosition = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val v = tab.customView
                val title = v!!.findViewById<TextView>(R.id.text)
                title.setTextColor(ContextCompat.getColor(context!!, R.color.graycolor2))
                title.background =
                    ContextCompat.getDrawable(context!!, R.drawable.ractengle_transprent)
                tab.setCustomView(v)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        setupTabIcons()
    }

    // Bottom tabs when we open an activity
    private fun setupTabIcons() {
        speedSelectionTab!!.getTabAt(0)!!.setCustomView(
            getCustomTabView(
                context!!.getString(R.string.speed_scale_one), ContextCompat.getColor(
                    context!!, R.color.graycolor2
                ), R.drawable.ractengle_transprent
            )
        )
        speedSelectionTab!!.getTabAt(1)!!.setCustomView(
            getCustomTabView(
                context!!.getString(R.string.speed_scale_two), ContextCompat.getColor(
                    context!!, R.color.graycolor2
                ), R.drawable.ractengle_transprent
            )
        )
        speedSelectionTab!!.getTabAt(2)!!.setCustomView(
            getCustomTabView(
                context!!.getString(R.string.speed_scale_three), ContextCompat.getColor(
                    context!!, R.color.graycolor2
                ), R.drawable.ractengle_transprent
            )
        )
        speedSelectionTab!!.getTabAt(3)!!.setCustomView(
            getCustomTabView(
                context!!.getString(R.string.speed_scale_four), ContextCompat.getColor(
                    context!!, R.color.graycolor2
                ), R.drawable.ractengle_transprent
            )
        )
        speedSelectionTab!!.getTabAt(4)!!.setCustomView(
            getCustomTabView(
                context!!.getString(R.string.speed_scale_five), ContextCompat.getColor(
                    context!!, R.color.graycolor2
                ), R.drawable.ractengle_transprent
            )
        )
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            speedSelectionTab!!.getTabAt(speedTabPosition)!!.select()
        }

    }

    private fun getCustomTabView(title: String, color: Int, background: Int): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_speed_tablayout, null)
        val textView = view.findViewById<TextView>(R.id.text)
        textView.text = title
        textView.setTextColor(color)
        textView.background = ContextCompat.getDrawable(context!!, background)
        return view
    }

    // start trimming activity
    var takeOrSelectVideoResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {

            override fun onActivityResult(result: ActivityResult?) {
                if (result!!.resultCode == RESULT_OK) {
                    val data = result.data
                    printLog(Constants.tag, "result.getData()" + data!!.data.toString())
                    if (TrimmerUtils.getDuration(
                            this@VideoRecoderActivity,
                            data.data
                        ) < Constants.MIN_TRIM_TIME
                    ) {
                        Toast.makeText(
                            context,
                            getString(R.string.video_must_be_larger_then_second),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    if (data.data != null) {
                        openTrimActivity(data.data.toString())
                    }
                }
            }
        })
    var videoTrimResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result!!.resultCode == RESULT_OK) {
                    val uri = Uri.parse(
                        TrimVideo.getTrimmedVideoPath(
                            result.data,
                            Variables.gallery_trimed_video
                        )
                    )
                    val filepath = uri.toString()
                    changeVideoSize(
                        filepath, getAppFolder(
                            context!!
                        ) + Variables.outputfile2
                    )
                } else Log.d(Constants.tag, "videoTrimResultLauncher data is null")
            }
        })

    fun setupVideoProgress() {
        videoProgress!!.enableAutoProgressView(Constants.RECORDING_DURATION.toLong())
        secPassed = 0
        timeInMilis = 0
        videoProgress!!.SetListener { mills ->
            Log.d("timeinMill", "timeinMill: $mills")
            timeInMilis = mills
            recordingTimerTxt!!.text = millisecondsToMMSS(timeInMilis)
            if (timeInMilis > 1000 && doneBtn!!.visibility != View.VISIBLE) {
                checkDoneBtnEnable()
            }
            secPassed = (mills / 1000).toInt()
            if (secPassed > Constants.RECORDING_DURATION / 1000 - 1) {
                startOrStopRecording("")
            }
            if (isRecordingTimerEnable && secPassed >= recordingTime) {
                isRecordingTimerEnable = false
                startOrStopRecording("")
            }
        }
    }

    fun startOrStopRecording(from: String) {
        if (videoType == "Photo") {
            doneBtn!!.visibility = View.VISIBLE
            mIsShooting = true
        } else {
            if (!isRecording) {
                number = number + 1
                isRecording = true
                val file = File(getAppFolder(this) + Variables.videoChunk + number + ".mp4")
                videopaths.add(getAppFolder(this) + Variables.videoChunk + number + ".mp4")
                startRecording(file.absolutePath)
                if (audio != null) {
                    audio!!.start()
                } else {
                    printLog(Constants.tag, "audio null")
                }


                //  doneBtn.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_not_done));
                //  checkDoneBtnEnable();
                //  doneBtn.setEnabled(false);
                videoProgress!!.resume()
                recordImage!!.setImageDrawable(
                    ContextCompat.getDrawable(
                        context!!,
                        R.drawable.ic_recoding_yes
                    )
                )
                cutVideoBtn!!.visibility = View.GONE
                findViewById<View>(R.id.tabVideoTypeSelection).visibility = View.INVISIBLE
                tabVideoLength!!.visibility = View.GONE
                recordingTimerTxt!!.visibility = View.VISIBLE
                findViewById<View>(R.id.upload_layout).isEnabled = false
                cameraOptions!!.visibility = View.GONE
                photoSlideOptions!!.visibility = View.GONE
                speedSelectionTab!!.visibility = View.GONE
                addSoundTxt!!.isClickable = false
                findViewById<View>(R.id.selectSoundLayout).alpha = 0.5f
                tabRotateCam!!.visibility = View.GONE
            } else if (isRecording) {
                isRecording = false
                videoProgress!!.pause()
                videoProgress!!.addDivider()
                try {
                    if (audio != null) {
                        if (audio!!.isPlaying) {
                            audio!!.pause()
                        }
                    }
                } catch (e: Exception) {
                    printLog(Constants.tag, "Exception: $e")
                }
                try {
                    stopRecording()
                } catch (e: Exception) {
                    Log.d(Constants.tag, "Stop cameraView: $e")
                }


                //  checkDoneBtnEnable();
                cutVideoBtn!!.visibility = View.VISIBLE
                findViewById<View>(R.id.upload_layout).isEnabled = true
                if (videoType == "Video") {
                    recordImage!!.setImageDrawable(
                        ContextCompat.getDrawable(
                            context!!,
                            R.drawable.ic_recoding_no
                        )
                    )
                } else if (videoType == "Photo") {
                    photoSlideOptions!!.visibility = View.VISIBLE
                    recordImage!!.setImageDrawable(
                        ContextCompat.getDrawable(
                            context!!,
                            R.drawable.ic_capture_photo
                        )
                    )
                } else {
                    recordImage!!.setImageDrawable(
                        ContextCompat.getDrawable(
                            context!!,
                            R.drawable.ic_recoding_story_no
                        )
                    )
                }
                if (isSpeedMode) {
                    speedSelectionTab!!.visibility = View.VISIBLE
                }
                cameraOptions!!.visibility = View.VISIBLE
                tabRotateCam!!.visibility = View.VISIBLE
                if (speedTabPosition != 2) {
                    applySpeedFunctionality(from)
                } else {
                    val intputPath = videopaths[videopaths.size - 1]
                    try {
                        val file = File(intputPath)
                        if (file.exists()) {
                            val ishorizontal = isWidthGreaterThanHeight(intputPath)
                            if (ishorizontal) {
                                rotateVideo(intputPath, from)
                            } else if (from == "done") {
                                combineAllVideos()
                            }
                        }
                    } catch (e: Exception) {
                    }
                }
            } else if (secPassed > Constants.RECORDING_DURATION / 1000) {
                showAlert(
                    this@VideoRecoderActivity,
                    this@VideoRecoderActivity.getString(R.string.alert),
                    this@VideoRecoderActivity.getString(R.string.video_only_can_be_a) + " " + Constants.RECORDING_DURATION / 1000 + " S"
                )
            }
        }
    }

    private fun takePictureOnGlThread(textureId: Int) {
        mIsShooting = false
        val ratio: ARGMedia.Ratio
        ratio = if (mScreenRatio == ARGFrame.Ratio.RATIO_FULL) {
            ARGMedia.Ratio.RATIO_16_9
        } else if (mScreenRatio == ARGFrame.Ratio.RATIO_4_3) {
            ARGMedia.Ratio.RATIO_4_3
        } else {
            ARGMedia.Ratio.RATIO_1_1
        }
        val fileName = System.currentTimeMillis().toString() + ".png"
        val dirPath = File(
            getAppFolder(
                context!!
            ) + Variables.APP_STORY_EDITED_FOLDER
        )
        val filePath = File(dirPath, fileName)
        makeDirectryAndRefresh(context!!, dirPath.absolutePath, fileName)
        mARGMedia!!.takePicture(textureId, filePath.absolutePath, ratio)
        runOnUiThread {
            if (filePath != null && !TextUtils.isEmpty("" + filePath)) {
                if (uploadPhotoPath.size < Constants.MAX_PICS_ALLOWED_FOR_VIDEO) {
                    uploadPhotoPath.add(filePath.absolutePath)
                    photoUploadAdapter!!.notifyDataSetChanged()
                } else {
                    val message =
                        Constants.MAX_PICS_ALLOWED_FOR_VIDEO.toString() + " " + context!!.getString(
                            R.string.pics_allow_only
                        )
                    showToastOnTop(this@VideoRecoderActivity, null, message)
                }
                updatePhotoUploadStatus()
            } else {
                showToastOnTop(
                    this@VideoRecoderActivity,
                    null,
                    context!!.getString(R.string.invalid_photo_format)
                )
            }
        }
    }

    private fun updatePhotoUploadStatus() {
        if (uploadPhotoPath.size > 0) {
            doneBtn!!.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_done_red))
            doneBtn!!.isEnabled = true
        } else {
            doneBtn!!.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_not_done))
            doneBtn!!.isEnabled = false
        }
    }

    private fun startRecording(path: String) {
        if (mCamera == null) {
            return
        }
        val bitrate = 10 * 1000 * 1000
        val ratio: ARGMedia.Ratio
        ratio = if (mScreenRatio == ARGFrame.Ratio.RATIO_FULL) {
            ARGMedia.Ratio.RATIO_16_9
        } else if (mScreenRatio == ARGFrame.Ratio.RATIO_4_3) {
            ARGMedia.Ratio.RATIO_4_3
        } else {
            ARGMedia.Ratio.RATIO_1_1
        }
        val previewSize = mCamera!!.previewSize
        mARGMedia!!.initRecorder(
            path,
            previewSize[0],
            previewSize[1], bitrate,
            false,
            false,
            false,
            ratio
        )
        mARGMedia!!.startRecording()
    }

    private fun makeFiveSecVideo(photoPaths: ArrayList<String>) {
        clearFilesCacheBeforeOperation(
            File(
                getAppFolder(
                    context!!
                ) + Variables.outputfile2
            )
        )
        showDeterminentLoader(this@VideoRecoderActivity, false, false)
        createImageVideo(
            this@VideoRecoderActivity, photoPaths
        ) { bundle ->
            if (bundle.getString("action") == "success") {
                cancelDeterminentLoader()
                Log.d(Constants.tag, "pathpath: " + bundle.getString("path"))
                try {
                    copyFile(
                        File("" + bundle.getString("path")), File(
                            getAppFolder(
                                context!!
                            ) + Variables.outputfile2
                        )
                    )
                } catch (e: Exception) {
                    printLog(Constants.tag, "" + e)
                }
                clearFilesCacheBeforeOperation(File(bundle.getString("path")))
                goToPreviewActivity()
            } else if (bundle.getString("action") == "failed") {
                cancelDeterminentLoader()
                printLog(Constants.tag, getString(R.string.invalid_video_format))
            } else if (bundle.getString("action") == "cancel") {
                cancelDeterminentLoader()
                printLog(Constants.tag, getString(R.string.invalid_video_format))
            } else if (bundle.getString("action") == "process") {
                val message = bundle.getString("message")
                try {
                    val progressPercentage = CalculateFFMPEGTimeToPercentage(
                        message!!, Constants.MAX_TIME_FOR_VIDEO_PICS
                    )
                    showLoadingProgress(progressPercentage)
                } catch (e: Exception) {
                    printLog(Constants.tag, "Exception: $e")
                }
            }
        }
    }

    fun checkDoneBtnEnable() {

//        if(!videopaths.isEmpty()) {
//            doneBtn.setVisibility(View.VISIBLE);
//        }
//        else {
//            doneBtn.setVisibility(View.INVISIBLE);
//        }
        if (timeInMilis > 1000) {
            doneBtn!!.visibility = View.VISIBLE
            doneBtn!!.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_done_red))
            doneBtn!!.isEnabled = true
        } else {
            doneBtn!!.visibility = View.INVISIBLE
            doneBtn!!.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_not_done))
            doneBtn!!.isEnabled = false
        }
    }

    // this will combine all the videos parts in one  fullvideo
    private fun combineAllVideos() {
        if (videopaths.size <= 0) {
            return
        }
        for (i in videopaths.indices) {
            if (isFileSizeLessThan50KB(videopaths[i])) {
                videopaths.removeAt(i)
            }
        }
        val outputFilePath = getAppFolder(this@VideoRecoderActivity) + Variables.outputfile2
        showDeterminentLoader(this@VideoRecoderActivity, false, false)
        ConcatenateMultipleVideos(this@VideoRecoderActivity, videopaths, outputFilePath) { bundle ->
            if (bundle.getString("action") == "success") {
                cancelDeterminentLoader()
                goToPreviewActivity()
            } else if (bundle.getString("action") == "failed") {
                cancelDeterminentLoader()
                printLog(Constants.tag, getString(R.string.invalid_video_format))
            } else if (bundle.getString("action") == "cancel") {
                cancelDeterminentLoader()
                printLog(Constants.tag, getString(R.string.invalid_video_format))
            } else if (bundle.getString("action") == "process") {
                val message = bundle.getString("message")
                try {
                    val progressPercentage = CalculateFFMPEGTimeToPercentage(
                        message!!, Constants.MAX_TIME_FOR_VIDEO_PICS
                    )
                    showLoadingProgress(progressPercentage)
                } catch (e: Exception) {
                    printLog(Constants.tag, "Exception: $e")
                }
            }
        }
    }

    fun removeLastSection(deleteFilePath: String?) {
        try {
            val file = File(deleteFilePath)
            if (file.exists()) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, Uri.fromFile(file))
                val hasVideo =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
                val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                var timeInMillisec = time!!.toLong()
                timeInMillisec = calculateExectChunkTime(videopaths, timeInMilis, timeInMillisec)
                val isVideo = "yes" == hasVideo
                if (isVideo) {
                    timeInMilis = timeInMilis - timeInMillisec
                    videoProgress!!.removeDivider()
                    videopaths.removeAt(videopaths.size - 1)
                    videoProgress!!.updateProgress(timeInMilis)
                    videoProgress!!.back_countdown(timeInMillisec)
                    if (audio != null) {
                        val audio_backtime = (audio!!.currentPosition - timeInMillisec).toInt()
                        audio!!.seekTo(audio_backtime)
                    }
                    secPassed = (timeInMilis / 1000).toInt()
                    checkDoneBtnEnable()
                }
                clearFilesCacheBeforeOperation(file)
            }
            if (videopaths.isEmpty()) {
                findViewById<View>(R.id.tabVideoTypeSelection).visibility = View.VISIBLE
                tabVideoLength!!.visibility = View.VISIBLE
                recordingTimerTxt!!.visibility = View.GONE
                doneBtn!!.visibility = View.INVISIBLE
                cutVideoBtn!!.visibility = View.GONE
                addSoundTxt!!.isClickable = true
                findViewById<View>(R.id.selectSoundLayout).alpha = 1.0f
                tabRotateCam!!.visibility = View.VISIBLE
                if (audio != null) {
                    preparedAudio()
                }
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "removeLastSection: $e")
        }
    }

    private fun calculateExectChunkTime(
        videopaths: ArrayList<String>,
        totalTime: Long,
        chunkTime: Long
    ): Long {
        var totalTime = totalTime
        for (path in videopaths) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, Uri.fromFile(File(path)))
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            totalTime = totalTime - time!!.toLong()
        }
        var adjustedTime = totalTime / videopaths.size
        adjustedTime = adjustedTime + chunkTime
        return adjustedTime
    }

    @SuppressLint("WrongConstant")
    override fun onClick(v: View) {
        when (v.id) {
            R.id.tabRotateCam -> {
                mARGSession!!.pause()
                mCamera!!.changeCameraFacing()
                mARGSession!!.resume()
            }

            R.id.upload_layout -> {
                if (videoType == "Photo") {
                    pickPhotoFromGallery()
                } else {
                    pickVideoFromGallery()
                }
            }

            R.id.done -> {
                Log.d(Constants.tag, "done click")
                if (videoType == "Photo") {
                    makeFiveSecVideo(uploadPhotoPath)
                } else {
                    if (isRecording) {
                        startOrStopRecording("done")
                    } else {
                        combineAllVideos()
                    }
                }
            }

            R.id.cut_video_btn -> showAlert(
                this@VideoRecoderActivity,
                "",
                getString(R.string.descard_the_last_clip_),
                getString(R.string.delete).uppercase(
                    Locale.getDefault()
                ),
                getString(R.string.cancel_).uppercase(Locale.getDefault())
            ) { resp ->
                if (resp.equals("yes", ignoreCase = true)) {
                    if (videopaths.size > 0) {
                        removeLastSection(videopaths[videopaths.size - 1])
                    }
                }
            }

            R.id.tabFlash -> {
                if (isFlashOn) {
                    try {
                        mCameraManager!!.setTorchMode(mCameraId!!, false)
                    } catch (e: Exception) {
                        printLog(Constants.tag, "Exception: $e")
                    }
                    isFlashOn = false
                    ivFlash!!.setImageDrawable(
                        ContextCompat.getDrawable(
                            context!!,
                            R.drawable.ic_flash_on
                        )
                    )
                } else {
                    try {
                        mCameraManager!!.setTorchMode(mCameraId!!, true)
                    } catch (e: Exception) {
                        printLog(Constants.tag, "Exception: $e")
                    }
                    isFlashOn = true
                    ivFlash!!.setImageDrawable(
                        ContextCompat.getDrawable(
                            context!!,
                            R.drawable.ic_flash_off
                        )
                    )
                }
            }

            R.id.goBack -> onBackPressed()
            R.id.add_sound_txt -> {
                val intent = Intent(this, SoundListMainActivity::class.java)
                resultCallback.launch(intent)
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
            }

            R.id.tabTimer -> if (secPassed + 1 < Constants.RECORDING_DURATION / 1000) {
                val recordingTimeRang_f = RecordingTimeRangFragment { bundle ->
                    if (bundle != null) {
                        isRecordingTimerEnable = true
                        recordingTime = bundle.getInt("end_time")
                        countdownTimerTxt!!.text = "3"
                        countdownTimerTxt!!.visibility = View.VISIBLE
                        recordImage!!.isClickable = false
                        val scaleAnimation: Animation = ScaleAnimation(
                            1.0f, 0.0f, 1.0f, 0.0f,
                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                        )
                        object : CountDownTimer(4000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                countdownTimerTxt!!.text = "" + millisUntilFinished / 1000
                                countdownTimerTxt!!.animation = scaleAnimation
                            }

                            override fun onFinish() {
                                recordImage!!.isClickable = true
                                countdownTimerTxt!!.visibility = View.GONE
                                startOrStopRecording("")
                            }
                        }.start()
                    }
                }
                val bundle = Bundle()
                if (secPassed < Constants.RECORDING_DURATION / 1000 - 3) bundle.putInt(
                    "end_time",
                    secPassed + 3
                ) else bundle.putInt("end_time", secPassed + 1)
                bundle.putInt("total_time", Constants.RECORDING_DURATION / 1000)
                recordingTimeRang_f.arguments = bundle
                recordingTimeRang_f.show(supportFragmentManager, "")
            }

            R.id.tabSpeed -> {
                if (isSpeedMode) {
                    isSpeedMode = false
                    speedSelectionTab!!.visibility = View.GONE
                } else {
                    isSpeedMode = true
                    speedSelectionTab!!.visibility = View.VISIBLE
                }
            }

            R.id.tabFeature -> {
                openFeatureDialogue()
            }

            R.id.tabFunny -> {
                openFunnyDialogue()
            }

            R.id.tabFilter -> {
                openFilterDialogue()
            }

            R.id.tvUploadVideo -> {
                val param = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                param.addRule(RelativeLayout.CENTER_HORIZONTAL)
                tvUploadVideo!!.layoutParams = param
                val param3 = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                param3.addRule(RelativeLayout.END_OF, R.id.tvUploadVideo)
                tvUploadStory!!.layoutParams = param3
                tvUploadVideo!!.setTextColor(ContextCompat.getColor(context!!, R.color.whiteColor))
                tvUploadStory!!.setTextColor(ContextCompat.getColor(context!!, R.color.graycolor2))
                clearCacheFiles()
                videoType = "Video"
                updateViewsAccordingToType()
                Constants.RECORDING_DURATION = timerSelectedDuration
                checkDoneBtnEnable()
                setupVideoProgress()
            }

            R.id.tvUploadStory -> {
                val param2 = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                param2.addRule(RelativeLayout.CENTER_HORIZONTAL)
                tvUploadStory!!.layoutParams = param2
                val param3 = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                param3.addRule(RelativeLayout.START_OF, R.id.tvUploadStory)
                tvUploadVideo!!.layoutParams = param3
                tvUploadVideo!!.setTextColor(ContextCompat.getColor(context!!, R.color.graycolor2))
                tvUploadStory!!.setTextColor(ContextCompat.getColor(context!!, R.color.whiteColor))
                clearCacheFiles()
                videoType = "Story"
                updateViewsAccordingToType()
                Constants.RECORDING_DURATION = 30 * 1000
                checkDoneBtnEnable()
                setupVideoProgress()
            }

            else -> return
        }
    }

    private fun openFunnyDialogue() {
        val fragment = BulgeFragment()
        fragment.show(supportFragmentManager, "BulgeFragment")
    }

    private fun openFeatureDialogue() {
        val fragment = BeautyFragment()
        val args = Bundle()
        args.putSerializable(BeautyFragment.BEAUTY_PARAM1, mScreenRatio)
        fragment.arguments = args
        fragment.show(supportFragmentManager, "BeautyFragment")
    }

    private fun openFilterDialogue() {
        val fragment = StickerFragment()
        fragment.show(supportFragmentManager, "StickerFragment")
    }

    private fun pickPhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultCallbackForGallery.launch(intent)
    }

    var resultCallbackForGallery = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {

            override fun onActivityResult(result: ActivityResult?) {
                if (result!!.resultCode == RESULT_OK) {
                    val data = result.data
                    val selectedImage = data!!.data
                    var filePath: String?
                    if (selectedImage!!.scheme == "content") {
                        val cursor =
                            context!!.contentResolver.query(selectedImage, null, null, null, null)
                        cursor!!.moveToFirst()
                        filePath =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                        cursor.close()
                    } else {
                        filePath = selectedImage.path
                    }
                    val outputBitmap = convertImage(filePath)
                    val outputFilepath = getBitmapToUri(
                        this@VideoRecoderActivity,
                        outputBitmap,
                        "uploadPhoto" + getCurrentDate("yyyy-MM-dd HH:mm:ss") + ".jpg"
                    )
                    filePath = outputFilepath!!.absolutePath
                    if (filePath != null && !TextUtils.isEmpty("" + filePath)) {
                        if (uploadPhotoPath.size < Constants.MAX_PICS_ALLOWED_FOR_VIDEO) {
                            uploadPhotoPath.add(filePath)
                            photoUploadAdapter!!.notifyDataSetChanged()
                        } else {
                            val message =
                                Constants.MAX_PICS_ALLOWED_FOR_VIDEO.toString() + " " + context!!.getString(
                                    R.string.pics_allow_only
                                )
                            showToastOnTop(this@VideoRecoderActivity, null, message)
                        }
                        updatePhotoUploadStatus()
                    } else {
                        showToastOnTop(
                            this@VideoRecoderActivity,
                            null,
                            context!!.getString(R.string.invalid_photo_format)
                        )
                    }
                }
            }
        })

    private fun updateViewsAccordingToType() {
        if (videoType == "Video") {
            tabVideoLength!!.visibility = View.VISIBLE
            speedSelectionTab!!.visibility = View.VISIBLE
            videoProgress!!.visibility = View.VISIBLE
            tabSpeed!!.visibility = View.VISIBLE
            photoSlideOptions!!.visibility = View.GONE
            recordImage!!.setImageDrawable(
                ContextCompat.getDrawable(
                    context!!,
                    R.drawable.ic_recoding_no
                )
            )
        } else if (videoType == "Photo") {
            speedSelectionTab!!.visibility = View.INVISIBLE
            videoProgress!!.visibility = View.INVISIBLE
            tabSpeed!!.visibility = View.GONE
            photoSlideOptions!!.visibility = View.VISIBLE
            recordImage!!.setImageDrawable(
                ContextCompat.getDrawable(
                    context!!,
                    R.drawable.ic_capture_photo
                )
            )
        } else {
            tabVideoLength!!.visibility = View.INVISIBLE
            speedSelectionTab!!.visibility = View.VISIBLE
            videoProgress!!.visibility = View.VISIBLE
            tabSpeed!!.visibility = View.VISIBLE
            photoSlideOptions!!.visibility = View.GONE
            recordImage!!.setImageDrawable(
                ContextCompat.getDrawable(
                    context!!,
                    R.drawable.ic_recoding_story_no
                )
            )
        }
    }

    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result!!.resultCode == RESULT_OK) {
                    val data = result.data
                    if (data != null) {
                        isSelected = data.getStringExtra("isSelected")
                        if (isSelected == "yes") {
                            addSoundTxt!!.text = data.getStringExtra("name")
                            Variables.selectedSoundId = data.getStringExtra("sound_id")
                            preparedAudio()
                        }
                    }
                }
            }
        })

    // open the intent for get the video from gallery
    fun pickVideoFromGallery() {
        val fileTrim = File(
            getAppFolder(
                context!!
            ) + Variables.gallery_trimed_video
        )
        val fileFilter = File(
            getAppFolder(
                context!!
            ) + Variables.output_filter_file
        )
        clearFilesCacheBeforeOperation(fileTrim, fileFilter)
        val intent = Intent()
        intent.setType("video/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        takeOrSelectVideoResultLauncher.launch(Intent.createChooser(intent, "Select Video"))
    }

    private fun openTrimActivity(data: String) {
        Variables.isCompressionApplyOnStart = false
        TrimVideo.activity(data)
            .setTrimType(TrimType.DEFAULT) // Use the default trim type if you don't want to set min and max values
            .setTitle("") // Set an appropriate title for the trim activity
            .setMaxTimeCheck(Constants.RECORDING_DURATION) // Set the maximum time check if necessary
            .start(this, videoTrimResultLauncher)
    }

    // change the video size
    fun changeVideoSize(src_path: String?, destination_path: String?) {
        try {
            val destinationFile=File(destination_path)
            copyFile(
                File(src_path),
                File(destination_path)
            )
            val file = File(src_path)
            if (file.exists()) file.delete()


            if(destinationFile.exists()) {
                printLog(Constants.tag, "video path:$destination_path")
            }
            else{
                printLog(Constants.tag, "video path not exist $destination_path ")
            }

            val intent = Intent(this, PreviewStoryVideoActivity::class.java)
            intent.putExtra("fromWhere", "video_recording")
            intent.putExtra("isSoundSelected", isSelected)
            intent.putExtra("soundName", "" + addSoundTxt!!.text.toString())
            intent.putExtra("videoType", videoType)
            startActivity(intent)
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            printLog(Constants.tag, e.toString())
        }
    }

    // this will play the sound with the video when we select the audio
    var audio: MediaPlayer? = null
    fun preparedAudio() {
        val file = File(getAppFolder(this) + Variables.SelectedAudio_AAC)
        if (file.exists()) {
            try {
                audio = MediaPlayer()
                try {
                    audio!!.setDataSource(getAppFolder(this) + Variables.SelectedAudio_AAC)
                    audio!!.prepare()
                } catch (e: Exception) {
                    e.printStackTrace()
                    printLog(Constants.tag, "audio exception:$e")
                }
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(this, Uri.fromFile(file))
                val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val file_duration = parseInterger(durationStr)
                printLog(Constants.tag, "File Duration:$file_duration")
                if (file_duration < Constants.MAX_RECORDING_DURATION) {
                    Constants.RECORDING_DURATION = file_duration
                    setupVideoProgress()
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
                Toast.makeText(
                    this,
                    getString(R.string.you_cannot_create_video_using_this_sound),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        onresume()
    }

    protected fun onresume() {
        super.onResume()
        if (mARGSession == null) {
            val config = ARGConfig(
                Constants.API_URL,
                Constants.API_KEY_ARGEAR,
                Constants.SECRET_KEY,
                Constants.AUTH_KEY
            )
            val inferenceConfig: Set<ARGInferenceConfig.Feature> =
                EnumSet.of(ARGInferenceConfig.Feature.FACE_HIGH_TRACKING)
            mARGSession = ARGSession(this@VideoRecoderActivity, config, inferenceConfig)
            mARGMedia = ARGMedia(mARGSession)
            mScreenRenderer = ScreenRenderer()
            mCameraTexture = CameraTexture()
            setBeauty(beautyItemData!!.beautyValues)
            initGLView()
            initCamera()
        }
        mCamera!!.startCamera()
        mARGSession!!.resume()
        setGLViewSize(mCamera!!.previewSize)
    }

    private fun setGLViewSize(cameraPreviewSize: IntArray) {
        val previewWidth = cameraPreviewSize[1]
        val previewHeight = cameraPreviewSize[0]
        if (mScreenRatio == ARGFrame.Ratio.RATIO_FULL) {
            gLViewHeight = mDeviceHeight
            gLViewWidth = (mDeviceHeight.toFloat() * previewWidth / previewHeight).toInt()
        } else {
            gLViewWidth = mDeviceWidth
            gLViewHeight = (mDeviceWidth.toFloat() * previewHeight / previewWidth).toInt()
        }
        if (mGlView != null
            && (gLViewWidth != mGlView!!.viewWidth || gLViewHeight != mGlView!!.viewHeight)
        ) {
            cameraLayout!!.removeView(mGlView)
            mGlView!!.holder.setFixedSize(gLViewWidth, gLViewHeight)
            cameraLayout!!.addView(mGlView)
        }
    }

    private fun initGLView() {
        cameraLayout = findViewById(R.id.camera_layout)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        mGlView = GLView(this, glViewListener)
        mGlView!!.setZOrderMediaOverlay(true)
        cameraLayout!!.addView(mGlView, params)
    }

    private fun initCamera() {
        mCamera = ReferenceCamera(this, cameraListener, windowManager.defaultDisplay.rotation)
    }

    var glViewListener: GLViewListener = object : GLViewListener {
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            mScreenRenderer!!.create(gl, config)
            mCameraTexture!!.createCameraTexture()
        }

        override fun onDrawFrame(gl: GL10, width: Int, height: Int) {
            if (mCameraTexture == null && mCameraTexture!!.surfaceTexture == null) {
                return
            }
            if (mCamera != null) {
                mCamera!!.setCameraTexture(
                    mCameraTexture!!.textureId,
                    mCameraTexture!!.surfaceTexture
                )
            }
            val frame = mARGSession!!.drawFrame(gl, mScreenRatio, width, height)
            mScreenRenderer!!.draw(frame, width, height)
            if (mHasTrigger) updateTriggerStatus(frame.itemTriggerFlag)
            if (mARGMedia != null) {
                if (mARGMedia!!.isRecording) mARGMedia!!.updateFrame(frame.textureId)
                if (mIsShooting) takePictureOnGlThread(frame.textureId)
            }
            if (mUseARGSessionDestroy) mARGSession!!.destroy()
        }
    }
    var cameraListener: CameraListener = object : CameraListener {
        override fun setConfig(
            previewWidth: Int,
            previewHeight: Int,
            verticalFov: Float,
            horizontalFov: Float,
            orientation: Int,
            isFrontFacing: Boolean,
            fps: Float
        ) {
            mARGSession!!.setCameraConfig(
                ARGCameraConfig(
                    previewWidth,
                    previewHeight,
                    verticalFov,
                    horizontalFov,
                    orientation,
                    isFrontFacing,
                    fps
                )
            )
        }

        override fun feedRawData(data: ByteArray) {
            mARGSession!!.feedRawData(data)
        }

        // endregion
        // region - for camera api 2
        override fun feedRawData(data: Image) {
            mARGSession!!.feedRawData(data)
        } // endregion
    }

    fun updateTriggerStatus(triggerstatus: Int) {
        runOnUiThread {
            if (mCurrentStickeritem != null && mHasTrigger) {
                var strTrigger: String? = null
                if (triggerstatus and 1 != 0) {
                    strTrigger = "Open your mouth."
                } else if (triggerstatus and 2 != 0) {
                    strTrigger = "Move your head side to side."
                } else if (triggerstatus and 8 != 0) {
                    strTrigger = "Blink your eyes."
                } else {
                    if (mTriggerToast != null) {
                        mTriggerToast!!.cancel()
                        mTriggerToast = null
                    }
                }
                if (strTrigger != null) {
                    mTriggerToast =
                        Toast.makeText(this@VideoRecoderActivity, strTrigger, Toast.LENGTH_SHORT)
                    mTriggerToast!!.setGravity(Gravity.CENTER, 0, 0)
                    mTriggerToast!!.show()
                    mHasTrigger = false
                }
            }
        }
    }

    fun setFilter(item: ItemModel) {
        val filePath = mItemDownloadPath + "/" + item.uuid
        if (getLastUpdateAt(this@VideoRecoderActivity) > getFilterUpdateAt(
                this@VideoRecoderActivity,
                item.uuid
            )
        ) {
            FileDeleteAsyncTask(File(filePath)) {
                printLog(Constants.tag, "file delete success!")
                setFilterUpdateAt(
                    this@VideoRecoderActivity,
                    item.uuid,
                    getLastUpdateAt(this@VideoRecoderActivity)
                )
                requestSignedUrl(item, filePath, false)
            }.execute()
        } else {
            if (File(filePath).exists()) {
                setItem(ARGContents.Type.FilterItem, filePath, item)
            } else {
                requestSignedUrl(item, filePath, false)
            }
        }
    }

    fun setItem(type: ARGContents.Type, path: String?, itemModel: ItemModel) {
        mCurrentStickeritem = null
        mHasTrigger = false
        mARGSession!!.contents().setItem(type, path, itemModel.uuid, object : ARGContents.Callback {
            override fun onSuccess() {
                if (type == ARGContents.Type.ARGItem) {
                    mCurrentStickeritem = itemModel
                    mHasTrigger = itemModel.hasTrigger
                }
            }

            override fun onError(e: Throwable) {
                mCurrentStickeritem = null
                mHasTrigger = false
                if (e is InvalidContentsException) {
                    printLog(Constants.tag, "InvalidContentsException")
                }
            }
        })
    }

    private fun requestSignedUrl(item: ItemModel, path: String, isArItem: Boolean) {
        progressBar!!.visibility = View.VISIBLE
        mARGSession!!.auth()
            .requestSignedUrl(item.zipFileUrl, item.title, item.type, object : ARGAuth.Callback {
                override fun onSuccess(url: String) {
                    requestDownload(path, url, item, isArItem)
                }

                override fun onError(e: Throwable) {
                    if (e is SignedUrlGenerationException) {
                        printLog(Constants.tag, "SignedUrlGenerationException !! ")
                    } else if (e is NetworkException) {
                        printLog(Constants.tag, "NetworkException !!")
                    }
                    progressBar!!.visibility = View.INVISIBLE
                }
            })
    }

    private fun requestDownload(
        targetPath: String,
        url: String,
        item: ItemModel,
        isSticker: Boolean
    ) {
        DownloadAsyncTask(targetPath, url) { result ->
            progressBar!!.visibility = View.INVISIBLE
            if (result) {
                if (isSticker) {
                    setItem(ARGContents.Type.ARGItem, targetPath, item)
                } else {
                    setItem(ARGContents.Type.FilterItem, targetPath, item)
                }
                printLog(Constants.tag, "download success!")
            } else {
                printLog(Constants.tag, "download failed!")
            }
        }.execute()
    }

    fun setSticker(item: ItemModel) {
        val filePath = mItemDownloadPath + "/" + item.uuid
        if (getLastUpdateAt(this@VideoRecoderActivity) > getStickerUpdateAt(
                this@VideoRecoderActivity,
                item.uuid
            )
        ) {
            FileDeleteAsyncTask(File(filePath)) {
                printLog(Constants.tag, "file delete success!")
                setStickerUpdateAt(
                    this@VideoRecoderActivity,
                    item.uuid,
                    getLastUpdateAt(this@VideoRecoderActivity)
                )
                requestSignedUrl(item, filePath, true)
            }.execute()
        } else {
            if (File(filePath).exists()) {
                setItem(ARGContents.Type.ARGItem, filePath, item)
            } else {
                requestSignedUrl(item, filePath, true)
            }
        }
    }

    fun setMeasureSurfaceView(view: View) {
        if (view.parent is FrameLayout) {
            view.layoutParams = FrameLayout.LayoutParams(gLViewWidth, gLViewHeight)
        } else if (view.parent is RelativeLayout) {
            view.layoutParams = RelativeLayout.LayoutParams(gLViewWidth, gLViewHeight)
        }
        if (mScreenRatio == ARGFrame.Ratio.RATIO_FULL && gLViewWidth > mDeviceWidth) {
            view.x = ((mDeviceWidth - gLViewWidth) / 2).toFloat()
        } else {
            view.x = 0f
        }
    }

    fun clearBulge() {
        try {
            mARGSession!!.contents().clear(ARGContents.Type.Bulge)
        } catch (e: Exception) {
        }
    }

    fun setBulgeFunType(type: Int) {
        var bulgeType = BulgeType.NONE
        when (type) {
            1 -> bulgeType = BulgeType.FUN1
            2 -> bulgeType = BulgeType.FUN2
            3 -> bulgeType = BulgeType.FUN3
            4 -> bulgeType = BulgeType.FUN4
            5 -> bulgeType = BulgeType.FUN5
            6 -> bulgeType = BulgeType.FUN6
        }
        mARGSession!!.contents().setBulge(bulgeType)
    }

    fun setBeauty(params: FloatArray?) {
        mARGSession!!.contents().setBeauty(params)
    }

    fun clearFilter() {
        mARGSession!!.contents().clear(ARGContents.Type.FilterItem)
    }

    fun setFilterStrength(strength: Int) {
        if (mFilterLevel + strength < 100 && mFilterLevel + strength > 0) {
            mFilterLevel += strength
        }
        mARGSession!!.contents().setFilterLevel(mFilterLevel)
    }

    fun setVignette() {
        mFilterVignette = !mFilterVignette
        mARGSession!!.contents()
            .setFilterOption(ARGContents.FilterOption.VIGNETTING, mFilterVignette)
    }

    fun setBlurVignette() {
        mFilterBlur = !mFilterBlur
        mARGSession!!.contents().setFilterOption(ARGContents.FilterOption.BLUR, mFilterBlur)
    }

    private fun getLastUpdateAt(context: Context): Long {
        return PreferenceUtil.getLongValue(context, AppConfig.USER_PREF_NAME, "ContentLastUpdateAt")
    }

    private fun getFilterUpdateAt(context: Context, itemId: String): Long {
        return PreferenceUtil.getLongValue(context, AppConfig.USER_PREF_NAME_FILTER, itemId)
    }

    private fun setFilterUpdateAt(context: Context, itemId: String, updateAt: Long) {
        PreferenceUtil.putLongValue(context, AppConfig.USER_PREF_NAME_FILTER, itemId, updateAt)
    }

    fun clearStickers() {
        mCurrentStickeritem = null
        mHasTrigger = false
        mARGSession!!.contents().clear(ARGContents.Type.ARGItem)
    }

    private fun getStickerUpdateAt(context: Context, itemId: String): Long {
        return PreferenceUtil.getLongValue(context, AppConfig.USER_PREF_NAME_STICKER, itemId)
    }

    private fun setStickerUpdateAt(context: Context, itemId: String, updateAt: Long) {
        PreferenceUtil.putLongValue(context, AppConfig.USER_PREF_NAME_STICKER, itemId, updateAt)
    }

    override fun onDestroy() {
        releaseResources()
        ondestroy()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        if (mARGSession != null) {
            mCamera!!.stopCamera()
            mARGSession!!.pause()
        }
    }

    protected fun ondestroy() {
        if (mARGSession != null) {
            mCamera!!.destroy()
            mUseARGSessionDestroy = true
        }
        try {
            Handler(Looper.getMainLooper()).postDelayed({
                runOnUiThread {
                    clearBulge()
                    clearStickers()
                    clearFilter()
                }
            }, 2000)
        } catch (e: Exception) {
            Log.d(Constants.tag, "Argear not init")
        }
    }

    fun releaseResources() {
        try {
            if (audio != null) {
                audio!!.stop()
                audio!!.reset()
                audio!!.release()
            }
            stopRecording()
        } catch (e: Exception) {
        }
    }

    private fun stopRecording() {
        mARGMedia!!.stopRecording()
    }

    // show a alert before close the activity
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.alert))
            .setMessage(getString(R.string.are_you_sure_if_you_back))
            .setNegativeButton(getString(R.string.no)) { dialog, which -> dialog.dismiss() }
            .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                dialog.dismiss()
                releaseResources()
                finish()
                overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom)
            }.show()
    }

    fun applySpeedFunctionality(from: String) {
        val intputPath = getAppFolder(this) + (Variables.videoChunk + number) + ".mp4"
        var second = 5
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(intputPath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            second = Integer.valueOf(duration) / 1000
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
        }
        showDeterminentLoader(this@VideoRecoderActivity, false, false)
        val finalSecond = second
        val frameRate = Integer.valueOf(getTrimVideoFrameRate(File("" + intputPath).absolutePath))
        videoSpeedProcess(
            this@VideoRecoderActivity, intputPath,
            speedTabPosition, frameRate
        ) { bundle ->
            if (bundle.getString("action") == "success") {
                cancelDeterminentLoader()
                val ishorizontal = isWidthGreaterThanHeight(intputPath)
                if (!ishorizontal) {
                    val index = videopaths.size - 1
                    videopaths.removeAt(index)
                    Log.d(Constants.tag, "index:$index path:$intputPath")
                    videopaths.add(index, intputPath)
                    if (from == "done") {
                        combineAllVideos()
                    }
                } else {
                    rotateVideo(intputPath, from)
                }
            } else if (bundle.getString("action") == "failed") {
                cancelDeterminentLoader()
                printLog(Constants.tag, getString(R.string.invalid_video_format))
            } else if (bundle.getString("action") == "cancel") {
                cancelDeterminentLoader()
                printLog(Constants.tag, getString(R.string.invalid_video_format))
            } else if (bundle.getString("action") == "process") {
                val message = bundle.getString("message")
                try {
                    val progressPercentage = CalculateFFMPEGTimeToPercentage(
                        message!!, finalSecond
                    )
                    showLoadingProgress(progressPercentage)
                } catch (e: Exception) {
                }
            }
        }
    }

    fun rotateVideo(intputPath: String, from: String) {
        var second = 5
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(intputPath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            second = Integer.valueOf(duration) / 1000
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
        }
        showDeterminentLoader(this@VideoRecoderActivity, false, false)
        val finalSecond = second
        rotateVideoToPotrate(
            this@VideoRecoderActivity, intputPath
        ) { bundle ->
            if (bundle.getString("action") == "success") {
                cancelDeterminentLoader()
                val index = videopaths.size - 1
                videopaths.removeAt(index)
                Log.d(Constants.tag, "index:$index path:$intputPath")
                videopaths.add(index, intputPath)
                if (from == "done") {
                    combineAllVideos()
                }
            } else if (bundle.getString("action") == "failed") {
                cancelDeterminentLoader()
                printLog(Constants.tag, getString(R.string.invalid_video_format))
            } else if (bundle.getString("action") == "cancel") {
                cancelDeterminentLoader()
                printLog(Constants.tag, getString(R.string.invalid_video_format))
            } else if (bundle.getString("action") == "process") {
                val message = bundle.getString("message")
                try {
                    val progressPercentage = CalculateFFMPEGTimeToPercentage(
                        message!!, finalSecond
                    )
                    showLoadingProgress(progressPercentage)
                } catch (e: Exception) {
                }
            }
        }
    }

    fun goToPreviewActivity() {
        Variables.isCompressionApplyOnStart = true
        val intent = Intent(this, PreviewStoryVideoActivity::class.java)
        intent.putExtra("fromWhere", "video_recording")
        intent.putExtra("isSoundSelected", isSelected)
        intent.putExtra("soundName", "" + addSoundTxt!!.text.toString())
        intent.putExtra("videoType", videoType)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    // this will hide the bottom mobile navigation controll
    fun hideNavigation() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // This work only for android 4.4+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = flags

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            val decorView = window.decorView
            decorView
                .setOnSystemUiVisibilityChangeListener { visibility ->
                    if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                        decorView.systemUiVisibility = flags
                    }
                }
        }
    }
}
