package com.coheser.app.activitesfragments.shoping

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.ReportTypeActivity
import com.coheser.app.activitesfragments.shoping.adapter.ProfileProductsAdapter
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityShopBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.repositories.UserRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.viewModels.ShopPViewModel

class ShopA : AppCompatActivity() {
    lateinit var binding : ActivityShopBinding
    var userName = ""
    var profile = ""
    var id = ""
    var adapter: ProfileProductsAdapter? = null


    lateinit var viewModel: ShopPViewModel
    private val userRepository = UserRepository()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelProviderFactory = ShopPViewModel.MyShopFactory(binding.root.context,userRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory )[ShopPViewModel::class.java]


        observer()
        inits()
        setactions()


    }
    fun setactions(){
        binding.backBtn.setOnClickListener {
            finish()
        }
    }
    fun observer(){
        viewModel.userShopLiveData.observe(this,{
            when(it){
                is ApiResponce.Success ->{
                    dataList = it.data!!
                    setRecycler()
                    Log.d(Constants.tag,"datalist size: ${dataList.size}")
                }
                else ->{}
            }
        })
    }
    fun refreshUI(){
        if (dataList.size == 0){
            binding.recyclerView.visibility = View.GONE
        }else{
            binding.recyclerView.visibility = View.GONE
            adapter!!.notifyDataSetChanged()
        }
    }
    fun  inits(){
        userName = intent.getStringExtra("name")!!
        profile = intent.getStringExtra("profile")!!
        id = intent.getStringExtra("id")!!

        binding.reportBtn.setOnClickListener { openUserShopReport() }

        binding.userImage.controller = Functions.frescoImageLoad(
            profile,
            binding.userImage,
            false
        )
        binding.userName.text = getString(R.string.dot_shop, userName)

        viewModel.showProducts(pageCount,id)

    }
    var dataList = ArrayList<ProductModel>()
    var pageCount = 0
    var ispostFinsh = false

    fun setRecycler(){
        adapter = ProfileProductsAdapter(
            this, dataList
        ) { view, pos, `object` ->

        }
        adapter = ProfileProductsAdapter(this,dataList,object :AdapterClickListener{
            override fun onItemClick(view: View?, pos: Int, `object`: Any?) {
                val productModel = `object` as ProductModel
                val intent = Intent(this@ShopA, ShopItemDetailA::class.java)
                intent.putExtra("data", productModel)
                shopItemResultLauncher.launch(intent)
            }

        })
        binding.recyclerView.adapter = adapter


        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                if (scrollInItem == 0) {
                    recyclerView.isNestedScrollingEnabled = true
                } else {
                    recyclerView.isNestedScrollingEnabled = false
                }
                if (userScrolled && scrollOutitems == dataList.size - 1) {
                    userScrolled = false
                    pageCount = pageCount + 1
                    viewModel.showProducts(pageCount,id)
                }
            }
        })

    }

    private val shopItemResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                val isUpdate = data.getBooleanExtra("update", false)
                if (isUpdate) {
                   viewModel.showProducts(pageCount,id)
                }
            }
        }
    }

    fun openUserShopReport() {
        val intent = Intent(this@ShopA, ReportTypeActivity::class.java)
        intent.putExtra("id", id)
        intent.putExtra("type", "shop")
        intent.putExtra("isFrom", false)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }
}