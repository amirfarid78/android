package com.coheser.app.activitesfragments.livestreaming.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.StreamingConstants
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.databinding.FragmentSettingBottomSheetBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class SettingBottonSheetFragment(val callBack: FragmentCallBack) : BottomSheetDialogFragment() {

    lateinit var model: LiveUserModel
    lateinit var binding:FragmentSettingBottomSheetBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            model = it.getParcelable("data")!!
        }
    }

   override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog=BottomSheetDialog(requireContext(), R.style.MyTransparentBottomSheetDialogTheme)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        arguments?.let {
            model = it.getParcelable("data")!!
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting_bottom_sheet, container, false)


        binding.coinsEdit.setText(model.getJoinStreamPrice())

        if(model.onlineType.equals(StreamingConstants.streamTypeMulticast)){
            binding.rbPublic.isChecked=true
        }else{
            binding.rbPrivate.isChecked=true
        }

        binding.tabMinus.setOnClickListener {
            if (!TextUtils.isEmpty(binding.coinsEdit.text.toString())) {
                substractNumber(binding.coinsEdit.text.toString())
            }
        }
        binding.tabAdd.setOnClickListener {
            if (!TextUtils.isEmpty(binding.coinsEdit.text.toString())) {
                if (!TextUtils.isEmpty(binding.coinsEdit.text.toString())) {
                    addNumber(binding.coinsEdit.text.toString())
                }
            }
        }

        return binding.root
    }

    private fun substractNumber(numberStr: String) {
        var number = Integer.valueOf(numberStr)
        if (number > 0) {
            number = number - 1
        }
        binding.coinsEdit.setText("" + number)
    }

    private fun addNumber(numberStr: String) {
        var number = Integer.valueOf(numberStr)
        if (number < 1000) {
            number = number + 1
        }
        binding.coinsEdit.setText("" + number)
    }


    override fun onDetach() {
        model.setJoinStreamPrice(binding.coinsEdit.text.toString())
        if(binding.rbPublic.isChecked){
            model.onlineType=StreamingConstants.streamTypeMulticast
        }
        else{
            model.onlineType=StreamingConstants.streamTypeMulticast
        }
        val bundle=Bundle()
        bundle.putParcelable("data",model)
        callBack.onResponce(bundle)
        super.onDetach()
    }

    companion object {

        @JvmStatic
        fun newInstance(model:LiveUserModel,callBack: FragmentCallBack) =
            SettingBottonSheetFragment(callBack).apply {
                arguments = Bundle().apply {
                    putParcelable("data", model)
                }
            }
    }
}