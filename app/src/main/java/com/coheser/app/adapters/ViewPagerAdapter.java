package com.coheser.app.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private  List<Fragment> mFragmentList = new ArrayList<>();
    FragmentManager fragmentManager;
    public ViewPagerAdapter(Fragment fragment) {
        super(fragment);
    }
    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        mFragmentList.clear();
        this.fragmentManager=fragmentManager;
    }


    public ViewPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(mFragmentList.get(position).isAdded() && fragmentManager!=null){
            fragmentManager.beginTransaction().remove(mFragmentList.get(position)).commit();
        }
        return mFragmentList.get(position);
    }


    public void addFrag(Fragment fragment) {
        mFragmentList.add(fragment);
    }

    // Set fragments dynamically
    public void setFragments( List<Fragment> fragments) {
        mFragmentList.clear();
        mFragmentList.addAll(fragments);
        notifyDataSetChanged();
    }

    public Fragment getFragments(int pos) {
        return mFragmentList.get(pos);
    }

    public void removeFrag(int index) {

        mFragmentList.remove(index);
    }


    public void clearFragments() {
        if(fragmentManager!=null) {
            for (int i = 0; i < mFragmentList.size(); i++) {
                if(mFragmentList.get(i).isAdded() && fragmentManager!=null){
                    fragmentManager.beginTransaction().remove(mFragmentList.get(i)).commit();
                }
            }
        }

        mFragmentList.clear();
    }

    @Override
    public int getItemCount() {
        return mFragmentList.size();
    }



}