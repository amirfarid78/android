package com.coheser.app.activitesfragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.adapters.MyVideosAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityTagedVideosBinding
import com.coheser.app.models.HomeModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.DataHolder
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.TaggedVideoViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TagedVideosActivity : AppCompatLocaleActivity(), View.OnClickListener {

    lateinit var myContext: Context
    var dataList: ArrayList<HomeModel> = ArrayList()
    lateinit var  adapter: MyVideosAdapter
    lateinit var linearLayoutManager: GridLayoutManager
    lateinit var binding: ActivityTagedVideosBinding
    private val viewModel: TaggedVideoViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Functions.setLocale(
            Functions.getSharedPreference(this@TagedVideosActivity)
                .getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this,
            javaClass,
            false
        )

        binding = DataBindingUtil.setContentView(this, R.layout.activity_taged_videos)

        myContext = this@TagedVideosActivity

       binding.viewModel=viewModel
        binding.lifecycleOwner = this


        binding.favLayout.setOnClickListener(this)
        linearLayoutManager = GridLayoutManager(myContext, 3)
        binding.recylerview.layoutManager = linearLayoutManager
        binding.recylerview.setHasFixedSize(true)

        adapter = MyVideosAdapter(myContext, dataList, "tagged") { view, pos, `object` -> openWatchVideo(pos) }
        binding.recylerview.adapter = adapter
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
                Functions.printLog("resp", "" + scrollOutitems)
                if (userScrolled && scrollOutitems == dataList!!.size - 1) {
                    userScrolled = false
                    if (binding.loadMoreProgress.visibility != View.VISIBLE && !viewModel.ispostFinsh) {
                        binding.loadMoreProgress.visibility = View.VISIBLE
                        viewModel.pageCount.set(viewModel.pageCount.get()+1)
                        viewModel.getTaggedVideo()
                    }
                }
            }
        })
        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            viewModel.pageCount.set(0)
            viewModel.getTaggedVideo()
        }

        binding.backBtn.setOnClickListener { finish() }

        setObserver()
        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        viewModel.tagTxt = intent.getStringExtra("tag")!!
        binding.tagTxtView.text = "#${viewModel.tagTxt}"
        binding.tagTitleTxt.text = viewModel.tagTxt

        binding.shimmerList.shimmerViewContainer.visibility=View.VISIBLE
        binding.shimmerList.shimmerViewContainer.startShimmer()
        viewModel.pageCount.set(0)
        viewModel.getTaggedVideo()

    }

    fun setObserver(){
        viewModel.videosLiveData.observe(this,{
            when(it){
                is ApiResponce.Loading ->{}
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {
                            if(viewModel.pageCount.get()==0){
                                dataList.clear()
                            }
                            dataList.addAll(it)
                            adapter.notifyDataSetChanged()
                        }
                        changeUi()
                    }
                }
                is ApiResponce.Error ->{
                    changeUi()
                }
            }
        })

        viewModel.hashtagModelLiveData.observe(this,{

            when(it){

                is ApiResponce.Success ->{
                    it.data?.let {
                        viewModel.tagId=it.id
                        viewModel.favourite=it.fav
                        setFavUi()
                        binding!!.videoCountTxt.text = it.videos_count+ " " + getString(R.string.videos)

                    }
                }

                is ApiResponce.Error ->{
                }

                else -> {}

            }
        })

        viewModel.favoriteLiveData.observe(this,{

        })

    }


    fun changeUi(){
        if (dataList!!.isEmpty()) {
            binding.noDataLayout.visibility = View.VISIBLE
        } else {
            binding.noDataLayout.visibility = View.GONE
        }
        binding.shimmerList.shimmerViewContainer.visibility = View.GONE
        binding.loadMoreProgress.visibility = View.GONE

    }


    fun setFavUi(){
        if (viewModel.favourite.equals("1", ignoreCase = true)) {
            binding!!.favBtn.setImageDrawable(
                ContextCompat.getDrawable(
                    myContext!!, R.drawable.ic_fav_fill
                )
            )
            binding!!.favTxt.text = getString(R.string.added_to_favourite)
        }
        else {
            binding!!.favBtn.setImageDrawable(
                ContextCompat.getDrawable(
                    myContext!!, R.drawable.ic_fav
                )
            )
            binding!!.favTxt.text = getString(R.string.add_to_favourite)
        }
    }


    private fun openWatchVideo(postion: Int) {
        val intent = Intent(this@TagedVideosActivity, WatchVideosActivity::class.java)

        val args = Bundle()
        args.putSerializable("arraylist", dataList)
        DataHolder.instance?.data = args

        intent.putExtra("position", postion)
        intent.putExtra("pageCount", viewModel.pageCount.get())
        intent.putExtra("hashtag", viewModel.tagTxt)
        intent.putExtra("userId", Functions.getSharedPreference(this@TagedVideosActivity).getString(Variables.U_ID, ""))
        intent.putExtra("whereFrom", Variables.tagedVideo)
        try {
            resultCallback.launch(intent)
        }catch (e:Exception){
            startActivity(intent)
        }

    }

    var resultCallback = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
                val bundle = DataHolder.instance?.data
                if (bundle != null) {
                    val arrayList = bundle.getSerializable("arraylist") as ArrayList<HomeModel>?
                    dataList.clear()
                    dataList.addAll(arrayList!!)
                }
                viewModel.pageCount.set(data.getIntExtra("pageCount", 0))
                adapter.notifyDataSetChanged()
            }
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.fav_layout ->
                if (Functions.checkLoginUser(this@TagedVideosActivity)) {
                if (viewModel.favourite != null && viewModel.favourite.equals("1", ignoreCase = true)) {
                    viewModel.favourite = "0"
                } else {
                    viewModel.favourite = "1"
                }
                 setFavUi()
                 viewModel.addHashtagFavourite()
            }
        }
    }
}
