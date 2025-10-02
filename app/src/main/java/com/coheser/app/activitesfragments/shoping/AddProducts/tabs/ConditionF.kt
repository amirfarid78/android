package com.coheser.app.activitesfragments.shoping.AddProducts.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.coheser.app.R
import com.coheser.app.databinding.FragmentConditionBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.google.android.material.bottomsheet.BottomSheetDialog


class ConditionF(callBack: FragmentCallBack) : Fragment() {

    lateinit var binding: FragmentConditionBinding
    val callBack:FragmentCallBack=callBack
    var selectedItem=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentConditionBinding.inflate(layoutInflater,container,false)

        binding.root.selectCondition.setOnClickListener {
            openBottomSheet()
        }
        return binding.getRoot()
    }

    companion object {
        fun newInstance(callBack: FragmentCallBack) =
            ConditionF(callBack).apply {
                arguments = Bundle().apply {
                }
            }
    }

    fun openBottomSheet(){
        val dialog = context?.let { BottomSheetDialog(it) }
        val view = layoutInflater.inflate(R.layout.add_condition_bottomsheet, null)

        val radio_group = view.findViewById<RadioGroup>(R.id.radio_group)
        radio_group.setOnCheckedChangeListener{ _, checkedId ->

            when(checkedId){
                R.id.brand_new_btn ->{
                    binding.root.selectCondition.text = getString(R.string.brand_new)
                    selectedItem="Brand New"
                    dialog!!.dismiss()
                }
                R.id.like_new_btn ->{
                    binding.root.selectCondition.text = getString(R.string.like_new)
                    selectedItem="Like New"
                    dialog?.dismiss()
                }
                R.id.lu_btn ->{
                    binding.root.selectCondition.text = getString(R.string.lightly_used)
                    selectedItem="Lightly Use"
                    dialog?.dismiss()
                }
                R.id.wu_btn ->{
                    binding.root.selectCondition.text = getString(R.string.well_used)
                    selectedItem="Well Used"
                    dialog?.dismiss()
                }
                R.id.hu_btn ->{
                    binding.root.selectCondition.text = getString(R.string.heavily_used)
                    selectedItem="Heavily Used"

                }

            }

            val bundle=Bundle()
            bundle.putString("condition",selectedItem)
            callBack.onResponce(bundle)
        }

        dialog?.setCancelable(true)
        dialog?.setContentView(view)
        dialog?.show()
    }

}