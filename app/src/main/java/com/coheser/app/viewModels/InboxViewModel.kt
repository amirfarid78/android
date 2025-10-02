package com.coheser.app.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.coheser.app.Constants
import com.coheser.app.interfaces.FirebaseChatCallback
import com.coheser.app.models.InboxModel
import com.coheser.app.simpleclasses.FirebaseChatUtil
import com.coheser.app.simpleclasses.Functions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InboxViewModel(
    private val context: Context
):ViewModel() {

    private val _actualList = MutableStateFlow<List<InboxModel>>(emptyList())

    private val _inboxList = MutableStateFlow<List<InboxModel>>(emptyList())
    val inboxList: StateFlow<List<InboxModel>> = _inboxList


    init {
        loadInboxData()
    }

    private fun loadInboxData() {
        FirebaseChatUtil.registerUserInbox(context,object : FirebaseChatCallback {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tempList = mutableListOf<InboxModel>()
                for (ds in dataSnapshot.children) {
                    val model = ds.getValue(InboxModel::class.java)
                    model!!.id = ds.key
                    if (model != null) tempList.add(model)
                }
                _actualList.value=tempList
                _inboxList.value = tempList

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }


    fun filteredInboxList(query: String) {
        Functions.printLog(Constants.tag,"query$query")
         if (query.isEmpty()) {
             _inboxList.value = _actualList.value

        }
        else {
             _inboxList.value = _actualList.value.filter {
                 it.name.lowercase().contains(query)
                         || it.msg.lowercase().contains(query)
             }
         }

    }
}
