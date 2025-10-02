package com.coheser.app.activitesfragments.livestreaming.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.coheser.app.activitesfragments.livestreaming.activities.MultiViewLiveActivity
import com.coheser.app.activitesfragments.livestreaming.fragments.MultipleStreamerListFragment
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel

class MultiCastStatAdapter(
    fm: FragmentManager,
    dataList: ArrayList<LiveUserModel>,
    activity: MultiViewLiveActivity
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    var dataList: ArrayList<LiveUserModel> = ArrayList()
    var activity: MultiViewLiveActivity

    init {
        this.dataList = dataList
        this.activity = activity
    }

    fun refreshStateSet(isRefresh: Boolean) {
        if (isRefresh) {
            PAGE_REFRESH_STATE = POSITION_NONE
        } else {
            PAGE_REFRESH_STATE = POSITION_UNCHANGED
        }
    }


    override fun getItemPosition(`object`: Any): Int {
        // refresh all fragments when data set changed
        return PAGE_REFRESH_STATE
    }

    override fun getItem(position: Int): Fragment {
        val item = dataList[position]
        val fragment = MultipleStreamerListFragment(item, activity)
        val bundle = Bundle()
        fragment.arguments = bundle
        return fragment
    }

    override fun getCount(): Int {
        return dataList.size
    }

    companion object {
        private var PAGE_REFRESH_STATE = POSITION_UNCHANGED
    }
}