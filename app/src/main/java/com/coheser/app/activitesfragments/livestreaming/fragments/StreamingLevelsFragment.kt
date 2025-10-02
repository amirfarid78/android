package com.coheser.app.activitesfragments.livestreaming.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coheser.app.R


class StreamingLevelsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_streaming_levels, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            StreamingLevelsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}