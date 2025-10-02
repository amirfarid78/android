package com.coheser.app.activitesfragments.profile.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.ProfileActivity
import com.coheser.app.adapters.BlockUserAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityBlockUserListBinding
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkProfileOpenValidation
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.BlockUsersViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class BlockUserListActivity : AppCompatLocaleActivity(), View.OnClickListener {
    var adapter: BlockUserAdapter? = null
    var datalist = mutableListOf<UserModel>()

    lateinit var binding:ActivityBlockUserListBinding

    private val viewModel: BlockUsersViewModel by viewModel()

    var resultCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode == RESULT_OK) {
                    val data = result.data
                    if (data!!.getBooleanExtra("isShow", false)) {
                        viewModel.getblockUsersList()
                    }
                }
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding = DataBindingUtil.setContentView(this,R.layout.activity_block_user_list)

        binding.viewModel=viewModel
        binding.lifecycleOwner = this

        setObserver()

        binding.shimmerLayout.shimmerViewContainer.startShimmer()

        binding.ivBack.setOnClickListener(this)
        adapter = BlockUserAdapter(datalist) { view: View, pos: Int, `object`: Any ->
            val item = `object` as UserModel
            when (view.id) {
                R.id.block_layout -> {
                    item.id?.let { viewModel.blockUser(it,pos) }
                }

                R.id.mainLayout -> {
                    openProfile(item)
                }

                else -> {}
            }
        }
        binding.recyclerview.setLayoutManager(
            LinearLayoutManager(
                this@BlockUserListActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        binding.recyclerview.setAdapter(adapter)

        viewModel.getblockUsersList()

    }


    fun setObserver(){
        viewModel.blockUsersLiveData.observe(this,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {
                            datalist.clear()
                            datalist.addAll(it)
                            changeUi()
                        }
                    }
                }
                is ApiResponce.Error ->{
                  changeUi()
                }
                else -> {

                }
            }
        })



        viewModel.blockUserLiveData.observe(this,{

            when(it){

                is ApiResponce.Loading ->{
                    showLoader(this@BlockUserListActivity, false, false)
                }

                is ApiResponce.Success ->{
                    cancelLoader()
                }

                is ApiResponce.Error ->{
                    cancelLoader()
                    if(it.message.equals("deleted")){
                        datalist.removeAt(viewModel.unblockPosition)
                        adapter!!.notifyDataSetChanged()
                    }
                    changeUi()
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

        adapter?.notifyDataSetChanged()

        binding!!.shimmerLayout.shimmerViewContainer.visibility = View.GONE
        binding!!.shimmerLayout.shimmerViewContainer.stopShimmer()

    }


    // this will open the profile of user which have uploaded the currenlty running video
    private fun openProfile(item: UserModel) {
        if (checkProfileOpenValidation(item.id)) {
            val intent = Intent(this@BlockUserListActivity, ProfileActivity::class.java)
            intent.putExtra("user_id", item.id)
            intent.putExtra("user_name", item.username)
            intent.putExtra("user_pic", item.getProfilePic())
            resultCallback.launch(intent)
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivBack -> finish()
            else -> {}
        }
    }


}