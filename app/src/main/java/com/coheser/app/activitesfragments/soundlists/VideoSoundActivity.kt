package com.coheser.app.activitesfragments.soundlists

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.WatchVideosActivity
import com.coheser.app.activitesfragments.videorecording.VideoRecoderActivity
import com.coheser.app.adapters.MyVideosAdapter
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.ActivityVideoSoundBinding
import com.coheser.app.models.HomeModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.DataHolder.Companion.instance
import com.coheser.app.simpleclasses.DataParsing.parseVideoData
import com.coheser.app.simpleclasses.Dialogs.showAlert
import com.coheser.app.simpleclasses.Downloading.DownloadFiles
import com.coheser.app.simpleclasses.FileUtils.getAppFolder
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkLoginUser
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.isStringHasValue
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class VideoSoundActivity : AppCompatLocaleActivity(), View.OnClickListener {
    private var item: HomeModel? = null
    private var audioFile: File? = null
    private var linearLayoutManager: GridLayoutManager? = null
    private var pageCount: Int = 0
    private var ispostFinsh: Boolean = false
    private var dataList: ArrayList<HomeModel>? = null
    private var adapter: MyVideosAdapter? = null

    private var binding: ActivityVideoSoundBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(
                this@VideoSoundActivity
            ).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this, javaClass, false
        )
        binding = ActivityVideoSoundBinding.inflate(layoutInflater)

        setContentView(binding!!.root)


        val intent = intent
        if (intent.hasExtra("data")) {
            item = intent.getParcelableExtra<Parcelable>("data") as HomeModel?
        }

        binding!!.backBtn.setOnClickListener(this)
        binding!!.useAudioBtn.setOnClickListener(this)
        binding!!.playBtn.setOnClickListener(this)
        binding!!.pauseBtn.setOnClickListener(this)
        binding!!.favbtn.setOnClickListener(this)


        printLog(Constants.tag, item!!.getSound_pic())
        printLog(Constants.tag, item!!.getSound_url_acc())

        saveAudio()


        linearLayoutManager = GridLayoutManager(this@VideoSoundActivity, 3)
        binding!!.recylerview.layoutManager = linearLayoutManager

        dataList = ArrayList()

        adapter = MyVideosAdapter(this, dataList!!, "sound") { view, pos, `object` ->
            val item = `object` as HomeModel
            openWatchVideo(pos)
        }
        binding!!.recylerview.adapter = adapter

        binding!!.recylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var userScrolled: Boolean = false
            var scrollOutitems: Int = 0

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                scrollOutitems = linearLayoutManager!!.findLastVisibleItemPosition()

                printLog("resp", "" + scrollOutitems)
                if (userScrolled && (scrollOutitems == dataList!!.size - 1)) {
                    userScrolled = false

                    if (binding!!.loadMoreProgress.visibility != View.VISIBLE && !ispostFinsh) {
                        binding!!.loadMoreProgress.visibility = View.VISIBLE
                        pageCount = pageCount + 1
                        callApi()
                    }
                }
            }
        })


        setData()


        pageCount = 0
        callApi()
    }


    fun setData() {
        binding!!.usernametxt.text = getString(R.string.created_by) + item!!.userModel!!.username
        binding!!.soundImage.controller = frescoImageLoad(
            item!!.getSound_pic(), R.drawable.ractengle_solid_primary, binding!!.soundImage, false
        )


        if (item!!.soundFav != null && item!!.soundFav.equals("1", ignoreCase = true)) {
            binding!!.favImg.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_fav_fill
                )
            )
            binding!!.favtxt.text = getString(R.string.added_to_favourite)
        } else {
            binding!!.favImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fav))
            binding!!.favtxt.text = getString(R.string.add_to_favourite)
        }
    }


    private fun callApiForFavSound() {
        val parameters = JSONObject()
        try {
            parameters.put("sound_id", item!!.sound_id)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        showLoader(this, false, false)
        VolleyRequest.JsonPostRequest(
            this, ApiLinks.addSoundFavourite, parameters, getHeaders(
                this
            )
        ) { resp ->
            checkStatus(this@VideoSoundActivity, resp)
            cancelLoader()

            if (item!!.soundFav == "1") item!!.soundFav = "0"
            else item!!.soundFav = "1"
            setData()
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.back_btn -> onBackPressed()
            R.id.favbtn -> if (checkLoginUser(this@VideoSoundActivity)) {
                callApiForFavSound()
            }

            R.id.useAudioBtn -> if (checkLoginUser(this@VideoSoundActivity)) {
                if (audioFile != null && audioFile!!.exists()) {
                    stopPlaying()
                    openVideoRecording()
                }
            }

            R.id.play_btn -> if (audioFile != null && audioFile!!.exists()) playaudio()

            R.id.pause_btn -> stopPlaying()
        }
    }

    fun downloadAEAudio(path: String, audioName: String) {
        val valuesaudio = ContentValues()
        valuesaudio.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
        valuesaudio.put(MediaStore.MediaColumns.TITLE, audioName)
        valuesaudio.put(MediaStore.Audio.Media.ARTIST, "")
        valuesaudio.put(MediaStore.Audio.Media.ALBUM, "")
        valuesaudio.put(MediaStore.MediaColumns.DISPLAY_NAME, audioName)
        valuesaudio.put(MediaStore.MediaColumns.MIME_TYPE, "audio/aac")
        valuesaudio.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
        valuesaudio.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
        valuesaudio.put(MediaStore.MediaColumns.IS_PENDING, 1)
        val resolver = contentResolver
        val uriSavedAudio =
            resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, valuesaudio)

        val pfd: ParcelFileDescriptor?

        try {
            pfd = contentResolver.openFileDescriptor(uriSavedAudio!!, "w")

            val out = FileOutputStream(pfd!!.fileDescriptor)

            val audioFile = File(path + audioName)

            val `in` = FileInputStream(audioFile)


            val buf = ByteArray(1024)
            var len: Int
            while ((`in`.read(buf).also { len = it }) > 0) {
                out.write(buf, 0, len)
            }


            out.close()
            `in`.close()
            pfd.close()


            showAlert(
                this@VideoSoundActivity,
                this@VideoSoundActivity.getString(R.string.audio_saved),
                this@VideoSoundActivity.getString(R.string.this_sound_is_successfully_saved)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }


        valuesaudio.clear()
        valuesaudio.put(MediaStore.MediaColumns.IS_PENDING, 0)
        contentResolver.update(uriSavedAudio!!, valuesaudio, null, null)
    }

    fun scanFile(downloadDirectory: String) {
        MediaScannerConnection.scanFile(
            this@VideoSoundActivity,
            arrayOf(downloadDirectory + Variables.SelectedAudio_AAC),
            null
        ) { path, uri ->
            showAlert(
                this@VideoSoundActivity,
                this@VideoSoundActivity.getString(R.string.audio_saved),
                this@VideoSoundActivity.getString(R.string.this_sound_is_successfully_saved)
            )
        }
    }


    // get the video list sound id
    fun callApi() {
        val params = JSONObject()
        try {
            params.put("sound_id", item!!.sound_id)
            params.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this, ApiLinks.showVideosAgainstSound, params, getHeaders(
                this
            )
        ) { resp ->
            checkStatus(this@VideoSoundActivity, resp)
            parseVideo(resp)
        }
    }


    // parse the data of the video list against sound id
    fun parseVideo(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgArray = jsonObject.getJSONArray("msg")

                val temp_list = ArrayList<HomeModel>()

                for (i in 0 until msgArray.length()) {
                    val itemdata = msgArray.optJSONObject(i)

                    val video = itemdata.optJSONObject("Video")
                    val user = itemdata.optJSONObject("User")
                    val sound = itemdata.optJSONObject("Sound")
                    val location = itemdata.optJSONObject("Location")
                    val store = itemdata.optJSONObject("Store")
                    val videoProduct = itemdata.optJSONObject("Product")
                    val userPrivacy = user.optJSONObject("PrivacySetting")
                    val userPushNotification = user.optJSONObject("PushNotification")

                    val item = parseVideoData(
                        user,
                        sound,
                        video,
                        location,
                        store,
                        videoProduct,
                        userPrivacy,
                        userPushNotification
                    )
                    if (isStringHasValue(item.userModel!!.username)) {
                        temp_list.add(item)
                    }
                }

                if (temp_list.isEmpty()) ispostFinsh = true
                else {
                    dataList!!.addAll(temp_list)
                    adapter!!.notifyDataSetChanged()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            binding!!.loadMoreProgress.visibility = View.GONE
        }
    }


    // open the video in full screen
    private fun openWatchVideo(postion: Int) {
        val intent = Intent(this@VideoSoundActivity, WatchVideosActivity::class.java)
        val args = Bundle()
        args.putSerializable("arraylist", dataList)
        instance!!.data = args

        intent.putExtra("position", postion)
        intent.putExtra("pageCount", pageCount)
        intent.putExtra("soundId", item!!.sound_id)
        intent.putExtra(
            "userId",
            getSharedPreference(this@VideoSoundActivity).getString(Variables.U_ID, "")
        )
        intent.putExtra("whereFrom", Variables.videoSound)
        startActivity(intent)
    }


    var player: SimpleExoPlayer? = null
    fun playaudio() {
        val trackSelector = DefaultTrackSelector(this)

        player = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()

        val cacheDataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            this@VideoSoundActivity, getString(R.string.app_name)
        )
        val videoSource: MediaSource =
            ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(
                MediaItem.fromUri(
                    item!!.getSound_url_mp3()!!
                )
            )
        player?.setMediaSource(videoSource)
        player?.prepare()
        player?.setPlayWhenReady(true)

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()
            player?.setAudioAttributes(audioAttributes, true)
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception audio focus : $e")
        }

        showPlayingState()
    }


    fun stopPlaying() {
        if (player != null) {
            player!!.playWhenReady = false
        }
        showPauseState()
    }


    override fun onBackPressed() {
        stopPlaying()
        finish()
    }

    override fun onStop() {
        super.onStop()
        stopPlaying()
        printLog(Constants.tag, "onStop")
    }


    // show the player state
    fun showPlayingState() {
        binding!!.playBtn.visibility = View.GONE
        binding!!.pauseBtn.visibility = View.VISIBLE
    }

    fun showPauseState() {
        binding!!.playBtn.visibility = View.VISIBLE
        binding!!.pauseBtn.visibility = View.GONE
    }

    var progressDialog: ProgressDialog? = null

    fun saveAudio() {
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage(getString(R.string.please_wait_))
        progressDialog!!.setCancelable(false)
        progressDialog!!.setCanceledOnTouchOutside(false)
        progressDialog!!.show()

        val fileName =  Variables.SelectedAudio_AAC.replace(".mp3","")
        CoroutineScope(Dispatchers.IO).launch {

            val file=  DownloadFiles.downloadFileWithProgress(item!!.getSound_url_acc().toString(),
                fileName,
                "mp3",
                File(getAppFolder(this@VideoSoundActivity)),
                progressCallback = { byteprogress, totalByte ->

                }
            )

            if(file?.exists()==true){
                CoroutineScope(Dispatchers.Main).launch {
                    progressDialog!!.dismiss()
                    audioFile = File(getAppFolder(this@VideoSoundActivity) + Variables.SelectedAudio_AAC)
                    showPauseState()
                }
            }

        }

    }


    // open the camera for recording video
    fun openVideoRecording() {
        val intent = Intent(this@VideoSoundActivity, VideoRecoderActivity::class.java)
        intent.putExtra("name", binding!!.soundName.text.toString())
        intent.putExtra("sound_id", item!!.sound_id)
        intent.putExtra("isSelected", "yes")
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }


}
