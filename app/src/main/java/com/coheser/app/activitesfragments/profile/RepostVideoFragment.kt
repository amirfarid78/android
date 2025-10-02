package com.coheser.app.activitesfragments.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.coheser.app.activitesfragments.WatchVideosActivity
import com.coheser.app.adapters.MyVideosAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentRepostVideoBinding
import com.coheser.app.models.HomeModel
import com.coheser.app.simpleclasses.DataHolder.Companion.instance
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.RepostVideosViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RepostVideoFragment : Fragment {
    var dataList= mutableListOf<HomeModel>()
    var adapter: MyVideosAdapter? = null

    var linearLayoutManager: GridLayoutManager? = null

    var userId = ""

    lateinit var binding:FragmentRepostVideoBinding
    private val viewModel: RepostVideosViewModel by viewModel()
   constructor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentRepostVideoBinding.inflate(inflater,container, false)


        linearLayoutManager = GridLayoutManager(context, 3)
        binding.recylerview.setLayoutManager(linearLayoutManager)
        binding.recylerview.setHasFixedSize(true)
        adapter = MyVideosAdapter(
            requireContext(),
            dataList,
            "myProfile"
        ) { view: View?, pos: Int, `object`: Any? ->
            val item = `object` as HomeModel?
            openWatchVideo(pos)
        }
        (binding.recylerview.getItemAnimator() as SimpleItemAnimator?)!!.supportsChangeAnimations = false
        binding.recylerview.setAdapter(adapter)
        binding.recylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                if (scrollInItem == 0) {
                    recyclerView.isNestedScrollingEnabled = true
                } else {
                    recyclerView.isNestedScrollingEnabled = false
                }
                if (userScrolled && scrollOutitems == dataList!!.size - 1) {
                    userScrolled = false
                    if (viewModel.loadMoreProgressVisibility.get() == false && !viewModel.ispostFinsh) {
                        viewModel.loadMoreProgressVisibility.set(true)
                        viewModel.pageCount.set(viewModel.pageCount.get() + 1)
                        viewModel.isScrollToTop=false
                        viewModel.getRepostVideo(userId)
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
            userId= it?.getString("userId").toString()
            viewModel.userName= it?.getString("userName").toString()
            viewModel.isUserAlreadyBlock= it?.getString("isUserAlreadyBlock").toString()
            viewModel.isMyProfile= it?.getBoolean("isMyProfile",true)!!

        }
        setObserveAble()

        callApi()

    }

    fun callApi(){
        if (viewModel.isUserAlreadyBlock != null && viewModel.isUserAlreadyBlock.equals("1", ignoreCase = true)) {
            viewModel.pageCount.set(0)
            viewModel.userName?.let { viewModel.showBlockView(it) }

        }
        else {
            if(dataList?.isEmpty() == true) {
                binding!!.shimmerList.shimmerViewContainer.visibility = View.VISIBLE
                binding!!.shimmerList.shimmerViewContainer.startShimmer()
            }
            userId?.let { viewModel.getRepostVideo(it) }
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


//    private fun setNoData() {
//        if (viewModel.isMyProfile) {
//            binding.tvMessageNoData!!.text = getString(R.string.you_has_not_repost_any_video)
//        } else {
//            binding.tvMessageNoData!!.text =
//               getString(R.string.this_user_has_not_repost_any_video)
//        }
//    }



    override fun setMenuVisibility(visible: Boolean) {
        super.setMenuVisibility(visible)
        if (visible) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(200)
                viewModel.pageCount.set(0)
                viewModel.isScrollToTop=true
                viewModel.getRepostVideo(userId)
            }

        }
    }

    // open the videos in full screen on click
    private fun openWatchVideo(postion: Int) {
        val intent = Intent(activity, WatchVideosActivity::class.java)
        val args = Bundle()
        args.putSerializable("arraylist", ArrayList(dataList))
        instance!!.data = args
        intent.putExtra("position", postion)
        intent.putExtra("pageCount", viewModel.pageCount.get())
        intent.putExtra("userId", userId)
        intent.putExtra("whereFrom", Variables.repostVideo)
        resultCallback.launch(intent)
    }

    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    if (data!!.getBooleanExtra("isShow", false)) {
                        val bundle = instance!!.data
                        if (bundle != null) {
                            dataList!!.clear()
                            dataList!!.addAll((bundle.getSerializable("arraylist") as ArrayList<HomeModel>?)!!)
                        }
                        viewModel.pageCount.set(data.getIntExtra("pageCount", 0))
                        adapter!!.notifyDataSetChanged()
                    }
                }
            }
        })


    companion object {

        fun newInstance(isMyProfile: Boolean,
                        userId: String,
                        userName: String,
                        isUserAlreadyBlock: String)=
            RepostVideoFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isMyProfile",isMyProfile)
                    putString("userId",userId)
                    putString("userName",userName)
                    putString("isUserAlreadyBlock",isUserAlreadyBlock)
                }
            }

    }


}
