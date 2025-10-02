package com.coheser.app.activitesfragments.livestreaming.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.model.SetGoalStream
import com.coheser.app.databinding.FragmentStreamingGoalBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class StreamingGoalFragment(val callback: FragmentCallBack) : BottomSheetDialogFragment() {

    lateinit var binding: FragmentStreamingGoalBinding
    lateinit var setGoalStream: SetGoalStream

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog= BottomSheetDialog(requireContext(), R.style.MyTransparentBottomSheetDialogTheme)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        return dialog
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStreamingGoalBinding.inflate(inflater, container, false)

        binding.doneBtn.setOnClickListener{
            if(!TextUtils.isEmpty(binding.coinsInput.text.toString())){
                val bundle=Bundle()
                setGoalStream= SetGoalStream()
                setGoalStream.goalAmount=binding.coinsInput.text.toString()
                setGoalStream.goalDescription=binding.descriptionInput.text.toString()
                bundle.putParcelable("data",setGoalStream)
                callback.onResponce(bundle)
                dismiss()
            }
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(callback: FragmentCallBack) = StreamingGoalFragment(callback)
    }


}