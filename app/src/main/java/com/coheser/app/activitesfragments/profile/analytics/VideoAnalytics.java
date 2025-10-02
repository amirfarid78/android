package com.coheser.app.activitesfragments.profile.analytics;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.databinding.ActivityVideoAnalyticsBinding;
import com.coheser.app.models.HomeModel;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.DataParsing;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONObject;


public class VideoAnalytics extends AppCompatLocaleActivity {

    ActivityVideoAnalyticsBinding binding;

    HomeModel item;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(),false);
        binding= DataBindingUtil.setContentView(this, R.layout.activity_video_analytics);

        item=(HomeModel) getIntent().getParcelableExtra("model");

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setdata();
        callApiVideoAnalytics();
    }


    private void callApiVideoAnalytics() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("video_id", item.video_id);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(this, ApiLinks.showVideoAnalytics, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {

                parseData(resp);
            }
        });


    }

    public void parseData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");

                JSONObject video = msg.optJSONObject("Video");
                JSONObject user = msg.optJSONObject("User");
                JSONObject sound = msg.optJSONObject("Sound");

                JSONObject location = msg.optJSONObject("Location");
                JSONObject store = msg.optJSONObject("Store");
                JSONObject videoProduct=msg.optJSONObject("Product");
                JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                JSONObject userPushNotification = user.optJSONObject("PushNotification");

                 item = DataParsing.parseVideoData(user, sound, video,location,store,videoProduct, userPrivacy, userPushNotification);

                 setdata();
            }


        } catch (Exception e) {
            Log.d(Constants.tag,"Exception: "+e);
        }
    }


    public void setdata(){

        binding.videoImage.setController(Functions.frescoImageLoad(item.getThum(),binding.videoImage,false));
        binding.dataPosttime.setText(binding.getRoot().getContext().getString(R.string.data_post_time_since)+":"+ DateOperations.INSTANCE.changeDateFormat("yyyy-MM-dd HH:mm:ss","MMM dd,yyyy hh:mm a",item.created_date));

        binding.videoduration.setText(item.duration+"s");
        binding.videoviewcount.setText(item.views);
        binding.videolikescount.setText(item.like_count);
        binding.videocommentcount.setText(item.video_comment_count);
        binding.videosharecount.setText(item.share);
        binding.videofavcount.setText(item.favourite_count);
    }

}