package com.coheser.app.activitesfragments.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coheser.app.R;
import com.coheser.app.adapters.OptionSelectionAdapter;
import com.coheser.app.databinding.FragmentOptionSelectionSheetBinding;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.interfaces.FragmentCallBack;
import com.coheser.app.models.OptionSelectionModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;


public class OptionSelectionSheetFragment extends BottomSheetDialogFragment {


    FragmentCallBack callback;
    FragmentOptionSelectionSheetBinding binding;
    ArrayList<OptionSelectionModel> dataList;
    OptionSelectionAdapter adapter;

    public OptionSelectionSheetFragment() {
    }

    public OptionSelectionSheetFragment(ArrayList<OptionSelectionModel> dataList, FragmentCallBack callback) {
        this.dataList = dataList;
        this.callback = callback;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_option_selection_sheet, container, false);
        initControl();
        actionControl();
        return binding.getRoot();
    }

    private void initControl() {
        setupAdapter();
    }

    private void setupAdapter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(binding.getRoot().getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.recyclerview.setLayoutManager(layoutManager);
        adapter = new OptionSelectionAdapter(dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isShow", true);
                bundle.putInt("position", pos);
                callback.onResponce(bundle);
                dismiss();
            }
        });
        binding.recyclerview.setAdapter(adapter);

    }

    private void actionControl() {
        binding.tabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}