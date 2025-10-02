package com.coheser.app.activitesfragments.profile.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.WebviewActivity;
import com.coheser.app.databinding.FragmentShowLocationPermissionBinding;
import com.coheser.app.interfaces.FragmentCallBack;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ShowLocationPermissionFragment extends BottomSheetDialogFragment {

    FragmentShowLocationPermissionBinding binding;
    FragmentCallBack callback;


    public ShowLocationPermissionFragment(FragmentCallBack callback) {
        this.callback = callback;
    }

    public ShowLocationPermissionFragment() {
    }

    public static ShowLocationPermissionFragment newInstance(FragmentCallBack callback) {
        ShowLocationPermissionFragment fragment = new ShowLocationPermissionFragment(callback);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_show_location_permission, container, false);
        initControl();
        actionControl();
        return binding.getRoot();
    }

    private void initControl() {

    }

    private void actionControl() {
        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        binding.tabLearnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWebUrl(binding.getRoot().getContext().getString(R.string.privacy_policy), Constants.privacy_policy);
            }
        });
        binding.btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isShow", true);
                callback.onResponce(bundle);
                dismiss();
            }
        });
    }


    public void openWebUrl(String title, String url) {
        Intent intent = new Intent(binding.getRoot().getContext(), WebviewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }
}