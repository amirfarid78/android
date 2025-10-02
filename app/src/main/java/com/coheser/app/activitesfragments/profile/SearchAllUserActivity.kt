package com.coheser.app.activitesfragments.profile

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AbsListView
import android.widget.TextView.OnEditorActionListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.adapters.RecentSearchAdapter
import com.coheser.app.adapters.UsersAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivitySearchAllUserBinding
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Functions.checkProfileOpenValidation
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.hideSoftKeyboard
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.SearchAllUsersViewModel
import io.paperdb.Paper
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import java.util.Locale
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchAllUserActivity : AppCompatLocaleActivity(), View.OnClickListener {

    var linearLayoutManager: LinearLayoutManager? = null
    var dataList= mutableListOf<UserModel>()
    var usersAdapter: UsersAdapter? = null
    var recentsearchAdapter: RecentSearchAdapter? = null
    var searchQueryList = mutableListOf<String>()
    lateinit var binding:ActivitySearchAllUserBinding

    private val viewModel: SearchAllUsersViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@SearchAllUserActivity).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding=DataBindingUtil.setContentView(this,R.layout.activity_search_all_user)

        binding.viewModel=viewModel
        binding.lifecycleOwner = this

        InitControl()
        setObserveAble()
    }




    private fun InitControl() {
        binding.ivBack.setOnClickListener(this)

        binding.searchBtn.setOnClickListener(this)
        showRecentSearch()
        binding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (binding.searchEdit.getText().toString().length > 0) {
                    binding.searchBtn.setVisibility(View.VISIBLE)
                } else {
                    binding.searchBtn.setVisibility(View.GONE)
                }
                showRecentSearch()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding.searchEdit.setFocusable(true)
        UIUtil.showKeyboard(this@SearchAllUserActivity, binding.searchEdit)
        binding.searchEdit.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.pageCount.set(0)
                viewModel.searchUsersList(binding.searchEdit.text.toString())
                binding.recentLayout.visibility = View.GONE
                addSearchKey(binding.searchEdit.text.toString())
                return@OnEditorActionListener true
            }
            false
        })
        binding.clearAllTxt.setOnClickListener(this)
        setSearchUserAdapter()
    }



    fun setObserveAble(){

        viewModel.listLiveData.observe(this,{
            when(it){

                is ApiResponce.Loading ->{

                    viewModel.dataLayoutVisibility.set(true)
                    viewModel.noDataLayoutVisibility.set(false)

                    if(viewModel.pageCount.get()==0){

                        dataList.clear()
                        usersAdapter?.notifyDataSetChanged()

                        binding.shimmerLayout.shimmerViewContainer.visibility = View.VISIBLE
                        binding.shimmerLayout.shimmerViewContainer.startShimmer()
                    }
                }

                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {

                            if (viewModel.pageCount.get() == 0) {
                                dataList!!.clear()
                            }
                            dataList!!.addAll(it)
                            usersAdapter!!.notifyDataSetChanged()
                        }
                        changeUi()
                    }

                }

                is ApiResponce.Error ->{

                    if (viewModel.pageCount.get() == 0) {
                        dataList!!.clear()
                        usersAdapter!!.notifyDataSetChanged()
                    }
                    else{

                        viewModel.pageCount.set(viewModel.pageCount.get()-1)
                        if(!it.isRequestError){
                            viewModel.ispostFinsh=true
                        }
                    }


                    changeUi()
                }

            }
        })

    }

    fun changeUi(){
        if (dataList.isEmpty()) {
            viewModel.showNoDataView()
        } else {
            viewModel.showDataView()
        }
        binding.shimmerLayout.shimmerViewContainer.visibility = View.GONE
        binding.shimmerLayout.shimmerViewContainer.stopShimmer()
        viewModel.loadMoreProgressVisibility.set(false)

    }





    private fun setSearchUserAdapter() {
        linearLayoutManager = LinearLayoutManager(this@SearchAllUserActivity)
        binding.recylerview.setLayoutManager(linearLayoutManager)
        usersAdapter = UsersAdapter(this@SearchAllUserActivity, dataList) { view, pos, `object` ->
            val item = `object` as UserModel
            hideSoftKeyboard(this@SearchAllUserActivity)
            openProfile(item.id, item.username, item.getProfilePic())
        }
        binding.recylerview.setAdapter(usersAdapter)
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
                    if (viewModel.loadMoreProgressVisibility.get()==false  && !viewModel.ispostFinsh) {
                       viewModel.loadMoreProgressVisibility.set(true)
                        viewModel.pageCount.set(viewModel.pageCount.get()+1)
                        viewModel.searchUsersList(binding.searchEdit.text.toString())
                    }
                }
            }
        })
    }

  /*  fun callApi() {
        val params = JSONObject()
        try {
            params.put("type", "user")
            params.put("keyword", binding.searchEdit!!.text.toString())
            params.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this@SearchAllUserActivity,
            ApiLinks.search,
            params,
            getHeaders(this)
        ) { resp ->
            checkStatus(this@SearchAllUserActivity, resp)
            binding.shimmerViewContainer!!.stopShimmer()
            binding.shimmerViewContainer!!.visibility = View.GONE
            parseUsers(resp)
        }
    }

    fun parseUsers(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code.equals("200", ignoreCase = true)) {
                val msg = jsonObject.optJSONArray("msg")
                val temp_list = ArrayList<UserModel>()
                for (i in 0 until msg.length()) {
                    val data = msg.optJSONObject(i)
                    val userDetailModel = getUserDataModel(data.optJSONObject("User"))
                    temp_list.add(userDetailModel)
                }
                if (pageCount == 0) {
                    dataList!!.clear()
                    dataList!!.addAll(temp_list)
                    if (dataList!!.isEmpty()) {
                        binding.noDataLayout!!.visibility = View.VISIBLE
                    } else {
                        binding.noDataLayout!!.visibility = View.GONE
                        binding.recylerview!!.adapter = usersAdapter
                    }
                } else {
                    if (temp_list.isEmpty()) ispostFinsh = true else {
                        dataList!!.addAll(temp_list)
                        usersAdapter!!.notifyDataSetChanged()
                    }
                }
            } else {
                if (pageCount == 0) {
                    dataList!!.clear()
                }
                if (dataList!!.isEmpty()) binding.noDataLayout!!.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            binding.loadMoreProgress!!.visibility = View.GONE
        }
    }
*/


    fun openProfile(fb_id: String?, username: String?, profile_pic: String?) {
        if (checkProfileOpenValidation(fb_id)) {
            val intent = Intent(this@SearchAllUserActivity, ProfileActivity::class.java)
            intent.putExtra("user_id", fb_id)
            intent.putExtra("user_name", username)
            intent.putExtra("user_pic", profile_pic)
            startActivity(intent)
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        }
    }

    fun addSearchKey(search_key: String?) {
        if (search_key != null && !search_key.isEmpty()) {
            val search_list = Paper.book("Search").read("RecentSearch", ArrayList<String>())!!
            search_list.add(search_key)
            Paper.book("Search").write("RecentSearch", search_list)
        }
    }

    fun showRecentSearch() {
        populateRecentSearch()
        if (searchQueryList.isEmpty()) {
            viewModel.recentLayoutVisibility.set(false)
            return
        } else {
            viewModel.recentLayoutVisibility.set(true)
        }
        if (recentsearchAdapter != null) {
            FilterList(binding.searchEdit.text.toString())
            return
        }

        viewModel.recentLayoutVisibility.set(true)
        viewModel.dataLayoutVisibility.set(false)

        recentsearchAdapter = RecentSearchAdapter(searchQueryList) { v, pos, `object` ->
            val selectedString = searchQueryList[pos]
            if (v.id == R.id.delete_btn) {
                searchQueryList.remove(selectedString)
                recentsearchAdapter!!.notifyDataSetChanged()
                Paper.book("Search").write("RecentSearch", searchQueryList)
            } else {
                binding.searchEdit!!.setText(selectedString)
                binding.searchEdit!!.setSelection(selectedString.length)
                viewModel.pageCount.set(0)
                viewModel.searchUsersList(binding.searchEdit.text.toString())
                viewModel.recentLayoutVisibility.set(false)
            }
        }
       val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        binding.recylerviewSuggestion.setLayoutManager(layoutManager)
        binding.recylerviewSuggestion.setHasFixedSize(true)
        binding.recylerviewSuggestion.setAdapter(recentsearchAdapter)
    }

    private fun populateRecentSearch() {
        val search_list = Paper.book("Search").read("RecentSearch", ArrayList<String>())
        try {
            searchQueryList.clear()
            if (search_list != null && search_list.size > 0) {
                searchQueryList.addAll(search_list)
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
        }
    }

    private fun FilterList(s: CharSequence) {
        try {
            val filter_list = ArrayList<String>()
            for (model in searchQueryList) {
                if (model.lowercase(Locale.getDefault())
                        .contains(s.toString().lowercase(Locale.getDefault()))
                ) {
                    filter_list.add(model)
                }
            }
            if (filter_list.size > 0) {
                recentsearchAdapter?.filter(filter_list)
            }
        } catch (e: Exception) {
            printLog(Constants.tag, "Error : $e")
        }
    }

    override fun onBackPressed() {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        super.onBackPressed()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.search_btn -> {
                hideSoftKeyboard(this@SearchAllUserActivity)

                viewModel.pageCount.set(0)
                viewModel.searchUsersList(binding.searchEdit.text.toString())
                viewModel.recentLayoutVisibility.set(false)
                addSearchKey(binding.searchEdit!!.text.toString())
            }

            R.id.clear_all_txt -> {
                Paper.book("Search").delete("RecentSearch")
                showRecentSearch()
            }

            R.id.ivBack -> super@SearchAllUserActivity.onBackPressed()
        }
    }
}