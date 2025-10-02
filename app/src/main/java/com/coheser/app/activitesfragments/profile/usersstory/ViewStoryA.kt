package com.coheser.app.activitesfragments.profile.usersstory

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.coheser.app.R
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.StoryModel
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Variables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewStoryA : AppCompatLocaleActivity() {
    var adapter: StoryPagerAdapter? = null
    var selectedPosition: Int = 0
    var storyDataList: ArrayList<StoryModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(
            getSharedPreference(
                this
            ).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this, javaClass, false
        )

        setContentView(R.layout.activity_view_story)
        window.navigationBarColor = ContextCompat.getColor(this@ViewStoryA, R.color.blackColor)


        val intent = intent
        selectedPosition = intent.getIntExtra("position", 0)
        storyDataList = intent.getParcelableArrayListExtra<StoryModel>("storyList") as ArrayList<StoryModel>

        lifecycleScope.launch {
            delay(300)
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                storyDataList?.let { setupPager() }
            }
        }

    }


    suspend fun setupPager() {
        withContext(Dispatchers.Main) {
            mPager = findViewById(R.id.viewPager)
            mPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL)
            adapter = storyDataList.let { StoryPagerAdapter(this@ViewStoryA, it, storyDeleteCallback) }
            mPager.setAdapter(adapter)
            mPager.setCurrentItem(selectedPosition)
            mPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    selectedPosition = position
                }
            })
            mPager.setUserInputEnabled(false)
        }
    }


    var storyDeleteCallback: FragmentCallBack = FragmentCallBack { bundle ->
        if (bundle.getBoolean("isShow", false)) {
            if (bundle.getString("action") == "deleteItem") {
                val itemPostion = bundle.getInt("itemPos", 0)
                val itemSelected = storyDataList[selectedPosition]
                val videoList = itemSelected.videoList
                if (videoList.size > 0) {
                    videoList.removeAt(itemPostion)
                    itemSelected.videoList = videoList
                    storyDataList[selectedPosition] = itemSelected
                    adapter?.notifyDataSetChanged()
                } else {
                    storyDataList.removeAt(selectedPosition)
                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }


    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("isShow", true)
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        lateinit var mPager: ViewPager2
    }
}
