package com.coheser.app.activitesfragments.profile.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

public class UpdateEmailPhoneActivity extends AppCompatLocaleActivity implements View.OnClickListener {

    TextView tvTitle;
    RelativeLayout tabPhoneNo;
    EditText  edtPhoneNo;
    CountryCodePicker ccp;
    String phoneNo;
    // start trimming activity
    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow", false)) {
                            moveBack();
                        }

                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(UpdateEmailPhoneActivity.this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        setContentView(R.layout.activity_update_email_phone);

        initControl();
    }

    private void initControl() {
        tvTitle = findViewById(R.id.tvTitle);
        tabPhoneNo = findViewById(R.id.tabPhoneNo);

        edtPhoneNo = findViewById(R.id.phone_edit);
        findViewById(R.id.goBack).setOnClickListener(this);
        ccp = findViewById(R.id.ccp);
        ccp.registerPhoneNumberTextView(edtPhoneNo);

        findViewById(R.id.btnSendCodePhone).setOnClickListener(this);


        setUpScreenData();
    }

    private void setUpScreenData() {
        tvTitle.setText(getString(R.string.update_phone));

    }

    public boolean checkPhoneValidation() {

        final String st_phone = edtPhoneNo.getText().toString();

        if (TextUtils.isEmpty(st_phone)) {
            edtPhoneNo.setError(getString(R.string.enter_valid_phone_no));
            edtPhoneNo.setFocusable(true);
            return false;
        }


        if (!ccp.isValid()) {
            edtPhoneNo.setError(getString(R.string.enter_valid_phone_no));
            edtPhoneNo.setFocusable(true);
            return false;
        }

        phoneNo = edtPhoneNo.getText().toString();
        phoneNo = Functions.applyPhoneNoValidation(phoneNo, ccp.getSelectedCountryCodeWithPlus());

        return true;
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSendCodePhone:
                if (checkPhoneValidation()) {

                    Log.d(Constants.tag, "Phone : " + phoneNo);
                    moveToVerificationScreen(phoneNo);
                }
                break;

            case R.id.goBack: {
                UpdateEmailPhoneActivity.super.onBackPressed();
            }
            break;
        }
    }

    private void moveToVerificationScreen(String data) {
        Intent intent = new Intent(UpdateEmailPhoneActivity.this, UpdateEmailPhoneNoVerification.class);
        intent.putExtra("type", getIntent().getStringExtra("type"));
        intent.putExtra("data", data);
        resultCallback.launch(intent);

    }

    private void moveBack() {
        Intent intent = new Intent();
        intent.putExtra("isShow", true);
        setResult(RESULT_OK, intent);
        finish();
    }
}