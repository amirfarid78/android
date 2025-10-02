package com.coheser.app.activitesfragments.profile.videopromotion;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.databinding.DataBindingUtil;
import androidx.viewpager2.widget.ViewPager2;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.adapters.ViewPagerAdapter;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.databinding.ActivityVideoPromoteStepsBinding;
import com.coheser.app.models.HomeModel;
import com.coheser.app.models.RequestPromotionModel;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONObject;

public class VideoPromoteStepsActivity extends AppCompatLocaleActivity {

    public static ViewPagerAdapter adapter;
    public static ViewPager2 viewpager;
    public static ProgressBar progressBar;
    public static RequestPromotionModel requestPromotionModel;
    ActivityVideoPromoteStepsBinding binding;
    HomeModel selectedVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_promote_steps);

        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        initControl();
        actionControl();
    }

    private void actionControl() {
        showAdsDetailSticker();
    }

    public void showAdsDetailSticker() {
        JSONObject params = new JSONObject();
        try {
            params.put("user_id", Functions.getSharedPreference(binding.getRoot().getContext()).getString(Variables.U_ID, ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        VolleyRequest.JsonPostRequest(VideoPromoteStepsActivity.this, ApiLinks.showAddSettings, params, Functions.getHeaders(VideoPromoteStepsActivity.this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(VideoPromoteStepsActivity.this, resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code != null && code.equals("200")) {
                        JSONObject msgObj = jsonObject.getJSONObject("msg");
                        requestPromotionModel.setVideoViewsStat(msgObj.optLong("video_views", 0));
                        requestPromotionModel.setWebsiteStat(msgObj.optLong("website_visits", 0));
                        requestPromotionModel.setFollowerStat(msgObj.optLong("followers", 0));
                    }

                } catch (Exception e) {
                    Log.d(Constants.tag, "Exception: " + e);
                }


            }
        });

    }


    private void initControl() {
        requestPromotionModel = new RequestPromotionModel();
        if (getIntent().hasExtra("modelData")) {
            selectedVideo = getIntent().getParcelableExtra("modelData");
            requestPromotionModel.setSelectedVideo(selectedVideo);
        } else {
            requestPromotionModel.setSelectedVideo(null);
        }
        SetTabs();
    }


    public void SetTabs() {
        progressBar = binding.progressBar;
        viewpager = binding.viewpager;
        adapter = new ViewPagerAdapter(this);
        viewpager.setOffscreenPageLimit(1);
        registerFragmentWithPager();
        viewpager.setAdapter(adapter);
        viewpager.setUserInputEnabled(false);
    }

    private void registerFragmentWithPager() {
        adapter.addFrag(VideoPromoteSelectGoalFragment.newInstance());
    }

    @Override
    public void onBackPressed() {
        int Counts = viewpager.getCurrentItem();
        if (Counts < 1) {
            super.onBackPressed();
        } else {
            try {
                adapter.removeFrag(Counts);
                adapter.notifyItemRemoved(Counts);
            } catch (Exception e) {
                Log.d(Constants.tag, "Exception Index: " + e);
            }
            Counts = Counts - 1;
            viewpager.setCurrentItem(Counts, true);
            progressBar.setProgress(Counts);
        }

    }
}