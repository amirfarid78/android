package com.coheser.app.activitesfragments.payment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.payment.utils.CreditCardBrand;
import com.coheser.app.activitesfragments.payment.utils.CreditCardNumberListener;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.databinding.FragmentAddCreditCardBinding;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.Dialogs;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Calendar;



public class AddCreditCardActivity extends AppCompatLocaleActivity implements View.OnClickListener {

    FragmentAddCreditCardBinding binding;
    String userId, userName;
    boolean isValid = true;
    int previousLength;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(AddCreditCardActivity.this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);

        binding = FragmentAddCreditCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonNext.setText(getString(R.string.next));
        userId = Functions.getSharedPreference(this).getString(Variables.U_ID, "");
        userName = Functions.getSharedPreference(this).getString(Variables.F_NAME, "")
                + " " + Functions.getSharedPreference(this).getString(Variables.L_NAME, "");
        methodInitLayout();
        initializeListeners();


    }

    /*MMethod InitLayouts*/
    private void methodInitLayout() {

        binding.buttonNext.setEnabled(false);
        binding.buttonNext.setClickable(false);
        binding.cardNumberEdit.addNumberListener(new CreditCardNumberListener() {
            @Override
            public void onChanged(@NonNull @NotNull String number, @NonNull @NotNull CreditCardBrand brand) {
                if (methodValidate()) {
                    enableBtn();
                } else {
                    disableBtn();
                }
            }
        });

        binding.expirationEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                previousLength = binding.expirationEdit.getText().toString().length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int before, int i2) {
                int length = binding.expirationEdit.getText().toString().trim().length();
                String working = charSequence.toString();
                if (previousLength <= length && length < 3) {
                    int month = Integer.parseInt(binding.expirationEdit.getText().toString());

                    Functions.printLog(Constants.tag, "month : " + month);
                    if (length == 1 && month >= 2) {
                        String autoFixStr = "0" + month + "/";
                        binding.expirationEdit.setText(autoFixStr);
                        binding.expirationEdit.setSelection(3);
                    } else if (length == 2 && month <= 12) {
                        String autoFixStr = binding.expirationEdit.getText().toString() + "/";
                        binding.expirationEdit.setText(autoFixStr);
                        binding.expirationEdit.setSelection(3);
                    } else if (length == 2 && month > 12) {
                        binding.expirationEdit.setText("1");
                        binding.expirationEdit.setSelection(1);
                    }
                } else if (working.length() == 5 && before == 0) {
                    String enteredYear = working.substring(3);
                    String enterYear = "20" + enteredYear;
                    Functions.printLog(Constants.tag, "enterYear : " + enterYear);

                    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                    isValid = Integer.parseInt(enterYear) >= currentYear;
                }

                if (working.length() != 5) {
                    isValid = false;
                }

                if (!isValid) {
                    disableBtn();
                } else if (isValid) {
                    if (methodValidate()) {
                        enableBtn();
                    } else {
                        disableBtn();
                    }
                }


            }

            @Override
            public void afterTextChanged(Editable s) {
                //auto generated method
            }
        });

        binding.cvvEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //auto generated method
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (methodValidate()) {
                    enableBtn();
                } else {
                    disableBtn();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //auto generated method
            }
        });

        binding.zipCodeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //auto generated method
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (methodValidate()) {
                    enableBtn();
                } else {
                    disableBtn();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                //auto generated method
            }
        });

    }


    public void enableBtn() {
        binding.buttonNext.setBackground(ContextCompat.getDrawable(AddCreditCardActivity.this, R.drawable.d_round_colord_6));
        binding.buttonNext.setClickable(true);
        binding.buttonNext.setEnabled(true);
        binding.buttonNext.setTextColor(getColor(R.color.whiteColor));
    }

    public void disableBtn() {
        binding.buttonNext.setClickable(false);
        binding.buttonNext.setEnabled(false);
        binding.buttonNext.setBackground(ContextCompat.getDrawable(AddCreditCardActivity.this, R.drawable.d_less_round_gray_transparent));
        binding.buttonNext.setTextColor(getColor(R.color.blackColor));
    }

    private void initializeListeners() {

        binding.backBtn.setOnClickListener(this);
        binding.buttonNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.backBtn) {
            Functions.hideSoftKeyboard(AddCreditCardActivity.this);
            onBackPressed();
        } else if (id == R.id.button_next) {
            Functions.hideSoftKeyboard(AddCreditCardActivity.this);
            callApiForCard();
        }
    }


    private void callApiForCard() {
        JSONObject params = new JSONObject();
        String[] date = binding.expirationEdit.getText().toString().split("/");
        String month = date[0];
        String year = date[1];
        try {
            params.put("user_id", userId);
            params.put("default", "0");
            params.put("name", userName);
            params.put("card", binding.cardNumberEdit.getText().toString().replace(" ", ""));
            params.put("cvc", binding.cvvEdit.getText().toString().trim());
            params.put("exp_month", month);
            params.put("exp_year", year);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.printLog(Constants.tag, "sendobj at callApiForCard:" + params);


        binding.buttonNext.startLoading();
        VolleyRequest.JsonPostRequest(this, ApiLinks.addCard, params, Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                binding.buttonNext.stopLoading();

                try {
                    JSONObject respobj = new JSONObject(resp);
                    if (respobj.getString("code").equals("200")) {
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        AddCreditCardActivity.this.onBackPressed();
                    } else {
                        Dialogs.showAlert(AddCreditCardActivity.this, binding.getRoot().getContext().getString(R.string.alert), respobj.getString("msg"));
                    }
                } catch (Exception e) {
                    Functions.printLog(Constants.tag, "Exception: " + e);
                }
            }


        });

    }

    private boolean methodValidate() {
        if (binding.cvvEdit.getText().length() < 3) {
            return false;
        }

        if (TextUtils.isEmpty(binding.cardNumberEdit.getText().toString())) {
            return false;
        }

        if (TextUtils.isEmpty(binding.zipCodeEdit.getText().toString())) {
            return false;
        }


        if (TextUtils.isEmpty(binding.expirationEdit.getText().toString())) {
            return false;
        }

        return isValid;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    hideKeyboard(v);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}