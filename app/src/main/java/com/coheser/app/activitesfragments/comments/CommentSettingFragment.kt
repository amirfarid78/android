package com.coheser.app.activitesfragments.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.databinding.FragmentCommentSettingBinding
import com.coheser.app.simpleclasses.Functions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentSettingFragment : BottomSheetDialogFragment, View.OnClickListener {
    var binding: FragmentCommentSettingBinding? = null
    var item: com.coheser.app.models.CommentModel? = null
    var callBack: com.coheser.app.interfaces.FragmentCallBack? = null

    constructor(item: com.coheser.app.models.CommentModel?, callBack: com.coheser.app.interfaces.FragmentCallBack?) {
        this.item = item
        this.callBack = callBack
    }

    constructor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_comment_setting, container, false)
        InitControl()
        return binding!!.getRoot()
    }

    private fun InitControl() {
        Functions.hideSoftKeyboard(activity)
        binding!!.tvPinComment.setOnClickListener(this)
        binding!!.tvCopy.setOnClickListener(this)
        binding!!.tvDelete.setOnClickListener(this)
        if (item!!.videoOwnerId == Functions.getSharedPreference(
                binding!!.root.context
            ).getString(com.coheser.app.simpleclasses.Variables.U_ID, "")
        ) {
            if (item!!.comment_id == item!!.pin_comment_id) {
                binding!!.tvPinComment.text =
                    binding!!.root.context.getString(R.string.unpin_comment)
            } else {
                binding!!.tvPinComment.text = binding!!.root.context.getString(R.string.pin_comment)
            }
            binding!!.tvPinComment.visibility = View.VISIBLE
            binding!!.tvDelete.visibility = View.VISIBLE
        } else {
            binding!!.tvPinComment.visibility = View.GONE
            if (item!!.userId == Functions.getSharedPreference(
                    binding!!.root.context
                ).getString(com.coheser.app.simpleclasses.Variables.U_ID, "")
            ) {
                binding!!.tvDelete.visibility = View.VISIBLE
            } else {
                binding!!.tvDelete.visibility = View.GONE
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvPinComment -> {
                performAction("pinComment")
            }

            R.id.tvCopy -> {
                performAction("copyText")
            }

            R.id.tvDelete -> {
                performAction("deleteComment")
            }
        }
    }

    private fun performAction(action: String) {
        val bundle = Bundle()
        bundle.putBoolean("isShow", true)
        bundle.putString("action", action)
        callBack!!.onResponce(bundle)
        dismiss()
    }
}