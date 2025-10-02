package com.coheser.app.activitesfragments.sendgift

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.coheser.app.R
import com.coheser.app.activitesfragments.WebviewActivity
import com.coheser.app.activitesfragments.walletandwithdraw.WalletModel
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentBonusCheckOutBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.StripeModel
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.StripeViewModel
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class BonusCheckOutFragment(val callback: FragmentCallBack) : BottomSheetDialogFragment() {

    lateinit var model: WalletModel
    lateinit var binding: FragmentBonusCheckOutBinding

    var percentage=0
    private val viewModel : StripeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_bonus_check_out,container,false)


        model=requireArguments().getSerializable("data") as WalletModel

        binding.tvCoins.text=model.coins+" "+getString(R.string.coins)
        binding.tvPrice.text=model.price

        percentage=(model.coins.toInt()*5/100).toInt()
        binding.tvBonus.text="+"+percentage+" "+getString(R.string.coins)

        binding.btnAddCard.setOnClickListener {
            viewModel.purchaseFromCard(model.coins+" "+"coins",model.price.replace("$",""))
        }
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
                        val intent = Intent(requireActivity(), WebviewActivity::class.java)
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
        viewModel.purchaseCoinLivedata.observe(this){
            when(it){
                is ApiResponce.Success ->{
                    Functions.cancelLoader()
                    it.data?.let {
                        if (it != null){
                            val jsonObject=JSONObject(it);
                            val userDetailModel = getUserDataModel(jsonObject.optJSONObject("User"))
                            val editor = getSharedPreference(requireContext()).edit()
                            editor.putString(Variables.U_WALLET, "" + userDetailModel.wallet)
                            editor.commit()
                            showToast(requireContext(),"Purchased")

                            callback.onResponce(Bundle())
                            dismiss()
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
                    viewModel.purchaseCoin((model.coins.toInt()+percentage).toString(),
                        model.coins+" "+getString(R.string.coins),
                        model.price.replace("$",""), stripeModel?.id.toString()
                    )
                }else{
                    Functions.showToast(requireContext(),"failed")
                }
            }
        }
    }



    companion object {
        @JvmStatic
        fun newInstance(callback:FragmentCallBack,model: WalletModel) =
            BonusCheckOutFragment(callback).apply {
                arguments = Bundle().apply {
                    putSerializable("data",model)
                }
            }
    }
}