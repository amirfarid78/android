package com.coheser.app.activitesfragments.location

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.coheser.app.R
import com.coheser.app.databinding.ActivityDropOffOptionsBinding
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import io.paperdb.Paper

class DropOffOptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDropOffOptionsBinding
    var deliveryAddress:DeliveryAddress?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDropOffOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)



        setAddress()

        binding.backBtn.setOnClickListener{
            finish()
        }

        binding.continueBtn.setOnClickListener {
            val selectedId = binding.radioGroup.checkedRadioButtonId
            val instructions = binding.editTextInstructions.text.toString()

            if(selectedId == R.id.radioHandToMe){
                deliveryAddress?.dropoff_option="0"
            }
            else if(selectedId == R.id.radioLeaveAtDoor){
                deliveryAddress?.dropoff_option="1"
            }

            deliveryAddress?.instructions=instructions

            deliveryAddress?.let {
                Paper.book().write(Variables.AdressModel, it)
            }

            val returnIntent = Intent()
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    fun setAddress(){
       deliveryAddress = Paper.book().read<DeliveryAddress>(Variables.AdressModel)

        if (deliveryAddress != null) {

            if (deliveryAddress!!?.dropoff_option.equals("0")){
                binding.radioHandToMe.isChecked=true
            }else if (deliveryAddress?.dropoff_option.equals("1")){
                binding.radioLeaveAtDoor.isChecked=true
            }

            if(Functions.isStringHasValue(deliveryAddress?.instructions)){
                binding.editTextInstructions.setText(deliveryAddress?.instructions!!)
            }

        }

    }


}