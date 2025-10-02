package com.coheser.app.activitesfragments.profile.usersstory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.coheser.app.R
import com.coheser.app.adapters.StoryEmojiAdapter
import com.coheser.app.databinding.FragmentStoryEmoticonBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.Functions.convertEmoji

class StoryEmoticonF : BottomSheetDialogFragment {
    var callBack: FragmentCallBack? = null
    lateinit var binding: FragmentStoryEmoticonBinding
    var adapter: StoryEmojiAdapter? = null
    var dataList: ArrayList<String> = ArrayList()
    var selectedEmoticon: String? = null

    constructor(callBack: FragmentCallBack?) {
        this.callBack = callBack
    }

    constructor()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_story_emoticon, container, false)
        initControl()
        actionControl()
        return binding.getRoot()
    }

    private fun actionControl() {
    }

    private fun initControl() {
        setupAdapter()
    }

    private fun setupAdapter() {
        val layoutManager = GridLayoutManager(binding!!.root.context, 5)
        layoutManager.orientation = RecyclerView.VERTICAL
        binding!!.recylerview.layoutManager = layoutManager
        adapter = StoryEmojiAdapter(dataList) { view, pos, `object` ->
            selectedEmoticon = dataList[pos]
            dismiss()
        }
        binding!!.recylerview.adapter = adapter

        emojiList
    }


    private val emojiList: Unit
        get() {
            if (dataList.size <= 0) {
                val emojiArray =
                    binding!!.root.context.resources.getStringArray(R.array.photo_editor_emoji)
                for (emoji in emojiArray) {
                    dataList.add(convertEmoji(emoji))
                }
                adapter!!.notifyDataSetChanged()
            }

            if (dataList.size > 0) {
                binding!!.progressBar.visibility = View.GONE
            }
        }

    override fun onDetach() {
        super.onDetach()
        if (selectedEmoticon != null) {
            val bundle = Bundle()
            bundle.putBoolean("isShow", true)
            bundle.putString("data", selectedEmoticon)
            callBack!!.onResponce(bundle)
        } else {
            val bundle = Bundle()
            bundle.putBoolean("isShow", false)
            callBack!!.onResponce(bundle)
        }
    }

    companion object {
        fun newInstance(callBack: FragmentCallBack?): StoryEmoticonF {
            val fragment = StoryEmoticonF(callBack)
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}