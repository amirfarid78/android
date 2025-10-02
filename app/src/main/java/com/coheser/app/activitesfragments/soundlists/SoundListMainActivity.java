package com.coheser.app.activitesfragments.soundlists;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.coheser.app.R;
import com.coheser.app.adapters.ViewPagerAdapter;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class SoundListMainActivity extends AppCompatLocaleActivity implements View.OnClickListener {

    protected TabLayout tablayout;
    protected ViewPager2 pager;
    Context context;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        setContentView(R.layout.activity_sound_list_main);

        initControl();
        actionControl();
    }

    private void actionControl() {
        findViewById(R.id.goBack).setOnClickListener(this);
    }

    private void initControl() {
        context = SoundListMainActivity.this;
        SetTabs();
    }


    public void SetTabs() {
        adapter = new ViewPagerAdapter(this);
        pager = findViewById(R.id.viewpager);
        tablayout = findViewById(R.id.groups_tab);

        pager.setOffscreenPageLimit(2);
        registerFragmentWithPager();
        pager.setAdapter(adapter);
        addTabs();

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tablayout.getTabAt(position).select();
            }
        });

    }

    private void addTabs() {
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tablayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0) {
                    tab.setText(context.getString(R.string.discover));
                } else if (position == 1) {
                    tab.setText(context.getString(R.string.my_fav));
                }
            }
        });
        tabLayoutMediator.attach();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goBack:
                onBackPressed();
                break;
        }
    }

    private void registerFragmentWithPager() {
        adapter.addFrag(DiscoverSoundListFragment.newInstance());
        adapter.addFrag(FavouriteSoundFragment.newInstance());
    }


}
