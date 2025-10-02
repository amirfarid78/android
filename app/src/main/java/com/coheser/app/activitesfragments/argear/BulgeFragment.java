package com.coheser.app.activitesfragments.argear;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.coheser.app.R;
import com.coheser.app.activitesfragments.videorecording.VideoRecoderActivity;
import com.coheser.app.activitesfragments.videorecording.VideoRecoderDuetActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class BulgeFragment extends BottomSheetDialogFragment implements View.OnClickListener {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bulge, container, false);

        rootView.findViewById(R.id.close_bulge_button).setOnClickListener(this);
        rootView.findViewById(R.id.clear_bulge_button).setOnClickListener(this);
        rootView.findViewById(R.id.bulge_fun1_button).setOnClickListener(this);
        rootView.findViewById(R.id.bulge_fun2_button).setOnClickListener(this);
        rootView.findViewById(R.id.bulge_fun3_button).setOnClickListener(this);
        rootView.findViewById(R.id.bulge_fun4_button).setOnClickListener(this);
        rootView.findViewById(R.id.bulge_fun5_button).setOnClickListener(this);
        rootView.findViewById(R.id.bulge_fun6_button).setOnClickListener(this);

        return rootView;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_bulge_button:
                dismiss();
                break;
            case R.id.clear_bulge_button: {
                if (getActivity() instanceof VideoRecoderActivity) {
                    ((VideoRecoderActivity) getActivity()).clearBulge();
                } else {
                    ((VideoRecoderDuetActivity) getActivity()).clearBulge();
                }
                dismiss();
            }
            break;
            case R.id.bulge_fun1_button:
                applyFunFilter(1);
                break;
            case R.id.bulge_fun2_button:
                applyFunFilter(2);
                break;
            case R.id.bulge_fun3_button:
                applyFunFilter(3);
                break;
            case R.id.bulge_fun4_button:
                applyFunFilter(4);
                break;
            case R.id.bulge_fun5_button:
                applyFunFilter(5);
                break;
            case R.id.bulge_fun6_button:
                applyFunFilter(6);
                break;

        }
    }

    private void applyFunFilter(int type) {
        if (getActivity() instanceof VideoRecoderActivity) {
            ((VideoRecoderActivity) getActivity()).setBulgeFunType(type);
        } else {
            ((VideoRecoderDuetActivity) getActivity()).setBulgeFunType(type);
        }
        dismiss();
    }
}
