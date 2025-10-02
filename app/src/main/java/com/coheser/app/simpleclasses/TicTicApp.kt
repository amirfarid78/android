package com.coheser.app.simpleclasses

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ProcessLifecycleOwner
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.danikula.videocache.HttpProxyCacheServer
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.messaging.FirebaseMessaging
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.koin.appModule
import com.coheser.app.simpleclasses.Observers.AppLifecycleObserver
import com.volley.plus.VPackages.VolleyRequest
import io.agora.rtc2.RtcEngine
import io.paperdb.Paper
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.io.File

/**
 * Created by qboxus on 3/18/2019.
 */
class TicTicApp : Application(){

    private var proxy: HttpProxyCacheServer? = null


    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()
        VolleyRequest.initalizeSdk(this)
        appLevelContext = applicationContext

        startKoin {
            androidLogger()
            androidContext(this@TicTicApp)
            modules(appModule) // Load Koin modules
        }

        sharedPreferences=Functions.getSharedPreference(applicationContext);

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver(applicationContext,sharedPreferences))

        Fresco.initialize(
            applicationContext,
            ImagePipelineConfigUtils.getDefaultImagePipelineConfig(
                applicationContext
            )
        )

        Paper.init(applicationContext)
        FirebaseApp.initializeApp(applicationContext)
        addFirebaseToken()

        if (leastRecentlyUsedCacheEvictor == null) {
            leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize)
        }
        if (exoDatabaseProvider == null) {
            exoDatabaseProvider = StandaloneDatabaseProvider(applicationContext)
        }
        if (simpleCache == null) {
            simpleCache =
                SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor!!, exoDatabaseProvider!!)
            if (simpleCache!!.cacheSpace >= 400207768) {
                freeMemory()
            }
        }
        initCrashActivity()
        initConfig()
        FileUtils.createNoMediaFile(applicationContext)

    }


    fun initCrashActivity() {
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
            .enabled(true)
            .showErrorDetails(true)
            .showRestartButton(true)
            .logErrorOnRestart(true)
            .trackActivities(true)
            .minTimeBetweenCrashesMs(2000)
            .restartActivity(com.coheser.app.activitesfragments.CustomErrorActivity::class.java)
            .errorActivity(com.coheser.app.activitesfragments.CustomErrorActivity::class.java)
            .apply()
    }
    var rootref: DatabaseReference? = null

    lateinit var sharedPreferences: SharedPreferences


    fun addFirebaseToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                Log.d(Constants.tag, "token: $token")
                val editor = sharedPreferences.edit()
                editor.putString(Variables.DEVICE_TOKEN, "" + token)
                editor.commit()
            })
    }

    private fun newProxy(): HttpProxyCacheServer {
        return HttpProxyCacheServer.Builder(applicationContext)
            .maxCacheSize((1024 * 1024 * 1024).toLong())
            .maxCacheFilesCount(50)
            .cacheDirectory(
                File(
                    FileUtils.getAppFolder(
                        applicationContext
                    ) + "videoCache"
                )
            )
            .build()
    }

    // check how much memory is available for cache video
    fun freeMemory() {
        try {
            val dir = cacheDir
            deleteDir(dir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        System.runFinalization()
        Runtime.getRuntime().gc()
        System.gc()
    }

    // delete the cache if it is full
    fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }

    private var mRtcEngine: RtcEngine? = null
    private val mGlobalConfig =
        com.coheser.app.activitesfragments.livestreaming.rtc.EngineConfig()
    private val mHandler = com.coheser.app.activitesfragments.livestreaming.rtc.AgoraEventHandler()
    private val mStatsManager =
        com.coheser.app.activitesfragments.livestreaming.stats.StatsManager()

    private fun initConfig() {
        try {
            mRtcEngine =
                RtcEngine.create(applicationContext, getString(R.string.agora_app_id), mHandler)
            mRtcEngine!!.setChannelProfile(io.agora.rtc2.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            mRtcEngine!!.enableVideo()
            mRtcEngine!!.setLogFile(
                com.coheser.app.activitesfragments.livestreaming.utils.FileUtil.initializeLogFile(
                    applicationContext
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
        val pref =
            com.coheser.app.activitesfragments.livestreaming.utils.PrefManager.getPreferences(
                applicationContext
            )

        mGlobalConfig.videoDimenIndex = pref.getInt(
            com.coheser.app.activitesfragments.livestreaming.StreamingConstants.PREF_RESOLUTION_IDX,
            com.coheser.app.activitesfragments.livestreaming.StreamingConstants.DEFAULT_PROFILE_IDX
        )

        val showStats = pref.getBoolean(
            com.coheser.app.activitesfragments.livestreaming.StreamingConstants.PREF_ENABLE_STATS,
            false
        )

        mGlobalConfig.setIfShowVideoStats(false)
        mStatsManager.enableStats(false)
        mGlobalConfig.mirrorLocalIndex = pref.getInt(
            com.coheser.app.activitesfragments.livestreaming.StreamingConstants.PREF_MIRROR_LOCAL,
            0
        )
        mGlobalConfig.mirrorRemoteIndex = pref.getInt(
            com.coheser.app.activitesfragments.livestreaming.StreamingConstants.PREF_MIRROR_REMOTE,
            0
        )
        mGlobalConfig.mirrorEncodeIndex = pref.getInt(
            com.coheser.app.activitesfragments.livestreaming.StreamingConstants.PREF_MIRROR_ENCODE,
            0
        )
    }

    fun engineConfig(): com.coheser.app.activitesfragments.livestreaming.rtc.EngineConfig {
        return mGlobalConfig
    }

    fun rtcEngine(): RtcEngine? {
        return mRtcEngine
    }

    fun statsManager(): com.coheser.app.activitesfragments.livestreaming.stats.StatsManager {
        return mStatsManager
    }

    fun registerEventHandler(handler: com.coheser.app.activitesfragments.livestreaming.rtc.EventHandler?) {
        mHandler.addHandler(handler!!)
    }

    fun removeEventHandler(handler: com.coheser.app.activitesfragments.livestreaming.rtc.EventHandler?) {
        mHandler.removeHandler(handler!!)
    }

    companion object {
        @JvmField
        var appLevelContext: Context? = null
        var simpleCache: SimpleCache? = null
        var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor? = null
        var exoDatabaseProvider: DatabaseProvider? = null
        var exoPlayerCacheSize = (100 * 1024 * 1024).toLong()
        var allOnlineUser = HashMap<String, com.coheser.app.models.UserOnlineModel?>()

        @JvmField
        var allLiveStreaming =
            HashMap<String, com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel?>()

        // below code is for cache the videos in local
        @JvmStatic
        fun getProxy(context: Context): HttpProxyCacheServer {
            val app = context.applicationContext as TicTicApp
            return try {
                if (app.proxy == null) app.newProxy().also { app.proxy = it } else app.proxy!!
            } catch (e: Exception) {
                app.newProxy()
            }
        }


        const val TAG = "BanubaVideoEditor"

        // Please set your license token for Banuba Video Editor SDK
        const val ERR_SDK_NOT_INITIALIZED =
            "Banuba Video Editor SDK is not initialized: license token is unknown or incorrect.\nPlease check your license token or contact Banuba"
        const val ERR_LICENSE_REVOKED =
            "License is revoked or expired. Please contact Banuba https://www.banuba.com/faq/kb-tickets/new"

    }
}