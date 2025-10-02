package com.coheser.app.activitesfragments.spaces

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.activitesfragments.chat.ChatModel
import com.coheser.app.activitesfragments.spaces.adapters.RoomChatAdapter
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.MainStreamingModel
import com.coheser.app.databinding.FragmentRoomChatBinding
import com.coheser.app.interfaces.AdapterClickListener2
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.hideSoftKeyboard
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Variables
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class RoomChatF : Fragment, View.OnClickListener {
    var mainStreamingModel: MainStreamingModel? = null
    lateinit var binding: FragmentRoomChatBinding
    var fragmentCallBack: FragmentCallBack? = null
    var rootref: DatabaseReference? = null


    constructor(mainStreamingModel: MainStreamingModel?, fragmentCallBack: FragmentCallBack?) {
        this.mainStreamingModel = mainStreamingModel
        this.fragmentCallBack = fragmentCallBack
    }

    constructor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_room_chat, container, false)
        rootref = FirebaseDatabase.getInstance().reference

        initcontrols()

        return binding.getRoot()
    }


    fun initcontrols() {
        binding.sendbtn.setOnClickListener(this)
        binding.goBack.setOnClickListener(this)

        binding.roomTitle.text = mainStreamingModel!!.model?.title + " Chat"

        binding.msgedittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length > 0) {
                    binding.sendbtn.visibility = View.VISIBLE
                } else {
                    binding.sendbtn.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        initAdapter()
        chatData
    }


    var dataList: ArrayList<ChatModel> = ArrayList()
    var adapter: RoomChatAdapter? = null
    var linearLayoutManager: LinearLayoutManager? = null
    fun initAdapter() {
        dataList.clear()
        linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager!!.stackFromEnd = true
        binding.recyclerview.layoutManager = linearLayoutManager

        binding.recyclerview.setHasFixedSize(true)

        adapter = RoomChatAdapter(dataList, getSharedPreference(
            context
        ).getString(Variables.U_ID, "")!!, object : AdapterClickListener2 {
            override fun onItemClick(view: View, pos: Int, `object`: Any) {
            }

            override fun onItemClick(`object`: Any) {
            }
        })
        binding.recyclerview.adapter = adapter


        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

                scrollOutitems = linearLayoutManager!!.findFirstCompletelyVisibleItemPosition()

                if (userScrolled && (scrollOutitems == 0 && dataList.size > 9)) {
                    userScrolled = false
                    rootref!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!)
                        .child(Variables.roomchat).orderByChild("chat_id")
                        .endAt(dataList[0]!!.chat_id).limitToLast(20)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val arrayList = ArrayList<ChatModel>()
                                for (snapshot in dataSnapshot.children) {
                                    val item = snapshot.getValue(ChatModel::class.java)
                                    item?.let { arrayList.add(it) }
                                }
                                for (i in arrayList.size - 2 downTo 0) {
                                    dataList.add(0, arrayList[i])
                                }

                                adapter!!.notifyDataSetChanged()

                                if (arrayList.size > 8) {
                                    binding.recyclerview.scrollToPosition(arrayList.size)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                            }
                        })
                }
            }
        })
    }


    var valueEventListener: ValueEventListener? = null
    var eventListener: ChildEventListener? = null
    var queryGetchat: Query? = null
    private val chatData: Unit
        get() {
            dataList.clear()
            queryGetchat = rootref!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!)
                .child(Variables.roomchat)

            // this will get all the messages between two users
            eventListener = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    try {
                        val model = dataSnapshot.getValue(ChatModel::class.java)
                        model?.let { dataList.add(it) }
                        adapter!!.notifyDataSetChanged()
                        binding.recyclerview.scrollToPosition(dataList.size - 1)
                    } catch (ex: Exception) {
                        Log.e("", ex.message!!)
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                    if (dataSnapshot?.value != null) {
                        try {
                            val model = dataSnapshot.getValue(ChatModel::class.java)

                            for (i in dataList.indices.reversed()) {
                                if (dataList[i]!!.timestamp == dataSnapshot.child("timestamp").value) {
                                    dataList.removeAt(i)
                                    model?.let { dataList.add(i, it) }
                                    break
                                }
                            }
                            adapter!!.notifyDataSetChanged()
                        } catch (ex: Exception) {
                            Log.e("", ex.message!!)
                        }
                    }
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    printLog("", databaseError.message)
                }
            }


            // this will check the two user are do chat before or not
            valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        binding.progressBar.visibility = View.GONE
                        queryGetchat?.removeEventListener(valueEventListener!!)
                    } else {
                        binding.progressBar.visibility = View.GONE
                        queryGetchat?.removeEventListener(valueEventListener!!)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }



            queryGetchat?.limitToLast(20)?.addChildEventListener(eventListener!!)
            rootref!!.child(Variables.roomKey).child(mainStreamingModel?.model?.id!!)
                .child(Variables.roomchat).addValueEventListener(valueEventListener!!)
        }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.goBack -> {
                hideSoftKeyboard(activity)
                parentFragmentManager.popBackStack()
            }

            R.id.sendbtn -> if (!TextUtils.isEmpty(binding.msgedittext.text.toString())) {
                sendMessage(binding.msgedittext.text.toString())
                binding.msgedittext.text = null
            }

        }
    }


    // this will add the new message in chat node and update the ChatInbox by new message by present date
    fun sendMessage(message: String?) {
        val c = Calendar.getInstance().time
        val formattedDate = Variables.df.format(c)

        val current_user_ref =
            Variables.roomKey + "/" + mainStreamingModel?.model?.id + "/" + Variables.roomchat

        val reference = rootref!!.child(Variables.roomKey).child(
            mainStreamingModel?.model?.id!!
        ).child(Variables.roomchat).push()
        val pushid = reference.key

        val message_user_map: HashMap<String, Any> = HashMap<String, Any>()
        message_user_map["receiver_id"] = mainStreamingModel?.model?.id!!
        message_user_map["sender_id"] = getSharedPreference(context).getString(Variables.U_ID, "").toString()
        message_user_map["sender_name"] = getSharedPreference(context).getString(Variables.U_NAME, "").toString()
        message_user_map["pic_url"] = getSharedPreference(context).getString(Variables.U_PIC, "").toString()
        message_user_map["chat_id"] = pushid.toString()
        message_user_map["text"] = message.toString()
        message_user_map["type"] = "text"
        message_user_map["status"] = "0"
        message_user_map["time"] = ""
        message_user_map["timestamp"] = formattedDate

        val user_map: HashMap<String, Any> = HashMap<String, Any>()
        user_map["$current_user_ref/$pushid"] = message_user_map

        rootref!!.updateChildren(
            user_map,
            DatabaseReference.CompletionListener { databaseError, databaseReference ->
                //if first message then set the visibility of whoops layout gone
            })
    }


    override fun onDetach() {
        super.onDetach()
        if (fragmentCallBack != null) {
            val bundle = Bundle()
            bundle.putBoolean("isShow", true)
            fragmentCallBack!!.onResponce(bundle)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(
            mainStreamingModel: MainStreamingModel?,
            fragmentCallBack: FragmentCallBack?
        ): RoomChatF {
            val fragment = RoomChatF(mainStreamingModel, fragmentCallBack)
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}