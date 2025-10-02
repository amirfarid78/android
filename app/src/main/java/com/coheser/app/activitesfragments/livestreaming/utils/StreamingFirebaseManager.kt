package com.coheser.app.activitesfragments.livestreaming.utils

import android.content.Context
import android.text.TextUtils
import com.coheser.app.Constants
import com.coheser.app.activitesfragments.livestreaming.StreamingConstants
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.TicTicApp.Companion.allOnlineUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class StreamingFirebaseManager(activity: Context) {
    var rootref: DatabaseReference = FirebaseDatabase.getInstance().reference.child(StreamingConstants.liveStreamingUsers)
    val userList = ObservableArrayList<LiveUserModel>()

    companion object {
        @Volatile
        var INSTANCE: StreamingFirebaseManager? = null
        fun getInstance(context: Context): StreamingFirebaseManager? {
            if (INSTANCE == null) {
                synchronized(StreamingFirebaseManager::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = StreamingFirebaseManager(context)
                    }
                }
            }
            return INSTANCE
        }
    }


    var liveUserEventListener:ChildEventListener?=null
    fun addLiveUserListener() {
        if(liveUserEventListener ==null) {
            userList.clear()
            liveUserEventListener = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

                    if (dataSnapshot.exists()) {
                        try {
                            val model = dataSnapshot.getValue(LiveUserModel::class.java)
                            if (model!!.getUserId() != null && !(TextUtils.isEmpty(model.getUserId())) && model.getUserId() != "null") {
                                if (allOnlineUser.containsKey(model.getUserId())) {
                                    userList.add(model)
                                } else {
                                    removeStreamingHead(dataSnapshot.key)
                                }
                            } else {
                                removeStreamingHead(dataSnapshot.key)
                            }
                        } catch (e: Exception) {
                            printLog(Constants.tag, e.message)
                        }
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                    if (dataSnapshot.exists()) {
                        try {
                            val model = dataSnapshot.getValue(LiveUserModel::class.java)
                            if (model!!.getUserId() != null && !(TextUtils.isEmpty(model.getUserId()))) {
                                for (i in userList.indices) {
                                    if (model.getUserId() == userList[i].getUserId()) {
                                        userList.set(i, model)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            printLog(Constants.tag, e.message)
                        }
                    }
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    try {
                        if (dataSnapshot.exists()) {
                            val model = dataSnapshot.getValue(LiveUserModel::class.java)
                            if (model!!.getUserId() != null && !(TextUtils.isEmpty(model.getUserId())) && !(TextUtils.isEmpty(
                                    model.getUserId()
                                )) && model.getUserId() != "null"
                            ) {
                                for (i in userList.indices) {
                                    if (model.getUserId() == userList[i]!!.getUserId()) {
                                        userList.removeAt(i)
                                    }
                                }

                            }
                        }
                    } catch (e: Exception) {
                    }
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
            rootref.addChildEventListener(liveUserEventListener!!)
        }
    }

    private fun removeStreamingHead(key: String?) {
        if (key == null || key.isEmpty()) {
            return
        }
        rootref.child(key).removeValue { error, ref ->
            com.google.android.exoplayer2.util.Log.d(
                Constants.tag, "Remove: $error"
            )
        }
    }


    fun removeListerner() {
        if (liveUserEventListener != null && rootref!=null) {
            rootref.removeEventListener(liveUserEventListener!!)
            liveUserEventListener=null
        }
    }




}