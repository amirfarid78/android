package com.coheser.app.activitesfragments.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.activitesfragments.spaces.models.HomeUserModel
import com.coheser.app.databinding.FragmentRoomStreamingSettingBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Variables
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.realpacific.clickshrinkeffect.applyClickShrink

class RoomStreamingSettingF : BottomSheetDialogFragment, View.OnClickListener {
    lateinit var binding: FragmentRoomStreamingSettingBinding
    var callBack: FragmentCallBack? = null
    var currentUserList: ArrayList<HomeUserModel>? = null
    var myUserModel: HomeUserModel? = null

    constructor(currentUserList: ArrayList<HomeUserModel>?, callBack: FragmentCallBack?) {
        this.currentUserList = currentUserList
        this.callBack = callBack
    }

    constructor()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_room_streaming_setting,
            container,
            false
        )
        InitControl()
        return binding.getRoot()
    }

    private fun InitControl() {
       binding.tvShareRoom.setOnClickListener(this)
       binding.tvShareRoom.applyClickShrink()
       binding.tvEndRoom.setOnClickListener(this)
       binding.tvEndRoom.applyClickShrink()
       binding.tvUserShareRoom.setOnClickListener(this)
       binding.tvUserShareRoom.applyClickShrink()
       binding.tvUserReportRoomTitle.setOnClickListener(this)
       binding.tvUserReportRoomTitle.applyClickShrink()

        setupSctreenData()
    }

    private fun setupSctreenData() {
        setupButtonLogic()
    }

    private fun setupButtonLogic() {
        for (myModel in currentUserList!!) {
            if (myModel.userModel?.id == getSharedPreference(
                    context
                ).getString(Variables.U_ID, "")
            ) {
                myUserModel = myModel
            }
        }


        //moderator
        if (myUserModel != null && myUserModel!!.userRoleType == "1") {
           binding.tabOwner.visibility = View.VISIBLE
           binding.tabOther.visibility = View.GONE
        } else if (myUserModel != null && myUserModel!!.userRoleType == "2") {
           binding.tabOwner.visibility = View.GONE
           binding.tabOther.visibility = View.VISIBLE
        } else  //user
        {
           binding.tabOwner.visibility = View.GONE
           binding.tabOther.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvShareRoom -> {
                perFormAction("ShareRoom")
            }

            R.id.tvEndRoom -> {
                perFormAction("EndRoom")
            }

            R.id.tvUserShareRoom -> {
                perFormAction("UserShareRoom")
            }

            R.id.tvUserReportRoomTitle -> {
                perFormAction("UserReportRoomTitle")
            }
        }
    }

    private fun perFormAction(action: String) {
        val bundle = Bundle()
        bundle.putBoolean("isShow", true)
        bundle.putString("action", action)
        callBack!!.onResponce(bundle)
        dismiss()
    }
}