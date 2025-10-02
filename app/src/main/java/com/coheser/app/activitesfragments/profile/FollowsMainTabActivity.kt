package com.coheser.app.activitesfragments.profile

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.coheser.app.R
import com.coheser.app.activitesfragments.profile.followtabs.FollowerUserFragment
import com.coheser.app.activitesfragments.profile.followtabs.FollowingUserFragment
import com.coheser.app.activitesfragments.profile.followtabs.SuggestionsFragment.Companion.newInstance
import com.coheser.app.adapters.ViewPagerAdapter
import com.coheser.app.databinding.ActivityFollowsMainTabBinding
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showUsername
import com.coheser.app.simpleclasses.Variables
import com.google.android.material.tabs.TabLayoutMediator

class FollowsMainTabActivity : AppCompatLocaleActivity() {
    var userName: String? = ""
    var userId: String? = ""
    var followerCount: Long = 0
    var followingCount: Long = 0
    var fromWhere: String? = ""
    var adapter: ViewPagerAdapter? = null
    var isActivityCallback = false

    lateinit var binding:ActivityFollowsMainTabBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(this@FollowsMainTabActivity).getString(
                Variables.APP_LANGUAGE_CODE,
                Variables.DEFAULT_LANGUAGE_CODE
            ), this, javaClass, false
        )
        binding = DataBindingUtil.setContentView(this,R.layout.activity_follows_main_tab)

        followingCount = intent.getLongExtra("followingCount", 0)
        followerCount = intent.getLongExtra("followerCount", 0)
        userId = intent.getStringExtra("id")
        userName = intent.getStringExtra("userName")
        fromWhere = intent.getStringExtra("from_where")
        binding.tvTitle.setText(showUsername(userName))
        binding.backBtn.setOnClickListener { super@FollowsMainTabActivity.onBackPressed() }
        if (userId == null) {
            userId = ""
        }
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
        if (fromWhere.equals("following", ignoreCase = true)) {
            binding.tabs.getTabAt(0)!!.select()
        } else if (fromWhere.equals("fan", ignoreCase = true)) {
            binding.tabs.getTabAt(1)!!.select()
        } else {
            binding.tabs.getTabAt(2)!!.select()
        }
    }

    private fun addTabs() {
        val tabLayoutMediator = TabLayoutMediator(
            binding.tabs!!, binding.viewpager!!
        ) { tab, position ->
            if (position == 0) {
                if (followingCount == 0L) {
                    tab.setText(getString(R.string.following))
                } else {
                    tab.setText(getString(R.string.following) + " " + followingCount)
                }
            } else if (position == 1) {
                if (followerCount == 0L) {
                    tab.setText(getString(R.string.followers))
                } else {
                    tab.setText(getString(R.string.followers) + " " + followerCount)
                }
            } else if (position == 2) {
                tab.setText(getString(R.string.suggested))
            }
        }
        tabLayoutMediator.attach()
    }

    private fun registerFragmentWithPager() {
        adapter!!.addFrag(FollowingUserFragment.newInstance(userId, false) { bundle ->
            val updateTab =  binding.tabs!!.getTabAt(0)
            if (bundle.getBoolean("isShow")) {
                followingCount++
            } else {
                followingCount--
            }
            isActivityCallback = true
            if (followingCount == 0L) {
                updateTab!!.setText(getString(R.string.following))
            } else {
                updateTab!!.setText(getString(R.string.following) + " " + followingCount)
            }
        })
        adapter!!.addFrag(FollowerUserFragment.newInstance(userId, false) { bundle ->
            val updateTab =  binding.tabs!!.getTabAt(0)
            if (bundle.getBoolean("isShow")) {
                followingCount++
            } else {
                followingCount--
            }
            isActivityCallback = true
            if (followerCount == 0L) {
                updateTab!!.setText(getString(R.string.followers))
            } else {
                updateTab!!.setText(getString(R.string.followers) + " " + followerCount)
            }
        })
        adapter!!.addFrag(newInstance(userId!!, false) { isActivityCallback = true })
    }

    override fun onBackPressed() {
        if (isActivityCallback) {
            val intent = Intent()
            intent.putExtra("isShow", true)
            setResult(RESULT_OK, intent)
        }
        finish()
    }
}