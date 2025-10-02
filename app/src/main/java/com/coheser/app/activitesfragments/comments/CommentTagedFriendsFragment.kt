package com.coheser.app.activitesfragments.comments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.adapters.UsersAdapter
import com.coheser.app.databinding.FragmentCommentTagedFriendsBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.Functions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask

class CommentTagedFriendsFragment : BottomSheetDialogFragment {

    var myContext: Context? = null
    var userId: String? = null
    var adapter: UsersAdapter? = null
    var datalist= mutableListOf<UserModel>()
    private var timer = Timer()
    private val DELAY: Long = 1000
    var pageCount = 0
    var ispostFinsh = false
    var linearLayoutManager: LinearLayoutManager? = null
    var callBack: com.coheser.app.interfaces.FragmentCallBack? = null
    var binding: FragmentCommentTagedFriendsBinding? = null

    constructor(userId: String?, callBack: com.coheser.app.interfaces.FragmentCallBack?) {
        this.userId = userId
        this.callBack = callBack
    }

    constructor()

    private var mBehavior: BottomSheetBehavior<*>? = null
    var dialog: BottomSheetDialog? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(getContext(), R.layout.fragment_comment_taged_friends, null)
        dialog!!.setContentView(view)
        mBehavior = BottomSheetBehavior.from(view.parent as View)
        mBehavior!!.setHideable(false)
        mBehavior!!.setDraggable(false)
        mBehavior!!.setPeekHeight(view.context.resources.getDimension(R.dimen._500sdp).toInt(), true)
        mBehavior!!.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState != BottomSheetBehavior.STATE_EXPANDED) {
                    mBehavior!!.setState(BottomSheetBehavior.STATE_EXPANDED)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        return dialog!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCommentTagedFriendsBinding.inflate(layoutInflater, container, false)
        myContext = binding!!.root.context
        linearLayoutManager = LinearLayoutManager(myContext)
        linearLayoutManager!!.orientation = RecyclerView.VERTICAL
        binding!!.recylerview.layoutManager = linearLayoutManager
        binding!!.recylerview.setHasFixedSize(true)
        callApiForGetAllfollowing(true)
        binding!!.searchEdit.addTextChangedListener(
            object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {
                    timer.cancel()
                    timer = Timer()
                    timer.schedule(
                        object : TimerTask() {
                            override fun run() {
                                if (activity != null) {
                                    activity!!.runOnUiThread {
                                        val search_txt = binding!!.searchEdit.text.toString()
                                        pageCount = 0
                                        if (search_txt.length > 0) {
                                            callApiForOtherUsers()
                                        } else {
                                            callApiForGetAllfollowing(true)
                                        }
                                    }
                                }
                            }
                        },
                        DELAY
                    )
                }
            }
        )
        adapter = UsersAdapter(myContext!!,datalist,object : AdapterClickListener{
            override fun onItemClick(view: View?, pos: Int, `object`: Any?) {
                val item1 = `object` as UserModel
                when (view!!.id) {
                    R.id.mainlayout -> {
                        item1.isSelected = !item1.isSelected
                        adapter!!.notifyDataSetChanged()
                    }
                }
            }

        })
        binding!!.recylerview.adapter = adapter
        binding!!.recylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var userScrolled = false
            var scrollOutitems = 0
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollOutitems = linearLayoutManager!!.findLastVisibleItemPosition()
                Functions.printLog("resp", "" + scrollOutitems)
                if (userScrolled && scrollOutitems == datalist!!.size - 1) {
                    userScrolled = false
                    if (binding!!.loadMoreProgress.visibility != View.VISIBLE && !ispostFinsh) {
                        binding!!.loadMoreProgress.visibility = View.VISIBLE
                        pageCount = pageCount + 1
                        if (binding!!.searchEdit.text.toString().length > 0) {
                            callApiForOtherUsers()
                        } else {
                            callApiForGetAllfollowing(false)
                        }
                    }
                }
            }
        })
        binding!!.refreshLayout.setOnRefreshListener {
            binding!!.refreshLayout.isRefreshing = false
            pageCount = 0
            if (binding!!.searchEdit.text.toString().length > 0) {
                callApiForOtherUsers()
            } else {
                callApiForGetAllfollowing(false)
            }
        }
        binding!!.backBtn.setOnClickListener { dismiss() }
        binding!!.donebtn.setOnClickListener {
            val selectedArray = ArrayList<UserModel>()
            for (i in datalist!!.indices) {
                if (datalist!![i].isSelected) {
                    selectedArray.add(datalist!![i])
                }
            }
            passDataBack(selectedArray)
        }
        return binding!!.root
    }

    //call api for get the all follwers of specific profile
    private fun callApiForOtherUsers() {
        val parameters = JSONObject()
        try {
            parameters.put("type", "user")
            parameters.put("keyword", binding!!.searchEdit.text.toString())
            parameters.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity,
            com.coheser.app.apiclasses.ApiLinks.search,
            parameters,
            Functions.getHeaders(myContext)
        ) { resp ->
            Functions.checkStatus(activity, resp)
            parseFollowingData(resp)
        }
    }

    // Bottom two function will call the api and get all the videos form api and parse the json data
    private fun callApiForGetAllfollowing(isProgressShow: Boolean) {

        val parameters = JSONObject()
        try {
            parameters.put(
                "user_id",
                Functions.getSharedPreference(myContext).getString(com.coheser.app.simpleclasses.Variables.U_ID, "")
            )
            parameters.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isProgressShow) {
            binding!!.pbar.visibility = View.VISIBLE
        }
        VolleyRequest.JsonPostRequest(
            activity,
            com.coheser.app.apiclasses.ApiLinks.showFollowing,
            parameters,
            Functions.getHeaders(myContext)
        ) { resp ->
            Functions.checkStatus(activity, resp)
            if (isProgressShow) {
                binding!!.pbar.visibility = View.GONE
            }
            parseFollowingData(resp)
        }
    }

    fun parseFollowingData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msg = jsonObject.optJSONArray("msg")
                val temp_list = ArrayList<UserModel>()
                for (i in 0 until msg.length()) {
                    val data = msg.optJSONObject(i)
                    var userObj = data.optJSONObject("User")

                    val userDetailModel = getUserDataModel(userObj)


                    temp_list.add(userDetailModel)
                }
                if (pageCount == 0) {
                    datalist!!.clear()
                    datalist!!.addAll(temp_list)
                } else {
                    datalist!!.addAll(temp_list)
                }
                adapter!!.notifyDataSetChanged()
            }
            if (datalist!!.isEmpty()) {
                binding!!.noDataLayout.visibility = View.VISIBLE
            } else {
                binding!!.noDataLayout.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            binding!!.loadMoreProgress.visibility = View.GONE
        }
    }

    // this will open the profile of user which have uploaded the currenlty running video
    private fun passDataBack(datalist: ArrayList<UserModel>) {
        val bundle = Bundle()
        bundle.putBoolean("isShow", true)
        bundle.putSerializable("data", datalist)
        callBack!!.onResponce(bundle)
        dismiss()
    }
}