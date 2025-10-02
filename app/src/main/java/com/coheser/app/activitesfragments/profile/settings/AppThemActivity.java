package com.coheser.app.activitesfragments.profile.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.coheser.app.R;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.DarkModePrefManager;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;

public class AppThemActivity extends AppCompatLocaleActivity {

    LinearLayout tabDark, tabLight;
    ImageView ivLightSelection, ivDarkSelection;
    DarkModePrefManager darkModePrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);

        setContentView(R.layout.activity_app_them);
        initControl();
    }

    private void initControl() {
        darkModePrefManager = new DarkModePrefManager(AppThemActivity.this);
        ivLightSelection = findViewById(R.id.ivLightSelection);
        ivDarkSelection = findViewById(R.id.ivDarkSelection);
        tabDark = findViewById(R.id.tabDark);
        tabDark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDarkMode(true);
            }
        });
        tabLight = findViewById(R.id.tabLight);
        tabLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDarkMode(false);
            }
        });
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        setupScreenData();
    }

    private void updateDarkMode(boolean isNightMode) {

        if (darkModePrefManager.isNightMode() && isNightMode) {

            Toast.makeText(AppThemActivity.this, getString(R.string.already_mode_active), Toast.LENGTH_SHORT).show();
        } else {
            darkModePrefManager.setDarkMode(isNightMode);
            darkModePrefManager.setDarkToReset(true);
            recreate();
        }

    }

    private void setupScreenData() {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            ivDarkSelection.setImageDrawable(ContextCompat.getDrawable(AppThemActivity.this, R.drawable.ic_selection));
            ivLightSelection.setImageDrawable(ContextCompat.getDrawable(AppThemActivity.this, R.drawable.ic_un_selected));
        } else {
            ivDarkSelection.setImageDrawable(ContextCompat.getDrawable(AppThemActivity.this, R.drawable.ic_un_selected));
            ivLightSelection.setImageDrawable(ContextCompat.getDrawable(AppThemActivity.this, R.drawable.ic_selection));
        }
    }

    @Override
    public void onBackPressed() {
        if (darkModePrefManager.isDarkToReset()) {
            Intent intent = new Intent();
            intent.putExtra("isShow", true);
            setResult(RESULT_OK, intent);
            darkModePrefManager.setDarkToReset(false);
        }
        finish();
    }
}