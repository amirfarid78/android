package com.coheser.app.activitesfragments.shoping

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.WebviewActivity
import com.coheser.app.activitesfragments.location.AddAddressActivity
import com.coheser.app.activitesfragments.location.DeliveryAddress
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentItemPuchaseSheetBinding
import com.coheser.app.models.StripeModel
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.changeValueToInt
import com.coheser.app.simpleclasses.Functions.checkLoginUser
import com.coheser.app.simpleclasses.Functions.getAddressString
import com.coheser.app.simpleclasses.Functions.hideSoftKeyboard
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.StripeViewModel
import io.paperdb.Paper
import org.koin.androidx.viewmodel.ext.android.viewModel


class ItemPuchaseSheet : BottomSheetDialogFragment() {
    lateinit var binding : FragmentItemPuchaseSheetBinding
    var model : ProductModel ? = null
    private val viewModel : StripeViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentItemPuchaseSheetBinding.inflate(layoutInflater, container, false)

        model = arguments?.getParcelable("product")

        inits()
        setobservers()

        return binding.root
    }
    var stripeModel : StripeModel? = null
    fun setobservers(){
        viewModel.stripeDetailLivedata.observe(this){
            when(it){
                is ApiResponce.Success ->{
                    Functions.cancelLoader()
                    stripeModel = it.data
                    if (stripeModel != null){
                        val intent = Intent(requireActivity(),WebviewActivity::class.java)
                        intent.putExtra("modelStripe",stripeModel)
                        intent.putExtra("url",stripeModel!!.url)
                        intent.putExtra("title","Stripe")
                        intent.putExtra("from","purchase")
                        paymentResultLauncher.launch(intent)
                    }

                }
                is ApiResponce.Loading ->{
                    Functions.showLoader(requireActivity(),false,false)
                }
                is  ApiResponce.Error ->{
                    Functions.cancelLoader()
                }
                else ->{}
            }
        }
        viewModel.purchaseProductLivedata.observe(this){
            when(it){
                is ApiResponce.Success ->{
                    Functions.cancelLoader()
                    it.data?.let {
                        if (it != null){
                            showToast(requireContext(),"Purchased")
                            dialog!!.dismiss()
                        }
                    }
                }
                is ApiResponce.Loading ->{
                    Functions.showLoader(requireActivity(),false,false)
                }
                is  ApiResponce.Error ->{
                    Functions.cancelLoader()
                    showToast(requireContext(), it.message)
                }
                else ->{}
            }
        }
    }
    private val paymentResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val isSuccess = data.getBooleanExtra("isSuccess", false)
                if (isSuccess) {
                    viewModel.purchaseProduct(
                        model!!.product.id,
                        addressId!!,
                        subTotal.toString(),
                        "android",
                        stripeModel!!.id
                    )
                }else{
                    Functions.showToast(requireContext(),"failed")
                }
            }
        }
    }


    fun inits(){
        binding.ivShirt.controller = Functions.frescoImageLoad(
            model!!.productImage.first().image,
            binding.ivShirt,
            false
        )
        binding.tvTitle.text = "${model!!.product.title}"
        binding.tvCurrentPrice.text = "${Constants.CURRENCY} ${model!!.product.price}"
        calculateTotalSum()

        binding.backBtn.setOnClickListener {
            dialog!!.dismiss()
        }

        binding.rlPlus.setOnClickListener {
            model!!.product.count = model!!.product.count +1
            calculateTotalSum()
        }
        binding.rlMinus.setOnClickListener {
            if (model!!.product.count > 1){
                model!!.product.count = model!!.product.count -1
                calculateTotalSum()
            }
        }
        binding.tvAddressChange.setOnClickListener {
            hideSoftKeyboard(requireActivity())
            if (checkLoginUser(requireActivity())) {
                val intent = Intent(requireActivity(), AddAddressActivity::class.java)
                intent.putExtra("showCurrentLocation", false)
                try {
                    resultCallback.launch(intent)
                } catch (e: Exception) {
                    startActivity(intent)
                }
            }
        }
        binding.payBtn.setOnClickListener {
            if (checkValidations()){
                val name  = Functions.removeAtSymbol(Functions.getSharedPreference(requireContext()).getString(Variables.U_NAME,"")!!)
                viewModel.purchaseFromCard(name,subTotal.toString())
            }
        }

        setAddress()
    }
    var subTotal = 0
    fun calculateTotalSum() {
        var subtotal = 0
        subtotal = subtotal + changeValueToInt(model!!.product.price) * model!!.product.count

        Log.d(Constants.tag,"subtotal : ${subtotal} , Price :${model!!.product.price}  count : ${model!!.product.count}" )
        this.subTotal = subtotal
        binding.totalPrice.setText(Constants.productShowingCurrency + subTotal)
        binding.tvQuantity.text = "${model!!.product.count}"
    }
    var addressId: String? = null
    fun setAddress() {
        val deliveryAddress = Paper.book().read<DeliveryAddress>(Variables.AdressModel)
        if (deliveryAddress != null) {
            addressId = deliveryAddress.id
            binding.tvUserAddress.visibility = View.VISIBLE
            binding.tvAddressChange.setText(R.string.change_address)
            binding.tvUserAddress.text = getAddressString(deliveryAddress)
        }
    }
    fun checkValidations(): Boolean {
        return if (TextUtils.isEmpty(addressId)) {
            showToast(context, getString(R.string.please_select_the_delivery_address))
            false
        } else {
            true
        }
    }
    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            setAddress()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(product: ProductModel) =
            ItemPuchaseSheet().apply {
                arguments = Bundle().apply {
                    putParcelable("product", product)
                }
            }
    }
}