package com.coheser.app.activitesfragments.profile.videopromotion;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.WebviewActivity;
import com.coheser.app.activitesfragments.walletandwithdraw.MyWallet;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.databinding.FragmentVideoPromoteResultBinding;
import com.coheser.app.models.UserModel;
import com.coheser.app.simpleclasses.DataParsing;
import com.coheser.app.simpleclasses.DateOprations;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.klinker.android.link_builder.TouchableMovementMethod;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class VideoPromoteResultFragment extends Fragment {

    List<Link> links = new ArrayList<>();
    FragmentVideoPromoteResultBinding binding;
    long myWalletCoins = 0;
    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow", false)) {
                            initControl();
                        }
                    }
                }
            });

    public VideoPromoteResultFragment() {
    }

    public static VideoPromoteResultFragment newInstance() {
        VideoPromoteResultFragment fragment = new VideoPromoteResultFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_promote_result, container, false);
        initControl();
        actionControl();
        return binding.getRoot();
    }

    private void actionControl() {
        binding.btnPromotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.btnPromotion.getText().toString().equals(
                        binding.getRoot().getContext().getString(R.string.recharge))) {
                    Intent intent = new Intent(binding.getRoot().getContext(), MyWallet.class);
                    startActivity(intent);
                    resultCallback.launch(intent);
                } else {
                    int counts = VideoPromoteStepsActivity.adapter.getItemCount();
                    VideoPromoteStepsActivity.progressBar.setProgress((counts), true);

                    requestToPromoteUserVideo();
                }

            }
        });
    }

    public void requestToPromoteUserVideo() {
        JSONObject params = new JSONObject();
        try {
            params.put("user_id", Functions.getSharedPreference(binding.getRoot().getContext()).getString(Variables.U_ID, ""));
            params.put("video_id", VideoPromoteStepsActivity.requestPromotionModel.getSelectedVideo().video_id);
            params.put("destination", getdestination(VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal()));
            params.put("audience_id", getAudience(VideoPromoteStepsActivity.requestPromotionModel.getAudienceType()));
            params.put("start_datetime", DateOprations.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
            params.put("end_datetime", DateOprations.getCurrentDate("yyyy-MM-dd HH:mm:ss", VideoPromoteStepsActivity.requestPromotionModel.getSelectedDuration()));
            params.put("coin", getEstimatedCoins(VideoPromoteStepsActivity.requestPromotionModel.getSelectedBudget(), VideoPromoteStepsActivity.requestPromotionModel.getSelectedDuration()));
            params.put("total_reach", getEstimatedReach(VideoPromoteStepsActivity.requestPromotionModel.getSelectedBudget(), VideoPromoteStepsActivity.requestPromotionModel.getSelectedDuration()));
            if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 2) {
                params.put("action_button", getActionButton(VideoPromoteStepsActivity.requestPromotionModel.getWebsiteLandingPage()));
                params.put("website_url", VideoPromoteStepsActivity.requestPromotionModel.getWebsiteULR());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Functions.showLoader(getActivity(), false, false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.addPromotion, params, Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(), resp);
                Functions.cancelLoader();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code != null && code.equals("200")) {
                        JSONObject msgObj = jsonObject.getJSONObject("msg");
                        UserModel userDetailModel = DataParsing.getUserDataModel(msgObj.optJSONObject("User"));
                        SharedPreferences.Editor editor = Functions.getSharedPreference(binding.getRoot().getContext()).edit();
                        editor.putString(Variables.U_WALLET, "" + userDetailModel.wallet);
                        editor.commit();

                        getActivity().finish();
                    }

                } catch (Exception e) {
                    Log.d(Constants.tag, "Exception: " + e);
                }


            }
        });

    }

    private String getAudience(int audienceType) {
        if (audienceType == 1) {
            return "0";
        } else if (audienceType == 2) {
            return VideoPromoteStepsActivity.requestPromotionModel.getAudienceId();
        } else {
            return VideoPromoteStepsActivity.requestPromotionModel.getSelectAudience().getId();
        }
    }

    private String getActionButton(int websiteLandingPage) {
        if (websiteLandingPage == 2) {
            return "Shop now";
        } else if (websiteLandingPage == 3) {
            return "Sign up";
        } else if (websiteLandingPage == 4) {
            return "Contact us";
        } else if (websiteLandingPage == 5) {
            return "Apply now";
        } else if (websiteLandingPage == 6) {
            return "Book now";
        } else {
            return "Learn more";
        }
    }

    private String getEstimatedReach(int coin, int days) {
        long total = (long) coin * days;

        long totalViews = 0;
        if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 1) {
            Functions.printLog(Constants.tag, "cost:" + total);
            Functions.printLog(Constants.tag, "getVideoViewsStat:" + VideoPromoteStepsActivity.requestPromotionModel.getVideoViewsStat());
            totalViews = total * VideoPromoteStepsActivity.requestPromotionModel.getVideoViewsStat();
        } else if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 2) {
            totalViews = total * VideoPromoteStepsActivity.requestPromotionModel.getWebsiteStat();
        } else if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 3) {
            totalViews = total * VideoPromoteStepsActivity.requestPromotionModel.getFollowerStat();
        }
        return "" + totalViews;
    }

    private String getEstimatedCoins(int coin, int days) {
        long total = (long) coin * days;
        return "" + total;
    }

    private String getdestination(int promoteGoal) {
        if (promoteGoal == 2) {
            return "website";
        } else if (promoteGoal == 3) {
            return "follower";
        } else {
            return "views";
        }
    }

    private void initControl() {
        myWalletCoins = Long.parseLong(Functions.getSharedPreference(binding.getRoot().getContext()).getString(Variables.U_WALLET, "0"));

        setupScreenData();
        setTermsAndConditionLink();
    }

    private void setupScreenData() {
        binding.ivPost.setController(Functions.frescoImageLoad(
                VideoPromoteStepsActivity.requestPromotionModel.getSelectedVideo().getThum(),
                R.drawable.image_placeholder, binding.ivPost, false));

        updateCalculation(VideoPromoteStepsActivity.requestPromotionModel.getSelectedBudget(),
                VideoPromoteStepsActivity.requestPromotionModel.getSelectedDuration());

        if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 1) {
            binding.tvGoal.setText(binding.getRoot().getContext().getString(R.string.more_video_views));
        } else if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 2) {
            binding.tvGoal.setText(binding.getRoot().getContext().getString(R.string.more_website_visit));
        } else if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 3) {
            binding.tvGoal.setText(binding.getRoot().getContext().getString(R.string.more_followers));
        }

        if (VideoPromoteStepsActivity.requestPromotionModel.getAudienceType() == 1) {
            binding.tvAudience.setText(binding.getRoot().getContext().getString(R.string.automatic_app_chooses_for_you_));
        } else if (VideoPromoteStepsActivity.requestPromotionModel.getAudienceType() == 2) {
            binding.tvAudience.setText(binding.getRoot().getContext().getString(R.string.custom));
        } else {
            binding.tvAudience.setText(VideoPromoteStepsActivity.requestPromotionModel.getSelectAudience().getName());
        }


    }


    private void updateCalculation(int coin, int days) {
        long total = (long) coin * days;
        String dayTitle;
        if (days < 2) {
            dayTitle = binding.getRoot().getContext().getString(R.string.day);
        } else {
            dayTitle = binding.getRoot().getContext().getString(R.string.days);
        }
        binding.tvTotalCost.setText("" + total);
        binding.tvSubtotal.setText("" + total);
        binding.tvGrossTotal.setText("" + total);
        binding.tvTotal.setText("" + total);
        binding.tvDayTotalCost.setText(" | " + days + " " + dayTitle);

        long totalViews = 0;
        if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 1) {
            totalViews = total * VideoPromoteStepsActivity.requestPromotionModel.getVideoViewsStat();
        } else if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 2) {
            totalViews = total * VideoPromoteStepsActivity.requestPromotionModel.getWebsiteStat();
        } else if (VideoPromoteStepsActivity.requestPromotionModel.getPromoteGoal() == 3) {
            totalViews = total * VideoPromoteStepsActivity.requestPromotionModel.getFollowerStat();
        }
        binding.tvTotalViews.setText(totalViews + " +");

        if (myWalletCoins > total) {
            binding.tvGrossTotalTitle.setText(binding.getRoot().getContext().getString(R.string.total));
            binding.tvTotalTitle.setText(binding.getRoot().getContext().getString(R.string.total));
            binding.btnPromotion.setText(binding.getRoot().getContext().getString(R.string.pay_and_start_promotion));
        } else {
            binding.tvGrossTotalTitle.setText(binding.getRoot().getContext().getString(R.string.insufficient_coins));
            binding.tvTotalTitle.setText(binding.getRoot().getContext().getString(R.string.insufficient_coins));
            binding.btnPromotion.setText(binding.getRoot().getContext().getString(R.string.recharge));
        }

        if (total > 0) {
            binding.btnPromotion.setEnabled(true);
            binding.btnPromotion.setClickable(true);
        } else {
            binding.btnPromotion.setEnabled(false);
            binding.btnPromotion.setClickable(false);
        }
    }


    private void setTermsAndConditionLink() {

        Link link = new Link(binding.getRoot().getContext().getString(R.string.terms_of_use));
        link.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.black));
        link.setTextColorOfHighlightedLink(ContextCompat.getColor(binding.getRoot().getContext(), R.color.darkgray));
        link.setUnderlined(true);
        link.setBold(false);
        link.setHighlightAlpha(.20f);
        link.setOnClickListener(new Link.OnClickListener() {
            @Override
            public void onClick(String clickedText) {
                openWebUrl(binding.getRoot().getContext().getString(R.string.terms_of_use), Constants.terms_conditions);
            }
        });

        Link link2 = new Link(binding.getRoot().getContext().getString(R.string.privacy_policy));
        link2.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.black));
        link2.setTextColorOfHighlightedLink(ContextCompat.getColor(binding.getRoot().getContext(), R.color.darkgray));
        link2.setUnderlined(true);
        link2.setBold(false);
        link2.setHighlightAlpha(.20f);
        link2.setOnClickListener(new Link.OnClickListener() {
            @Override
            public void onClick(String clickedText) {
                openWebUrl(binding.getRoot().getContext().getString(R.string.privacy_policy), Constants.privacy_policy);
            }
        });
        links.add(link);
        links.add(link2);
        CharSequence sequence = LinkBuilder.from(binding.getRoot().getContext(), binding.tvTermsAndCondition.getText().toString())
                .addLinks(links)
                .build();
        binding.tvTermsAndCondition.setText(sequence);
        binding.tvTermsAndCondition.setMovementMethod(TouchableMovementMethod.getInstance());
    }

    public void openWebUrl(String title, String url) {
        Intent intent = new Intent(binding.getRoot().getContext(), WebviewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

    }

}