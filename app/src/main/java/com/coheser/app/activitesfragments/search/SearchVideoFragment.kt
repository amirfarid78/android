package com.coheser.app.activitesfragments.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.WatchVideosActivity
import com.coheser.app.adapters.VideosListAdapter
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentSearchBinding
import com.coheser.app.models.HomeModel
import com.coheser.app.simpleclasses.DataParsing.parseVideoData
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.isStringHasValue
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.MainSearchViewModel
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 */
class SearchVideoFragment : Fragment() {
    lateinit var binding : FragmentSearchBinding
    var type: String? = ""
    var linearLayoutManager: GridLayoutManager? = null
    var pageCount = 0
    var ispostFinsh = false
    var dataList = ArrayList<HomeModel>()
    var adapter: VideosListAdapter? = null


    private val viewModel : MainSearchViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_search, container, false)

        binding.lifecycleOwner = this

        type = if (arguments != null && isStringHasValue(
                arguments?.getString("type")
            )
        ) {
            arguments?.getString("type")
        } else {
            "video"
        }
        linearLayoutManager = GridLayoutManager(context, 3)
        binding.recylerview.setLayoutManager(linearLayoutManager)
        dataList = ArrayList()
        adapter = VideosListAdapter(context, dataList) { view, pos, `object` ->
            val item = `object` as HomeModel
            item.video_id?.let { openWatchVideo(it) }
        }
        (binding.recylerview.getItemAnimator() as SimpleItemAnimator?)!!.supportsChangeAnimations = false
        binding.recylerview.setAdapter(adapter)
        binding.recylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                    if (binding.loadMoreProgress.visibility != View.VISIBLE && !ispostFinsh) {
                        binding.loadMoreProgress.visibility = View.VISIBLE
                        pageCount = pageCount + 1
                        viewModel.getSearchVideoData(pageCount,SearchMainActivity.searchEdit.text.toString(),type!!)
                    }
                }
            }
        })
        pageCount = 0
        viewModel.getSearchVideoData(pageCount,SearchMainActivity.searchEdit.text.toString(),type!!)

        initObserver()
        return binding.root
    }
    fun initObserver(){
        viewModel.videosLiveData.observe(requireActivity()){response ->
            when(response){
                is ApiResponce.Success ->{
                    response.data?.let { list ->
                        dataList.addAll(list)
                        adapter?.updateData(dataList)
                        viewModel.showDataView()
                        binding?.shimmerViewContainer?.visibility = View.GONE
                        binding!!.loadMoreProgress.visibility = View.GONE
                        Log.d(Constants.tag,"datasizeUser : ${dataList.size}")
                    }
                }
                is ApiResponce.Error ->{
                    if (pageCount > 0){
                        binding!!.loadMoreProgress.visibility = View.GONE
                    }else{
                        binding?.shimmerViewContainer?.visibility = View.GONE
//                        viewModel.hideDataView()
                        binding!!.noDataLayout!!.visibility = View.VISIBLE
                        binding!!.nodataTxt!!.text = requireContext().getString(R.string.no_result_found_for) + SearchMainActivity.searchEdit.text.toString() + "\""

                    }
                }
                is ApiResponce.Loading ->{
                    if (pageCount == 0){
                        binding?.shimmerViewContainer?.visibility = View.VISIBLE
                        binding?.shimmerViewContainer?.startShimmer()
                    }
                }

                else ->{}
            }
        }
    }

    fun callApi() {
        val params = JSONObject()
        try {
            params.put("type", "" + type)
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
            binding.shimmerViewContainer.stopShimmer()
            binding.noDataLayout.visibility = View.GONE
            if (type.equals("video", ignoreCase = true)) parseVideo(resp)
        }
    }

    fun parseVideo(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgArray = jsonObject.getJSONArray("msg")
                val temp_list = ArrayList<HomeModel>()
                for (i in 0 until msgArray.length()) {
                    val itemdata = msgArray.optJSONObject(i)
                    val video = itemdata.optJSONObject("Video")
                    val user = itemdata.optJSONObject("User")
                    val sound = itemdata.optJSONObject("Sound")
                    val location = itemdata.optJSONObject("Location")
                    val store = itemdata.optJSONObject("Store")
                    val videoProduct = itemdata.optJSONObject("Product")
                    val userPrivacy = user.optJSONObject("PrivacySetting")
                    val userPushNotification = user.optJSONObject("PushNotification")
                    val item = parseVideoData(
                        user,
                        sound,
                        video,
                        location,
                        store,
                        videoProduct,
                        userPrivacy,
                        userPushNotification
                    )
                    if (isStringHasValue(item.userModel!!.username)) {
                        temp_list.add(item)
                    }
                }
                Log.d(Constants.tag, "video sie" + dataList!!.size)
                if (pageCount == 0) {
                    dataList!!.addAll(temp_list)
                    if (dataList!!.isEmpty()) {
                        binding.noDataLayout.visibility = View.VISIBLE
                    } else {
                        binding.noDataLayout.visibility = View.GONE
                        binding.recylerview.adapter = adapter
                    }
                } else {
                    if (temp_list.isEmpty()) ispostFinsh = true else {
                        dataList!!.addAll(temp_list)
                        adapter!!.notifyDataSetChanged()
                    }
                }
            } else {
                if (dataList!!.isEmpty()) {
                    binding.noDataLayout.visibility = View.VISIBLE
                    binding.nodataTxt.text =
                        getString(R.string.no_result_found_for) + SearchMainActivity.searchEdit.text.toString() + "\""
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            binding.loadMoreProgress.visibility = View.GONE
        }
    }

    private fun openWatchVideo(videoId: String) {
        val intent = Intent(requireActivity(), WatchVideosActivity::class.java)
        intent.putExtra("video_id", videoId)
        intent.putExtra("position", 0)
        intent.putExtra("pageCount", 0)
        intent.putExtra("userId", getSharedPreference(binding.root.context).getString(Variables.U_ID, ""))
        intent.putExtra("whereFrom", Variables.IdVideo)
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String?): SearchVideoFragment {
            val fragment = SearchVideoFragment()
            val args = Bundle()
            args.getString("type", type)
            fragment.arguments = args
            return fragment
        }
    }
}
