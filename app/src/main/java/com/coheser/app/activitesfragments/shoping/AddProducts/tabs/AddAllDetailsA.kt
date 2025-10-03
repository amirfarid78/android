package com.coheser.app.activitesfragments.shoping.AddProducts.tabs

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.shoping.AddProducts.AddDetailsA
import com.coheser.app.activitesfragments.shoping.models.AddProductModel
import com.coheser.app.adapters.ViewPagerAdapter
import com.coheser.app.databinding.ActivityAddAllDetailsBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.Functions

class AddAllDetailsA : AppCompatActivity() {
    protected var pager: ViewPager2? = null
    private var adapter: ViewPagerAdapter? = null
    lateinit var binding: ActivityAddAllDetailsBinding
    var addProductModel: AddProductModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAllDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent!=null) {
            addProductModel = intent.getParcelableExtra<AddProductModel>("dataModel")!!
        }
        pager = binding.viewpager
        setTabs()

        binding.nextBtn.setOnClickListener {
            val currentItem = pager?.currentItem

            if (currentItem == 3) {
                if (TextUtils.isEmpty(PriceF.bindingPrice.rootD.priceEdt.text.toString())) {
                    PriceF.bindingPrice.rootD.priceEdt.error = getString(R.string.enter_price)
                    PriceF.bindingPrice.rootD.priceEdt.requestFocus()
                } else {
                    addProductModel?.price = PriceF.bindingPrice.rootD.priceEdt.text.toString()
                    Log.d(Constants.tag, addProductModel?.title.toString())
                    val intent = Intent(this, AddDetailsA::class.java)
                    intent.putExtra("dataModel", addProductModel)
                    startActivity(intent)
                    overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
                    finish()
                }

            }


            else if (currentItem == 1) {
                if (TextUtils.isEmpty(ItemDetailF.bindingDetail.rootD.listingTitle.text.toString())) {
                    ItemDetailF.bindingDetail.rootD.listingTitle.error = getString(R.string.listing_title)
                    ItemDetailF.bindingDetail.rootD.listingTitle.requestFocus()
                } else {
                    addProductModel?.title =
                        ItemDetailF.bindingDetail.rootD.listingTitle.text.toString()
                    addProductModel?.description =
                        ItemDetailF.bindingDetail.rootD.listDescription.text.toString()
                    nextFrag()
                }
            }
            else if (currentItem == 2) {

                    if (DealMethodF.binding.rootD.arrangeMyself.isChecked){
                        addProductModel?.dealMethod ="pickup"

                        nextFrag()
                    }
                    else if(DealMethodF.binding.rootD.meetUp.isChecked){

                        addProductModel?.dealMethod = "meetup"
                        addProductModel?.locationString = DealMethodF.locationString!!
                        addProductModel?.lat = DealMethodF.latitude
                        addProductModel?.lng = DealMethodF.longitude

                        if(TextUtils.isEmpty(addProductModel?.locationString)){
                            Functions.showToast(this, getString(R.string.please_pick_the_location))
                        }
                        else {
                            nextFrag()
                        }
                    }
                    else{
                        Functions.showToast(this, getString(R.string.please_select_the_deal_method))
                    }


            }
            else {
                nextFrag()
            }
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

    }

    fun setTabs() {
        adapter = ViewPagerAdapter(this)
        pager!!.offscreenPageLimit = 4
        registerFragmentWithPager()
        pager?.adapter = adapter
        pager?.setUserInputEnabled(false)
    }

    private fun registerFragmentWithPager() {
        adapter!!.addFrag(ConditionF.newInstance(object :FragmentCallBack{
            override fun onResponce(bundle: Bundle?) {
                addProductModel!!.condition=bundle!!.getString("condition","")
            }
        }), getString(R.string.condition))
        adapter!!.addFrag(ItemDetailF.newInstance(), getString(R.string.item_details))
        adapter!!.addFrag(DealMethodF.newInstance(), getString(R.string.deal_method))
        adapter!!.addFrag(PriceF.newInstance(), getString(R.string.price))
    }

    override fun onBackPressed() {
        when (pager?.getCurrentItem()) {
            0 -> {
                super.onBackPressed()
                prevFrag()
            }
            1 -> {
                prevFrag()
            }

            2 -> {
                prevFrag()
            }

            3 -> {
                prevFrag()
            }

        }
    }

    fun nextFrag() {
        val currentItem = pager?.currentItem

        val totalItems = adapter?.itemCount
        val nextItem = (currentItem!! + 1) % totalItems!!

        pager?.setCurrentItem(nextItem, false)

        when (nextItem) {
            0 -> {
                binding.fragNameTxt.text = getString(R.string.condition)
                binding.progress1.progress = 100
                Log.d(Constants.tag, "" + currentItem)
            }

            1 -> {
                binding.fragNameTxt.text = getString(R.string.item_details)
                binding.progress2.progress = 100
                Log.d(Constants.tag, "" + currentItem)

            }

            2 -> {
                binding.fragNameTxt.text = getString(R.string.deal_method)
                binding.progress3.progress = 100
                Log.d(Constants.tag, "" + currentItem)

            }

            3 -> {
                binding.fragNameTxt.text =getString(R.string.price)
                Log.d(Constants.tag, "" + currentItem)
                binding.progress4.progress = 100
            }
        }

    }

    fun prevFrag() {
        val currentItem = pager?.currentItem

        val prevItem = if (currentItem!! > 0) currentItem - 1 else 0

        pager?.setCurrentItem(prevItem, true)

        when (prevItem) {
            0 -> {
                binding.fragNameTxt.text = getString(R.string.condition)
                binding.progress1.progress = 100
                binding.progress2.progress = 0
                binding.progress3.progress = 0
                binding.progress4.progress = 0
                Log.d(Constants.tag, "" + currentItem)
            }

            1 -> {
                binding.fragNameTxt.text = getString(R.string.item_details)
                binding.progress2.progress = 100
                binding.progress1.progress = 100
                binding.progress3.progress = 0
                binding.progress4.progress = 0
                Log.d(Constants.tag, "" + currentItem)

            }

            2 -> {
                binding.fragNameTxt.text = getString(R.string.deal_method)
                binding.progress3.progress = 100
                binding.progress1.progress = 100
                binding.progress2.progress = 100
                binding.progress4.progress = 0
                Log.d(Constants.tag, "" + currentItem)

            }

            3 -> {
                binding.fragNameTxt.text = getString(R.string.price)
                Log.d(Constants.tag, "" + currentItem)
                binding.progress4.progress = 100
                binding.progress1.progress = 100
                binding.progress2.progress = 100
                binding.progress3.progress = 100
            }
        }
    }

}