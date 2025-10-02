package com.coheser.app.activitesfragments.livestreaming.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.coheser.app.databinding.FragmentAnimationViewBinding
import com.coheser.app.simpleclasses.FileUtils
import com.coheser.app.simpleclasses.TicTicApp.Companion.appLevelContext
import com.coheser.app.simpleclasses.Variables
import com.tencent.qgame.animplayer.AnimConfig
import com.tencent.qgame.animplayer.inter.IAnimListener
import kotlinx.coroutines.launch
import java.io.File


class AnimationViewF : DialogFragment(), IAnimListener {
    lateinit var binding : FragmentAnimationViewBinding
    lateinit var giftId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        giftId= arguments?.getString("id").toString()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAnimationViewBinding.inflate(layoutInflater, container, false)

        binding.aniView.setAnimListener(this)

        val filePath = File(FileUtils.getAppFolder(appLevelContext!!) + Variables.APP_Gifts_Folder + giftId+".mp4")


        lifecycleScope.launch {
            binding.aniView.startPlay(filePath)
        }

        return binding.root
    }



    companion object {

        @JvmStatic
        fun newInstance(giftId:String) =
            AnimationViewF().apply {
                arguments = Bundle().apply {
                    putString("id",giftId)
                }
            }

    }

    override fun onFailed(errorType: Int, errorMsg: String?) {

    }

    override fun onVideoComplete() {
        dismiss()

    }

    override fun onVideoDestroy() {

    }

    override fun onVideoRender(frameIndex: Int, config: AnimConfig?) {

    }

    override fun onVideoStart() {

    }
}