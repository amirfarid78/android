package com.coheser.app.activitesfragments.shoping.AddProducts

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.RadioGroup
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.models.AddProductModel
import com.coheser.app.databinding.ActivityAddProductConditionBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class AddProductConditionA : AppCompatActivity() {
    lateinit var binding : ActivityAddProductConditionBinding
    var dataModel: AddProductModel?=null
    var selectedItem=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductConditionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(intent!=null) {
            dataModel = intent.getParcelableExtra<AddProductModel>("dataModel")!!
        }

        binding!!.ivBack.setOnClickListener{
            finish()
        }

        initis()
    }

    fun initis(){


        if(!TextUtils.isEmpty(dataModel!!.condition)) {
            binding.rootD.selectCondition.text = dataModel!!.condition
        }

        binding.rootD.selectCondition.setOnClickListener {
            openBottomSheet()
        }

        binding.saveBtn.setOnClickListener {
            val data = Intent()
            dataModel!!.condition= selectedItem
            data.putExtra("dataModel",dataModel)
            setResult(Activity.RESULT_OK,data)
            finish()
        }
    }

    fun openBottomSheet(){
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.add_condition_bottomsheet, null)

        val radio_group = view.findViewById<RadioGroup>(R.id.radio_group)
        radio_group.setOnCheckedChangeListener{ _, checkedId ->

            when(checkedId){
                R.id.brand_new_btn ->{
                    binding.rootD.selectCondition.text = getString(R.string.brand_new)
                    selectedItem="Brand New"
                    dialog!!.dismiss()
                }
                R.id.like_new_btn ->{
                    binding.rootD.selectCondition.text = getString(R.string.like_new)
                    selectedItem="Like New"
                    dialog?.dismiss()
                }
                R.id.lu_btn ->{
                    binding.rootD.selectCondition.text = getString(R.string.lightly_used)
                    selectedItem="Lightly Use"
                    dialog?.dismiss()
                }
                R.id.wu_btn ->{
                    binding.rootD.selectCondition.text = getString(R.string.well_used)
                    selectedItem="Well Used"
                    dialog?.dismiss()
                }
                R.id.hu_btn ->{
                    binding.rootD.selectCondition.text = getString(R.string.heavily_used)
                    selectedItem="Heavily Used"

                }

            }

        }

        dialog.setCancelable(true)
        dialog.setContentView(view)
        dialog.show()
    }


}