package com.coheser.app.activitesfragments.livestreaming.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.databinding.FragmentGoalDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GoalDetailF : BottomSheetDialogFragment() {

    lateinit var liveUserModel: LiveUserModel
    lateinit var binding: FragmentGoalDetailBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            liveUserModel = it.getParcelable("data")!!

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGoalDetailBinding.inflate(inflater, container, false)

        binding.tvGoalValue.text=liveUserModel.setGoalStream?.goalAmount
        binding.closeBtn.setOnClickListener{
            dismiss()
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(liveUserModel: LiveUserModel) =
            GoalDetailF().apply {
                arguments = Bundle().apply {
                    putParcelable("data", liveUserModel)
                }
            }
    }
}