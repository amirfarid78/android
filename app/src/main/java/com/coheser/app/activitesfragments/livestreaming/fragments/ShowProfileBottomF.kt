package com.coheser.app.activitesfragments.livestreaming.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.databinding.FragmentShowProfileBottomBinding
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ShowProfileBottomF : BottomSheetDialogFragment() {

    lateinit var binding:FragmentShowProfileBottomBinding
    lateinit var model: LiveUserModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.MyTransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_show_profile_bottom, container, false)
        model= arguments?.getParcelable("data")!!

        binding.nameTxt.text=model.userName
        binding.profileImage.controller = frescoImageLoad(model.userPicture, binding.profileImage, false)

        if(!TextUtils.isEmpty(model.description)){
            binding.descriptionTxt.text = model.description
        }


        binding.donebtn.setOnClickListener{
            dismiss()
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(model: LiveUserModel) =
            ShowProfileBottomF().apply {
                arguments = Bundle().apply { putParcelable("data",model) }
            }
    }
}