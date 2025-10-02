package com.coheser.app.activitesfragments.profile.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.coheser.app.R;
import com.coheser.app.databinding.ActivityChangePasswordBinding;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatLocaleActivity implements View.OnClickListener {

    private Boolean oldCheck = true, newCheck = true, confirmCheck = true;

    ActivityChangePasswordBinding binding;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(ChangePasswordActivity.this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_change_password);

         user = FirebaseAuth.getInstance().getCurrentUser();

        binding.goBack.setOnClickListener(this);
        binding.changePassBtn.setOnClickListener(this);


        binding.llOldHide.setOnClickListener(this);
        binding.llNewHide.setOnClickListener(this);
        binding.llConfirmHide.setOnClickListener(this);



        binding.editOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        binding.editNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        binding.editConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        binding.editConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                String txtName = binding.editConfirmPassword.getText().toString();
                if (txtName.length() > 0) {
                    binding.changePassBtn.setEnabled(true);
                    binding.changePassBtn.setClickable(true);
                } else {
                    binding.changePassBtn.setEnabled(false);
                    binding.changePassBtn.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    // this will check the validations like none of the field can be the empty
    public boolean checkValidation() {

        String o_password = binding.editOldPassword.getText().toString();
        String n_password = binding.editNewPassword.getText().toString();
        String v_password = binding.editConfirmPassword.getText().toString();

        if (o_password.isEmpty()) {
            binding.editOldPassword.setError(getString(R.string.enter_valid_old_password));
            binding.editOldPassword.setFocusable(true);
            return false;
        }


        if (TextUtils.isEmpty(n_password)) {
            binding.editNewPassword.setError(getString(R.string.enter_valid_new_password));
            binding.editNewPassword.setFocusable(true);
            return false;
        }

        if (n_password.length() <= 5 || n_password.length() >= 12) {
            binding.editNewPassword.setError(getString(R.string.valid_password_length));
            binding.editNewPassword.setFocusable(true);
            return false;
        }


        if (n_password.equalsIgnoreCase(o_password)) {
            binding.editNewPassword.setError(getString(R.string.your_password_must_be_different_from_old));
            binding.editNewPassword.setFocusable(true);
            return false;
        }


        if (v_password.isEmpty()) {
            binding.editConfirmPassword.setError(getString(R.string.enter_valid_verify_password));
            binding.editConfirmPassword.setFocusable(true);
            return false;
        }

        if (!v_password.equals(n_password)) {
            binding.editConfirmPassword.setError(getString(R.string.password_not_match));
            binding.editConfirmPassword.setFocusable(true);
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goBack:
                ChangePasswordActivity.super.onBackPressed();
                break;


            case R.id.changePassBtn:
                if (checkValidation()) {
                    reAuthunticate();
                }
                break;
            case R.id.ll_old_hide: {
                if (oldCheck) {
                    binding.editOldPassword.setTransformationMethod(null);
                    binding.oldPasswordHide.setImageDrawable(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.ic_show));
                    oldCheck = false;
                    binding.editOldPassword.setSelection(binding.editOldPassword.length());
                } else {
                    binding.editOldPassword.setTransformationMethod(new PasswordTransformationMethod());
                    binding.oldPasswordHide.setImageDrawable(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.ic_hide));
                    oldCheck = true;
                    binding.editOldPassword.setSelection(binding.editOldPassword.length());
                }
            }
            break;
            case R.id.ll_new_hide: {
                if (newCheck) {
                    binding.editNewPassword.setTransformationMethod(null);
                    binding.newPasswordHide.setImageDrawable(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.ic_show));
                    newCheck = false;
                    binding.editNewPassword.setSelection(binding.editNewPassword.length());
                } else {
                    binding.editNewPassword.setTransformationMethod(new PasswordTransformationMethod());
                    binding.newPasswordHide.setImageDrawable(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.ic_hide));
                    newCheck = true;
                    binding.editNewPassword.setSelection(binding.editNewPassword.length());
                }
            }
            break;
            case R.id.ll_confirm_hide: {
                if (confirmCheck) {
                    binding.editConfirmPassword.setTransformationMethod(null);
                    binding.confirmPasswordHide.setImageDrawable(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.ic_show));
                    confirmCheck = false;
                    binding.editConfirmPassword.setSelection(binding.editConfirmPassword.length());
                } else {
                    binding.editConfirmPassword.setTransformationMethod(new PasswordTransformationMethod());
                    binding.confirmPasswordHide.setImageDrawable(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.ic_hide));
                    confirmCheck = true;
                    binding. editConfirmPassword.setSelection(binding.editConfirmPassword.length());
                }
            }
            break;

        }
    }


    private void reAuthunticate(){
        String email = user.getEmail();
        String currentPassword = binding.editOldPassword.getText().toString(); // Obtain this from the user
        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

        Functions.showLoader(this,false,false);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    changePasswordFirebase();
                } else {
                    Functions.cancelLoader();
                   Functions.showToast(ChangePasswordActivity.this,"Incorrect Old Password");
                }
            }
        });

    }

    public void changePasswordFirebase(){
        String newPassword =binding.editNewPassword.getText().toString(); // Obtain this from the user

        user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Functions.cancelLoader();
                if (task.isSuccessful()) {
                    Functions.showToast(ChangePasswordActivity.this,"Password change successfully");
                    finish();
                } else {

                    Functions.showToast(ChangePasswordActivity.this,task.getException().getLocalizedMessage());

                }
            }
        });

    }


}