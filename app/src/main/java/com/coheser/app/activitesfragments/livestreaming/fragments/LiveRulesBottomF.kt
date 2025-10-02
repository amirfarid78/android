package com.coheser.app.activitesfragments.livestreaming.fragments

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coheser.app.R
import com.coheser.app.databinding.FragmentLiveRulesBottomBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LiveRulesBottomF(val callBack: FragmentCallBack) : BottomSheetDialogFragment() {

    lateinit var binding: FragmentLiveRulesBottomBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLiveRulesBottomBinding.inflate(inflater, container, false)

        binding.closeBtn.setOnClickListener{
            dismiss()
        }

        binding.agreeButton.setOnClickListener{
            if(checkValidation()){
                callBack.onResponce(Bundle())
                dismiss()
            }
        }

        binding.rule1.setOnCheckedChangeListener{ buttonView, isChecked ->
            checkValidation()
        }
        binding.rule2.setOnCheckedChangeListener{ buttonView, isChecked ->
            checkValidation()
        }
        binding.rule3.setOnCheckedChangeListener{ buttonView, isChecked ->
            checkValidation()
        }



        binding.rule4.setOnCheckedChangeListener{ buttonView, isChecked ->
            checkValidation()
        }

        val fullText = getString(R.string.i_will_adhere_to_the_community_guidelines)
        val clickablePart = "Community Guidelines"
        val spannableString = SpannableString(fullText)
        val startIndex = fullText.indexOf(clickablePart)
        val endIndex = startIndex + clickablePart.length
        if (startIndex != -1) {
            spannableString.setSpan(
                UnderlineSpan(),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                }
            }
            spannableString.setSpan(
                clickableSpan,
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding.rule4.text = spannableString
        binding.rule4.movementMethod = android.text.method.LinkMovementMethod.getInstance()

        return binding.root
    }

    fun checkValidation():Boolean{
        if(binding.rule1.isChecked && binding.rule2.isChecked && binding.rule3.isChecked && binding.rule4.isChecked){
            binding.agreeButton.background=resources.getDrawable(R.drawable.btn_color_round_order)
            return true
        }
        else{
            binding.agreeButton.background=resources.getDrawable(R.drawable.disable_round_btn)
            return false
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(callBack: FragmentCallBack) = LiveRulesBottomF(callBack)
    }

}