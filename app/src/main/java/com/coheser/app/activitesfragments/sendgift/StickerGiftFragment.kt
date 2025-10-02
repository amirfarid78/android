package com.coheser.app.activitesfragments.sendgift

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.walletandwithdraw.MyWallet
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentStickerGiftBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.Dialogs.showAlert
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.createChunksOfList
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Variables
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.coheser.app.activitesfragments.EditTextSheetFragment
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.Callback
import io.paperdb.Paper
import org.json.JSONObject

class StickerGiftFragment(val callBack: FragmentCallBack) : BottomSheetDialogFragment() {

    var total_coins: Double = 0.0
    var giftSliderAdapter: SendGiftVHAdapter? = null
    var sliderList: ArrayList<MutableList<GiftModel>> = ArrayList()
    var data_list: ArrayList<GiftModel> = ArrayList()
    var selectedModel: GiftModel? = null


    var receiverID=""
    var videoID=""
    var streamingId=""

    lateinit var from:String


    var dialog: BottomSheetDialog? = null
    private var mBehavior: BottomSheetBehavior<View>? = null
    lateinit var binding: FragmentStickerGiftBinding



    companion object {
         val fromWishList = "wishList"
         val fromSendGift = "sendGift"

        @JvmStatic
        fun newInstance(userID: String,streamingID: String,videoID: String,from:String, callBack: FragmentCallBack) = StickerGiftFragment(callBack).apply {
            arguments = Bundle().apply {
                putString("userID",userID)
                putString("streamingID",streamingID)
                putString("videoId",videoID)
                putString("from",from)
            }
        }

        @JvmStatic
        fun newInstance(from:String, callBack: FragmentCallBack) = StickerGiftFragment(callBack).apply {
            arguments = Bundle().apply {
                putString("from",from)
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            receiverID=it.getString("userID","")
            videoID=it.getString("videoId","")
            streamingId=it.getString("streamingID","")
            from=it.getString("from", fromSendGift)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        binding = FragmentStickerGiftBinding.inflate(layoutInflater)
        dialog!!.setContentView(binding.root)
        mBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        mBehavior!!.setPeekHeight(binding.root.context.resources.getDimension(R.dimen._550sdp).toInt(), true)
        mBehavior!!.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState != BottomSheetBehavior.STATE_EXPANDED) {
                    mBehavior!!.setState(BottomSheetBehavior.STATE_EXPANDED)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        return dialog!!
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentStickerGiftBinding.inflate(inflater, container, false)

        showCoins()
        setUpGiftSliderAdapter()

        if(from== fromWishList){
            binding.saveBtn.visibility=View.VISIBLE
            binding.rechargeBtn.visibility=View.GONE

            binding.wishListTitleLayout.visibility=View.VISIBLE
            binding.rechargeTitleLayout.visibility=View.GONE

            binding.giftCountLayout.visibility=View.VISIBLE

            binding.count20Txt.setOnClickListener {
                selectCount(20)
            }
            binding.count50Txt.setOnClickListener{
                selectCount(50)
            }
            binding.count100Txt.setOnClickListener{
                selectCount(100)
            }
            binding.customizeTxt.setOnClickListener{
                selectCount(0)
                binding.customizeTxt.background=ContextCompat.getDrawable(requireContext(),R.drawable.bg_outline_appcolor)

                val fragment = EditTextSheetFragment(EditTextSheetFragment.commentSelectNumber, null) { bundle ->
                    if (bundle.getBoolean("isShow", false)) {
                        val message = bundle.getString("message")
                        if (message != null) {
                            giftSelectedCount=message.toInt()
                        }
                    }
                }
                fragment.show(childFragmentManager, "EditTextSheetF")

            }
            binding.saveBtn.setOnClickListener {
                if(selectedModel!=null){
                val bundle=Bundle()
                selectedModel?.count=giftSelectedCount
                bundle.putParcelable("Data",selectedModel)

                callBack!!.onResponce(bundle)
                dismiss()
                }
            }
            binding.backBtn.setOnClickListener {
                dismiss()
            }

        }
        else {
            binding.rechargeBtn.setOnClickListener {
                dismiss()
                startActivity(Intent(activity, MyWallet::class.java))
            }
            binding.bonusRechargeBtn.setOnClickListener { showBonusBottomSheet() }
        }
        hitSHowGiftScreen()
        return binding.root
    }

    fun showCoins(){
        val wallet = getSharedPreference(requireContext()).getString(Variables.U_WALLET, "0")
        total_coins = wallet!!.toDouble()
        binding.coinsTxt.setText(wallet)
    }

    var giftSelectedCount=1
     fun selectCount(count: Int) {
         giftSelectedCount=count
         binding.count20Txt.background=ContextCompat.getDrawable(requireContext(),R.drawable.d_less_round_gray_transparent)
         binding.count50Txt.background=ContextCompat.getDrawable(requireContext(),R.drawable.d_less_round_gray_transparent)
         binding.count100Txt.background=ContextCompat.getDrawable(requireContext(),R.drawable.d_less_round_gray_transparent)
         binding.customizeTxt.background=ContextCompat.getDrawable(requireContext(),R.drawable.d_less_round_gray_transparent)
        when (count) {
            20->{
                binding.count20Txt.background=ContextCompat.getDrawable(requireContext(),R.drawable.bg_outline_appcolor)

            }
            50->{
                binding.count50Txt.background=ContextCompat.getDrawable(requireContext(),R.drawable.bg_outline_appcolor)

            }
            100->{
                binding.count100Txt.background=ContextCompat.getDrawable(requireContext(),R.drawable.bg_outline_appcolor)

            }
        }
    }

    fun showBonusBottomSheet() {
        val bonusFragment = ExtraBonusFragment.newInstance { bundle ->
            if (bundle != null) {
                showRechargeBottomSheet(bundle.getString("type"))
            }
        }
        bonusFragment.show(childFragmentManager, "")
    }

    fun showRechargeBottomSheet(type: String?) {
        val bonusFragment = RechargeBottomSheet.newInstance({ bundle ->
            showCoins()
        }, type!!)
        bonusFragment.show(childFragmentManager, "")
    }

    fun sendGiftAction() {
        for (model in data_list) {
            if (model.isSelected) {
                val coin_required = (model.coin?.times(1))
                if (total_coins >= coin_required!!) {

                    callApiSendGift(model, 1)
                } else {
                    Toast.makeText(
                        activity,
                        requireContext().getString(R.string.you_dont_have_sufficent_coins),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun hitSHowGiftScreen() {
        run {
            if (Paper.book("Gift").contains("giftList")) {
                data_list.clear()
                val list=Paper.book("Gift").read<ArrayList<GiftModel>>("giftList")
                list?.let {
                    data_list.addAll(it)
                    sliderList.clear()
                    sliderList.addAll(createChunksOfList(data_list, 12))
                    giftSliderAdapter!!.notifyDataSetChanged()
                }

            }
        }

        if (data_list.size < 1) {
            binding.progressBar.visibility = View.VISIBLE
        }

        val jsonObject = JSONObject()
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.showGifts, jsonObject, getHeaders(
                activity
            ), object : Callback {
                override fun onResponce(resp: String) {
                    binding.progressBar!!.visibility = View.GONE
                    if (resp != null) {
                        try {
                            val jsonObject = JSONObject(resp)

                            val code = jsonObject.optString("code")
                            if (code != null && code == "200") {
                                val msgarray = jsonObject.getJSONArray("msg")

                                data_list.clear()
                                for (i in 0 until msgarray.length()) {
                                    val giftArray = msgarray.getJSONObject(i)
                                    val giftObj = giftArray.getJSONObject("Gift")
                                    val model = Gson().fromJson(giftObj.toString(), GiftModel::class.java)
                                    model.isSelected = false
                                    model.count = 0
                                    data_list.add(model)
                                }


                                Paper.book("Gift").write<List<GiftModel>>("giftList", data_list)


                                run {
                                    data_list.clear()
                                    data_list.addAll(Paper.book("Gift").read("giftList")!!)

                                    sliderList.clear()
                                    sliderList.addAll(createChunksOfList(data_list, 12))
                                    giftSliderAdapter!!.notifyDataSetChanged()
                                }
                            } else {
                                showAlert(
                                    activity,
                                    activity!!.applicationContext.getString(R.string.server_error),
                                    jsonObject.optString(
                                        "msg",
                                        "Our technical team work on this issue"
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.d(Constants.tag, "Exception : $e")
                        }
                    }
                }
            })

    }

    private fun setUpGiftSliderAdapter() {
        sliderList.clear()
        sliderList.addAll(createChunksOfList(data_list, 12))
        giftSliderAdapter = SendGiftVHAdapter(sliderList,from,{ bundle ->
            if (bundle.getBoolean("isShow", false)) {
                selectedModel = bundle.getParcelable("Data") as GiftModel?
                if (bundle.getBoolean("isSend", false)) {
                    sendGiftAction()
                }
            }
        })
        binding.imageSlider!!.setSliderAdapter(giftSliderAdapter!!)
    }

    fun callApiSendGift(model: GiftModel, giftCount: Int) {

        val params = JSONObject()
        try {
            params.put("sender_id", Variables.sharedPreferences.getString(Variables.U_ID, ""))

            if(receiverID.isNotEmpty()) {
                params.put("receiver_id", receiverID)
            }

            if(videoID.isNotEmpty())
                params.put("video_id", videoID)

            if(streamingId.isNotEmpty())
                params.put("live_streaming_id", streamingId)

            params.put("gift_id", model.id)
            params.put("gift_count", giftCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.sendGift, params, getHeaders(
                activity
            )
        ) { resp ->
            checkStatus(activity, resp)
            cancelLoader()
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code != null && code == "200") {
                    val msgObj = jsonObject.getJSONObject("msg")
                    val userDetailModel = getUserDataModel(msgObj.optJSONObject("User"))
                    val editor = getSharedPreference(
                        requireContext()
                    ).edit()
                    editor.putString(Variables.U_WALLET, "" + userDetailModel.wallet)
                    editor.commit()

                    val bundle = Bundle()
                    bundle.putBoolean("isShow", true)
                    bundle.putString("count", "" + giftCount)
                    bundle.putParcelable("Data", model)
                    callBack.onResponce(bundle)
                    dismiss()
                } else if (code != null && code == "201") {
                    showAlert(
                        activity,
                       requireContext().getString(R.string.server_error),
                        jsonObject.optString("msg")
                    )
                } else Toast.makeText(context, jsonObject.optString("msg"), Toast.LENGTH_SHORT)
                    .show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


}
