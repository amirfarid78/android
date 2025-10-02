package com.coheser.app.activitesfragments.shoping.services;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.shoping.Utils.FileUploaderProductImages;
import com.coheser.app.activitesfragments.shoping.models.AddProductModel;
import com.coheser.app.mainmenu.MainMenuActivity;
import com.coheser.app.simpleclasses.FileUtils;
import com.coheser.app.simpleclasses.Functions;

/**
 * Created by qboxus on 6/7/2018.
 */


// this the background service which will upload the video into database
public class ProductImagesService extends Service implements FileUploaderProductImages.FileUploaderCallback {

     AddProductModel dataModel=null;

    private final IBinder mBinder = new LocalBinder();


    public class LocalBinder extends Binder {
        public ProductImagesService getService() {
            return ProductImagesService.this;
        }
    }

    boolean mAllowRebind;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    SharedPreferences sharedPreferences;

    public ProductImagesService() {
        super();
    }


    @Override
    public void onCreate() {
        sharedPreferences = Functions.getSharedPreference(this);
    }


    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        // get the all the selected date for send to server during the post video

        if (intent != null && intent.getAction().equals("startservice")) {
            showNotification();

            dataModel = intent.getParcelableExtra("dataModel");

            new Thread(new Runnable() {
                @Override
                public void run() {

                    uploadFile();

                }
            }).start();


        } else if (intent != null && intent.getAction().equals("stopservice")) {
            stopForeground(true);
            stopSelf();
        }


        return Service.START_STICKY;
    }


    void uploadFile(){

        if(dataModel.getImagesList().size()>0 && dataModel.getImagesList().get(0)!=null) {
            Functions.printLog(Constants.tag,"image Uri"+dataModel.getImagesList().get(0).toString());
            File file = null;
            try {
                file=FileUtils.getFileFromUri(this, Uri.parse(dataModel.getImagesList().get(0).toString()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            dataModel.getImagesList().remove(0);
            if ( file!=null && file.exists()) {
                Functions.printLog(Constants.tag,"File exists");

                FileUploaderProductImages fileUploader = new FileUploaderProductImages(file, getApplicationContext(), dataModel);
                fileUploader.SetCallBack(this);
            } else {
                Functions.printLog(Constants.tag,"File not exists");
                uploadFile();
            }

        }
        else {
            stopForeground(true);
            stopSelf();
        }
    }




    // this will show the sticky notification during uploading video
    @SuppressLint("InlinedApi")
    private void showNotification() {

        Intent notificationIntent = new Intent(this, MainMenuActivity.class);


        PendingIntent pendingIntent=null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }else {
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        final String CHANNEL_ID = "default";
        final String CHANNEL_NAME = "Default";

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel defaultChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(defaultChannel);
        }

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle(getString(R.string.uploading_product))
                .setContentText(getString(R.string.please_wait_your_product_is_uploading))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        android.R.drawable.stat_sys_upload))
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            startForeground(101, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE);
        } else {
            startForeground(101, notification);
        }

    }


    @Override
    public void onError() {
        Functions.printLog(Constants.tag,"onError()");
        uploadFile();
    }

    @Override
    public void onFinish(String responses) {
        Functions.printLog(Constants.tag,"onFinish()"+responses);
        uploadFile();
    }

    @Override
    public void onProgressUpdate(int currentpercent, int totalpercent, String msg) {

        Functions.printLog(Constants.tag,"currentpercent:"+currentpercent+" totalpercent:"+totalpercent);
    }


}