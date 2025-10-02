package com.coheser.app.activitesfragments.profile.settings;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.facebook.drawee.view.SimpleDraweeView;
import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.profile.ShareItemViaIntentActivity;
import com.coheser.app.interfaces.ShareIntentCallback;
import com.coheser.app.simpleclasses.AppCompatLocaleActivity;
import com.coheser.app.simpleclasses.DateOprations;
import com.coheser.app.simpleclasses.FileUtils;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.ImageSaver;
import com.coheser.app.simpleclasses.PermissionUtils;
import com.coheser.app.simpleclasses.Variables;
import com.mindorks.Screenshot;
import com.mindorks.properties.Quality;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QrCodeProfileActivity extends AppCompatLocaleActivity implements View.OnClickListener {

    SimpleDraweeView ivUserProfile, imgQrCode;
    TextView tvName;
    RelativeLayout qrContainerBg, tabScreenShot;
    PermissionUtils takePermissionUtils;
    private final ActivityResultLauncher<String[]> mPermissionStorageResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    boolean allPermissionClear = true;
                    List<String> blockPermissionCheck = new ArrayList<>();
                    for (String key : result.keySet()) {
                        if (!(result.get(key))) {
                            allPermissionClear = false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(QrCodeProfileActivity.this, key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked")) {
                        Functions.showPermissionSetting(QrCodeProfileActivity.this, getString(R.string.we_need_storage_permission_for_save_qr_code));
                    } else if (allPermissionClear) {
                        saveQrPicture();
                    }

                }
            });
    private final ActivityResultLauncher<String[]> mPermissionCameraResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    boolean allPermissionClear = true;
                    List<String> blockPermissionCheck = new ArrayList<>();
                    for (String key : result.keySet()) {
                        if (!(result.get(key))) {
                            allPermissionClear = false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(QrCodeProfileActivity.this, key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked")) {
                        Functions.showPermissionSetting(QrCodeProfileActivity.this, getString(R.string.we_need_camera_permission_for_qr_scan));
                    } else if (allPermissionClear) {
                        moveScannerScreen();
                    }

                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(QrCodeProfileActivity.this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        setContentView(R.layout.activity_qr_code_profile);

        inItControl();
        String profielLink = Variables.https + "://" + getString(R.string.domain) + getString(R.string.share_profile_endpoint_second) + Functions.removeAtSymbol(Functions.getSharedPreference(this).getString(Variables.U_NAME,""));
        GenrateQrCode(imgQrCode, profielLink);
    }

    private void inItControl() {
        tabScreenShot = findViewById(R.id.tabScreenShot);
        tabScreenShot.setOnClickListener(this);
        qrContainerBg = findViewById(R.id.qrContainerBg);
        qrContainerBg.setOnClickListener(this);
        tvName = findViewById(R.id.tvName);
        ivUserProfile = findViewById(R.id.ivUserProfile);
        imgQrCode = findViewById(R.id.ivQr);
        findViewById(R.id.ivBack).setOnClickListener(this);
        findViewById(R.id.ivShareQrCode).setOnClickListener(this);
        findViewById(R.id.tabSaveQr).setOnClickListener(this);
        findViewById(R.id.tabScanQr).setOnClickListener(this);


        setUpScreenData();
    }

    private void setUpScreenData() {
        String fullName = Functions.getSharedPreference(QrCodeProfileActivity.this).getString(Variables.F_NAME, "") + " " +
                Functions.getSharedPreference(QrCodeProfileActivity.this).getString(Variables.L_NAME, "");
        tvName.setText(fullName);
        String imgUrl = Functions.getSharedPreference(QrCodeProfileActivity.this).getString(Variables.U_PIC, "");

        ivUserProfile.setController(Functions.frescoImageLoad(imgUrl, ivUserProfile, false));
    }

    private void GenrateQrCode(SimpleDraweeView img_qr, String qr_string) {

        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        QRGEncoder qrgEncoder = new QRGEncoder(
                qr_string, null,
                QRGContents.Type.TEXT,
                smallerDimension);
        try {
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            File uriPath = FileUtils.getBitmapToUri(QrCodeProfileActivity.this, bitmap, "qrCodeTemporary.jpg");

            img_qr.setController(Functions.frescoImageLoad(Uri.fromFile(uriPath), false));
        } catch (Exception e) {
            Functions.printLog(Constants.tag, "Exception : " + e);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivBack: {
                QrCodeProfileActivity.super.onBackPressed();
            }
            break;
            case R.id.ivShareQrCode: {
                Bitmap bitmap = Screenshot.INSTANCE.with(QrCodeProfileActivity.this)
                        .setView(tabScreenShot)
                        .setQuality(Quality.HIGH)
                        .getScreenshot();
                String imgName = "QrScreenshot.png";
                new ImageSaver(QrCodeProfileActivity.this, true).
                        setFileName(imgName).
                        setDirectoryName("Screenshots").
                        save(bitmap);

                final ShareItemViaIntentActivity fragment = new ShareItemViaIntentActivity(new ShareIntentCallback() {
                    @Override
                    public void onResponse(ResolveInfo resolveInfo) {
                        Bitmap bitmap = Screenshot.INSTANCE.with(QrCodeProfileActivity.this)
                                .setView(tabScreenShot)
                                .setQuality(Quality.HIGH)
                                .getScreenshot();
                        shareProfile(bitmap, resolveInfo);
                    }
                });
                fragment.show(getSupportFragmentManager(), "");

            }
            break;
            case R.id.tabSaveQr: {
                takePermissionUtils = new PermissionUtils(QrCodeProfileActivity.this, mPermissionStorageResult);
                if (takePermissionUtils.isStoragePermissionGranted()) {
                    saveQrPicture();
                } else {
                    takePermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_for_save_qr_code));
                }
            }
            break;
            case R.id.tabScanQr: {
                takePermissionUtils = new PermissionUtils(QrCodeProfileActivity.this, mPermissionCameraResult);
                if (takePermissionUtils.isCameraPermissionGranted()) {
                    moveScannerScreen();
                } else {
                    takePermissionUtils.showCameraPermissionDailog(getString(R.string.we_need_camera_permission_for_qr_scan));
                }
            }
            break;
            case R.id.tabScreenShot: {
                changeBgColorRandom();
            }
            break;
        }
    }

    private void moveScannerScreen() {
        startActivity(new Intent(QrCodeProfileActivity.this, QrCodeScannerActivity.class));
    }

    private void saveQrPicture() {
        Bitmap bitmap = Screenshot.INSTANCE.with(QrCodeProfileActivity.this)
                .setView(tabScreenShot)
                .setQuality(Quality.HIGH)
                .getScreenshot();
        String imgName = DateOprations.getCurrentDate("yyyy-MM-dd") + "screenShot.png";

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            saveAEImage(bitmap, imgName);
        } else {
            new ImageSaver(QrCodeProfileActivity.this, false).
                    setFileName(imgName).
                    setDirectoryName("Screenshots").
                    save(bitmap);
        }

    }

    private void saveAEImage(Bitmap bitmap, String imageName) {
        OutputStream outputStream;
        try {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "Screenshots");
            Uri imagePhoto = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            outputStream = resolver.openOutputStream(Objects.requireNonNull(imagePhoto));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            Objects.requireNonNull(outputStream);
            Toast.makeText(QrCodeProfileActivity.this, getString(R.string.image_save_sucessfully), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Functions.printLog(Constants.tag, "Exception : " + e);
        }
    }

    private void shareProfile(Bitmap bitmap, ResolveInfo resolveInfo) {

        Uri uri = getmageToShare(bitmap);
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        ActivityInfo activity = resolveInfo.activityInfo;
        ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                activity.name);
        intent.setComponent(name);
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent, ""));
    }

    private Uri getmageToShare(Bitmap bitmap) {
        File imagefolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imagefolder.mkdirs();
            File file = new File(imagefolder, "shared_image.png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        } catch (Exception e) {
            Log.d(Constants.tag, "Exception : " + e);
        }
        return uri;
    }

    private void changeBgColorRandom() {
        String[] colorArray = getResources().
                getStringArray(R.array.bg_color_array);

        int random = new Random().nextInt(colorArray.length);
        qrContainerBg.setBackgroundColor(Color.parseColor(colorArray[random]));
        tabScreenShot.setBackgroundColor(Color.parseColor(colorArray[random]));
    }

    @Override
    protected void onDestroy() {
        mPermissionStorageResult.unregister();
        mPermissionCameraResult.unregister();
        super.onDestroy();
    }

}