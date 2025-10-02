package com.coheser.app.activitesfragments.shoping.AddProducts.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coheser.app.databinding.FragmentItemDetailBinding


class ItemDetailF : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingDetail = FragmentItemDetailBinding.inflate(layoutInflater,container,false)


        return bindingDetail.root
    }

    companion object {
       @JvmStatic
       lateinit var bindingDetail: FragmentItemDetailBinding
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            ItemDetailF().apply {
                arguments = Bundle().apply {
                }
            }
    }
}