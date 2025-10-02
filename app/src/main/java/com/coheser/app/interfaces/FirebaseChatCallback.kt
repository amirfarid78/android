package com.coheser.app.interfaces

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

interface FirebaseChatCallback {
    fun onDataChange(dataSnapshot: DataSnapshot)
    fun onCancelled(databaseError: DatabaseError)
}