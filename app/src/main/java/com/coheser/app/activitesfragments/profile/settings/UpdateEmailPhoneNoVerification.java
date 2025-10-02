package com.coheser.app.activitesfragments.profile.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chaos.view.PinView;
import com.coheser.app.R;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONObject;

public class UpdateEmailPhoneNoVerification extends AppCompatLocaleActivity implements View.OnClickListener {


    TextView tv1, resendCode, tvData;
    ImageView ivBack;
    RelativeLayout rl1;
    Button sendOtpBtn;
    PinView etCode;
    boolean isEmail = true;
    String data = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(UpdateEmailPhoneNoVerification.this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        setContentView(R.layout.activity_update_email_phone_no_verification);


        initViews();
        addClicklistner();
        oneMinuteTimer();

        etCode.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                // this will check th opt code validation
                String txtName = etCode.getText().toString();
                if (txtName.length() == 4) {
                    sendOtpBtn.setEnabled(true);
                    sendOtpBtn.setClickable(true);
                } else {
                    sendOtpBtn.setEnabled(false);
                    sendOtpBtn.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void initViews() {
        tv1 = findViewById(R.id.tv1_id);
        resendCode = findViewById(R.id.resend_code);
        tvData = findViewById(R.id.tvData);
        ivBack = findViewById(R.id.goBack);
        rl1 = findViewById(R.id.rl1_id);
        sendOtpBtn = findViewById(R.id.send_otp_btn);
        etCode = findViewById(R.id.et_code);


        SetUpScreenData();
    }

    private void SetUpScreenData() {
        isEmail = getIntent().getStringExtra("type").equalsIgnoreCase("email");
        data = getIntent().getStringExtra("data");
        tvData.setText(data);
    }

    // initlize all the click lister
    private void addClicklistner() {
        ivBack.setOnClickListener(this);
        resendCode.setOnClickListener(this);
        sendOtpBtn.setOnClickListener(this);
    }

    // run the one minute countdown timer
    private void oneMinuteTimer() {
        rl1.setVisibility(View.VISIBLE);

        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
                tv1.setText(getString(R.string.resend_code) + " 00:" + l / 1000);
            }

            @Override
            public void onFinish() {
                rl1.setVisibility(View.GONE);
                resendCode.setVisibility(View.VISIBLE);
            }

        }.start();

    }



    private void moveBack() {
        Intent intent = new Intent();
        intent.putExtra("isShow", true);
        setResult(RESULT_OK, intent);
        finish();
    }


    private void updatePhoneNoHitApi() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("auth_token", Functions.getSharedPreference(UpdateEmailPhoneNoVerification.this).getString(Variables.AUTH_TOKEN, "0"));
            parameters.put("phone", data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(UpdateEmailPhoneNoVerification.this, false, false);
        VolleyRequest.JsonPostRequest(UpdateEmailPhoneNoVerification.this, ApiLinks.editProfile, parameters, Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(UpdateEmailPhoneNoVerification.this, resp);
                Functions.cancelLoader();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        SharedPreferences.Editor editor = Functions.getSharedPreference(UpdateEmailPhoneNoVerification.this).edit();
                        editor.putString(Variables.U_PHONE_NO, data).commit();
                        moveBack();
                    } else {
                        Functions.showToast(UpdateEmailPhoneNoVerification.this, jsonObject.optString("msg"));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.goBack:
                UpdateEmailPhoneNoVerification.super.onBackPressed();
                break;

            case R.id.resend_code:
                etCode.setText("");


                break;

            case R.id.send_otp_btn: {

            }

            break;

        }
    }





}