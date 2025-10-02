package com.coheser.app.simpleclasses.Observers

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.utils.StreamingFirebaseManager
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.TicTicApp.Companion.allOnlineUser
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.launch


class AppLifecycleObserver(private val context: Context,val sharedPreferences: SharedPreferences):DefaultLifecycleObserver {
   private var onlineEventListener: ChildEventListener? = null
   private var rootref: DatabaseReference? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        rootref = FirebaseDatabase.getInstance().reference
    }
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        owner.lifecycleScope.launch {
            ConnectivityObserver(context).obserNetworkConnectivity().collect{
                isConnected->
                if(isConnected){
                    addOnlineListener()
                }
                else{
                    removeOnlineListener()
                    val toast=Toast.makeText(context, R.string.your_network_is_unstable, Toast.LENGTH_SHORT)
                    toast.setGravity(
                        Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                        0,
                        100
                    )
                    toast.show()
                }


            }
        }

        addOnlineListener()
        StreamingFirebaseManager.getInstance(context)?.addLiveUserListener()
        Functions.printLog(Constants.tag, "App moved to foreground")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        removeOnlineListener()
        StreamingFirebaseManager.getInstance(context)?.removeListerner()
        Functions.printLog(Constants.tag, "App moved to Background")
    }



    fun addOnlineListener() {
        if (onlineEventListener == null) {
            addOnlineStatus()
            onlineEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (!TextUtils.isEmpty(snapshot.value.toString())) {
                        val item = snapshot.getValue(
                            com.coheser.app.models.UserOnlineModel::class.java
                        )
                        allOnlineUser[item!!.getUserId()] = item
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    if (!TextUtils.isEmpty(snapshot.value.toString())) {
                        val item = snapshot.getValue(
                            com.coheser.app.models.UserOnlineModel::class.java
                        )
                        allOnlineUser.remove(item!!.getUserId())
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            }
            rootref!!.child(Variables.onlineUser).addChildEventListener(onlineEventListener!!)
        }
    }

    fun removeOnlineListener() {
        if (rootref != null && onlineEventListener != null) {
            removeOnlineStatus()
            rootref!!.child(Variables.onlineUser).removeEventListener(onlineEventListener!!)
            onlineEventListener = null
        }
    }

    private fun removeOnlineStatus() {
        if (sharedPreferences.getBoolean(Variables.IS_LOGIN, false)
        ) {
            rootref!!.child(Variables.onlineUser)
                .child(sharedPreferences.getString(Variables.U_ID, "0")!!)
                .removeValue()
        }
    }

    private fun addOnlineStatus() {
        if (sharedPreferences.getBoolean(Variables.IS_LOGIN, false)
        ) {
            val onlineModel = com.coheser.app.models.UserOnlineModel()
            onlineModel.setUserId(
                sharedPreferences.getString(Variables.U_ID, "0")
            )
            onlineModel.setUserName(
                sharedPreferences.getString(Variables.U_NAME, "")
            )
            onlineModel.setUserPic(
                sharedPreferences.getString(Variables.U_PIC, "")
            )
            rootref!!.child(Variables.onlineUser)
                .child(sharedPreferences.getString(Variables.U_ID, "0")!!)
                .onDisconnect().removeValue()

            rootref!!.child(Variables.onlineUser)
                .child(sharedPreferences.getString(Variables.U_ID, "0")!!)
                .keepSynced(true)

            rootref!!.child(Variables.onlineUser)
                .child(sharedPreferences.getString(Variables.U_ID, "0")!!)
                .setValue(onlineModel)
                .addOnCompleteListener {
                    Log.d(
                        Constants.tag,
                        "addOnlineStatus: " + onlineModel.getUserId()
                    )
                }
        }
    }


}