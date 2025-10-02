package com.coheser.app.activitesfragments.profile.uservideos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.WatchVideosActivity
import com.coheser.app.activitesfragments.accounts.AccountUtils
import com.coheser.app.activitesfragments.accounts.AccountUtils.saveProfileVideoJson
import com.coheser.app.activitesfragments.profile.creatorplaylist.CreatePlaylistActivity
import com.coheser.app.adapters.MyVideosAdapter
import com.coheser.app.adapters.PlaylistTitleAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentUserVideoBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.HomeModel
import com.coheser.app.models.PlaylistTitleModel
import com.coheser.app.simpleclasses.DataHolder.Companion.instance
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.SpacesItemDecoration
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.UserVideosViewModel
import io.paperdb.Paper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 */
class UserVideoFragment : Fragment() {
    var dataList= mutableListOf<HomeModel>()
    var adapter: MyVideosAdapter? = null
    var linearLayoutManager: GridLayoutManager? = null

    var isUserAlreadyBlock = "0"
    var userId = ""
    var userName = ""
    var blockedByUserName = ""

    var playlistTitleAdapter: PlaylistTitleAdapter? = null
    var playlistList= mutableListOf<PlaylistTitleModel>()


    lateinit var binding: FragmentUserVideoBinding
    private val viewModel: UserVideosViewModel by viewModel()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUserVideoBinding.inflate(layoutInflater, container, false)

        arguments.let {
            userId= it?.getString("userId").toString()
            userName= it?.getString("userName").toString()
            isUserAlreadyBlock= it?.getString("isUserAlreadyBlock").toString()
        }

        linearLayoutManager = GridLayoutManager(requireContext(), 3)
        binding.recylerview.setLayoutManager(linearLayoutManager)
        binding.recylerview.setHasFixedSize(true)
        binding!!.shimmerList.shimmerViewContainer.startShimmer()
        adapter = MyVideosAdapter(
            requireContext(),
            dataList,
            "myProfile"
        ) { view: View?, pos: Int, `object`: Any? -> openWatchVideo(pos) }
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
                        viewModel.getUserVideo(userId)
                    }
                }
            }
        })


        binding.ivClosePlaylist.setOnClickListener(View.OnClickListener {
            binding.tabCreatePlaylist.setVisibility(
                View.GONE
            )
        })
        binding.tabCreatePlaylist.setOnClickListener(View.OnClickListener { view ->
            val intent = Intent(view.context, CreatePlaylistActivity::class.java)
            resultInfoAgainCallback.launch(intent)
        })

        setupPlaylistRecyclerbview()

        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        arguments.let {
            viewModel.isMyProfile= it?.getBoolean("isMyProfile",true)!!

        }
        setObserveAble()

        if (isUserAlreadyBlock.equals("1", ignoreCase = true)) {
            viewModel.pageCount.set(0)
            viewModel.showBlockView(blockedByUserName)
        }
        else {

            val arrayList=AccountUtils.getProfileVideo(requireContext())
            if(arrayList.isNotEmpty()){
                val userModel=arrayList.get(0).userModel
                if(userModel?.id.equals(userId)){
                    dataList?.addAll(arrayList)
                    adapter?.notifyDataSetChanged()
                }
            }

            if(dataList?.isEmpty() == true) {
                binding!!.shimmerList.shimmerViewContainer.visibility = View.VISIBLE
                binding!!.shimmerList.shimmerViewContainer.startShimmer()
            }

            viewModel.getUserVideo(userId)
        }

    }

    fun setObserveAble(){

        viewModel.videosLiveData.observe(viewLifecycleOwner,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {

                            if (viewModel.pageCount.get() == 0) {
                                saveProfileVideoJson(requireContext(), it)
                            }

                            val pinnedVideo = HashMap<String?, HomeModel>()
                            val temp_list = ArrayList<HomeModel>()

                            it.forEach {
                                getSharedPreference(context).edit()
                                    .putString(Variables.other_userName, it.userModel?.username).commit()
                                if (!isUserAlreadyBlock.equals("1", ignoreCase = true)) {
                                    if (Functions.isStringHasValue(it.userModel?.id)) {
                                        if (it.pin == "1") {
                                            pinnedVideo[it.video_id] = it
                                        } else {
                                            temp_list.add(it)
                                        }
                                    }
                                }
                            }
                            Paper.book("PinnedVideo").write("pinnedVideo", pinnedVideo)

                            if (viewModel.pageCount.get() == 0) {
                                dataList!!.clear()
                            }
                            dataList!!.addAll(temp_list)
                            for (key in pinnedVideo.keys) {
                                val itemModel = pinnedVideo[key]
                                itemModel?.let { it1 -> dataList!!.add(0, it1) }
                            }
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
                    else{

                        viewModel.pageCount.set(viewModel.pageCount.get()-1)
                        if(!it.isRequestError){
                            viewModel.ispostFinsh=true
                        }
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
                delay(500)
                if(dataList?.isEmpty() == true || viewModel.pageCount.get()==0) {
                        viewModel.isScrollToTop = true
                        viewModel.getUserVideo(userId)

                }
            }
        }
    }


    private fun setupPlaylistRecyclerbview() {
        val layoutManager = LinearLayoutManager(binding.root.context)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        binding.playlistRecyclerview.setLayoutManager(layoutManager)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen._6sdp)
        binding.playlistRecyclerview.addItemDecoration(SpacesItemDecoration(spacingInPixels))
        playlistTitleAdapter = PlaylistTitleAdapter(playlistList,
            AdapterClickListener { view, pos, `object` ->
                val itemUpdate = playlistList[pos]
                if (itemUpdate.id == "0") {
                    val intent = Intent(view.context, CreatePlaylistActivity::class.java)
                    resultInfoAgainCallback.launch(intent)
                } else {
                    openPlaylistVideo(itemUpdate.id, itemUpdate.name)
                }
            })
        binding.playlistRecyclerview.setAdapter(playlistTitleAdapter)
    }

    var callBackForDetailRefresh: FragmentCallBack? = null
    fun updateUserPlaylist(
        playlistArray: JSONArray,
        verifiedId: String,
        callBackForDetailRefresh: FragmentCallBack?
    ) {
        this.callBackForDetailRefresh = callBackForDetailRefresh
        try {
            playlistList.clear()

            if (playlistArray.length() > 0) {
                if (userId == getSharedPreference(context).getString(Variables.U_ID, "")) {
                    val model = PlaylistTitleModel()
                    model.id = "0"
                    model.name = ""
                    playlistList.add(model)
                }
            }

            for (i in 0 until playlistArray.length()) {
                val `object` = playlistArray.getJSONObject(i).optJSONObject("Playlist")
                val model = PlaylistTitleModel()
                model.id = `object`.optString("id")
                model.name = `object`.optString("name")
                playlistList.add(model)
            }
            playlistTitleAdapter!!.notifyDataSetChanged()

            if (playlistList.size > 0) {
                binding.tabCreatePlaylist.setVisibility(View.GONE)
                binding.playlistRecyclerview.setVisibility(View.VISIBLE)
            } else {
                if (userId == getSharedPreference(context).getString(Variables.U_ID, "")) {
                    if (verifiedId == "1") {
                        binding.tabCreatePlaylist.setVisibility(View.VISIBLE)
                    }
                }
                binding.playlistRecyclerview.setVisibility(View.GONE)
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }
    }


    var resultInfoAgainCallback: ActivityResultLauncher<Intent> =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
                override fun onActivityResult(result: ActivityResult?) {
                    if (result?.resultCode == Activity.RESULT_OK) {
                        val data = result.data
                        if (data!!.getBooleanExtra("isShow", false)) {
                            if (callBackForDetailRefresh != null) {
                                val bundle = Bundle()
                                bundle.putBoolean("isShow", data.getBooleanExtra("isShow", false))
                                callBackForDetailRefresh!!.onResponce(bundle)
                            }
                        }
                    }
                }
            })


    fun updatePlaylistCreate() {
        if(::binding.isInitialized) {
            if (userId == getSharedPreference(context).getString(Variables.U_ID, "") && playlistList.size==0)
            {
                binding.tabCreatePlaylist.setVisibility(View.VISIBLE)
            }
            else {
                binding.tabCreatePlaylist.setVisibility(View.GONE)
            }
        }
    }


    // open the videos in full screen on click
    private fun openPlaylistVideo(id: String, playlistName: String) {
        val intent = Intent(activity, WatchVideosActivity::class.java)
        intent.putExtra("playlist_id", id)
        intent.putExtra("position", 0)
        intent.putExtra("pageCount", viewModel.pageCount.get())
        intent.putExtra("userId", userId)
        intent.putExtra("playlistName", playlistName)
        intent.putExtra("whereFrom", Variables.playlistVideo)
        resultInfoAgainCallback.launch(intent)
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
        intent.putExtra("whereFrom", Variables.userVideo)
        resultCallback.launch(intent)
    }

    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    if (data!!.getBooleanExtra("isShow", false)) {
                        if (Paper.book("pinnedRefresh").contains("refresh")) {
                            Paper.book("pinnedRefresh").destroy()
                            viewModel.pageCount.set(0)
                            viewModel.isScrollToTop=true
                            viewModel.getUserVideo(userId)
                        } else {
                            val bundle = instance!!.data
                            if (bundle != null) {
                                dataList!!.clear()
                                val list=bundle.getSerializable("arraylist") as ArrayList<HomeModel>
                                dataList!!.addAll(list)
                            }
                            viewModel.pageCount.set(data.getIntExtra("pageCount", 0))
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                }
            }
        })

    fun refreshData() {
        viewModel.isScrollToTop=true
        viewModel.getUserVideo(userId)
    }

    companion object {

        fun newInstance(isMyProfile: Boolean,
                        userId: String,
                        userName: String,
                        isUserAlreadyBlock: String)=
            UserVideoFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isMyProfile",isMyProfile)
                    putString("userId",userId)
                    putString("userName",userName)
                    putString("isUserAlreadyBlock",isUserAlreadyBlock)
                }
            }

    }
}
