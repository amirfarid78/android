package com.coheser.app.activitesfragments.sendgift

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.walletandwithdraw.WalletModel
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentRechargeBottomSheetBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.DataParsing
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Variables
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import kotlin.math.max

class RechargeBottomSheet(val callBack: FragmentCallBack) : BottomSheetDialogFragment(),
    PurchasesUpdatedListener {

    lateinit var binding: FragmentRechargeBottomSheetBinding
    var datalist: ArrayList<WalletModel> = ArrayList()
    lateinit var adapter: CoinRechargeAdapter

    var selectedWalletModel: WalletModel? = null

    var type:String =""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_recharge_bottom_sheet,container,false)

        type=requireArguments().getString("type","")

        binding.coinsTxt.setText(getSharedPreference(requireContext()).getString(Variables.U_WALLET, "0"))

        setAdapter()
        populateDataList()

        if(!type.equals("bonus")){
            initalizeBill()
        }

        return binding.root
    }




    private fun populateDataList() {

        datalist.add(WalletModel(Constants.Product_ID0, "", Constants.COINS0, Constants.PRICE0))
        datalist.add(WalletModel(Constants.Product_ID1, "", Constants.COINS1, Constants.PRICE1))
        datalist.add(WalletModel(Constants.Product_ID2, "", Constants.COINS2, Constants.PRICE2))
        datalist.add(WalletModel(Constants.Product_ID3, "", Constants.COINS3, Constants.PRICE3))
        datalist.add(WalletModel(Constants.Product_ID4, "", Constants.COINS4, Constants.PRICE4))

        adapter.notifyDataSetChanged()
    }

    private fun setAdapter() {
        val linearLayoutManager = GridLayoutManager(requireContext(),3)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        binding.recylerView.setLayoutManager(linearLayoutManager)
        adapter = CoinRechargeAdapter(
            requireContext(), datalist,
            AdapterClickListener { view, pos, `object` ->
                 selectedWalletModel=`object` as WalletModel
                if(type.equals("bonus")){
                    showBonusCheckout(selectedWalletModel!!)
                }
                else{
                    purchaseItem(pos)
                }


            })
        binding.recylerView.setAdapter(adapter)
    }

    companion object {
        @JvmStatic
        fun newInstance(callback:FragmentCallBack,type:String) =
            RechargeBottomSheet(callback).apply {
                arguments = Bundle().apply {
                    putString("type",type)
                }

            }
    }


    fun showBonusCheckout(model: WalletModel) {
        val bonusFragment = BonusCheckOutFragment.newInstance({
            dismiss()
            callBack.onResponce(it)
        },model)
        bonusFragment.show(childFragmentManager, "")
    }



    var billingClient: BillingClient? = null
    var inAppProductList: ArrayList<ProductDetails> = ArrayList()
    fun initalizeBill() {
        billingClient = BillingClient.newBuilder(requireContext())
            .setListener(this)
            .enablePendingPurchases()
            .build()
        startBillingConnection()
    }

    private fun startBillingConnection() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.d(Constants.tag, "Not Connected Connect Again")
                startBillingConnection()
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.d(Constants.tag, "startConnection: " + billingResult.responseCode)
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    InitPurchases()
                   previousPurchaseDetails
                    populateDataList()
                }
            }
        })
    }


    val previousPurchaseDetails: Unit
        get() {
            val queryPurchasesParams =
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                    .build()
            billingClient!!.queryPurchasesAsync(queryPurchasesParams) { billingResult, list ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (purchase in list) {
                        consumeItem(purchase)
                    }
                }
            }
        }




    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                 handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(
                Constants.tag,
                "" + billingResult.responseCode + "--" + BillingClient.BillingResponseCode.USER_CANCELED
            )
        } else {
            Log.d(Constants.tag, "" + billingResult.responseCode)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)

                billingClient!!.acknowledgePurchase(acknowledgePurchaseParams.build()) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(
                            Constants.tag,
                            "Billing : Call API Fo Success " + purchase.originalJson
                        )

                        consumeItem(purchase)

                        callAPIUpdateWallet(
                            selectedWalletModel!!.coins + " coins",
                            selectedWalletModel!!.coins,
                            selectedWalletModel!!.price,
                            purchase.purchaseToken
                        )
                    } else {
                        Log.d(Constants.tag, "ResponseCode : " + billingResult.responseCode)
                    }
                }
            }
        }
    }


    fun consumeItem(purchase: Purchase) {
        val consumeParams =
            ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient!!.consumeAsync(consumeParams, object : ConsumeResponseListener {
            override fun onConsumeResponse(p0: BillingResult, p1: String) {
                TODO("Not yet implemented")
            }
        })
    }


    // when we click the continue btn this method will call
    fun purchaseItem(postion: Int) {
        var selectedProduct: ProductDetails? = null
        Log.d(Constants.tag, "inAppProductList Size: " + inAppProductList.size)
        for (item in inAppProductList) {
            if (item.productId == "" + datalist[postion].getId()) {
                selectedProduct = item
            }
        }
        if (selectedProduct != null) {
            val productDetailsParamsList: MutableList<ProductDetailsParams> = ArrayList()
            productDetailsParamsList.add(
                ProductDetailsParams.newBuilder()
                    .setProductDetails(selectedProduct)
                    .build()
            )
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()
            activity?.let { billingClient!!.launchBillingFlow(it, billingFlowParams) }
        }
    }


    private fun InitPurchases() {
        val queryProductDetailsParams = QueryProductDetailsParams
            .newBuilder()
            .setProductList(inAppProducts)
        billingClient!!.queryProductDetailsAsync(queryProductDetailsParams.build()) { billingResult, productDetailsList ->
            Log.d(Constants.tag, "queryProductDetailsAsync: " + billingResult.responseCode)
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                populateRegisterInAppProducts(productDetailsList)
            }
        }
    }

    private fun populateRegisterInAppProducts(productDetailsList: List<ProductDetails>) {
        Log.d(Constants.tag, "populateRegisterInAppProducts: $productDetailsList")
        inAppProductList.clear()
        for (item in productDetailsList) {
            Log.d(Constants.tag, "productDetails: " + item.productId)
            inAppProductList.add(item)
        }
    }

    private val inAppProducts: List<QueryProductDetailsParams.Product>
        get() {
            val productList: MutableList<QueryProductDetailsParams.Product> = ArrayList()
            productList.add(
                QueryProductDetailsParams
                    .Product.newBuilder()
                    .setProductId(Constants.Product_ID0)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            productList.add(
                QueryProductDetailsParams
                    .Product.newBuilder()
                    .setProductId(Constants.Product_ID1)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            productList.add(
                QueryProductDetailsParams
                    .Product.newBuilder()
                    .setProductId(Constants.Product_ID2)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            productList.add(
                QueryProductDetailsParams
                    .Product.newBuilder()
                    .setProductId(Constants.Product_ID3)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            productList.add(
                QueryProductDetailsParams
                    .Product.newBuilder()
                    .setProductId(Constants.Product_ID4)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            return productList
        }


    fun callAPIUpdateWallet(name: String?, coins: String?, price: String, Tid: String?) {
        val params = JSONObject()
        try {
            params.put("user_id", getSharedPreference(requireContext()).getString(Variables.U_ID, ""))
            params.put("coin", coins)
            params.put("title", name)
            params.put("price", price.replace(Constants.CURRENCY, ""))
            params.put("transaction_id", Tid)
            params.put("device", "android")
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }
            Functions.showLoader(activity, false, false)
            VolleyRequest.JsonPostRequest(
                activity, ApiLinks.purchaseCoin, params, Functions.getHeaders(
                    requireContext()
                )
            ) { resp ->
                Functions.cancelLoader()
                Log.d(Constants.tag, "get coins before run \n$params")
                try {
                    val jsonObject = JSONObject(resp)

                    val code = jsonObject.optString("code")
                    if (code != null && code == "200") {
                        val msgObj = jsonObject.getJSONObject("msg")
                        val userDetailModel =
                            DataParsing.getUserDataModel(msgObj.optJSONObject("User"))
                        val editor = Functions.getSharedPreference(requireContext()).edit()
                        editor.putString(
                            Variables.U_WALLET,
                            max(userDetailModel.wallet.toDouble(), 0.0).toString()
                        )
                        editor.commit()

                    }

                } catch (e: Exception) {
                    Log.d(Constants.tag, "Exception : $e")
                }
            }
        }


    override fun onDestroy() {
        if(billingClient!=null) {
            billingClient!!.endConnection()
        }
        super.onDestroy()
    }


}