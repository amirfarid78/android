package com.coheser.app.activitesfragments.payment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.databinding.FragmentDeleteFragmantDialogBinding;
import com.coheser.app.interfaces.FragmentCallBack;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONObject;

public class DeleteFragmantDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    FragmentDeleteFragmantDialogBinding binding;
    FragmentCallBack fragmentCallBack;
    Bundle bundle;
    String id, userId;

    public DeleteFragmantDialog(FragmentCallBack fragmentCallBack) {
        this.fragmentCallBack = fragmentCallBack;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDeleteFragmantDialogBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        bundle = getArguments();
        if (bundle != null) {
            id = bundle.getString("id");
        }
        userId = Functions.getSharedPreference(getActivity()).getString(Variables.U_ID, "");
        initViews();
        return view;
    }

    private void initViews() {
        binding.deletePaymentBtn.setOnClickListener(this);
        binding.keepCard.setOnClickListener(this);
        binding.backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        if (vId == R.id.delete_payment_btn) {
            callApiForDelete();
        } else if (vId == R.id.keep_card) {
            dismiss();
        } else if (vId == R.id.backBtn) {
            dismiss();
        }

    }

    private void callApiForDelete() {
        JSONObject params = new JSONObject();

        try {
            params.put("user_id", userId);
            params.put("id", id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(getActivity(), false, false);

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.deletePaymentCard, params, Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.cancelLoader();

                if (resp != null) {
                    Functions.printLog(Constants.tag, "resp at callApiForDelete : " + resp);
                    try {
                        JSONObject respobj = new JSONObject(resp);
                        if (respobj.getString("code").equals("200")) {

                            String paymentMethodId = Functions.getSharedPreference(getContext()).getString(Variables.payment_id, "0");
                            if(paymentMethodId.equals(id)){
                                Functions.getSharedPreference(getContext()).edit().putString(Variables.last_4, "").apply();
                                Functions.getSharedPreference(getContext()).edit().putString(Variables.cardExpireDate, "").apply();
                                Functions.getSharedPreference(getContext()).edit().putString(Variables.payment_id, "0").apply();
                            }

                            if (fragmentCallBack != null) {
                                fragmentCallBack.onResponce(new Bundle());
                                dismiss();
                            }

                        } else {
                            if (fragmentCallBack != null) {
                                fragmentCallBack.onResponce(new Bundle());
                                dismiss();
                            }
                        }
                    } catch (Exception e) {
                        Functions.printLog(Constants.tag, "Exception: " + e);
                    }
                }


            }


        });


    }
}