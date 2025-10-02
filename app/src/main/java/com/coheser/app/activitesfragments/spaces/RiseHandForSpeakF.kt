package com.coheser.app.activitesfragments.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.databinding.FragmentRiseHandForSpeakBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.realpacific.clickshrinkeffect.applyClickShrink

class RiseHandForSpeakF : BottomSheetDialogFragment, View.OnClickListener {
    lateinit var binding: FragmentRiseHandForSpeakBinding
    var callBack: FragmentCallBack? = null

    constructor(callBack: FragmentCallBack?) {
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
            R.layout.fragment_rise_hand_for_speak,
            container,
            false
        )
        initControl()
        return binding.getRoot()
    }

    private fun initControl() {
        binding.tabRiseHandForSpeak.setOnClickListener(this)
        binding.tabRiseHandForSpeak.applyClickShrink()
        binding.tabNeverMind.setOnClickListener(this)
        binding.tabNeverMind.applyClickShrink()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tabRiseHandForSpeak -> {
                performAction("riseHandForSpeak")
            }

            R.id.tabNeverMind -> {
                performAction("neverMind")
            }
        }
    }

    private fun performAction(action: String) {
        val bundle = Bundle()
        bundle.putBoolean("isShow", true)
        bundle.putString("action", action)
        callBack!!.onResponce(bundle)
        dismiss()
    }
}