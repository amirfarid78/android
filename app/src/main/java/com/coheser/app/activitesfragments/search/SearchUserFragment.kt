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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.adapters.UsersAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentSearchBinding
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions.checkProfileOpenValidation
import com.coheser.app.simpleclasses.Functions.hideSoftKeyboard
import com.coheser.app.simpleclasses.Functions.isStringHasValue
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.viewModels.MainSearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 */
class SearchUserFragment : Fragment {

    var type: String? = null
    var linearLayoutManager: LinearLayoutManager? = null
    var pageCount = 0
    var ispostFinsh = false
    var dataList= mutableListOf<UserModel>()
    var usersAdapter: UsersAdapter? = null
    var binding:FragmentSearchBinding?=null

    private val viewModel : MainSearchViewModel by viewModel()
   var position = 0
    constructor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_search, container, false)

       binding?.lifecycleOwner = this

        type = if (arguments != null && isStringHasValue(arguments?.getString("type")
            )
        ) {
            arguments?.getString("type")
        } else {
            "user"
        }


        linearLayoutManager = LinearLayoutManager(context)
        binding!!.recylerview.setLayoutManager(linearLayoutManager)
        dataList = ArrayList()
        usersAdapter = UsersAdapter(requireContext(), dataList) { view, pos, `object` ->
            val item = `object` as UserModel
            if (view.id == R.id.tvFollowBtn || view.id == R.id.unFriendBtn) {
                viewModel.followUser(item.id!!)
                position = pos
            } else {
                hideSoftKeyboard(activity)
                openProfile(item)
            }
        }
        (binding!!.recylerview.getItemAnimator() as SimpleItemAnimator?)!!.supportsChangeAnimations = false
        binding!!.recylerview.setAdapter(usersAdapter)
        binding!!.recylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                    if (binding!!.loadMoreProgress!!.visibility != View.VISIBLE && !ispostFinsh) {
                        binding!!.loadMoreProgress!!.visibility = View.VISIBLE
                        pageCount = pageCount + 1
                        viewModel.getSearchData(
                            pageCount,
                            SearchMainActivity.searchEdit.text.toString(),
                            type!!
                        )
                    }
                }
            }
        })
        pageCount = 0

        viewModel.getSearchData(pageCount, SearchMainActivity.searchEdit.text.toString(),type!!)
        initObserver()
        return binding!!.root
    }
    fun initObserver(){
        viewModel.userLiveData.observe(requireActivity()){response->
            when(response){
                is ApiResponce.Success ->{
                    response.data?.let { list ->
                        dataList.addAll(list)
                        usersAdapter?.updateData(dataList)
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

        viewModel.followLiveData.observe(requireActivity()){response ->
            when(response){
                is ApiResponce.Success ->{
                    response.data?.let { model ->
                        dataList.set(position, model)
                        usersAdapter!!.notifyItemChanged(position, model)
                    }
                }
                else ->{}
            }
        }
    }

    fun openProfile(item:UserModel) {

            if (checkProfileOpenValidation(item.id)) {
                val intent = Intent(binding!!.root.context, ProfileActivity::class.java)
                intent.putExtra("user_id", item.id)
                intent.putExtra("user_name", item.username)
                intent.putExtra("user_pic", item.getProfilePic())
                intent.putExtra("userModel", item)
                startActivity(intent)
               requireActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
            }

    }


    companion object {
        @JvmStatic
        fun newInstance(type: String?): SearchUserFragment {
            val fragment = SearchUserFragment()
            val args = Bundle()
            args.putString("type",type)
            fragment.arguments = args
            return fragment
        }
    }
}
