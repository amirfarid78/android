package com.coheser.app.activitesfragments.shoping

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.chat.ChatActivity
import com.coheser.app.activitesfragments.shoping.adapter.OrderDetailAdapter
import com.coheser.app.activitesfragments.shoping.models.OrderHistoryModel
import com.coheser.app.activitesfragments.shoping.models.OrderProduct
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentOrderHistoryDetailBinding
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONException
import org.json.JSONObject

class OrderHistory_F : Fragment(), View.OnClickListener {
    var isview = false
    var isTrue = false
    var model: OrderHistoryModel? = null
    lateinit var binding: FragmentOrderHistoryDetailBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_order_history_detail,
            container,
            false
        )
        model = requireArguments().getParcelable<OrderHistoryModel>("data") as OrderHistoryModel
        methodInitializeClickListner()
        binding.orderIdTxt.text = model!!.order.id
        if (isTrue) {
            binding.trackOrderBtn.visibility = View.VISIBLE
        } else {
            binding.trackOrderBtn.visibility = View.GONE
        }
        fetchOrderHistoryDetailApi()
        return binding.root
    }

    private fun methodInitializeClickListner() {
        binding.backIcon.setOnClickListener(this)
        binding.contactStoreBtn.setOnClickListener(this)
        binding.trackOrderBtn.setOnClickListener(this)
        binding.delievryInfoDiv.setOnClickListener(this)
    }

    fun setData() {
        binding.totalDiscountTv.text = Constants.productShowingCurrency + model!!.order.discount
        binding.totalPriceTv.text = Constants.productShowingCurrency + model!!.order.total
        binding.orderDateTxt.text = model!!.order.created
        binding.totalShippingTv.text =
            Constants.productShowingCurrency + model!!.order.delivery_fee
        binding.totalSubTv.text = Constants.productShowingCurrency + model!!.order.total
        binding.totalPriceTv.text =
            Constants.productShowingCurrency + model!!.order.total
        if (model!!.order.status == "0") {
            binding.orderStatusTxt.text = "Pending"
        } else if (model!!.order.status == "1") {
            binding.orderStatusTxt.text = "Active"
        } else if (model!!.order.status == "2") {
            binding.orderStatusTxt.text = "Completed"
            binding.trackOrderBtn.visibility = View.GONE
        } else if (model!!.order.status == "3") {
            binding.orderStatusTxt.text = "Cancel"
            binding.trackOrderBtn.visibility = View.GONE
        }
        Log.d(Constants.tag,model!!.user.email?:"No Email")
        binding.orderUserEmailTv.text = model!!.user?.email?:"No Email"
        binding.orderUserNameTv.text = model!!.user?.username?:""
        binding.orderStoreName.text = model!!.user?.username?:""
        binding.orderUserAddressTv.text = model!!.deliveryAddress.location_string
        binding.orderUserCityTv.text = model!!.deliveryAddress.city
        binding.orderUserPostalTv.text = model!!.deliveryAddress.zip
        writeRecycler()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.delievry_info_div -> if (isview) {
                binding.infoRlt.visibility = View.VISIBLE
                binding.arrowDown.visibility = View.GONE
                binding.arrowUp.visibility = View.VISIBLE
                isview = false
            } else {
                binding.infoRlt.visibility = View.GONE
                binding.arrowDown.visibility = View.VISIBLE
                binding.arrowUp.visibility = View.GONE
                isview = true
            }

            R.id.track_order_btn -> {}
            R.id.contact_store_btn -> {
                val intent = Intent(activity, ChatActivity::class.java)
                intent.putExtra("user_id", model!!.user!!.id)
                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
            }

            R.id.back_icon -> requireActivity().onBackPressed()
            else -> {}
        }
    }

    private fun fetchOrderHistoryDetailApi() {
        showLoader(activity, false, false)
        val params = JSONObject()
        try {
            params.put(
                "user_id", getSharedPreference(
                    context
                ).getString(Variables.U_ID, "")
            )
            params.put("order_id", model!!.order.id)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.showOrderDetail,
            params,
            getHeaders(requireContext())
        ) { resp ->
            cancelLoader()
            if (resp != null) {
                try {
                    val response = JSONObject(resp)
                    val code = response.optInt("code")
                    if (code == 200) {
                        val msgObj = response.optJSONObject("msg")
                        model = Gson().fromJson(msgObj.toString(), OrderHistoryModel::class.java)
                        setData()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun writeRecycler() {
        binding.recylerview.layoutManager = LinearLayoutManager(context)
        binding.recylerview.setHasFixedSize(true)
        val adapter = OrderDetailAdapter(requireContext(), model!!) { view, pos, `object` ->
            if (view.id == R.id.ratingLayout) {
                val orderProduct = `object` as OrderProduct
                //                    if(orderProduct.productRating==null) {
//                        openRatingScreen(orderProduct);
//                    }
            }
        }
        binding.recylerview.adapter = adapter
        binding.recylerview.isNestedScrollingEnabled = false
    }

    fun openRatingScreen(orderProduct: OrderProduct?) {
        val intent = Intent(activity, RatingA::class.java)
        intent.putExtra("data", orderProduct)
        intent.putExtra("order_id", model!!.order.id)
        resultCallback.launch(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {

            override fun onActivityResult(result: ActivityResult?) {
                if (result!!.resultCode == Activity.RESULT_OK) {
                    fetchOrderHistoryDetailApi()
                }
            }
        })
}
