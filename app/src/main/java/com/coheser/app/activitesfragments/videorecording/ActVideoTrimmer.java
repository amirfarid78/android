package com.coheser.app.activitesfragments.videorecording;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.facebook.drawee.view.SimpleDraweeView;
import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.interfaces.FragmentCallBack;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.CrystalRangeSeekbar;
import com.coheser.app.simpleclasses.CrystalSeekbar;
import com.coheser.app.simpleclasses.DateOprations;
import com.coheser.app.simpleclasses.Dialogs;
import com.coheser.app.simpleclasses.FFMPEGFunctions;
import com.coheser.app.simpleclasses.FileUtils;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.coheser.app.trimmodule.CustomProgressView;
import com.coheser.app.trimmodule.TrimVideo;
import com.coheser.app.trimmodule.TrimVideoOptions;
import com.coheser.app.trimmodule.TrimmerUtils;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.video.VideoSize;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;

public class ActVideoTrimmer extends AppCompatLocaleActivity implements View.OnClickListener {


    ProgressBar compressionProgress;
    int recordingDuration;
    SimpleDraweeView ivThumbnail;
    private ImageView imagePlayPause, btnBack;
    TextView  btnNext;
    private SimpleDraweeView[] imageViews;
    private long totalDuration;
    private Uri uri;
    private String uriRealPath;
    private TextView txtStartDuration, txtEndDuration;
    private CrystalRangeSeekbar seekbar;
    private long lastMinValue = 0;
    private long lastMaxValue = 0;
    private MenuItem menuDone;
    private CrystalSeekbar seekbarController;
    private boolean isValidVideo = true, isVideoEnded;
    private Handler seekHandler;
    private Bundle bundle;
    private TrimVideoOptions trimVideoOptions;
    private long currentDuration, lastClickedTime;
    private String outputPath;
    private int trimType;    Runnable updateSeekbar = new Runnable() {
        @Override
        public void run() {
            try {
                currentDuration = videoPlayer.getCurrentPosition() / 1000;
                if (!videoPlayer.getPlayWhenReady())
                    return;
                if (currentDuration <= lastMaxValue)
                    seekbarController.setMinStartValue((int) currentDuration).apply();
                else if (videoPlayer != null)
                    videoPlayer.setPlayWhenReady(false);

            } finally {
                seekHandler.postDelayed(updateSeekbar, 1000);
            }
        }
    };
    private long fixedGap, minGap, minFromGap, maxToGap;
    private boolean hidePlayerSeek;
    private CustomProgressView progressView;
    private StyledPlayerView playerView;
    private ExoPlayer videoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(ActVideoTrimmer.this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        hideNavigation();
        setContentView(R.layout.activity_act_video_trimmer);

        bundle = getIntent().getExtras();
        recordingDuration = bundle.getInt("recordingDuration", 0);
        Gson gson = new Gson();
        String videoOption = bundle.getString(TrimVideo.TRIM_VIDEO_OPTION);
        trimVideoOptions = gson.fromJson(videoOption, TrimVideoOptions.class);
        progressView = new CustomProgressView(this);
        compressionProgress = findViewById(R.id.compressionProgress);
        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);
    }


    private void changeVideoFormat(String videoPath) {

        Log.d(Constants.tag, "InputPath: " + videoPath);
        Dialogs.showDeterminentLoader(ActVideoTrimmer.this, false, false);
        FFMPEGFunctions.INSTANCE.changeVideoFormat(ActVideoTrimmer.this,
                new File(videoPath)
                , new FragmentCallBack() {
                    @Override
                    public void onResponce(Bundle bundle) {
                        if (bundle.getString("action").equals("success")) {
                            Dialogs.cancelDeterminentLoader();

                            uriRealPath = bundle.getString("path");

                            checkAndResizeVideo(false);
                        } else if (bundle.getString("action").equals("failed")) {
                            Dialogs.cancelDeterminentLoader();

                            Functions.printLog(Constants.tag, getString(R.string.invalid_video_format));

                        } else if (bundle.getString("action").equals("cancel")) {
                            Dialogs.cancelDeterminentLoader();
                            Functions.printLog(Constants.tag, getString(R.string.invalid_video_format));
                        } else if (bundle.getString("action").equals("process")) {
                            String message = bundle.getString("message");
                            try {
                                int progressPercentage = FFMPEGFunctions.CalculateFFMPEGTimeToPercentage(message, 60);
                                Dialogs.showLoadingProgress(progressPercentage);
                            } catch (Exception e) {
                                Functions.printLog(Constants.tag, "Exception: " + e);
                            }

                        }
                    }
                });
    }

    private void compressionApplyOnVideo(String videoPath, int width, int height) {
        Log.d(Constants.tag, "InputPath: " + videoPath);
        int frameRate = Integer.valueOf(FileUtils.getTrimVideoFrameRate(new File(videoPath).getAbsolutePath()));
        Dialogs.showDeterminentLoader(ActVideoTrimmer.this, false, false);
        FFMPEGFunctions.INSTANCE.compressVideoHighToLowProcess(ActVideoTrimmer.this,
                new File(videoPath)
                , frameRate, width, height
                , new FragmentCallBack() {
                    @Override
                    public void onResponce(Bundle bundle) {
                        if (bundle.getString("action").equals("success")) {
                            Dialogs.cancelDeterminentLoader();

                            uriRealPath = bundle.getString("path");
                            uri = Uri.parse(uriRealPath);
                            Log.d(Constants.tag, "OutputPath: " + uriRealPath);
                            Functions.printLog(Constants.tag, "Compressing Done");
                            setDataInView();
                        } else if (bundle.getString("action").equals("failed")) {
                            Dialogs.cancelDeterminentLoader();

                            Functions.printLog(Constants.tag, getString(R.string.invalid_video_format));

                        } else if (bundle.getString("action").equals("cancel")) {
                            Dialogs.cancelDeterminentLoader();
                            Functions.printLog(Constants.tag, getString(R.string.invalid_video_format));
                        } else if (bundle.getString("action").equals("process")) {
                            String message = bundle.getString("message");
                            try {
                                int progressPercentage = FFMPEGFunctions.CalculateFFMPEGTimeToPercentage(message, 60);
                                Dialogs.showLoadingProgress(progressPercentage);
                            } catch (Exception e) {
                                Functions.printLog(Constants.tag, "Exception: " + e);
                            }

                        }
                    }
                });
    }

    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ivThumbnail = findViewById(R.id.ivThumbnail);
        playerView = findViewById(R.id.player_view_lib);
        imagePlayPause = findViewById(R.id.image_play_pause);
        seekbar = findViewById(R.id.range_seek_bar);
        txtStartDuration = findViewById(R.id.txt_start_duration);
        txtEndDuration = findViewById(R.id.txt_end_duration);
        seekbarController = findViewById(R.id.seekbar_controller);

        SimpleDraweeView imageOne = findViewById(R.id.image_one);
        SimpleDraweeView imageTwo = findViewById(R.id.image_two);
        SimpleDraweeView imageThree = findViewById(R.id.image_three);
        SimpleDraweeView imageFour = findViewById(R.id.image_four);
        SimpleDraweeView imageFive = findViewById(R.id.image_five);
        SimpleDraweeView imageSix = findViewById(R.id.image_six);
        SimpleDraweeView imageSeven = findViewById(R.id.image_seven);
        SimpleDraweeView imageEight = findViewById(R.id.image_eight);
        imageViews = new SimpleDraweeView[]{imageOne, imageTwo, imageThree,
                imageFour, imageFive, imageSix, imageSeven, imageEight};
        seekHandler = new Handler(Looper.getMainLooper());
        try {
            Functions.printLog(Constants.tag,"TrimVideo.TRIM_VIDEO_URI"+bundle.getString(TrimVideo.TRIM_VIDEO_URI));
            uri = Uri.parse(bundle.getString(TrimVideo.TRIM_VIDEO_URI));
            Functions.printLog(Constants.tag,"uri"+uri.toString());

            uriRealPath = com.coheser.app.trimmodule.FileUtils.getPath(ActVideoTrimmer.this, uri);
            Functions.printLog(Constants.tag,"uriRealPath"+uriRealPath);

           // uri = Uri.parse(uriRealPath);
            Functions.printLog(Constants.tag,"uri:uriRealPath"+uri.toString());

        } catch (Exception e) {
            Log.d(Constants.tag, "Exception: " + e);
        }


        checkAndResizeVideo(true);

    }

    public void checkAndResizeVideo(boolean checkExtention) {
        String extention= com.coheser.app.trimmodule.FileUtils.getFileExtensionFromUri(this,uri);
        Functions.printLog(Constants.tag,"extention"+extention);
        if(!checkExtention || (extention!=null && extention.contains("mp4"))) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uriRealPath);
            String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

            if (width != null && height != null) {
                int videoWidth = Integer.parseInt(width);
                int videoHeight = Integer.parseInt(height);
                Functions.printLog(Constants.tag,"width:"+videoWidth+" height:"+videoHeight);
                if (videoWidth < videoHeight && (videoWidth > 1000)) {
                    int[] newSize = FileUtils.resizeVideo(900, videoWidth, videoHeight);
                    compressionApplyOnVideo(uriRealPath, newSize[0], newSize[1]);

                } else {
                    setDataInView();
                }

            } else {
                setDataInView();
            }
        }
        else {
            changeVideoFormat(uriRealPath);
        }
    }



    private void setDataInView() {
        try {
            Runnable fileUriRunnable = () -> {

                runOnUiThread(() -> {

                    Log.d(Constants.tag, "Real uri : " + uri);

                    totalDuration = TrimmerUtils.getDuration(ActVideoTrimmer.this, uri);
                    imagePlayPause.setOnClickListener(v ->
                            onVideoClicked());
                    Objects.requireNonNull(playerView.getVideoSurfaceView()).setOnClickListener(v ->
                            onVideoClicked());
                    initTrimData();
                    buildMediaSource(uri);
                    loadThumbnails();
                    setUpSeekBar();
                });
            };
            Executors.newSingleThreadExecutor().execute(fileUriRunnable);
        } catch (Exception e) {
            Log.d(Constants.tag, "Exception: " + e);
        }
    }

    private void initTrimData() {
        try {
            assert trimVideoOptions != null;
            trimType = TrimmerUtils.getTrimType(trimVideoOptions.trimType);
            hidePlayerSeek = trimVideoOptions.hideSeekBar;
            fixedGap = trimVideoOptions.fixedDuration;
            fixedGap = fixedGap != 0 ? fixedGap : totalDuration;
            minGap = trimVideoOptions.minDuration;
            minGap = minGap != 0 ? minGap : totalDuration;
            if (trimType == 3) {
                minFromGap = trimVideoOptions.minToMax[0];
                maxToGap = trimVideoOptions.minToMax[1];
                minFromGap = minFromGap != 0 ? minFromGap : totalDuration;
                maxToGap = maxToGap != 0 ? maxToGap : totalDuration;
            }
        } catch (Exception e) {
            Log.d(Constants.tag, "Exception: " + e);
        }
    }

    private void onVideoClicked() {
        try {
            if (isVideoEnded) {
                seekTo(lastMinValue);

                if (videoPlayer != null)
                    videoPlayer.setPlayWhenReady(true);

                return;
            }
            if ((currentDuration - lastMaxValue) > 0)
                seekTo(lastMinValue);

            if (videoPlayer != null)
                videoPlayer.setPlayWhenReady(!videoPlayer.getPlayWhenReady());

        } catch (Exception e) {
            Log.d(Constants.tag, "Exception: " + e);
        }
    }

    private void seekTo(long sec) {
        if (videoPlayer != null)
            videoPlayer.seekTo(sec * 1000);
    }

    private void buildMediaSource(Uri mUri) {
        try {

            videoPlayer = new ExoPlayer.Builder(ActVideoTrimmer.this).
                    setTrackSelector(new DefaultTrackSelector(ActVideoTrimmer.this))
                    .build();

            try {
                MediaItem mediaItem = MediaItem.fromUri(mUri);
                videoPlayer.setMediaItem(mediaItem);
                videoPlayer.prepare();

                if (videoPlayer != null)
                    videoPlayer.setPlayWhenReady(true);

            } catch (Exception e) {
                Log.d(Constants.tag, "Exception: getExoPlayerInit " + e);
            }

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build();
            videoPlayer.setAudioAttributes(audioAttributes, true);

            videoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                    imagePlayPause.setVisibility(playWhenReady ? View.GONE :
                            View.VISIBLE);
                }

                @Override
                public void onVideoSizeChanged(VideoSize videoSize) {
                    Player.Listener.super.onVideoSizeChanged(videoSize);
                    if (videoSize.width > videoSize.height) {
                        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
                    } else {
                        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                    }
                }


                @Override
                public void onPlaybackStateChanged(int state) {
                    switch (state) {
                        case Player.STATE_ENDED:
                            Log.d(Constants.tag, "onPlayerStateChanged: Video ended.");
                            imagePlayPause.setVisibility(View.VISIBLE);
                            isVideoEnded = true;
                            break;
                        case Player.STATE_READY:
                            isVideoEnded = false;
                            startProgress();
                            Log.d(Constants.tag, "onPlayerStateChanged: Ready to play.");
                            break;
                        default:
                            break;
                        case Player.STATE_BUFFERING:
                            Log.d(Constants.tag, "onPlayerStateChanged: STATE_BUFFERING.");
                            break;
                        case Player.STATE_IDLE:
                            Log.d(Constants.tag, "onPlayerStateChanged: STATE_IDLE.");
                            break;
                    }
                }


                @Override
                public void onPlayerError(PlaybackException error) {
                    Player.Listener.super.onPlayerError(error);
                    try {
                        ivThumbnail.setController(Functions.frescoImageLoad(Uri.parse(bundle.getString(TrimVideo.TRIM_VIDEO_URI)), false));
                        Variables.isCompressionApplyOnStart = true;
                        if (Variables.isCompressionApplyOnStart) {
                                checkAndResizeVideo(false);
                        }
                    } catch (Exception e) {
                        Log.d(Constants.tag, "Exception: " + e);
                    } finally {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Dialogs.showToastOnTop(ActVideoTrimmer.this, null, getString(R.string.hold_on_it_take_few_seconds));
                            }
                        }, 5000);
                    }
                    Log.d(Constants.tag, "Player Error: " + error.getMessage());
                }

            });

        } catch (Exception e) {
            Log.d(Constants.tag, "Exception: " + e);
        }

        ActVideoTrimmer.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                playerView.setPlayer(videoPlayer);

            }
        });
    }

    /*
     *  loading thumbnails
     * */
    private void loadThumbnails() {
        try {
            long diff = totalDuration / 8;
            int sec = 1;
            String thumb = extractThumbnail(uri);
            for (SimpleDraweeView img : imageViews) {
                img.setController(Functions.frescoImageLoad(Uri.fromFile(new File(thumb)), false));
                if (sec < totalDuration)
                    sec++;
            }
        } catch (Exception e) {
            Log.d(Constants.tag, "Exception: loadThumbnails" + e);
        }
    }

    private String extractThumbnail(Uri videoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, videoUri);
            Bitmap bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            retriever.release();

            // Convert bitmap to URI
            return saveBitmapToFile(bitmap);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
//                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private String saveBitmapToFile(Bitmap bitmap) {
        try {
            File thumbnailFile = File.createTempFile("thumbnail", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(thumbnailFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.close();
            return thumbnailFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setUpSeekBar() {
        seekbar.setVisibility(View.VISIBLE);
        txtStartDuration.setVisibility(View.VISIBLE);
        txtEndDuration.setVisibility(View.VISIBLE);

        seekbarController.setMaxValue(totalDuration).apply();
        seekbar.setMaxValue(totalDuration).apply();
        seekbar.setMaxStartValue((float) totalDuration).apply();
        if (trimType == 1) {
            seekbar.setFixGap(fixedGap).apply();
            lastMaxValue = totalDuration;
        } else if (trimType == 2) {
            seekbar.setMaxStartValue((float) minGap);
            seekbar.setGap(minGap).apply();
            lastMaxValue = totalDuration;
        } else if (trimType == 3) {
            seekbar.setMaxStartValue((float) maxToGap);
            seekbar.setGap(minFromGap).apply();
            lastMaxValue = maxToGap;
        } else {
            seekbar.setGap(2).apply();
            lastMaxValue = totalDuration;
        }
        if (hidePlayerSeek)
            seekbarController.setVisibility(View.GONE);

        seekbar.setOnRangeSeekbarFinalValueListener((minValue, maxValue) -> {
            if (!hidePlayerSeek)
                seekbarController.setVisibility(View.VISIBLE);
        });

        seekbar.setOnRangeSeekbarChangeListener((minValue, maxValue) -> {
            long minVal = (long) minValue;
            long maxVal = (long) maxValue;
            if (lastMinValue != minVal) {
                seekTo((long) minValue);
                if (!hidePlayerSeek)
                    seekbarController.setVisibility(View.INVISIBLE);
            }
            lastMinValue = minVal;
            lastMaxValue = maxVal;
            txtStartDuration.setText(TrimmerUtils.formatSeconds(minVal));
            txtEndDuration.setText(TrimmerUtils.formatSeconds(maxVal));
            if (trimType == 3)
                setDoneColor(minVal, maxVal);
        });

        seekbarController.setOnSeekbarFinalValueListener(value -> {
            long value1 = (long) value;
            if (value1 < lastMaxValue && value1 > lastMinValue) {
                seekTo(value1);
                return;
            }
            if (value1 > lastMaxValue)
                seekbarController.setMinStartValue((int) lastMaxValue).apply();
            else if (value1 < lastMinValue) {
                seekbarController.setMinStartValue((int) lastMinValue).apply();
                if (videoPlayer.getPlayWhenReady())
                    seekTo(lastMinValue);
            }
        });
    }

    private void setDoneColor(long minVal, long maxVal) {
        try {
            if (menuDone == null)
                return;
            //changed value is less than maxDuration
            if ((maxVal - minVal) <= maxToGap) {
                menuDone.getIcon().setColorFilter(
                        new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.white)
                                , PorterDuff.Mode.SRC_IN)
                );
                isValidVideo = true;
            } else {
                menuDone.getIcon().setColorFilter(
                        new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.white)
                                , PorterDuff.Mode.SRC_IN)
                );
                isValidVideo = false;
            }
        } catch (Exception e) {
            Log.d(Constants.tag, "Exception: " + e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoPlayer != null)
            videoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoPlayer != null)
            videoPlayer.release();
        if (progressView != null && progressView.isShowing())
            progressView.dismiss();
        stopRepeatingTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuDone = menu.findItem(R.id.action_done);
        return super.onPrepareOptionsMenu(menu);
    }

    private void trimVideo() {
        if (isValidVideo) {
            //not exceed given maxDuration if has given
            outputPath = com.coheser.app.simpleclasses.FileUtils.getAppFolder(ActVideoTrimmer.this) + Variables.gallery_trimed_video;

            if (videoPlayer != null)
                videoPlayer.setPlayWhenReady(false);


            Functions.printLog(Constants.tag, " videoPlayer.getDuration(): " + videoPlayer.getDuration());
            Functions.printLog(Constants.tag, "lastMinValue: " + lastMinValue);
            Functions.printLog(Constants.tag, "lastMaxValue: " + lastMaxValue);
            Log.d(Constants.tag, "startTimeString: " + DateOprations.getTimeWithAdditionalSecond("HH:mm:ss", (int) lastMinValue) +
                    " endTimeString: " + DateOprations.getTimeWithAdditionalSecond("HH:mm:ss", (int) lastMaxValue));

            Log.d(Constants.tag, "recordingDuration: " + recordingDuration);
            Log.d(Constants.tag, "Max:Second Allow: " + (lastMaxValue - lastMinValue));
            int videoDuration = (int) ((videoPlayer.getDuration() / 1000) - 1);
            Log.d(Constants.tag, "videoDuration: " + videoDuration);

            int videoSelectedDuration = (int) (lastMaxValue - lastMinValue);

            if (videoSelectedDuration >= videoDuration) {
                try {

                    File file1=new File(com.coheser.app.trimmodule.FileUtils.getPath(ActVideoTrimmer.this,uri));
                    if(file1.exists()) {
                        com.coheser.app.simpleclasses.FileUtils.copyFile(file1, new File(outputPath));
                        Intent intent = new Intent();
                        intent.putExtra(Variables.gallery_trimed_video, outputPath);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    else {
                        Functions.printLog(Constants.tag,"File not exist");
                    }

                } catch (Exception e) {
                    Functions.printLog(Constants.tag,"copy crash"+e.toString());
                    trimVideofinal(videoSelectedDuration);
                }
            } else {

                trimVideofinal(videoSelectedDuration);
            }
        }
    }

    public void trimVideofinal(int videoSelectedDuration) {
        File file;
        try {
          file= new File(com.coheser.app.trimmodule.FileUtils.getPath(this,uri));
        }
        catch (Exception e){
            file= new File(String.valueOf(uri));
        }
        Dialogs.showDeterminentLoader(ActVideoTrimmer.this, false, false);
        FFMPEGFunctions.INSTANCE.trimVideoProcess(file,
                outputPath, DateOprations.getTimeWithAdditionalSecond("HH:mm:ss", (int) lastMinValue)
                , DateOprations.getTimeWithAdditionalSecond("HH:mm:ss", (int) lastMaxValue)
                , new FragmentCallBack() {
                    @Override
                    public void onResponce(Bundle bundle) {
                        if (bundle.getString("action").equals("success")) {
                            Dialogs.cancelDeterminentLoader();
                            Intent intent = new Intent();
                            intent.putExtra(Variables.gallery_trimed_video, outputPath);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else if (bundle.getString("action").equals("failed")) {
                            Dialogs.cancelDeterminentLoader();
                            Functions.printLog(Constants.tag, getString(R.string.invalid_video_format));
                        } else if (bundle.getString("action").equals("cancel")) {
                            Dialogs.cancelDeterminentLoader();
                            Functions.printLog(Constants.tag, getString(R.string.invalid_video_format));
                        } else if (bundle.getString("action").equals("process")) {
                            String message = bundle.getString("message");
                            try {
                                int progressPercentage = FFMPEGFunctions.CalculateFFMPEGTimeToPercentage(message, videoSelectedDuration);
                                Dialogs.showLoadingProgress(progressPercentage);
                            } catch (Exception e) {
                                Log.d(Constants.tag, "Exception: " + e);
                            }

                        }
                    }
                });
    }

    void startProgress() {
        updateSeekbar.run();
    }

    void stopRepeatingTask() {
        seekHandler.removeCallbacks(updateSeekbar);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBack: {
                ActVideoTrimmer.super.onBackPressed();
            }
            break;
            case R.id.btnNext: {
                //prevent multiple clicks
                if (SystemClock.elapsedRealtime() - lastClickedTime < 800)
                    return;
                lastClickedTime = SystemClock.elapsedRealtime();
                trimVideo();
            }
            break;
        }
    }

    // this will hide the bottom mobile navigation controll
    public void hideNavigation() {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }

    }




}
