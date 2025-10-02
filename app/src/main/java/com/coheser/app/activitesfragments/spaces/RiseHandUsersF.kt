package com.coheser.app.activitesfragments.spaces

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.spaces.adapters.RiseHandUsersAdapter
import com.coheser.app.activitesfragments.spaces.models.HomeUserModel
import com.coheser.app.databinding.FragmentRiseHandUsersBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.Dialogs.showError
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Variables
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.realpacific.clickshrinkeffect.applyClickShrink

class RiseHandUsersF : BottomSheetDialogFragment, View.OnClickListener {
    lateinit var binding: FragmentRiseHandUsersBinding
    var adapter: RiseHandUsersAdapter? = null
    var currentUserList: ArrayList<HomeUserModel> = ArrayList()
    var callBack: FragmentCallBack? = null
    var reference: DatabaseReference? = null
    var roomId: String? = null
    var riseHandRule: String? = null

    constructor(roomId: String?, riseHandRule: String?, callBack: FragmentCallBack?) {
        this.roomId = roomId
        this.riseHandRule = riseHandRule
        this.callBack = callBack
    }

    constructor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_rise_hand_users, container, false)
        initControl()
        return binding.getRoot()
    }

    private fun initControl() {
        reference = FirebaseDatabase.getInstance().reference
        binding.tvEdit.setOnClickListener(this)
        binding.tvEdit.applyClickShrink()



        setupAdapter()
        setupScreenData()
    }

    private fun setupScreenData() {
        var rule = ""
        printLog(Constants.tag, "riseHandRule:$riseHandRule")

        if ((riseHandRule == "1")) {
            rule = binding.root.context.getString(R.string.open_to_everyone)
            registerMyRoomListener()
        } else {
            rule = binding.root.context.getString(R.string.off)
            removeMyRoomListener()
            currentUserList.clear()
            adapter!!.notifyDataSetChanged()
        }

        binding.tvRaiseRule.text = "" + rule
    }

    private fun setupAdapter() {
        val layoutManager = LinearLayoutManager(binding.root.context)
        layoutManager.orientation = RecyclerView.VERTICAL
        binding.recylerview.layoutManager = layoutManager
        adapter =
            RiseHandUsersAdapter(currentUserList, AdapterClickListener { view, pos, `object` ->
                val itemUpdate = currentUserList[pos]
                if (view.id == R.id.ivProfile) {
                } else if (view.id == R.id.tabAddToSpeak) {
                    if ((itemUpdate!!.riseHand == "1")) {
                        sendInviteToUserForSpeak(itemUpdate, pos)
                    }
                }
            })
        binding.recylerview.adapter = adapter
    }

    private fun sendInviteToUserForSpeak(itemUpdate: HomeUserModel?, pos: Int) {
        itemUpdate!!.riseHand = "2"
        reference!!.child(Variables.roomKey)
            .child((roomId)!!).child(Variables.roomUsers)
            .child((itemUpdate.userModel!!.id)!!)
            .setValue(itemUpdate)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val bundle = Bundle()
                    bundle.putBoolean("isShow", true)
                    bundle.putString("action", "invite")
                    bundle.putParcelable("itemModel", itemUpdate)
                    callBack!!.onResponce(bundle)

                    currentUserList[pos] = itemUpdate
                    adapter!!.notifyDataSetChanged()
                }
            }
    }

    var myRoomListener: ChildEventListener? = null
    private fun registerMyRoomListener() {
        if (myRoomListener == null) {
            myRoomListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (!(TextUtils.isEmpty(snapshot.value.toString()))) {
                        val itemModel = snapshot.getValue(
                            HomeUserModel::class.java
                        )
                        if ((itemModel!!.riseHand == "1")) {
                            currentUserList.add(itemModel)
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    if (!(TextUtils.isEmpty(snapshot.value.toString()))) {
                        val itemModel = snapshot.getValue(
                            HomeUserModel::class.java
                        )

                        val indexPostion = getlistPostion(currentUserList, itemModel)
                        if (indexPostion >= 0) {
                            if ((itemModel!!.riseHand == "0")) {
                                currentUserList.removeAt(indexPostion)
                            } else {
                                currentUserList[indexPostion] = itemModel
                            }

                            adapter!!.notifyDataSetChanged()
                        } else if (itemModel!!.riseHand != "0") {
                            currentUserList.add(itemModel)
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val dataItem = snapshot.getValue(HomeUserModel::class.java)
                        val indexPostion = getlistPostion(currentUserList, dataItem)
                        if (indexPostion >= 0) {
                            currentUserList.removeAt(indexPostion)
                            adapter!!.notifyItemRemoved(indexPostion)
                        }
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
            reference!!.child(Variables.roomKey).child((roomId)!!).child(Variables.roomUsers)
                .addChildEventListener(myRoomListener!!)
        } else {
            Log.d(Constants.tag, "myRoomListener not null")
        }
    }

    fun removeMyRoomListener() {
        if (reference != null && myRoomListener != null) {
            reference!!.child(Variables.roomKey).child((roomId)!!).removeEventListener(
                myRoomListener!!
            )
            myRoomListener = null
        }
    }


    private fun getlistPostion(
        currentUserList: ArrayList<HomeUserModel>,
        dataItem: HomeUserModel?
    ): Int {
        for (i in currentUserList.indices) {
            if ((currentUserList[i]!!.userModel!!.id == dataItem!!.userModel!!.id)) {
                return i
            }
        }
        return -1
    }

    override fun onDetach() {
        removeMyRoomListener()
        super.onDetach()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvEdit -> {
                openEditMenu()
            }
        }
    }

    private fun openEditMenu() {
        val wrapper: Context =
            ContextThemeWrapper(binding.root.context, R.style.AlertDialogCustom)
        val popup = PopupMenu(wrapper, binding.tvEdit)

        popup.menuInflater.inflate(R.menu.room_rise_hand_rule, popup.menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.gravity = Gravity.TOP or Gravity.END
        }

        val itemOne = popup.menu.getItem(0)
        val itemTwo = popup.menu.getItem(1)

        printLog(Constants.tag, "openEditMenu:riseHandRule:$riseHandRule")

        if ((riseHandRule == "1")) {
            itemOne.setChecked(false)
            itemTwo.setChecked(true)
        } else {
            itemOne.setChecked(true)
            itemTwo.setChecked(false)
        }
        popup.show()

        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.itemOff -> {
                        if (!(itemOne.isChecked)) {
                            val updateRiseRuleMap = HashMap<String, Any>()
                            updateRiseRuleMap["riseHandRule"] = "0"
                            reference!!.child(Variables.roomKey).child((roomId)!!)
                                .updateChildren(updateRiseRuleMap).addOnCompleteListener(
                                OnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        riseHandRule = "0"
                                        showError(
                                            activity,
                                            binding.root.context.getString(R.string.hand_raising_was_turned_off_by_moderators_of_the_room)
                                        )
                                        setupScreenData()
                                    }
                                })
                        }
                    }

                    R.id.itemEveryone -> {
                        if (!(itemTwo.isChecked)) {
                            val updateRiseRuleMap = HashMap<String, Any>()
                            updateRiseRuleMap["riseHandRule"] = "1"
                            reference!!.child(Variables.roomKey).child((roomId)!!)
                                .updateChildren(updateRiseRuleMap)
                                .addOnCompleteListener(object : OnCompleteListener<Void?> {
                                    override fun onComplete(task: Task<Void?>) {
                                        if (task.isSuccessful) {
                                            riseHandRule = "1"
                                            setupScreenData()
                                        }
                                    }
                                })
                        }
                    }
                }
                return true
            }
        })
    }
}