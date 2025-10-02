package com.coheser.app.activitesfragments.videorecording;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.facebook.drawee.view.SimpleDraweeView;
import com.coheser.app.activitesfragments.location.AddressPlacesModel;
import com.coheser.app.activitesfragments.soundlists.SoundListMainActivity;
import com.coheser.app.mainmenu.MainMenuActivity;
import com.coheser.app.models.UploadVideoModel;
import com.coheser.app.services.VideoUploadWorker;
import com.coheser.app.simpleclasses.DataHolder;
import com.coheser.app.simpleclasses.DebounceClickHandler;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.coheser.app.activitesfragments.storyeditors.ShapeBSFragment;
import com.coheser.app.activitesfragments.storyeditors.StoryStickerArtFragment;
import com.coheser.app.activitesfragments.storyeditors.TextEditorDialogFragment;
import com.coheser.app.Constants;
import com.coheser.app.interfaces.FragmentCallBack;
import com.coheser.app.interfaces.GenrateBitmapCallback;
import com.coheser.app.models.TextEditorModel;
import com.coheser.app.R;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.Dialogs;
import com.coheser.app.simpleclasses.FFMPEGFunctions;
import com.coheser.app.simpleclasses.FileUtils;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;

import com.google.android.exoplayer2.video.VideoSize;
import com.coheser.app.simpleclasses.VideoThumbnailExtractor;

import org.json.JSONArray;
import java.io.File;
import java.io.FileOutputStream;
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.SaveSettings;
import ja.burhanrashid52.photoeditor.TextStyleBuilder;
import ja.burhanrashid52.photoeditor.ViewType;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;
import ja.burhanrashid52.photoeditor.shape.ShapeType;


public class PreviewStoryVideoActivity extends AppCompatLocaleActivity implements Player.Listener {


    String isSoundSelected;
    File videoFile;
    String videoType="",isSelected;
    String draftFile, duetVideoId, duetOrientation,width,height;

    String soundFilePath;

    SimpleDraweeView ivUserPic;
    Context context;
    LinearLayout tabSound, tabRedo, tabUndo;
    TextView tvSound;
    PhotoEditorView photoEditorView;
    PhotoEditor mPhotoEditor;
    LinearLayout tabPublishStory,tabDraw,tabEraser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        hideNavigation();
        setContentView(R.layout.activity_preview_story_video);
        context = PreviewStoryVideoActivity.this;


        tabPublishStory = findViewById(R.id.tabPublishStory);
        tabDraw=findViewById(R.id.tabDraw);
        tabEraser=findViewById(R.id.tabEraser);
        photoEditorView = findViewById(R.id.photoEditorView);
        tvSound = findViewById(R.id.tvSound);
        tabSound = findViewById(R.id.tabSound);
        tabUndo = findViewById(R.id.tabUndo);
        tabRedo = findViewById(R.id.tabRedo);

        initEditor();

        Intent intent = getIntent();
        if (intent != null) {
            String fromWhere = intent.getStringExtra("fromWhere");
            if (fromWhere != null && fromWhere.equals("video_recording")) {
                isSoundSelected = intent.getStringExtra("isSoundSelected");
                draftFile = intent.getStringExtra("draft_file");
                videoType = ""+intent.getStringExtra("videoType");
            }
            else {
                draftFile = intent.getStringExtra("draft_file");
            }
        }
        ivUserPic = findViewById(R.id.ivUserPic);
        String picUrl = Functions.getSharedPreference(PreviewStoryVideoActivity.this).getString(Variables.U_PIC, "null");
        ivUserPic.setController(Functions.frescoImageLoad(picUrl, R.drawable.ic_user_icon, ivUserPic, false));

        videoFile = new File(FileUtils.getAppFolder(this) + Variables.outputfile2);

        Log.d(Constants.tag,"videoFile: "+videoFile);
        startPlayerConfiguration();
        getVideoSize();

        try {
            File aiVideo=new File(FileUtils.getAppFolder(this) + Variables.AiVideo);
            if(aiVideo.exists()){
                aiVideo.delete();
            }
            FileUtils.copyFile(videoFile,new File(FileUtils.getAppFolder(this) + Variables.AiVideo));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }




        tvSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSelectSound();
            }
        });

        tabUndo.setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhotoEditor.undo();
            }
        }));

        tabRedo.setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhotoEditor.redo();
            }
        }));

        findViewById(R.id.goBack).setOnClickListener(new DebounceClickHandler(v -> {
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }));

        findViewById(R.id.tabText).setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openKotlinTextEditor();
            }
        }));

        findViewById(R.id.tabSticker).setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openStickerAddedSheet();
            }
        }));

        findViewById(R.id.tabNext).setOnClickListener(new DebounceClickHandler(v -> {
            saveEditedImage(false);

        }));

        tabPublishStory.setOnClickListener(new DebounceClickHandler(v -> {
            videoType="Story";
            saveEditedImage(true);
        }));

        tabEraser.setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhotoEditor.brushEraser();
            }
        }));


        tabDraw.setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhotoEditor.setBrushDrawingMode(true);
                openKotlinDrawShapeEditor();
            }
        }));

    }



    public void openSelectSound(){
        Intent intent = new Intent(this, SoundListMainActivity.class);
        resultCallback.launch(intent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }


    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            isSelected = data.getStringExtra("isSelected");
                            if (isSelected.equals("yes")) {
                                videoPlayer.seekTo(0);

                                new Handler(Looper.getMainLooper())
                                        .postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                PreviewStoryVideoActivity.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        tvSound.setText(""+data.getStringExtra("name"));
                                                        Variables.selectedSoundId = data.getStringExtra("sound_id");
                                                        soundFilePath = data.getStringExtra("outputFile");
                                                        preparedAudio();

                                                    }
                                                });

                                            }
                                        },600);


                            }

                        }

                    }
                }
            });




    private void saveEditedImage(boolean isPublish) {

        if(viewsAdded>0){
            String fileName = System.currentTimeMillis() + ".png";
            File dirPath=new File(FileUtils.getAppFolder(PreviewStoryVideoActivity.this)+Variables.APP_STORY_EDITED_FOLDER);
            File filePath=new File(dirPath,fileName);
            FileUtils.makeDirectryAndRefresh(PreviewStoryVideoActivity.this,dirPath.getAbsolutePath(),fileName);

            PreviewStoryVideoActivity.this.runOnUiThread(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    SaveSettings saveSettings=new SaveSettings.Builder().build();
                    mPhotoEditor.saveAsBitmap(saveSettings, new OnSaveBitmap() {
                        @Override
                        public void onBitmapReady(@Nullable Bitmap bitmap) {
                            PreviewStoryVideoActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, Variables.videoWidth, Variables.videoHeight, true);
                                        FileOutputStream out = new FileOutputStream(filePath.getAbsolutePath());
                                        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                                        out.close();
                                    }
                                    catch (Exception e)
                                    {
                                        Log.d(Constants.tag,"Exception scaledBitmap: "+e);
                                    }
                                    finally {
                                        merageEditing(filePath.getAbsolutePath(),isPublish);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(@Nullable Exception e) {
                            Log.d(Constants.tag,"Exception BitmapEditor: "+e);
                        }
                    });
                }
            });
        }
        else {
            try {
                FileUtils.copyFile(videoFile, new File(FileUtils.getAppFolder(PreviewStoryVideoActivity.this) + Variables.output_filter_file));

            } catch (Exception e) {
                Functions.printLog(Constants.tag, "" + e);
            }

            if (isPublish) {
                moveToPublish();
            } else {
                moveToNext();
            }
        }


    }

    private void openKotlinDrawShapeEditor() {
        ShapeBuilder mShapeBuilder =new ShapeBuilder();
        mPhotoEditor.setShape(mShapeBuilder);
        ShapeBSFragment fragment = new ShapeBSFragment();
        fragment.setPropertiesChangeListener(new ShapeBSFragment.Properties() {
            @Override
            public void onColorChanged(int colorCode) {
                mPhotoEditor.setShape(mShapeBuilder.withShapeColor(colorCode));
            }

            @Override
            public void onOpacityChanged(int opacity) {
                mPhotoEditor.setShape(mShapeBuilder.withShapeOpacity(opacity));
            }

            @Override
            public void onShapeSizeChanged(int shapeSize) {
                mPhotoEditor.setShape(mShapeBuilder.withShapeSize(Float.valueOf(""+shapeSize)));
            }

            @Override
            public void onShapePicked(@Nullable ShapeType shapeType) {
                mPhotoEditor.setShape(mShapeBuilder.withShapeType(shapeType));
            }
        });
        Bundle bundle=new Bundle();
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "TextEditorDialogFragment");
    }

    private void openKotlinTextEditor() {
        TextEditorDialogFragment fragment = new TextEditorDialogFragment(new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                TextEditorModel model=(TextEditorModel) bundle.getSerializable("data");


                TextStyleBuilder styleBuilder=new TextStyleBuilder();
                styleBuilder.withTextColor(model.colorCode);
                Typeface typeface = ResourcesCompat.getFont(context, model.selectedFont.font);
                styleBuilder.withTextFont(typeface);
                if(model.direction==0)
                    styleBuilder.withGravity(Gravity.START);
                if(model.direction==1)
                    styleBuilder.withGravity(Gravity.CENTER);
                if(model.direction==2)
                    styleBuilder.withGravity(Gravity.END);
                mPhotoEditor.addText(model.text,styleBuilder);
            }
        });
        Bundle bundle=new Bundle();
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "TextEditorDialogFragment");
    }

    private void openKotlinTextEditor(View rootView,String inputText, int colorCode) {
        TextEditorDialogFragment fragment = new TextEditorDialogFragment(new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                TextEditorModel model=(TextEditorModel) bundle.getSerializable("data");


                TextStyleBuilder styleBuilder=new TextStyleBuilder();
                styleBuilder.withTextColor(model.colorCode);
                Typeface typeface = ResourcesCompat.getFont(context, model.selectedFont.font);
                styleBuilder.withTextFont(typeface);
                if(model.direction==0)
                    styleBuilder.withGravity(Gravity.START);
                if(model.direction==1)
                    styleBuilder.withGravity(Gravity.CENTER);
                if(model.direction==2)
                    styleBuilder.withGravity(Gravity.END);
                mPhotoEditor.editText(rootView,inputText,styleBuilder);
            }
        });
        TextEditorModel model=new TextEditorModel();
        model.text=inputText;
        model.colorCode=colorCode;
        Bundle bundle=new Bundle();
        bundle.putSerializable("data",model);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "TextEditorDialogFragment");
    }


    int viewsAdded=0;
    private void initEditor() {
        viewsAdded=0;
        photoEditorView.getSource().setBackground(ContextCompat.getDrawable(PreviewStoryVideoActivity.this,R.drawable.transprent_editor));
        mPhotoEditor =new PhotoEditor.Builder(PreviewStoryVideoActivity.this,photoEditorView)
                .setPinchTextScalable(true)
                .build();
        mPhotoEditor.setOnPhotoEditorListener(new OnPhotoEditorListener() {
            @Override
            public void onEditTextChangeListener(@Nullable View view, @Nullable String inputText, int colorCode) {
                Functions.printLog(Constants.tag,"onEditTextChangeListener");
                openKotlinTextEditor(view,inputText,colorCode);
            }

            @Override
            public void onAddViewListener(@Nullable ViewType viewType, int i) {
                Functions.printLog(Constants.tag,"onAddViewListener");
                viewsAdded++;
            }

            @Override
            public void onRemoveViewListener(@Nullable ViewType viewType, int i) {
                Functions.printLog(Constants.tag,"onRemoveViewListener");
                viewsAdded--;
            }

            @Override
            public void onStartViewChangeListener(@Nullable ViewType viewType) {
                Functions.printLog(Constants.tag,"onStartViewChangeListener");
            }

            @Override
            public void onStopViewChangeListener(@Nullable ViewType viewType) {
                Functions.printLog(Constants.tag,"onStopViewChangeListener");
            }

            @Override
            public void onTouchSourceImage(@Nullable MotionEvent motionEvent) {

            }
        });


    }

    private void setupScreenData() {
        if (isSoundSelected != null && isSoundSelected.equals("yes"))
        {
            tabSound.setVisibility(View.VISIBLE);
            tvSound.setText(""+getIntent().getStringExtra("soundName"));
            preparedAudio();
        }
    }





    // this will play the sound with the video when we select the audio
    MediaPlayer audio;
    public void preparedAudio() {
        videoPlayer.setVolume(0);

        File file = new File(FileUtils.getAppFolder(this) + Variables.SelectedAudio_AAC);
        if (file.exists()) {
            audio = new MediaPlayer();
            try {
                audio.setDataSource(FileUtils.getAppFolder(this) + Variables.SelectedAudio_AAC);
                audio.prepare();
                audio.setLooping(true);

                videoPlayer.setPlayWhenReady(true);
                audio.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
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


    private void startPlayerConfiguration() {
        setPlayer(videoFile.getAbsolutePath());
        setupScreenData();
    }


    public void getVideoSize(){
        if(videoFile.exists()) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoFile.getAbsolutePath());
            width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        }
    }


    private void openStickerAddedSheet() {
        StoryStickerArtFragment fragment = new StoryStickerArtFragment(new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                if (bundle.getBoolean("isShow"))
                {
                    if (bundle.getString("type").equals("sticker"))
                    {
                        String url=bundle.getString("data");
                        addBitmapImage(url);
                    }
                    else
                    if (bundle.getString("type").equals("emoji"))
                    {
                        String emojiCode=bundle.getString("data");
                        mPhotoEditor.addEmoji(emojiCode);
                    }

                }
            }
        });
        Bundle bundle=new Bundle();
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "StoryStickerArtF");
    }

    private void addBitmapImage(String url) {
        FileUtils.UrlToBitmapGenrator(url, new GenrateBitmapCallback() {
            @Override
            public void onResult(Bitmap bitmap) {
                PreviewStoryVideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPhotoEditor.addImage(bitmap);
                    }
                });
            }
        });
    }


    private void merageEditing(String path,boolean isPublish) {
        FileUtils.clearFilesCacheBeforeOperation(new File(FileUtils.getAppFolder(PreviewStoryVideoActivity.this)+Variables.output_filter_file));

        Dialogs.showDeterminentLoader(PreviewStoryVideoActivity.this,false,false);
        String outputPath = FileUtils.getAppFolder(PreviewStoryVideoActivity.this)+Variables.output_filter_file;

        if(videoFile.exists()){
            Functions.printLog(Constants.tag,"path"+videoFile.getAbsolutePath());
        }

        FFMPEGFunctions.INSTANCE.addImageProcess(path,
                videoFile, outputPath
                , new FragmentCallBack() {
                    @Override
                    public void onResponce(Bundle bundle) {
                        if (bundle.getString("action").equals("success"))
                        {
                            Dialogs.cancelDeterminentLoader();
                            FileUtils.clearFilesCacheBeforeOperation(new File(path));
                            videoFile=new File(bundle.getString("path"));

                            if (isPublish)
                            {
                                moveToPublish();
                            }
                            else
                            {

                                moveToNext();
                            }


                            startPlayerConfiguration();
                        }
                        else
                        if (bundle.getString("action").equals("failed"))
                        {
                            Dialogs.cancelDeterminentLoader();
                            Functions.printLog(Constants.tag, getString(R.string.invalid_video_format));                        }
                        else
                        if (bundle.getString("action").equals("cancel"))
                        {
                            Dialogs.cancelDeterminentLoader();
                            Functions.printLog(Constants.tag, getString(R.string.invalid_video_format));
                        }
                        else
                        if (bundle.getString("action").equals("process"))
                        {
                            String message=bundle.getString("message");
                            try {
                                int progressPercentage=FFMPEGFunctions.CalculateFFMPEGTimeToPercentage(message,videoDuration);
                                Dialogs.showLoadingProgress(progressPercentage);
                            }
                            catch (Exception e){}

                        }
                    }
                });
    }


    // this will call when swipe for another video and
    // this function will set the player to the current video
    ExoPlayer videoPlayer;
    StyledPlayerView  playerView;
    int videoDuration=5;
    public void setPlayer(String path) {
        videoPlayer =new ExoPlayer.Builder(context).
                setTrackSelector(new DefaultTrackSelector(context)).
                setLoadControl(Functions.getExoControler()).
                build();
        Uri videoURI = Uri.parse(path);
        MediaItem mediaItem = MediaItem.fromUri(videoURI);
        videoPlayer.setMediaItem(mediaItem);
        videoPlayer.prepare();
        videoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
        videoPlayer.addListener(PreviewStoryVideoActivity.this);
        try {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build();
            videoPlayer.setAudioAttributes(audioAttributes, true);
        }catch (Exception e)
        {
            Log.d(Constants.tag,"Exception: getExoPlayerInit "+e);
        }

        PreviewStoryVideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerView = findViewById(R.id.playerview);
                playerView.setPlayer(videoPlayer);
                playerView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                videoPlayer.setPlayWhenReady(true);
            }
        });
    }

    private int getVideoDurationSeconds(ExoPlayer player)
    {
        int timeMs=(int) player.getDuration();
        int totalSeconds = timeMs / 1000;
        return totalSeconds;
    }


    private void moveToPublish() {
        makeParamAccordingToStory();
        onStop();
        makeThumbnailOfVideo();

    }


    JSONArray hashTag, friendsTag;
    String privcyType="Public";
    public void makeParamAccordingToStory() {
        hashTag = new JSONArray();
        friendsTag = new JSONArray();
        privcyType="Public";
    }

    private void makeThumbnailOfVideo() {
        VideoThumbnailExtractor.getThumbnailFromVideoFilePath(FileUtils.getAppFolder(this)+Variables.output_filter_file, "1000",
                new VideoThumbnailExtractor.ThumbnailListener() {
                    @Override
                    public void onThumbnail(Bitmap thumbnail) {
                        if (thumbnail != null) {
                            makeDifferentTypeThumbnail(thumbnail);
                        }
                    }

        });
    }

    private void makeDifferentTypeThumbnail(Bitmap thumbnail) {

        Bitmap bitmap = Bitmap.createScaledBitmap(
                thumbnail,
                thumbnail.getWidth() / 3,
                thumbnail.getHeight() / 3,
                false
        );

        thumbnail.recycle();

        Functions.getSharedPreference(this).edit()
                .putString(Variables.default_video_thumb, FileUtils.bitmapToBase64(bitmap))
                .commit();

        enqueueVideoUpload();
    }


    public void enqueueVideoUpload() {
        String videoPath = FileUtils.getAppFolder(this)+Variables.output_filter_file;
        UploadVideoModel uploadModel=new UploadVideoModel();
        uploadModel.userId = Functions.getSharedPreference(getApplicationContext()).getString(Variables.U_ID, "0");
        uploadModel.soundId = Variables.selectedSoundId;
        uploadModel.description = "";
        uploadModel.privacyPolicy = privcyType;
        uploadModel.allowComments = "0";
        uploadModel.allowDuet = "0";
        uploadModel.hashtagsJson = hashTag.toString();
        uploadModel.usersJson = friendsTag.toString();
        uploadModel.product_json = "";
        uploadModel.setPlacesModel(new AddressPlacesModel());
        uploadModel.width = width;
        uploadModel.height = height;
        if (duetVideoId != null) {
            uploadModel.videoId = duetVideoId;
            uploadModel.duet = "" + duetOrientation;
        } else {
            uploadModel.videoId = "0";
        }
        uploadModel.videoType = "1";

        Bundle bundle=new Bundle();
        bundle.putString("uri", videoPath);
        bundle.putString("draft_file", draftFile);
        bundle.putParcelable("data", uploadModel);
        DataHolder.Companion.getInstance().setData(bundle);

        Data inputData = new Data.Builder().build();

        OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(VideoUploadWorker.class)
                .setInputData(inputData)
                .addTag("videoUpload")
                .build();
        WorkManager.getInstance(this).enqueue(uploadWorkRequest);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendBroadByName(Variables.homeBroadCastAction);
                Intent intent = new Intent(PreviewStoryVideoActivity.this, MainMenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void sendBroadByName(String action) {
        Intent intent= new Intent(action);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }


    private void moveToNext() {
        Variables.isCompressionApplyOnStart=true;
        Intent intent = new Intent(this, PostVideoActivity.class);
        intent.putExtra("fromWhere", ""+getIntent().getStringExtra("fromWhere"));
        intent.putExtra("isSoundSelected", ""+getIntent().getStringExtra("isSoundSelected"));
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }


    // play the video again on resume
    @Override
    protected void onResume() {
        super.onResume();
        if (videoPlayer != null) {
            videoPlayer.setPlayWhenReady(true);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        try {
            if (videoPlayer != null) {
                videoPlayer.setPlayWhenReady(false);
            }
            if (audio != null) {
                audio.pause();
            }
        } catch (Exception e) {

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (videoPlayer != null) {
            videoPlayer.release();
        }

        if (audio != null) {
            audio.pause();
            audio.release();
        }
    }


    // handle that will be call on player state change


    @Override
    public void onPlaybackStateChanged(int playbackState) {
        if (playbackState == Player.STATE_ENDED) {

            videoPlayer.seekTo(0);
            videoPlayer.setPlayWhenReady(true);

            if (audio != null) {
                audio.seekTo(0);
                audio.start();
            }

        }
        if (playbackState == Player.STATE_READY)
        {
            videoDuration= getVideoDurationSeconds(videoPlayer);
            Log.d(Constants.tag,"videoDuration: "+videoDuration);
        }

    }


    @Override
    public void onVideoSizeChanged(VideoSize videoSize) {
        Player.Listener.super.onVideoSizeChanged(videoSize);
        if (videoSize.width>videoSize.height)
        {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
        }
        else
        {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        }
    }



    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }



}
