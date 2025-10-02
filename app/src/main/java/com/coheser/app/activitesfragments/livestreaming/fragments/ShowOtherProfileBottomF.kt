package com.coheser.app.activitesfragments.livestreaming.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coheser.app.R
import com.coheser.app.databinding.FragmentShowOtherProfileBottomBinding
import com.coheser.app.models.StreamJoinModel
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ShowOtherProfileBottomF : BottomSheetDialogFragment() {
    lateinit var binding : FragmentShowOtherProfileBottomBinding
    lateinit var model:StreamJoinModel
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.MyTransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShowOtherProfileBottomBinding.inflate(layoutInflater, container, false)

        model= requireArguments().getParcelable("data")!!

        binding.nameTxt.text=model.userName
        binding.profileImage.controller = frescoImageLoad(model.userPic, binding.profileImage, false)

        binding.followingCount.text=model.followingCount
        binding.followerCountTxt.text = model.followersCount


        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(model: StreamJoinModel) =
            ShowOtherProfileBottomF().apply {
                arguments = Bundle().apply {
                    putParcelable("data",model)
                }
            }
    }
}