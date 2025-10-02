package com.coheser.app.activitesfragments.profile;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import com.coheser.app.R;
import com.coheser.app.databinding.ActivitySeeFullImageBinding;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;

public class SeeFullImageActivity extends AppCompatLocaleActivity {

    ActivitySeeFullImageBinding binding;
    String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_see_full_image);


        imageUrl = getIntent().getStringExtra("image_url");

        binding.ivClose.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.ivProfile.setController(Functions.frescoImageLoad(imageUrl, binding.ivProfile, getIntent().getBooleanExtra("isGif", false)));

    }
}