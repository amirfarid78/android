package com.coheser.app.activitesfragments.shoping

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.models.OrderProduct
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.ActivityRatingBinding
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONException
import org.json.JSONObject

class RatingA : AppCompatActivity() , View.OnClickListener{

    var binding:ActivityRatingBinding?=null

    var model: OrderProduct?=null
    var orderId: String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_rating)

        model=intent.getSerializableExtra("data") as OrderProduct
        orderId = intent.getStringExtra("order_id")

        binding!!.submitBtn.setOnClickListener(this@RatingA)

        binding!!.orderImage.setController(
            Functions.frescoImageLoad(
                model!!.product_image,
                R.drawable.image_placeholder,
                binding!!.orderImage,
                false
            )
        )

        binding!!.orderDetailTitle.setText(model!!.product_title)

        binding!!.ratingbar.setOnRatingBarChangeListener(object: OnRatingBarChangeListener{
            override fun onRatingChanged(p0: RatingBar?, rating: Float, p2: Boolean) {
                if(rating<2){
                    binding!!.ratingtxt.setText("Very Poor")
                }
                else if(rating<3){
                    binding!!.ratingtxt.setText("Poor")

                }
                else if(rating<4){
                    binding!!.ratingtxt.setText("Average")

                }
                else if(rating<5){
                    binding!!.ratingtxt.setText("Satisfied")

                }
                else if(rating<6){
                    binding!!.ratingtxt.setText("Excellent")

                }
            }

        })

    }

    override fun onClick(v: View?) {
        if(v!!.id==R.id.submitBtn){

            if(checkValidation()){
                callApiRating()
            }
        }
    }

    fun checkValidation():Boolean{
        val rating=binding!!.ratingbar.rating
        if(rating==0f){
            Toast.makeText(this@RatingA,"please do rating...",Toast.LENGTH_SHORT).show()

            return false;
        }
        else{
            return true
        }

    }

    private fun callApiRating() {

         Functions.showLoader(this, false, false)
        val params = JSONObject()
        try {
            params.put("user_id", Functions.getSharedPreference(this).getString(Variables.U_ID,""))
            params.put("product_id", model!!.product_id)
            params.put("star", binding!!.ratingbar.rating)
            params.put("comment", binding!!.reviewEdit.text.toString())
            params.put("order_id", model!!.order_id)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
           this, ApiLinks.addProductRating, params, Functions.getHeaders(this)
        ) { resp ->
            Functions.cancelLoader()
            if (resp != null) {
                try {
                    val response = JSONObject(resp)
                    val code = response.optInt("code")
                    if (code == 200) {
                        Toast.makeText(this@RatingA,"Submit Successfully",Toast.LENGTH_SHORT).show()

                        setResult(RESULT_OK,Intent())
                        finish()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }


    }


}