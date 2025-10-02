package com.coheser.app.activitesfragments.soundlists;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.request.DownloadRequest;
import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.adapters.SoundListAdapter;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.models.SoundsModel;
import com.coheser.app.simpleclasses.FileUtils;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SectionSoundListActivity extends AppCompatActivity implements Player.Listener, View.OnClickListener {

    public static String running_sound_id;
    static boolean active = false;
    private final long DELAY = 1000; // Milliseconds
    Context context;
    TextView titleTxt;
    String id;
    ArrayList<Object> datalist;
    SoundListAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;
    DownloadRequest prDownloader;
    ProgressBar pbar;
    SwipeRefreshLayout swiperefresh;
    RelativeLayout noDataLayout;
    int pageCount = 0;
    boolean ispostFinsh;
    ProgressBar loadMoreProgress;
    EditText etSearch;
    View previousView;
    ExoPlayer player;
    Thread thread;
    String previous_url = "none";
    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        setContentView(R.layout.activity_section_sound_list);
        context = SectionSoundListActivity.this;


        titleTxt = findViewById(R.id.title_txt);
        etSearch = findViewById(R.id.search_edit);
        id = getIntent().getStringExtra("id");
        titleTxt.setText(getIntent().getStringExtra("name"));

        running_sound_id = "none";
        PRDownloader.initialize(context);

        findViewById(R.id.back_btn).setOnClickListener(this);
        pbar = findViewById(R.id.pbar);
        loadMoreProgress = findViewById(R.id.load_more_progress);

        noDataLayout = findViewById(R.id.no_data_layout);

        recyclerView = findViewById(R.id.listview);
        linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);


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
                        callApiForSound();
                    }
                }


            }
        });


        swiperefresh = findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                pageCount = 0;
                callApiForSound();
            }
        });


        etSearch.addTextChangedListener(
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
                                        SectionSoundListActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                pageCount = 0;
                                                callApiForSound();
                                            }
                                        });
                                    }
                                },
                                DELAY
                        );
                    }
                }
        );
        setAdapter();
        callApiForSound();

    }

    private void callApiForSound() {
        if (etSearch.getText().toString().length() > 0) {
            callApiForAllsoundSearch(etSearch.getText().toString());
        } else {
            callApi();
        }
    }


    // initialize the player for play the audio

    private void callApiForAllsoundSearch(String key) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("sound_section_id", id);
            parameters.put("keyword", key);
            parameters.put("starting_point", "" + pageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(SectionSoundListActivity.this, ApiLinks.searchSoundsAgainstSection, parameters, Functions.getHeaders(SectionSoundListActivity.this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(SectionSoundListActivity.this, resp);
                swiperefresh.setRefreshing(false);
                pbar.setVisibility(View.GONE);
                parseData(resp);
            }
        });
    }

    // set the adapter for show list
    public void setAdapter() {
        datalist = new ArrayList<>();

        adapter = new SoundListAdapter(context, datalist, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

                SoundsModel item = (SoundsModel) object;

                if (view.getId() == R.id.done) {
                    stopPlaying();
                    downLoadMp3(item.id, item.name, item.getAudio());
                } else if (view.getId() == R.id.fav_btn) {
                    callApiForFavSound(pos, item);
                } else {
                    if (thread != null && !thread.isAlive()) {
                        stopPlaying();
                        playaudio(view, item);
                    } else if (thread == null) {
                        stopPlaying();
                        playaudio(view, item);
                    }
                }
            }
        });

        recyclerView.setAdapter(adapter);


    }

    public void callApi() {

        JSONObject params = new JSONObject();
        try {
            params.put("starting_point", pageCount);
            params.put("sound_section_id", id);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(SectionSoundListActivity.this, ApiLinks.showSoundsAgainstSection, params, Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(SectionSoundListActivity.this, resp);
                pbar.setVisibility(View.GONE);
                loadMoreProgress.setVisibility(View.GONE);
                parseData(resp);
            }
        });

    }

    // parse  the data of sound list
    public void parseData(String responce) {


        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                JSONArray msgArray = jsonObject.getJSONArray("msg");

                ArrayList<SoundsModel> tempList = new ArrayList<>();
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i).optJSONObject("Sound");

                    SoundsModel item = new SoundsModel();

                    item.id = itemdata.optString("id");

                    item.setAudio(itemdata.optString("audio"));
                    item.name = itemdata.optString("name");
                    item.description = itemdata.optString("description");
                    item.section = itemdata.optString("section");
                    item.setThum(itemdata.optString("thum"));

                    item.duration = itemdata.optString("duration");
                    item.created = itemdata.optString("created");
                    item.favourite = itemdata.optString("favourite");


                    tempList.add(item);
                }


                if (pageCount == 0) {
                    datalist.clear();
                    datalist.addAll(tempList);
                } else {
                    datalist.addAll(tempList);
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

    public void playaudio(View view, final SoundsModel item) {
        previousView = view;

        if (previous_url.equals(item.getAudio())) {
            previous_url = "none";
            running_sound_id = "none";
        } else {

            previous_url = item.getAudio();
            running_sound_id = item.id;

            player = new ExoPlayer.Builder(context).
                    setTrackSelector(new DefaultTrackSelector(context)).
                    build();

            player.setMediaItem(MediaItem.fromUri(item.getAudio()));
            player.prepare();
            player.addListener(this);
            player.setPlayWhenReady(true);

            try {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                        .build();
                player.setAudioAttributes(audioAttributes, true);
            } catch (Exception e) {
                Log.d(Constants.tag, "Exception audio focus : " + e);
            }
        }

    }


    public void stopPlaying() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        showStopState();

    }


    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;

        running_sound_id = "null";

        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        showStopState();

    }


    public void showRunState() {

        if (previousView != null) {
            previousView.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previousView.findViewById(R.id.pause_btn).setVisibility(View.VISIBLE);
            View imgDone = previousView.findViewById(R.id.done);
            View imgFav = previousView.findViewById(R.id.fav_btn);
            imgFav.animate().translationX(0).setDuration(400).start();
            imgDone.animate().translationX(0).setDuration(400).start();
        }

    }


    public void showLoadingState() {
        previousView.findViewById(R.id.play_btn).setVisibility(View.GONE);
        previousView.findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
    }


    public void showStopState() {

        if (previousView != null) {
            previousView.findViewById(R.id.play_btn).setVisibility(View.VISIBLE);
            previousView.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previousView.findViewById(R.id.pause_btn).setVisibility(View.GONE);
            View imgDone = previousView.findViewById(R.id.done);
            View imgFav = previousView.findViewById(R.id.fav_btn);
            imgDone.animate().translationX(Float.valueOf("" + context.getResources().getDimension(R.dimen._80sdp))).setDuration(400).start();
            imgFav.animate().translationX(Float.valueOf("" + context.getResources().getDimension(R.dimen._50sdp))).setDuration(400).start();
        }

        running_sound_id = "none";

    }


    public void downLoadMp3(final String id, final String sound_name, String url) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(getString(R.string.please_wait_));
        progressDialog.show();

        prDownloader = PRDownloader.download(url, FileUtils.getAppFolder(SectionSoundListActivity.this), Variables.SelectedAudio_AAC)
                .build();

        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                progressDialog.dismiss();
                Intent output = new Intent();
                output.putExtra("isSelected", "yes");
                output.putExtra("name", sound_name);
                output.putExtra("sound_id", id);
                setResult(RESULT_OK, output);
                finish();
                overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);
            }

            @Override
            public void onError(Error error) {
                progressDialog.dismiss();
            }
        });

    }


    private void callApiForFavSound(final int pos, final SoundsModel item) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("sound_id", item.id);


        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(SectionSoundListActivity.this, false, false);
        VolleyRequest.JsonPostRequest(SectionSoundListActivity.this, ApiLinks.addSoundFavourite, parameters, Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(SectionSoundListActivity.this, resp);
                Functions.cancelLoader();

                if (item.favourite.equals("1"))
                    item.favourite = "0";
                else
                    item.favourite = "1";

                datalist.remove(item);
                datalist.add(pos, item);
                adapter.notifyDataSetChanged();

            }
        });


    }


    @Override
    public void onPlaybackStateChanged(int playbackState) {
        if (playbackState == Player.STATE_BUFFERING) {
            showLoadingState();
        } else if (playbackState == Player.STATE_READY) {
            showRunState();
        } else if (playbackState == Player.STATE_ENDED) {
            showStopState();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                SectionSoundListActivity.super.onBackPressed();
                break;
        }
    }


}