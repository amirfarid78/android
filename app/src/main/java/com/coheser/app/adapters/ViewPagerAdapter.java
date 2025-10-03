package com.coheser.app.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(Fragment fragment) {
        super(fragment);
    }

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }


    public ViewPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragmentList.get(position);
    }


    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    public List<Fragment> getFragments() {
        return mFragmentList;
    }

    public void clearFragments() {
        mFragmentList.clear();
        mFragmentTitleList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mFragmentList.size();
    }

    @Override
    public long getItemId(int position) {
        // Give an ID to each fragment
        return mFragmentList.get(position).hashCode();
    }

    @Override
    public boolean containsItem(long itemId) {
        // Check if the item is still in the dataset
        for (Fragment fragment : mFragmentList) {
            if (fragment.hashCode() == itemId) {
                return true;
            }
        }
        return false;
    }
}
