package com.coheser.app.firebasenotification;

import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.facebook.drawee.view.SimpleDraweeView;
import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.WatchVideosActivity;
import com.coheser.app.activitesfragments.WebviewActivity;
import com.coheser.app.activitesfragments.chat.ChatActivity;
import com.coheser.app.activitesfragments.livestreaming.activities.LiveUsersActivity;
import com.coheser.app.activitesfragments.profile.ProfileActivity;
import com.coheser.app.mainmenu.MainMenuActivity;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    NotificationManagerCompat notificationManager;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable;
    Snackbar snackbar;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        NotificationHandlerBroadcast(remoteMessage.getData());
    }

    private void NotificationHandlerBroadcast(Map<String, String> data) {
        try {

            Log.d(Constants.tag, "Notification check : " + data);

            if (data.containsKey("type")) {
                showTopSnackbar(getApplicationContext(),
                        data.get("receiver_id"), data.get("title"),
                        data.get("body"), data.get("image"), data);
            } else {
                //admin Notification
                showNotification(getApplicationContext(), data.get("title"), data.get("body"), data);
            }


        } catch (Exception e) {
            Log.d(Constants.tag, "Error Notification: " + e);
        }

    }

    private void showTopSnackbar(Context context, String userId, String title, String message, String image, Map<String, String> data) {
        if (Functions.getSharedPreference(getApplicationContext()).getString(Variables.U_ID, "").equalsIgnoreCase(userId)) {
            sendBroadByName("NotificationHit",data);
            showNotification(context, title, message, data);
            if (MainMenuActivity.mainMenuActivity != null) {


                if (snackbar != null) {
                    snackbar.getView().setVisibility(View.INVISIBLE);
                    snackbar.dismiss();
                }

                if (handler != null && runnable != null) {
                    handler.removeCallbacks(runnable);
                }


                View layout = MainMenuActivity.mainMenuActivity.getLayoutInflater().inflate(R.layout.item_layout_custom_notification, null);
                TextView titletxt = layout.findViewById(R.id.username);
                TextView messagetxt = layout.findViewById(R.id.message);
                SimpleDraweeView imageView = layout.findViewById(R.id.user_image);
                titletxt.setText(title);
                messagetxt.setText(message);

                imageView.setController(Functions.frescoImageLoad(image, R.drawable.ic_user_icon, imageView, false));
                snackbar = Snackbar.make(MainMenuActivity.mainMenuActivity.findViewById(R.id.mainMenuFragment), "", Snackbar.LENGTH_LONG);

                Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
                TextView textView = snackbarLayout.findViewById(R.id.snackbar_text);
                textView.setVisibility(View.INVISIBLE);

                final ViewGroup.LayoutParams params = snackbar.getView().getLayoutParams();
                if (params instanceof CoordinatorLayout.LayoutParams) {
                    ((CoordinatorLayout.LayoutParams) params).gravity = Gravity.TOP;
                } else {
                    ((FrameLayout.LayoutParams) params).gravity = Gravity.TOP;
                }

                snackbarLayout.setPadding(0, 0, 0, 0);
                snackbarLayout.addView(layout, 0);


                snackbar.getView().setVisibility(View.INVISIBLE);

                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onShown(Snackbar sb) {
                        super.onShown(sb);
                        snackbar.getView().setVisibility(View.VISIBLE);
                    }

                });


                runnable = new Runnable() {
                    @Override
                    public void run() {
                        snackbar.getView().setVisibility(View.INVISIBLE);

                    }
                };

                handler.postDelayed(runnable, 2750);


                snackbar.setDuration(Snackbar.LENGTH_LONG);
                snackbar.show();


                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        snackbar.getView().setVisibility(View.INVISIBLE);
                        actionHandle(context, data);
                    }
                });


            }

        }
    }

    private void actionHandle(Context context, Map<String, String> data) {
        String receiver_id = data.get("receiver_id");
        String sender_id = data.get("sender_id");
        String user_id = data.get("user_id");
        String video_id = data.get("video_id");
        String image = data.get("image");
        String title = data.get("title");
        String order_id =data.get("order_id");
        String tracking_link = data.get("tracking_link");

        if (Functions.getSharedPreference(context).getString(Variables.U_ID, "").equalsIgnoreCase(receiver_id)) {
            if (data.get("type").equals("live")) {
                Intent goingIntent = new Intent(context, LiveUsersActivity.class);
                goingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(goingIntent);
            }
            else if (data.get("type").equals("follow")) {
                if (Functions.checkProfileOpenValidation(sender_id)) {
                    Intent goingIntent = new Intent(context, ProfileActivity.class);
                    goingIntent.putExtra("user_id", sender_id);
                    goingIntent.putExtra("user_name", title.replace(" started following you", ""));
                    goingIntent.putExtra("user_pic", image);
                    goingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(goingIntent);
                }
            }
            else if (data.get("type").equals("video_new_post")) {
                Intent goingIntent = new Intent(context, WatchVideosActivity.class);
                goingIntent.putExtra("video_id", video_id);
                goingIntent.putExtra("position", 0);
                goingIntent.putExtra("pageCount", 0);
                goingIntent.putExtra("userId", receiver_id);
                goingIntent.putExtra("whereFrom", "IdVideo");
                goingIntent.putExtra("video_comment", false);
                goingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(goingIntent);
            } else if (data.get("type").equals("message")) {
                Intent goingIntent = new Intent(context, ChatActivity.class);
                goingIntent.putExtra("user_id", user_id);
                goingIntent.putExtra("user_name", title);
                goingIntent.putExtra("user_pic", image);
                goingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(goingIntent);
            } else if (data.get("type").equals("comment") || data.get("type").equals("comment_like")) {
                Intent goingIntent = new Intent(context, WatchVideosActivity.class);
                goingIntent.putExtra("video_id", video_id);
                goingIntent.putExtra("position", 0);
                goingIntent.putExtra("pageCount", 0);
                goingIntent.putExtra("userId", receiver_id);
                goingIntent.putExtra("whereFrom", "IdVideo");
                goingIntent.putExtra("video_comment", true);
                goingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(goingIntent);
            }
            else if (data.get("type").equals("order_update")) {
                Intent goingIntent = new Intent(context, WebviewActivity.class);
                goingIntent.putExtra("url", tracking_link);
                goingIntent.putExtra("title", order_id);
                goingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(goingIntent);

            }
            else {
                Intent goingIntent = new Intent(context, MainMenuActivity.class);
                goingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(goingIntent);
            }

        }
    }


    public void showNotification(Context context, String title, String body, Map<String, String> data) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = new Random().nextInt(1999999991);
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        Intent actionIntent = new Intent(this, NotificationActionHandler.class);
        actionIntent.putExtra("notification_id", notificationId);
        actionIntent.putExtra("title", data.get("title"));
        actionIntent.putExtra("body", data.get("body"));
        actionIntent.putExtra("image", data.get("image"));
        actionIntent.putExtra("receiver_id", data.get("receiver_id"));
        actionIntent.putExtra("sender_id", data.get("sender_id"));
        actionIntent.putExtra("user_id", data.get("user_id"));
        actionIntent.putExtra("video_id", data.get("video_id"));
        actionIntent.putExtra("type", data.get("type"));
        actionIntent.putExtra("order_id", data.get("order_id"));
        actionIntent.putExtra("tracking_link", data.get("tracking_link"));

        PendingIntent actionpendingintent;
        if (SDK_INT >= Build.VERSION_CODES.S) {
            actionpendingintent = PendingIntent.getBroadcast(getApplicationContext(), 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            actionpendingintent = PendingIntent.getBroadcast(getApplicationContext(), 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        @SuppressLint("NotificationTrampoline") NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(actionpendingintent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setLights(Color.CYAN, 7, 7)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true);
        notificationManager.notify(notificationId, mBuilder.build());
    }

    private void sendBroadByName(String action,Map<String, String> data) {
        Intent intent = new Intent(action);
        intent.putExtra("data",new HashMap(data));
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }


}
