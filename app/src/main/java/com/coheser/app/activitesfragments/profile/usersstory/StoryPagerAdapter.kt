package com.coheser.app.activitesfragments.profile.usersstory

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.StoryModel

class StoryPagerAdapter(
    fragmentActivity: FragmentActivity,
    var allDataList: ArrayList<StoryModel>,
    var callBack: FragmentCallBack
) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        val fragment = StoryItemF(allDataList, position, callBack)
        val bundle = Bundle()
        fragment.arguments = bundle
        return fragment
    }

    override fun getItemCount(): Int {
        return allDataList.size
    }
}
