package com.coheser.app.simpleclasses

import android.content.Context
import com.coheser.app.interfaces.FirebaseChatCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

object FirebaseChatUtil {

    var inboxQuery: Query? = null
    var inboxEventListener: ValueEventListener? = null
    fun registerUserInbox(context: Context,callback: FirebaseChatCallback){
        inboxQuery=FirebaseDatabase.getInstance().reference.child("Inbox")
            .child("${Functions.getSharedPreference(context).getString(Variables.U_ID, "0")}")
            .orderByChild("timestamp")
        inboxEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback.onDataChange(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onCancelled(databaseError)
            }
        }
        inboxEventListener?.let {listener->
            inboxQuery?.addValueEventListener(listener)
        }
    }

    fun unregisterUserInbox(){
        inboxEventListener?.let {listener->
            inboxQuery?.removeEventListener(listener)
            inboxEventListener=null
            inboxQuery=null
        }
    }
}