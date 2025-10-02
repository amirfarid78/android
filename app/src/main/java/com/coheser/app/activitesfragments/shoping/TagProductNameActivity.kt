package com.coheser.app.activitesfragments.shoping

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.databinding.ActivityTagProductNameBinding
import com.coheser.app.simpleclasses.DelayedTextWatcher

class TagProductNameActivity : AppCompatActivity() {

    lateinit var binding:ActivityTagProductNameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityTagProductNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productModel : ProductModel? =intent.getParcelableExtra("data")
        binding.productNameEdit.addTextChangedListener(
            DelayedTextWatcher(delayMillis = 200) { text ->
                binding.countTxt.text = text.length.toString() + "/" + com.coheser.app.Constants.USERNAME_CHAR_LIMIT
                checkValidation()
            }
        )

        binding.btnNext.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {

                productModel!!.product.taggedName=binding.productNameEdit.text.toString()

                val intent= Intent()
                intent.putExtra("data",productModel)
                setResult(RESULT_OK,intent)
                finish()

            }
        })

    }

    fun checkValidation() {
        if (TextUtils.isEmpty(binding.productNameEdit.text.toString())) {
            binding.btnNext.isEnabled=false
            binding.btnNext.isClickable = false
        }
        else{
            binding.btnNext.isEnabled=true
            binding.btnNext.isClickable = true
        }

    }

}