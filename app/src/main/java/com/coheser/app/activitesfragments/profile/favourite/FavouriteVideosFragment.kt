package com.coheser.app.activitesfragments.profile.favourite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.activitesfragments.WatchVideosActivity
import com.coheser.app.adapters.VideosListAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentFavouriteVideosBinding
import com.coheser.app.models.HomeModel
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.FavouriteVideosViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavouriteVideosFragment : Fragment() {
    var adapter: VideosListAdapter? = null
    var dataList: ArrayList<HomeModel>? = null
    var linearLayoutManager: GridLayoutManager? = null
    var userId:String?=null

    lateinit var binding:FragmentFavouriteVideosBinding

    private val viewModel: FavouriteVideosViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentFavouriteVideosBinding.inflate(inflater,container, false)

        dataList = ArrayList()
       linearLayoutManager = GridLayoutManager(context, 3)
        binding.recylerview.setLayoutManager(linearLayoutManager)
        binding.recylerview.setHasFixedSize(true)
        adapter = VideosListAdapter(context, dataList) { view, pos, `object` ->
            val item = `object` as HomeModel
            openWatchVideo(item.video_id)
        }
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
                    if (viewModel.loadMoreProgressVisibility.get() == false && !viewModel.ispostFinsh) {
                        viewModel.loadMoreProgressVisibility.set(true)
                        viewModel.pageCount.set(viewModel.pageCount.get()+1)
                        userId?.let { viewModel.showFavouriteVideos(it) }

                    }
                }
            }
        })

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        arguments.let {
            userId=it?.getString("userId").toString()
        }

        setObserveAble()

        if (viewModel.isUserAlreadyBlock.equals("1", ignoreCase = true)) {
            viewModel.pageCount.set(0)
            viewModel.showBlockView()
        }
        else {
            if(dataList?.isEmpty() == true) {
                binding!!.shimmerList.shimmerViewContainer.visibility = View.VISIBLE
                binding!!.shimmerList.shimmerViewContainer.startShimmer()
            }
            userId?.let { viewModel.showFavouriteVideos(it) }
        }

    }

    fun setObserveAble(){

        viewModel.videosLiveData.observe(viewLifecycleOwner,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {

                            if (viewModel.pageCount.get() == 0) {
                                dataList!!.clear()
                            }
                            dataList!!.addAll(it)
                            if (viewModel.isScrollToTop) {
                                binding.recylerview.smoothScrollToPosition(0)
                            }
                            adapter!!.notifyDataSetChanged()
                        }

                        changeUi()
                    }

                }

                is ApiResponce.Error ->{


                    if (viewModel.pageCount.get() == 0) {
                        dataList!!.clear()
                        adapter!!.notifyDataSetChanged()
                    }
                    else {
                        viewModel.pageCount.set((viewModel.pageCount.get()-1))
                        if(!it.isRequestError)
                            viewModel.ispostFinsh=true
                    }

                    changeUi()
                }

                else -> {}
            }
        })

    }

    fun changeUi(){
        if (dataList!!.isEmpty()) {
            viewModel.showNoDataView()
        } else {
            viewModel.showDataView()
        }


        binding!!.shimmerList.shimmerViewContainer.visibility = View.GONE
        binding!!.shimmerList.shimmerViewContainer.stopShimmer()
        viewModel.loadMoreProgressVisibility.set(false)

    }



    // open the video in full screen
    private fun openWatchVideo(videoId: String?) {
        val intent = Intent(requireActivity(), WatchVideosActivity::class.java)
        intent.putExtra("video_id", videoId)
        intent.putExtra("position", 0)
        intent.putExtra("pageCount", viewModel.pageCount.get())
        intent.putExtra("userId", userId)
        intent.putExtra("whereFrom", Variables.IdVideo)
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance(userId:String?, isUserAlreadyBlock: String?): FavouriteVideosFragment {
            val fragment = FavouriteVideosFragment()
            val args = Bundle().apply {
                putString("userId",userId)
                putString("isUserAlreadyBlock", isUserAlreadyBlock)
            }
            fragment.arguments = args
            return fragment
        }
    }
}