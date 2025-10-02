package com.coheser.app.activitesfragments.shoping;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.coheser.app.adapters.ViewPagerAdapter;
import com.coheser.app.databinding.ActivityHistoryBinding;

public class HistoryA extends AppCompatActivity{


    ActivityHistoryBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.ivBack.setOnClickListener(v -> onBackPressed());

        SetTabs();
    }
    ViewPagerAdapter adapter;
    public void SetTabs() {
        adapter = new ViewPagerAdapter(this);
        binding.viewpager.setAdapter(adapter);

        adapter.addFrag(OrderListF.newInstance("all"));
        adapter.addFrag(OrderListF.newInstance("shipped"));
        adapter.addFrag(OrderListF.newInstance("completed"));

        adapter.notifyDataSetChanged();

        TabLayoutMediator tabLayoutMediator=new TabLayoutMediator(binding.tablayout, binding.viewpager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                if(position==0)
                tab.setText("All");
                else if(position==1)
                    tab.setText("Shipped");
                else if(position==2)
                    tab.setText("Complete");
            }
        });
        tabLayoutMediator.attach();

        binding.viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.tablayout.getTabAt(position).select();
            }
        });

    }

}
