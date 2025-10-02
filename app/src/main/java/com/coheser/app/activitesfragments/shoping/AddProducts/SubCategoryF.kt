package com.coheser.app.activitesfragments.shoping.AddProducts

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.adapter.ProductCategoryAdapter
import com.coheser.app.activitesfragments.shoping.models.CategoryModel
import com.coheser.app.databinding.FragmentSubCategoryBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.google.android.material.bottomsheet.BottomSheetBehavior

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SubCategoryF(fragmentCallBack: FragmentCallBack) :  BottomSheetDialogFragment() {


    var title:String?=null
    val fragmentCallBack=fragmentCallBack
    var binding:FragmentSubCategoryBinding?=null

    var dataList:ArrayList<CategoryModel>?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dataList = it.getParcelableArrayList<CategoryModel>("data") as ArrayList<CategoryModel>
            title = it.getString("title")
        }
    }


    var dialog: BottomSheetDialog? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.fragment_sub_category, null)
        dialog!!.setContentView(view)
       var mBehavior = BottomSheetBehavior.from(view.parent as View)
        mBehavior.setHideable(false)
        mBehavior.setDraggable(false)
        mBehavior.setPeekHeight(view.context.resources.getDimension(R.dimen._500sdp).toInt(), true)
        mBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState != BottomSheetBehavior.STATE_EXPANDED) {
                    mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        return dialog!!
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_sub_category, container, false)

        binding!!.titleTxt.setText(title)
        setCategoryAdapter()
        return binding!!.root
    }


    fun setCategoryAdapter(){

        var linearLayoutManager = LinearLayoutManager(activity)
        binding!!.recyclerview.setLayoutManager(linearLayoutManager)

        var adapter = ProductCategoryAdapter(
            requireContext(), dataList!!
        ) { view, pos, `object` ->
            val categoryModel = `object` as CategoryModel
            when (view.id) {
                R.id.mainlayout -> {
                    if(fragmentCallBack!=null) {
                        val bundle = Bundle()
                        bundle.putParcelable("data",categoryModel)
                        fragmentCallBack.onResponce(bundle)
                        dismiss()
                    }
                }
            }
        }
        binding!!.recyclerview.setAdapter(adapter)

    }



    companion object {

        @JvmStatic
        fun newInstance(categoryModel: ArrayList<CategoryModel>, title: String,fragmentCallBack: FragmentCallBack) =
            SubCategoryF(fragmentCallBack).apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("data", categoryModel)
                    putString("title", title)
                }
            }
    }


}