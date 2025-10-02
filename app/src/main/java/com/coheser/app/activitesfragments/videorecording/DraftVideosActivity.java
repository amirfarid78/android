package com.coheser.app.activitesfragments.videorecording;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.adapters.DraftVideosAdapter;
import com.coheser.app.models.DraftVideoModel;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.FileUtils;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class DraftVideosActivity extends AppCompatLocaleActivity implements View.OnClickListener {

    public RecyclerView recyclerView;
    ArrayList<DraftVideoModel> dataList = new ArrayList<>();
    DraftVideosAdapter adapter;

    ProgressBar pbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(DraftVideosActivity.this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        setContentView(R.layout.activity_gallery_videos);

        pbar = findViewById(R.id.pbar);


        recyclerView = findViewById(R.id.recylerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(DraftVideosActivity.this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);


        adapter = new DraftVideosAdapter(this, dataList, new DraftVideosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, DraftVideoModel item, View view) {

                if (view.getId() == R.id.cross_btn) {
                    File file_data = new File(item.video_path);
                    if (file_data.exists()) {
                        file_data.delete();
                    }
                    dataList.remove(postion);
                    adapter.notifyItemRemoved(postion);

                } else {

                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    Bitmap bmp = null;
                    try {
                        retriever.setDataSource(item.video_path);
                        bmp = retriever.getFrameAtTime();
                        int videoHeight = bmp.getHeight();
                        int videoWidth = bmp.getWidth();

                        Functions.printLog(Constants.tag, videoWidth + "---" + videoHeight);

                    } catch (Exception e) {
                        Log.d(Constants.tag, "Exception: " + e);
                    }

                    if (item.video_duration_ms <= Constants.MAX_RECORDING_DURATION) {

                        if (Functions.isWorkManagerRunning(DraftVideosActivity.this,"videoUpload")) {
                      //  if (!Functions.isMyServiceRunning(DraftVideosA.this, new UploadService().getClass())) {

                            changeSmallVideoSize(item.video_path, FileUtils.getAppFolder(DraftVideosActivity.this) + Variables.outputfile2);
                        } else {
                            Toast.makeText(DraftVideosActivity.this, getString(R.string.please_wait_video_uploading_is_already_in_progress), Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        try {
                            changeVideoSize(item.video_path, FileUtils.getAppFolder(DraftVideosActivity.this) + Variables.outputfile2);
                        } catch (Exception e) {
                            Log.d(Constants.tag, "Exception: " + e);
                        }
                    }

                }

            }
        });

        recyclerView.setAdapter(adapter);
        getAllVideoPathDraft();


        findViewById(R.id.goBack).setOnClickListener(this);


    }


    // get the videos from loacal directory and show them in list
    public void getAllVideoPathDraft() {
        String path = FileUtils.getAppFolder(this) + Variables.DRAFT_APP_FOLDER;
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                DraftVideoModel item = new DraftVideoModel();
                item.video_path = file.getAbsolutePath();
                item.video_duration_ms = getfileduration(Uri.parse(file.getAbsolutePath()));

                Functions.printLog(Constants.tag, "" + item.video_duration_ms);

                if (item.video_duration_ms > 5000) {
                    item.video_time = changeSecToTime(item.video_duration_ms);
                    dataList.add(item);
                }
            }

        }
    }


    // get the audio file duration that is store in our directory
    public long getfileduration(Uri uri) {
        try {

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            final int file_duration = Functions.parseInterger(durationStr);

            return file_duration;
        } catch (Exception e) {
            Log.d(Constants.tag, "Exception: " + e);
        }
        return 0;
    }


    public String changeSecToTime(long file_duration) {
        long second = (file_duration / 1000) % 60;
        long minute = (file_duration / (1000 * 60)) % 60;

        return String.format(Locale.ENGLISH, "%02d:%02d", minute, second);

    }


    // change the video size before post
    public void changeSmallVideoSize(String src_path, String destination_path) {

        File source = new File(src_path);
        try {

            if (source.exists()) {

                FileUtils.copyFile(new File(src_path),
                        new File(destination_path));

                Intent intent = new Intent(this, PreviewStoryVideoActivity.class);
                intent.putExtra("video_path", FileUtils.getAppFolder(this) + Variables.outputfile2);
                intent.putExtra("draft_file", src_path);
                intent.putExtra("fromWhere", "video_recording");
                if (getIntent().hasExtra("name")) {
                    intent.putExtra("isSoundSelected", "yes");
                    intent.putExtra("soundName", getIntent().getStringExtra("name"));
                }
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);


            } else {
                Functions.showToast(DraftVideosActivity.this, getString(R.string.fail_to_get_video_from_draft));
            }

        } catch (Exception e) {
            Log.d(Constants.tag, "Exception: " + e);
        }

    }


    // change the video size
    public void changeVideoSize(String src_path, String destination_path) {

        try {
            FileUtils.copyFile(new File(src_path),
                    new File(destination_path));


            Intent intent = new Intent(this, PreviewStoryVideoActivity.class);
            intent.putExtra("video_path", FileUtils.getAppFolder(this) + Variables.outputfile2);
            intent.putExtra("draft_file", src_path);
            intent.putExtra("fromWhere", "video_recording");
            if (getIntent().hasExtra("name")) {
                intent.putExtra("isSoundSelected", "yes");
                intent.putExtra("soundName", getIntent().getStringExtra("name"));
            }
            startActivity(intent);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);


        } catch (Exception e) {
            e.printStackTrace();
            Functions.printLog(Constants.tag, e.toString());
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        deleteFile();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        deleteFile();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteFile();
    }

    // delete the files if exist
    public void deleteFile() {
        File output2 = new File(FileUtils.getAppFolder(this) + Variables.outputfile2);
        File gallery_trim_video = new File(FileUtils.getAppFolder(this) + Variables.gallery_trimed_video);
        File gallery_resize_video = new File(FileUtils.getAppFolder(this) + Variables.outputfile2);


        if (output2.exists()) {
            output2.delete();
        }


        if (gallery_trim_video.exists()) {
            gallery_trim_video.delete();
        }

        if (gallery_resize_video.exists()) {
            gallery_resize_video.delete();
        }


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goBack:
                finish();
                overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);

                break;

        }
    }
}
