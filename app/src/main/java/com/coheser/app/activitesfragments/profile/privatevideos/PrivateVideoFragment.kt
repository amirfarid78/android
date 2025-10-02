package com.coheser.app.activitesfragments.profile.privatevideos

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
import com.coheser.app.activitesfragments.WatchVideosActivity
import com.coheser.app.adapters.MyVideosAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentPrivateVideoBinding
import com.coheser.app.models.HomeModel
import com.coheser.app.simpleclasses.DataHolder.Companion.instance
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.PrivateVideosViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 */
class PrivateVideoFragment : Fragment() {
    var dataList= mutableListOf<HomeModel>()
    var adapter: MyVideosAdapter? = null
    var linearLayoutManager: GridLayoutManager? = null
    lateinit var binding: FragmentPrivateVideoBinding
    private val viewModel: PrivateVideosViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPrivateVideoBinding.inflate(layoutInflater, container, false)

        linearLayoutManager = GridLayoutManager(context, 3)
        binding.recylerview.setLayoutManager(linearLayoutManager)
        binding.recylerview.setHasFixedSize(true)
        adapter = MyVideosAdapter(requireContext(), dataList, "private") { view, pos, `object` ->
            val item = `object` as HomeModel
            openWatchVideo(pos)
        }
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
                        viewModel.getUserVideo()
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


        setObserveAble()

        binding!!.shimmerList.shimmerViewContainer.visibility = View.VISIBLE
        binding!!.shimmerList.shimmerViewContainer.startShimmer()

        viewModel.getUserVideo()

    }

    fun setObserveAble(){

        viewModel.videosLiveData.observe(viewLifecycleOwner,{
            when(it){
                is ApiResponce.Success ->{
                    viewModel.isApiRun=false
                    it.data?.let {
                        if (it != null) {

                             if (viewModel.pageCount.get() == 0) {
                                dataList!!.clear()
                            }
                            dataList!!.addAll(it)
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                    changeUi()
                }

                is ApiResponce.Error ->{
                    viewModel.isApiRun=false
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

    override fun setMenuVisibility(visible: Boolean) {
        super.setMenuVisibility(visible)
        if (visible) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(200)
                viewModel.pageCount.set(0)
                viewModel.getUserVideo()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    // open the video in full screen
    private fun openWatchVideo(postion: Int) {
        val intent = Intent(activity, WatchVideosActivity::class.java)
        val args = Bundle()
        args.putSerializable("arraylist", ArrayList(dataList))
        instance!!.data = args
        intent.putExtra("position", postion)
        intent.putExtra("pageCount", viewModel.pageCount.get())
        intent.putExtra("userId", getSharedPreference(context).getString(Variables.U_ID, ""))
        intent.putExtra("whereFrom", Variables.privateVideo)
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

                        viewModel.pageCount.set( data.getIntExtra("pageCount", 0))
                        adapter!!.notifyDataSetChanged()
                    }
                }
            }
        })

    companion object {
        fun newInstance(): PrivateVideoFragment {
            val fragment = PrivateVideoFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
