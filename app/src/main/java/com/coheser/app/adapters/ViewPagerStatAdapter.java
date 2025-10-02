package com.coheser.app.adapters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.VideosPlayFragment;
import com.coheser.app.interfaces.FragmentCallBack;
import com.coheser.app.models.HomeModel;
import com.coheser.app.simpleclasses.Variables;
import com.coheser.app.simpleclasses.VerticalViewPager;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class ViewPagerStatAdapter extends FragmentStatePagerAdapter {
    private static int PAGE_REFRESH_STATE = PagerAdapter.POSITION_UNCHANGED;
    private final List<Fragment> mFragmentList = new ArrayList<>();
    VerticalViewPager menuPager;

    FragmentCallBack callBack;

    public ViewPagerStatAdapter(@NonNull FragmentManager fm, VerticalViewPager menuPager, boolean isFirstTime, FragmentCallBack callBack) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.menuPager = menuPager;
        this.callBack = callBack;

        if (isFirstTime) {
            if (Paper.book(Variables.PromoAds).contains(Variables.PromoAdsModel)) {
                try {

                    HomeModel initItem = Paper.book(Variables.PromoAds).read(Variables.PromoAdsModel);
                    if (initItem != null)
                        addFragment(new VideosPlayFragment(true, initItem, menuPager, callBack, R.id.mainMenuFragment));

                } catch (Exception e) {
                    Log.d(Constants.tag, "Exception: " + e);
                }
            }
        }


    }

    public void refreshStateSet(boolean isRefresh) {
        if (isRefresh) {
            PAGE_REFRESH_STATE = PagerAdapter.POSITION_NONE;
        } else {
            PAGE_REFRESH_STATE = PagerAdapter.POSITION_UNCHANGED;
        }
    }


    public void addFragment(Fragment fragment) {
        mFragmentList.add(fragment);
    }

    public void removeFragment(int position) {
        mFragmentList.remove(position);
        notifyDataSetChanged();
    }


    @Override
    public int getItemPosition(Object object) {
        // refresh all fragments when data set changed
        return PAGE_REFRESH_STATE;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }
}