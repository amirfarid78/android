package com.coheser.app.activitesfragments.profile.videopromotion;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.adapters.PromotionAudiencesAdapter;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.databinding.FragmentVideoPromoteSelectAudienceBinding;
import com.coheser.app.models.PromotionAudiencesModel;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class VideoPromoteSelectAudienceFragment extends Fragment {

    FragmentVideoPromoteSelectAudienceBinding binding;
    ArrayList<PromotionAudiencesModel> dataList = new ArrayList<>();
    PromotionAudiencesAdapter adapter;
    PromotionAudiencesModel itemUpdate;


    public VideoPromoteSelectAudienceFragment() {
    }

    public static VideoPromoteSelectAudienceFragment newInstance() {
        VideoPromoteSelectAudienceFragment fragment = new VideoPromoteSelectAudienceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_promote_select_audience, container, false);
        initControl();
        actionControl();
        return binding.getRoot();
    }

    private void actionControl() {
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (VideoPromoteStepsActivity.requestPromotionModel.getAudienceType() == 2) {

                    int counts = VideoPromoteStepsActivity.adapter.getItemCount();
                    if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 2) {
                        VideoPromoteStepsActivity.progressBar.setMax(6);
                    } else {
                        VideoPromoteStepsActivity.progressBar.setMax(5);
                    }

                    if (VideoPromoteStepsActivity.requestPromotionModel.getSelectedVideo() == null) {
                        int progressCount = VideoPromoteStepsActivity.progressBar.getMax() + 1;
                        VideoPromoteStepsActivity.progressBar.setMax(progressCount);
                    }

                    if (counts > (counts + 1)) {
                        VideoPromoteStepsActivity.viewpager.setCurrentItem((counts + 1), true);
                        VideoPromoteStepsActivity.progressBar.setProgress((counts), true);
                    } else {
                        VideoPromoteStepsActivity.adapter.addFrag(VideoPromoteCustomFragment.newInstance());
                        VideoPromoteStepsActivity.adapter.notifyItemInserted((counts + 1));
                        VideoPromoteStepsActivity.viewpager.setCurrentItem((counts + 1), true);
                        VideoPromoteStepsActivity.progressBar.setProgress((counts), true);
                    }
                } else {
                    int counts = VideoPromoteStepsActivity.adapter.getItemCount();
                    VideoPromoteStepsActivity.progressBar.setMax(4);

                    if (VideoPromoteStepsActivity.requestPromotionModel.getSelectedVideo() == null) {
                        int progressCount = VideoPromoteStepsActivity.progressBar.getMax() + 1;
                        VideoPromoteStepsActivity.progressBar.setMax(progressCount);
                    }

                    if (counts > (counts + 1)) {
                        VideoPromoteStepsActivity.viewpager.setCurrentItem((counts + 1), true);
                        VideoPromoteStepsActivity.progressBar.setProgress((counts), true);
                    } else {
                        VideoPromoteStepsActivity.adapter.addFrag(VideoPromoteStepSelectBudgetFragment.newInstance());
                        VideoPromoteStepsActivity.adapter.notifyItemInserted((counts + 1));
                        VideoPromoteStepsActivity.viewpager.setCurrentItem((counts + 1), true);
                        VideoPromoteStepsActivity.progressBar.setProgress((counts), true);
                    }
                }
            }
        });
    }

    private void initControl() {
        setupRecyclerView();
        callApiMyAudience();
    }

    private void callApiMyAudience() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(binding.getRoot().getContext()).getString(Variables.U_ID, ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Functions.showLoader(getActivity(), false, false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showAudiences, parameters, Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(), resp);
                Functions.cancelLoader();
                parseData(resp);
            }
        });
    }

    public void parseData(String responce) {
        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                dataList.clear();

                PromotionAudiencesModel automaticModel = new PromotionAudiencesModel();
                automaticModel.setId("");
                automaticModel.setName(binding.getRoot().getContext().getString(R.string.automatic_app_chooses_for_you_));
                automaticModel.setMin_age("");
                automaticModel.setMax_age("");
                automaticModel.setGender("");
                automaticModel.setSelected(false);
                dataList.add(automaticModel);

                PromotionAudiencesModel customModel = new PromotionAudiencesModel();
                customModel.setId("0");
                customModel.setName(binding.getRoot().getContext().getString(R.string.custom));
                customModel.setMin_age("");
                customModel.setMax_age("");
                customModel.setGender("");
                customModel.setSelected(false);
                dataList.add(customModel);

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i).getJSONObject("Audience");

                    PromotionAudiencesModel model = new PromotionAudiencesModel();
                    model.setId(itemdata.optString("id"));
                    model.setName(itemdata.optString("name"));
                    model.setMin_age(itemdata.optString("min_age"));
                    model.setMax_age(itemdata.optString("max_age"));
                    model.setGender(itemdata.optString("gender"));
                    model.setSelected(false);

                    dataList.add(model);
                }
                adapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            Log.d(Constants.tag, "Exception: " + e);
        }
    }


    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(binding.getRoot().getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.recylerview.setLayoutManager(layoutManager);
        binding.recylerview.setHasFixedSize(true);
        adapter = new PromotionAudiencesAdapter(binding.getRoot().getContext(), dataList, (view, pos, object) -> {
            itemUpdate = dataList.get(pos);
            for (int i = 0; i < dataList.size(); i++) {
                PromotionAudiencesModel item = dataList.get(i);
                item.setSelected(false);
                dataList.set(i, item);
            }
            itemUpdate.setSelected(true);
            dataList.set(pos, itemUpdate);
            UpdateButtonStatus();
            adapter.notifyDataSetChanged();


        });
        binding.recylerview.setAdapter(adapter);
    }

    private void UpdateButtonStatus() {
        if (itemUpdate.getId().equals("")) {
            VideoPromoteStepsActivity.requestPromotionModel.setAudienceType(1);
            VideoPromoteStepsActivity.requestPromotionModel.setSelectAudience(null);
            binding.btnNext.setEnabled(true);
            binding.btnNext.setClickable(true);
        } else if (itemUpdate.getId().equals("0")) {
            VideoPromoteStepsActivity.requestPromotionModel.setAudienceType(2);
            VideoPromoteStepsActivity.requestPromotionModel.setSelectAudience(null);
            binding.btnNext.setEnabled(true);
            binding.btnNext.setClickable(true);
        } else {
            VideoPromoteStepsActivity.requestPromotionModel.setAudienceType(3);
            VideoPromoteStepsActivity.requestPromotionModel.setSelectAudience(itemUpdate);
            binding.btnNext.setEnabled(true);
            binding.btnNext.setClickable(true);
        }
    }

}