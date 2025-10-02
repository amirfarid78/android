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
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.activitesfragments.profile.SearchAllUserActivity
import com.coheser.app.adapters.FollowingAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentSuggestionsBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.checkLoginUser
import com.coheser.app.simpleclasses.Functions.checkProfileOpenValidation
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.FollowersViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SuggestionsFragment : Fragment, View.OnClickListener {
    var datalist= mutableListOf<UserModel>()
    lateinit var adapter: FollowingAdapter

    lateinit var userId: String
    lateinit var linearLayoutManager: LinearLayoutManager

    lateinit var binding: FragmentSuggestionsBinding
    var callBack: FragmentCallBack? = null

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
        binding = FragmentSuggestionsBinding.inflate(inflater, container, false)


        return binding!!.root
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

        if (viewModel.isFromTab) {
            binding.searchLayout.setVisibility(View.VISIBLE)
            binding.searchLayout.setOnClickListener(this)
            Functions.hideSoftKeyboard(activity)
        } else {
            binding.searchLayout.setVisibility(View.GONE)
        }

        setUpSuggesionAdapter()
        setObserveAble()
        if(datalist?.isEmpty() == true) {
            binding.shimmerLayout.shimmerViewContainer.visibility = View.VISIBLE
            binding.shimmerLayout.shimmerViewContainer.startShimmer()
        }
        userId?.let { viewModel.getSuggesstionList(it) }

    }



    fun setObserveAble(){

        viewModel.listLiveData.observe(viewLifecycleOwner,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {

                            if (viewModel.pageCount.get() == 0) {
                                datalist.clear()
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

        binding.shimmerLayout.shimmerViewContainer.visibility = View.GONE
        binding.shimmerLayout.shimmerViewContainer.stopShimmer()
        viewModel.loadMoreProgressVisibility.set(false)

    }


    private fun setUpSuggesionAdapter() {
        datalist = ArrayList()
        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager!!.orientation = RecyclerView.VERTICAL
        binding.recylerview.layoutManager = linearLayoutManager
        binding.recylerview.setHasFixedSize(true)
        adapter = FollowingAdapter(
            requireContext(),
            datalist!!,
            object : AdapterClickListener {
                override fun onItemClick(view: View?, pos: Int, `object`: Any?) {
                    val item=`object` as UserModel
                    when (view!!.id) {
                        R.id.action_txt -> if (checkLoginUser(
                                activity
                            )
                        ) {
                            if (item!!.id != getSharedPreference(activity).getString(Variables.U_ID, "")) {
                                item!!.id?.let { viewModel.followUser(it) }

                            }
                        }

                        R.id.mainlayout -> openProfile(item)
                        R.id.ivCross -> {
                            datalist!!.removeAt(pos)
                            adapter!!.notifyItemRemoved(pos)
                        }
                    }
                }
            }
        )
        binding!!.recylerview.adapter = adapter
        binding!!.recylerview.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
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
                        userId?.let { viewModel.getSuggesstionList(it) }

                    }
                }
            }
        })
    }


    fun openProfile(item: UserModel?) {
         if (checkProfileOpenValidation(item?.id)) {
            val intent = Intent(activity, ProfileActivity::class.java)
            intent.putExtra("user_id", item?.id)
            intent.putExtra("user_name", item?.username)
            intent.putExtra("user_pic", item?.getProfilePic())
            intent.putExtra("userModel", item)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        }
    }


    fun openSearch() {
        val intent = Intent(context, SearchAllUserActivity::class.java)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.search_layout -> openSearch()

        }
    }

    companion object {
        fun newInstance(userId:String,isFromTab:Boolean,callBack: FragmentCallBack?)
        = SuggestionsFragment(callBack).apply {
            arguments = Bundle().apply {
                putString("userId",userId)
                putBoolean("isFromTab",isFromTab)
            }
        }
    }
}