package com.coheser.app.activitesfragments.profile.videopromotion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.databinding.FragmentVideoPromoteSelectBudgetBinding;
import com.coheser.app.simpleclasses.Functions;

import java.util.Locale;

public class VideoPromoteStepSelectBudgetFragment extends Fragment {

    FragmentVideoPromoteSelectBudgetBinding binding;

    public VideoPromoteStepSelectBudgetFragment() {
    }

    public static VideoPromoteStepSelectBudgetFragment newInstance() {
        VideoPromoteStepSelectBudgetFragment fragment = new VideoPromoteStepSelectBudgetFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_promote_select_budget, container, false);
        initControl();
        actionControl();
        return binding.getRoot();
    }

    private void actionControl() {
        binding.seekbarBudget.setMaxValue(1000);
        binding.seekbarBudget.setMinValue(0);
        binding.seekbarBudget.setActivated(true);
        binding.seekbarBudget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateBudgetInfoPosition(progress);
                        }
                    });
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateCalculation(binding.seekbarBudget.getProgress(), binding.seekbarDuration.getProgress());
            }
        });

        binding.seekbarDuration.setMaxValue(7);
        binding.seekbarDuration.setMinValue(0);
        binding.seekbarDuration.setActivated(true);
        binding.seekbarDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateDurationInfoPosition(progress);
                        }
                    });
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateCalculation(binding.seekbarBudget.getProgress(), binding.seekbarDuration.getProgress());
            }
        });

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoPromoteStepsActivity.requestPromotionModel.setSelectedBudget(binding.seekbarBudget.getProgress());
                VideoPromoteStepsActivity.requestPromotionModel.setSelectedDuration(binding.seekbarDuration.getProgress());

                if (VideoPromoteStepsActivity.requestPromotionModel.getSelectedVideo() == null) {
                    int counts = VideoPromoteStepsActivity.adapter.getItemCount();
                    if (counts > (counts + 1)) {
                        VideoPromoteStepsActivity.viewpager.setCurrentItem((counts + 1), true);
                        VideoPromoteStepsActivity.progressBar.setProgress((counts), true);
                    } else {
                        VideoPromoteStepsActivity.adapter.addFrag(VideoPromoteVideosFragment.newInstance());
                        VideoPromoteStepsActivity.adapter.notifyItemInserted((counts + 1));
                        VideoPromoteStepsActivity.viewpager.setCurrentItem((counts + 1), true);
                        VideoPromoteStepsActivity.progressBar.setProgress((counts), true);
                    }
                } else {
                    int counts = VideoPromoteStepsActivity.adapter.getItemCount();
                    if (counts > (counts + 1)) {
                        VideoPromoteStepsActivity.viewpager.setCurrentItem((counts + 1), true);
                        VideoPromoteStepsActivity.progressBar.setProgress((counts), true);
                    } else {
                        VideoPromoteStepsActivity.adapter.addFrag(VideoPromoteResultFragment.newInstance());
                        VideoPromoteStepsActivity.adapter.notifyItemInserted((counts + 1));
                        VideoPromoteStepsActivity.viewpager.setCurrentItem((counts + 1), true);
                        VideoPromoteStepsActivity.progressBar.setProgress((counts), true);
                    }
                }

            }
        });
    }

    private void updateBudgetInfoPosition(int progress) {
        int max = binding.seekbarBudget.getMaxValue() - binding.seekbarBudget.getMinValue();
        binding.tvBudgetInfo.setText(" " + String.format(Locale.ENGLISH, "%d", progress) + " " +
                binding.getRoot().getContext().getString(R.string.per_day));
        int paddingLeft = 0;
        int paddingRight = 0;
        int offset = -5;
        int viewWidth = binding.tabBudgetInfo.getWidth();
        int x;
        if (Functions.isRTL(binding.tabBudget)) {
            x = (int) ((float) (binding.seekbarBudget.getLeft() - binding.seekbarBudget.getRight() - paddingLeft - paddingRight - viewWidth - 2 * offset)
                    * (progress - binding.seekbarBudget.getMinValue()) / max)
                    + binding.seekbarBudget.getRight() + paddingLeft + offset;
        } else {
            x = (int) ((float) (binding.seekbarBudget.getRight() - binding.seekbarBudget.getLeft() - paddingLeft - paddingRight - viewWidth - 2 * offset)
                    * (progress - binding.seekbarBudget.getMinValue()) / max)
                    + binding.seekbarBudget.getLeft() + paddingLeft + offset;
        }
        binding.tabBudgetInfo.setX(x);
    }


    private void updateDurationInfoPosition(int progress) {
        int max = binding.seekbarDuration.getMaxValue() - binding.seekbarDuration.getMinValue();
        if (progress > 1) {
            binding.tvDurationInfo.setText(String.format(Locale.ENGLISH, "%d", progress) + " " + binding.getRoot().getContext().getString(R.string.days));
        } else {
            binding.tvDurationInfo.setText(String.format(Locale.ENGLISH, "%d", progress) + " " + binding.getRoot().getContext().getString(R.string.day));
        }

        int paddingLeft = 0;
        int paddingRight = 0;
        int offset = -5;
        int viewWidth = binding.tabDurationInfo.getWidth();
        int x;
        if (Functions.isRTL(binding.tabDurationInfo)) {
            x = (int) ((float) (binding.seekbarDuration.getLeft() - binding.seekbarDuration.getRight() - paddingLeft - paddingRight - viewWidth - 2 * offset)
                    * (progress - binding.seekbarDuration.getMinValue()) / max)
                    + binding.seekbarDuration.getRight() + paddingLeft + offset;
        } else {
            x = (int) ((float) (binding.seekbarDuration.getRight() - binding.seekbarDuration.getLeft() - paddingLeft - paddingRight - viewWidth - 2 * offset)
                    * (progress - binding.seekbarDuration.getMinValue()) / max)
                    + binding.seekbarDuration.getLeft() + paddingLeft + offset;
        }
        binding.tabDurationInfo.setX(x);
    }


    private void initControl() {

        updateCalculation(binding.seekbarBudget.getProgress(), binding.seekbarDuration.getProgress());

    }


    private void updateCalculation(int coin, int days) {
        Functions.printLog(Constants.tag, "updateCalculation");

        long total = (long) coin * days;
        String dayTitle, costTitle;
        if (days < 2) {
            dayTitle = binding.getRoot().getContext().getString(R.string.day);
        } else {
            dayTitle = binding.getRoot().getContext().getString(R.string.days);
        }
        if (total < 2) {
            costTitle = binding.getRoot().getContext().getString(R.string.coin_spent_over);
        } else {
            costTitle = binding.getRoot().getContext().getString(R.string.coins_spent_over);
        }
        binding.tvTotalCost.setText("" + total);
        binding.tvDayTotalCost.setText(" " + costTitle + " " + days + " " + dayTitle);

        long totalViews = 0;
        if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 1) {
            Functions.printLog(Constants.tag, "cost:" + total);
            Functions.printLog(Constants.tag, "getVideoViewsStat:" + VideoPromoteStepsActivity.requestPromotionModel.getVideoViewsStat());

            totalViews = total * VideoPromoteStepsActivity.requestPromotionModel.getVideoViewsStat();
        } else if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 2) {
            Functions.printLog(Constants.tag, "cost:" + total);
            Functions.printLog(Constants.tag, "getWebsiteStat:" + VideoPromoteStepsActivity.requestPromotionModel.getWebsiteStat());

            totalViews = total * VideoPromoteStepsActivity.requestPromotionModel.getWebsiteStat();
        } else if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 3) {
            Functions.printLog(Constants.tag, "cost:" + total);
            Functions.printLog(Constants.tag, "getFollowerStat:" + VideoPromoteStepsActivity.requestPromotionModel.getFollowerStat());

            totalViews = total * VideoPromoteStepsActivity.requestPromotionModel.getFollowerStat();
        }
        binding.tvTotalViews.setText(totalViews + " +");

        if (total > 0) {
            binding.btnNext.setEnabled(true);
            binding.btnNext.setClickable(true);
        } else {
            binding.btnNext.setEnabled(false);
            binding.btnNext.setClickable(false);
        }
    }


}