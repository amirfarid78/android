package com.coheser.app.activitesfragments.spaces.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.spaces.voicecallmodule.openacall.VoiceStreamingNonUiChat
import com.coheser.app.mainmenu.MainMenuActivity
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.TicTicApp
import com.coheser.app.simpleclasses.Variables
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RoomStreamService : Service() {

    var reference: DatabaseReference? = null
    var roomId: String? = null
    var userId: String? = null
    override fun onCreate() {
        super.onCreate()
        reference = FirebaseDatabase.getInstance().reference
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if ((intent?.action != null) && (intent?.action == "start")) {
            val title = intent.getStringExtra("title")
            val message = intent.getStringExtra("message")
            roomId = intent.getStringExtra("roomId")
            userId = intent.getStringExtra("userId")

            showForgroundService(title, message)
            startRoomStreaming(roomId, userId)
        } else if ((intent?.action != null) && (intent?.action == "stop")) {
            stopForeground(true)
            stopSelf()
        }
        return START_STICKY
    }


    private fun startRoomStreaming(roomId: String?, userId: String?) {
        streamingInstance = VoiceStreamingNonUiChat(application as TicTicApp)
        streamingInstance!!.setChannelNameAndUid("" + roomId, "" + userId)
        streamingInstance!!.startStream { }

        onlineUser()
    }


    private fun showForgroundService(title: String?, message: String?) {
        val notificationIntent = Intent(this, MainMenuActivity::class.java)
        var pendingIntent: PendingIntent? = null
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                applicationContext, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                applicationContext,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val CHANNEL_ID = "default"
        val CHANNEL_NAME = "Default"

        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultChannel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(defaultChannel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    this.resources,
                    R.mipmap.ic_launcher
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setOnlyAlertOnce(false)
            .setOngoing(true)
            .setContentIntent(pendingIntent).build()

        startForeground(101, notification)
    }

    override fun onDestroy() {
        printLog(Constants.tag, "RoomStreamService:onDestroy")
        if (streamingInstance != null) {
            streamingInstance!!.quitCall()
        }
        offlineUser()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    fun onlineUser() {
        val updateMice = HashMap<String, Any>()
        updateMice["online"] = "1"
        reference!!.child(Variables.roomKey).child(roomId!!).child(Variables.roomUsers).child(userId!!).updateChildren(updateMice)
    }

    fun offlineUser() {
        reference!!.child(Variables.roomKey).child(roomId!!).child(Variables.roomUsers).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val updateMice = HashMap<String, Any>()
                    updateMice["online"] = "0"
                    reference!!.child(Variables.roomKey).child(roomId!!).child(Variables.roomUsers).child(userId!!).updateChildren(updateMice)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    companion object {
        @JvmField
        var streamingInstance: VoiceStreamingNonUiChat? = null
    }
}