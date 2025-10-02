package com.coheser.app.activitesfragments.shoping.AddProducts.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coheser.app.databinding.FragmentPriceBinding


class PriceF : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingPrice = FragmentPriceBinding.inflate(layoutInflater,container,false)
        return bindingPrice.root
    }

    companion object {
        @JvmStatic
        lateinit var bindingPrice : FragmentPriceBinding
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            PriceF().apply {
                arguments = Bundle().apply {
                }
            }
    }
}