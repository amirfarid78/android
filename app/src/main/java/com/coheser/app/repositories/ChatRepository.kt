package com.coheser.app.repositories

import androidx.lifecycle.MutableLiveData
import com.coheser.app.models.InboxModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository {

   suspend fun updateInboxCount(userId:String,_inboxCount:MutableLiveData<Int>) {

        withContext(Dispatchers.IO){
            val inboxQuery = FirebaseDatabase.getInstance().reference.child("Inbox")
                .child(userId)
                .orderByChild("date")
            inboxQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var notReadInboxlist: ArrayList<InboxModel> = ArrayList<InboxModel>()
                    if (dataSnapshot.exists()) {
                        notReadInboxlist.clear()
                        for (ds in dataSnapshot.children) {
                            val model: InboxModel? = ds.getValue(InboxModel::class.java)
                            model?.setId(ds.key)
                            if (model?.getStatus() != null && model.getStatus().equals("0")) {
                                notReadInboxlist.add(model)
                            }
                        }
                    }

                    _inboxCount.value = notReadInboxlist.size


                }

                override fun onCancelled(error: DatabaseError) {
                    _inboxCount.value = 0

                }
            })
        }

    }
}