package com.coheser.app.activitesfragments.comments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.coheser.app.R
import com.coheser.app.activitesfragments.sendgift.GiftHistoryModel
import com.coheser.app.adapters.VideosGiftsAdapter
import com.coheser.app.databinding.FragmentVideoGiftsBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.interfaces.FragmentCallBack
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class VideoGiftsFragment(val callBack: FragmentCallBack) : BottomSheetDialogFragment() {

    var dataList:ArrayList<GiftHistoryModel>?=null
    var userId:String?=null
    var videoId:String?=null

    lateinit var binding: FragmentVideoGiftsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dataList = it.getParcelableArrayList("dataList")
        }
    }

    private var mBehavior: BottomSheetBehavior<*>? = null
    var dialog: BottomSheetDialog? = null
   override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(getContext(), R.layout.fragment_video_gifts, null)
        dialog!!.setContentView(view)
        mBehavior = BottomSheetBehavior.from(view.parent as View)
        mBehavior!!.setPeekHeight(view.context.resources.getDimension(R.dimen._450sdp).toInt(), true)
        mBehavior!!.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState != BottomSheetBehavior.STATE_EXPANDED) {
                    mBehavior!!.setState(BottomSheetBehavior.STATE_EXPANDED)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        return (dialog)!!
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentVideoGiftsBinding.inflate(inflater,container,false)

        val adapter=VideosGiftsAdapter(binding.root.context,dataList!!,object :AdapterClickListener{
            override fun onItemClick(view: View?, pos: Int, `object`: Any?) {
            }
        })
        binding.recylerview.layoutManager=LinearLayoutManager(requireContext())
        binding.recylerview.adapter=adapter

        binding.backBtn.setOnClickListener{
            dismiss()
        }

        binding.sendBtn.setOnClickListener{
            dismiss()
            callBack.onResponce(Bundle())

        }
        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(dataList: ArrayList<GiftHistoryModel>,callBack: FragmentCallBack) =
            VideoGiftsFragment(callBack).apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("dataList", dataList)
                }
            }

    }

}