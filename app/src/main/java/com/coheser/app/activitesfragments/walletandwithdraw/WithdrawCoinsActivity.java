package com.coheser.app.activitesfragments.walletandwithdraw;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.livestreaming.CallBack;
import com.coheser.app.activitesfragments.profile.settings.AddPayoutMethodActivity;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.models.UserModel;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.DataParsing;
import com.coheser.app.simpleclasses.Dialogs;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONObject;


public class WithdrawCoinsActivity extends AppCompatLocaleActivity {


    TextView coins_txt, coins_txt2, amount_txt, checkout_btn;

    double total_coins = 0f, total_amount = 0f, unit_amount = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(WithdrawCoinsActivity.this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        setContentView(R.layout.activity_withdraw_coins);

        coins_txt = findViewById(R.id.coins_txt);
        coins_txt2 = findViewById(R.id.coins_txt2);

        amount_txt = findViewById(R.id.amount_txt);
        amount_txt.setText(Constants.CURRENCY + Functions.ParseDouble(total_amount, 2));
        checkout_btn = findViewById(R.id.checkout_btn);

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveBack();
            }
        });

        checkout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (total_amount >= 1)
                    Call_api_cash_out();
                else
                    Toast.makeText(WithdrawCoinsActivity.this, getString(R.string.insufficient_funds_you_need_a_minimum_of) + " 1" +
                            Constants.CURRENCY + " " + getString(R.string.one_to_request_a_cashout), Toast.LENGTH_SHORT).show();

            }
        });

        String wallet = Functions.getSharedPreference(WithdrawCoinsActivity.this).getString(Variables.U_WALLET, "0");
        coins_txt.setText(wallet);
        coins_txt2.setText(wallet);
        total_coins = Double.parseDouble(wallet);
        Call_api_get_coins_value();


    }

    private void moveBack() {
        Intent intent = new Intent();
        intent.putExtra("isShow", true);
        setResult(RESULT_OK, intent);
        finish();
    }


    public void Call_api_get_coins_value() {

        String amount = Functions.getSettingsPreference(WithdrawCoinsActivity.this).getString(Variables.CoinWorth, "0");
        unit_amount = Double.parseDouble(amount);

        total_amount = (total_coins * unit_amount);

        if (total_amount >= 1)
            checkout_btn.setBackgroundTintList(ContextCompat.getColorStateList(WithdrawCoinsActivity.this, R.color.blueColor));

        amount_txt.setText(Constants.CURRENCY + Functions.ParseDouble(total_amount, 2));
    }


    public void Call_api_cash_out() {

        JSONObject params = new JSONObject();
        try {
            params.put("user_id", Functions.getSharedPreference(WithdrawCoinsActivity.this).getString(Variables.U_ID, ""));
            params.put("coin", "" + total_coins);
            params.put("amount", "" + total_amount);
            params.put("email", Functions.getSharedPreference(WithdrawCoinsActivity.this).getString(Variables.U_PAYOUT_ID, ""));

        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(this, false, false);
        VolleyRequest.JsonPostRequest(WithdrawCoinsActivity.this, ApiLinks.withdrawRequest, params, Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(WithdrawCoinsActivity.this, resp);

                Functions.cancelLoader();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        JSONObject msgObj = jsonObject.getJSONObject("msg");
                        UserModel userDetailModel = DataParsing.getUserDataModel(msgObj.optJSONObject("User"));

                        SharedPreferences.Editor editor = Functions.getSharedPreference(WithdrawCoinsActivity.this).edit();
                        editor.putString(Variables.U_WALLET, "" + userDetailModel.wallet);
                        editor.commit();

                        Toast.makeText(WithdrawCoinsActivity.this, getString(R.string.check_out_successfully), Toast.LENGTH_SHORT).show();
                        moveBack();
                    } else if (code.equals("201") && (!jsonObject.optString("msg").equalsIgnoreCase("You have already requested a payout."))) {
                        Dialogs.showAlert(WithdrawCoinsActivity.this, getString(R.string.alert), getString(R.string.for_payout_you_must_need_to_add_paypal_id), new CallBack() {
                            @Override
                            public void getResponse(String requestType, String response) {

                                Intent intent = new Intent(WithdrawCoinsActivity.this, AddPayoutMethodActivity.class);
                                intent.putExtra("email", "");
                                intent.putExtra("isEdit", false);
                                addPaymentMethodResult.launch(intent);
                                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        moveBack();
    }

    ActivityResultLauncher<Intent> addPaymentMethodResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (result.getData().getBooleanExtra("isShow", false)) {
                            Call_api_cash_out();
                        }
                    }
                }
            });


}
