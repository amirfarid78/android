package com.coheser.app.activitesfragments.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.coheser.app.R
import com.coheser.app.activitesfragments.TagedVideosActivity
import com.coheser.app.adapters.HashTagFavouriteAdapter
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.models.HashTagModel
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.isStringHasValue
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Variables
import com.facebook.shimmer.ShimmerFrameLayout
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject

// search the hash tag
class SearchHashTagsFragment : Fragment() {
    var viewRoot: View? = null
    var type: String? = null
    var shimmerFrameLayout: ShimmerFrameLayout? = null
    var recyclerView: RecyclerView? = null
    var linearLayoutManager: LinearLayoutManager? = null
    var loadMoreProgress: ProgressBar? = null
    var noDataTxt: TextView? = null
    var pageCount = 0
    var ispostFinsh = false
    var dataList: ArrayList<HashTagModel>? = null
    var adapter: HashTagFavouriteAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewRoot = inflater.inflate(R.layout.fragment_search, container, false)
        type = if (arguments != null && isStringHasValue(
                arguments?.getString("type")
            )
        ) {
            arguments?.getString("type")
        } else {
            "hashtag"
        }
        shimmerFrameLayout = viewRoot?.findViewById(R.id.shimmer_view_container)
        noDataTxt = viewRoot?.findViewById(R.id.nodataTxt)
        shimmerFrameLayout?.startShimmer()
        recyclerView = viewRoot?.findViewById(R.id.recylerview)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView?.setLayoutManager(linearLayoutManager)
        dataList = ArrayList()
        adapter = HashTagFavouriteAdapter(requireContext(), dataList!!) { view, pos, `object` ->
            when (view.id) {
                else -> {
                    val item = `object` as HashTagModel
                    openHashtag(item.name)
                }
            }
        }
        (recyclerView?.getItemAnimator() as SimpleItemAnimator?)!!.supportsChangeAnimations = false
        recyclerView?.setAdapter(adapter)
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                printLog("resp", "" + scrollOutitems)
                if (userScrolled && scrollOutitems == dataList!!.size - 1) {
                    userScrolled = false
                    if (loadMoreProgress!!.visibility != View.VISIBLE && !ispostFinsh) {
                        loadMoreProgress!!.visibility = View.VISIBLE
                        pageCount = pageCount + 1
                        if (type != null && type.equals("favourite", ignoreCase = true)) {
                            callApiGetFavourite()
                        } else callApiSearch()
                    }
                }
            }
        })
        loadMoreProgress = viewRoot?.findViewById(R.id.load_more_progress)
        pageCount = 0
        if (type != null && type.equals("favourite", ignoreCase = true)) {
            callApiGetFavourite()
        } else callApiSearch()
        return viewRoot
    }

    // get the hashtage that a user search for
    fun callApiSearch() {
        val params = JSONObject()
        try {
            if (getSharedPreference(context).getString(Variables.U_ID, null) != null) {
                params.put("user_id", getSharedPreference(context).getString(Variables.U_ID, "0"))
            }
            params.put("type", type)
            params.put("keyword", SearchMainActivity.searchEdit.text.toString())
            params.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.search, params, getHeaders(
                activity
            )
        ) { resp ->
            checkStatus(activity, resp)
            shimmerFrameLayout!!.stopShimmer()
            shimmerFrameLayout!!.visibility = View.GONE
            parseData(resp)
        }
    }

    // get the hash tag that a user is favourite it
    fun callApiGetFavourite() {
        val params = JSONObject()
        try {
            params.put("user_id", getSharedPreference(context).getString(Variables.U_ID, "0"))
            params.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.showFavouriteHashtags, params, getHeaders(
                activity
            )
        ) { resp ->
            checkStatus(activity, resp)
            shimmerFrameLayout!!.stopShimmer()
            shimmerFrameLayout!!.visibility = View.GONE
            parseData(resp)
        }
    }

    // parse the data of hashtag list
    fun parseData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgArray = jsonObject.getJSONArray("msg")
                val temp_list = ArrayList<HashTagModel>()
                for (i in 0 until msgArray.length()) {
                    val itemdata = msgArray.optJSONObject(i)
                    val hashtag = itemdata.optJSONObject("Hashtag")
                    val item = HashTagModel()
                    item.id = hashtag.optString("id")
                    item.name = hashtag.optString("name")
                    item.views = hashtag.optString("views")
                    item.videos_count = hashtag.optString("videos_count")
                    item.fav = hashtag.optString("favourite", "1")
                    temp_list.add(item)
                }
                if (pageCount == 0) {
                    dataList!!.clear()
                }
                dataList!!.addAll(temp_list)
                adapter!!.notifyDataSetChanged()
                if (dataList!!.isEmpty()) {
                    viewRoot!!.findViewById<View>(R.id.no_data_layout).visibility = View.VISIBLE
                } else {
                    viewRoot!!.findViewById<View>(R.id.no_data_layout).visibility = View.GONE
                }
            } else {
                if (dataList!!.isEmpty()) {
                    viewRoot!!.findViewById<View>(R.id.no_data_layout).visibility = View.VISIBLE
                    noDataTxt!!.text =
                        getString(R.string.no_result_found_for) + SearchMainActivity.searchEdit.text.toString() + "\""
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            loadMoreProgress!!.visibility = View.GONE
        }
    }

    // open the video list against the hashtags
    private fun openHashtag(tag: String) {
        val intent = Intent(viewRoot!!.context, TagedVideosActivity::class.java)
        intent.putExtra("tag", tag)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String?): SearchHashTagsFragment {
            val fragment = SearchHashTagsFragment()
            val args = Bundle()
            args.getString("type", type)
            fragment.arguments = args
            return fragment
        }
    }
}
