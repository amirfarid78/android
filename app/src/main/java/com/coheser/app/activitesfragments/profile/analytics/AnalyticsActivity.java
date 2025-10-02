package com.coheser.app.activitesfragments.profile.analytics;

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

public class AnalyticsActivity extends AppCompatLocaleActivity {


    protected TabLayout tabLayout;
    protected ViewPager2 pager;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        setContentView(R.layout.activity_analytics);

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        SetTabs();
    }


    public void SetTabs() {
        adapter = new ViewPagerAdapter(this);
        pager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tabs);

        pager.setOffscreenPageLimit(2);
        pager.setUserInputEnabled(false);

        adapter.addFrag(OverviewFragment.newInstance());
        //  adapter.addFrag(ContentAnalyticF.newInstance());
        adapter.addFrag(FollowersAnalyticsFragment.newInstance());

        pager.setAdapter(adapter);
        addTabs();

    }

    private void addTabs() {
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0) {
                    tab.setText(R.string.overview);
                }
//                else
//                if (position==1)
//                {
//                    tab.setText(R.string.content);
//                }
                else if (position == 1) {
                    tab.setText(R.string.following);
                }
            }
        });
        tabLayoutMediator.attach();
    }


}