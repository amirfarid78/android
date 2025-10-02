package com.coheser.app.activitesfragments.profile.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.coheser.app.R;
import com.coheser.app.activitesfragments.profile.analytics.AnalyticsActivity;
import com.coheser.app.activitesfragments.profile.videopromotion.VideoPromoteStepsActivity;
import com.coheser.app.databinding.ActivityCreatorToolsBinding;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;

public class CreatorToolsActivity extends AppCompatLocaleActivity {

    ActivityCreatorToolsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_creator_tools);

        initControl();
        actionControl();
    }

    private void actionControl() {
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        binding.tabAnalytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAnalytics();
            }
        });
        binding.tabPromoteHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPromotionHistory();
            }
        });
        binding.tabPromote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPromotion();
            }
        });
    }

    private void openAnalytics() {
        Intent intent = new Intent(binding.getRoot().getContext(), AnalyticsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }

    private void openPromotionHistory() {
        Intent intent = new Intent(binding.getRoot().getContext(), PromotionHistoryActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }

    private void openPromotion() {
        Intent intent = new Intent(binding.getRoot().getContext(), VideoPromoteStepsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }


    private void initControl() {
    }
}