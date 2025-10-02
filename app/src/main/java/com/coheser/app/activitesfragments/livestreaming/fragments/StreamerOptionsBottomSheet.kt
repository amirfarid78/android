package com.coheser.app.activitesfragments.livestreaming.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coheser.app.R
import com.coheser.app.databinding.FragmentStreamerOptionsBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class StreamerOptionsBottomSheet(val callBack: FragmentCallBack) : BottomSheetDialogFragment(),View.OnClickListener {

    var isAudioMute=false
    var isVideoActivated=false
    var isJoinInvitation=true

    lateinit var binding:FragmentStreamerOptionsBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog=BottomSheetDialog(requireContext(), R.style.MyTransparentBottomSheetDialogTheme)
            dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStreamerOptionsBinding.inflate(inflater, container, false)

        isAudioMute=requireArguments().getBoolean("isAudioActivated")
        isVideoActivated=requireArguments().getBoolean("isVideoActivated")
        isJoinInvitation=requireArguments().getBoolean("isJoinInvitation")


        binding.microPhoneSwitch.isChecked=isAudioMute
        binding.pauseSwitch.isChecked=!isVideoActivated
        binding.joinInvitationSwitch.isChecked=isJoinInvitation

        binding.microPhoneSwitch.setOnCheckedChangeListener { buttonView, isChecked ->

            val bundle=Bundle()
            bundle.putString("type","muteStreaming")
            if(callBack!=null){
                callBack.onResponce(bundle)
            }

        }
        binding.pauseSwitch.setOnCheckedChangeListener { buttonView, isChecked ->

            val bundle=Bundle()
            bundle.putString("type","pauseLive")
            if(callBack!=null){
                callBack.onResponce(bundle)
            }
        }
        binding.joinInvitationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            val bundle=Bundle()
            bundle.putString("type","joinInvitation")
            if(callBack!=null){
                callBack.onResponce(bundle)
            }
        }

        binding.commentLayout.setOnClickListener(this)
        binding.flipCameraLayout.setOnClickListener(this)
        binding.muteStreamingLayout.setOnClickListener(this)
        binding.pauseStreamingLayout.setOnClickListener(this)
        binding.joinInvitation.setOnClickListener(this)

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(type:String,callBack: FragmentCallBack,
                        isAudioActivated:Boolean,
                        isVideoActivated:Boolean,
                        joinInvitation:Boolean) =
            StreamerOptionsBottomSheet(callBack).apply {
                arguments = Bundle().apply {
                    putString("type", type)
                    putBoolean("isAudioActivated",isAudioActivated)
                    putBoolean("isVideoActivated",isVideoActivated)
                    putBoolean("joinInvitation",joinInvitation)
                }
            }
    }

    override fun onClick(v: View?) {
        val bundle=Bundle()
        when(v?.id){

            R.id.commentLayout -> {
                bundle.putString("type","comment")
            }
            R.id.flipCameraLayout -> {
                bundle.putString("type","flipCamera")
            }
            R.id.muteStreamingLayout -> {
                bundle.putString("type","muteStreaming")
            }
            R.id.pauseStreamingLayout->{
                bundle.putString("type","pauseLive")
            }
            R.id.joinInvitation ->{
                bundle.putString("type","joinInvitation")
            }

        }
        if(callBack!=null){
            callBack.onResponce(bundle)
            dismiss()
        }
    }
}