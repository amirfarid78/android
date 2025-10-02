package com.coheser.app.activitesfragments.videorecording;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.coheser.app.R;
import com.coheser.app.adapters.UsersAdapter;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.models.UserModel;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.DataParsing;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;

import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class FriendsActivity extends AppCompatLocaleActivity {

    private final long DELAY = 1000; // Milliseconds
    Context context;
    String userId;
    UsersAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<UserModel> datalist;
    EditText searchEdit;
    ProgressBar pbar;
    CardView searchLayout;
    TextView titleTxt;
    SwipeRefreshLayout refreshLayout;
    int pageCount = 0;
    boolean ispostFinsh;
    ProgressBar loadMoreProgress;
    LinearLayoutManager linearLayoutManager;
    String from = "";
    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        setContentView(R.layout.activity_friends);
        context = FriendsActivity.this;


        userId = getIntent().getStringExtra("id");

        if (getIntent().hasExtra("from")) {
            from = getIntent().getStringExtra("from");
        }
        titleTxt = findViewById(R.id.title_txt);
        titleTxt.setText(from);


        datalist = new ArrayList<>();
        refreshLayout = findViewById(R.id.refreshLayout);
        searchEdit = findViewById(R.id.search_edit);
        searchLayout = findViewById(R.id.search_layout);
        pbar = findViewById(R.id.pbar);
        loadMoreProgress = findViewById(R.id.load_more_progress);
        recyclerView = findViewById(R.id.recylerview);
        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);


        callApiForGetAllfollowing(true);

        searchEdit.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(final Editable s) {
                        timer.cancel();
                        timer = new Timer();
                        timer.schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        FriendsActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                String search_txt = searchEdit.getText().toString();
                                                pageCount = 0;
                                                if (search_txt.length() > 0) {
                                                    callApiForOtherUsers();
                                                } else {
                                                    callApiForGetAllfollowing(true);
                                                }
                                            }
                                        });
                                    }
                                },
                                DELAY
                        );
                    }
                }
        );

        adapter = new UsersAdapter(context, datalist,from, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

                UserModel item1 = (UserModel) object;
                switch (view.getId()) {
                    case R.id.mainlayout:

                        item1.isSelected = !item1.isSelected;
                        adapter.notifyDataSetChanged();


                        break;
                }
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutitems;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                scrollOutitems = linearLayoutManager.findLastVisibleItemPosition();

                Functions.printLog("resp", "" + scrollOutitems);
                if (userScrolled && (scrollOutitems == datalist.size() - 1)) {
                    userScrolled = false;

                    if (loadMoreProgress.getVisibility() != View.VISIBLE && !ispostFinsh) {
                        loadMoreProgress.setVisibility(View.VISIBLE);
                        pageCount = pageCount + 1;
                        if (searchEdit.getText().toString().length() > 0) {
                            callApiForOtherUsers();
                        } else {
                            callApiForGetAllfollowing(false);
                        }
                    }
                }


            }
        });


        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
                pageCount = 0;
                if (searchEdit.getText().toString().length() > 0) {
                    callApiForOtherUsers();
                } else {
                    callApiForGetAllfollowing(false);
                }

            }
        });

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendsActivity.super.onBackPressed();
            }
        });

        findViewById(R.id.donebtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<UserModel> selectedArray = new ArrayList<>();
                for (int i = 0; i < datalist.size(); i++) {
                    if (datalist.get(i).isSelected) {
                        selectedArray.add(datalist.get(i));
                    }
                }

                passDataBack(selectedArray);

            }
        });

    }


    //call api for get the all follwers of specific profile
    private void callApiForOtherUsers() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("type", "user");
            parameters.put("keyword", searchEdit.getText().toString());
            parameters.put("starting_point", "" + pageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }


        VolleyRequest.JsonPostRequest(FriendsActivity.this, ApiLinks.search, parameters, Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(FriendsActivity.this, resp);
                parseFollowingData(resp);
            }
        });


    }


    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void callApiForGetAllfollowing(boolean isProgressShow) {
        if (from.equals("@friends")) {
            if (datalist == null)
                datalist = new ArrayList<>();

            JSONObject parameters = new JSONObject();
            try {
                parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
                parameters.put("starting_point", "" + pageCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (isProgressShow) {
                pbar.setVisibility(View.VISIBLE);
            }
            VolleyRequest.JsonPostRequest(FriendsActivity.this, ApiLinks.showFollowing, parameters, Functions.getHeaders(this), new Callback() {
                @Override
                public void onResponce(String resp) {
                    Functions.checkStatus(FriendsActivity.this, resp);
                    if (isProgressShow) {
                        pbar.setVisibility(View.GONE);
                    }
                    parseFollowingData(resp);
                }
            });
        }

        else if (from.equals("@shops")) {
            if (datalist == null)
                datalist = new ArrayList<>();

            JSONObject parameters = new JSONObject();
            try {
                parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
                parameters.put("starting_point", "" + pageCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (isProgressShow) {
                pbar.setVisibility(View.VISIBLE);
            }
            VolleyRequest.JsonPostRequest(FriendsActivity.this, ApiLinks.showStoreUsers, parameters, Functions.getHeaders(this), new Callback() {
                @Override
                public void onResponce(String resp) {
                    Functions.checkStatus(FriendsActivity.this, resp);
                    if (isProgressShow) {
                        pbar.setVisibility(View.GONE);
                    }
                    parseFollowingData(resp);
                }
            });
        }


    }

    public void parseFollowingData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                JSONArray msg = jsonObject.optJSONArray("msg");
                ArrayList<UserModel> temp_list = new ArrayList<>();

                for (int i = 0; i < msg.length(); i++) {
                    JSONObject data = msg.optJSONObject(i);

                    JSONObject userObj = data.optJSONObject("User");
                    UserModel userDetailModel = DataParsing.getUserDataModel(userObj);

                    temp_list.add(userDetailModel);


                }

                if (pageCount == 0) {
                    datalist.clear();
                    datalist.addAll(temp_list);
                } else {
                    datalist.addAll(temp_list);
                }

                adapter.notifyDataSetChanged();
            }

            if (datalist.isEmpty()) {
                findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.no_data_layout).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadMoreProgress.setVisibility(View.GONE);
        }
    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void passDataBack(final ArrayList<UserModel> datalist) {
        Intent intent = new Intent();
        intent.putExtra("isShow", true);
        intent.putExtra("data", datalist);
        setResult(RESULT_OK, intent);
        finish();
    }

}