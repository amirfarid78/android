package com.coheser.app.activitesfragments.shoping.AddProducts.tabs

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.coheser.app.activitesfragments.location.AddAddressActivity
import com.coheser.app.activitesfragments.location.DeliveryAddress
import com.coheser.app.databinding.FragmentDealMethodBinding
import com.coheser.app.simpleclasses.Variables
import io.paperdb.Paper


class DealMethodF : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDealMethodBinding.inflate(layoutInflater,container,false)
        inits()
        return binding.root
    }


    fun inits(){
        binding.rootD.selectLocationLayout.setOnClickListener{
            openMapActivity()
        }
        binding.rootD.arrangeMyself.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
               if(p1){
                   binding.rootD.meetUp.isChecked=false
               }
            }

        })

        binding.rootD.meetUp.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                if(p1){
                    binding.rootD.arrangeMyself.isChecked=false
                }
            }
        })

    }

    fun openMapActivity() {

        val intent = Intent(requireActivity(), AddAddressActivity::class.java)
        intent.putExtra("showCurrentLocation", true)
        try {
            resultCallback.launch(intent)
        } catch (e: Exception) {
            startActivity(intent)
        }

    }


    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {

           val deliveryAddress =  Paper.book().read<DeliveryAddress>(Variables.AdressModel)

            locationString = deliveryAddress!!.location_string
            latitude =  deliveryAddress!!.lat
            longitude =  deliveryAddress!!.lng

            binding.rootD.locationTxt.text=locationString


        }
    }

    companion object {
        @JvmStatic
        lateinit var binding : FragmentDealMethodBinding
        var locationString:String=""
        var latitude:String=""
        var longitude:String=""
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            DealMethodF().apply {
                arguments = Bundle().apply {
                }
            }
    }
}