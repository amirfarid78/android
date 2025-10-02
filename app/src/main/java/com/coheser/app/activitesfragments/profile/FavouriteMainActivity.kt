package com.coheser.app.activitesfragments.profile

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.favourite.FavouriteVideosFragment.Companion.newInstance
import com.coheser.app.activitesfragments.search.SearchHashTagsFragment
import com.coheser.app.activitesfragments.soundlists.FavouriteSoundFragment
import com.coheser.app.adapters.ViewPagerAdapter
import com.coheser.app.databinding.ActivityFavouriteMainBinding
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Variables
import com.google.android.material.tabs.TabLayoutMediator

class FavouriteMainActivity : AppCompatLocaleActivity() {
    var adapter: ViewPagerAdapter? = null
    lateinit var binding:ActivityFavouriteMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding=DataBindingUtil.setContentView(this,R.layout.activity_favourite_main_)
        initControl()
        actionControl()
    }

    private fun actionControl() {
       binding.backBtn.setOnClickListener { super@FavouriteMainActivity.onBackPressed() }
    }

    private fun initControl() {
        SetTabs()
    }

    fun SetTabs() {
        adapter = ViewPagerAdapter(this)

        binding.viewpager.setOffscreenPageLimit(3)
        registerFragmentWithPager()
        binding.viewpager.setAdapter(adapter)
        addTabs()
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabs.getTabAt(position)!!.select()
            }
        })
    }

    private fun addTabs() {
        val tabLayoutMediator = TabLayoutMediator(
            binding.tabs, binding.viewpager
        ) { tab, position ->
            if (position == 0) {
                tab.setText(getString(R.string.videos))
            } else if (position == 1) {
                tab.setText(getString(R.string.sounds))
            } else if (position == 2) {
                tab.setText(getString(R.string.hashtag))
            }
        }
        tabLayoutMediator.attach()
    }

    private fun registerFragmentWithPager() {
        adapter!!.addFrag(newInstance(getSharedPreference(this).getString(Variables.U_ID, ""), "0"))
        adapter!!.addFrag(FavouriteSoundFragment.newInstance())
        adapter!!.addFrag(SearchHashTagsFragment.newInstance("favourite"))
    }


}
