package com.coheser.app.activitesfragments.spaces

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.spaces.adapters.FriendsSelectionAdapter
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentAddFriendsSelectionBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Variables
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class AddFriendsSelectionF : BottomSheetDialogFragment, View.OnClickListener {
    lateinit var binding: FragmentAddFriendsSelectionBinding
    var callBack: FragmentCallBack? = null
    var adapter: FriendsSelectionAdapter? = null
    var datalist: ArrayList<UserModel>? = ArrayList()
    private var timer: Timer? = null

    var pageCount: Int = 0
    var ispostFinsh: Boolean = false
    var layoutManager: GridLayoutManager? = null
    var isFromClub: Boolean = false
    var clubId: String? = null


    constructor()

    constructor(callBack: FragmentCallBack?, isFromClub: Boolean) {
        this.callBack = callBack
        this.isFromClub = isFromClub
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_friends_selection,
            container,
            false
        )
        initControl()
        return binding.getRoot()
    }

    private fun initControl() {
        binding.tvDone.setOnClickListener(this)

        if (isFromClub) {
            clubId = arguments?.getString("clubId")
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (timer != null) {
                    timer!!.cancel()
                }
            }

            override fun afterTextChanged(s: Editable) {
                timer = Timer()
                timer!!.schedule(object : TimerTask() {
                    override fun run() {
                        activity!!.runOnUiThread { }
                    }
                }, 500)
            }
        })

        setupAdapter()

        populateData()
    }

    private fun populateData() {
        if (isFromClub) {
            clubMembersList
        } else {
            friendsList
        }
    }


    val friendsList: Unit
        get() {
            if (datalist == null) datalist = ArrayList()

            val parameters = JSONObject()
            try {
                parameters.put("starting_point", "" + pageCount)
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }


            VolleyRequest.JsonPostRequest(
                activity, ApiLinks.showFriends, parameters, getHeaders(
                    activity
                )
            ) { resp -> parseFriendsData(resp) }
        }


    val clubMembersList: Unit
        get() {
            if (datalist == null) datalist = ArrayList()

            val parameters = JSONObject()
            try {
                parameters.put(
                    "user_id", getSharedPreference(
                        context
                    ).getString(Variables.U_ID, "")
                )
                parameters.put("club_id", clubId)
                parameters.put("starting_point", "" + pageCount)
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }



            VolleyRequest.JsonPostRequest(
                activity, ApiLinks.showClubMembers, parameters, getHeaders(
                    activity
                )
            ) { resp -> parseClubMembersData(resp) }
        }

    fun parseFriendsData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgArray = jsonObject.getJSONArray("msg")

                val temp_list = ArrayList<UserModel>()

                for (i in 0 until msgArray.length()) {
                    val `object` = msgArray.optJSONObject(i)
                    val item = getUserDataModel(`object`.optJSONObject("Friends"))


                    val userStatus = item.button!!.lowercase(Locale.getDefault())
                    if (userStatus.equals("following", ignoreCase = true)) {
                        item.button = ("Following")
                    } else if (userStatus.equals("friends", ignoreCase = true)) {
                        item.button = ("Friends")
                    } else if (userStatus.equals("follow back", ignoreCase = true)) {
                        item.button = ("Follow back")
                    } else {
                        item.button = ("Follow")
                    }

                    temp_list.add(item)
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
                binding.tabNoData.visibility = View.VISIBLE
                binding.tvNoData.text =
                    binding.root.context.getString(R.string.you_have_no_friends)
            } else {
                binding.tabNoData.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        } finally {
            binding.loadMoreProgress.visibility = View.GONE
        }
    }


    fun parseClubMembersData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgArray = jsonObject.getJSONArray("msg")

                val temp_list = ArrayList<UserModel>()

                for (i in 0 until msgArray.length()) {
                    val `object` = msgArray.optJSONObject(i)
                    val userDetailModel = getUserDataModel(`object`.optJSONObject("User"))

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
                binding.tabNoData.visibility = View.VISIBLE
                binding.tvNoData.text =
                    binding.root.context.getString(R.string.you_have_no_friends)
            } else {
                binding.tabNoData.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        } finally {
            binding.loadMoreProgress.visibility = View.GONE
        }
    }


    private fun setupAdapter() {
        layoutManager = GridLayoutManager(binding.root.context, 3)
        layoutManager!!.orientation = RecyclerView.VERTICAL
        binding.recylerview.layoutManager = layoutManager
        binding.recylerview.setHasFixedSize(true)

        adapter = FriendsSelectionAdapter(datalist!!) { view, pos, `object` ->
            val itemUpdate = datalist!![pos]
            if (itemUpdate.isSelected) {
                itemUpdate.isSelected = false
            } else {
                itemUpdate.isSelected = true
            }
            datalist!![pos] = itemUpdate
            adapter!!.notifyDataSetChanged()
        }
        binding.recylerview.adapter = adapter
        binding.recylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var userScrolled: Boolean = false
            var scrollOutitems: Int = 0

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                scrollOutitems = layoutManager!!.findLastVisibleItemPosition()

                if (userScrolled && (scrollOutitems == datalist!!.size - 1)) {
                    userScrolled = false

                    if (binding.loadMoreProgress.visibility != View.VISIBLE && !ispostFinsh) {
                        binding.loadMoreProgress.visibility = View.VISIBLE
                        pageCount = pageCount + 1
                        populateData()
                    }
                }
            }
        })
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvDone -> {
                val selectedUser = ArrayList<UserModel>()
                for (item in datalist!!) {
                    if (item.isSelected) {
                        selectedUser.add(item)
                    }
                }
                val bundle = Bundle()
                bundle.putBoolean("isShow", true)
                bundle.putSerializable("UserList", selectedUser)
                callBack!!.onResponce(bundle)
                dismiss()
            }
        }
    }
}