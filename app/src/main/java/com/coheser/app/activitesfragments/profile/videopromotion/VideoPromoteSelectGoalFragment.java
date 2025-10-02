package com.coheser.app.activitesfragments.profile.videopromotion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.coheser.app.R;
import com.coheser.app.databinding.FragmentVideoPromoteSelectGoalBinding;


public class VideoPromoteSelectGoalFragment extends Fragment {


    FragmentVideoPromoteSelectGoalBinding binding;


    public VideoPromoteSelectGoalFragment() {
    }

    public static VideoPromoteSelectGoalFragment newInstance() {
        VideoPromoteSelectGoalFragment fragment = new VideoPromoteSelectGoalFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_promote_select_goal, container, false);
        initControl();
        actionControl();
        return binding.getRoot();
    }

    private void actionControl() {
        binding.tabVideoViews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSelection(1);
            }
        });

        binding.tabMoreWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSelection(2);
            }
        });

        binding.tabMoreFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSelection(3);
            }
        });

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 1) {
                    int counts = VideoPromoteStepsActivity.adapter.getItemCount();
                    VideoPromoteStepsActivity.progressBar.setMax(4);

                    if (counts > (counts + 1)) {
                        VideoPromoteStepsActivity.viewpager.setCurrentItem((counts + 1), true);
                        VideoPromoteStepsActivity.progressBar.setProgress((counts), true);
                    } else {
                        VideoPromoteStepsActivity.adapter.addFrag(VideoPromoteSelectAudienceFragment.newInstance());
                        VideoPromoteStepsActivity.adapter.notifyItemInserted((counts + 1));
                        VideoPromoteStepsActivity.viewpager.setCurrentItem((counts + 1), true);
                        VideoPromoteStepsActivity.progressBar.setProgress((counts), true);
                    }

                } else if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 2) {
                    int counts = VideoPromoteStepsActivity.adapter.getItemCount();
                    VideoPromoteStepsActivity.progressBar.setMax(5);

                    if (counts > (counts + 1)) {
                        VideoPromoteStepsActivity.viewpager.setCurrentItem((counts + 1), true);
                        VideoPromoteStepsActivity.progressBar.setProgress((counts), true);
                    } else {
                        VideoPromoteStepsActivity.adapter.addFrag(VideoPromoteWebsiteFragment.newInstance());
                        VideoPromoteStepsActivity.adapter.notifyItemInserted((counts + 1));
                        VideoPromoteStepsActivity.viewpager.setCurrentItem((counts + 1), true);
                        VideoPromoteStepsActivity.progressBar.setProgress((counts), true);
                    }

                } else if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 3) {
                    int counts = VideoPromoteStepsActivity.adapter.getItemCount();
                    VideoPromoteStepsActivity.progressBar.setMax(4);

                    if (counts > (counts + 1)) {
                        VideoPromoteStepsActivity.viewpager.setCurrentItem((counts + 1), true);
                        VideoPromoteStepsActivity.progressBar.setProgress((counts), true);
                    } else {
                        VideoPromoteStepsActivity.adapter.addFrag(VideoPromoteSelectAudienceFragment.newInstance());
                        VideoPromoteStepsActivity.adapter.notifyItemInserted((counts + 1));
                        VideoPromoteStepsActivity.viewpager.setCurrentItem((counts + 1), true);
                        VideoPromoteStepsActivity.progressBar.setProgress((counts), true);
                    }
                }
            }
        });
    }

    private void initControl() {

        updateSelection(0);
    }

    private void updateSelection(int select) {
        VideoPromoteStepsActivity.requestPromotionModel.setPromoteGoal(select);
        switch (select) {
            case 1: {
                binding.ivVideoViewsSelection.setImageDrawable(
                        ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.ic_circle_selection));
                binding.ivMoreWebsiteSelection.setImageDrawable(
                        ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.ic_un_selected));
                binding.ivMoreFollowersSelection.setImageDrawable(
                        ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.ic_un_selected));
                binding.btnNext.setEnabled(true);
                binding.btnNext.setClickable(true);
            }
            break;
            case 2: {
                binding.ivVideoViewsSelection.setImageDrawable(
                        ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.ic_un_selected));
                binding.ivMoreWebsiteSelection.setImageDrawable(
                        ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.ic_circle_selection));
                binding.ivMoreFollowersSelection.setImageDrawable(
                        ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.ic_un_selected));
                binding.btnNext.setEnabled(true);
                binding.btnNext.setClickable(true);
            }
            break;
            case 3: {
                binding.ivVideoViewsSelection.setImageDrawable(
                        ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.ic_un_selected));
                binding.ivMoreWebsiteSelection.setImageDrawable(
                        ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.ic_un_selected));
                binding.ivMoreFollowersSelection.setImageDrawable(
                        ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.ic_circle_selection));
                binding.btnNext.setEnabled(true);
                binding.btnNext.setClickable(true);
            }
            break;
            default: {
                binding.btnNext.setEnabled(false);
                binding.btnNext.setClickable(false);
            }
            break;
        }
    }
}