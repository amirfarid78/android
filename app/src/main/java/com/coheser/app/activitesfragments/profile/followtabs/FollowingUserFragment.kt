package com.coheser.app.activitesfragments.profile.followtabs

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.adapters.FollowingAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentFollowingUserBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions.checkLoginUser
import com.coheser.app.simpleclasses.Functions.checkProfileOpenValidation
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.hideSoftKeyboard
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.FollowersViewModel
import java.util.Timer
import java.util.TimerTask
import org.koin.androidx.viewmodel.ext.android.viewModel

class FollowingUserFragment : Fragment {
    var datalist= mutableListOf<UserModel>()
    lateinit var adapter: FollowingAdapter
    lateinit var userId: String

    private var timer = Timer()
    private val DELAY: Long = 1000 // Milliseconds

    lateinit var linearLayoutManager: LinearLayoutManager

    var callBack: FragmentCallBack? = null

    lateinit var binding:FragmentFollowingUserBinding
    private val viewModel: FollowersViewModel by viewModel()

    constructor()
    constructor(callBack: FragmentCallBack?) {

        this.callBack = callBack
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentFollowingUserBinding.inflate(inflater, container, false)

        datalist = ArrayList()


        binding.searchEdit.addTextChangedListener(
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
                                    activity!!.runOnUiThread(Runnable {
                                        val search_txt = binding.searchEdit.getText().toString()
                                        viewModel.pageCount.set(0)
                                        if (search_txt.length > 0) {
                                            binding.shimmerLayout.shimmerViewContainer.setVisibility(View.VISIBLE)
                                            binding.shimmerLayout.shimmerViewContainer.startShimmer()
                                            datalist!!.clear()
                                            adapter!!.notifyDataSetChanged()
                                            userId?.let { viewModel.getFollowingSearch(it,binding.searchEdit.text.toString()) }

                                        } else {
                                            binding.noDataLayout.visibility = View.GONE
                                            binding.shimmerLayout.shimmerViewContainer.setVisibility(View.VISIBLE)
                                            binding.shimmerLayout.shimmerViewContainer.startShimmer()
                                            datalist!!.clear()
                                            adapter!!.notifyDataSetChanged()

                                            userId?.let { viewModel.getFollowingList(it) }
                                        }
                                    })
                                }
                            }
                        },
                        DELAY
                    )
                }
            }
        )

        binding.refreshLayout.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                binding.refreshLayout.setRefreshing(false)
                viewModel.pageCount.set(0)
                binding.shimmerLayout.shimmerViewContainer.setVisibility(View.VISIBLE)
                binding.shimmerLayout.shimmerViewContainer.startShimmer()
                datalist.clear()
                adapter.notifyDataSetChanged()
                if (binding.searchEdit.getText().toString().length > 0) {
                    userId?.let { viewModel.getFollowingSearch(it,binding.searchEdit.text.toString()) }
                } else {
                    userId?.let { viewModel.getFollowingList(it) }
                }
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.recylerview.setOnScrollChangeListener(object : View.OnScrollChangeListener {
                override fun onScrollChange(view: View, i: Int, i1: Int, i2: Int, i3: Int) {
                    Log.d(Constants.tag, "recyclerView : $i")
                }
            })
        }
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        arguments.let {
            userId=it?.getString("userId")!!
            viewModel.isFromTab= it?.getBoolean("isFromTab",false)!!

        }

        viewModel.isMyProfile= userId.equals(
            getSharedPreference(context).getString(Variables.U_ID, ""),
            ignoreCase = true
        )

        if (viewModel.isMyProfile) {
            binding.searchLayout.setVisibility(View.VISIBLE)
            hideSoftKeyboard(activity)
        } else {
            binding.searchLayout.setVisibility(View.GONE)
        }

        setAdapter()
        setObserveAble()
        if(datalist?.isEmpty() == true) {
            binding.shimmerLayout.shimmerViewContainer.visibility = View.VISIBLE
            binding.shimmerLayout.shimmerViewContainer.startShimmer()
        }
        userId?.let { viewModel.getFollowingList(it) }

    }


    fun setAdapter(){
        linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager!!.orientation = RecyclerView.VERTICAL
        binding.recylerview.layoutManager = linearLayoutManager
        binding.recylerview.setHasFixedSize(true)

        adapter = FollowingAdapter(
            requireContext(),
            datalist,
            object :AdapterClickListener{
                override fun onItemClick(view: View?, pos: Int, `object`: Any?) {
                    val item=`object` as UserModel
                    when (view!!.id) {
                        R.id.action_txt -> if (checkLoginUser(
                                activity
                            )
                        ) {
                            if (item!!.id != getSharedPreference(context).getString(Variables.U_ID, "")) {
                                item.id?.let { viewModel.followUser(it) }
                            }
                        }

                        R.id.mainlayout -> openProfile(item)
                        R.id.ivCross -> selectNotificationPriority(pos)
                    }
                }
            }
        )

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
                printLog("resp", "" + scrollOutitems)
                if (userScrolled && (scrollOutitems == datalist!!.size - 1)) {
                    userScrolled = false
                    if (viewModel.loadMoreProgressVisibility.get() == false && !viewModel.ispostFinsh) {
                        viewModel.loadMoreProgressVisibility.set(true)
                        viewModel.pageCount.set(viewModel.pageCount.get()+1)
                        if (binding.searchEdit.getText().toString().length > 0) {
                            userId?.let { viewModel.getFollowingSearch(it,binding.searchEdit.text.toString()) }
                        } else {
                            userId?.let { viewModel.getFollowingList(it) }
                        }
                    }
                }
            }
        })
    }

    fun setObserveAble(){

        viewModel.listLiveData.observe(viewLifecycleOwner,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {

                            if (viewModel.pageCount.get() == 0) {
                                datalist!!.clear()
                            }
                            datalist!!.addAll(it)
                            adapter!!.notifyDataSetChanged()
                        }

                        changeUi()
                    }

                }

                is ApiResponce.Error ->{

                    if (viewModel.pageCount.get() == 0) {
                        datalist!!.clear()
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


        viewModel.followLiveData.observe(viewLifecycleOwner,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let { userModel->
                        if (userModel != null) {
                            for ((index,item) in datalist!!.withIndex()) {
                                if(item.id.equals(userModel.id)){

                                    datalist!![index] = userModel
                                    adapter!!.notifyItemChanged(index, userModel)
                                    break
                                }
                            }
                        }
                    }

                }
                else -> {}
            }
        })


    }

    fun changeUi(){
        if (datalist!!.isEmpty()) {
            viewModel.showNoDataView()
        } else {
            viewModel.showDataView()
        }

        binding!!.shimmerLayout.shimmerViewContainer.visibility = View.GONE
        binding!!.shimmerLayout.shimmerViewContainer.stopShimmer()
        viewModel.loadMoreProgressVisibility.set(false)

    }


    private fun selectNotificationPriority(position: Int) {
        var isFriend = false
        if (datalist!![position].button.equals("Follow", ignoreCase = true)) {
            isFriend = false
        } else {
            isFriend = true
        }
        val f = NotificationPriorityFragment(
            datalist!![position].notification, isFriend,
            datalist!![position].username, datalist!![position].id, object : FragmentCallBack {
                override fun onResponce(bundle: Bundle) {
                    if (bundle.getBoolean("isShow", false)) {
                        val itemUpdate = datalist!![position]
                        itemUpdate.notification = bundle.getString("type")
                        datalist!![position] = itemUpdate
                        adapter!!.notifyDataSetChanged()
                    } else {
                        val itemUpdte = datalist!![position]
                        if (itemUpdte.button.equals("Follow", ignoreCase = true)) {
                            itemUpdte.button = "Following"
                        } else {
                            itemUpdte.button = "Follow"
                        }
                        datalist!![position] = itemUpdte
                        adapter!!.notifyDataSetChanged()
                    }
                }
            })
        f.show(childFragmentManager, "")
    }

    // this will open the profile of user which have uploaded the currenlty running video
    private fun openProfile(item: UserModel?) {
        var userName: String? = ""
        if (view != null) {
            userName = item!!.username
        } else {
            userName = item!!.first_name + " " + item.last_name
        }
         if (checkProfileOpenValidation(item.id)) {
            val intent = Intent(activity, ProfileActivity::class.java)
            intent.putExtra("user_id", item.id)
            intent.putExtra("user_name", userName)
            intent.putExtra("user_pic", item.getProfilePic())
            intent.putExtra("userModel", item)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        }
    }

    
    companion object {
        fun newInstance(
            userId: String?,
            isFromTab: Boolean,
            callBack: FragmentCallBack?
        )= FollowingUserFragment(callBack).apply {
            arguments = Bundle().apply {
                putString("userId",userId)
                putBoolean("isFromTab",isFromTab)
            }
        }

    }

}