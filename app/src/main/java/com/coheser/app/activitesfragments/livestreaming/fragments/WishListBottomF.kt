package com.coheser.app.activitesfragments.livestreaming.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.adapter.TopGifterAdapter
import com.coheser.app.activitesfragments.livestreaming.adapter.WishListGiftSelectAdapter
import com.coheser.app.activitesfragments.livestreaming.model.GiftUsers
import com.coheser.app.activitesfragments.livestreaming.model.GiftWishListModel
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.activitesfragments.sendgift.StickerGiftFragment
import com.coheser.app.activitesfragments.sendgift.GiftModel
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentWishListBottomBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.Dialogs.showAlert
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Variables
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject


class WishListBottomF(val callBack: FragmentCallBack) : BottomSheetDialogFragment() {

    lateinit var liveUserModel: LiveUserModel
    lateinit var binding: FragmentWishListBottomBinding
    lateinit var adapter: WishListGiftSelectAdapter
     var dataList=ArrayList<GiftWishListModel>()
    var from=fromSelection

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog= BottomSheetDialog(requireContext(), R.style.MyTransparentBottomSheetDialogTheme)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            liveUserModel=it.getParcelable("data")!!
            from=it.getString("from")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWishListBottomBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        if(from.equals(fromSelection)){
            binding.addGiftBtn.visibility=View.VISIBLE
            binding.saveBtn.visibility=View.VISIBLE
        }
        else{
            dataList.clear()
            dataList.addAll(liveUserModel.GiftWishList!!)
            binding.topGifterLayout.visibility=View.VISIBLE
        }

        adapter = WishListGiftSelectAdapter(requireContext(), from,dataList) { view, pos, `object` ->
            val model = `object` as GiftWishListModel
            when(view.id){
                R.id.crossBtn ->{
                    dataList.removeAt(pos)
                    adapter.notifyDataSetChanged()
                }
                R.id.sendBtn ->{
                    val wallet = getSharedPreference(requireContext()).getString(Variables.U_WALLET, "0")
                    val total_coins = wallet!!.toDouble()
                    if(total_coins>=model.giftPrice.toDouble()){
                        val giftModel = GiftModel()
                        giftModel.id = model.id.toInt()
                        giftModel.coin = model.giftPrice.toInt()
                        giftModel.icon = model.giftImage
                        giftModel.count = 1
                        giftModel.title = model.giftName

                        val bundle = Bundle()
                        bundle.putBoolean("isShow", false)
                        bundle.putBoolean("showCount", true)
                        bundle.putString("count", "1")
                        bundle.putParcelable("Data", giftModel)
                        callBack!!.onResponce(bundle)

                        callApiSendGift(giftModel, 1)
                    }
                }
            }
        }


        binding.recyclerView.adapter = adapter

        binding.closeBtn.setOnClickListener{
            dismiss()
        }

        binding.addGiftBtn.setOnClickListener{
            openGiftScreen()
        }

        binding.saveBtn.setOnClickListener{
           liveUserModel.GiftWishList=dataList
            val bundle = Bundle()
            bundle.putParcelable("data",liveUserModel)
            callBack.onResponce(bundle)
            dismiss()
        }

        setTopGifterAdapter()

        return binding.root
    }



    fun setTopGifterAdapter(){
        val topList=ArrayList<GiftUsers>()
        for (item in dataList){
            topList.addAll(item.AllGiftUsers!!)
        }

        if(topList.isEmpty()){
            binding.topGifterLayout.visibility=View.GONE
        }
        else{

            val mergedList = topList
                .groupingBy { it.userId }
                .fold(GiftUsers()) { acc, user ->
                    acc.apply {
                        userId = user.userId
                        userName = user.userName
                        userPicture = user.userPicture
                        count += user.count
                    }
                }.values.toList()


            binding.topGifterLayout.visibility=View.VISIBLE

            binding.topRecylerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            val adapter = TopGifterAdapter(requireContext(),mergedList,object : AdapterClickListener {
                override fun onItemClick(view: View?, pos: Int, `object`: Any?) {
                }
            })
            binding.topRecylerView.adapter = adapter

        }
    }

    fun openGiftScreen(){
        val giftFragment = StickerGiftFragment.newInstance(
            StickerGiftFragment.fromWishList,
            object : FragmentCallBack {
                override fun onResponce(bundle: Bundle) {
                        val model = bundle.getParcelable("Data") as GiftModel?

                        val model1 = GiftWishListModel()
                        model1.id = model?.id.toString()
                        model1.giftImage = model?.icon!!
                        model1.giftPrice = model.coin.toString()
                        model1.totalGiftWant = ""+model.count
                        model1.giftName = model.title!!
                        dataList.add(model1)
                        adapter.notifyDataSetChanged()

                }
            })
        giftFragment.show(childFragmentManager, "giftFragment")
    }


    fun callApiSendGift(model: GiftModel, giftCount: Int) {

        val params = JSONObject()
        try {
            params.put("sender_id", Variables.sharedPreferences.getString(Variables.U_ID, ""))
            params.put("receiver_id", liveUserModel.userId)
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
                    callBack!!.onResponce(bundle)
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


    companion object {
        val fromSelection="fromSelection"
        val fromAdmin="fromAdmin"
        val fromJoiner="fromJoiner"
        @JvmStatic
        fun newInstance(model: LiveUserModel,from:String,callBack: FragmentCallBack) = WishListBottomF(callBack).apply {
                arguments = Bundle().apply {
                    putParcelable("data", model)
                    putString("from",from)
                }
            }
    }


}