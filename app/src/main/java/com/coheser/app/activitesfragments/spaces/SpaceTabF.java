package com.coheser.app.activitesfragments.spaces;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.profile.ReportTypeActivity;
import com.coheser.app.activitesfragments.spaces.adapters.MainHomeAdapter;
import com.coheser.app.activitesfragments.spaces.models.HomeUserModel;
import com.coheser.app.activitesfragments.spaces.models.RoomModel;
import com.coheser.app.activitesfragments.spaces.models.TopicModel;
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.RoomFirebaseListener;
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.RoomFirebaseManager;
import com.coheser.app.activitesfragments.spaces.utils.RoomManager.RoomManager;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.databinding.FragmentHomeTabBinding;
import com.coheser.app.interfaces.FragmentCallBack;
import com.coheser.app.mainmenu.MainMenuActivity;
import com.coheser.app.models.UserModel;
import com.coheser.app.simpleclasses.DataParsing;
import com.coheser.app.simpleclasses.Dialogs;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.PermissionUtils;
import com.coheser.app.simpleclasses.Variables;
import com.realpacific.clickshrinkeffect.ClickShrinkUtils;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SpaceTabF extends Fragment implements View.OnClickListener{

    FragmentHomeTabBinding binding;
    PermissionUtils takePermissionUtils;
    MainHomeAdapter adapter;
    ArrayList<RoomModel> dataList=new ArrayList<>();
    DatabaseReference reference;
    int width=0;
    RoomManager roomManager;
    RoomFirebaseManager firebaseRoomManager;

    public SpaceTabF() {
    }
    public static SpaceTabF newInstance() {
        SpaceTabF fragment = new SpaceTabF();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_home_tab, container, false);
        initControl();
        return binding.getRoot();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if(menuVisible) {
          new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
              @Override
              public void run() {
                  actionControl();
                  setupRecyclerView();
                  getRoomList();
                  setupScreenData();
              }
          },200);
        }
    }

    private void setupScreenData() {
        binding.userImage.setController(Functions.frescoImageLoad(Functions.getSharedPreference(requireContext()).getString(Variables.U_PIC, ""),R.drawable.ic_user_icon,binding.userImage,true));
    }


    RoomFirebaseListener roomFirebaseListener;
    private void actionControl() {
        MainMenuActivity mainMenuActivity=(MainMenuActivity) getActivity();

        if(mainMenuActivity.roomManager==null){
            mainMenuActivity.setRoomListerner();
        }

        roomManager= mainMenuActivity.roomManager;
        firebaseRoomManager= mainMenuActivity.roomFirebaseManager;

        roomFirebaseListener=new RoomFirebaseListener() {
            @Override
            public void createRoom(Bundle bundle) {

            }

            @Override
            public void JoinedRoom(Bundle bundle) {
                getRoomList();
            }

            @Override
            public void onRoomLeave(Bundle bundle) {
                getRoomList();
            }

            @Override
            public void onRoomDelete(Bundle bundle) {
                getRoomList();
            }

            @Override
            public void onRoomUpdate(Bundle bundle) {

            }

            @Override
            public void onRoomUsersUpdate(Bundle bundle) {

            }

            @Override
            public void onMyUserUpdate(Bundle bundle) {

            }

            @Override
            public void onSpeakInvitationReceived(Bundle bundle) {

            }

            @Override
            public void onWaveUserUpdate(Bundle bundle) {
            }

        };
        if (firebaseRoomManager!=null)
        {
            firebaseRoomManager.setListerner2(roomFirebaseListener);
        }
    }


    private void initControl() {
        takePermissionUtils = new PermissionUtils(getActivity(), mPermissionResult);

        width = (int)(getResources().getDisplayMetrics().widthPixels*0.95);

        binding.tabStartRoom.setOnClickListener(this);
        ClickShrinkUtils.applyClickShrink(binding.tabStartRoom);

        reference= FirebaseDatabase.getInstance().getReference();


    }

    // its for user status get mute, speak, leave, poor connection

    boolean isRoomApiRunning;
    private void getRoomList() {
        if(!isRoomApiRunning) {

            isRoomApiRunning = true;
            JSONObject parameters = new JSONObject();
            try {
                parameters.put("user_id", Functions.getSharedPreference(getContext()).getString(Variables.U_ID,""));
            } catch (Exception e) {
                e.printStackTrace();
            }
            binding.refreshLayout.setRefreshing(true);

            VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showRooms, parameters, Functions.getHeaders(getActivity()), new Callback() {
                @Override
                public void onResponce(String resp) {
                    binding.refreshLayout.setRefreshing(false);
                    parseRoomData(resp);
                }
            });

        }
    }

    public void parseRoomData(String responce) {

        try {
            dataList.clear();

            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");

            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int i = 0; i < msgArray.length(); i++) {

                    JSONObject object = msgArray.optJSONObject(i);
                    JSONObject roomObj=object.optJSONObject("Room");
                    JSONObject topicobject=object.optJSONObject("Topic");
                    JSONArray roomMemberArray=object.optJSONArray("RoomMember");

                    RoomModel model=new RoomModel();
                    model.id = roomObj.optString("id");
                    model.adminId = roomObj.optString("user_id");
                    model.title = roomObj.optString("title");
                    model.privacyType = roomObj.optString("privacy");

                    ArrayList<TopicModel> topicList=new ArrayList<>();
                    TopicModel topicModel= DataParsing.INSTANCE.getTopicDataModel(topicobject);
                    topicList.add(topicModel);
                    model.topicModels = topicList;

                    ArrayList<HomeUserModel> userList=new ArrayList<>();
                    for (int j=0;j<roomMemberArray.length();j++)
                    {
                        JSONObject innerObj=roomMemberArray.getJSONObject(j);
                        UserModel userModel= DataParsing.getUserDataModel(innerObj.optJSONObject("User"));

                        HomeUserModel userItemModel=new HomeUserModel();
                        userItemModel.userModel = userModel;
                        userItemModel.userRoleType = innerObj.optString("moderator");
                        userList.add(userItemModel);
                    }
                    model.userList = userList;
                    dataList.add(model);
                }
            }
            adapter.notifyDataSetChanged();


        }
        catch (JSONException e) {
            Log.d(Constants.tag,"Exception : "+e);
        }
        finally {
            isRoomApiRunning=false;

            if(dataList.isEmpty()){
                binding.nodataLayout.setVisibility(View.VISIBLE);
                binding.dataLayout.setVisibility(View.GONE);
            }
            else {
                binding.nodataLayout.setVisibility(View.GONE);
                binding.dataLayout.setVisibility(View.VISIBLE);
            }

        }
    }


    private void setupRecyclerView() {
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.recyclerview.setLayoutManager(layoutManager);
        adapter =new MainHomeAdapter(requireContext(),dataList, (view, pos, object) -> {
            RoomModel itemUpdate= (RoomModel) dataList.get(pos);
            switch (view.getId())
            {
                case R.id.tabView:
                    if (takePermissionUtils.isStorageRecordingPermissionGranted()) {

                        roomManager.checkMyRoomJoinStatus("join", itemUpdate.id);

                    }
                    else {
                        takePermissionUtils.showStorageRecordingPermissionDailog(binding.getRoot().getContext().getString(R.string.we_need_voice_and_read_write_storage_permission));
                    }
                break;

                case R.id.menuBtn:
                    displayPopupWindow(view,itemUpdate);
                    break;
            }
        });
        binding.recyclerview.setAdapter(adapter);

        binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRoomList();
            }
        });
    }


    private ActivityResultLauncher<String[]> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allPermissionClear = true;

                List<String> blockPermissionCheck = new ArrayList<>();
                for (String key : result.keySet()) {
                    if (!(result.get(key))) {
                        allPermissionClear = false;
                        blockPermissionCheck.add(Functions.getPermissionStatus(getActivity(), key));
                    }
                }

                if (blockPermissionCheck.contains("blocked")) {
                    Dialogs.INSTANCE.showAlert(getActivity(),binding.getRoot().getContext().getString(R.string.permission),
                            binding.getRoot().getContext().getString(R.string.we_need_voice_and_read_write_storage_permission));

                }
                else if (allPermissionClear) {
                    Toast.makeText(getContext(), "Tap Again", Toast.LENGTH_SHORT).show();
                }

            });


    private void createRoomByUser() {

        final CreateRoomFragment fragment = new CreateRoomFragment(new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {

                    if (bundle.getString("action","").equals("genrateRoom"))
                    {
                        roomManager.selectedInviteFriends= (ArrayList<UserModel>) bundle.getSerializable("selectedFriends");
                        roomManager.selectedTopics= (ArrayList<TopicModel>) bundle.getSerializable("topics");
                        roomManager.roomName=bundle.getString("roomName");
                        roomManager.privacyType=bundle.getString("privacyType");

                        roomManager.checkMyRoomJoinStatus("create","");

                    }


            }
        });

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top,
                R.anim.in_from_top, R.anim.out_from_bottom);
        ft.replace(R.id.mainMenuFragment, fragment, "CreateRoomF")
                .addToBackStack("CreateRoomF").commit();


    }

    PopupWindow popup;
    private void displayPopupWindow(View anchorView, RoomModel model) {
        popup = new PopupWindow(requireContext());
        View layout = LayoutInflater.from(requireContext()).inflate(R.layout.item_menu_popup_option, null);

        TextView report=layout.findViewById(R.id.report);
        popup.setContentView(layout);

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
                openRoomReport(model);
            }
        });


        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setOutsideTouchable(true);

        String language=Functions.getSharedPreference(getActivity())
                .getString(Variables.APP_LANGUAGE_CODE,"");
        if(language.equals("ar")){
            popup.showAsDropDown(anchorView, anchorView.getWidth(),anchorView.getHeight()
                    - (Functions.convertDpToPx(getActivity(),40)));
        }else {
            popup.showAsDropDown(anchorView, 0,anchorView.getHeight()
                    - (Functions.convertDpToPx(getActivity(),35)));
        }

        Functions.printLog(Constants.tag,"anchorView.getWidth()"+anchorView.getWidth());
        Functions.printLog(Constants.tag,"anchorView.getHeight()"+anchorView.getHeight());

    }


    public void openRoomReport(RoomModel roomModel) {
        Intent intent = new Intent(getActivity(), ReportTypeActivity.class);
        intent.putExtra("id", roomModel.id);
        intent.putExtra("type", "room");
        intent.putExtra("isFrom", false);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }

    private void startRoom() {
        if (takePermissionUtils.isStorageRecordingPermissionGranted()) {

            createRoomByUser();
        }
        else {
            takePermissionUtils.showStorageRecordingPermissionDailog(binding.getRoot().getContext().getString(R.string.we_need_voice_and_read_write_storage_permission));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {

            case R.id.tabStartRoom: {
                startRoom();
            }
            break;


        }
    }

    @Override
    public void onDetach() {
        if(firebaseRoomManager!=null)
            firebaseRoomManager.setListerner2(null);

        super.onDetach();

    }


}