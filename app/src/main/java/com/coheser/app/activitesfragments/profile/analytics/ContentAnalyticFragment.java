package com.coheser.app.activitesfragments.profile.analytics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.videorecording.VideoRecoderActivity;
import com.coheser.app.adapters.MyVideosAdapter;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.databinding.FragmentContentAnalyticBinding;
import com.coheser.app.models.HomeModel;
import com.coheser.app.simpleclasses.DataParsing;
import com.coheser.app.simpleclasses.FileUtils;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.PermissionUtils;
import com.coheser.app.simpleclasses.Variables;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


public class ContentAnalyticFragment extends Fragment {

    FragmentContentAnalyticBinding binding;
    Calendar startCalender,endCalender;
    ArrayList<HomeModel> recentDataList,trandingDataList;
    MyVideosAdapter recentAdapter,trandingAdapter;

    PermissionUtils takePermissionUtils;
    public ContentAnalyticFragment() {
    }

    public static ContentAnalyticFragment newInstance() {
        ContentAnalyticFragment fragment = new ContentAnalyticFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_content_analytic,container,false);

        startCalender= Calendar.getInstance();
        endCalender=Calendar.getInstance();

        startCalender.set(Calendar.DAY_OF_YEAR,startCalender.get(Calendar.DAY_OF_YEAR)-7);

        binding.dateRangeTxt.setText(DateOperations.INSTANCE.getDate(startCalender.getTimeInMillis(),"MMM dd") +" - "+
                DateOperations.INSTANCE.getDate(endCalender.getTimeInMillis(),"MMM dd"));

        long totalDays = DateOperations.INSTANCE.getDays(startCalender.getTime(),endCalender.getTime());
        binding.daysTxt.setText("Last "+totalDays+" days");

        recentDataList = new ArrayList<>();
        trandingDataList = new ArrayList<>();

        recentAdapter = new MyVideosAdapter(requireContext(), recentDataList, "myProfile", (view, pos, object) -> {
            HomeModel item = (HomeModel) object;
            openVideoAnalytics(item);

        });

        binding.recyclerviewPostedVideos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerviewPostedVideos.setHasFixedSize(true);
        binding.recyclerviewPostedVideos.setAdapter(recentAdapter);




        trandingAdapter = new MyVideosAdapter(requireContext(), trandingDataList, "myProfile", (view, pos, object) -> {
            HomeModel item = (HomeModel) object;
               openVideoAnalytics(item);

        });
        binding.recyclerviewTrandingVideos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerviewTrandingVideos.setHasFixedSize(true);
        binding.recyclerviewTrandingVideos.setAdapter(trandingAdapter);

        callApiTrandingvideos();


        binding.videoPostMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DetailMsgFragment fragment = DetailMsgFragment.newInstance(binding.getRoot().getContext().getString(R.string.video_postes),binding.getRoot().getContext().getString(R.string.video_postes_msg));
                fragment.show(getChildFragmentManager(), "DetailMsgF");

            }
        });

        binding.trandingPostMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DetailMsgFragment fragment = DetailMsgFragment.newInstance(binding.getRoot().getContext().getString(R.string.tranding_postes),binding.getRoot().getContext().getString(R.string.tranding_postes_msg));
                fragment.show(getChildFragmentManager(), "DetailMsgF");

            }
        });

        binding.createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePermissionUtils=new PermissionUtils(getActivity(),mPermissionResult);
                if (takePermissionUtils.isStorageCameraRecordingPermissionGranted()) {

                    uploadNewVideo();
                }
                else
                {
                    takePermissionUtils.showStorageCameraRecordingPermissionDailog(requireContext().getString(R.string.we_need_storage_camera_recording_permission_for_make_new_video));
                }
            }
        });

        return binding.getRoot();
    }


    @SuppressLint("SuspiciousIndentation")
    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(recentDataList.isEmpty() && trandingDataList.isEmpty())
                        callApiTrandingvideos();
                }
            },200);
        }
    }


    private void callApiTrandingvideos() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("start_datetime", DateOperations.INSTANCE.getDate(startCalender.getTimeInMillis(),"yyyy-MM-dd")+" 12:00:00");
            parameters.put("end_datetime", DateOperations.INSTANCE.getDate(endCalender.getTimeInMillis(),"yyyy-MM-dd")+" 12:00:00");

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showUserVideosTrendingAndRecent, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {

                parseData(resp);
            }
        });


    }

    public void parseData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");
                JSONArray recent_array = msg.optJSONArray("Recent");
                JSONArray tranding_array = msg.optJSONArray("Trending");

                binding.totalPostTxt.setText(msg.optString("VideoCount")+" Posts");


                if (recent_array != null) {
                    ArrayList<HomeModel> temp_list = new ArrayList<>();

                    for (int i = 0; i < recent_array.length(); i++) {
                        JSONObject itemdata = recent_array.optJSONObject(i);

                        JSONObject video = itemdata.optJSONObject("Video");
                        JSONObject user = itemdata.optJSONObject("User");
                        JSONObject sound = itemdata.optJSONObject("Sound");
                        JSONObject location = itemdata.optJSONObject("Location");
                        JSONObject store = itemdata.optJSONObject("Store");
                        JSONObject videoProduct=itemdata.optJSONObject("Product");
                        JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                        JSONObject userPushNotification = user.optJSONObject("PushNotification");

                        HomeModel item = DataParsing.parseVideoData(user, sound, video,location,store, videoProduct,userPrivacy, userPushNotification);

                        temp_list.add(item);

                    }
                    recentDataList.clear();
                    recentDataList.addAll(temp_list);
                    recentAdapter.notifyDataSetChanged();
                }


                if (tranding_array != null) {
                    ArrayList<HomeModel> temp_list = new ArrayList<>();

                    for (int i = 0; i < tranding_array.length(); i++) {
                        JSONObject itemdata = tranding_array.optJSONObject(i);

                        JSONObject video = itemdata.optJSONObject("Video");
                        JSONObject user = itemdata.optJSONObject("User");
                        JSONObject sound = itemdata.optJSONObject("Sound");
                        JSONObject location = itemdata.optJSONObject("Location");
                        JSONObject store = itemdata.optJSONObject("Store");
                        JSONObject videoProduct=itemdata.optJSONObject("Product");
                        JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                        JSONObject userPushNotification = user.optJSONObject("PushNotification");

                        HomeModel item = DataParsing.parseVideoData(user, sound, video,location,store,videoProduct, userPrivacy, userPushNotification);

                        temp_list.add(item);

                    }
                    trandingDataList.clear();
                    trandingDataList.addAll(temp_list);
                    trandingAdapter.notifyDataSetChanged();
                }



            }

            if (recentDataList.isEmpty()) {
               binding.noPostData.setVisibility(View.VISIBLE);
            } else {
                binding.noPostData.setVisibility(View.GONE);
            }

            if (trandingDataList.isEmpty()) {
                binding.notrandingData.setVisibility(View.VISIBLE);
            } else {
                binding.notrandingData.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.d(Constants.tag,"Exception: "+e);
        }
    }


    private void openVideoAnalytics(HomeModel item) {

        Intent intent = new Intent(getActivity(), VideoAnalytics.class);
        intent.putExtra("model", item);
        resultCallback.launch(intent);
    }

    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                    }
                }
            });




    private ActivityResultLauncher<String[]> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    boolean allPermissionClear=true;
                    List<String> blockPermissionCheck=new ArrayList<>();
                    for (String key : result.keySet())
                    {
                        if (!(result.get(key)))
                        {
                            allPermissionClear=false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(getActivity(),key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(getActivity(),requireContext().getString(R.string.we_need_storage_camera_recording_permission_for_make_new_video));
                    }
                    else
                    if (allPermissionClear)
                    {
                        uploadNewVideo();
                    }

                }
            });

    private void uploadNewVideo() {
        FileUtils.makeDirectry(FileUtils.getAppFolder(requireContext())+Variables.APP_HIDED_FOLDER);
        FileUtils.makeDirectry(FileUtils.getAppFolder(requireContext())+Variables.DRAFT_APP_FOLDER);
        if (Functions.checkLoginUser(getActivity()))
        {
            if (Functions.isWorkManagerRunning(requireContext(),"videoUpload")) {
           // if (Functions.isMyServiceRunning(requireContext(), new UploadService().getClass())) {
                Toast.makeText(requireContext(), requireContext().getString(R.string.video_already_in_progress), Toast.LENGTH_SHORT).show();
            } else {
                boolean isOpenGLSupported = Functions.isOpenGLVersionSupported(requireContext(), 0x00030001);
                if (isOpenGLSupported) {
                    Intent intent = new Intent(requireContext(), VideoRecoderActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                } else {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.your_device_opengl_verison_is_not_compatible_to_use_this_feature), Toast.LENGTH_SHORT).show();
                }

            }
        }
    }



}