package com.coheser.app.activitesfragments.profile.followtabs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.adapters.FollowingAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentFollowerUserBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions.checkLoginUser
import com.coheser.app.simpleclasses.Functions.checkProfileOpenValidation
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.FollowersViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class FollowerUserFragment : Fragment {
    var datalist= mutableListOf<UserModel>()
    lateinit var adapter: FollowingAdapter
    lateinit var userId: String
    lateinit var linearLayoutManager: LinearLayoutManager
    var callBack: FragmentCallBack? = null

    lateinit var binding:FragmentFollowerUserBinding

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
        binding=FragmentFollowerUserBinding.inflate(inflater,container, false)


        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        binding.recylerview.layoutManager = linearLayoutManager
        binding.recylerview.setHasFixedSize(true)

        binding.refreshLayout.setOnRefreshListener(OnRefreshListener {
            binding.refreshLayout.setRefreshing(false)
            viewModel.pageCount.set(0)
            binding.shimmerLayout.shimmerViewContainer.setVisibility(View.VISIBLE)
            binding.shimmerLayout.shimmerViewContainer.startShimmer()
            datalist.clear()
            adapter.notifyDataSetChanged()
            userId?.let { viewModel.getFollowersList(it) }
        })

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
        setAdapter()
        setObserveAble()
        if(datalist?.isEmpty() == true) {
                binding.shimmerLayout.shimmerViewContainer.visibility = View.VISIBLE
                binding.shimmerLayout.shimmerViewContainer.startShimmer()
            }
        userId?.let { viewModel.getFollowersList(it) }

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
                            datalist.addAll(it)
                            adapter.notifyDataSetChanged()
                        }

                        changeUi()
                    }

                }

                is ApiResponce.Error ->{

                    if (viewModel.pageCount.get() == 0) {
                        datalist.clear()
                        adapter.notifyDataSetChanged()
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
                                    datalist[index] = userModel
                                    adapter.notifyItemChanged(index, userModel)
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


    fun setAdapter(){
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
                            if (item!!.id != getSharedPreference(context).getString(Variables.U_ID, ""))
                                viewModel.followUser(item.id!!)

                        }

                        R.id.mainlayout -> openProfile(item)

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
                if (userScrolled && scrollOutitems == datalist!!.size - 1) {
                    userScrolled = false
                    if (viewModel.loadMoreProgressVisibility.get() == false && !viewModel.ispostFinsh) {
                        viewModel.loadMoreProgressVisibility.set(true)
                        viewModel.pageCount.set(viewModel.pageCount.get()+1)
                        userId?.let { viewModel.getFollowersList(it) }

                    }
                }
            }
        })
    }


    // this will open the profile of user which have uploaded the currenlty running video
    private fun openProfile(item: UserModel?) {
        var userName: String? = ""
        userName = if (view != null) {
            item!!.username
        }
        else {
            item!!.first_name + " " + item.last_name
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
        @JvmStatic
        fun newInstance(
            userId: String?,
            isFromTab: Boolean,
            callBack: FragmentCallBack?
        )= FollowerUserFragment(callBack).apply {
                arguments = Bundle().apply {
                   putString("userId",userId)
                    putBoolean("isFromTab",isFromTab)
                }
            }
    }

}