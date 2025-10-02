package com.coheser.app.activitesfragments.profile.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AbsListView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.analytics.CustomeCalenderFragment
import com.coheser.app.activitesfragments.profile.analytics.DateOperations.getDate
import com.coheser.app.activitesfragments.profile.analytics.DateOperations.getDays
import com.coheser.app.activitesfragments.profile.analytics.DateSelectSheetFragment
import com.coheser.app.activitesfragments.walletandwithdraw.MyWallet
import com.coheser.app.adapters.PromotionHistoryAdapter
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.ActivityPromotionHistoryBinding
import com.coheser.app.databinding.PromoteDetailBinding
import com.coheser.app.models.PromotionHistoryModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.DataParsing.parsePromotionHistory
import com.coheser.app.simpleclasses.DateOprations.getCurrentDate
import com.coheser.app.simpleclasses.DateOprations.getDurationInDays
import com.coheser.app.simpleclasses.Dialogs.showAlert
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.getSuffix
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale

class PromotionHistoryActivity : AppCompatLocaleActivity() {
    var binding: ActivityPromotionHistoryBinding? = null
    var startCalender: Calendar? = null
    var endCalender: Calendar? = null
    var totalDays: Long = 7
    var totalCoins = "0"
    var totalDestinationTap = "0"
    var totalLikes = "0"
    var totalViews = "0"
    var myWalletCoins: Long = 0
    var linearLayoutManager: LinearLayoutManager? = null
    var pageCount = 0
    var ispostFinsh = false
    var dataList = ArrayList<PromotionHistoryModel>()
    var itemPromotionSelected: PromotionHistoryModel? = null
    var adapter: PromotionHistoryAdapter? = null
    var isNotifyCallback = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_promotion_history)
        initControl()
        actionControl()
    }

    private fun actionControl() {
        binding!!.ivBack.setOnClickListener { onBackPressed() }
        binding!!.selectDateLayout.setOnClickListener { openBottomSheetforDate() }
    }

    private fun openBottomSheetforDate() {
        val fragment = DateSelectSheetFragment { bundle ->
            if (bundle != null) {
                if (bundle.getBoolean("isCustom")) {
                    openBottomSheetforCalender()
                } else {
                    startCalender!!.timeInMillis = bundle.getLong("startDate")
                    endCalender!!.timeInMillis = bundle.getLong("endDate")
                    pageCount = 0
                    callApiShowHistory()
                }
            }
        }
        val bundle = Bundle()
        bundle.putLong("startDate", startCalender!!.timeInMillis)
        bundle.putLong("endDate", endCalender!!.timeInMillis)
        fragment.arguments = bundle
        fragment.show(supportFragmentManager, "DateSelectSheetF")
    }

    private fun openBottomSheetforCalender() {
        val fragment = CustomeCalenderFragment { bundle ->
            if (bundle != null) {
                startCalender!!.timeInMillis = bundle.getLong("startDate")
                endCalender!!.timeInMillis = bundle.getLong("endDate")
                pageCount = 0
                callApiShowHistory()
            }
        }
        val bundle = Bundle()
        bundle.putLong("startDate", startCalender!!.timeInMillis)
        bundle.putLong("endDate", endCalender!!.timeInMillis)
        fragment.arguments = bundle
        fragment.show(supportFragmentManager, "DateSelectSheetF")
    }

    private fun initControl() {
        myWalletCoins = getSharedPreference(binding!!.root.context)
            .getString(Variables.U_WALLET, "0")!!.toLong()
        setupDates()
        setupAdapter()
        pageCount = 0
        callApiShowHistory()
    }

    private fun setupDates() {
        startCalender = Calendar.getInstance()
        endCalender = Calendar.getInstance()
        startCalender!!.set(Calendar.DAY_OF_YEAR, startCalender!!.get(Calendar.DAY_OF_YEAR) - 7)
    }

    private fun setupAdapter() {
        linearLayoutManager = LinearLayoutManager(binding!!.root.context)
        linearLayoutManager!!.orientation = RecyclerView.VERTICAL
        binding!!.recylerview.layoutManager = linearLayoutManager
        adapter = PromotionHistoryAdapter(dataList) { view, pos, `object` ->
            itemPromotionSelected = dataList[pos]
            when (view.id) {
                R.id.btnPromoteAgain -> {
                    addNewPromotionByUsingOldData()
                }
                R.id.mainLay -> {
                    openPromoteDetailSheet(itemPromotionSelected!!)
                }
                R.id.btnCancle -> {
                    if (itemPromotionSelected!!.status.equals("stopped", ignoreCase = true)) {
                        showAlert(
                            this@PromotionHistoryActivity,
                            getString(R.string.re_run_ad),
                            getString(R.string.are_you_sure_you_want_to_run_your_ad_again),
                            getString(R.string.yes).uppercase(
                                Locale.getDefault()
                            ),
                            getString(R.string.no).uppercase(Locale.getDefault())
                        ) { s ->
                            if (s.equals("yes", ignoreCase = true)) {
                                callApiUpdatePromotion(itemPromotionSelected, "1")
                            }
                        }
                    } else if (itemPromotionSelected!!.status.equals("active", ignoreCase = true)) {
                        showAlert(
                            this@PromotionHistoryActivity,
                            getString(R.string.stop_ad),
                            getString(R.string.are_you_sure_you_want_to_stop_your_ad),
                            getString(R.string.yes).uppercase(
                                Locale.getDefault()
                            ),
                            getString(R.string.no).uppercase(Locale.getDefault())
                        ) { s ->
                            if (s.equals("yes", ignoreCase = true)) {
                                callApiUpdatePromotion(itemPromotionSelected, "2")
                            }
                        }
                    }
                }
            }
        }
        (binding!!.recylerview.itemAnimator as SimpleItemAnimator?)!!.supportsChangeAnimations =
            false
        binding!!.recylerview.adapter = adapter
        binding!!.recylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var userScrolled = false
            var scrollOutitems = 0
            var scrollInItem = 0
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollInItem = linearLayoutManager!!.findFirstVisibleItemPosition()
                scrollOutitems = linearLayoutManager!!.findLastVisibleItemPosition()
                recyclerView.isNestedScrollingEnabled = scrollInItem == 0
                if (userScrolled && scrollOutitems == dataList.size - 1) {
                    userScrolled = false
                    if (binding!!.loadMoreProgress.visibility != View.VISIBLE && !ispostFinsh) {
                        binding!!.loadMoreProgress.visibility = View.VISIBLE
                        pageCount = pageCount + 1
                        callApiShowHistory()
                    }
                }
            }
        })
        binding!!.refreshLayout.setOnRefreshListener {
            binding!!.refreshLayout.isRefreshing = false
            pageCount = 0
            callApiShowHistory()
        }
    }

    private fun addNewPromotionByUsingOldData() {
        val total = itemPromotionSelected!!.coin.toLong()
        if (myWalletCoins > total) {
            requestToPromoteUserVideo()
        } else {
            val intent = Intent(binding!!.root.context, MyWallet::class.java)
            startActivity(intent)
            resultCallback.launch(intent)
        }
    }

    fun requestToPromoteUserVideo() {
        val params = JSONObject()
        try {
            val differenceDays = getDurationInDays(
                "yyyy-MM-dd HH:mm:ss",
                itemPromotionSelected!!.start_datetime,
                itemPromotionSelected!!.end_datetime
            )
            params.put(
                "user_id", getSharedPreference(
                    binding!!.root.context
                ).getString(Variables.U_ID, "")
            )
            params.put("video_id", itemPromotionSelected!!.video_id)
            params.put("destination", itemPromotionSelected!!.destination)
            params.put("audience_id", itemPromotionSelected!!.audience_id)
            params.put("start_datetime", getCurrentDate("yyyy-MM-dd HH:mm:ss"))
            params.put(
                "end_datetime",
                getCurrentDate("yyyy-MM-dd HH:mm:ss", Integer.valueOf(differenceDays))
            )
            params.put("coin", itemPromotionSelected!!.coin)
            params.put("total_reach", itemPromotionSelected!!.total_reach)
            if (itemPromotionSelected!!.destination.equals("website", ignoreCase = true)) {
                params.put("action_button", itemPromotionSelected!!.action_button)
                params.put("website_url", itemPromotionSelected!!.website_url)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d(Constants.tag, "params: $params")
        showLoader(this@PromotionHistoryActivity, false, false)
        VolleyRequest.JsonPostRequest(
            this@PromotionHistoryActivity,
            ApiLinks.addPromotion,
            params,
            getHeaders(this@PromotionHistoryActivity)
        ) { resp ->
            checkStatus(this@PromotionHistoryActivity, resp)
            cancelLoader()
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code != null && code == "200") {
                    val msgObj = jsonObject.getJSONObject("msg")
                    val userDetailModel = getUserDataModel(msgObj.optJSONObject("User"))
                    val editor = getSharedPreference(
                        binding!!.root.context
                    ).edit()
                    editor.putString(Variables.U_WALLET, "" + userDetailModel.wallet)
                    editor.commit()
                    pageCount = 0
                    callApiShowHistory()
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception: $e")
            }
        }
    }

    private fun callApiShowHistory() {
        totalDays = getDays(startCalender!!.time, endCalender!!.time)
        binding!!.dateRangeTxt.text = getDate(
            startCalender!!.timeInMillis,
            "MMM dd"
        ) + " - " + getDate(endCalender!!.timeInMillis, "MMM dd")
        binding!!.daysTxt.text =
            binding!!.root.context.getString(R.string.last) + " " + totalDays + " " + binding!!.root.context.getString(
                R.string.days
            )
        val parameters = JSONObject()
        try {
            parameters.put(
                "user_id", getSharedPreference(
                    binding!!.root.context
                ).getString(Variables.U_ID, "")
            )
            parameters.put(
                "start_datetime",
                getDate(startCalender!!.timeInMillis, "yyyy-MM-dd hh:mm:ss")
            )
            parameters.put(
                "end_datetime",
                getDate(endCalender!!.timeInMillis, "yyyy-MM-dd hh:mm:ss")
            )
            parameters.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this@PromotionHistoryActivity, ApiLinks.showPromotions, parameters, getHeaders(
                binding!!.root.context
            )
        ) { resp ->
            checkStatus(this@PromotionHistoryActivity, resp)
            binding!!.refreshLayout.isRefreshing = false
            parseData(resp)
        }
    }

    // parse the video list data
    fun parseData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgObj = jsonObject.getJSONObject("msg")
                val statsObj = msgObj.getJSONObject("Stats")
                val detailsArray = msgObj.getJSONArray("Details")
                val temp_list = ArrayList<PromotionHistoryModel>()
                totalCoins = statsObj.optString("total_coins", "0")
                totalDestinationTap = statsObj.optString("total_destination_tap", "0")
                totalLikes = statsObj.optString("total_likes", "0")
                totalViews = statsObj.optString("total_views", "0")
                setupDashboard()
                for (i in 0 until detailsArray.length()) {
                    val itemdata = detailsArray.optJSONObject(i)
                    val video = itemdata.optJSONObject("Video")
                    val item = parsePromotionHistory(itemdata.optJSONObject("Promotion"))
                    item.video_thumb = video.optString("thum")
                    item.video_views = video.optString("view")
                    temp_list.add(item)
                }
                if (pageCount == 0) {
                    dataList.clear()
                    dataList.addAll(temp_list)
                } else {
                    dataList.addAll(temp_list)
                }
                adapter!!.notifyDataSetChanged()
            } else {
                if (pageCount == 0) {
                    pageCount = 0
                    dataList.clear()
                    adapter!!.notifyDataSetChanged()
                }
            }
            if (dataList.isEmpty()) {
                binding!!.noDataLayout.visibility = View.VISIBLE
            } else {
                binding!!.noDataLayout.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
        } finally {
            binding!!.loadMoreProgress.visibility = View.GONE
        }
    }

    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {

            override fun onActivityResult(result: ActivityResult?) {
                if (result!!.resultCode == RESULT_OK) {
                    val data = result.data
                    if (data!!.getBooleanExtra("isShow", false)) {
                        myWalletCoins = getSharedPreference(
                            binding!!.root.context
                        )
                            .getString(Variables.U_WALLET, "0")!!.toLong()
                        addNewPromotionByUsingOldData()
                    }
                }
            }
        })

    private fun setupDashboard() {
        binding!!.tvCoinSpent.text = getSuffix(totalCoins)
        binding!!.tvVideoViews.text = getSuffix(totalViews)
        binding!!.tvLinkClicks.text = getSuffix(totalDestinationTap)
        binding!!.tvTotalLikes.text = getSuffix(totalLikes)
    }

    override fun onBackPressed() {
        if (isNotifyCallback) {
            val intent = Intent()
            intent.putExtra("isShow", true)
            setResult(RESULT_OK, intent)
        }
        finish()
    }
    private fun openPromoteDetailSheet(model: PromotionHistoryModel) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bsheet = PromoteDetailBinding.inflate(LayoutInflater.from(this))
        bottomSheetDialog.setContentView(bsheet.root)

        bsheet.ivVideo.setController(
            Functions.frescoImageLoad(
                model.video_thumb,
                R.drawable.image_placeholder,
                bsheet.ivVideo,
                false
            )
        )

        bsheet.txtDestinationValue.text = model.destination
        bsheet.txtActionValue.text = model.action_button
        bsheet.durationTxt.text = "${getDurationInDays("yyyy-MM-dd HH:mm:ss", model.start_datetime, model.end_datetime)} ${getString(R.string.day)}"
        bsheet.totalCoinsTxt.text = "${getSuffix(model.coin)}"
        bsheet.spentCoinsTxt.text = "${getSuffix(model.coins_consumed)}"
        bsheet.videoViewsTxt.text = "${getSuffix(model.video_views)}"
        bsheet.linkClickTxt.text = "${getSuffix(model.destination_tap)}"
        bsheet.okayBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // Add an OnShowListener to expand the bottom sheet once it's shown
        bottomSheetDialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }

        bottomSheetDialog.show()
    }

    private fun callApiUpdatePromotion(model: PromotionHistoryModel?, status: String) {
        // status 1 for re-run ad again : status 2 for stop ad
        val parameters = JSONObject()
        try {
            parameters.put("active", status)
            parameters.put("promotion_id", model!!.id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this@PromotionHistoryActivity, ApiLinks.updatePromotion, parameters, getHeaders(
                binding!!.root.context
            )
        ) { resp ->
            checkStatus(this@PromotionHistoryActivity, resp)
            parsePromotion(resp)
        }
    }

    fun parsePromotion(response: String?) {
        try {
            val jsonObject = JSONObject(response)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgObj = jsonObject.getJSONObject("msg")
                val userObj = msgObj.getJSONObject("User")
                val wallet = userObj.optString("wallet")
                getSharedPreference(applicationContext).edit().putString(Variables.U_WALLET, wallet)
                    .apply()
                pageCount = 0
                callApiShowHistory()
            }
        } catch (exp: Exception) {
            Log.d(Constants.tag, exp.toString())
        }
    }


}