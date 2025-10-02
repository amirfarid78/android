package com.coheser.app.activitesfragments.livestreaming.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.StreamingConstants
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.databinding.FragmentPkBattleInviteSendBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Variables
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PkBattleInviteSendFragment(var fragmentCallBack: FragmentCallBack) :
    BottomSheetDialogFragment(), View.OnClickListener {
    lateinit var binding: FragmentPkBattleInviteSendBinding
    var rootref: DatabaseReference? = null
    var liveUserModel: LiveUserModel? = null
    var streamingId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_pk_battle_invite_send,
            container,
            false
        )

        rootref = FirebaseDatabase.getInstance().reference

        val bundle = arguments

        liveUserModel = bundle!!.getParcelable("data")
        streamingId = bundle.getString("streamingId")

        binding.myImage.controller = frescoImageLoad(
            getSharedPreference(context)
                .getString(Variables.U_PIC, ""), binding.myImage, false
        )

        binding.otherImage.controller = frescoImageLoad(
            liveUserModel!!.getUserPicture(), binding.otherImage, false
        )

        binding.cancelBtn.setOnClickListener(this)


        return binding.getRoot()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.cancelBtn -> {
                rootref!!.child(StreamingConstants.liveStreamingUsers).child(liveUserModel!!.streamingId!!)
                    .child("pkInvitation").removeValue()
                rootref!!.child(StreamingConstants.liveStreamingUsers).child(streamingId!!).child("pkInvitation")
                    .removeValue()
            }
        }
    }

    companion object {
        fun newInstance(
            streamingId: String?,
            liveUserModel: LiveUserModel?,
            fragmentCallBack: FragmentCallBack
        ): PkBattleInviteSendFragment {
            val fragment = PkBattleInviteSendFragment(fragmentCallBack)
            val args = Bundle()
            args.putParcelable("data", liveUserModel)
            args.putSerializable("streamingId", streamingId)
            fragment.arguments = args
            return fragment
        }
    }

}