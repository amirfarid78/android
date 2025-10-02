package com.coheser.app.activitesfragments.livestreaming.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.StreamingConstants
import com.coheser.app.activitesfragments.livestreaming.adapter.PkBattleAdapter
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentPkBattleInviteBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.Dialogs.showAlert
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.isStringHasValue
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.TicTicApp.Companion.allOnlineUser
import com.coheser.app.simpleclasses.Variables
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.volley.plus.VPackages.VolleyRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class PkBattleInviteFragment(var fragmentCallBack: FragmentCallBack) : BottomSheetDialogFragment() {

    var rootref: DatabaseReference? = null
    lateinit var binding: FragmentPkBattleInviteBinding
    var friendsDataList: ArrayList<LiveUserModel> = ArrayList()
    var recomendedDataList: ArrayList<LiveUserModel> = ArrayList()
    var friendsAdapter: PkBattleAdapter? = null
    var recomendedAdapter: PkBattleAdapter? = null
    var dataList: ArrayList<LiveUserModel> = ArrayList()
    var valueEventListener: ChildEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootref = FirebaseDatabase.getInstance().reference
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pk_battle_invite, container, false)


        binding.recyclerviewFriends.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewFriends.setHasFixedSize(true)
        friendsAdapter = PkBattleAdapter(requireContext(), friendsDataList) { view, pos, `object` ->
            if (view.id == R.id.action_txt) {
                val liveUserModel = `object` as LiveUserModel
                val bundle = Bundle()
                bundle.putParcelable("data", liveUserModel)
                fragmentCallBack.onResponce(bundle)
                dismiss()
            }
        }
        binding.recyclerviewFriends.adapter = friendsAdapter


        binding.recyclerviewRecomended.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewRecomended.setHasFixedSize(true)
        recomendedAdapter = PkBattleAdapter(requireContext(), recomendedDataList) { view, pos, `object` ->
            if (view.id == R.id.action_txt) {
                val liveUserModel = `object` as LiveUserModel
                val bundle = Bundle()
                bundle.putParcelable("data", liveUserModel)
                fragmentCallBack.onResponce(bundle)
                dismiss()
            }
        }
        binding.recyclerviewRecomended.adapter = recomendedAdapter


        addDataListener()
        return binding.getRoot()
    }

    fun addDataListener() {
        valueEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                if (dataSnapshot.exists()) {
                    printLog(Constants.tag, dataSnapshot.toString())

                    val model = dataSnapshot.getValue(LiveUserModel::class.java)
                    if (isStringHasValue(model!!.getUserId()) && !getSharedPreference(
                            context
                        ).getString(Variables.U_ID, "").equals(
                            model.getUserId(), ignoreCase = true
                        )
                    ) {
                        if (allOnlineUser.containsKey(model.getUserId())) {
                            if (model.getOnlineType().equals("multicast", ignoreCase = true)) {
                                dataList.add(model)
                                timerCallApi()
                            }
                        }
                    }
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val model = dataSnapshot.getValue(LiveUserModel::class.java)
                    if (model!!.getUserId() != null && !(TextUtils.isEmpty(model.getUserId())) && !(TextUtils.isEmpty(
                            model.getUserId()
                        )) && model.getUserId() != "null"
                    ) {
                        for (i in friendsDataList.indices) {
                            if (model.getUserId() == friendsDataList[i].getUserId()) {
                                friendsDataList.removeAt(i)
                            }
                        }
                        for (i in recomendedDataList.indices) {
                            if (model.getUserId() == recomendedDataList[i].getUserId()) {
                                recomendedDataList.removeAt(i)
                            }
                        }

                        for (i in dataList.indices) {
                            if (model.getUserId() == dataList[i].getUserId()) {
                                dataList.removeAt(i)
                            }
                        }


                        friendsAdapter?.notifyDataSetChanged()
                        recomendedAdapter?.notifyDataSetChanged()

                        if (friendsDataList.isEmpty()) {
                            binding.nodataFriends.visibility = View.VISIBLE
                        } else {
                            binding.nodataFriends.visibility = View.GONE
                        }

                        if (recomendedDataList.isEmpty()) {
                            binding.nodataRecommend.visibility = View.VISIBLE
                        } else {
                            binding.nodataRecommend.visibility = View.GONE
                        }
                    }
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }

        rootref!!.child(StreamingConstants.liveStreamingUsers).addChildEventListener(valueEventListener!!)
    }

    fun removeDataListener() {
        if (valueEventListener != null && rootref != null) {
            rootref!!.child(StreamingConstants.liveStreamingUsers).removeEventListener(valueEventListener!!)
        }
    }

    private var job:Job?=null
    fun timerCallApi() {
        if(job?.isActive==true) return
        job = CoroutineScope(Dispatchers.Main).launch {
           delay(1000)
            callApi()
        }
    }

    fun callApi() {
        val params = JSONObject()
        try {
            params.put("user_id", Variables.sharedPreferences.getString(Variables.U_ID, ""))

            val array = JSONArray()
            for (i in dataList.indices) {
                val jsonObject = JSONObject()
                jsonObject.put("user_id", dataList[i]!!.getUserId())
                array.put(jsonObject)
            }
            params.put("users", array)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.showUsers, params, getHeaders(
                activity
            )
        ) { resp ->
            checkStatus(activity, resp)
            cancelLoader()
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code != null && code == "200") {
                    val msg = jsonObject.optJSONArray("msg")

                    friendsDataList.clear()
                    recomendedDataList.clear()

                    for (i in 0 until msg.length()) {
                        val data = msg.optJSONObject(i)
                        val user = data.optJSONObject("User")
                        val button = user.optString("button")
                        if (button.equals("friends", ignoreCase = true)) {
                            dataList[i]?.let { friendsDataList.add(it) }
                        } else {
                            recomendedDataList.add(dataList[i])
                        }
                    }

                    if (friendsDataList.isEmpty()) {
                        binding.nodataFriends.visibility = View.VISIBLE
                    } else {
                        binding.nodataFriends.visibility = View.GONE
                    }

                    if (recomendedDataList.isEmpty()) {
                        binding.nodataRecommend.visibility = View.VISIBLE
                    } else {
                        binding.nodataRecommend.visibility = View.GONE
                    }


                    friendsAdapter?.notifyDataSetChanged()
                    recomendedAdapter?.notifyDataSetChanged()
                } else {
                    showAlert(
                        activity,
                        requireContext().applicationContext.getString(R.string.server_error),
                        jsonObject.optString("msg")
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        removeDataListener()
        super.onDestroy()
    }

    companion object {
        fun newInstance(fragmentCallBack: FragmentCallBack): PkBattleInviteFragment {
            val fragment = PkBattleInviteFragment(fragmentCallBack)
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}