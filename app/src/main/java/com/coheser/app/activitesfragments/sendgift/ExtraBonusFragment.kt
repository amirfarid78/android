package com.coheser.app.activitesfragments.sendgift

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.databinding.FragmentExtraBonusBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ExtraBonusFragment(val callBack: FragmentCallBack) : BottomSheetDialogFragment() {

    lateinit var binding: FragmentExtraBonusBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_extra_bonus,container,false)

        binding.btnGetBonus.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("type","bonus")
            callBack.onResponce(bundle)
            dismiss()
        }
        binding.btnMaybeLater.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("type","later")
            callBack.onResponce(bundle)
            dismiss()
        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(callBack: FragmentCallBack) =
            ExtraBonusFragment(callBack).apply {
            }
    }
}